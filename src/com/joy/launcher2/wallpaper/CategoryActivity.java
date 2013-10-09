package com.joy.launcher2.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.joy.launcher2.R;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.wallpaper.WallpaperActivity.SimpleAdapter;
import com.joy.launcher2.wallpaper.WallpaperActivity.ViewHolder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 显示某一类别的壁纸列表
 * @author huangming
 *
 */
public class CategoryActivity extends Activity implements ImageLoader.Callback, OnItemClickListener, OnScrollListener
{

	private final static int ACTIVITY_TYPE = 3;
	private final static boolean DEBUG = false;
	private final static String TAG = "CategoryActivity";
	GridView categoryGridView;
	TextView categoryTitle;
	TextView categoryDiscription;
	int categoryType = 3;
	ImageLoader imageLoader;
	String titleStr;
	String discriptionStr;
	int screenWidth;
    int screenHeight;
    boolean isLoaded = false;
    
    int previousPageIndex = 0;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joy_wallpaper_category_main);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		imageLoader = ImageLoader.getInstance(getApplicationContext());
		imageLoader.setScreenSize(screenWidth, screenHeight);
		imageLoader.setCallback(this, ACTIVITY_TYPE);
		
		
		Intent intent = getIntent();
		if(intent != null)
		{
			categoryType = intent.getIntExtra(Constants.CATEGORY_TYPE, 3);
			titleStr = intent.getStringExtra(Constants.CATEGORY_NAME);
			discriptionStr = intent.getStringExtra(Constants.CATEGORY_DESCRIPTION);
		}
		
		if(titleStr == null || discriptionStr== null)
		{
			finish();
			return;
		}
		categoryGridView = (GridView)findViewById(R.id.gridview_category);
		categoryTitle = (TextView)findViewById(R.id.category_title);
		categoryDiscription = (TextView)findViewById(R.id.category_discription);
		categoryTitle.setText(titleStr);
		categoryDiscription.setText(discriptionStr);
		isLoaded = imageLoader.isCategoryloaded(categoryType);
		if(!isLoaded)
		{
			showProgressBar();
			imageLoader.parseJSON(ACTIVITY_TYPE, categoryType, previousPageIndex);	
		}
		else
		{
			setAdapter(imageLoader.getInfos(categoryType), categoryType,true);
		}
		
		
		categoryGridView.setOnScrollListener(this);
		categoryGridView.setOnItemClickListener(this);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch(scrollState)
		{
		    case OnScrollListener.SCROLL_STATE_FLING:
		    	imageLoader.lock();
		    	break;
		    case OnScrollListener.SCROLL_STATE_IDLE:
		    	if(view.getLastVisiblePosition() == view.getCount() - 1)
		    	{
		                	showProgressBar();
				    		previousPageIndex++;
				    		imageLoader.parseJSON(ACTIVITY_TYPE, categoryType, previousPageIndex);
		        }
		    	imageLoader.unlock();
		    	break;
		    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		    	imageLoader.lock();
		    	break;
			default:
				break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}
	
	public void setCategoryNameAndDis(String name, String dis)
	{
		titleStr = name;
		discriptionStr = dis;
		categoryTitle.setText(titleStr);
		categoryDiscription.setText(discriptionStr);
	}
	
	@Override
	public void setAdapter(ArrayList<WallpaperInfo> wis, int categoryType, boolean loadSuccess) {
		// TODO Auto-generated method stub
		dismissProgressBar();
		SimpleAdapter adapter = null;
		if(!loadSuccess)
		{
			previousPageIndex--;
		}
		if(categoryGridView.getAdapter() instanceof SimpleAdapter)
		{
			adapter = (SimpleAdapter)categoryGridView.getAdapter();
			adapter.notifyDataSetChanged();
		}
		else
		{
			categoryGridView.setAdapter(new SimpleAdapter(getLayoutInflater(),wis, imageLoader));
		}
		imageLoader.unlock();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		ViewHolder holder = (ViewHolder)view.getTag();
		Intent intent = new Intent(this, PreviewActivity.class);
		intent.putExtra(Constants.POSITION, position);
		intent.putExtra(Constants.CATEGORY_TYPE, categoryType);
		startActivity(intent);
		if(holder != null)
		{
			WallpaperInfo wi = (WallpaperInfo)holder.image.getTag();
		}
	}

	@Override
	public void setRecommend(List<CategoryInfo> cis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void success(boolean s, WallpaperInfo wi) {
		// TODO Auto-generated method stub
		
	}
	
	private void showProgressBar()
	{
		if(categoryGridView != null)
		{
			View parent = (View)categoryGridView.getParent().getParent();
			View progressBar = parent.findViewById(R.id.progress_bar_bottom);
			if(parent instanceof ViewGroup && progressBar == null)
			{
				progressBar = getLayoutInflater().inflate(R.layout.joy_wallpaper_progressbar_bottom, (ViewGroup) parent);
			}
		}
		
	}
	
	private void dismissProgressBar()
	{
		if(categoryGridView != null)
		{
			View parent = (View)categoryGridView.getParent().getParent();
			View progressBar = parent.findViewById(R.id.progress_bar_bottom);
			if(parent instanceof ViewGroup && progressBar != null)
			{
				((ViewGroup)parent).removeView(progressBar);
			}
		}
	}
	
	private void display(int textId)
	{
		Toast.makeText(this, textId,Toast.LENGTH_SHORT).show();
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(imageLoader != null)imageLoader.unlock();
		if(categoryGridView != null && categoryGridView.getAdapter() instanceof SimpleAdapter)
		{
			((SimpleAdapter)categoryGridView.getAdapter()).notifyDataSetChanged();
		}
	}
}
