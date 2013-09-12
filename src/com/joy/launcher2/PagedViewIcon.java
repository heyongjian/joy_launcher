/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.preference.PreferencesProvider.Size;
import com.joy.launcher2.preference.PreferencesProvider.TextStyle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class PagedViewIcon extends TextView {
    /** A simple callback interface to allow a PagedViewIcon to notify when it has been pressed */
    public static interface PressedCallback {
        void iconPressed(PagedViewIcon icon);
    }

    @SuppressWarnings("unused")
    private static final String TAG = "joy.launcher2.PagedViewIcon";
    private static final float PRESS_ALPHA = 0.4f;

    private PagedViewIcon.PressedCallback mPressedCallback;
    private boolean mLockDrawableState = false;

    private Bitmap mIcon;
    //add for huangming for app show or hide
    private Rect mDeleteRect;
    private Drawable mSelectedDrawable;
    private Drawable mUnSelectedDrawable;
    private boolean mSelected = false;
    //end

    public PagedViewIcon(Context context) {
        this(context, null);
    }

    public PagedViewIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedViewIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //add for huangming for app show or hide
        final Resources r = context.getResources();
        mDeleteRect = new Rect();
        mSelectedDrawable = r.getDrawable(R.drawable.app_selected_on);
        mUnSelectedDrawable = r.getDrawable(R.drawable.app_selected_off);
        //end
        
      //add by huangming for Desktop appearance
        Size textSize = PreferencesProvider.Interface.Homescreen.getIconTextSize(
        		context, 
        		context.getResources().getString(R.string.config_defaultSize));
        int defaultSize = context.getResources().getDimensionPixelSize(R.dimen.icon_text_size_default);
        if(textSize == Size.Large)
        {
        	defaultSize = (int)(defaultSize * Utilities.LARGE_RATIO);
        }
        else if(textSize == Size.Small)
        {
        	defaultSize = (int)(defaultSize * Utilities.SMALL_RATIO);
        }
        setTextSize(defaultSize);
        
        TextStyle textStyle = PreferencesProvider.Interface.Homescreen.getIconTextStyle(
        		getContext(), 
        		TextStyle.Marquee.toString());
        if(textStyle == TextStyle.Marquee)
        {
        	setSingleLine();
        	setEllipsize(TruncateAt.MARQUEE);
        }
        else if(textStyle == TextStyle.Ellipsis)
        {
        	setSingleLine();
        	setEllipsize(TruncateAt.END);
        }
        else if(textStyle == TextStyle.TwoLines)
        {
        	setSingleLine(false);
        	setMaxLines(2);
        }
        //end
    }

    public void applyFromApplicationInfo(ApplicationInfo info, boolean scaleUp,
            PagedViewIcon.PressedCallback cb) {
        mIcon = info.iconBitmap;
        mPressedCallback = cb;
        setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIcon), null, null);
        setText(info.title);
        setTag(info);
    }

    public void lockDrawableState() {
        mLockDrawableState = true;
    }

    public void resetDrawableState() {
        mLockDrawableState = false;
        post(new Runnable() {
            @Override
            public void run() {
                refreshDrawableState();
            }
        });
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // We keep in the pressed state until resetDrawableState() is called to reset the press
        // feedback
        if (isPressed()) {
            setAlpha(PRESS_ALPHA);
            if (mPressedCallback != null) {
                mPressedCallback.iconPressed(this);
            }
        } else if (!mLockDrawableState) {
            setAlpha(1f);
        }
    }
    
    //add by huangming for app show or hide.
    
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	if(AppsCustomizePagedView.mIsShowOrHideEidt)
        {
        	Drawable d = null;
        	if(mSelected)
        	{
        		d = mSelectedDrawable;
        	}
        	else
        	{
        		d = mUnSelectedDrawable;
        	}
        	if(d == null)return;
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int drawableWidth = d.getIntrinsicWidth();
            int drawableHeight = d.getIntrinsicHeight();
            mDeleteRect.set(width - drawableWidth, 0, width, drawableHeight);
            canvas.save();
            canvas.translate(mScrollX, mScrollY);
            d.setBounds(mDeleteRect);
            d.draw(canvas);
            canvas.translate(-mScrollX,- mScrollY);
            canvas.restore();
        } 
    }
    
    public void setSelected(boolean selected)
    {
    	if(mSelected != selected)
    	{
    		mSelected = selected;
    		invalidate();
    	}
    }
    
    public boolean getSelected()
    {
    	return mSelected;
    }
    //end
    
}
