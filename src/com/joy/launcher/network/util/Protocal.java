package com.joy.launcher.network.util;

import org.json.JSONObject;

/**
 * 协议
 * @author wanghao
 *
 */
public class Protocal {
	//交互协议 （eg：op=2000&sign=xx&sjz=xx）
	private String getData;
	// post的数据（json对象） 注：getData与postData的区别
	private JSONObject postData = new JSONObject();
	//主机
	private String host;
	// 超时时间
	private int soTimeout = -1;
	// 是否启动重连机制
	private boolean reTry = true;
	
	/**
	 * @return the getData
	 */
	public String getGetData() {
		return getData;
	}

	/**
	 * @param getData
	 *            the getData to set
	 */
	public void setGetData(String getData) {
		this.getData = getData;
	}

	/**
	 * @return the postData
	 */
	public JSONObject getPostData() {
		return postData;
	}

	/**
	 * @param postData
	 *            the postData to set
	 */
	public void putPostData(String key, Object value) {
//		this.postData = postData;
		try {
			postData.put(key, value);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the soTimeout
	 */
	public int getSoTimeout() {
		return soTimeout;
	}

	/**
	 * @param soTimeout
	 *            the soTimeout to set
	 */
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	/**
	 * @return the reTry
	 */
	public boolean isReTry() {
		return reTry;
	}

	/**
	 * @param reTry
	 *            the reTry to set
	 */
	public void setReTry(boolean reTry) {
		this.reTry = reTry;
	}

}
