package com.joy.launcher;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 在线文件夹中应用推荐列表
 * @author wanghao
 *
 */
public class JoyFolderAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	List<Map<String, Object>> list;
	Context c;
	
	public JoyFolderAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		this.c = context;
	}
	public void setList(List<Map<String, Object>> list2){
		this.list = list2;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder myHolder;

		if (convertView == null) {
			myHolder = new Holder();
			convertView = inflater.inflate(R.layout.joy_folder_app_icon, null);
			myHolder.img = (ImageView) convertView.findViewById(R.id.imgv);
			myHolder.tv = (TextView) convertView.findViewById(R.id.textv);
			convertView.setTag(myHolder);
		} else {
			myHolder = (Holder) convertView.getTag();
		}

		int id = (Integer) list.get(position).get("img");
		myHolder.img.setImageResource(id);
		myHolder.tv.setText(list.get(position).get("name").toString());
		return convertView;
	}


	class Holder {
		ImageView img;
		TextView tv;
	}
}
