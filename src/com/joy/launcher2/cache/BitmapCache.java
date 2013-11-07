package com.joy.launcher2.cache;

import java.io.FileInputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.network.impl.Service;
import com.joy.launcher2.network.impl.Service.CallBack;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;

/**
 * 图片缓存处理
 * @author wanghao
 *
 */
public class BitmapCache {
	private static final String TAG = "BitmapCache";
	private static final Boolean DEBUG = false;
	private static BitmapCache cache;
	private Map<String, MySoftRef> hashRefs;
	private ReferenceQueue<Bitmap> queue;
	private Service mService;
	private class MySoftRef extends SoftReference<Bitmap> {
		public String key;
		public MySoftRef(Bitmap bmp, ReferenceQueue<Bitmap> queue,String key) {
			super(bmp, queue);
			this.key = key;
		}
	}
	
	private BitmapCache() {
		hashRefs = new Hashtable<String, MySoftRef>();
		queue = new ReferenceQueue<Bitmap>();
		try {
			mService = Service.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static BitmapCache getInstance() {
		if (cache == null) {
			cache = new BitmapCache();
		}
		return cache;
	}

	private void addCacheBitmap(String key,Bitmap bmp) {
		cleanCache();
		MySoftRef ref = new MySoftRef(bmp, queue,key);
		hashRefs.put(key, ref);
	}
	
	private void setBitmap(View view,Bitmap bm){
		if(view==null||bm==null){			
			if(DEBUG) Log.i(TAG, "--- setBitmap 图片为空！");
		}else if(view instanceof ImageView){
			ImageView iv = (ImageView)view;
			iv.setImageBitmap(bm);
		}else if(view instanceof LinearLayout){
			LinearLayout ll = (LinearLayout)view;
			ll.setBackgroundDrawable(new BitmapDrawable(bm));
		}else{
			if(DEBUG) Log.i(TAG, "---setBitmap " + view + "不是 ImageView|LinearLayout!");
		}
	}
	
	/**
	 * //从网络下载图片
	 * @param key
	 * @param imageDownLoadCallback
	 */
	public void getBitmap(final String key,final ImageDownLoadCallback imageDownLoadCallback,final ImageOption... option) {
		
		new Thread() {
			public void run() {
				Bitmap bm = mService.getBitmapByUrl(key,option);
				if(imageDownLoadCallback!=null){
					imageDownLoadCallback.imageDownLoaded(bm);
				}
			}
		}.start();
	}
	/**
	 * 获取图片 (缓存=>本地资源=>SD卡=>网络=>null)
	 * @param key
	 * @param view
	 * @param imageDownLoadCallback
	 * @param option
	 */
	public void getBitmap(final String key, final View view,final ImageDownLoadCallback imageDownLoadCallback,String suffix,final ImageOption... option) {
		
		Bitmap bm = null;
		//从队列中取
		if (hashRefs.containsKey(key)) {
			MySoftRef ref = hashRefs.get(key);
			bm = ref.get();
			if(bm!=null){
				if(DEBUG) Log.i(TAG, "---getBitmap 从缓存中获取=>"+key);
				setBitmap(view,bm);
				if(imageDownLoadCallback!=null){
					imageDownLoadCallback.imageDownLoaded(bm);
				}
				return;
			}
		}
		
		//本地资源文件中取(drawable)
		Integer resId = null;
		try{
			resId = Integer.parseInt(key);
		}catch(Exception e){
		}
		if(resId!=null){
			bm = Util.getBitmapById(resId);
			if(bm!=null){
				if(DEBUG) Log.i(TAG, "---getBitmap 从Drawable中获取=>"+key);
				addCacheBitmap(key,bm);
				setBitmap(view,bm);
				if(imageDownLoadCallback!=null){
					imageDownLoadCallback.imageDownLoaded(bm);
				}
				return;
			}
		}
		
		//从SD卡中取
		String imageName = Util.getFileNameByUrl(key)+ suffix;
		try {
			bm = BitmapFactory.decodeStream(new FileInputStream(Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName));
		}catch(OutOfMemoryError e){
			if(DEBUG) Log.e(TAG, "---getbitmap悲剧，加载文件("+Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName+")发生内存泄漏...");
		}catch (Exception e) {
		}
		if (bm != null) {
			//DsLog.i(TAG, "从SD中获取=>"+key);
			addCacheBitmap(key,bm);
			setBitmap(view,bm);
			if(imageDownLoadCallback!=null){
				imageDownLoadCallback.imageDownLoaded(bm);
			}
			return;
		}		
		
		LauncherApplication.mService.GotoNetwork(new CallBack() {
			Bitmap bitmap = null;
			@Override
			public void onPreExecute() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPostExecute() {
				// TODO Auto-generated method stub
				setBitmap(view,bitmap);
				if(imageDownLoadCallback!=null){
					imageDownLoadCallback.imageDownLoaded(bitmap);
				}
			}
			
			@Override
			public void doInBackground() {
				// TODO Auto-generated method stub
				bitmap = mService.getBitmapByUrl(key,option);
				if(bitmap!=null){
					addCacheBitmap(key,bitmap);
				}else{
					if(DEBUG) Log.i(TAG, "---getbitmap 从网络中获取失败 bitmapCache=>"+key);
				}
			}
		});

	}
	/**
	 * 默认获取图片为.png格式的（若想获取其他格式的 请用上面的方法）
	 * @param key
	 * @param view
	 * @param imageDownLoadCallback
	 * @param option
	 */
	public void getBitmap(final String key, final View view,final ImageDownLoadCallback imageDownLoadCallback,final ImageOption... option){
		getBitmap(key, view, imageDownLoadCallback, ".png", option);
	}
	private void cleanCache() {
		MySoftRef ref = null;
		while ((ref = (MySoftRef) queue.poll()) != null) {
			hashRefs.remove(ref.key);
		}
	}

	public void clearCache() {
		cleanCache();
		hashRefs.clear();
		hashRefs = null;
		System.runFinalization();
	}
}
