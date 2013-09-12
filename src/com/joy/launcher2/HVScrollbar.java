package com.joy.launcher2;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

public class HVScrollbar extends ViewGroup
{

	private boolean isVertical = false;
	private int cellWidth;
	private int cellHeight;

	public HVScrollbar(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public HVScrollbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public HVScrollbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}
	
	private void init()
	{
		//scroller = new Scroller(getContext(), new ScrollInterpolator());
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        //touchSlop = configuration.getScaledTouchSlop();
        //maximumVelocity = configuration.getScaledMaximumFlingVelocity(); 
	}
	
	public void setIsVertical(boolean isVertical)
	{
		this.isVertical = isVertical;
	}
	
	public void setCell(int cellWidth, int cellHeight)
	{
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)getLayoutParams();
		int count = getChildCount();
		for(int i = 0; i < count; i++)
		{
			View child = getChildAt(i);
			ViewGroup.LayoutParams vlp = child.getLayoutParams();
			if(vlp instanceof LayoutParams)
			{
				LayoutParams lp = (LayoutParams)vlp;
				child.layout(lp.x, lp.y, lp.x + lp.width, lp.y+lp.height);
			}
		}
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int count = getChildCount();
		int widthSpecSize = 0;
		int heightSpecSize = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            ViewGroup.LayoutParams vlp = child.getLayoutParams();
            if(vlp instanceof LayoutParams)
            {
            	LayoutParams lp = (LayoutParams)vlp;
            	if(isVertical)
            	{
            		lp.x = 0;
            		lp.y = lp.height * i;
            		widthSpecSize = lp.width;
            		heightSpecSize +=lp.height;
            	}
            	else
            	{
            		lp.y = 0;
            		lp.x = lp.width * i;
            		heightSpecSize = lp.height;
            		widthSpecSize +=lp.width;
            	}
            	int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
            			lp.width, 
            			MeasureSpec.EXACTLY);
                int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(
                		lp.height,
                        MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childheightMeasureSpec);
            }
        }
       
        setMeasuredDimension(widthSpecSize, heightSpecSize);
	}


	

	static class LayoutParams extends ViewGroup.LayoutParams
	{
		
		int x;
		int y;

		public LayoutParams(int width, int height) {
			super(width, height);
		}
		
	}
	
	private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1;
        }
    }
	
	
	public void setIcons(ArrayList<Integer> resIds, View.OnClickListener l)
	{
		int iconWidth = cellWidth * 2 / 3;
		int iconHeight = cellHeight * 2 / 3;
		LayoutInflater inflater = LayoutInflater.from(getContext());
		for(int i = 0; i < resIds.size(); i++)
		{
			LayoutParams lp = new LayoutParams(cellWidth, cellHeight);
			FrameLayout fl = (FrameLayout)inflater.inflate(R.layout.icon_style_item, null);
			fl.setOnClickListener(l);
			fl.setLayoutParams(lp);
			ImageView image = (ImageView)fl.findViewById(R.id.icon_image);
			FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)image.getLayoutParams();
			flp.width = iconWidth;
			flp.height = iconHeight;
			Drawable d = null;
			if(resIds.get(i) > -1)
			{
				d = getResources().getDrawable(resIds.get(i));
				Bitmap b = createIconBitmap(d, getContext());
				image.setImageBitmap(b);
			}
			
			addView(fl, i);
			
			
			
		}
	}
	
	public void setIconTexts(View.OnClickListener l)
	{
		int textWidth = cellWidth * 2/ 3;
		int textHeight = cellHeight / 3;
		LayoutInflater inflater = LayoutInflater.from(getContext());
		LayoutParams lp;
		FrameLayout.LayoutParams flp;
		FrameLayout fl;
		TextView text;
		//ColorDrawable cd = new ColorDrawable(Color.TRANSPARENT);
		//Bitmap b = createIconBitmap(cd, getContext());
		
		lp = new LayoutParams(cellWidth, cellHeight);
		fl = (FrameLayout)inflater.inflate(R.layout.icon_style_item, null);
		fl.setOnClickListener(l);
		fl.setLayoutParams(lp);
		text = (TextView)fl.findViewById(R.id.icon_text);
		text.setVisibility(View.VISIBLE);
		flp = (FrameLayout.LayoutParams)text.getLayoutParams();
		flp.width = textWidth;
		flp.height = textHeight;
		text.setSingleLine();
		addView(fl);
		
		lp = new LayoutParams(cellWidth, cellHeight);
		fl = (FrameLayout)inflater.inflate(R.layout.icon_style_item, null);
		fl.setOnClickListener(l);
		fl.setLayoutParams(lp);
		text = (TextView)fl.findViewById(R.id.icon_text);
		text.setVisibility(View.VISIBLE);
		flp = (FrameLayout.LayoutParams)text.getLayoutParams();
		flp.width = textWidth;
		flp.height = textHeight;
		text.setSingleLine();
		text.setEllipsize(TextUtils.TruncateAt.END);
		addView(fl);
		
		lp = new LayoutParams(cellWidth, cellHeight);
		fl = (FrameLayout)inflater.inflate(R.layout.icon_style_item, null);
		fl.setOnClickListener(l);
		fl.setLayoutParams(lp);
		text = (TextView)fl.findViewById(R.id.icon_text);
		text.setVisibility(View.VISIBLE);
		flp = (FrameLayout.LayoutParams)text.getLayoutParams();
		flp.width = textWidth;
		flp.height = textHeight;
		text.setSingleLine(false);
		text.setMaxLines(2);
		addView(fl);
		
	}
	
	private static final Canvas sCanvas = new Canvas();
	
	private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static final Rect sOldBounds = new Rect();
	
	static Bitmap createIconBitmap(Drawable icon, Context context) {
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();

            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconWidth;
            int textureHeight = sIconHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
    }
	
	private static void initStatics(Context context) {
		sIconHeight = sIconWidth = (int) context.getResources().getDimension(R.dimen.app_icon_size);
	}

}
