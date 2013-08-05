package com.joy.launcher.network.impl;

import java.util.List;

import com.joy.launcher.cache.ImageOption;

import android.graphics.Bitmap;

/**
 * 联网接口
 * @author wanghao
 *
 */
public interface ServiceInterface {
	
	/**
	 * 网络是否已连接OK
	 * @param @return 
	 * @return boolean
	 */
	public boolean netWorkIsOK();
	/**
	 * 释放网络资源
	 */
	public void shutdownNetwork();
	
	/**
	 * 从网络上下载图片
	 * @param url
	 * @param option
	 * @return
	 * @throws Exception
	 */
	public Bitmap getBitmapByUrl(String url, ImageOption... option) throws Exception;
	
	public void getWallpaper() throws Exception;;
	
	public String getTestData() throws Exception;
}
