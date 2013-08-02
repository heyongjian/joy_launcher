//package com.joy.launcher.test;
//
//import android.app.Application;
//import android.content.Context;
//
//import com.joy.launcher.cache.BitmapCache;
//import com.joy.launcher.network.impl.Service;
//
//public class MApplication extends Application{
//	public static Service mService;
//	public static Context mContext;
//	public static BitmapCache mBcache ;
//	@Override
//	public void onCreate() {
//		// TODO Auto-generated method stub
//		super.onCreate();
//		
//		mContext = this;
//		mBcache = BitmapCache.getInstance();
//		try {
//			mService = Service.getInstance();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
