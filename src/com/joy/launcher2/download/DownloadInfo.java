package com.joy.launcher2.download;

import java.io.Serializable;

import android.view.View;

/**
 * 下载的基本信息
 * @author 王浩
 *
 */
public class DownloadInfo implements Serializable{

	private int id;// id
	private String filename;// 软件名称
	private String localname;// 软件名称
	private String url;// 下载地址
	private int filesize;// 文件总大小
	private int completesize;// 已经下载大小---通过保存
	private View view;//所属view
	
	public void setView(View view){
		this.view = view;
	}
	public View getView(){
		return view;
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
	
	@Override
	public String toString() {
		return "DownloadInfo [id=" + id + ", filename=" + filename
				+ ", localname=" + localname
				+ ", url=" + url + ", filesize=" + filesize + ", completesize="
				+ completesize + ", view=" + view + "]";
	}

}

