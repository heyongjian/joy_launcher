package com.joy.launcher2.joyfolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joy.launcher2.CellLayout;
import com.joy.launcher2.DragLayer;
import com.joy.launcher2.Folder;
import com.joy.launcher2.FolderEditText;
import com.joy.launcher2.R;

/**
 * online folder
 * @author wanghao
 *
 */
public class JoyFolder extends Folder{
	int mJoyFolderTopHeight;
	int mJoyFolderButtomHeight;
	int mJoyFolderAppLayoutTitleHeight;
	int mJoyFolderAppLayoutHeight;
	RelativeLayout joyFolderTop;
	LinearLayout joyFolderButtom;
	JoyFolderGridView gridView;
	RelativeLayout appLayoutTitle;

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
    
    public void initJoyFolder(){
    	List<Map<String, Object>> allList = getAppList();
 
		gridView.initJoyFolderGridView(allList);
 
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Map<String, Object> curMap =  ( Map<String, Object>)gridView.getAdapter().getItem(position);
				 Toast.makeText(JoyFolder.this.getContext(), curMap.get("name")+"--暂未实现下载功能", Toast.LENGTH_SHORT).show();
			}});
		
		TextView textView = (TextView)findViewById(R.id.refresh);
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gridView.update();
				
			}
		});
		final ImageButton imgButton = (ImageButton)findViewById(R.id.imgb);
		imgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
    }
	public List<Map<String, Object>> getAppList(){
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		 for(int i=0;i<16;i++){
			 Map<String, Object> map = new HashMap<String, Object>();
			 map.put("img", R.drawable.joy_folder_app_icon_default);
			 map.put("name", "应用"+i);
			 list.add(map);
		 }
		 return list;
	}
}