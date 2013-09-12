package com.joy.launcher2.network.impl;

import java.io.InputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.joy.launcher2.cache.ImageOption;
import com.joy.launcher2.network.handler.ActivateHanlder;
import com.joy.launcher2.network.handler.BitmapHandler;
import com.joy.launcher2.network.util.ClientHttp;
import com.joy.launcher2.network.util.ClientInterface;
import com.joy.launcher2.network.util.Protocal;
import com.joy.launcher2.util.Constants;

/**
 * 联网接口的具体实现
 * @author wanghao
 *
 */
public class Service {
	private static Service service;
	ClientInterface cs = null;
	ProtocalFactory pfactory;

	// 类似于AsycTask类
	public interface CallBack {
		/**
		 * 在doInBackground之前被调用，这里是联网前，更新UI
		 */
		public void onPreExecute();

		/**
		 * 在doInBackground之后被调用，更新UI
		 */
		public void onPostExecute();

		/**
		 * 处理后台耗时事情，不可在此更新UI
		 */
		public void doInBackground();
	}

	private Service() {
	};

	public static synchronized Service getInstance() throws Exception {
		if (service == null) {
			service = new Service();
			service.cs = new ClientHttp();
			service.pfactory = new ProtocalFactory();
		}
		return service;
	}

	public void GotoNetwork(final CallBack callBack) {

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {

				int what = message.what;
				switch (what) {
				case 0:
					callBack.onPreExecute();
					break;
				case 1:
					callBack.onPostExecute();
					break;
				}
			}
		};
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(0);
				callBack.doInBackground();
				handler.sendEmptyMessage(1);
			}
		}).start();
		;
	}

	// --------------------------------------------------------------------------
	/**
	 * 释放网络资源
	 */
	public void shutdownNetwork() {
		cs.shutdownNetwork();
		cs = null;
		service = null;
	}

	public boolean netWorkIsOK() {
		return cs.isOK();
	}

	public String getTestData() throws Exception {
		// TODO Auto-generated method stub
		String url = Constants.TEST_URL;
		Protocal protocal = pfactory.bitmapProtocal(url);
		JSONObject json = cs.request(protocal);
		if (json == null) {
			return null;
		}
		return json.toString();
	}

	public Bitmap getBitmapByUrl(String url, ImageOption... option) {
		// TODO Auto-generated method stub
		Protocal protocal = pfactory.bitmapProtocal(url);
		InputStream in = cs.getInputStream(protocal);
		BitmapHandler bhandler = new BitmapHandler();
		Bitmap bp = bhandler.getBitmapByUrl(in, url, option);
		return bp;
	}

	public InputStream getDownLoadInputStream(){
		
		Protocal protocal = pfactory.downloadApkProtocal();
		InputStream iStream = cs.getInputStream(protocal);
		
		return iStream;
	}

	public boolean activateLauncher(){
		// TODO Auto-generated method stub
		Protocal protocal = pfactory.activateProtocal();
		JSONObject result = null;
		try {
			result = cs.request(protocal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ActivateHanlder activate = new ActivateHanlder();
		boolean isActivate = activate.isActivate(result);
	
		return isActivate;
	}
}
