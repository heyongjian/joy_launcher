package com.joy.launcher2.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.R;
import com.joy.launcher2.joyfolder.JoyIconView;
//import com.joy.launcher2.ShortcutInfo;
import com.joy.launcher2.network.impl.Service;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;

/**
 * download manager
 * 
 * @author wanghao
 * 
 */
public class DownloadManager {

	final String TAG = "DownloadManager";
	
	private ExecutorService pool = Executors.newFixedThreadPool(2);

	public static Map<String, DownLoadTask> map = new HashMap<String, DownLoadTask>();

	private Service mService;

	static DownloadManager mDownloadManager;

	Context mContext;
	private DownloadManager(Context context) {
		 mContext = context;
		try {
			mService = Service.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static DownloadManager getInstances() {
		if (mDownloadManager == null) {
			mDownloadManager = new DownloadManager(LauncherApplication.mContext);
		}
		return mDownloadManager;
	}

	public static boolean isPause = false;

	public void createTask(JoyIconView view,DownloadInfo dInfo,CallBack callback,boolean secretly) {
		if (dInfo == null) {
			return;
		}

		int id = dInfo.getId();
		// 已经在下载了
		DownLoadTask task = getDowmloadingTask(id);
		if (task != null) {
			Log.i(TAG, "--------> 已经在下载了！！！");
			return;
		}
//		DownloadInfo dInfo = null;
		// 首先从数据库里读取，是否有未完成的下载
		// dInfo = dbHelper.get(id);暂不支持断点下载
		Log.i(TAG, "-----dInfo---> " + dInfo);
		
		// 检查本地是否有重名了的文件
		File localfile = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getFilename());
		localfile = Util.getCleverFileName(localfile);
		dInfo.setLocalname(localfile.getName());

		dInfo.setView(view);

		// 创建线程开始下载
		File file = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getLocalname());

		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(file, "rwd");
			// 从断点处 继续下载（初始为0）
			rf.seek(dInfo.getCompletesize());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DownLoadTask downloader = new DownLoadTask(dInfo, rf,callback,secretly);
		// 加入map
		map.put(String.valueOf(id), downloader);
		// 加入线程池
		pool.execute(downloader);

	}
	// 下载子线程
	public class DownLoadTask extends Thread {

		private boolean isSecretly;//下载方式 false正常  true静默下载
		private RandomAccessFile randomAccessFile;
		private DownloadInfo downinfo;
		CallBack callback;
		public DownLoadTask(DownloadInfo downinfo,RandomAccessFile randomAccessFile,CallBack callback,boolean secretly) {
			this.downinfo = downinfo;
			this.randomAccessFile = randomAccessFile;
			this.callback = callback;
			isSecretly = secretly;
			sendBroadcast(downinfo,UPDATE_UI);
		}
		public DownloadInfo getDownloadInfo(){
			return downinfo;
		}
		public boolean isSecretly(){
			return isSecretly;
		}
		public void run() {
			InputStream is = null;
			try {
//				long begin = downinfo.getCompletesize();
				is = mService.getDownLoadInputStream(downinfo.getUrl());
				if (is == null) {
					return;
				}
				final int length = 1024;
				byte[] b = new byte[length];
				int len = -1;
				int pool = 0;

				boolean isover = false;
				long startime = System.currentTimeMillis();
				Log.i(TAG, "-----downinfo starttime--->1 " + startime);
				while ((len = is.read(b))!=-1) {
					if (isPause) {
						return;
					}

					randomAccessFile.write(b, 0, len);
					
					pool += len;
					if (pool >= 100 * 1024) { // 100kb写一次数据库
						Log.i(TAG, "-----下载  未完成----");
						// dbHelper.update(downinfo); 暂不支持断点下载
						pool = 0;
						sendBroadcast(downinfo,UPDATE_UI);// 刷新一次
					}
					int tempLen = pool/1024;
					downinfo.setCompletesize(tempLen);
					if (pool != 0) {
						// dbHelper.update(downinfo); 暂不支持断点下载
					}
				}
				long endtime = System.currentTimeMillis();
				Log.i(TAG, "-----downinfo time--->1 " + (endtime - startime));
			} catch (Exception e) {
				Log.i(TAG, "-------->e " + e);
			} finally {
				// end.countDown();
				Log.i(TAG, "---over----");

				if (downinfo.getCompletesize() >= downinfo.getFilesize()) {
					Log.i(TAG, "----finsh----");
					DownLoadDBHelper.getInstances().update(downinfo);
					if(callback != null){
						callback.downloadSucceed();
						sendBroadcast(downinfo,UPDATE_UI);// 刷新一次
					}
				}else{
					callback.downloadFailed();
					sendBroadcast(downinfo,UPDATE_UI);// 刷新一次
					sendBroadcast(downinfo,DOWNLOAD_ERROR);// 网络错误
				}

				map.remove(String.valueOf(downinfo.getId()));
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
				if (randomAccessFile != null) {
					try {
						randomAccessFile.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		public void sendBroadcast(Object obj,int what) {
			if (!isSecretly) {
				Message msg = new Message();
				msg.what = what;
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		}
	}
	public DownLoadTask getDowmloadingTask(int id){
		DownLoadTask task = map.get(String.valueOf(id));
		if (task != null) {
			return task;
		}
		return null;
	}
	public boolean isCompleted(int id){
		final DownloadInfo dInfo = DownLoadDBHelper.getInstances().get(id);
		if (dInfo!= null&&dInfo.getCompletesize() >= dInfo.getFilesize()) {
			return true;
		}
		return false;
	}

	public interface CallBack{
		public void downloadSucceed();
		public void downloadFailed();
	}
	final int UPDATE_UI = 0;
	final int DOWNLOAD_ERROR = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			DownloadInfo td = (DownloadInfo) msg.obj;
			int what = msg.what;
			switch(what){
			case UPDATE_UI:
				View view = td.getView();
				if (view != null) {
					view.invalidate();
				}
				break;
			case DOWNLOAD_ERROR:
				CharSequence errorStrings =  mContext.getResources().getText(R.string.download_error);
				Toast.makeText(mContext, errorStrings, Toast.LENGTH_LONG).show();
				break;
			case 2:
				break;
			}
		}
	};

	public void onDestroy() {
		pool.shutdown();
	}
}
