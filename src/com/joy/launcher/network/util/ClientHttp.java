package com.joy.launcher.network.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.util.Log;

import com.joy.launcher.network.impl.ProtocalFactory;
import com.joy.launcher.util.Constants;
import com.joy.launcher.util.SystemInfo;
import com.joy.launcher.util.Util;


/**
 * 网络处理
 * @author wanghao
 *
 */
public class ClientHttp implements ClientInterface {

	private static final String TAG = "ClientHttp";
	private static final Boolean DEBUG = true;
	@Override
	public JSONObject request(Protocal protocal) throws Exception {
		JSONObject data = post(protocal);
		return data;
	}
	
	@Override
	public void shutdownNetwork() {
		
	}

	@Override
	public boolean isOK() {
		return true;
	}
	
	public JSONObject post(Protocal protocal) throws Exception {
		JSONObject json = null;
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer buffer = new StringBuffer();
		try {
			in = getInputStream(protocal);
			Log.i(TAG, " in : "+in);
			if(in == null){
				return null;
			}
			
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			String line = null;
			while((line = reader.readLine()) != null){
//				Log.i(TAG, "  line : "+line);
				buffer.append(line);
			}
			
			try {
//				Log.i(TAG, "  buffer : "+buffer.toString());
				json = new JSONObject(buffer.toString());
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "  json异常 : "+e);
			}
//			Log.e("", "URL:" + url + ",获取到服务器数据:" + json);
			Log.i(TAG, "  str : "+json.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i(TAG, "网络异常    -----------》3  "+ex);
		}
		finally {
			if(reader != null){
				reader.close();
				reader = null;
			}
			if(in != null){
				in.close();
				in = null;
			}
			buffer = null;
		}
		return json;
	}

	/**
	 * 获取输入流
	 */
	public InputStream getInputStream(Protocal protocal){
		if(!Util.isNetworkConnected()){
			if(DEBUG) Log.e(TAG, "---getInputStream 没有打开网络连接！");
			return null;
		}
		DefaultHttpClient httpClient = new DefaultHttpClient();  

		InputStream result = null;
		try {
			if (protocal.isReTry()) {
				httpClient.setHttpRequestRetryHandler(new RetryHandler());
			}
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT); // ���ӳ�ʱ

			String urlStrl = null;
			if (protocal.getHost() == null) {
				urlStrl = Constants.BASE_URL;
			} else {
				urlStrl = protocal.getHost();
			}
			String randomTS = Util.getTS();
			// url
			if (protocal.getGetData() != null) {
				urlStrl += "?" + protocal.getGetData() +ProtocalFactory.getSign(randomTS);
			}
			if(DEBUG) Log.i(TAG, "---getInputStream urlStrl： "+urlStrl);
			
			HttpRequestBase httpRequest = null;
			// post
			if (protocal.getPostData() != null) {
				httpRequest = new HttpPost(urlStrl);
				byte[] sendData = protocal.getPostData().toString().getBytes("UTF-8");
				((HttpPost) httpRequest).setEntity(new ByteArrayEntity(sendData));
			} else {
				httpRequest = new HttpGet(urlStrl);
			}
			Log.i(TAG, "网络连接  1");
			
			httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT);

			//添加头
			httpRequest.addHeader("ts", randomTS);//–随机数
			httpRequest.addHeader("deviceId", SystemInfo.deviceid);// –唯一设备号
			httpRequest.addHeader("Accept-Encoding", "gzip");
			httpRequest.addHeader("Content-Type", "text/json;charset=UTF-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);
			int httpCode = httpResponse.getStatusLine().getStatusCode();
			if(DEBUG) Log.i(TAG, "-----httpCode-------"+httpCode);
			if (httpCode == HttpURLConnection.HTTP_OK) {
				Header encodeHader = httpResponse.getLastHeader("Content-Encoding");
				if (encodeHader != null && "gzip".equals(encodeHader.getValue())) {
					result = handleReponse(httpResponse, true);
				} else {
					result = handleReponse(httpResponse, false);
				}
			}else{
				if(DEBUG) Log.e(TAG, "---getInputStream 网络异常    -----------》2");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			if(DEBUG) Log.e(TAG, "---getInputStream 网络异常    -----------》1");
		}
		return result;
	}
	/**
	 * 转换方法  HttpResponse->InputStream
	 * @param response
	 * @param gzip
	 * @return
	 * @throws IOException
	 */
	private InputStream handleReponse(HttpResponse response, boolean gzip) throws IOException {
		InputStream is = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			if (gzip) {
				is = new GZIPInputStream(entity.getContent(), 8196);
			} else {
				is = new BufferedInputStream(entity.getContent());
			}
		}
		return is;
	}
	/**
	 * 设置重连机制和异常自动恢复处理
	 * @author User
	 *
	 */
	private class RetryHandler implements HttpRequestRetryHandler {

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

			if(DEBUG) Log.i(TAG, "---retryRequest requestServiceResource response executionCount: " + 
					executionCount + " exception:"+ exception);
			
			if (executionCount > 3) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// Do not retry on SSL handshake exception
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}

	}

}
