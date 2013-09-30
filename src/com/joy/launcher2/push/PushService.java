package com.joy.launcher2.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.joy.launcher2.R;
import com.joy.launcher2.network.impl.ProtocalFactory;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiConfiguration.Protocol;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class PushService extends Service{

	private final static String TAG = "PushService";
	private final static boolean DEBUG = true;
	
	private final static int MAX_ONE_DAY_COUNT = 5;
	private final static int MIN_HOUR = 8;
	private final static int MAX_HOUR = 22;
	private final static long ONE_DAY_LONG = 24 * 60 * 60 * 1000;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(DEBUG)Log.e(TAG, "create service" );
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(DEBUG)Log.e(TAG, "onDestroy");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		
		super.onStart(intent, startId);
		if (intent == null) {
			if(DEBUG)Log.e(TAG, "the intent of service is null , stop.");
            stopSelf();
            PushWakeLock.releaseCpuLock();
            return;
		}
		String action = intent.getAction();
		WakeLock wakelock = PushWakeLock.createPartialWakeLock(getApplicationContext());
		wakelock.acquire();
		if(DEBUG)Log.e(TAG, "start service (action = " + action+ ")");
		if(PushUtils.PUSH_ACTION.equals(action))
		{
			int pushType = intent.getIntExtra(PushUtils.PUSH_TYPE, PushUtils.PUSH_NONE_TYPE);
			
			PushAsyncHandler.post(new PushTask(getApplicationContext(), pushType, wakelock));
		}
		else if(PushUtils.PUSH_DOWNLOAD_ACTION.equals(action))
		{
			Bundle bundle = intent.getBundleExtra(PushUtils.PUSH_DETAIL_INFO);
			PushAsyncHandler.post(new DownloadAPK(getApplicationContext(), bundle, wakelock));
		}
		else 
		{
			if(wakelock != null)
			{
				wakelock.release();
				wakelock = null;
			}
		}
		
		PushWakeLock.releaseCpuLock();
		
	}
	
	
	static class DownloadAPK implements Runnable
	{

		Context mContext;
		WakeLock mWakeLock ;
		Bundle mBundle;
		Notification mNotification;
		NotificationManager mNotificationManager;
		
		public DownloadAPK(Context context, Bundle bundle, WakeLock wakeLock)
		{
			mContext = context;
			mWakeLock = wakeLock;
			mBundle = bundle;
			mNotificationManager = (NotificationManager)    
					mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);   
		}
		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int type = mBundle.getInt(PushUtils.PUSH_DETAIL_TYPE);
			String title = mBundle.getString(PushUtils.PUSH_DETAIL_TITLE);
			String description = mBundle.getString(PushUtils.PUSH_DETAIL_DESCRIPTION);;
			String packageName = mBundle.getString(PushUtils.PUSH_DETAIL_PACKAGE_NAME);;
			Bitmap icon = (Bitmap)mBundle.getParcelable(PushUtils.PUSH_DETAIL_ICON);;
			String url = mBundle.getString(PushUtils.PUSH_DETAIL_URL);
			String name = packageName + ".apk";
			int id = mBundle.getInt(PushUtils.PUSH_DETAIL_ID);
			int size = mBundle.getInt(PushUtils.PUSH_DETAIL_SIZE) * 1024;
			int completeSize = 0;
			
			int progress = 0;
			boolean isSilent = (type == PushUtils.PUSH_DETAIL_SILENT_DOWNLOAD_TYPE);
			if(!isSilent && mNotification == null)
			{
				mNotification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
				mNotification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.push_notificaticon);
				mNotification.contentView.setViewVisibility(R.id.push_icon, View.VISIBLE);
				mNotification.contentView.setViewVisibility(R.id.push_download_progressBar, View.VISIBLE);
				mNotification.contentView.setViewVisibility(R.id.push_text, View.VISIBLE);
				mNotification.contentView.setViewVisibility(R.id.push_text1, View.GONE);
				mNotification.contentView.setTextViewText(R.id.push_text, title);
				mNotification.contentView.setImageViewBitmap(R.id.push_icon, icon);
				mNotification.contentView.setProgressBar(R.id.push_download_progressBar, 100, progress, false);
				mNotification.contentIntent = null;
				mNotification.flags |= Notification.FLAG_NO_CLEAR;
				mNotificationManager.notify(id, mNotification);
			}
			
			if(DEBUG)Log.e(TAG, "start download APK");
			boolean success = false;
 			//if(size > 0 && url != null && packageName != null)
			if(size > 0)
			{
 				//download apk
				String path =  Constants.DOWNLOAD_APK_DIR + "/"+name;
 				InputStream is = null;
 				FileOutputStream fos = null;
 				try {
 					is = com.joy.launcher2.network.impl.Service.getInstance().getDownLoadPushApkInputStream(url);
 					
 					final int length = 1024;
 					byte[] b = new byte[length];
 					int len = -1;
 					
 					File file = new File(path).getParentFile();
 					if (!file.exists()) {
 						file.mkdirs();
 					}
 					fos = new FileOutputStream(path);
 					while ((len = is.read(b))!=-1) {
 						fos.write(b, 0, len);
 						if(!isSilent)
 						{
 							completeSize += len;
 							int oldProgress = progress;
 							
 							progress = Math.min((int)((completeSize / (float)size) * 100), 100);
 							if(progress != oldProgress)
 							{
 								if(DEBUG)Log.e(TAG, "download apk progress = " + progress);
 								mNotification.contentView.setProgressBar(R.id.push_download_progressBar, 100, progress, false);
 								mNotificationManager.notify(id, mNotification);
 							}
 						}
 					}
 					success = true;
 					
 				}
 				catch(Exception e)
 				{
 					
 				}
 				finally
 				{
 					
 					try {
 						if (fos != null)fos.close();
 						if(is != null)is.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 				if(success)
 				{
 					Util.installAPK(Constants.DOWNLOAD_APK_DIR, name, false);
 					if(!isSilent)
 					{
 						//mNotificationManager.cancel(id);
 					}
 				}
 				else
 				{
 					Log.e(TAG, "download apk failed");
 					if(!isSilent)
 					{
 						
 						//mNotificationManager.cancel(id);
 					}
 					else
 					{
 					}
 				}
 				
			}
			else
			{
				
			}
			
			if(!isSilent && mNotification != null)mNotificationManager.cancel(id);
			
			if(DEBUG)Log.e(TAG, "Download APK success = " + success);
			if(mWakeLock != null)
			{
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		
	}
	
	static class PushTask implements Runnable
	{

		Context mContext;
		int mPushType;
		WakeLock mWakeLock ;
		
		public PushTask(Context context, int pushType,WakeLock wakeLock)
		{
			mContext = context;
			mPushType = pushType;
			mWakeLock = wakeLock;
		}
		
		@Override
		public void run() {
			//step 1
			
			SharedPreferences sp = mContext.getSharedPreferences(PushUtils.PUSH_PREFERENCES, 0);
			//int pushStartHour = sp.getInt(PushUtils.PUSH_START_HOUR, 0);
			//int pushEndHour = sp.getInt(PushUtils.PUSH_END_HOUR, 24);
			long pushFirstInterval = sp.getLong(PushUtils.PUSH_FIRST_INTERVAL,  30 * 60 * 1000);
			boolean pushFirst = sp.getBoolean(PushUtils.PUSH_FIRST, true);
			String pushCurrentDay = sp.getString(PushUtils.PUSH_CURRENT_DAY, PushUtils.PUSH_DEFAULT_STR);
			int pushCurrentDayNum = sp.getInt(PushUtils.PUSH_CURRENT_DAY_NUM, 0);
			int pushCurrentDayNumMax = sp.getInt(PushUtils.PUSH_CURRENT_DAY_NUM_MAX, 5);
			String pushSettings = sp.getString(PushUtils.PUSH_SETTINGS, PushUtils.PUSH_DEFAULT_STR);
			String pushList = sp.getString(PushUtils.PUSH_LIST, PushUtils.PUSH_DEFAULT_STR); 
			long pushListNextTime = sp.getLong(PushUtils.PUSH_LIST_NEXT_TIME, -1);
			long pushNextTime = sp.getLong(PushUtils.PUSH_NEXT_TIME, -1);
			int pushListTimeInterval = sp.getInt(PushUtils.PUSH_LIST_TIME_INTERVAL, 1800);
			int pushTimeInterval = sp.getInt(PushUtils.PUSH_TIME_INTERVAL, 1800);
			boolean activate = sp.getBoolean(PushUtils.ACTIVATE, false);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			Calendar calendar = Calendar.getInstance();
			
			long currentSystemTime = System.currentTimeMillis();
			
			long nextTime = -1;
			int nextPushType = PushUtils.PUSH_NONE_TYPE;
			boolean canNext = false;
			boolean isNetworkOK = Util.isNetworkConnected();
			if(isNetworkOK && !activate)
			{
				if(DEBUG)Log.e(TAG, "activate launcher start");
				int count = 0;
				while(count < 3 && !activate)
				{
					count++;
					try {
						activate = com.joy.launcher2.network.impl.Service.getInstance().activateLauncher();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(DEBUG)Log.e(TAG, "activate = " + activate);
				
				if(!activate)
				{
					pushFirst = true;
					mPushType = PushUtils.PUSH_SETTINGS_TYPE;
				}
				
			}
			if(isNetworkOK && activate)
			{
				//获取网络时间
				long networkTime = getNetworkTime();
				if(networkTime != -1)
				{
					canNext = true;
				}
				else
				{
					canNext = false;
				}
				calendar.setTimeInMillis(networkTime);
				String networkCurrentDay = sdf.format(new Date(networkTime));
				//int networkCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
				if(DEBUG)Log.e(TAG, "1 : get network time: " + networkCurrentDay);
				
				//判断是否是当前天，不是当前天需清除昨天记录
				if(!networkCurrentDay.equals(pushCurrentDay) && canNext)
				{
					if(DEBUG)Log.e(TAG, "push settings start");
					//获得push settings
					pushCurrentDayNum = 0;
					JSONObject settingsJson = null;
					try {
						settingsJson = com.joy.launcher2.network.impl.Service
								.getInstance()
								.getPushSettings();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//解析push settings json
					if(settingsJson != null)
					{
						pushSettings = settingsJson.toString();
						try {
							int stateSettings = settingsJson.getInt("state");
							if(stateSettings == -4 || stateSettings == -5)
							{
								activate = false;
								
							}
							JSONObject item = settingsJson.getJSONObject("item");
							pushTimeInterval = 
									item.isNull("aiirpush_unittime")? 1800 : item.getInt("aiirpush_unittime") * 60 ;
							pushListTimeInterval = 
									item.isNull("heart_beat_time")? 1800 : item.getInt("heart_beat_time");
							pushCurrentDayNumMax = 
									item.isNull("max_recieive_num")? 5 : item.getInt("max_recieive_num");
							
							pushFirstInterval = 
									item.isNull("first_receivetime_unit")? 60 * 1000 : item.getInt("first_receivetime_unit")  * 60 * 1000;
							
							if(item.isNull("aiirpush_unittime")
									|| item.isNull("heart_beat_time")
									|| item.isNull("max_recieive_num") 
									|| item.isNull("first_receivetime_unit"))
							{
								pushCurrentDay = PushUtils.PUSH_DEFAULT_STR;
								canNext = false;
							}
							else
							{
								if(DEBUG)Log.e(TAG, "push settings success");
								pushCurrentDay = networkCurrentDay;
								canNext = true;
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							pushCurrentDay = PushUtils.PUSH_DEFAULT_STR;
							canNext = false;
						}
						
					}
					else
					{
						pushCurrentDay = PushUtils.PUSH_DEFAULT_STR;
						canNext = false;
					}
				}
				if(DEBUG)Log.e(TAG, "2:current time: " + pushCurrentDay);
				
				//当获取当天网络时间正确或者获取push设置正确时开始获取push list
				if(mPushType == PushUtils.PUSH_LIST_TYPE && activate)
				{
					if(DEBUG)Log.e(TAG, "push list start:" + pushList);
					JSONObject listJson = null;
					JSONObject nativeListJson = null;
					try {
						
						nativeListJson = new JSONObject(pushList);
						if(getNotPushNum(nativeListJson) < pushCurrentDayNumMax)
						{
							listJson = com.joy.launcher2.network.impl.Service
									.getInstance()
									.getPushList();
							int stateList = listJson.getInt("state");
							if(stateList == -4 || stateList == -5)
							{
								activate = false;
								
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(listJson != null && nativeListJson != null)
					{
						copyJsonItem(listJson, nativeListJson);
						//保证最多16条
						ensureMaxNum(nativeListJson);
						pushList = nativeListJson.toString();
					}
				}
				
				//push一条消息
				
				if(
						(mPushType == PushUtils.PUSH_ONE_MESSAGE_TYPE || mPushType == PushUtils.PUSH_LIST_TYPE) 
						&& pushCurrentDayNum < pushCurrentDayNumMax && activate)
				{
					if(DEBUG)Log.e(TAG, "push message start");
					JSONObject pushListJson = null;
					try {
						pushListJson = new JSONObject(pushList);
						JSONArray items = pushListJson.getJSONArray("item");
						int id = -1;
						int index = -1;
					    for(int i = 0; i < items.length(); i++)
					    {
					    	JSONObject item = items.getJSONObject(i);
					    	id = item.getInt("id");
					    	boolean isPushed = item.getBoolean("isPushed");
					    	if(!isPushed)
					    	{
					    		index = i;
					    		break;
					    	}
					    }
						if(id >= 0 && index >= 0)
						{
							
							JSONObject detailJson = com.joy.launcher2.network.impl.Service
									.getInstance()
									.getPushDetail(id);
							int stateDetail = detailJson.getInt("state");
							if(stateDetail == -4 || stateDetail == -5)
							{
								activate = false;
								
							}
							JSONObject detail = detailJson.getJSONObject("item");
							int flag =  detail.isNull("flag")?0:detail.getInt("flag");
							String iconUrl =  detail.isNull("icon")?null:detail.getString("icon");
							Bitmap icon = null;
							try
							{
								icon = com.joy.launcher2.network.impl.Service.getInstance().getDownLoadPushImage(iconUrl);
							}
							catch(Exception e)
							{
								
							}
							if(icon == null)
							{
								icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
							}
							String title =  detail.isNull("title")?null:detail.getString("title");
							String introduce =  detail.isNull("introduce")?null:detail.getString("introduce");
							String packageName =  detail.isNull("packageName")?null:detail.getString("packageName");
							int type =  detail.isNull("type")?0:detail.getInt("type");
							String url = detail.isNull("url")?null:detail.getString("url");
							int size = 	detail.isNull("size")?0:detail.getInt("size");
						    String site = detail.isNull("site")?null:detail.getString("site");
							Bundle bundle = new Bundle();
							bundle.putInt(PushUtils.PUSH_DETAIL_TYPE, type);
							bundle.putString(PushUtils.PUSH_DETAIL_TITLE, title);
							bundle.putString(PushUtils.PUSH_DETAIL_DESCRIPTION, introduce);
							bundle.putString(PushUtils.PUSH_DETAIL_PACKAGE_NAME, packageName);
							bundle.putInt(PushUtils.PUSH_DETAIL_ID, id);
							bundle.putInt(PushUtils.PUSH_DETAIL_SIZE, size);
							bundle.putString(PushUtils.PUSH_DETAIL_URL, url);
							bundle.putParcelable(PushUtils.PUSH_DETAIL_ICON, icon);
							//通知
							if(type == PushUtils.PUSH_DETAIL_SILENT_DOWNLOAD_TYPE)
							{
								WakeLock wakeLock = PushWakeLock.createPartialWakeLock(mContext);
								wakeLock.acquire();
								PushAsyncHandler.post(new DownloadAPK(mContext, bundle, wakeLock));
							}
							else if(type >= PushUtils.PUSH_DETAIL_LINK_TYPE && type <= PushUtils.PUSH_DETAIL_DOWNLOAD_NO_REMIND)
							{
								NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
								Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
								notification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.push_notificaticon);
								notification.contentView.setViewVisibility(R.id.push_icon, View.VISIBLE);
								notification.contentView.setViewVisibility(R.id.push_text1, View.VISIBLE);
								notification.contentView.setViewVisibility(R.id.push_text, View.VISIBLE);
								notification.contentView.setImageViewBitmap(R.id.push_icon, icon);
								notification.contentView.setTextViewText(R.id.push_text, title);
								notification.contentView.setTextViewText(R.id.push_text1, introduce);
								notification.flags |= Notification.FLAG_AUTO_CANCEL;
								Intent notificationIntent = new Intent();
								notificationIntent.putExtra(PushUtils.PUSH_DETAIL_INFO, bundle);
								if(type == PushUtils.PUSH_DETAIL_LINK_TYPE)
								{
									notificationIntent.setData(Uri.parse(site));
									notificationIntent.setAction(Intent.ACTION_VIEW);
									notificationIntent.addCategory(Intent.CATEGORY_BROWSABLE);
									notification.contentIntent = PendingIntent.getActivity(
											mContext,
											id, 
											notificationIntent,
											PendingIntent.FLAG_CANCEL_CURRENT);
								}
								else if(type == PushUtils.PUSH_DETAIL_TEXT_TYPE)
								{
									notificationIntent.setAction(PushUtils.PUSH_REMIND_ACTION);
									notification.contentIntent = PendingIntent.getActivity(
											mContext, 
											id, 
											notificationIntent, 
											PendingIntent.FLAG_CANCEL_CURRENT);
								}
								else if(type == PushUtils.PUSH_DETAIL_DOWNLOAD_REMIND)
								{
									notificationIntent.setAction(PushUtils.PUSH_REMIND_ACTION);
									notification.contentIntent = PendingIntent.getActivity(
											mContext, 
											id, 
											notificationIntent, 
											PendingIntent.FLAG_CANCEL_CURRENT);
								}
								else
								{
									notificationIntent.setAction(PushUtils.PUSH_DOWNLOAD_ACTION);
									
									notification.contentIntent = PendingIntent.getBroadcast(mContext, id, notificationIntent, 0);
								}
								nm.notify(id, notification);
							}
							
							JSONObject item = items.getJSONObject(index);
							item.put("isPushed", true);
							pushList = pushListJson.toString();
							pushCurrentDayNum++;
							if(DEBUG)Log.e(TAG, "push one message : " + id  + "  " + title);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				
			}
			
			if(pushFirst && mPushType == PushUtils.PUSH_SETTINGS_TYPE)
			{
				nextTime = pushNextTime  = pushListNextTime = currentSystemTime + pushFirstInterval;
				if(canNext)
				{
					
					nextPushType = PushUtils.PUSH_LIST_TYPE;
					pushFirst = false;
				}
				else
				{
					nextPushType = PushUtils.PUSH_SETTINGS_TYPE;
					pushFirst = true;
				}
				if(DEBUG)Log.e(TAG, "canNext = " + canNext + "   pushFirst = " + pushFirst);
				
			}
			else
			{
				boolean isPushListFull = false;
				boolean isCurrentDayNeedToPush = true;
				if(pushCurrentDayNum < pushCurrentDayNumMax)
				{
					isCurrentDayNeedToPush = true;
				}
				else
				{
					isCurrentDayNeedToPush = false;
				}
				int listNum = 0;
				try {
					listNum = getNotPushNum(new JSONObject(pushList));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(listNum + pushCurrentDayNum< pushCurrentDayNumMax)
				{
					isPushListFull = false;
				}
				else
				{
					isPushListFull = true;
				}
				
				
				if(DEBUG)Log.e(TAG, "isPushListFull = " + isPushListFull + "   isCurrentDayNeedToPush = " + isCurrentDayNeedToPush + "  " +listNum + "  " + pushCurrentDayNum);
				
				if(isPushListFull && !isCurrentDayNeedToPush)
				{
					nextPushType = PushUtils.PUSH_LIST_TYPE;
					
					calendar.setTimeInMillis(currentSystemTime);
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					calendar.set(Calendar.HOUR_OF_DAY, 8);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					nextTime = pushNextTime = pushListNextTime = calendar.getTimeInMillis();
				}
				else if(isPushListFull && isCurrentDayNeedToPush)
				{
					pushListNextTime = currentSystemTime + (long)pushListTimeInterval * 1000;
					nextTime = pushNextTime = currentSystemTime + (long)pushTimeInterval * 1000;
					nextPushType = PushUtils.PUSH_ONE_MESSAGE_TYPE;
					
				}
				else if( !isPushListFull && !isCurrentDayNeedToPush)
				{
					nextPushType = PushUtils.PUSH_LIST_TYPE;
					nextTime = pushListNextTime = currentSystemTime + (long)pushListTimeInterval * 1000;
					pushNextTime = currentSystemTime + (long)pushTimeInterval * 1000;
				}
				else
				{
					pushListNextTime = currentSystemTime + (long)pushListTimeInterval * 1000;
					nextTime = pushNextTime = currentSystemTime + (long)pushTimeInterval * 1000;
					
					if(pushListNextTime <= pushNextTime)
					{
						nextTime = pushListNextTime;
						nextPushType = PushUtils.PUSH_LIST_TYPE;
					}
					else
					{
						nextTime = pushNextTime;
						nextPushType = PushUtils.PUSH_ONE_MESSAGE_TYPE;
					}
				}
			}
			//设置配置文件
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PushUtils.PUSH_SETTINGS, pushSettings);
			editor.putString(PushUtils.PUSH_LIST, pushList);
			editor.putLong(PushUtils.PUSH_LIST_NEXT_TIME, pushListNextTime);
			editor.putLong(PushUtils.PUSH_NEXT_TIME, pushNextTime);
			editor.putBoolean(PushUtils.PUSH_FIRST, pushFirst);
			editor.putLong(PushUtils.PUSH_FIRST_INTERVAL, pushFirstInterval);
			//editor.putInt(PushUtils.PUSH_START_HOUR, pushStartHour);
			//editor.putInt(PushUtils.PUSH_END_HOUR, pushEndHour);
			editor.putString(PushUtils.PUSH_CURRENT_DAY, pushCurrentDay);
			editor.putInt(PushUtils.PUSH_LIST_TIME_INTERVAL, pushListTimeInterval);
			editor.putInt(PushUtils.PUSH_TIME_INTERVAL, pushTimeInterval);
			editor.putInt(PushUtils.PUSH_CURRENT_DAY_NUM, pushCurrentDayNum);
			editor.putInt(PushUtils.PUSH_CURRENT_DAY_NUM_MAX, pushCurrentDayNumMax);
			editor.putBoolean(PushUtils.ACTIVATE, activate);
			editor.commit();
			
			calendar.setTimeInMillis(nextTime);
			if(DEBUG)Log.e(TAG,"nextTime ("  +sdf.format(new Date(nextTime)) + " :"+calendar.get(Calendar.HOUR_OF_DAY)+"h"+ calendar.get(Calendar.MINUTE) + "m " + calendar.get(Calendar.SECOND) +"s)");
			//继续设置闹钟
			PushUtils.startPushBroacast(mContext, nextTime, PushUtils.PUSH_ACTION, nextPushType);
			//释放锁
			if(mWakeLock != null)
			{
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		
	}
	
	
	public static long getNetworkTime()
	{
		URL url;
		long ld = -1;
		try {
			url = new URL("http://www.baidu.com");
			URLConnection uc = url.openConnection();
		    uc.connect(); 
		    ld=uc.getDate();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return ld;
	    
	}
	
	private static void copyJsonItem(final JSONObject fromJson, final JSONObject toJson)
	{
		try {
			
			if(fromJson.getInt("state") == 1)
			{
				if(toJson.isNull("item"))
				{
					//JSONArray items = fromJson.getJSONArray("item");
					String listStr = fromJson.getString("item");
					String[] items = listStr.split(",");
					JSONArray nativeItems = new JSONArray();
					for(int i = 0; i < items.length; i++)
					{
						JSONObject item = new JSONObject();
						item.put("id", Integer.parseInt(items[i]));
						item.put("isPushed", false);
						nativeItems.put(item);
					}
					toJson.put("item", nativeItems);
					if(DEBUG)Log.e(TAG, "push list copy");
				}
				else
				{
					//JSONArray items = fromJson.getJSONArray("item");
					String listStr = fromJson.getString("item");
					String[] items = listStr.split(",");
					JSONArray nativeItems = null;
					if(toJson.isNull("item"))
					{
						nativeItems = new JSONArray();
						toJson.put("item", nativeItems);
					}
					else
					{
						nativeItems = toJson.getJSONArray("item");
					}
					for(int i = 0; i < items.length ; i++)
					{
						
						int id = Integer.parseInt(items[i]);
						if(!isItemExist(id, nativeItems))
						{
							JSONObject nativeItem = new JSONObject();
							nativeItem.put("id", id);
							nativeItem.put("isPushed", false);
							if(DEBUG)Log.e(TAG, "add:" + nativeItem.toString());
							nativeItems.put(nativeItem);
						}
						
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private static boolean isItemExist(int id, JSONArray array) throws JSONException
	{
		boolean exist = false;
		for(int i = 0; i < array.length() ; i++)
		{
			JSONObject item = array.getJSONObject(i);
			if(!item.isNull("id"))
			{
				int nativeId = item.getInt("id");
				if(id == nativeId)
				{
					exist = true;
					break;
				}
			}
		}
		return exist;
	}
	
	private static int getNotPushNum(JSONObject json)
	{
		int num = 0;
		if(json!= null)
		{
			JSONArray array;
			try {
				array = json.getJSONArray("item");
				for(int i = 0; i < array.length() ; i++)
				{
					JSONObject item = array.getJSONObject(i);
					if(!item.getBoolean("isPushed"))
					{
						num++;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return num;
	}
	
	private static void ensureMaxNum(JSONObject json)
	{
		if(json!= null)
		{
			JSONArray array;
			try {
				JSONArray arrayNew = new JSONArray();
				array = json.getJSONArray("item");
				if(array.length() > 16)
				{
					for(int i = array.length() - 16; i < array.length() ; i++)
					{
						arrayNew.put(array.getJSONObject(i));
					}
					json.put("item", arrayNew);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
