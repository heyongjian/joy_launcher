package com.joy.launcher.network.impl;

import com.joy.launcher.network.util.Protocal;
import com.joy.launcher.util.Util;


/**
 * 协议的工厂类
 * @author wanghao
 *
 */
public class ProtocalFactory{
	
	public static final int OP_WALLPAPER = 2000;//获取壁纸类别
	public static final int OP_WALLPAPER_LIST = 2001;//获取壁纸列表
	public static final int OP_FOLDER_LIST = 2010;//在线文件夹列表
	static String SIGN_KEY = "deskt0pj@y";//约定字符串
	
	public ProtocalFactory() {

	}

	public static String getSign(String ts){
		String randomString = Util.randomString(6);
		StringBuffer sb = new StringBuffer(200);
		sb.append("&sign=").append(Util.encodeContentForUrl(Util.md5Encode(ts+randomString)))
		.append(SIGN_KEY)
		.append("&sjz=").append(Util.encodeContentForUrl(randomString));;
		return sb.toString();
	}
	
	public Protocal testProtocal(String url){
		Protocal pw = new Protocal();
		pw.setHost(url);
		return pw;
	}
	
	public Protocal bitmapProtocal(String url){
		Protocal pw = new Protocal();
		pw.setHost(url);
//		pw.setGetData("op=" + USER_LOGIN);
		return pw;
	}
	public Protocal wallpaperProtocal(){
		Protocal pw = new Protocal();
//		op=2000&sign=xx&sjz=xxx
		pw.setGetData("op=" + OP_WALLPAPER +"&sign="+1+"&sjz="+2);
		return pw;
	}
	
	public Protocal downloadApkProtocal(){
		Protocal pw = new Protocal();
//		pw.setStart(start);
		String host = "http://www.appchina.com/market/d/1121573/cop.baidu_0/com.polontech.android.c360by.apk";
		pw.setHost(host);
		pw.setSoTimeout(30000);
		return pw;
	}
}
