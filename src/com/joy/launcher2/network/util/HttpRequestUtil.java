package com.joy.launcher2.network.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

import com.joy.launcher2.network.impl.ProtocalFactory;
import com.joy.launcher2.util.SystemInfo;
import com.joy.launcher2.util.Util;

import android.util.Log;

public class HttpRequestUtil 
{

	 /** 
     * 发送xml数据 
     * @param path 请求地址 
     * @param xml xml数据 
     * @param encoding 编码 
     * @return 
     * @throws Exception 
     */  
    public static byte[] postXml(String path, String xml, String encoding) throws Exception{  
        byte[] data = xml.getBytes(encoding);  
        URL url = new URL(path);  
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
        conn.setRequestMethod("POST");  
        conn.setDoOutput(true);  
        conn.setRequestProperty("Content-Type", "text/xml; charset="+ encoding);  
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));  
        conn.setConnectTimeout(5 * 1000);  
        OutputStream outStream = conn.getOutputStream();  
        outStream.write(data);  
        outStream.flush();  
        outStream.close();  
        if(conn.getResponseCode()==200){  
            return readStream(conn.getInputStream());  
        }  
        return null;  
    }  
      
    /** 
     * 直接通过HTTP协议提交数据到服务器,实现如下面表单提交功能: 
     *   <FORM METHOD=POST ACTION="http://192.168.0.200:8080/ssi/fileload/test.do" enctype="multipart/form-data"> 
            <INPUT TYPE="text" NAME="name"> 
            <INPUT TYPE="text" NAME="id"> 
            <input type="file" name="imagefile"/> 
            <input type="file" name="zip"/> 
         </FORM> 
     * @param path 上传路径(注：避免使用localhost或127.0.0.1这样的路径测试， 
     *                  因为它会指向手机模拟器，你可以使用http://www.baidu.com或http://192.168.1.10:8080这样的路径测试) 
     * @param params 请求参数 key为参数名,value为参数值 
     * @param file 上传文件 
     */  
    public static boolean post(String path, Map<String, String> params, FormFile[] files) throws Exception  
    {     
        //数据分隔线  
        final String BOUNDARY = "---------------------------7da2137580612";   
        //数据结束标志"---------------------------7da2137580612--"  
        final String endline = "--" + BOUNDARY + "--\r\n";  
          
        //下面两个for循环都是为了得到数据长度参数，依据表单的类型而定  
        //首先得到文件类型数据的总长度(包括文件分割线)  
        int fileDataLength = 0;  
        for(FormFile uploadFile : files)  
        {  
            StringBuilder fileExplain = new StringBuilder();  
            fileExplain.append("--");  
            fileExplain.append(BOUNDARY);  
            fileExplain.append("\r\n");  
            fileExplain.append("Content-Disposition: form-data;name=\""+ uploadFile.getParameterName()+"\";filename=\""+ uploadFile.getFilname() + "\"\r\n");  
            fileExplain.append("Content-Type: "+ uploadFile.getContentType()+"\r\n\r\n");  
            fileExplain.append("\r\n");  
            fileDataLength += fileExplain.length();  
            if(uploadFile.getInStream()!=null){  
                fileDataLength += uploadFile.getFile().length();  
            }else{  
                fileDataLength += uploadFile.getData().length;  
            }  
        }  
        //再构造文本类型参数的实体数据  
        StringBuilder textEntity = new StringBuilder();          
        for (Map.Entry<String, String> entry : params.entrySet())   
        {    
            textEntity.append("--");  
            textEntity.append(BOUNDARY);  
            textEntity.append("\r\n");  
            textEntity.append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n");  
            textEntity.append(entry.getValue());  
            textEntity.append("\r\n");  
        }  
          
        //计算传输给服务器的实体数据总长度(文本总长度+数据总长度+分隔符)  
        int dataLength = textEntity.toString().getBytes().length + fileDataLength +  endline.getBytes().length;  
          
        URL url = new URL(path);  
        //默认端口号其实可以不写  
        int port = url.getPort()==-1 ? 80 : url.getPort();  
        //建立一个Socket链接  
        //Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);
        Socket socket = new Socket();  
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(url.getHost()), port);
        socket.connect(isa, 15 * 1000);
        //获得一个输出流（从Android流到web）  
        OutputStream outStream = socket.getOutputStream();  
        //下面完成HTTP请求头的发送  
        String requestmethod = "POST "+ url.getPath()+" HTTP/1.1\r\n";  
        outStream.write(requestmethod.getBytes());  
        //构建accept  
        String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";  
        outStream.write(accept.getBytes());  
        //构建language  
        String language = "Accept-Language: zh-CN\r\n";  
        outStream.write(language.getBytes());  
        //构建contenttype  
        String contenttype = "Content-Type: multipart/form-data; boundary="+ BOUNDARY+ "\r\n";  
        outStream.write(contenttype.getBytes());  
        //构建contentlength  
        String contentlength = "Content-Length: "+ dataLength + "\r\n";  
        outStream.write(contentlength.getBytes());  
        //构建alive  
        String alive = "Connection: Keep-Alive\r\n";          
        outStream.write(alive.getBytes());  
        //构建host  
        String host = "Host: "+ url.getHost() +":"+ port +"\r\n";  
        outStream.write(host.getBytes());  
        //写完HTTP请求头后根据HTTP协议再写一个回车换行  
        outStream.write("\r\n".getBytes());  
        //把所有文本类型的实体数据发送出来  
        outStream.write(textEntity.toString().getBytes());           
          
        //把所有文件类型的实体数据发送出来  
        for(FormFile uploadFile : files)  
        {  
            StringBuilder fileEntity = new StringBuilder();  
            fileEntity.append("--");  
            fileEntity.append(BOUNDARY);  
            fileEntity.append("\r\n");  
            fileEntity.append("Content-Disposition: form-data;name=\""+ uploadFile.getParameterName()+"\";filename=\""+ uploadFile.getFilname() + "\"\r\n");  
            fileEntity.append("Content-Type: "+ uploadFile.getContentType()+"\r\n\r\n");  
            outStream.write(fileEntity.toString().getBytes());  
            //边读边写  
            if(uploadFile.getInStream()!=null)  
            {  
                byte[] buffer = new byte[1024];  
                int len = 0;  
                while((len = uploadFile.getInStream().read(buffer, 0, 1024))!=-1)  
                {  
                    outStream.write(buffer, 0, len);  
                }  
                uploadFile.getInStream().close();  
            }  
            else  
            {  
                outStream.write(uploadFile.getData(), 0, uploadFile.getData().length);  
            }  
            outStream.write("\r\n".getBytes());  
        }  
        //下面发送数据结束标志，表示数据已经结束  
        outStream.write(endline.getBytes());          
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
        //读取web服务器返回的数据，判断请求码是否为200，如果不是200，代表请求失败  
        if(reader.readLine().indexOf("200")==-1)  
        {  
            return false;  
        }  
        outStream.flush();  
        outStream.close();  
        reader.close();  
        socket.close();  
        return true;  
    } 
    
    
    public static boolean httpPostWithAnnex(String actionUrl, String channel,
    		FormFile[] files) throws IOException {
//    	try {
    	
    	Map<String, String> params = new HashMap<String, String>();  
    	String randomTS = Util.getTS();
		String randomString = Util.randomString(6);
		params.put("op", Integer.toString(ProtocalFactory.OP_BACKUP));
		params.put("channel", channel);
		params.put("sign", ProtocalFactory.getSign(randomTS, randomString));
		params.put("sjz", ProtocalFactory.getSjz(randomString));
		
		
		String BOUNDARY = "---------7d4a6d158c9"; // 数据分隔线
		String MULTIPART_FORM_DATA = "multipart/form-data";
		URL url = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false);// 不使用Cache
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA
				+ "; boundary=" + BOUNDARY);
			
		conn.setRequestProperty("Accept-Encoding", "gzip");
		conn.setRequestProperty("ts",randomTS);
		conn.setRequestProperty("deviceId",SystemInfo.deviceid) ;
		//User user = new User();
		/*AppContext ac = (AppContext)mContext.getApplicationContext();
		User user = ac.user ;
		
		if(user!=null)
			conn.setRequestProperty("sid",user.getSessionId());
		else
			conn.setRequestProperty("sid","");*/
		
		
		StringBuilder sb = new StringBuilder();
		// 上传的表单参数部分
		for (HashMap.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
			sb.append("--");
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"\r\n\r\n");
			sb.append(entry.getValue());
			sb.append("\r\n");
			
			//System.out.println("++++++++++++++++++++++++++++ entry.getKey() :" + entry.getKey()  +  "  entry.getValue():" +entry.getValue());
		}
		DataOutputStream outStream = new DataOutputStream(
				conn.getOutputStream());
		outStream.write(EncodingUtils.getBytes(sb.toString(), "utf-8"));// 发送表单字段数据
		// 上传的文件部分
		for (FormFile file : files) {
			System.out.println("file:::::::::::::::::::::"+file);
			if (file != null) {
				
				String srcPath ="" ;
				srcPath = file.getFilname() ;
				
				
				StringBuilder split = new StringBuilder(); 
				split.append("--");
				split.append(BOUNDARY);
				split.append("\r\n");
				/*split.append("Content-Disposition: form-data;name=\""
						+ file.getFormname() + "\";filename=\""
						+ file.getFileName() + "\"\r\n");*/
				split.append("Content-Disposition: form-data;name=\""
						+ file.getParameterName() + "\";filename=\""
						+ srcPath.substring(srcPath.lastIndexOf("/") + 1) + "\"\r\n");
				split.append("Content-Type: " + file.getContentType()
						+ "\r\n\r\n");
				outStream.write(split.toString().getBytes());
				if (file.getData() == null) {
					if (file.getInStream() != null) {
						byte[] buffer = new byte[1024];
						int length = -1;
						while ((length = file.getInStream().read(buffer)) != -1) {
							outStream.write(buffer, 0, length);
						}
					}
					file.getInStream().close();
				} else {
					outStream.write(file.getData(), 0,
							file.getData().length);
				}
				outStream.write("\r\n".getBytes());
			}
		}
		//String strResult="";
		byte[] end_data = ("--" + BOUNDARY + "--\r\n").getBytes();// 数据结束标志
		outStream.write(end_data);
		outStream.flush();
		int cah = conn.getResponseCode();
		
		System.out.println("conn.getResponseCode()conn.getResponseCode()conn.getResponseCode():"+cah);
		if (cah != 200) {
			throw new RuntimeException("请求url失败");
		}
	
		if(conn.getContentEncoding().equalsIgnoreCase("gzip")) {
			InputStream is = conn.getInputStream();
			InputStream inputStream = new GZIPInputStream(is);
			
			BufferedInputStream bis = new BufferedInputStream(inputStream);
			bis.mark(2);
			// 取前两个字节
			byte[] header = new byte[2];
			int result = bis.read(header);
			// reset输入流到开始位置
			bis.reset();
			// 判断是否是GZIP格式
			 int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);  
		        if(result!=-1 && ss == GZIPInputStream.GZIP_MAGIC) {  
				inputStream= new GZIPInputStream(bis);
			} else {
			        // 取前两个字节
				inputStream= bis;
			}
			//strResult = Utils.convertStreamToString(inputStream, HTTP.UTF_8);
		    //strResult = Util.getBytes(inputStream).toString();
			inputStream.close();

		} else {
			/** 非压缩格式 **/
			InputStream is = conn.getInputStream();
			int ch;
			StringBuilder b = new StringBuilder();
			while ((ch = is.read()) != -1) {
				b.append( ch);
			}
			//strResult = b.toString() ;
		}
		outStream.close();
		conn.disconnect();	
		//return strResult;
    	return true;		
    }
    
      
    /**  
     * 提交数据到服务器  
     * @param path 上传路径(注：避免使用localhost或127.0.0.1这样的路径测试，因为它会指向手机模拟器，你可以使用http://www.baidu.com或http://192.168.1.10:8080这样的路径测试)  
     * @param params 请求参数 key为参数名,value为参数值  
     * @param file 上传文件  
     */  
    public static boolean post(String path, Map<String, String> params, FormFile file) throws Exception  
    {  
       return post(path, params, new FormFile[]{file});  
    }  
    /** 
     * 提交数据到服务器 
     * @param path 上传路径(注：避免使用localhost或127.0.0.1这样的路径测试，因为它会指向手机模拟器，你可以使用http://www.baidu.com或http://192.168.1.10:8080这样的路径测试) 
     * @param params 请求参数 key为参数名,value为参数值 
     * @param encode 编码 
     */  
    public static byte[] postFromHttpClient(String path, Map<String, String> params, String encode) throws Exception  
    {  
        //用于存放请求参数  
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
        for(Map.Entry<String, String> entry : params.entrySet())  
        {  
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));  
        }  
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, encode);  
        HttpPost httppost = new HttpPost(path);  
        httppost.setEntity(entity);  
        //看作是浏览器  
        HttpClient httpclient = new DefaultHttpClient();  
        //发送post请求    
        HttpResponse response = httpclient.execute(httppost);     
        return readStream(response.getEntity().getContent());  
    }  
    /** 
     * 发送请求 
     * @param path 请求路径 
     * @param params 请求参数 key为参数名称 value为参数值 
     * @param encode 请求参数的编码 
     */  
    public static byte[] post(String path, Map<String, String> params, String encode) throws Exception  
    {  
        //String params = "method=save&name="+ URLEncoder.encode("老毕", "UTF-8")+ "&age=28&";//需要发送的参数  
        StringBuilder parambuilder = new StringBuilder("");  
        if(params!=null && !params.isEmpty())  
        {  
            for(Map.Entry<String, String> entry : params.entrySet())  
            {  
                parambuilder.append(entry.getKey()).append("=")  
                    .append(URLEncoder.encode(entry.getValue(), encode)).append("&");  
            }  
            parambuilder.deleteCharAt(parambuilder.length()-1);  
        }  
        byte[] data = parambuilder.toString().getBytes();  
        URL url = new URL(path);  
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
        //设置允许对外发送请求参数  
        conn.setDoOutput(true);  
        //设置不进行缓存  
        conn.setUseCaches(false);  
        conn.setConnectTimeout(5 * 1000);  
        conn.setRequestMethod("POST");  
        //下面设置http请求头  
        conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");  
        conn.setRequestProperty("Accept-Language", "zh-CN");  
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");  
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));  
        conn.setRequestProperty("Connection", "Keep-Alive");  
          
        //发送参数  
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());  
        outStream.write(data);//把参数发送出去  
        outStream.flush();  
        outStream.close();  
        if(conn.getResponseCode()==200)  
        {  
            return readStream(conn.getInputStream());  
        }  
        return null;  
    }  
      
    /**  
     * 读取流  
     * @param inStream  
     * @return 字节数组  
     * @throws Exception  
     */  
    public static byte[] readStream(InputStream inStream) throws Exception  
    {  
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];  
        int len = -1;  
        while( (len=inStream.read(buffer)) != -1)  
        {  
            outSteam.write(buffer, 0, len);  
        }  
        outSteam.close();  
        inStream.close();  
        return outSteam.toByteArray();  
    }  
	
}
