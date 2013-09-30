package com.joy.launcher2.util;

import android.os.Environment;

/**
 * 常量类
 * @author wanghao
 *
 */
public class Constants {
	//host
		
		public static final String BASE_URL = "http://192.168.164.134:8080/app/api.do";
		
		public static final String APK_LIST_HOST = "http://192.168.164.134:8080/client/api.do";
		
		public static final int TIMEOUT = 15000; //超时时间
 
		//SD卡存储根目录
		public static final String DS_ROOT = "joy";
		//图片存入位置
		public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+DS_ROOT+"/images";
		//apk存入位置
		public static final String DOWNLOAD_APK_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+DS_ROOT+"/apk";
		//json存入位置
		public static final String DOWNLOAD_JSON_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+DS_ROOT+"/json";
		
		public static final String FILENAME_APP_LIST = "applist.txt";
		
		//正常的虚框
		public static final int SOFT_TYPE_VIRTUAL = 1;
		//静默安装的虚框软件
		public static final int SOFT_TYPE_SECRETLY = 2;
		
		//每次获取apk列表个数
		public static final int APK_LIST_NUM = 20;
}
