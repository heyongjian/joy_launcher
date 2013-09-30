package com.joy.launcher2.network.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class VirtualShortcutListHandler {
	private static final String TAG = "VirtualShortcutListHandler";
	private static final Boolean DEBUG = true;

	public List<Map<String, Object>> geShortcutList(JSONObject json) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if (json == null || json.getInt("state") != 1) {
				return null;
			}
			JSONArray jsonarry = json.getJSONArray("item");
		
		int length = jsonarry.length();
		for(int i=0;i<length;i++){
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject item = jsonarry.getJSONObject(i);
			map.put("id", item.getInt("id"));
			map.put("icon", item.getString("icon"));
			map.put("soft_type", item.getInt("type"));
			map.put("class_name", item.getString("packageName"));//
			map.put("package_name", item.getString("packageName"));
			map.put("soft_name", item.getString("name"));
			map.put("soft_size", item.getInt("size"));
			map.put("url", item.getString("url"));
			list.add(map);
		}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "-------e:"+e);
			return null;
		}
		return list;
	}
}