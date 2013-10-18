package com.joy.launcher2;

import com.joy.launcher2.preference.PreferencesProvider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DesktopIndicator extends ViewGroup implements AnimationListener 
{
    private static final String TAG = "DesktopIndicator";
	private View mIndicator;
	public static final int INDICATOR_TYPE_IOS = 1;
	public static final int INDICATOR_TYPE_SAMSUNG = 2;
	public static final int INDICATOR_TYPE_TOP = 3;
	private int mIndicatorType = 1;
	private int mItems = 5;
	private int mCurrent = 0;
	private int mVisibleTime = -1;
	private int mDeviceWith = -1;
	private Animation mAnimation;
	private Handler mHandler = new Handler();
	private LinearLayout.LayoutParams params;
	
	public DesktopIndicator(Context context) 
	{
		super(context);
		initIndicator(context);
	}
	
	public DesktopIndicator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initIndicator(context);
	}
	
	public DesktopIndicator(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initIndicator(context);
	}
	
	private void initIndicator(Context context)
	{
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mDeviceWith = context.getResources().getDisplayMetrics().widthPixels;
		switch(mIndicatorType)
		{
		case INDICATOR_TYPE_IOS:
			mItems = PreferencesProvider.Interface.Homescreen.getNumberHomescreens();
			mCurrent = PreferencesProvider.Interface.Homescreen.getDefaultHomescreen(mItems / 2);
			mIndicator = new viewPagerIndicator(context);
			((viewPagerIndicator)mIndicator).setTotalItems(mItems);
			((viewPagerIndicator)mIndicator).setCurrentItem(mCurrent);
			break;
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
			break;
		}
		addView(mIndicator);
	}
	
	public void setItems(int items)
	{
//		Log.e(TAG, "----setItems: " + items);
		mItems = items;
		switch(mIndicatorType)
		{
		case INDICATOR_TYPE_IOS:
			removeView(mIndicator);
			initIndicator(getContext());
			break;
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		// TODO Auto-generated method stub
		int realHeight = 0;
		switch(mIndicatorType)
		{
		case INDICATOR_TYPE_IOS:
			realHeight = 20;
			mIndicator.measure(getWidth(), realHeight);
			break;
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
			break;
		}
		int realHeightMeasurespec = MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, realHeightMeasurespec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) 
	{
		// TODO Auto-generated method stub
		switch(mIndicatorType)
		{
		case INDICATOR_TYPE_IOS:
			mIndicator.measure(getWidth(), 20);
			mIndicator.setLayoutParams(params);
			mIndicator.layout(0, 0, getWidth(), 20);
			break;
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
			break;
		}
	}
	public void indicate(float percent)
	{
		setVisibility(View.VISIBLE);
		int position = Math.round(mItems*percent);
		switch(mIndicatorType){
		case INDICATOR_TYPE_IOS:
			((viewPagerIndicator) mIndicator).setCurrentItem(position);
			break;
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
			break;
		}
		mHandler.removeCallbacks(mAutoHide);
		if(mVisibleTime>0)
			mHandler.postDelayed(mAutoHide, mVisibleTime);
		mCurrent = position;
	}
	
	/**
	 * the values of screenScroll is real-time from onTouchEvent(eg:workspace screenScroll).
	 * use this can make indicator performer actually.
	 * @param screenScroll
	 */
	public void fullIndicate(int screenScroll)
	{
		Log.e(TAG, "----fullIndicate:screenScrill " + screenScroll + "curr: " + 
				screenScroll / mDeviceWith + " " + screenScroll % mDeviceWith);
		setVisibility(View.VISIBLE);
		switch(mIndicatorType)
		{
		case INDICATOR_TYPE_IOS:
		case INDICATOR_TYPE_SAMSUNG:
		case INDICATOR_TYPE_TOP:
			break;
		}
		mHandler.removeCallbacks(mAutoHide);
		if(mVisibleTime>0)
			mHandler.postDelayed(mAutoHide, mVisibleTime);
	}
	
	public void setType(int type)
	{
		if(type != mIndicatorType)
		{
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(this.getLayoutParams());
			if(type == INDICATOR_TYPE_TOP)
			{
				lp.gravity = Gravity.TOP;
			}
			setLayoutParams(lp);
			mIndicatorType = type;
			removeView(mIndicator);
			initIndicator(getContext());
		}
	}
	
	public void setAutoHide(boolean autohide)
	{
		if(autohide)
		{
			mVisibleTime = 300;
			setVisibility(INVISIBLE);
		}
		else
		{
			mVisibleTime = -1;
			setVisibility(VISIBLE);
		}
	}
	
	private Runnable mAutoHide = new Runnable()
	{
		public void run()
		{
			if(mAnimation == null)
			{
				mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast);
				mAnimation.setAnimationListener(DesktopIndicator.this);
			}
			else
			{
				try{
					//This little thing seems to be making some androids piss off
					if(!mAnimation.hasEnded())mAnimation.cancel();
				}
				catch (NoSuchMethodError e)
				{
				}
			}
			startAnimation(mAnimation);				   
		}
	};	
	
	/**
	 * custom view for ios style indicator.
	 * @author yongjian.he
	 *
	 */
	private class viewPagerIndicator extends ViewGroup{	
		private int mTotalItems;
		private int mCurrentItem;
		private int mDotDrawableId;
		
		public viewPagerIndicator(Context context, AttributeSet attrs) 
		{
			super(context, attrs);
			// TODO Auto-generated constructor stub
			initPager();
		}
		
		public viewPagerIndicator(Context context)
		{
			super(context);
			initPager();
			// TODO Auto-generated constructor stub
		}
		private void initPager()
		{
			setFocusable(false);
			setWillNotDraw(false);
			mDotDrawableId = R.drawable.pager_dots;
		}
		
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b)
		{
			if(mTotalItems <= 0) return;
			createLayout();
		}
		private void updateLayout()
		{
			for(int i = 0; i < getChildCount(); i++)
			{
				final ImageView img = (ImageView) getChildAt(i);
				TransitionDrawable tmp = (TransitionDrawable)img.getDrawable();
				if(i == mCurrentItem)
				{
					tmp.startTransition(50);
				}
				else
				{
					tmp.resetTransition();
				}
			}
		}
		private void createLayout()
		{
			detachAllViewsFromParent();
			Resources themeResources = null;
			int resource_id = 0;
			
			int dotWidth = getResources().getDrawable(mDotDrawableId).getIntrinsicWidth();
			int separation = (int)(dotWidth * 0.75);
			int marginLeft = ((getWidth()) / 2)-(((mTotalItems*dotWidth) / 2)+(((mTotalItems-1)*separation) / 2));
			int marginTop = ((getHeight()) / 2)-(dotWidth / 2);
			for(int i = 0; i < mTotalItems; i++)
			{
				ImageView dot = new ImageView(getContext());
				TransitionDrawable td;
				if(themeResources != null && resource_id != 0)
				{
					td = (TransitionDrawable)themeResources.getDrawable(resource_id);
				}
				else
				{
					td = (TransitionDrawable)getResources().getDrawable(mDotDrawableId);
				}
				td.setCrossFadeEnabled(true);
				dot.setImageDrawable(td);
				ViewGroup.LayoutParams p;
				p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				dot.setLayoutParams(p);
				int childHeightSpec = getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.UNSPECIFIED), 0, p.height);
				int childWidthSpec = getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.EXACTLY), 0, p.width);
				dot.measure(childWidthSpec, childHeightSpec);
				int left = marginLeft+(i*(dotWidth+separation));
				dot.layout(left, marginTop, left+dotWidth,marginTop+dotWidth );
				addViewInLayout(dot, getChildCount(), p, true);
				if(i == mCurrentItem)
				{
					TransitionDrawable tmp = (TransitionDrawable)dot.getDrawable();
					tmp.startTransition(200); 
				}
			}
			postInvalidate();
		}
		@SuppressWarnings("unused")
		public int getTotalItems()
		{
			return mTotalItems;
		}
		
		public void setTotalItems(int totalItems)
		{
			Log.e(TAG, "----setTotalItems: " + totalItems);
			if(totalItems != mTotalItems)
			{
				this.mTotalItems = totalItems;
				createLayout();
			}
		}
		
		@SuppressWarnings("unused")
		public int getCurrentItem() 
		{
			return mCurrentItem;
		}
		
		public void setCurrentItem(int currentItem) 
		{
			if(currentItem != mCurrentItem)
			{
				this.mCurrentItem  =  currentItem;
				updateLayout();
			}
		}
	}
	
	
	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
	public void hide() 
	{
		// TODO Auto-generated method stub
		setVisibility(View.INVISIBLE);
	}

	public void show() 
	{
		// TODO Auto-generated method stub
		if(mVisibleTime < 0)
		{
			mIndicator.setVisibility(View.VISIBLE);
			setVisibility(View.VISIBLE);
		}
	}
}
