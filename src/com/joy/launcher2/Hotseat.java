/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joy.launcher2;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.joy.launcher2.R;
import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.preference.PreferencesProvider.Size;

public class Hotseat extends PagedView {
    private int mCellCount;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;
    private int mAllAppsButtonRank;

    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();

    private static final int DEFAULT_CELL_COUNT = 5;
    private Launcher mLauncher;
    //add by xiong.chen for bug WXY-130
    private int mWidth = 0;
    private int mHeight = 0;
    //add end
    
    private static Canvas sCanvas = new Canvas();

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //add by xiong.chen for bug WXY-130
        DisplayMetrics dm = new DisplayMetrics();
        ((Launcher) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        //add end
        
        mFadeInAdjacentScreens = false;
        mHandleScrollIndicator = true;

        int hotseatPages = PreferencesProvider.Interface.Dock.getNumberPages();
        int defaultPage = PreferencesProvider.Interface.Dock.getDefaultPage(0);
//        Log.e("for hotseat", "----hotseat---defaultPage: " + defaultPage);
        
        //add by xiong.chen for bug wxy-131
        if (hotseatPages <= defaultPage) {
        	defaultPage = hotseatPages / 2;
        }
        //add end
        mCurrentPage = defaultPage;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Hotseat, defStyle, 0);
        mTransposeLayoutWithOrientation =
                context.getResources().getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mAllAppsButtonRank = context.getResources().getInteger(R.integer.hotseat_all_apps_index);
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
        mCellCount = a.getInt(R.styleable.Hotseat_cellCount, DEFAULT_CELL_COUNT);
        mCellCount = PreferencesProvider.Interface.Dock.getNumberIcons(mCellCount);

        if(LauncherApplication.sTheme == LauncherApplication.THEME_IOS)
        {
        	if(LauncherApplication.sIsRealIos)setBackgroundResource(R.drawable.joy_ios_hotseat_bg);
        	mCellCount = 4;
        	mAllAppsButtonRank = 3;
        }
        LauncherModel.updateHotseatLayoutCells(mCellCount);

        mVertical = hasVerticalHotseat();


        float childrenScale = PreferencesProvider.Interface.Dock.getIconScale(
                getResources().getInteger(R.integer.hotseat_item_scale_percentage)) / 100f;

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < hotseatPages; i++) {
            CellLayout cl = (CellLayout) inflater.inflate(R.layout.hotseat_page, null);
            cl.setChildrenScale(childrenScale);
            cl.setGridSize((!hasVerticalHotseat() ? mCellCount : 1), (hasVerticalHotseat() ? mCellCount : 1));
            
            //modify by xiong.chen for bug WXY-130 at 2013-06-27
            /*if (mIsLandscape) {
            	int topOrBottom = (mHeight - cl.getCellHeight() * mCellCount) / 2;
            	cl.setPadding(0, topOrBottom, 0,topOrBottom);
			} else {
	            int leftOrRightMargin = (mWidth - cl.getCellWidth() * mCellCount) / 2;
	            cl.setPadding(leftOrRightMargin, 0, leftOrRightMargin, 0);
			}*/            
            addView(cl);
            //modify end
        }

        // No data needed
        setDataIsReady();

        setOnKeyListener(new HotseatIconKeyEventListener());
    }
    
    //modify by ming.huang at 2013-4-27
    public void setup(Launcher l)
    {
    	mLauncher = l;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();      
        resetLayout();
    }

    void resetLayout()
    {
    	if(LauncherApplication.sTheme == LauncherApplication.THEME_IOS)
        {
    		//resetLayoutIos();
    		resetLayoutDefault();
        }
    	else
    	{
    		resetLayoutDefault();
    	}
    }
    
    //add by huangming for ios adaptation.
    void resetLayoutIos()
    {
    	
    }
    
    public static boolean isViewOnHotseat(View view)
    {
    	boolean isOnHotseat = false;
    	if(view == null)return false;
    	ViewParent parent = view.getParent();
	    while(parent != null && !isOnHotseat)
	    {
	    	if(parent instanceof Hotseat)
	    	{
	    		isOnHotseat = true;
	    		break;
	    	}
	    	parent = parent.getParent();
	    }
	    return isOnHotseat;
    }
    
    
    public static Bitmap getOriginalImage(Drawable d, int lastHeight)
	{
		int width = d.getIntrinsicWidth();
		int height = d.getIntrinsicHeight();
		return getOriginalImage(d, width, height, lastHeight);
	}
    
    public static Bitmap getOriginalImage(Drawable d, int width, int height, int lastHeight)
    {
		Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		final Canvas canvas = sCanvas;
		Rect oldRect = d.getBounds();
		d.setBounds(0, 0, width, height);
		canvas.setBitmap(bm);
		d.draw(canvas);
		d.setBounds(oldRect);
		canvas.setBitmap(null);
		if(lastHeight < height)
		{
			bm = Bitmap.createBitmap(bm, 0, height - lastHeight, width, lastHeight);
		}
		return bm;
    }
	
	public static Bitmap createReflectedImage(Bitmap originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, 0, width,
        		height, matrix, false);
        
        final Canvas canvas = sCanvas;
        canvas.setBitmap(reflectionImage);
		Paint shaderPaint = new Paint();
		
		LinearGradient shader = new LinearGradient(0, 0, 0, height, 0x70ffffff,
				0x00ffffff, TileMode.MIRROR);
		shaderPaint.setShader(shader);
		shaderPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		
		canvas.drawRect(0, 0, width, height, shaderPaint);
		canvas.setBitmap(null);
		
        return reflectionImage;
    }
	//end
    
    void resetLayoutDefault() {
    	Context context = getContext();
    	LayoutInflater inflater = LayoutInflater.from(context);
    	int count = getChildCount();
    	int paddingLeft = context.getResources().getDimensionPixelSize(R.dimen.all_app_button_padding_left);
    	int paddingTop = context.getResources().getDimensionPixelSize(R.dimen.all_app_button_padding_top);
    	int paddingRight = context.getResources().getDimensionPixelSize(R.dimen.all_app_button_padding_right);
    	int paddingBottom = context.getResources().getDimensionPixelSize(R.dimen.all_app_button_padding_bottom);
    	for(int i = 0; i < count; i++)
    	{
    		View child = getChildAt(i);
    		if(child instanceof CellLayout)
    		{
    			CellLayout cl = (CellLayout)child;
    			cl.removeAllViewsInLayout();
    			//modify by huangming for icon size
    			BubbleTextView allAppsButton = (BubbleTextView)
    	                inflater.inflate(R.layout.application, cl, false);
    			allAppsButton.isCanEdit(false);
    	        Drawable d = null;
    	        if(LauncherApplication.sTheme == LauncherApplication.THEME_IOS)
    	        {
    	        	d = context.getResources().getDrawable(R.drawable.joy_ios_all_apps_button_icon);
    	        	allAppsButton.setText(R.string.main_menu_text);
    	        }
    	        else
    	        {
    	        	d = context.getResources().getDrawable(R.drawable.all_apps_button_icon);
    	        }
    	        Bitmap b = Utilities.createIconBitmap(d, context);
    	        allAppsButton.setCompoundDrawablesWithIntrinsicBounds(null,
    	                new FastBitmapDrawable(b), null, null);
    	        //add by huangming for hotseat adaptation.
    	        Size iconSize= PreferencesProvider.Interface.Homescreen.getIconSize(
    	        		context, 
    	        		context.getResources().getString(R.string.config_defaultSize));
    	        if(iconSize == Size.Large)
    	        {
    	        	allAppsButton.setPadding(allAppsButton.getPaddingLeft(), 
    	        			allAppsButton.getPaddingTop() / 4, 
    	        			allAppsButton.getPaddingRight(), 
    	        			allAppsButton.getPaddingBottom());
    	        }
    			//allAppsButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    	       /* allAppsButton.setCompoundDrawablesWithIntrinsicBounds(null,
    	                context.getResources().getDrawable(R.drawable.all_apps_button_icon), null, null);*/
    	        allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
    	        allAppsButton.setOnTouchListener(new View.OnTouchListener() {
    	            @Override
    	            public boolean onTouch(View v, MotionEvent event) {
    	                if (mLauncher != null &&
    	                    (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
    	                	if (Workspace.mDeleteState == Workspace.DELETE_NONE) {
    	                		mLauncher.onTouchDownAllAppsButton(v);
							}
    	                }
    	                return false;
    	            }
    	        });
  	        
    	        allAppsButton.setOnClickListener(new View.OnClickListener() {
    	            @Override
    	            public void onClick(android.view.View v) {
    	                if (mLauncher != null) {
    	                	if (Workspace.mDeleteState == Workspace.DELETE_NONE) {
    	                		 mLauncher.onClickAllAppsButton(v);
    	                	}
    	                }
    	            }
    	        });
    	        int x = getCellXFromOrder(mAllAppsButtonRank);
    	        int y = getCellYFromOrder(mAllAppsButtonRank);
    	        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x,y,1,1);
    	        lp.canReorder = false;
    	        cl.addViewToCellLayout(allAppsButton, -1, 0, lp, true);
    		}
    	}       
    }
    //end
    
    public int getHotseatCellCount()
    {
    	return mCellCount;
    }
    
    public int getAllAppsButtonRank()
    {
    	return mAllAppsButtonRank;
    }
    
    public View getChildAt(int x, int y)
    {
    	if(getChildAt(mCurrentPage) instanceof CellLayout)
    	{
    		CellLayout currentContent = (CellLayout)getChildAt(mCurrentPage);
    		return currentContent.getChildAt(x, y);
    	}
    	return null;
    }

    public boolean hasPage(View view) {
        for (int i = 0; i < getChildCount(); i++) {
            if (view == getChildAt(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mCellCount - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mCellCount - rank - 1) : 0;
    }
    int getScreenFromOrder(int screen) {
        return hasVerticalHotseat() ? (getChildCount() - screen - 1) : screen;
    }

    /*
     *
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     * if cachedInverseMatrix is not null, this method will just use that matrix instead of
     * computing it itself; we use this to avoid redundant matrix inversions in
     * findMatchingPageForDragOver
     *
     */
    void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        if (cachedInverseMatrix == null) {
            v.getMatrix().invert(mTempInverseMatrix);
            cachedInverseMatrix = mTempInverseMatrix;
        }
        int scrollX = getScrollX();
        if (mNextPage != INVALID_PAGE) {
            scrollX = mScroller.getFinalX();
        }
        xy[0] = xy[0] + scrollX - v.getLeft();
        xy[1] = xy[1] + getScrollY() - v.getTop();
        cachedInverseMatrix.mapPoints(xy);
    }

    /**
     * Convert the 2D coordinate xy from this CellLayout's coordinate space to
     * the parent View's coordinate space. The argument xy is modified with the return result.
     */
    void mapPointFromChildToSelf(View v, float[] xy) {
        v.getMatrix().mapPoints(xy);
        int scrollX = getScrollX();
        if (mNextPage != INVALID_PAGE) {
            scrollX = mScroller.getFinalX();
        }
        xy[0] -= (scrollX - v.getLeft());
        xy[1] -= (getScrollY() - v.getTop());
    }

    /**
     * This method returns the CellLayout that is currently being dragged to. In order to drag
     * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
     * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
     *
     * Return null if no CellLayout is currently being dragged over
     */
    CellLayout findMatchingPageForDragOver(float originX, float originY, boolean exact) {
        // We loop through all the screens (ie CellLayouts) and see which ones overlap
        // with the item being dragged and then choose the one that's closest to the touch point
        final int screenCount = getChildCount();
        CellLayout bestMatchingScreen = null;
        float smallestDistSoFar = Float.MAX_VALUE;

        for (int i = 0; i < screenCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);

            final float[] touchXy = {originX, originY};
            // Transform the touch coordinates to the CellLayout's local coordinates
            // If the touch point is within the bounds of the cell layout, we can return immediately
            cl.getMatrix().invert(mTempInverseMatrix);
            mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);

            if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                    touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                return cl;
            }

            if (!exact) {
                // Get the center of the cell layout in screen coordinates
                final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                cellLayoutCenter[0] = cl.getWidth()/2;
                cellLayoutCenter[1] = cl.getHeight()/2;
                mapPointFromChildToSelf(cl, cellLayoutCenter);

                touchXy[0] = originX;
                touchXy[1] = originY;

                // Calculate the distance between the center of the CellLayout
                // and the touch point
                float dist = Workspace.squaredDistance(touchXy, cellLayoutCenter);

                if (dist < smallestDistSoFar) {
                    smallestDistSoFar = dist;
                    bestMatchingScreen = cl;
                }
            }
        }
        return bestMatchingScreen;
    }

    public void setChildrenOutlineAlpha(float alpha) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            cl.setBackgroundAlpha(alpha);
        }
    }

   

    @Override
    public void syncPages() {
    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
    }

    @Override
    protected void loadAssociatedPages(int page) {
    }
    @Override
    protected void loadAssociatedPages(int page, boolean immediateAndOnly) {
    }
}
