package com.joy.launcher2.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver{

	
	private final static String TAG = "PushReceiver";
	private final static boolean DEBUG = true;
	@Override
	public void onReceive(final Context context, final Intent intent) {
		PushWakeLock.acquireCpuWakeLock(context);
		
		String action = intent.getAction();
		if(DEBUG)Log.e(TAG, "onReceive action = " + action);
		if(PushUtils.PUSH_ACTION.equals(action))
		{
			int pushType = intent.getIntExtra(PushUtils.PUSH_TYPE, PushUtils.PUSH_LIST_TYPE);
			PushUtils.startPushService(context, PushUtils.PUSH_ACTION, pushType);
		}
		else if(PushUtils.PUSH_DOWNLOAD_ACTION.equals(action))
		{
			Intent download = new Intent(action);
			download.putExtra(PushUtils.PUSH_DETAIL_INFO, intent.getBundleExtra(PushUtils.PUSH_DETAIL_INFO));
			PushUtils.startPushService(context, download);
		}
		
	}

}
