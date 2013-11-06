package com.joy.launcher2.wallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.joy.launcher2.R;
import com.joy.launcher2.util.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 预览图片Activity
 * @author huangming
 *
 */
public class PreviewActivity extends Activity implements ImageLoader.Callback, OnPageChangeListener, OnClickListener{

	private final static int ACTIVITY_TYPE = 2;
	private final static boolean DEBUG = false;
	private final static String TAG = "PreviewActivity";
	int type;
	
	private TextView imageName;
	private Button previewBtn;
	private Button applyOrDownloadBtn;
	private View bottom;
	private PreviewPager pager;
	private ImageView previewIcon;
	private FrameLayout progressContainer;
	
	private ImageLoader mImageLoader;
	
	int categoryType = -1;
	int initialPosition = 0;
	
	int page = 0;
	int screenWidth;
    int screenHeight;
    int previousPageIndex = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joy_wallpaper_preview_main);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		Intent intent = getIntent();
		if(intent != null)
		{
			categoryType = -intent.getIntExtra(Constants.CATEGORY_TYPE, 1);
			initialPosition = intent.getIntExtra(Constants.POSITION, 0);
		}
		page = initialPosition;
		mImageLoader = ImageLoader.getInstance(getApplicationContext());
		mImageLoader.setScreenSize(screenWidth, screenHeight);
		mImageLoader.setCallback(this, ACTIVITY_TYPE);
		mImageLoader.unlock();
		progressContainer = (FrameLayout)findViewById(R.id.progress_container);
		pager = (PreviewPager)findViewById(R.id.preview_pager);
		pager.setPreviewActivity(this);
		imageName = (TextView)findViewById(R.id.image_name);
		previewBtn = (Button)findViewById(R.id.preview);
		previewBtn.setOnClickListener(this);
		applyOrDownloadBtn = (Button)findViewById(R.id.apply_or_download);
		applyOrDownloadBtn.setOnClickListener(this);
		bottom = findViewById(R.id.bottom);
		mImageLoader.parseJSON(ACTIVITY_TYPE, categoryType, previousPageIndex);

		previewIcon =(ImageView) findViewById(R.id.preview_icon);
		previewIcon.setClickable(false);
		//previewIcon.setOnClickListener(this);
		previewBtn.setText(R.string.preview_text);	
		pager.setOnPageChangeListener(this);	
	}
	
	class SimplePagerAdapter extends PagerAdapter
	{
		ArrayList<WallpaperInfo> wis;
		
		SimplePagerAdapter( ArrayList<WallpaperInfo> wis)
		{
			this.wis = wis;
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
		
		public ArrayList<WallpaperInfo> getWallpaperInfos()
		{
			return wis;
		}
					
		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			//super.destroyItem(container, position, object);
			if ((object instanceof ImageView) && (container instanceof ViewPager)) 
			{
				View child = (ImageView)object;
				ViewPager p = (ViewPager)container;
				int count = p.getChildCount();
				for(int i = 0; i < count; i++)
				{
					if(p.getChildAt(i) == child)
					{
						p.removeView(child);
					}
				}
				
			}
			
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			PreviewFrameLayout fl = (PreviewFrameLayout)getLayoutInflater().inflate(R.layout.joy_wallpaper_preview_image, null);
			ImageView image = (ImageView)fl.findViewById(R.id.preview_image);
			WallpaperInfo wi = wis.get(position);
			image.setImageResource(R.drawable.joy_wallpaper_resource_preview_bg);
			image.setTag(wi);
			fl.showProgressBar();
			mImageLoader.addLoadAndDisplayTask(wi, image);
			((ViewPager)container).addView(fl);
			return fl;
		}

		@Override
		public int getCount() {
			return wis.size();
		}
	}
	


	@Override
	public void setAdapter(ArrayList<WallpaperInfo> wis, int categoryType , boolean success) {
		// TODO Auto-generated method stub
		pager.setAdapter(new SimplePagerAdapter(wis));
		pager.setCurrentItem(initialPosition);
		WallpaperInfo currentInfo = ((SimplePagerAdapter)pager.getAdapter()).getWallpaperInfos().get(initialPosition);
		String currentName = currentInfo.wallpaperName;
		int size= currentInfo.size;
		mImageLoader.unlock();
		imageName.setText(getTitle(currentInfo));;
		if(mImageLoader.isApplyOrDown(currentInfo))
		{
			applyOrDownloadBtn.setText(R.string.apply_text);
		}
		else
		{
			applyOrDownloadBtn.setText(R.string.download_text);
		}
		
		
	}

	@Override
	public void setCategoryNameAndDis(String name, String dis) {
		// TODO Auto-generated method stub
		
	}


	public String getTitle(WallpaperInfo wi)
	{
		if(wi.id == Integer.MIN_VALUE)return wi.wallpaperName;
		return wi.wallpaperName +"("+wi.size+"KB)";
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

		if(page != arg0)
		{
			page = arg0;
			WallpaperInfo currentInfo = ((SimplePagerAdapter)pager.getAdapter()).getWallpaperInfos().get(page);
			String currentName = currentInfo.wallpaperName;
			int size = currentInfo.size;
			imageName.setText(getTitle(currentInfo));
			if(mImageLoader.isApplyOrDown(currentInfo))
			{
				applyOrDownloadBtn.setText(R.string.apply_text);
			}
			else
			{
				applyOrDownloadBtn.setText(R.string.download_text);
			}
		}
	}



	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}


	private void showProgressBar()
	{
		int visible = progressContainer.getVisibility();
		if(visible != View.VISIBLE)
		{
			progressContainer.setVisibility(View.VISIBLE);
		}
	}
	
	private void dismissProgressBar()
	{
		int visible = progressContainer.getVisibility();
		if(visible != View.GONE)
		{
			progressContainer.setVisibility(View.GONE);
		}
	}
	

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id)
		{
		case R.id.preview:
	
			previewIcon.setVisibility(View.VISIBLE);
			
		    Animation bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_bottom_exit);
		    Animation titleAnimation = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_title_exit);
		    Animation iconAnimation = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_icon_enter);
		    bottomAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					bottom.setVisibility(View.GONE);
				}
			});
		    titleAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					imageName.setVisibility(View.GONE);
				}
			});
		    
		    bottom.startAnimation(bottomAnimation);
		    imageName.startAnimation(titleAnimation);
		    previewIcon.startAnimation(iconAnimation);
			
			break;
		case R.id.apply_or_download:
			WallpaperInfo currentInfo = ((SimplePagerAdapter)pager.getAdapter()).getWallpaperInfos().get(page);
			
			if(mImageLoader.isApplyOrDown(currentInfo))
			{
				boolean success = applyWallpaper(currentInfo.urls[1], currentInfo.id == Integer.MIN_VALUE);
				int textId = success?R.string.apply_success:R.string.apply_error;
				display(textId);
				if(success && !currentInfo.isNative)
				{
					mImageLoader.saveOnlineToNative(ACTIVITY_TYPE ,currentInfo);
				}
			}
			else
			{
				showProgressBar();
				downloadWallpaper(currentInfo);
			}
			
			break;
		case R.id.preview_icon:
			
			break;
		default:
			break;
	    }
	}
	
	public void dismissPreviewIcon()
	{
		if(previewIcon.getVisibility() == View.VISIBLE)
		{
			//previewIcon.setVisibility(View.GONE);
			bottom.setVisibility(View.VISIBLE);
			
		    imageName.setVisibility(View.VISIBLE);
		    
		    
		    Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_bottom_enter);
		    Animation titleAnim = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_title_enter);
		    Animation iconAnim = AnimationUtils.loadAnimation(this, R.anim.joy_wallpaper_preview_icon_exit);
		    iconAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					previewIcon.setVisibility(View.GONE);
				}
			});
		    
		    bottom.startAnimation(bottomAnim);
		    imageName.startAnimation(titleAnim);
		    previewIcon.startAnimation(iconAnim);
		}
	}
	
	private void downloadWallpaper(WallpaperInfo wi)
	{
		mImageLoader.downloadOriginWallpaper(ACTIVITY_TYPE, wi);
	}
	
	
	@SuppressLint("ServiceCast")
	private boolean  applyWallpaper(String originalUrl, boolean isNativeRes) {
		boolean successful = false;
		WallpaperManager wpm = (WallpaperManager)getSystemService(Context.WALLPAPER_SERVICE);
		if(isNativeRes)
		{
			int resId = Integer.parseInt(originalUrl);
            try {	        	
            	wpm.setResource(resId);
	            successful = true;
	        } catch (IOException e) {
	            Log.e("set wallpaper", "Failed to set wallpaper: " + e);
	        } finally {
	        }
            return successful;
		}
		InputStream is = mImageLoader.getDownloadedInputStream(originalUrl);
		if(is != null)
		{
			try {	        	
	            
	            wpm.setStream(is);
	            successful = true;
	        } catch (IOException e) {
	            Log.e("set wallpaper", "Failed to set wallpaper: " + e);
	        } finally {
	        	try {
					if(is != null)is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		}
        return successful;
    }



	@Override
	public void setRecommend(List<CategoryInfo> cis) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void success(boolean s, WallpaperInfo wi) {
		// TODO Auto-generated method stub
		WallpaperInfo currentInfo = ((SimplePagerAdapter)pager.getAdapter()).getWallpaperInfos().get(page);
		dismissProgressBar();
		if(wi == currentInfo)
		{
			int textId = s?R.string.download_success:R.string.download_error;
			display(textId);
			if(DEBUG)Log.e(TAG, "load success : " + mImageLoader.isApplyOrDown(currentInfo));
			if(mImageLoader.isApplyOrDown(currentInfo))
			{
				applyOrDownloadBtn.setText(R.string.apply_text);
			}
			else
			{
				applyOrDownloadBtn.setText(R.string.download_text);
			}
		}
		
	}
	
	private void display(int textId)
	{
		Toast.makeText(this, textId,Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mImageLoader != null)mImageLoader.unlock();
		if(pager != null && pager.getAdapter() instanceof SimplePagerAdapter)
		{
			((SimplePagerAdapter)pager.getAdapter()).notifyDataSetChanged();
		}
	}
	
	

}
