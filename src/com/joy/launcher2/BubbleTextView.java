/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.joy.launcher2.download.DownloadInfo;

/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends TextView implements ShortcutInfo.ShortcutListener {
    static final float SHADOW_LARGE_RADIUS = 4.0f;
    static final float SHADOW_SMALL_RADIUS = 1.75f;
    static final float SHADOW_Y_OFFSET = 2.0f;
    static final int SHADOW_LARGE_COLOUR = 0xDD000000;
    static final int SHADOW_SMALL_COLOUR = 0xCC000000;
    static final float PADDING_H = 8.0f;
    static final float PADDING_V = 3.0f;

    private int mPrevAlpha = -1;

    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private final Canvas mTempCanvas = new Canvas();
    private final Rect mTempRect = new Rect();
    private boolean mDidInvalidateForPressedState;
    private Bitmap mPressedOrFocusedBackground;
    private int mFocusedOutlineColor;
    private int mFocusedGlowColor;
    private int mPressedOutlineColor;
    private int mPressedGlowColor;

    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;

    private boolean mStayPressed;
    private CheckLongPressHelper mLongPressHelper;

    private boolean mTextVisible = true;
    private CharSequence mVisibleText;

    //add by wanghao
    DownLoadProgressBar mProgressBar;
    public BubbleTextView(Context context) {
        super(context);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        mBackground = getBackground();

        final Resources res = getContext().getResources();
        mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor =
            res.getColor(android.R.color.holo_blue_light);

        setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
        Bitmap b = info.getIcon(iconCache);

        setCompoundDrawablesWithIntrinsicBounds(null,
                new FastBitmapDrawable(b),
                null, null);
        setText(info.title);
        setTag(info);
        info.setListener(this);
    }

    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (getLeft() != left || getRight() != right || getTop() != top || getBottom() != bottom) {
            mBackgroundSizeChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mBackground || super.verifyDrawable(who);
    }

    @Override
    public void setTag(Object tag) {
        if (tag != null) {
            LauncherModel.checkItemInfo((ItemInfo) tag);
        }
        super.setTag(tag);
    }

    @Override
    protected void drawableStateChanged() {
        if (isPressed()) {
            // In this case, we have already created the pressed outline on ACTION_DOWN,
            // so we just need to do an invalidate to trigger draw
            if (!mDidInvalidateForPressedState) {
                setCellLayoutPressedOrFocusedIcon();
            }
        } else {
            // Otherwise, either clear the pressed/focused background, or create a background
            // for the focused state
            final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
            if (!mStayPressed) {
                mPressedOrFocusedBackground = null;
            }
            if (isFocused()) {
                if (getLayout() == null) {
                    // In some cases, we get focus before we have been layed out. Set the
                    // background to null so that it will get created when the view is drawn.
                    mPressedOrFocusedBackground = null;
                } else {
                    mPressedOrFocusedBackground = createGlowingOutline(
                            mTempCanvas, mFocusedGlowColor, mFocusedOutlineColor);
                }
                mStayPressed = false;
                setCellLayoutPressedOrFocusedIcon();
            }
            final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
            if (!backgroundEmptyBefore && backgroundEmptyNow) {
                setCellLayoutPressedOrFocusedIcon();
            }
        }

        Drawable d = mBackground;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    @Override
    public void onTitleChanged(CharSequence title) {
        if (mTextVisible) {
            setText(title);
        } else {
            mVisibleText = title;
        }
    }

    /**
     * Draw this BubbleTextView into the given Canvas.
     *
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private void drawWithPadding(Canvas destCanvas, int padding) {
        final Rect clipRect = mTempRect;
        getDrawingRect(clipRect);

        // adjust the clip rect so that we don't include the text label
        clipRect.bottom =
            getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V + getLayout().getLineTop(0);

        // Draw the View into the bitmap.
        // The translate of scrollX and scrollY is necessary when drawing TextViews, because
        // they set scrollX and scrollY to large values to achieve centered text
        destCanvas.save();
        destCanvas.scale(getScaleX(), getScaleY(),
                (getWidth() + padding) / 2, (getHeight() + padding) / 2);
        destCanvas.translate(-getScrollX() + padding / 2, -getScrollY() + padding / 2);
        destCanvas.clipRect(clipRect, Op.REPLACE);
        draw(destCanvas);
        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createGlowingOutline(Canvas canvas, int outlineColor, int glowColor) {
        final int padding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
        final Bitmap b = Bitmap.createBitmap(
                getWidth() + padding, getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawWithPadding(canvas, padding);
        mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur(b, canvas, glowColor, outlineColor);
        canvas.setBitmap(null);

        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // So that the pressed outline is visible immediately when isPressed() is true,
                // we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
                // to create it)
                if (mPressedOrFocusedBackground == null) {
                    mPressedOrFocusedBackground = createGlowingOutline(
                            mTempCanvas, mPressedGlowColor, mPressedOutlineColor);
                }
                // Invalidate so the pressed state is visible, or set a flag so we know that we
                // have to call invalidate as soon as the state is "pressed"
                if (isPressed()) {
                    mDidInvalidateForPressedState = true;
                    setCellLayoutPressedOrFocusedIcon();
                } else {
                    mDidInvalidateForPressedState = false;
                }

                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If we've touched down and up on an item, and it's still not "pressed", then
                // destroy the pressed outline
                if (!isPressed()) {
                    mPressedOrFocusedBackground = null;
                }

                mLongPressHelper.cancelLongPress();
                break;
        }
        return result;
    }

    void setStayPressed(boolean stayPressed) {
        mStayPressed = stayPressed;
        if (!stayPressed) {
            mPressedOrFocusedBackground = null;
        }
        setCellLayoutPressedOrFocusedIcon();
    }

    void setCellLayoutPressedOrFocusedIcon() {
        if (getParent() instanceof ShortcutAndWidgetContainer) {
            ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer) getParent();
            if (parent != null) {
                CellLayout layout = (CellLayout) parent.getParent();
                layout.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this : null);
            }
        }
    }

    void clearPressedOrFocusedBackground() {
        mPressedOrFocusedBackground = null;
        setCellLayoutPressedOrFocusedIcon();
    }

    Bitmap getPressedOrFocusedBackground() {
        return mPressedOrFocusedBackground;
    }

    int getPressedOrFocusedBackgroundPadding() {
        return HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS / 2;
    }

    @Override
    public void draw(Canvas canvas) {
        final Drawable background = mBackground;
        if (background != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0,  getRight() - getLeft(), getBottom() - getTop());
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }

        // If text is transparent, don't draw any shadow
        if (getCurrentTextColor() == getResources().getColor(android.R.color.transparent)) {
            getPaint().clearShadowLayer();
            super.draw(canvas);
            return;
        }

        // We enhance the shadow by drawing the shadow twice
        getPaint().setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
        super.draw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(),
                getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT);
        getPaint().setShadowLayer(SHADOW_SMALL_RADIUS, 0.0f, 0.0f, SHADOW_SMALL_COLOUR);
        super.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mBackground != null) mBackground.setCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackground != null) mBackground.setCallback(null);
    }

    @Override
    protected boolean onSetAlpha(int alpha) {
        if (mPrevAlpha != alpha) {
            mPrevAlpha = alpha;
            super.onSetAlpha(alpha);
        }
        return true;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

    public void setTextVisible(boolean visible) {
        if (mTextVisible == visible) return;
        mTextVisible = visible;
        if (visible) {
            setText(mVisibleText);
        } else {
            mVisibleText = getText();
            setText("");
        }
    }
    
    //add by wanghao
    @Override
    protected void onDraw(Canvas canvas) {
    	// TODO Auto-generated method stub
    	super.onDraw(canvas);
    	
    	  final int scrollX = mScrollX;
          final int scrollY = mScrollY;
          if ((scrollX | scrollY) == 0) {
          	drawProgressBar(canvas);
          } else {
              canvas.translate(scrollX, scrollY);
              drawProgressBar(canvas);
              canvas.translate(-scrollX, -scrollY);
          }
    }

    //add by wanghao
    private void initProgressBar(){
    	mProgressBar = new DownLoadProgressBar();
    	
    }
    /**
     * draw progress bar,add by wanghao
     * @param canvas
     */
	protected void drawProgressBar(Canvas canvas) {

		ShortcutInfo info = (ShortcutInfo)getTag();
		
		if (info == null) {
			return;
		}
		if (info.intent == null) {
			return;
		}
		
		int shortcutType = (Integer) info.intent.getExtra(LauncherProvider.SHORTCUT_TYPE, LauncherProvider.SHORTCUT_TYPE_NORMAL);
		boolean isVirtual = shortcutType == LauncherProvider.SHORTCUT_TYPE_VIRTUAL;
		if (isVirtual) {
			if (mProgressBar == null) {
				mProgressBar = new DownLoadProgressBar();
			}
			mProgressBar.drawProgressBar(canvas,info,isVirtual);
		}else {
			if(mProgressBar != null){
				mProgressBar = null;
			}
		}
	}
	protected void drawProgressBar(Canvas canvas,int left,int top) {

		ShortcutInfo info = (ShortcutInfo)getTag();
		
		if (info == null) {
			return;
		}
		if (info.intent == null) {
			return;
		}
		
		int shortcutType = (Integer) info.intent.getExtra(LauncherProvider.SHORTCUT_TYPE, LauncherProvider.SHORTCUT_TYPE_NORMAL);
		boolean isVirtual = shortcutType == LauncherProvider.SHORTCUT_TYPE_VIRTUAL;
		if (isVirtual) {
			if (mProgressBar == null) {
				mProgressBar = new DownLoadProgressBar();
			}
			mProgressBar.drawProgressBar(canvas,info,isVirtual,left,top);
		}else {
			if(mProgressBar != null){
				mProgressBar = null;
			}
		}
	}
	
    public Drawable getCompoundDrawable(Canvas canvas,int left,int top) {
    	
    	if (mProgressBar!= null) {
    		drawProgressBar(canvas,left,top);
		}
    	return null;
    }
    
    // add by wanghao
    class DownLoadProgressBar{
        
        private Drawable progressbar;
        private Drawable progressbar_groove;
        private Drawable progressbar_cursor;
        private Drawable progressbar_bg;
        private Rect progressbarBounds;
        
        public DownLoadProgressBar(){
        	
        	progressbar = getResources().getDrawable(R.drawable.download_progressbar);
        	progressbar_bg = getResources().getDrawable(R.drawable.download_progressbar_bg);
        	progressbar_groove = getResources().getDrawable(R.drawable.download_progressbar_groove);
        	progressbar_cursor = getResources().getDrawable(R.drawable.download_progressbar_cursor);
        	Drawable[] drawables = getCompoundDrawables();
            Drawable drawableTop = drawables[1];
            progressbarBounds = drawableTop.getBounds();
        }

		public void drawProgressBar(Canvas canvas, ShortcutInfo info,boolean isVirtual) {
			int left =(getWidth() - progressbarBounds.width()) / 2;
			int top = getPaddingTop();
			
			drawProgressBar(canvas, info, isVirtual, left, top);
		}

        private void drawProgressBar(Canvas canvas,ShortcutInfo info,boolean isVirtual,int left,int top){
    		
        	DownloadInfo downloadInfo = info.getDownLoadInfo();

    		if (downloadInfo == null&&isVirtual) {
    			canvas.save();
    			canvas.translate(left, top);
    			progressbar.setBounds(progressbarBounds);
    			progressbar.draw(canvas);
    			canvas.restore();
    			return;
    		}else if (downloadInfo != null) {
    			canvas.save();
    			canvas.translate(left, top);
    			progressbar_bg.setBounds(progressbarBounds);
    			progressbar_bg.draw(canvas);
    			
    			int maxw = progressbarBounds.width();
    			int maxh = progressbarBounds.height()/10;

    			int progressbar_left = progressbarBounds.left;
    			int progressbar_top = (progressbarBounds.bottom - progressbar_cursor.getIntrinsicHeight())/2;
    			int progressbar_right = maxw;
    			int progressbar_bottom = progressbar_top+maxh;
    			progressbar_groove.setBounds(progressbar_left, progressbar_top, progressbar_right, progressbar_bottom);
    			progressbar_groove.draw(canvas);
    			
    			int w = maxw*downloadInfo.getCompletesize()/downloadInfo.getFilesize();
    			if (w < progressbar_cursor.getIntrinsicWidth()) {
//    				w = progressbar_cursor.getIntrinsicWidth();
    				canvas.restore();
    				return;
    			}
    			progressbar_cursor.setBounds(progressbar_left, progressbar_top, w, progressbar_bottom);
    			progressbar_cursor.draw(canvas);
    			canvas.translate(left, top);
    			canvas.restore();
    		}
        }
    }
}
