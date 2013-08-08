package com.joy.launcher.preference.mylistpreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.joy.launcher.R;

public class DialogListView extends ListView {

	List<Map<String, Object>> list;
	ListView myList;
	MyListAdapter adapter;
	int currentID = -1;

	public DialogListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DialogListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DialogListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void initListView(Context context,CharSequence[] entries,CharSequence[] entryValues,TypedArray entryDrawables,final CallBack callback) {
		
		myList = (ListView) findViewById(R.id.my_list);
		initData(context,entries,entryValues,entryDrawables);
		adapter = new MyListAdapter(context, list);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position != currentID) {
					setCurrentID(position);
				}
				currentID = position;
				if(callback != null){
					callback.OnMyClickListener();
				}
			}
		});
	}
	
	public void setCurrentID(int curid){
		currentID = curid;
		adapter.setCurrentID(currentID);
		adapter.notifyDataSetChanged();
	}

	public String getStringData(){
		Map<String, Object> map = list.get(currentID);
		String data = (String)map.get("value");
		return data;
	}
	public void initData(Context context,CharSequence[] entries,CharSequence[] entryValues,TypedArray entryDrawables) {

		list = new ArrayList<Map<String, Object>>();

		int size = entries.length;
		for (int i = 0; i < size; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("img", entryDrawables.getDrawable(i));
			map.put("title", entries[i]);
			map.put("value", entryValues[i]);
			list.add(map);
		}
	}
	public interface CallBack{
		void OnMyClickListener();
	};
}

