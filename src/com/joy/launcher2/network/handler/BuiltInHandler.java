package com.joy.launcher2.network.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.joy.launcher2.util.Util;
/**
 * 内置apk 、widget、虚框软件、在线文件夹 解析
 * @author User
 *
 */
public class BuiltInHandler {
	private static final String TAG = "BuiltInHandler";
	private static final Boolean DEBUG = true;

	public List<Map<String, Object>> getBuiltInShortcutList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			String string = Util.getStringFromAssets("built-in.txt");
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonarry = jsonObject.getJSONArray("built_in_shortcut");

			int length = jsonarry.length();
			for (int i = 0; i < length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				JSONObject item = jsonarry.getJSONObject(i);
				
				map.put("id", item.getInt("id"));
				map.put("container", item.getInt("container"));
				map.put("icon", item.getString("icon"));
				map.put("name", item.getString("name"));
				map.put("url", item.getString("url"));
				map.put("filesize", item.getInt("filesize"));
				map.put("packageName", item.getString("packageName"));
				map.put("className", item.getString("className"));
				map.put("title", item.getString("title"));
				map.put("screen", item.getInt("screen"));
				map.put("x", item.getInt("x"));
				map.put("y", item.getInt("y"));
				
				list.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w(TAG, "-- e:"+e);
			return null;
		}
		return list;
	}

	public List<Map<String, Object>> getBuiltInJoyFolderList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			String string = Util.getStringFromAssets("built-in.txt");
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonarry = jsonObject.getJSONArray("built_in_joyfolder");
			int length = jsonarry.length();
			for (int i = 0; i < length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				JSONObject item = jsonarry.getJSONObject(i);
				
				map.put("id", item.getInt("id"));
				map.put("natureId", item.getInt("natureId"));
				map.put("icon", item.getString("icon"));
				map.put("title", item.getString("title"));
				map.put("screen", item.getInt("screen"));
				map.put("x", item.getInt("x"));
				map.put("y", item.getInt("y"));
				list.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w(TAG, "-- e:"+e);
			return null;
		}
		return list;
	}

	public List<Map<String, Object>> getBuiltInWidgetList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			String string = Util.getStringFromAssets("built-in.txt");
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonarry = jsonObject.getJSONArray("built_in_widget");
			int length = jsonarry.length();
			for (int i = 0; i < length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				JSONObject item = jsonarry.getJSONObject(i);
				
				map.put("packageName", item.getString("packageName"));
				map.put("className", item.getString("className"));
				map.put("screen", item.getInt("screen"));
				map.put("x", item.getInt("x"));
				map.put("y", item.getInt("y"));
				map.put("spanX", item.getInt("spanX"));
				map.put("spanY", item.getInt("spanY"));
				list.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w(TAG, "-- e:"+e);
			return null;
		}
		return list;
	}

}