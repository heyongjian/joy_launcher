package com.joy.launcher2;

import java.util.logging.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetWorkStatusChangeReceiver extends BroadcastReceiver {
	private Refreshable refreshable;
	public NetWorkStatusChangeReceiver(Refreshable refreshable){
		this.refreshable = refreshable;
	}
	
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		//有网络连接了
		if(wifiState != null && mobileState != null && (wifiState == State.CONNECTED || mobileState == State.CONNECTED)){
			if(refreshable!=null){
				try {
					refreshable.refresher();
				} catch (Exception e) {
				}
			}
		}
	}
	public interface Refreshable {

		public void refresher();
	}
}
