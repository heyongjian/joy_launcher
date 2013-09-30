package com.joy.launcher2.joyfolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joy.launcher2.CellLayout;
import com.joy.launcher2.DragLayer;
import com.joy.launcher2.Folder;
import com.joy.launcher2.FolderEditText;
import com.joy.launcher2.ItemInfo;
import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.LauncherModel;
import com.joy.launcher2.R;
import com.joy.launcher2.ShortcutAndWidgetContainer;
import com.joy.launcher2.ShortcutInfo;
import com.joy.launcher2.Utilities;
import com.joy.launcher2.download.DownLoadDBHelper;
import com.joy.launcher2.download.DownloadInfo;
import com.joy.launcher2.download.DownloadManager;
import com.joy.launcher2.network.handler.AppListHandler;
import com.joy.launcher2.network.impl.Service.CallBack;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;

/**
 * online folder
 * @author wanghao
 *
 */
public class JoyFolder extends Folder implements OnItemClickListener{
	public static final String ACTION_UPDATE_SHORTCUT = "com.joy.launcher2.action.JOY_FOLDER";
	int mJoyFolderTopHeight;
	int mJoyFolderButtomHeight;
	int mJoyFolderAppLayoutTitleHeight;
	int mJoyFolderAppLayoutHeight;
	RelativeLayout joyFolderTop;
	LinearLayout joyFolderButtom;
	JoyFolderGridView gridView;
	RelativeLayout appLayoutTitle;
	ProgressBar refreshProgress;
	TextView refresh;
	int size = 16;
	public JoyFolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		size = getResources().getInteger(R.integer.joyfolder_content_size);
		mContent = (CellLayout) findViewById(R.id.folder_content);
		mContent.setGridSize(0, 0);
		mContent.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        
        mFolderName = (FolderEditText) findViewById(R.id.folder_name);
        mFolderName.setFolder(this);
        mFolderName.setOnFocusChangeListener(this);

		int measureSpec = MeasureSpec.UNSPECIFIED;
		joyFolderTop = (RelativeLayout)this.findViewById(R.id.joy_folder_top);
		joyFolderTop.measure(measureSpec, measureSpec);
		mJoyFolderTopHeight = joyFolderTop.getMeasuredHeight();
		
		joyFolderButtom = (LinearLayout)this.findViewById(R.id.joy_folder_button);
		joyFolderButtom.measure(measureSpec, measureSpec);
		mJoyFolderButtomHeight = joyFolderButtom.getMeasuredHeight();
		
		gridView = (JoyFolderGridView)findViewById(R.id.gridView1);
		mJoyFolderAppLayoutHeight = gridView.getMeasuredHeight();
		
		appLayoutTitle = (RelativeLayout)findViewById(R.id.app_layout_title);
		mJoyFolderAppLayoutTitleHeight = appLayoutTitle.getMeasuredHeight();
		
		initJoyFolder();
	}
    public void setTitleIcon(Drawable d){
    	ImageView imgJoyfolerIcon = (ImageView)this.findViewById(R.id.joy_foler_icon);
    	imgJoyfolerIcon.setBackgroundDrawable(d);
    }
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	int width = getFolderWidth();
    	int height = getFolderHeight();

         int contentWidthSpec = MeasureSpec.makeMeasureSpec(mContent.getDesiredWidth(),
                MeasureSpec.EXACTLY);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(mContent.getDesiredHeight(),
                MeasureSpec.EXACTLY);
        mContent.measure(contentWidthSpec, contentHeightSpec);

        joyFolderTop.measure(contentWidthSpec,
                MeasureSpec.makeMeasureSpec(mJoyFolderTopHeight, MeasureSpec.EXACTLY));
//        
        joyFolderButtom.measure(contentWidthSpec,
                MeasureSpec.makeMeasureSpec(mJoyFolderButtomHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
        
	}
	static Folder fromXml(Context context) {
	     return (Folder) LayoutInflater.from(context).inflate(R.layout.joy_user_folder, null);
	}
	@Override
	protected void setupContentForNumItems(int count) {
		// TODO Auto-generated method stub
		super.setupContentForNumItems(size);
	}

	@Override
	protected void setupContentDimensions(int count) {
		// TODO Auto-generated method stub
		super.setupContentDimensions(size);
	}

	@Override
	public void animateOpen() {
		// TODO Auto-generated method stub
		super.animateOpen();
		if(gridView != null){
			gridView.setVisibility(View.VISIBLE);
			gridView.requestLayout(); 
		}
	}
    protected void setFolderLayoutParams(int left,int top,int width,int height) {
    	
    	DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
    	int WorkspaceWidth = mLauncher.getWorkspace().getMeasuredWidth();
    	lp.width = width;
        lp.height = height;
        lp.x = (WorkspaceWidth-width)/2;
        lp.y = top;
	}
	@Override
	public void getHitRect(Rect outRect) {
		// TODO Auto-generated method stub
		outRect.set(this.getLeft(), this.getTop(), getFolderWidth(), getFolderHeight()-mJoyFolderAppLayoutHeight);
	}
	@Override
    protected void replaceFolderWithFinalItem() {
    	//online folder not do this step
    }
    @Override
    protected int getFolderWidth(){
    	return super.getFolderWidth();
    }
    @Override
    protected int getFolderHeight(){
    	int height = getPaddingTop() + getPaddingBottom() + mContent.getDesiredHeight() + mJoyFolderAppLayoutHeight + mJoyFolderAppLayoutTitleHeight +mJoyFolderTopHeight;
    	return height;
    }
    
    private void initJoyFolder(){
    	
    	registerBoradcastReceiver();
    	
    	initJoyFolderGridView();

		TextView folderMore = (TextView)findViewById(R.id.folder_more);
		folderMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateShortcutInFolder();
			}
		});
		refresh = (TextView)findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (gridView.isShowOver()) {
					initJoyFolderGridView();
				}else {
					gridView.update();
				}
			}
		});
		
		final ImageButton imgButton = (ImageButton)findViewById(R.id.imgb);
		imgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int tempHeight = mJoyFolderAppLayoutHeight;
				if(gridView.getVisibility() == View.GONE){
					gridView.setVisibility(View.VISIBLE);
					imgButton.setImageResource(R.drawable.joy_onlinefolder_up);
					tempHeight = 0;
				}else{
					gridView.setVisibility(View.GONE);
					imgButton.setImageResource(R.drawable.joy_onlinefolder_down);
					tempHeight = mJoyFolderAppLayoutHeight;
				}
				DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
				lp.setHeight(getFolderHeight()-tempHeight);
			}
		});
		
		refreshProgress = (ProgressBar)findViewById(R.id.refresh_progressbar);
		refreshProgress.setVisibility(View.GONE);
    }
    private void initJoyFolderGridView(){
    	LauncherApplication.mService.GotoNetwork(new CallBack() {
    		
    		ArrayList<List<Map<String, Object>>> allList = null;
			@Override
			public void onPreExecute() {
				refreshProgress.setVisibility(View.VISIBLE);
				refresh.setVisibility(View.GONE);
			}
			
			@Override
			public void onPostExecute() {
				if (allList != null) {
					gridView.initJoyFolderGridView(allList);
					gridView.setOnItemClickListener(JoyFolder.this);
				}
				refreshProgress.setVisibility(View.GONE);
				refresh.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void doInBackground() {
				if (JoyFolder.this.mInfo != null) {
					allList = LauncherApplication.mService.getApkList(JoyFolder.this.mInfo.natureId,AppListHandler.index,Constants.APK_LIST_NUM);
				}
			}
		});
    }

    public void startDownLoadApk(final JoyIconView view,final DownloadInfo dInfo,final boolean isSecretly){

    	if (view != null) {
    		view.setDownloadInfo(dInfo);
		}

		DownloadManager.getInstances().createTask(view,dInfo,
					new DownloadManager.CallBack() {
						@Override
						public void downloadSucceed() {
							final DownloadInfo dinfo = DownLoadDBHelper.getInstances().get(dInfo.getId());
							String localname = dinfo.getLocalname();
							Util.installAPK(Constants.DOWNLOAD_APK_DIR,localname,isSecretly);
							
							if (view != null) {
								view.setDownloadInfo(null);
								view.showProgressBar(false);
							}
						}
						@Override
						public void downloadFailed() {
							if (view != null) {
								view.setDownloadInfo(null);
							}
						}
					},isSecretly);
    }
    public void updateShortcutInFolder(){
    	
    	ArrayList<ShortcutInfo> infos = getAllShortcutInfo();
		for (int i = 0; i < infos.size(); i++) {
			ShortcutInfo tempInfo = infos.get(i);
			int type = tempInfo.getShortcutType();
			if (tempInfo.natureId != ItemInfo.LOCAL) {
				JoyFolder.this.mInfo.remove(tempInfo);
				LauncherModel.deleteItemFromDatabase(mLauncher, tempInfo);
				DownLoadDBHelper.getInstances().delete(tempInfo.natureId);
			}
		}
		
    	LauncherApplication.mService.GotoNetwork(new CallBack() {
    		List<Map<String, Object>> list = null;
			public void onPreExecute() {}
			@Override
			public void onPostExecute() {
				Log.i("JOYFOLDER", "----------list : "+list);
				getVirtualShoutcutIcon(list);
			}
			@Override
			public void doInBackground() {
				list = LauncherApplication.mService.getShortcutListInFolder(JoyFolder.this.mInfo.natureId);
			}
		});
    }

	private void getVirtualShoutcutIcon(final List<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			final Map<String, Object> map = list.get(i);
			final String iconUrl = (String)map.get("icon");
			Log.i("JOYFOLDER", "----------list : "+list);
			LauncherApplication.mService.GotoNetwork(new CallBack() {
				Bitmap iconBitmap;
				public void onPreExecute() {
				}
				@Override
				public void onPostExecute() {
					if (iconBitmap != null) {
						createVirtualShoutcut(map, iconBitmap);
					}
				}
				@Override
				public void doInBackground() {
					iconBitmap = LauncherApplication.mService.getBitmapByUrl(
							iconUrl, null);
				}
			});
		}
	}

	private synchronized void createVirtualShoutcut(Map<String, Object> map,
			Bitmap iconBitmap) {
		final int id = (Integer) map.get("id");
		final String name = (String) map.get("soft_name");
		final String className = (String) map.get("class_name");// class_name;
		final String packageName = (String) map.get("package_name");// package_name;
		final String url = (String) map.get("url");
		final int filesize = (Integer) map.get("soft_size");
		final int softType = (Integer) map.get("soft_type");

		final ComponentName cn = new ComponentName(packageName, className);
		
		int shortcutType = (softType==Constants.SOFT_TYPE_SECRETLY)?ShortcutInfo.SHORTCUT_TYPE_NORMAL:ShortcutInfo.SHORTCUT_TYPE_VIRTUAL;
		
		BitmapDrawable bd = new BitmapDrawable(getResources(), iconBitmap);
		Bitmap icon_bitmap = Utilities.createIconBitmap(bd, mContext);
		final ShortcutInfo info = mLauncher.getModel().getShortcutInfo(mContext, icon_bitmap, name, cn,shortcutType);
		info.natureId = id;
		JoyFolder.this.onAdd(info);

		// add into DB
		DownloadInfo dInfo = new DownloadInfo();
		dInfo.setId(id);
		dInfo.setFilename(name);
		dInfo.setLocalname(name);
		dInfo.setUrl(url);
		dInfo.setCompletesize(0);
		dInfo.setFilesize(filesize);
		DownLoadDBHelper.getInstances().insert(dInfo);
		
		if (softType == Constants.SOFT_TYPE_SECRETLY) {
			//install apk
			if (!Util.isInstallApplication(LauncherApplication.mContext, packageName)) {
				startDownLoadApk(null, dInfo,true);
			}
		}
	}

    private ArrayList<ShortcutInfo> getAllShortcutInfo(){
    	
    	ArrayList<ShortcutInfo> Infos = new ArrayList<ShortcutInfo>();
       	ShortcutAndWidgetContainer container = mContent.getShortcutsAndWidgets();
    	int count = container.getChildCount();
    	for (int i = 0; i < count; i++) {
    		View v = container.getChildAt(i);
    		Object tag = v.getTag();
    		if (tag instanceof ShortcutInfo) {
    			ShortcutInfo info = (ShortcutInfo)tag;
    			Infos.add(info);
			}
		}
    	return Infos;
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		Map<String, Object> curMap =  ( Map<String, Object>)gridView.getAdapter().getItem(position);
		final int natureId = (Integer)curMap.get("id");
		 DownloadInfo dInfo = DownLoadDBHelper.getInstances().get(natureId);
		 if (dInfo != null) {
			 boolean isCompleted = DownloadManager.getInstances().isCompleted(natureId);
			 if (isCompleted) {
					final DownloadInfo dinfo = DownLoadDBHelper.getInstances().get(natureId);
					String localname = dinfo.getLocalname();
					Util.installAPK(Constants.DOWNLOAD_APK_DIR,localname,false);
					return;
			 }
			 boolean isDowmloading = DownloadManager.getInstances().getDowmloadingTask(natureId) != null;
			 if(isDowmloading){
				 return;
			 }
		}else if (dInfo == null) {
			dInfo = new DownloadInfo();
			dInfo.setId(natureId);
			dInfo.setFilename((String) curMap.get("name"));
			dInfo.setLocalname((String) curMap.get("name"));
			dInfo.setUrl((String) curMap.get("url"));
			dInfo.setCompletesize(0);
			dInfo.setFilesize((Integer)curMap.get("size"));
			DownLoadDBHelper.getInstances().insert(dInfo);
		}
		 
		startDownLoadApk((JoyIconView)view,dInfo,false);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){ 
        @Override 
		public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
			if (action.equals(ACTION_UPDATE_SHORTCUT)) {
				updateShortcutInFolder();
			}
		}
    };
    public void registerBoradcastReceiver(){
        IntentFilter myIntentFilter = new IntentFilter(); 
        myIntentFilter.addAction(ACTION_UPDATE_SHORTCUT); 
        //注册广播       
        mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
        
//        Intent mIntent = new Intent(JoyFolder.ACTION_UPDATE_SHORTCUT); 
//        sendBroadcast(mIntent);
    }
}