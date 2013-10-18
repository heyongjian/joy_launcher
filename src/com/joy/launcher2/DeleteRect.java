package com.joy.launcher2;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * delete icon when long click
 * @author wanghao
 *
 */
public class DeleteRect{
	Context mContext;
	Rect rect = new Rect();
//	DeleteListener mDeleteListener = null;
	boolean isDelete;
	private boolean mIsTouchAlwaysInRect = false;
	private CheckLongPressHelper mLongPressHelper;
	View view;
	Launcher mLauncher;
	private int left;
	private int top;
	private Drawable deleteRectNormal;
	private Drawable deleteRectPress;
	private long currentTime;
	private boolean isMove;
	private final long PRESS_DELAY_TIME = 70L;
	public DeleteRect(View view){
		this.view = view;
		mContext = view.getContext();
		mLongPressHelper = new CheckLongPressHelper(view);
		if (mContext instanceof Launcher) {
			mLauncher = ((Launcher) mContext);
		}
		deleteRectNormal = mContext.getResources().getDrawable(R.drawable.delete_rect_normal);
		deleteRectPress = mContext.getResources().getDrawable(R.drawable.delete_rect_press);
	}
	//draw delete image
    public void drawDelete(Canvas canvas,int scrollX,int scrollY){
  
    	if(!isDelete){
    		return;
    	}
    	
    	Drawable d = null;
    	if (mIsTouchAlwaysInRect) {
			d = deleteRectPress;
		}else {
			d = deleteRectNormal;
		}
    	
		int drawableWidth = d.getIntrinsicWidth();
		int drawableHeight = d.getIntrinsicHeight();
		if (view instanceof BubbleTextView) {
			Drawable[] drawables = ((BubbleTextView) view).getCompoundDrawables();
			Drawable drawableTop = drawables[1];
			if (drawableTop != null) {
				int w = drawableTop.getBounds().width();
				int h = drawableTop.getBounds().height();
				left = Math.abs(view.getMeasuredWidth()-w)/2-drawableWidth/2;
				left = left<0?0:left;
				top = 0;
			}
		}else if (view instanceof FolderIcon) {
//			FolderIcon icon = (FolderIcon)view;
//			left = icon.getPreviewBackgroundLeft()-drawableWidth/2;
//			left = left<0?0:left;
//			top = icon.getPreviewBackgroundTop()-drawableHeight/2;
//			top = top<0?0:top;
			return;
		}else {
			left = 0;
			top = 0;
		}
		rect.set(left, top, drawableWidth+left, drawableHeight+top);
		d.setBounds(rect);
		canvas.save();
    	canvas.translate(scrollX, scrollY);
		d.draw(canvas);
		canvas.translate(-scrollX, -scrollY);
		canvas.restore();
    }
    
    //check touch rect
    public boolean onTouchEventDelete(boolean result,MotionEvent event){
    	if(!isDelete){
    		return result;
    	}
    	int action = event.getAction();
		int x;
		int y;
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			x = (int)event.getX();
			y = (int)event.getY();
			mIsTouchAlwaysInRect = rect.contains(x, y);
			if(!mIsTouchAlwaysInRect){
				currentTime = System.currentTimeMillis();
				isMove = false;
				view.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if (!isMove) {
							mLongPressHelper.postCheckForLongPress(0);
						}
					}
				}, PRESS_DELAY_TIME);
			}else {
				view.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(mIsTouchAlwaysInRect)
			{
				x = (int)event.getX();
    			y = (int)event.getY();
    			mIsTouchAlwaysInRect = rect.contains(x, y);
			}else{
				if (!isMove&&(System.currentTimeMillis() - currentTime)<PRESS_DELAY_TIME) {
					isMove = true;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mIsTouchAlwaysInRect)
			{
				x = (int)event.getX();
    			y = (int)event.getY();
    			mIsTouchAlwaysInRect = rect.contains(x, y);
    			if(mIsTouchAlwaysInRect)
    			{
    				Log.i("DeleteRect", "DeleteRect : "+mIsTouchAlwaysInRect);
    				deleteViewInScreen();
    				return true;
    			}
			}
			view.invalidate();
			mLongPressHelper.cancelLongPress();
			break;
			default:
				mLongPressHelper.cancelLongPress();
				view.invalidate();
				break;
		}
		return result;
    }

    private void deleteViewInScreen(){
    	if (mLauncher == null) {
			return;
		}
		if (view instanceof LauncherAppWidgetHostView) {

			LauncherAppWidgetInfo item = (LauncherAppWidgetInfo) view.getTag();
			mLauncher.getWorkspace().removeInScreen(view, item.container,item.screen);
			mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
			LauncherModel.deleteItemFromDatabase(mLauncher, item);

			final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
			final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
			if (appWidgetHost != null) {
				new Thread("deleteAppWidgetId") {
					public void run() {
						appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
					}
				}.start();
			}

		} else if (view instanceof FolderIcon) {
//			FolderInfo item = (FolderInfo) view.getTag();
//			mLauncher.getWorkspace().removeInScreen(view, item.container,item.screen);
//			mLauncher.removeFolder(item);
//			LauncherModel.deleteFolderContentsFromDatabase(mLauncher, item);

		} else if (view instanceof BubbleTextView) {
			ItemInfo item = (ItemInfo) view.getTag();
			mLauncher.getWorkspace().removeInScreen(view, item.container,item.screen);
			LauncherModel.deleteItemFromDatabase(mLauncher, item);
		}
    }
}