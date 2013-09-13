package com.joy.launcher2.preference.iconlistpreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.joy.launcher2.R;

public class IconListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Map<String, Object>> list;
	private Context c;
	private int currentID = 0;
	
	public IconListAdapter(Context context, List<Map<String, Object>> list2) {
		inflater = LayoutInflater.from(context);
		this.c = context;
		this.list = list2;
	}

	public void setList(ArrayList<Map<String, Object>> list) {
		this.list = list;
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
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder myHolder;
		if (convertView == null) {
			myHolder = new Holder();
			convertView = inflater.inflate(R.layout.joy_list_single, null);
			myHolder.tv1 = (TextView) convertView.findViewById(R.id.title); 
			myHolder.img = (ImageView) convertView.findViewById(R.id.icon);
			myHolder.cbox = (ImageView) convertView.findViewById(R.id.checkbox);
			convertView.setTag(myHolder);
		} else {
			myHolder = (Holder) convertView.getTag();
		}

		if (position == this.currentID)
			myHolder.cbox.setImageDrawable(c.getResources().getDrawable(
					R.drawable.common_radiobutton_selected)); 
		else
			myHolder.cbox.setImageDrawable(c.getResources().getDrawable(
					R.drawable.common_radiobutton_unselected));
		
		myHolder.img.setImageDrawable((Drawable)list.get(position).get("img"));
		myHolder.tv1.setText(list.get(position).get("title").toString());

		return convertView;
	}

	class Holder {
		ImageView img;
		ImageView cbox;
		TextView tv1;
	}

	public void setCurrentID(int currentID) {
		this.currentID = currentID;
	}

}
