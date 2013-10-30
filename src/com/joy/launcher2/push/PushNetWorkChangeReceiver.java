package com.joy.launcher2.push;

import com.joy.launcher2.push.PushService.DownloadAPK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PushNetWorkChangeReceiver  extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent data) {
		// TODO Auto-generated method stub
		Log.e("PushNetWorkChangeReceiver", "network changed");
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if(wifiState != null && mobileState != null && (wifiState == State.CONNECTED || mobileState == State.CONNECTED))
		{
			PushDownLoadDBHelper dbHelper = PushDownLoadDBHelper.getInstances();
			Cursor cur = null;
			try
			{
				dbHelper.open();
				
				cur = dbHelper.getAll();
				if(cur != null)
				{
					while(cur.moveToNext())
					{
						int fileSize = cur.getInt(cur.getColumnIndex("file_size"));
						int completeSize = cur.getInt(cur.getColumnIndex("complete_size"));
						int id = cur.getInt(cur.getColumnIndex("id"));
						if(completeSize < fileSize && PushDownloadManager.getInstances().getDowmloadingTask(id) == null)
						{
							PushAsyncHandler.post(new DownloadAPK(context, null, null, id));
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally{
				if(cur != null)cur.close();
				if(dbHelper != null)dbHelper.close();
			}
			
		}
	}

}
