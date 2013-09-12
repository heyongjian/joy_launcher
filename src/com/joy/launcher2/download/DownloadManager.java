package com.joy.launcher2.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.R;
import com.joy.launcher2.ShortcutInfo;
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

	static DownLoadDBHelper dbHelper;
	static DownloadManager mDownloadManager;

	Context mContext;
	private DownloadManager(Context context) {
		 dbHelper = new DownLoadDBHelper(context);
		 mContext = context;
		try {
			mService = Service.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

	// 创建下载任务
	public DownloadInfo createTask(View view, CallBack callback) {
		ShortcutInfo info = (ShortcutInfo)view.getTag();
		DownloadInfo downinfo = info.getDownLoadInfo();
		
		String name = downinfo.getFilename();
		int id = downinfo.getId();
		int fileSize = downinfo.getFilesize();
		
		return createTask(view, name, id, fileSize,callback);
	}
	// 创建下载任务
	public DownloadInfo createTask(View view, String name, int id, int fileSize,CallBack callback) {

		Log.i(TAG,"-------->map.get(String.valueOf(id)  11=="
						+ map.get(String.valueOf(id)));
		Log.i(TAG, "-------->map.size()  11==" + map.size());

		// 已经在下载了
		DownLoadTask task = map.get(String.valueOf(id));
		if (task != null) {
			Log.i(TAG, "--------> 已经在下载了！！！");
			return null;
		}
		DownloadInfo dInfo = null;
		// 首先从数据库里读取，是否有未完成的下载
		// dInfo = dbHelper.get(id);暂不支持断点下载
		Log.i(TAG, "-----dInfo---> " + dInfo);
		// 没有未完成的下载，新建下载任务
		if (dInfo == null) {
			// 创建下载
			dInfo = new DownloadInfo();
			dInfo.setId(id);
			dInfo.setFilename(name);
			dInfo.setLocalname(name);
			dInfo.setUrl("null");
			dInfo.setCompletesize(0);
			dInfo.setFilesize(fileSize);
			dbHelper.insert(dInfo);

			// 检查本地是否有重名了的文件
			File file = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getFilename());
			file = Util.getCleverFileName(file);
			dInfo.setLocalname(file.getName());
		}
		dInfo.setView(view);
		((ShortcutInfo)view.getTag()).setDownLoadInfo(dInfo);

//		Log.i(TAG, "-----dInfo---> " + dInfo);

		// 创建线程开始下载
		File file = new File(Constants.DOWNLOAD_APK_DIR + "/"
				+ dInfo.getLocalname());

		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(file, "rwd");
			// 从断点处 继续下载（初始为0）
			rf.seek(dInfo.getCompletesize());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DownLoadTask downloader = new DownLoadTask(dInfo, rf,callback);
		// 加入map
		map.put(String.valueOf(id), downloader);
		// 加入线程池
		pool.execute(downloader);
		
		return dInfo;
	}

	// 下载子线程
	class DownLoadTask extends Thread {

		private RandomAccessFile randomAccessFile;
		private DownloadInfo downinfo;
		CallBack callback;
		public DownLoadTask(DownloadInfo downinfo,RandomAccessFile randomAccessFile,CallBack callback) {
			this.downinfo = downinfo;
			this.randomAccessFile = randomAccessFile;
			this.callback = callback;
			sendBroadcast(downinfo,UPDATE_UI);
		}

		public void run() {
			InputStream is = null;
			try {
//				long begin = downinfo.getCompletesize();
				is = mService.getDownLoadInputStream();
				Log.i(TAG, "-----is--11111-> " + is);
				if (is == null) {
					return;
				}
//				final int length = 4096;
				final int length = 1024;
				byte[] b = new byte[length];
				int len = -1;
				int pool = 0;

				Log.i(TAG,"-----Completesize()--->1 "+ downinfo.getCompletesize());
				Log.i(TAG, "-----Filesize()--->1 " + downinfo.getFilesize());

				boolean isover = false;
				long startime = System.currentTimeMillis();
				Log.i(TAG, "-----downinfo starttime--->1 " + startime);
				while ((len = is.read(b))!=-1) {
					if (isPause) {
						return;
					}
//					len = is.read(b);

					randomAccessFile.write(b, 0, len);

					downinfo.setCompletesize(downinfo.getCompletesize() + len);
//					Log.i(TAG, "-----下载downinfo.getCompletesize()----"+ downinfo.getCompletesize());
					
					pool += len;
					if (pool >= 100 * 1024) { // 100kb写一次数据库
						Log.i(TAG, "-----下载  未完成----");
						// dbHelper.update(downinfo); 暂不支持断点下载
						pool = 0;
						sendBroadcast(downinfo,UPDATE_UI);// 刷新一次
					}
					// 最后再写一次
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
				Log.i(TAG,"-----Completesize()--->1 "+ downinfo.getCompletesize());
				Log.i(TAG, "-------结束-------");

				if (downinfo.getCompletesize() == downinfo.getFilesize()) {
					Log.i(TAG, "-----下载  完成----");
					if(callback != null){
						callback.DownloadSucceed();
						((ShortcutInfo)downinfo.getView().getTag()).setDownLoadInfo(null);
						sendBroadcast(downinfo,UPDATE_UI);// 刷新一次
					}
				}else{
					if (downinfo.getCompletesize() > downinfo.getFilesize()) {
						Log.i(TAG, "-----下载的apk超出实际大小,请重新下载----");
						// dbHelper.delete(downinfo);暂不支持断点下载
					} else {
						
					}
					((ShortcutInfo)downinfo.getView().getTag()).setDownLoadInfo(null);
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


	}

	public interface CallBack{
		public void DownloadSucceed();
	}
	final int UPDATE_UI = 0;
	final int DOWNLOAD_ERROR = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			DownloadInfo td = (DownloadInfo) msg.obj;
			int what = msg.what;
			switch(what){
			case UPDATE_UI://更新ui
				td.getView().invalidate();
				Log.i(TAG, "-------->下载中----"+td.getView());
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

	// 发送广播
	public void sendBroadcast(Object obj,int what) {
		Message msg = new Message();
		msg.what = what;
		msg.obj = obj;

		mHandler.sendMessage(msg);
	}

	
	public DownloadInfo getFromDB(int id){
		DownloadInfo info = dbHelper.get(id);
		return info;
	}
	public void onDestroy() {
		// 关闭线程池
		pool.shutdown();
	}
}
