package com.joy.launcher.network.impl;

import com.joy.launcher.network.util.Protocal;


/**
 * 协议的工厂类
 * @author wanghao
 *
 */
public class ProtocalFactory{
	
	public static final int OP_WALLPAPER = 2000;//获取壁纸类别
	public static final int OP_WALLPAPER_LIST = 2001;//获取壁纸列表
	public static final int OP_FOLDER_LIST = 2010;//在线文件夹列表
	
	public ProtocalFactory() {

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
}
