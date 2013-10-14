package com.joy.launcher2.joyfolder;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.location.i;
import com.joy.launcher2.network.handler.BuiltInHandler;
import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.preference.PreferencesProvider.Size;
import com.joy.launcher2.util.Util;
import com.joy.launcher2.BubbleTextView;
import com.joy.launcher2.Folder;
import com.joy.launcher2.FolderIcon;
import com.joy.launcher2.FolderInfo;
import com.joy.launcher2.Hotseat;
import com.joy.launcher2.ItemInfo;
import com.joy.launcher2.Launcher;
import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.R;
import com.joy.launcher2.Utilities;

/**
 * online folder icon
 * @author wanghao
 *
 */
public class JoyFolderIcon extends FolderIcon {
	Drawable foldericon;
    public JoyFolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JoyFolderIcon(Context context) {
        super(context);
    }

    public static JoyFolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,FolderInfo folderInfo) {

        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        JoyFolderIcon icon = (JoyFolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName.setText(folderInfo.title);
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);

        //add by huangming for icon size
        Resources res = launcher.getResources();
        if(mPreviewSize <= 0)
        {
            mPreviewSize = (int)launcher.getResources().getDimension(R.dimen.folder_preview_size);
            Size iconSize= PreferencesProvider.Interface.Homescreen.getIconSize(
            		launcher, 
            		res.getString(R.string.config_defaultSize));
            if(iconSize == Size.Small)
            {
            	mPreviewSize = (int)(mPreviewSize * Utilities.SMALL_RATIO);
            }
            else if(iconSize == Size.Large)
            {
            	mPreviewSize = (int)(mPreviewSize * Utilities.LARGE_RATIO);
            }
        }
        int previewSize = mPreviewSize;
        
        if(icon.mPreviewBackground.getLayoutParams() instanceof LinearLayout.LayoutParams)
        {
        	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)icon.mPreviewBackground.getLayoutParams();
        	lp.width = lp.height = previewSize;
        	lp.topMargin = icon.mFolderMarginTop = icon.mFolderName.getPaddingTop();
            lp.bottomMargin = (int)res.getDimension(R.dimen.app_icon_drawable_padding) - icon.mFolderName.getPaddingTop();
        }
        //end
        
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = JoyFolder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        Drawable drawable = icon.initJoyFolderIcon((String)folderInfo.iconPath);
        ((JoyFolder)folder).setTitleIcon(drawable);
        if (folderInfo != null) {
        	folder.bind(folderInfo);
		}
        
        icon.mFolder = folder;
        
        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);
        return icon;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	 dispatchDrawSuper(canvas);
    	
    	 canvas.save();
    	 if (foldericon == null&&mInfo!= null) {
    		 foldericon = initJoyFolderIcon((String)mInfo.iconPath);
		}
    	 Rect oldRect = foldericon.getBounds();
    	 foldericon.setBounds(mPreviewBackground.getLeft(), mPreviewBackground.getTop(), mPreviewBackground.getRight(), mPreviewBackground.getBottom());
    	 foldericon.draw(canvas);
    	 foldericon.setBounds(oldRect);
    	 canvas.restore();
    	 
    	 //add by huangming for ios adaptation.
    	 if(LauncherApplication.sTheme == LauncherApplication.THEME_IOS)
         {
 			boolean isOnHotseat = Hotseat.isViewOnHotseat(this);
 		    
 		    if(isOnHotseat)
 		    {
 		    	drawIos(canvas, foldericon);
 		    }
         }
    	 //end
    	 computePreviewDrawingParams(foldericon);
    }
    
    
    //add by huangming for ios adaptation.
    @Override
	protected Bitmap getOriginalImage(Drawable originalDrawable, int width,
			int height, int lastHeight) {
		// TODO Auto-generated method stub
    	return Hotseat.getOriginalImage(
    			originalDrawable, 
    			width, 
    			height, 
    			lastHeight);
	}
    //end

	private Drawable initJoyFolderIcon(String iconPath){
    		Drawable d = null;
    		Bitmap bitmap = Util.getBitmapFromAssets(iconPath);
    		d = new BitmapDrawable(bitmap);
    	return d;
    }
}
