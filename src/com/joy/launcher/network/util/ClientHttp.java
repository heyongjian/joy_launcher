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
import org.json.JSONTokener;

import com.joy.launcher.util.Constants;
import com.joy.launcher.util.Util;


/**
 * 网络处理
 * @author wanghao
 *
 */
public class ClientHttp implements ClientInterface {

	private String TAG = "ClientHttp";
	
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
		try {
			InputStream in = getInputStream(protocal);
			Util.i(TAG, " in : "+in);
			if(in == null){
				return null;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while((line = reader.readLine()) != null){
				buffer.append(line);
			}
			reader.close();
			in.close();
			json = (JSONObject)new JSONTokener(buffer.toString()).nextValue();
//			Log.e("", "URL:" + url + ",获取到服务器数据:" + json);
			Util.i(TAG, "  str : "+json.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			Util.i(TAG, "网络异常    -----------》3");
		}
		return json;
	}

	/**
	 * 获取输入流
	 */
	public InputStream getInputStream(Protocal protocal){
		if(!Util.isNetworkConnected()){
			Util.i(TAG, "没有打开网络连接！");
			return null;
		}
		DefaultHttpClient httpClient = null;

		InputStream result = null;
		try {
			// ��������
//			httpClient = NetworkinfoParser.getHttpConnector(marketContext);
			httpClient = new DefaultHttpClient();  
			// �Ƿ�Ҫ�������Ի���
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
			// url
			if (protocal.getGetData() != null) {
				urlStrl += "?" + protocal.getGetData();
			}
			Util.i(TAG, "urlStrl： "+urlStrl);
			
			HttpRequestBase httpRequest = null;
			// post
			if (protocal.getPostData() != null) {
				httpRequest = new HttpPost(urlStrl);
				byte[] sendData = protocal.getPostData().toString().getBytes("UTF-8");
				((HttpPost) httpRequest).setEntity(new ByteArrayEntity(sendData));
			} else {
				httpRequest = new HttpGet(urlStrl);
			}
			Util.i(TAG, "网络连接  1");
			
			httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : 15000);

			//添加头
			httpRequest.addHeader("ts", Util.getTS());//–随机数
			httpRequest.addHeader("deviceId", Util.getDeviceID());// –唯一设备号
			httpRequest.addHeader("Accept-Encoding", "gzip");
			httpRequest.addHeader("Content-Type", "text/json;charset=UTF-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);
			int httpCode = httpResponse.getStatusLine().getStatusCode();
			if (httpCode == HttpURLConnection.HTTP_OK) {
				Header encodeHader = httpResponse.getLastHeader("Content-Encoding");
				if (encodeHader != null && "gzip".equals(encodeHader.getValue())) {
					result = handleReponse(httpResponse, true);
				} else {
					result = handleReponse(httpResponse, false);
				}
			}else{
				Util.i(TAG, "网络异常    -----------》2");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			Util.i(TAG, "网络异常    -----------》1");
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
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

			Util.i(TAG, "requestServiceResource response executionCount: " + executionCount + " exception:"+ exception);
			
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
