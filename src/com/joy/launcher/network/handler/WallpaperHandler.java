package com.joy.launcher.network.handler;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.graphics.Bitmap;

import com.joy.launcher.util.Util;

/**
 * 解析json数据与壁纸的逻辑处理
 * @author wanghao
 *
 */
public class WallpaperHandler {
	
	class WallpaperInfo{
		int id;//壁纸编号
		Bitmap icon;//壁纸缩略图
		String Category;//壁纸类型
		String name;//壁纸名称
		String url;//图片下载链接
	}
	
	String tempjson = "{ \"state\": 1, \"item\": [ { \"id\": 1, \"icon\": \"?op=9001&type=1&id=1\", \"name\":\"xxxx\", \"description\": \"xxxxxx\" }, { \"id\": 2, \"icon\": \"?op=9001&type=1&id=1\", \"name\": \"xxxx\", \"description\": \"xxxxxx\" } ] }";
	String TAG = "JSON";
	public void getWallpaperType(JSONObject json){
		try {
			JSONTokener jsonParser = new JSONTokener(tempjson);//为了测试，根据tempjson生成json对象。
			JSONObject wallpager = (JSONObject) jsonParser.nextValue();
			int state = wallpager.getInt("state");
			Util.i(TAG, "state: "+state);
			JSONArray jsonarry = wallpager.getJSONArray("item");
			int length = jsonarry.length();
			for(int i=0;i<length;i++){
				JSONObject item = jsonarry.getJSONObject(i);
				int id = item.getInt("id");
				String icon = item.getString("icon");
				String name = item.getString("name");
				String description = item.getString("description");
				Util.i(TAG, "id: "+id+"  icon: "+icon+"  name: "+name+"  description: "+description);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<WallpaperInfo> getWallpaperList(){
		
		return null;
	}
	
}
