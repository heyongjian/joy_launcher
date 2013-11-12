package com.joy.launcher2.push;


import java.util.List;

import com.joy.launcher2.LauncherApplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class PushBootOrUserBroadcastReceiver extends BroadcastReceiver{

	private final static String TAG = "PushBootOrUserBroadcastReceiver";
	private final static boolean DEBUG = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(DEBUG)Log.e(TAG, "action:"+action);
		
		PushAsyncHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isServiceRunning(LauncherApplication.mContext, "com.joy.launcher2", "com.joy.launcher2.push.PushService"))
				{
					SharedPreferences sp = LauncherApplication.mContext.getSharedPreferences(PushUtils.PUSH_PREFERENCES, 0);
					long nextTime = sp.getLong("next_time", -1);
					int nextPushType = sp.getInt("push_type", PushUtils.PUSH_SETTINGS_TYPE);
					if(DEBUG)Log.e(TAG,"start service : " + nextTime);
					PushUtils.startPushBroacast(LauncherApplication.mContext, nextTime, PushUtils.PUSH_ACTION, nextPushType);
				}
			}
		});
	}
	
	/**
	 * 用来判断服务是否运行.
	 * @param context
	 * @param className 判断的服务名字
	 * @return true 在运行 false 不在运行
	*/
	public static boolean isServiceRunning(Context context, String packageName, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager)
				context.getSystemService(Context.ACTIVITY_SERVICE); 
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
		if (!(serviceList.size()>0)) {
			return false;

		}
		for (int i=0; i<serviceList.size(); i++) {
			ComponentName cpn = serviceList.get(i).service;
			
			if(cpn != null)
			{
				String pn = cpn.getPackageName();
				String cn = cpn.getClassName();
				if(packageName.equals(pn) && className.equals(cn))
				{
					isRunning = true;
					break;
				}
			}
		}
	
		return isRunning;
	}

}
