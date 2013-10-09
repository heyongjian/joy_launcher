package com.joy.launcher2.wallpaper;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * 预览Viewpager
 * @author huangming
 *
 */
public class PreviewPager extends ViewPager{
	
	
	private int lastDownX;
	private int touchSlop;
	private final static int RESET_STATE = 0;
	private final static int SCROLLING_STATE = 1;
	private int touchState = RESET_STATE;
	private PreviewActivity pa;

	public PreviewPager(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public PreviewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}
	
	
	private void init()
	{
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		touchSlop = configuration.getScaledTouchSlop();
	}
	
	public void setPreviewActivity(PreviewActivity pa)
	{
		this.pa = pa;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			touchState = RESET_STATE;
			lastDownX = (int)event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(event.getX() - lastDownX);
			boolean xMoved = xDiff > touchSlop;
			if(xMoved && touchState != SCROLLING_STATE)
			{
				touchState = SCROLLING_STATE;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(touchState != SCROLLING_STATE && pa != null)
			{
				pa.dismissPreviewIcon();
			}
			break;
			default:
				break;
		}
		return super.onTouchEvent(event);
		
	}

	

}
