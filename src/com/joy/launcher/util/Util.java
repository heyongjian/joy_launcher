package com.joy.launcher.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.joy.launcher.LauncherApplication;

/**
 * 工具类 提供随机数生成（数字和字符），加密相关（mod5），字符串操作（追加和替换），图片操作（生成、缩放、裁剪）， 本机信息（id，mac，IME,
 * sd operate，network status ）这些分类方法。
 * 
 * @author hao.wang
 * 
 */
public class Util {

	private static String TAG = "Util";
	private static String deviceid;
	private static int sBitmapTextureWidth = 48;
	private static int sBitmapTextureHeight = 48;
	private static Random mRandom = new Random();

	/**
	 * 随机数
	 * 
	 * @return
	 */
	public static String getTS(int n) {
		String str = String.valueOf(mRandom.nextInt(n));
		return str;
	}

	/**
	 * 获取随机字符串
	 */

	public static final String randomString(int length) {
		if (length < 1) {
			return null;
		}
		char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
				+ "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[mRandom.nextInt(71)];
			// randBuffer[i] = numbersAndLetters[randGen.nextInt(35)];
		}
		return new String(randBuffer);
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

	/**
	 * 字符串转换
	 * 
	 * @param content
	 * @return
	 */
	public static String encodeContentForUrl(String content) {

		try {
			return (content == null ? "" : URLEncoder.encode(
					URLEncoder.encode(content, "UTF-8"), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * 字符串转换
	 * 
	 * @param content
	 * @return
	 */
	public static String encodeDevice(String value) {
		StringBuilder sb = new StringBuilder(32);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(value.getBytes("utf-8"));

			for (int i = 0; i < array.length; i++) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.toUpperCase().substring(1, 3));
			}
		} catch (Exception e) {
			return null;
		}
		return sb.toString();
	}

	/**
	 * 根据url返回文件名
	 * 
	 * @param content
	 * @return
	 */
	public static String getFileNameByUrl(String url) {
		if (url == null || "".equals(url.trim())) {
			return null;
		}
		return url.substring(url.lastIndexOf("/") + 1);
	}

	/**
	 * 将流转换成字节数组
	 * 
	 * @param is
	 * @return
	 */
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
	 * generate bitmap rely on ID
	 * 
	 * @author yongjian.he
	 * @param id
	 * @return
	 */
	public static Bitmap getBitmapById(int id) {
		return BitmapFactory.decodeResource(
				LauncherApplication.mContext.getResources(), id);
	}

	/**
	 * bitmap to drawable
	 * 
	 * @author yongjian.he
	 */
	public static Drawable bitmapToDrawable(Bitmap bp) {
		Bitmap bitmap = bp;
		if (bitmap != null) {
			return new BitmapDrawable(
					LauncherApplication.mContext.getResources(), bitmap);
		} else {
			Log.e(TAG, "---bitmapToDrawable "
					+ "error: the src bitmap is null!");
			bitmap = Bitmap.createBitmap(sBitmapTextureWidth,
					sBitmapTextureHeight, Bitmap.Config.ARGB_8888);
			return new BitmapDrawable(
					LauncherApplication.mContext.getResources(), bitmap);
		}
	}

	/**
	 * bitmap to drawable
	 * 
	 * @author yongjian.he
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable != null) {
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
					: Bitmap.Config.RGB_565;
			Bitmap bitmap = Bitmap.createBitmap(w, h, config);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, w, h);
			drawable.draw(canvas);
			return bitmap;
		} else {
			Log.e(TAG, "---drawableToBitmap "
					+ "error: the src drawable is null!");
			return null;
		}
	}

	/**
	 * zoom drawable
	 * 
	 * @author yongjian.he
	 */
	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap oldbmp = drawableToBitmap(drawable);
			Matrix matrix = new Matrix();
			float sx = ((float) w / width);
			float sy = ((float) h / height);
			matrix.postScale(sx, sy);
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
					matrix, true);
			return new BitmapDrawable(
					LauncherApplication.mContext.getResources(), newbmp);
		} else {
			Log.e(TAG, "---zoomDrawable " + "error: the src drawable is null!");
			return null;
		}
	}

	/**
	 * generate RoundedCornerBitmap
	 * 
	 * @author yongjian.he
	 * @param roundPx
	 *            : corner radius.
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
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

	/**
	 * 获取本机唯一标识符
	 * 
	 * @return
	 */
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
		return deviceid;
	}

	/**
	 * get current API Versions
	 * 
	 * @return
	 */
	public static int getCurrentApiVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取mac地址
	 * 
	 * @return
	 */
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
	 * 检测文件是否有重名 若重名则在名字后面加数字来区别 eg：a.txt --> a(1).txt --> a(2).txt
	 * 
	 * @param file
	 * @return
	 */
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
	
	/**
	 * 自动安装 hao.wang
	 * @param apkPath
	 * @param apkName
	 */
	public static void installAPK(String apkPath, String apkName) {
		File file = new File(apkPath, apkName);
		Log.i("OpenFile", file.getName());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		LauncherApplication.mContext.startActivity(intent);
	}
	
	/**
	 * 是否已经安装APP应用程序
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isInstallApplication(Context context, String packageName){
		try {
			PackageInfo pkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return pkg != null;
		} catch(Exception e){
		}
		return false;
	}
}
