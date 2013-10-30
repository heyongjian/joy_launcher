package com.joy.launcher2.push;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import com.android.internal.telephony.MccTable;
import com.joy.launcher2.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

/**
 * 下载的基本信息
 * @author 王浩
 *
 */
public class PushDownloadInfo implements Serializable{

	private int id;// id
	private String filename;// 软件名称
	private String localname;// 软件名称
	private String url;// 下载地址
	private int filesize;// 文件总大小
	private int completesize;// 已经下载大小---通过保存
	
	private byte[] apkIconBuffer;
	private int downloadType;
	private String title;
	
	public void setApkIconBuffer(byte[] apkIconBuffer)
	{
		this.apkIconBuffer = apkIconBuffer;
	}
	
	public byte[] getApkIconBuffer()
	{
		return apkIconBuffer;
	}
	
	public void setDownloadType(int downloadType)
	{
		this.downloadType = downloadType;
	}
	
	public int getDownloadType()
	{
		return downloadType;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
 
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getLocalname() {
		return localname;
	}

	public void setLocalname(String localname) {
		this.localname = localname;
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getFilesize() {
		return filesize;
	}

	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}

	public int getCompletesize() {
		return completesize;
	}

	public void setCompletesize(int completesize) {
		this.completesize = completesize;
	}
	
	public static Bitmap getApkIcon(byte[] data)
	{
		try {
			return BitmapFactory.decodeByteArray(data, 0, data.length);
	    } catch (Exception e) {
	        return null;
	    } 
	}
	
	public static byte[] bitmapToBytes(Bitmap bm) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
	        return baos.toByteArray();
		} catch (Exception e) {
			return null;
	    }
	}
	@Override
	public String toString() {
		return "DownloadInfo [id=" + id + ", filename=" + filename
				+ ", localname=" + localname
				+ ", url=" + url + ", filesize=" + filesize + ", completesize="
				+ completesize +  "]";
	}

}

