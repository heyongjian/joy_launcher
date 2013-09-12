package com.joy.launcher2.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.util.Util;

/**
 * 静默安装
 * @author wanghao
 * 
 */
public class SecretlyInstallReceiver extends BroadcastReceiver {

	boolean isDebug = true;
	static String TAG = "SecretlyInstallReceiver";

	public static final String ACTION_SECRETLY_INSTALL = "com.android.launcher.action.ACTION_SECRETLY_INSTALL";

	public static final String INSTALL_APK_NAME = "install_apk_name";
	public static final String INSTALL_APK_PATCH = "install_apk_patch";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String actiongString = intent.getAction();
		if (!ACTION_SECRETLY_INSTALL.equals(actiongString)) {
			return;
		}
		Bundle bundle = intent.getExtras();

		final String apkPatch = bundle.getString(INSTALL_APK_PATCH);
		final String apkName = bundle.getString(INSTALL_APK_NAME);

		new Thread(new Runnable() {

			@Override
			public void run() {
				SecretlyInstall(apkPatch, apkName);
			}
		}).start();

	}

	public static void SecretlyInstall(String apkPatch, String apkName) {

		if (apkName != null&&apkPatch != null) {
			
		}
		if (apkPatch.equals("assets")) {
 
			String toPath = "/data/data/" + LauncherApplication.mContext.getPackageName();

			CopyApkFromAssets(toPath, apkName);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			toPath = toPath + "/files/";
			install(toPath + apkName, apkName);
		} else {
			install(apkPatch + "/" + apkName, apkName);
		}
	}

	/**
	 * copy apk from assets
	 * add by wanghao
	 * @param apkName
	 */
	private static void CopyApkFromAssets(String toPath,String apkName) {

		File file = new File(toPath, apkName);
		try {
			InputStream is = LauncherApplication.mContext.getAssets().open(apkName);
			if (is==null) {
				return;
			}
			if (!file.exists()) {
				{
					File folder = new File(toPath);
					if (!folder.exists())
						folder.mkdirs();
				}

				file.createNewFile();
				FileOutputStream os = LauncherApplication.mContext.openFileOutput(
						file.getName(), Context.MODE_WORLD_WRITEABLE);
				byte[] bytes = new byte[512];
				int i = -1;
				while ((i = is.read(bytes)) > 0) {
					os.write(bytes);
				}

				os.close();
				is.close();
				Log.i(TAG, "----copy succeed");
			} else {
				Log.i(TAG, "----exist");
			}
			String permission = "666";

			try {
				String command = "chmod " + permission + " " + toPath
						+ "/files/" + apkName;
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

	}

	/**
	 * 
	 * @param apkPath
	 * @param apkName
	 */
	private static void install(String apkPath, String apkName) {
		Log.i(TAG, "----install,apkPath " + apkPath);
		File file = new File(apkPath);
		Log.i(TAG, "----install,file.exists() " + file.exists());
		if (!file.exists())
			return;
		Uri mPackageURI = Uri.fromFile(file);
		int installFlags = 0;
		PackageManager pm = LauncherApplication.mContext.getApplicationContext()
				.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		Log.i(TAG, "----install,info " + info);
		if (info != null) {
			try {
				PackageInfo pi = pm.getPackageInfo(info.packageName,
						PackageManager.GET_UNINSTALLED_PACKAGES);
				if (pi != null) {
					installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
				}
			} catch (NameNotFoundException e) {
				Log.i(TAG, "----install, e " + e);
			}

			IPackageInstallObserver observer = new PackageInstallObserver(file);
			PackageManager pManager = LauncherApplication.mContext.getPackageManager();
			pManager.installPackage(mPackageURI, observer, installFlags,info.packageName);
		}
	}

	/**
	 * package install listener
	 * delete the file when it is installed
	 * @author wanghao
	 */
	private static class PackageInstallObserver extends
			JoyPackageInstallObserver {
		File file;
		public PackageInstallObserver(File f){
			file = f;
		}
		@Override
		public void packageInstalled(String arg0, int arg1)
				throws RemoteException {
			Util.deleteFile(file);
			Log.i(TAG, "----packageInstalled");
		}
	}
}
