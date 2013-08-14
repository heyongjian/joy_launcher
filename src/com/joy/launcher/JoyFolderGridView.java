package com.joy.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridView;

/**
 *在线文件夹中应用推荐列表
 * @author wanghao
 *
 */
public class JoyFolderGridView extends GridView{
	List<Map<String, Object>> allList;
	JoyFolderAdapter adtaAdapter;
	Context mContext;
	public JoyFolderGridView(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public JoyFolderGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public JoyFolderGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initJoyFolderGridView(List<Map<String, Object>> l){
		allList = l;
		adtaAdapter = new JoyFolderAdapter(mContext);
		List<Map<String, Object>> list = getAppFromArray(4);
		adtaAdapter.setList(list);
		
		setAdapter(adtaAdapter);
	}
	
	public void update(){
		List<Map<String, Object>> list = getAppFromArray(4);
		adtaAdapter.setList(list);
		adtaAdapter.notifyDataSetChanged();
		
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

	int group=0;
	public List<Map<String, Object>> getAppFromArray(int num) {
		
		List<Map<String, Object>> showAppList = new ArrayList<Map<String, Object>>();
		for (int i = group; i < group+num; i++) {
		 
			int index = i%allList.size();
			Map<String, Object> info = allList.get(index);
			int count = showAppList.size();
			if(count<num){
				showAppList.add(info);
			}
		}
		group+=num;
		return showAppList;
	}
}
