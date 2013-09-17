package com.joy.launcher2.network.impl;

import com.joy.launcher2.network.util.Protocal;
import com.joy.launcher2.util.SystemInfo;
import com.joy.launcher2.util.Util;


/**
 * 协议的工厂类
 * @author wanghao
 *
 */
public class ProtocalFactory{
	
	public static final int OP_WALLPAPER = 2000;//获取壁纸类别
	public static final int OP_WALLPAPER_LIST = 2001;//获取壁纸列表
	public static final int OP_FOLDER_LIST = 2010;//在线文件夹列表
	public static final int OP_BACKUP = 1111;//备份
	public static final String HOST = "http://192.168.164.134:8080/client/upload.do";
	public static String SIGN_KEY = "deskt0pj@y";//约定字符串
	
	public ProtocalFactory() {

	}

	public static String getSign(String ts, String rs){
	
		StringBuffer sb = new StringBuffer(200);
		sb.append(Util.encodeContentForUrl(Util.md5Encode(ts+rs))).append(SIGN_KEY);
		return sb.toString();
	}
	
	public static String getSjz(String rs)
	{
		return Util.encodeContentForUrl(rs);
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
	public Protocal activateProtocal(){
		Protocal pw = new Protocal();

		StringBuffer sb = new StringBuffer(200);
		sb.append("op=").append(1000)
		.append("&channel=").append(Util.encodeContentForUrl(SystemInfo.channel))
		.append("&imei=").append(Util.encodeContentForUrl(SystemInfo.imei))
		.append("&imsi=").append(Util.encodeContentForUrl(SystemInfo.imsi))
		.append("&mac=").append(Util.encodeContentForUrl(SystemInfo.mac))
		.append("&os=").append(Util.encodeContentForUrl(SystemInfo.os))
		.append("&province=").append(Util.encodeContentForUrl(SystemInfo.province))
		.append("&city=").append(Util.encodeContentForUrl(SystemInfo.city))
//		.append("&sms=").append(Util.encodeContentForUrl(SystemInfo.sms))
		.append("&display=").append(Util.encodeContentForUrl(SystemInfo.display))
		.append("&product=").append(Util.encodeContentForUrl(SystemInfo.product))
		.append("&brand=").append(Util.encodeContentForUrl(SystemInfo.brand))
		.append("&model=").append(Util.encodeContentForUrl(SystemInfo.model))
		.append("&language=").append(Util.encodeContentForUrl(SystemInfo.language))
		.append("&operators=").append(SystemInfo.operators)
		.append("&network=").append(SystemInfo.network)
		.append("&vcode=").append(SystemInfo.vcode)
		.append("&vname=").append(Util.encodeContentForUrl(SystemInfo.vname))
		.append("&bid=").append(Util.encodeContentForUrl(SystemInfo.id))
		.append("&board=").append(Util.encodeContentForUrl(SystemInfo.board))
		.append("&abi=").append(Util.encodeContentForUrl(SystemInfo.abi))
		.append("&device=").append(Util.encodeContentForUrl(SystemInfo.device))
		.append("&mf=").append(Util.encodeContentForUrl(SystemInfo.mf))
		.append("&tags=").append(Util.encodeContentForUrl(SystemInfo.tags))
		.append("&user=").append(Util.encodeContentForUrl(SystemInfo.user))
		.append("&btype=").append(Util.encodeContentForUrl(SystemInfo.type));

		pw.setGetData(sb.toString());
		return pw;
	}
}
