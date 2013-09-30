package com.joy.launcher2.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class PushUtils {

	public final static String PUSH_ACTION = "com.joy.launcher2.push";
	public final static String PUSH_DOWNLOAD_ACTION = "com.joy.launcher2.push.download";
	public final static String PUSH_REMIND_ACTION = "com.joy.launcher2.push.remind";
	public final static String PUSH_PREFERENCES = "push_preferences";
	public final static String PUSH_START_HOUR = "push_start_hour";
	public final static String PUSH_END_HOUR = "push_end_hour";
	public final static String PUSH_FIRST_INTERVAL = "push_first_interval";
	public final static String PUSH_FIRST = "push_first";
	public final static String PUSH_CURRENT_DAY = "push_current_day";
	public final static String PUSH_SETTINGS = "push_settings";
	public final static String PUSH_LIST_TIME_INTERVAL = "push_list_time_interval";
	public final static String PUSH_TIME_INTERVAL = "push_time_interval";
	public final static String PUSH_LIST = "push_list";
	public final static String PUSH_CURRENT_DAY_NUM = "push_current_day_num";
	public final static String PUSH_CURRENT_DAY_NUM_MAX = "push_current_day_num_max";
	public final static String PUSH_LIST_NEXT_TIME = "push_list_next_time";
	public final static String PUSH_NEXT_TIME = "push_next_time";
	public final static String ACTIVATE = "activate";
	public final static int PUSH_DEFAULT_INT = -1;
	public final static String PUSH_DEFAULT_STR = "{}";
	
	public final static int PUSH_NONE_TYPE = -1;
	public final static int PUSH_SETTINGS_TYPE = 0;
	public final static int PUSH_ONE_MESSAGE_TYPE = 1;
	public final static int PUSH_LIST_TYPE  = 2;
	public final static String PUSH_TYPE = "push_type";
	
	public final static int PUSH_DETAIL_LINK_TYPE = 1;
	public final static int PUSH_DETAIL_TEXT_TYPE = 2;
	public final static int PUSH_DETAIL_SILENT_DOWNLOAD_TYPE = 3;
	public final static int PUSH_DETAIL_DOWNLOAD_REMIND = 4;
	public final static int PUSH_DETAIL_DOWNLOAD_NO_REMIND = 5;
	
	public final static String PUSH_DETAIL_INFO = "info";
	public final static String PUSH_DETAIL_TYPE = "type";
	public final static String PUSH_DETAIL_TITLE = "title";
	public final static String PUSH_DETAIL_DESCRIPTION = "description";
	public final static String PUSH_DETAIL_ICON = "icon";
	public final static String PUSH_DETAIL_ID = "id";
	public final static String PUSH_DETAIL_SIZE = "size";
	public final static String PUSH_DETAIL_PACKAGE_NAME = "packageName";
	public final static String PUSH_DETAIL_URL = "url";
	
	public static void startPollingBroadcast(Context context, int seconds, String action) {
		
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		long triggerAtTime = System.currentTimeMillis();
		manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
				seconds * 1000, pendingIntent);
		
	}
	
	public static void startPushBroacast(Context context, long triggerAtTime, String action, int pushType)
	{
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(action);
		intent.putExtra(PUSH_TYPE, pushType);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
	}

	public static void stopPushBroadcast(Context context, String action) {
		
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		manager.cancel(pendingIntent);
	}
	
	public static void startPushService(Context context, String action, int pushType)
	{
		Intent intent = new Intent(action);
		intent.putExtra(PUSH_TYPE, pushType);
		context.startService(intent);
	}
	
	public static void startPushService(Context context, Intent intent)
	{
		context.startService(intent);
	}
}
