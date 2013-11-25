package com.joy.launcher2.network.util;

import java.io.InputStream;

import org.json.JSONObject;

import com.joy.launcher2.cache.ImageOption;

import android.graphics.Bitmap;



/**
 * 通信接口
 * @author wanghao
 *
 */
public interface ClientInterface {
	/**
	 * 发送数据请求，得到返回的JSON对象
	 * @param protocal
	 * @return
	 * @throws Exception
	 */
	public JSONObject request(Protocal protocal) throws Exception ;
	
	/**
	 * 获取字符串
	 * @param protocal
	 * @return
	 * @throws Exception
	 */
	public String getString(Protocal protocal) throws Exception ;
	/**
	 * 发送数据，得到返回的流
	 * @param protocal
	 * @return
	 */
	public InputStream getInputStream(Protocal protocal);
	
	/**
	 * 释放资源
	 * @param  
	 * @return void
	 */
	public void shutdownNetwork();
	
	/**
	 * 网络状态是否OK
	 * @param @return 
	 * @return boolean
	 */
	public boolean isOK();
	
	
	//add by huangming for online wallpaer
		/**
		 * 通过网络协议获得bitmap
		 * @param protocal
		 * @return Bitmap
		 */
    public Bitmap getBitmap(Protocal protocal);
    //end
    
    /**
     * 通过网络协议获得bitmap
     * @param protocal
     * @param width
     * @return
     */
    public Bitmap getBitmap(Protocal protocal,  int width);
	
}
