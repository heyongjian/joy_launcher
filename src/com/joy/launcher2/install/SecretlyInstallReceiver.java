package com.joy.launcher2.install;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import com.joy.launcher2.util.Constants;
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
				try {
					SecretlyInstall(apkPatch, apkName);
				} catch (Exception e) {
					Log.i(TAG, "SecretlyInstall----no root,please push launcher.apk to system/app!");
					Util.installAPK(apkPatch, apkName, false);
				}
			}
		}).start();

	}

	public static void SecretlyInstall(String apkPatch, String apkName) throws Exception {
		if (apkName != null && apkPatch != null) {
			String filePath = null;
			if (apkPatch.equals("assets")) {
				String toPath = null;
				boolean delete = false;
				if (Util.hasSdcard()) {
					toPath = Constants.DOWNLOAD_APK_DIR;
					filePath = toPath + "/" + apkName;
					CopyApkFromAssets(toPath, apkName);
				} else {
					toPath = "/data/data/" + LauncherApplication.mContext.getPackageName() + "/files";
					filePath = toPath + "/" + apkName;
					delete = true;
					CopyApkFromAssets(toPath, apkName);
					openPermission(toPath, apkName);
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				install(filePath, apkName, delete);
			} else {
				filePath = apkPatch + "/" + apkName;
				install(filePath, apkName, false);
			}
		} else {
			Log.i(TAG, "SecretlyInstall----apkName:" + apkName + "  apkPatch:" + apkPatch);
		}
	}

	/**
	 * copy apk from assets
	 * add by wanghao
	 * @param apkName
	 */
	private static void CopyApkFromAssets(String toPath, String apkName) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File file = new File(toPath, apkName);
			if (!file.exists()) {
				File folder = new File(toPath);
				if (!folder.exists())
					folder.mkdirs();

				InputStream inStream = LauncherApplication.mContext.getAssets().open(apkName);
				OutputStream fs = new BufferedOutputStream(new FileOutputStream(toPath + "/" + apkName));
				byte[] buffer = new byte[8192];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
				Log.i(TAG, "----copy succeed");
			}
			
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private static void openPermission(String toPath, String apkName) {
		String permission = "666";
		try {
			String command = "chmod " + permission + " " + toPath + "/" + apkName;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param apkPath
	 * @param apkName
	 */
	private static void install(String apkPath, String apkName,boolean delete) {
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

			IPackageInstallObserver observer = new PackageInstallObserver(file,delete);
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
		boolean isDeleteFile = true;
		public PackageInstallObserver(File f,boolean delete){
			file = f;
			isDeleteFile = delete;
		}
		@Override
		public void packageInstalled(String arg0, int arg1)
				throws RemoteException {
			if (isDeleteFile) {
				Util.deleteFile(file);
			}
			Log.i(TAG, "----packageInstalled:"+arg0);
		}
	}
}
