package com.joy.launcher2;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;

public class HVFrameLayout extends FrameLayout implements OnClickListener
{
	
	private HVScrollbar mHorizontal;
	private HVScrollbar mVertical;
	private FrameLayout mIcon;
	private boolean mIsVertical = false;
	private int mTouchSlop = 0;
	private int mMaximumVelocity;
	
	private final static int STATE_OUT = -1;
	private final static int STATE_RESET = 0;
	private final static int STATE_SCROLLING = 1;
	private int mState = STATE_RESET;
	private VelocityTracker mVelocityTracker;
	
	private float mDownX;
	private float mDownY;
	private float mLastX;
	private float mLastY;
	private float mTotalX;
	private float mTotalY;
	private int mCellWidth;
	private int mCellHeight;
	
	private int mAnchorX;
	private int mAnchorY;
	
	private int mMinMarginTop;
	private int mMaxMarginTop;
	private int mMinMarginLeft;
	private int mMaxMarginLeft;
	
	private ObjectAnimator mHorizontalAnimation;
	private ObjectAnimator mVerticalAnimation;
	
	private int mAnimationDuration = 350;
	
	private static final float INFLEXION = 0.35f;
	private static float PHYSICAL_COEF;
	private float mFlingFriction = ViewConfiguration.getScrollFriction();
	private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
	
	public int mHItem;
	public int mVItem;
	public boolean  mFirstLayout = true;
	
	public HVFrameLayout(Context context) {
		super(context);
		init(context);
		// TODO Auto-generated constructor stub
	}
	
	public HVFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public HVFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		// TODO Auto-generated constructor stub
	}
	
	private void init(Context context)
	{
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity(); 
        final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        PHYSICAL_COEF = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f; // look and feel tuning
        
        // add icon style
        FrameLayout.LayoutParams flp;
        mVertical = new HVScrollbar(context);
        flp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL;
        mVertical.setLayoutParams(flp);
        mVertical.setIsVertical(true);
        mVertical.setBackgroundResource(R.color.translucent);
        addView(mVertical);
        
        // add icon text style
        mHorizontal = new HVScrollbar(context);
        flp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_VERTICAL;
        mHorizontal.setLayoutParams(flp);
        mHorizontal.setIsVertical(false);
        mHorizontal.setBackgroundResource(R.color.translucent);
        addView(mHorizontal);
        

        LayoutInflater inflater = LayoutInflater.from(context);
        mIcon = (FrameLayout)inflater.inflate(R.layout.icon_style_item, null);
        flp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        mIcon.setLayoutParams(flp);
        addView(mIcon);
        
	}
	
	public void setContent(int cellWidth, int cellHeight, ArrayList<Integer> resIds)
	{
		mCellWidth = cellWidth;
		mCellHeight = cellHeight;
		
		mVertical.setCell(cellWidth, cellHeight);
		mVertical.setIcons(resIds, this);
		mHorizontal.setCell(cellWidth, cellHeight);
		mHorizontal.setIconTexts(this);
		
		
		int iconWidth = cellWidth * 2 / 3;
		int iconHeight = cellHeight * 2 / 3;
		Drawable d = getResources().getDrawable(R.drawable.ic_launcher_home);
        Bitmap b = HVScrollbar.createIconBitmap(d, getContext());
        ImageView image =(ImageView) mIcon.findViewById(R.id.icon_image);
        
        FrameLayout.LayoutParams imageflp = (FrameLayout.LayoutParams)image.getLayoutParams();
        imageflp.width = iconWidth;
        imageflp.height = iconHeight;
        image.setImageBitmap(b);
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mIcon.getLayoutParams();
        flp.width = mCellWidth;
        flp.height = mCellHeight;
        
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }
	
	private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
        	mVelocityTracker.recycle();
        	mVelocityTracker = null;
        }
    }
	
	
	
	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		super.onLayout(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
        
        if(mFirstLayout)
        {
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
            mAnchorX = (widthSpecSize - mCellWidth) / 2;
			mAnchorY = (heightSpecSize - mCellHeight) / 2;
			FrameLayout.LayoutParams hLp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
			hLp.leftMargin = mAnchorX - mHItem * mCellWidth;
			
			FrameLayout.LayoutParams vLp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
			vLp.topMargin = mAnchorY - mVItem * mCellHeight;
        }
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mFirstLayout = false;
		acquireVelocityTrackerAndAddMovement(ev);
		int action = ev.getAction();
		final float x = ev.getX();
        final float y = ev.getY();
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			mDownX = mLastX = x;
			mDownY = mLastY = y;
			mTotalX = mTotalY = 0;
			mAnchorX = (getWidth() - mCellWidth) / 2;
			mAnchorY = (getHeight() - mCellHeight) / 2;
			mMinMarginTop = mAnchorY - mVertical.getHeight() + mCellHeight;
			mMaxMarginTop = mAnchorY;
			mMinMarginLeft = mAnchorX - mHorizontal.getWidth() +  mCellWidth;
			mMaxMarginLeft = mAnchorX;
			
			if(isTouchInH(x, y) || isTouchInV(x, y))
			{
				mState = STATE_RESET;
			}
			else
			{
				mState = STATE_OUT;
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(mState == STATE_RESET)
			{
				determineScrollingStart(x, y);
			}
		
			break;
		case MotionEvent.ACTION_UP:
		default:
			mState = STATE_RESET;
			releaseVelocityTracker();
			break;
		}
		
		return mState == STATE_SCROLLING;
	}
	
	private boolean isTouchInH(float x, float y)
	{
		Rect hRect = new Rect();
		mHorizontal.getHitRect(hRect);
		if(hRect.contains((int)x, (int)y))
		{
			return true;
		}
		return false;
		
	}
	
	private boolean isTouchInV(float x, float y)
	{
        Rect vRect = new Rect();		
		mVertical.getHitRect(vRect);
		if(vRect.contains((int)x, (int)y))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mFirstLayout = false;
		acquireVelocityTrackerAndAddMovement(event);
		int action = event.getAction();
		final float x = event.getX();
        final float y = event.getY();
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			mDownX = mLastX = x;
			mDownY = mLastY = y;
			mTotalX = mTotalY = 0;
			
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(mState == STATE_SCROLLING)
			{
				float deltaX = x - mLastX;
				float deltaY = y - mLastY;
				if(mIsVertical && Math.abs(deltaY) > 1.0f)
				{
					
					if(mVertical.getLayoutParams() instanceof FrameLayout.LayoutParams)
					{
						FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
						int topMargin = (int)(flp.topMargin + deltaY);
						if(topMargin < mMinMarginTop)
						{
							topMargin = mMinMarginTop;
						}
						else if(topMargin > mMaxMarginTop)
						{
							topMargin = mMaxMarginTop;
						}
						flp.topMargin = topMargin;
						requestLayout();
					}
					mTotalX += deltaX;
					mTotalY += deltaY;
					mLastX = x;
					mLastY = y;
				}
				else if(!mIsVertical && Math.abs(deltaX) > 1.0f)
				{
					if(mHorizontal.getLayoutParams() instanceof FrameLayout.LayoutParams)
					{
						FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
						int leftMargin = (int)(flp.leftMargin + deltaX);
						if(leftMargin < mMinMarginLeft)
						{
							leftMargin = mMinMarginLeft;
						}
						else if(leftMargin > mMaxMarginLeft)
						{
							leftMargin = mMaxMarginLeft;
						}
						flp.leftMargin = leftMargin;
						requestLayout();
					}
					mTotalX += deltaX;
					mTotalY += deltaY;
					mLastX = x;
					mLastY = y;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mState == STATE_SCROLLING)
			{
				final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int)velocityTracker.getXVelocity();
                int velocityY = (int)velocityTracker.getYVelocity();
                int duration = 500;
                
                if(mHorizontalAnimation != null)
                {
                	mHorizontalAnimation.cancel();
                	mHorizontalAnimation = null;
                }
                
                if(mVerticalAnimation != null)
                {
                	mVerticalAnimation.cancel();
                	mVerticalAnimation = null;
                }
                
                if(mIsVertical)
                {
                	if(mVertical.getLayoutParams() instanceof FrameLayout.LayoutParams)
					{
                		final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
                		int dstTopMargin = Math.min(Math.max(mMinMarginTop, getFinalDistance((int)getSplineFlingDistance(velocityY)) + flp.topMargin), mMaxMarginTop);
						
                		mVerticalAnimation = ObjectAnimator.ofInt(mVertical, "topMargin", flp.topMargin, dstTopMargin);
                		mVerticalAnimation.setDuration(mAnimationDuration);
                		mVerticalAnimation.setInterpolator(new DecelerateInterpolator());
                		mVerticalAnimation.addUpdateListener(new AnimatorUpdateListener() {
							
							@Override
							public void onAnimationUpdate(ValueAnimator animation) {
								// TODO Auto-generated method stub
								flp.topMargin = ((Integer)animation.getAnimatedValue()).intValue();
								requestLayout();
							}
						});
                		mVerticalAnimation.start();
					}
                	
                }
                else
                {
                	if(mHorizontal.getLayoutParams() instanceof FrameLayout.LayoutParams)
					{
                		final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
                		int dstLeftMargin = Math.min(Math.max(mMinMarginLeft, getFinalDistance((int)getSplineFlingDistance(velocityX)) + flp.leftMargin), mMaxMarginLeft);
						
                		mHorizontalAnimation = ObjectAnimator.ofInt(mHorizontal, "leftMargin", flp.leftMargin, dstLeftMargin);
                		mHorizontalAnimation.setDuration(mAnimationDuration);
                		mHorizontalAnimation.setInterpolator(new DecelerateInterpolator());
                		mHorizontalAnimation.addUpdateListener(new AnimatorUpdateListener() {
							
							@Override
							public void onAnimationUpdate(ValueAnimator animation) {
								// TODO Auto-generated method stub
								flp.leftMargin = ((Integer)animation.getAnimatedValue()).intValue();
								requestLayout();
							}
						});
                		mHorizontalAnimation.start();
					}
                }
                
			}
			mState = STATE_RESET;
			releaseVelocityTracker();
			break;
		default:
			
			break;
		}
		
		return true;
	}
	
	public void setHVItems(int h, int v)
	{
		mHItem = h;
		mVItem = v;
	}
	
	public boolean getHVItem(int[] items)
	{
		if(items == null)return false;
		if(items.length < 2)return false;
		boolean isAnimationRunning = isAnimationRunning();
		FrameLayout.LayoutParams hLp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
		FrameLayout.LayoutParams vLp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
		items[0] = (mAnchorX - hLp.leftMargin) / mCellWidth;
		items[1] = (mAnchorY - vLp.topMargin) / mCellHeight;
		return !isAnimationRunning;
	}
	
	private double getSplineDeceleration(int velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * PHYSICAL_COEF));
    }

    private double getSplineFlingDistance(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        int distance = (int)(mFlingFriction * PHYSICAL_COEF * Math.exp(DECELERATION_RATE / decelMinusOne * l));
        if(velocity < 0)return -distance;
        return distance;
    }
    
    private int getSplineFlingDuration(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }
	
    
    private int getFinalDistance(int distance)
    {
    	if(mIsVertical)
    	{
    		
    		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
    		int dstTopMargin = flp.topMargin + distance;
    		int finalItem = -1;
    		int finalDistance = Integer.MAX_VALUE;
    		for(int i = 0; i < mVertical.getChildCount(); i++)
    		{
    			int itemDistance = Math.abs(mAnchorY - dstTopMargin - i * mCellHeight);
    			if(itemDistance <= finalDistance)
    			{
    				finalItem = i;
    				finalDistance = itemDistance;
    			}
    		}
    		if(finalItem >= 0)
    		{
    			return mAnchorY - dstTopMargin - finalItem * mCellHeight + distance;
    		}
    	}
    	else
    	{
    		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
    		int dstLeftMargin = flp.leftMargin + distance;
    		int finalItem = -1;
    		int finalDistance = Integer.MAX_VALUE;
    		for(int i = 0; i < mHorizontal.getChildCount(); i++)
    		{
    			int itemDistance = Math.abs(mAnchorX - dstLeftMargin - i * mCellWidth);
    			if(itemDistance <= finalDistance)
    			{
    				finalItem = i;
    				finalDistance = itemDistance;
    			}
    		}
    		if(finalItem >= 0)
    		{
    			return mAnchorX - dstLeftMargin - finalItem * mCellWidth + distance;
    		}
    	}
    	
    	
    	return distance;
    }
	
	private void determineScrollingStart(float x, float y)
	{
		final int xDiff = (int) Math.abs(x - mLastX);
		final int yDiff = (int) Math.abs(y - mLastY);
		boolean xMoved = xDiff > mTouchSlop;
		boolean yMoved = yDiff > mTouchSlop;
		boolean isAnimationRunning = isAnimationRunning();
		/*if(mVerticalAnimation != null)
		{
			isAnimationRunning = mVerticalAnimation.isRunning();
		}
		if(mHorizontalAnimation != null)
		{
			isAnimationRunning = mHorizontalAnimation.isRunning();
		}*/
		if((xMoved || yMoved) && !isAnimationRunning)
		{
			mState = STATE_SCROLLING;
			mTotalX += x - mLastX;
			mTotalY += y - mLastY;
			mLastX = x;
			mLastY = y;			
			boolean isTouchInH = isTouchInH(mDownX, mDownY);
			boolean isTouchInV = isTouchInV(mDownX, mDownY);
			if(isTouchInH && isTouchInV)
			{
				mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				mIsVertical = Math.abs(mVelocityTracker.getXVelocity()) < Math.abs(mVelocityTracker.getYVelocity());
			}
			else if(isTouchInH)
			{
				mIsVertical = false;
			}
			else
			{
				mIsVertical = true;
			}
			
		}
	}
	
	boolean isAnimationRunning()
	{
		boolean isAnimationRunning = false;
		if(mVerticalAnimation != null)
		{
			isAnimationRunning = mVerticalAnimation.isRunning();
		}
		if(mHorizontalAnimation != null)
		{
			isAnimationRunning = mHorizontalAnimation.isRunning();
		}
		return isAnimationRunning;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		View parent = (View)v.getParent();
		boolean isAnimationRunning = isAnimationRunning();
		if(parent == mHorizontal && !isAnimationRunning)
		{
			 if(mHorizontalAnimation != null)
             {
             	mHorizontalAnimation.cancel();
             	mHorizontalAnimation = null;
             }
			int item = mHorizontal.indexOfChild(v);
			final FrameLayout.LayoutParams hLp = (FrameLayout.LayoutParams)mHorizontal.getLayoutParams();
			
			mHorizontalAnimation = ObjectAnimator.ofInt(mHorizontal, "leftMargin", hLp.leftMargin, mAnchorX - item * mCellWidth);
    		mHorizontalAnimation.setDuration(mAnimationDuration);
    		mHorizontalAnimation.setInterpolator(new DecelerateInterpolator());
    		mHorizontalAnimation.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// TODO Auto-generated method stub
					hLp.leftMargin = ((Integer)animation.getAnimatedValue()).intValue();
					requestLayout();
				}
			});
    		mHorizontalAnimation.start();
			requestLayout();
		}
		else if(parent == mVertical && !isAnimationRunning)
		{
			if(mVerticalAnimation != null)
            {
            	mVerticalAnimation.cancel();
            	mVerticalAnimation = null;
            }
			int item = mVertical.indexOfChild(v);
			final FrameLayout.LayoutParams vLp = (FrameLayout.LayoutParams)mVertical.getLayoutParams();
			mVerticalAnimation = ObjectAnimator.ofInt(mVertical, "topMargin", vLp.topMargin, mAnchorY - item * mCellHeight);
    		mVerticalAnimation.setDuration(mAnimationDuration);
    		mVerticalAnimation.setInterpolator(new DecelerateInterpolator());
    		mVerticalAnimation.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// TODO Auto-generated method stub
					vLp.topMargin = ((Integer)animation.getAnimatedValue()).intValue();
					requestLayout();
				}
			});
    		mVerticalAnimation.start();
			requestLayout();
		}
		
		
	}

}
