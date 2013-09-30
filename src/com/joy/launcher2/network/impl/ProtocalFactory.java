package com.joy.launcher2.network.impl;

import com.joy.launcher2.network.util.Protocal;
import com.joy.launcher2.util.Constants;
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
	public static final int OP_BACKUP = 1111;//备份
	public static final int OP_APKLIST = 2011;//游戏应用列表
	public static final int OP_APP_IN_FOLDER = 4002;//文件夹里的虚框软件
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
	
	public static String getSign(String ts) {
		String randomString = Util.randomString(6);
		StringBuffer sb = new StringBuffer(200);
		sb.append("&sign=")
				.append(Util.encodeContentForUrl(Util.md5Encode(Util
						.md5Encode(ts + SIGN_KEY) + randomString)))
				.append("&sjz=").append(Util.encodeContentForUrl(randomString));
		;
		return sb.toString();
	}
	
	public Protocal testProtocal(String url){
		Protocal pw = new Protocal();
		pw.setHost(url);
		return pw;
	}
	
	public Protocal bitmapProtocal(String url) {
		Protocal pw = new Protocal();
		// pw.setGetData("op=" + USER_LOGIN);
		pw.setHost(Constants.APK_LIST_HOST);
		pw.setGetData(url);
		return pw;
	}

	public Protocal downloadApkProtocal(String url) {
		Protocal pw = new Protocal();
		pw.setHost(Constants.APK_LIST_HOST);
		pw.setGetData(url + "&channel="+SystemInfo.channel);
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

	public Protocal getAppInFolderProtocal(int type) {
		Protocal pw = new Protocal();
		pw.setHost(Constants.APK_LIST_HOST);
		int id = 2;//application
		if (type == 0) {
			id = 1;//game
		}
		pw.setGetData("?op="+OP_APP_IN_FOLDER+"&channel="+SystemInfo.channel+"&id="+id);
		return pw;
	}

	/**
	 * 获取游戏、应用列表
	 * 
	 * @return
	 */
	public Protocal getApkListProtocal(int type, int index, int num) {
		Protocal pw = new Protocal();
		pw.setHost(Constants.APK_LIST_HOST);
		int category = 1;//application
		if (type == 0) {
			category = 2;//game
		}
		pw.setGetData("?op="+OP_APKLIST+"&channel="+SystemInfo.channel+"&category="+category+"&pi="+index+"&ps="+num);
		return pw;
	}
}
