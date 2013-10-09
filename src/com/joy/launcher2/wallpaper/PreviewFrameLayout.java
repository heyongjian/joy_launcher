package com.joy.launcher2.wallpaper;


import com.joy.launcher2.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 预览图片的FrameLayout，控制进度条显示和消失
 * @author huangming
 *
 */
public class PreviewFrameLayout extends FrameLayout{

	LayoutInflater inflater;
	
	public PreviewFrameLayout(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}
	
	public PreviewFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}
	
	public PreviewFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflater = LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}

	
	public void showProgressBar()
	{
		
		View progressBar = findViewById(R.id.progressbar_center);
		if(progressBar == null)
		{
			progressBar = inflater.inflate(R.layout.joy_wallpaper_progressbar_center, this);
		}
	}
	
	public void dismissProgressBar()
	{
		
			View progressBar = findViewById(R.id.progressbar_center);
			if(progressBar != null)
			{
				removeView(progressBar);
			}
		
	}
	

}
