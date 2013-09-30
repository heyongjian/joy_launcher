package com.joy.launcher2.joyfolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridView;
import android.widget.TextView;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.network.handler.AppListHandler;
import com.joy.launcher2.network.impl.Service.CallBack;

/**
 *在线文件夹中应用推荐列表
 * @author wanghao
 *
 */
public class JoyFolderGridView extends GridView{

	ArrayList<List<Map<String, Object>>> allList;
	JoyFolderAdapter adtaAdapter;
	Context mContext;
	int curPage=0;
	public JoyFolderGridView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public JoyFolderGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public JoyFolderGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	private void init(){
		adtaAdapter = new JoyFolderAdapter(mContext);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		adtaAdapter.setList(list);
		setAdapter(adtaAdapter);
		curPage = 0;
	}
	public void initJoyFolderGridView(ArrayList<List<Map<String, Object>>> allList){
		this.allList = allList;
		curPage = 0;
		update(curPage);
	}

	private void initAdapter(int page){
		List<Map<String, Object>> list = allList.get(curPage);
		adtaAdapter.setList(list);
		adtaAdapter.notifyDataSetChanged();
	}
	public boolean isShowOver(){
		if (allList==null) {
			return true;
		}
		 if (curPage>=allList.size()-1) {
			return true;
		}
		 return false;
	}
	public void update(int page){

		initAdapter(page);
		List<Map<String, Object>> list = allList.get(curPage);
		int count = this.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = this.getChildAt(i);
			ScaleAnimation scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
			scale.setFillAfter(true);
			scale.setRepeatCount(0);
			scale.setDuration(500);
			scale.start();
			view.setAnimation(scale);
		}
	}

	public void update(){
		
		if (!isShowOver()) {
			curPage++;
			update(curPage);
		}
	}

}
