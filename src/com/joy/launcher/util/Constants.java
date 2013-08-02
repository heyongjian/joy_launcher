package com.joy.launcher.util;

import android.os.Build;
import android.os.Environment;

/**
 * 常量类
 * @author User
 *
 */
public class Constants {
	//host
		public static final String HOST = "http://www.baidu.com";
		
		public static final String BASE_URL = "http://www.baidu.com/img/bdlogo.gif";
		
		public static final String TEST_URL = "http://www.baidu.com";
		
		public static final int TIMEOUT = 15000; //超时时间
 
		//SD卡存储根目录
		public static final String DS_ROOT = "joy";
		//图片存入位置
		public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+DS_ROOT+"/images";
		//apk存入位置
		public static final String DOWNLOAD_APK_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+DS_ROOT+"/apk";

}
