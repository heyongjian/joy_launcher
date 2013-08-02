package com.joy.launcher.cache;
/**
 * 处理图片时的参数
 * @author wanghao
 *
 */
public class ImageOption {
	private boolean inclinationWidth;// 倾向宽(遇到高比宽长的，旋转90度)
	private boolean inclinationHeight;// 倾向高(遇到宽比高长的，旋转90度)
	private int width = -1; // 下载后的图片宽
	private int height = -1;// 下载后的图片高

	public ImageOption() {

	}

	public ImageOption(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public ImageOption(int width,boolean inclinationWidth) {
		this.width = width;
		this.inclinationWidth = inclinationWidth;
	}


	public int getWidth() {
		return width;
	}

	public boolean isInclinationWidth() {
		return inclinationWidth;
	}

	public void setInclinationWidth(boolean inclinationWidth) {
		this.inclinationWidth = inclinationWidth;
	}

	public boolean isInclinationHeight() {
		return inclinationHeight;
	}

	public void setInclinationHeight(boolean inclinationHeight) {
		this.inclinationHeight = inclinationHeight;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
