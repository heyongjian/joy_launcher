package com.joy.launcher2.install;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.joy.launcher2.Launcher;
import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.util.Util;

/**
 * the first time to install built-in apk
 * @author wanghao
 */
public class InstallAPK {

	
	Launcher mLauncher;
	ArrayList<InstallApkInfo> apkList = new ArrayList<InstallApkInfo>();
	public InstallAPK(Launcher launcher){
		mLauncher = launcher;
		init();
	}
	
	public void init(){
		String string = Util.getStringFromAssets("built-in.txt");
		
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonarry = jsonObject.getJSONArray("built_in_apk");
			int length = jsonarry.length();
			for(int i=0;i<length;i++){
				JSONObject item = jsonarry.getJSONObject(i);
				InstallApkInfo info = new InstallApkInfo();
				info.apkName = item.getString("apkName");
				info.packageName = item.getString("packageName");
				info.className = item.getString("className");
				apkList.add(info);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isInstall(){
		if (apkList==null||apkList.size()==0) {
			return true;
		}
		int size = apkList.size();
		for (int i = 0; i < size; i++) {
			InstallApkInfo info = apkList.get(i);
			String packageString = info.packageName;
			boolean isInstall = Util.isInstallApplication(LauncherApplication.mContext, packageString);

			if (!isInstall) {
				return false;
			}
		}
		return true;
	}
	
    public void installApk(final InstallApkListener listener){
    	
    	if (listener != null) {
			listener.InstallStart();
		}
        final Handler handler = new Handler(){
        	@Override
        	public void handleMessage(Message msg) {
        		// TODO Auto-generated method stub
        		if (listener != null) {
        			listener.InstallEnd();
				}
        	}
        };
        new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				installApkFromAssets();
				handler.sendEmptyMessageDelayed(0, 500);
			}
		}).start();
    }
    
	public void installApkFromAssets(){
		for (int i = 0; i < apkList.size(); i++) {
			InstallApkInfo info = apkList.get(i);
			String packageString = info.packageName;
			String apkName = info.apkName;
			boolean isInstall = Util.isInstallApplication(LauncherApplication.mContext, packageString);
			if (!isInstall) {
				try {
					SecretlyInstallReceiver.SecretlyInstall("assets", apkName);
				} catch (Exception e) {
					Log.i("InstallAPK", "installApkFromAssets:"+e);
				}
			}
		}
	}
	
	class InstallApkInfo{
		String apkName;
		String packageName;
		String className;
	}
	
	public interface InstallApkListener{
		public void InstallStart();
		public void InstallEnd();
	}
}


