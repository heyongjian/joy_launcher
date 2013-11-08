package com.joy.launcher2.wallpaper;

import com.joy.launcher2.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.TextView;

/**
 * 推荐壁纸类别，显示四种类别
 * @author huangming
 *
 */
public class RecommendsView extends ViewGroup{

	private int countX = 2;
	private int countY = 2;
	private int count;
	

	private int widthGap;
	private int heightGap;
	
	private int width;
	private int height;
	
	public RecommendsView(Context context) {
		super(context, null);
		init(context);
		
	}
	
	public RecommendsView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init(context);

	}
	
	
	public RecommendsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
		// TODO Auto-generated constructor stub
	}
	
	private void init(Context context)
	{
		Resources res = context.getResources();
		widthGap = res.getDimensionPixelSize(R.dimen.recommend_width_gap);
		heightGap = res.getDimensionPixelSize(R.dimen.recommend_height_gap);
		height = res.getDimensionPixelSize(R.dimen.recommend_height);
	}
	
	public void setDimens(int width, int height)
	{
		this.width = width;
		if(height > 0)
		{
			this.height = height;
		}
		
	}
	
	public int getMaxCount()
	{
		return countX * countY;
	}
	
	public void setOnClickListener(OnClickListener listener)
	{
		int count  = getChildCount();
		for(int i = 0; i < count; i++)
		{
			View child = getChildAt(i);
			child.setOnClickListener(listener);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getLayoutParams() instanceof LayoutParams)
            {
                LayoutParams lp = (LayoutParams)child.getLayoutParams();
                if(child.getVisibility() == View.VISIBLE)child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
            }
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getLayoutParams() instanceof LayoutParams)
            {
            	LayoutParams lp = (LayoutParams)child.getLayoutParams();
                int cellWidth = (width - widthGap * ( countX + 1))/ 2;
                int cellHeight = (height - heightGap * ( countY + 1))/ 2;
                if(cellWidth >= 0 && cellHeight >= 0)
                {
                	lp.width = cellWidth;
                	lp.height = cellHeight;
                }
                else
                {
                	lp.width = 0;
                	lp.height = 0;
                }
                lp.x = widthGap + (i % countX) * (cellWidth + widthGap);
                lp.y = heightGap + (i / countY) * (cellHeight + heightGap);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childheightMeasureSpec);
            }

        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }
	
	public int getActualHeight()
	{
		int actualHeight = 0;
		
		int count = getVisibleChildCount();
		if(count > 0)
		{
			int cellY = (count + 1) /2;
			actualHeight = (height - heightGap) / countY * cellY + heightGap;
			
		}
		return actualHeight;
	}
	
	public int getVisibleChildCount()
	{
		int count = 0;
		for(int i = 0; i < getChildCount(); i++)
		{
			View child = getChildAt(i);
			if(child != null && child.getVisibility() == View.VISIBLE)count++;
		}
		return count;
	}

	static class LayoutParams extends ViewGroup.LayoutParams
	{
		

		int x; 
		int y;
		

		public LayoutParams(int arg0, int arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}
		
	}

}
