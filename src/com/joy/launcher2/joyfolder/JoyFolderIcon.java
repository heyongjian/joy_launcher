package com.joy.launcher2.joyfolder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.joy.launcher2.BubbleTextView;
import com.joy.launcher2.Folder;
import com.joy.launcher2.FolderIcon;
import com.joy.launcher2.FolderInfo;
import com.joy.launcher2.ItemInfo;
import com.joy.launcher2.Launcher;
import com.joy.launcher2.R;

/**
 * online folder icon
 * @author wanghao
 *
 */
public class JoyFolderIcon extends FolderIcon {
	
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

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = JoyFolder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        ((JoyFolder)folder).setTitleIcon(icon.getJoyFolderIcon());
        folder.bind(folderInfo);
        
        icon.mFolder = folder;
        
        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);
        return icon;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	 dispatchDrawSuper(canvas);
    	
    	 canvas.save();
    	 Drawable d = getJoyFolderIcon();
    	 d.draw(canvas);
    	 canvas.restore();
    	 
    	 computePreviewDrawingParams(d);
    }

    private  Drawable getJoyFolderIcon(){
    	Drawable d = null;
    	int id = R.drawable.game_folder;
    	switch(mInfo.natureId){
    	case ItemInfo.LOCAL:
    		id = R.drawable.game_folder;
    		break;
    	case ItemInfo.ONLINE:
    		id = R.drawable.game_folder;
    		break;
    	case ItemInfo.ONLINE_1:
    		id = R.drawable.application_folder;
    		break;
    	}
    	d  =  getResources().getDrawable(id);
    	d.setBounds(mPreviewBackground.getLeft(), mPreviewBackground.getTop(), 
   			 mPreviewBackground.getRight(), mPreviewBackground.getBottom());
    	return d;
    }
}
