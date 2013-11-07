package com.joy.launcher2.network.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;

import com.joy.launcher2.cache.ImageOption;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;

/**
 * 根据流生成图片
 * @author wanghao
 *
 */
public class BitmapHandler {

	/**
	 * 默认是获取.png格式图片
	 * @param in
	 * @param url
	 * @param option
	 * @return
	 */
	public Bitmap getBitmapByUrl(InputStream in,String url,ImageOption... option){
		return getBitmapByUrl(in, url, ".png", option);
	}
	/**
	 * 从网络上根据url获取，生成指定后缀名suffix的图片
	 * @param in
	 * @param url
	 * @param suffix
	 * @param option
	 * @return
	 */
	public Bitmap getBitmapByUrl(InputStream in,String url,String suffix,ImageOption... option){
		InputStream is = in;
		if (is == null) {
			return null;
		}
		byte[] b = Util.getBytes(is);
		if(b==null){
			return null;
		}
		Bitmap bm = null;
		if (option != null && option.length > 0) {
			ImageOption op = option[0];
			
			int width = op.getWidth();
			int height = op.getHeight();
			
			Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(b, 0, b.length, opts);
			opts.inJustDecodeBounds = false;
			if(op.isInclinationWidth()&&opts.outHeight>opts.outWidth){//倾向宽，但当前高比宽长的
				opts.inSampleSize = opts.outHeight/width;
				bm = BitmapFactory.decodeByteArray(b, 0, b.length, opts);
				//旋转90度
				Matrix matrix = new Matrix(); 
			    matrix.postRotate(-90); 
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			}else if(op.isInclinationHeight()&&opts.outWidth>opts.outHeight){
				opts.inSampleSize = opts.outWidth/height;
				bm = BitmapFactory.decodeByteArray(b, 0, b.length, opts);
				//旋转90度
				Matrix matrix = new Matrix(); 
			    matrix.postRotate(-90); 
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			}else{
				int x = opts.outWidth / width;
				int y = opts.outHeight / height;
				opts.inSampleSize = x > y ? x : y;
				bm = BitmapFactory.decodeByteArray(b, 0, b.length, opts);
			}
			
		} else {
			bm = BitmapFactory.decodeByteArray(b, 0, b.length);
		}

		// 存入SD卡
		if (Util.hasSdcard()&&bm!=null) {
			try {
				String fileName = Util.getFileNameByUrl(url)+suffix;
				File file = new File(Constants.DOWNLOAD_IMAGE_DIR + "/"+ fileName);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(file);
				if (fileName.toUpperCase().endsWith(".PNG")) {
					bm.compress(CompressFormat.PNG, 100, fos);
				} else {
					bm.compress(CompressFormat.JPEG, 100, fos);
				}
			} catch (FileNotFoundException e) {
//				Logger.error(e);
			}
		}

		return bm;
	}
}
