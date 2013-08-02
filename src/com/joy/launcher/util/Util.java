package com.joy.launcher.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.joy.launcher.LauncherApplication;

/**
 * 工具类
 * 
 * @author User
 * 
 */
public class Util {

	private static String TAG = "Util";
	private static boolean isDebug = true;

	public static void i(String tag, String info) {
		if (isDebug) {
			Log.i(tag, info);
		}
	}
	public static void i(String tag, int info) {
		if (isDebug) {
			Util.i(tag, info+"");
		}
	}
	private static Random mRandom = new Random();

	public static String getTS() {
		String random = String.valueOf(mRandom.nextInt(9999));
		Util.i(TAG, random);
		return random;
	}

	private static String deviceid;

	public static String getDeviceID() {
		if (deviceid == null) {
			String mac = getMac();
			String imei = getImei();
			String id = buildString(mac, imei, Build.BOARD, Build.BRAND,
					Build.CPU_ABI, Build.DEVICE, Build.DISPLAY, Build.ID,
					Build.MANUFACTURER, Build.MODEL, Build.PRODUCT, Build.TAGS,
					Build.TYPE, Build.USER);
			deviceid = md5Encode(id);
		}
		Util.i(TAG, deviceid);
		return deviceid;
	}

	/**
	 * 将字符串编码为md5格式
	 * 
	 * @param value
	 * @return
	 */
	public static String md5Encode(String value) {
		String tmp = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(value.getBytes("utf8"));
			byte[] md = md5.digest();
			tmp = binToHex(md);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return tmp;
	}

	public static String binToHex(byte[] md) {
		StringBuffer sb = new StringBuffer("");
		int read = 0;
		for (int i = 0; i < md.length; i++) {
			read = md[i];
			if (read < 0)
				read += 256;
			if (read < 16)
				sb.append("0");
			sb.append(Integer.toHexString(read));
		}
		return sb.toString();
	}

	/**
	 * 构造String字符串
	 * 
	 * @param args
	 * @return
	 */
	public static String buildString(String... args) {
		StringBuffer buffer = new StringBuffer();
		for (String arg : args) {
			buffer.append(arg);
		}
		return buffer.toString();
	}

	public static String getMac() {
		WifiManager wifi = (WifiManager) LauncherApplication.mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 获取IMSI标识
	 * 
	 * @param context
	 * @return
	 */
	public static String getImsi(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}

	/**
	 * 获取IMEI标识
	 * 
	 * @param context
	 * @return
	 */
	public static String getImei() {
		TelephonyManager tm = (TelephonyManager) LauncherApplication.mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 获取手机号码
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhone(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	/**
	 * 获取网络类型
	 * 
	 * @param context
	 * @return 1:GPRS 2:Wifi
	 */
	public static int getNetworkType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
			return 2;
		}
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			return 1;
		} else {
			return 2;
		}
	}

	/**
	 * 是否为系统应用程序
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isSysteLauncherApplication(Context context,
			String packageName) {
		boolean ret = false;
		try {
			PackageInfo pkg = context.getPackageManager().getPackageInfo(
					packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pkg != null) {
				if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
						|| (pkg.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				} else {
					ret = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 打开应用程序
	 * 
	 * @param context
	 *            上下文环境
	 * @param packageName
	 *            包名称
	 */
	public static void startApplication(Context context, String packageName) {
		if (isInstallApplication(context, packageName)) {
			PackageManager pm = context.getPackageManager();
			Intent openIntent = pm.getLaunchIntentForPackage(packageName);
			openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			List<ResolveInfo> acts = pm.queryIntentActivities(openIntent, 0);
			if (acts.size() > 0) {
				context.startActivity(openIntent);
			}
		} else {
			Util.toast(packageName);
		}
	}
	/**
	 * 是否已经安装APP应用程序
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isInstallApplication(Context context,
			String packageName) {
		try {
			PackageInfo pkg = context.getPackageManager().getPackageInfo(
					packageName, PackageManager.GET_ACTIVITIES);
			return pkg != null;
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 卸载应用程序
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void unInstallApplication(Context context, String packageName) {
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}

	/**
	 * 获取所有已经安装的程序包(非系统程序)
	 * 
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getApplications(Context context) {
		List<PackageInfo> pkgs = new ArrayList<PackageInfo>();
		List<PackageInfo> list = context.getPackageManager()
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkg : list) {
			if (!Util.isSysteLauncherApplication(context, pkg.packageName)) {
				pkgs.add(pkg);
			}
		}
		return pkgs;
	}

	public static int dip2px(float dipValue) {
		final float scale = LauncherApplication.mContext.getResources()
				.getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(float pxValue) {
		final float scale = LauncherApplication.mContext.getResources()
				.getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static Bitmap autoIcon(Bitmap bm) {
		return zoomBitmap(bm, dip2px(70), dip2px(70));
	}

	/**
	 * 缩放
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		if (bitmap == null) {
			return null;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	/**
	 * 判断是否有网络连接
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) LauncherApplication.mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * 提示框
	 * @param message
	 */
	public static void toast(String message) {
		Toast.makeText(LauncherApplication.mContext, message, Toast.LENGTH_SHORT)
				.show();
	}
	/**
	 * 获取资源字符串
	 * 
	 * @param id
	 * @return
	 */
	public static String getResourcesStr(int id) {
		return LauncherApplication.mContext.getResources().getString(id);
	}

	public static String getFileNameByUrl(String url) {
		if (url == null || "".equals(url.trim())) {
			return null;
		}
		return url.substring(url.lastIndexOf("/") + 1);
	}

	/**
	 * 判断SD卡是否存在
	 * 
	 * @return
	 */
	public static boolean hasSdcard() {
		String status = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(status)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 把文件放到SD卡中
	 * 
	 * @param bm
	 */
	public static void saveFileToSD(InputStream is, String path) {
		if (!hasSdcard()) {
			return;
		}
		if (is == null) {
			return;
		}
		FileOutputStream fos = null;
		try {
			File file = new File(path).getParentFile();
			if (!file.exists()) {
				file.mkdirs();
			}
			fos = new FileOutputStream(path);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = is.read(b)) != -1) {
				fos.write(b, 0, len);
			}
		} catch (Exception e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// 文件如果存在重命名
	public static File getCleverFileName(File file) {
		if (file == null) {
			return null;
		}
		String fileName = file.getName();
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			return file;
		} else {
			int index = fileName.lastIndexOf(".");
			if (index == -1) {
				index = fileName.length();
			}
			boolean end = false;
			String numStr = "";
			String name = file.getName().substring(0, index);
			for (int i = name.length(); i > 0; i--) {
				String c = name.substring(i - 1, i);
				if ("(".equals(c)) {
					break;
				}
				if (end) {
					numStr += c;
				}
				if (")".equals(c)) {
					end = true;
				}
			}
			if (end) {
				int x = 1;
				try {
					x = Integer.parseInt(numStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
				int y = name.length() - ("(" + x + ")").length();
				name = name.substring(0, y);
				fileName = name + "(" + (x + 1) + ")"
						+ file.getName().substring(index);
			} else {
				fileName = name + "(1)" + file.getName().substring(index);
			}

			file = new File(file.getParentFile() + "/" + fileName);
			if (file.exists()) {
				return getCleverFileName(file);
			} else {
				return file;
			}
		}
	}

	// 单位转换
	public static String getSizeUnit(int size) {
		if (size == 0) {
			return "0K";
		}
		String str = "";
		if (size >= 1024 * 1024 * 1024) {
			double d = (double) size / 1024 / 1024 / 1024;
			BigDecimal b = new BigDecimal(d).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			str = b + "G";
		} else if (size >= 1024 * 1024) {
			double d = (double) size / 1024 / 1024;
			BigDecimal b = new BigDecimal(d).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			str = b + "M";
		} else if (size >= 1024) {
			double d = (double) size / 1024;
			BigDecimal b = new BigDecimal(d).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			str = b + "K";
		} else {
			str = size + "B";
		}
		return str;
	}

	public static String getDownLoadNumUnit(int count) {
		if (count < 10000) {
			return count + "";
		} else {
			double d = (double) count / 10000;
			BigDecimal b = new BigDecimal(d).setScale(1,
					BigDecimal.ROUND_HALF_UP);
			return b + "万";
		}
	}

	// 状态栏高度
	public static int getStatusBarHeight(Context context) {
		int h = 0;
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object obj = clazz.newInstance();
			Field field = clazz.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			h = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
		}
		return h;
	}

	public static byte[] getBytes(InputStream is) {

		ByteArrayOutputStream bab = new ByteArrayOutputStream();
		byte[] datas = new byte[8192];
		int count = -1;

		try {
			while ((count = is.read(datas, 0, datas.length)) != -1) {
				bab.write(datas, 0, count);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bab.toByteArray();
	}

	/**
	 * 获取图片
	 * 
	 * @param id
	 * @return
	 */
	public static Bitmap getBitmapById(int id) {
		return BitmapFactory.decodeResource(
				LauncherApplication.mContext.getResources(), id);
	}
 
}
