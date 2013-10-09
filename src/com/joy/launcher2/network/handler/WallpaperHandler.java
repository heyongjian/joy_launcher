package com.joy.launcher2.network.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.joy.launcher2.cache.JsonFile;
import com.joy.launcher2.wallpaper.CategoryInfo;
import com.joy.launcher2.wallpaper.WallpaperInfo;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 解析json数据与壁纸的逻辑处理
 * @author wanghao
 *
 */
public class WallpaperHandler {
	

	//add by huangming for online wallpaper
	/**
	 * 通过jsonfile获得json对象
	 * @param jsonFile
	 * @param isNative
	 * @return JSONObject
	 */
	public static JSONObject createWPListJson(final JsonFile jsonFile, 
			final boolean isNative)
	{
		JSONObject json = jsonFile.getJsonFromFile("native_json_item", isNative);
		if(json.isNull("state"))
		{
			
			try {
				json.put("state", 1);
				json.put("item", new JSONArray());
				JSONObject page = new JSONObject();
				page.put("ps", 0);
				page.put("pi", 0);
				page.put("pn", 0);
				page.put("rn", 0);
				json.put("page", page);
				jsonFile.saveJsonToFile(json, "native_json_item", isNative);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return json;
	}
	
	/**
	 * 将一条信息写入jsonfile
	 * @param wi
	 * @param jsonFile
	 */
	public static void putJsonToNative(final WallpaperInfo wi, final JsonFile jsonFile)
	{
		JSONObject json = createWPListJson(jsonFile, true);
		try {
			JSONArray items = json.getJSONArray("item");
			JSONObject item = new JSONObject();
			item.put("id", wi.id);
			item.put("size",wi.size);
			item.put("name", wi.wallpaperName);
			item.put("icon", wi.urls[0]);
			item.put("preview", wi.url);
			item.put("url", wi.urls[1]);
			items.put(item);
			jsonFile.saveJsonToFile(json, "native_json_item", true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过json对象获得壁纸信息
	 * @param wis
	 * @param wisThum
	 * @param json
	 * @param isNative
	 * @return
	 */
	public static int wallpaperList(final ArrayList<WallpaperInfo> wis, 
			final ArrayList<WallpaperInfo> wisThum, 
			final JSONObject json, 
			final boolean isNative)
	{
		int i = 0;
		try {
			if(json != null && !json.isNull("state")&& json.getInt("state") == 1)
			{
				int state = json.getInt("state");
				JSONArray items = json.getJSONArray("item");
				JSONObject page = json.getJSONObject("page");
				int pi = page.getInt("pi"); //当前页
				int ps = page.getInt("ps");  //每次解析一页，每一页的数据量
				int pn = page.getInt("pn");
				int rn = page.getInt("rn");
				
				for(i = 0;i < items.length(); i++)
				{
					JSONObject item = items.getJSONObject(i);
					int id = item.isNull("id")?0:item.getInt("id");
					String iconThumUrl = item.isNull("icon")?null:getUrl(item.getString("icon"));
					int iconSize = item.isNull("size")?0:item.getInt("size");
					String iconName = item.isNull("name")?null:item.getString("name");
					String iconUrl = item.isNull("url")?null:getUrl(item.getString("url"));
					String previewlUrl = item.isNull("preview")?null:getUrl(item.getString("preview"));
					
					WallpaperInfo wiThum = new WallpaperInfo();
					wiThum.id = id;
					wiThum.size = iconSize;
					wiThum.isNative = isNative;
					wiThum.isThumbnail = true;
					wiThum.url = iconThumUrl;
					wiThum.wallpaperName = iconName;
					wisThum.add(wiThum);
					
					WallpaperInfo wi = new WallpaperInfo();
					wi.id = id;
					wi.size = iconSize;
					wi.isNative = isNative;
					wi.isThumbnail = false;
					wi.url = previewlUrl;
					wi.wallpaperName = iconName;
					wi.urls[0] = iconThumUrl;
					wi.urls[1] = iconUrl;
					wis.add(wi);
					
				}
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
	/**
	 * 去除特殊字符“？”
	 * @param url
	 * @return String
	 */
	public static String getUrl(String url) {
		if (url == null || "".equals(url.trim())) {
			return null;
		}
		return url.substring(url.lastIndexOf("?") + 1);
	}
	
	/**
	 * 通过json对象获得壁纸列表
	 * @param cInfos
	 * @param json
	 */
	public static void wallpaperCategoryList(final List<CategoryInfo> cInfos, 
			final JSONObject json)
	{
		try {
			if(json != null && !json.isNull("state")&& json.getInt("state") == 1)
			{
				int state = json.getInt("state");
				JSONArray items = json.getJSONArray("item");
				for(int i = 0;i < items.length(); i++)
				{
					JSONObject item = items.getJSONObject(i);
					int id = item.getInt("id");
					String categoryThumUrl = item.isNull("icon")?null:getUrl(item.getString("icon"));
					String categoryName = item.isNull("name")?null:item.getString("name");
					String categoryDescription = item.isNull("memo")?null:item.getString("memo");
					CategoryInfo ci = new CategoryInfo();
					ci.id = id;
					ci.url = categoryThumUrl;
					ci.name = categoryName;
					ci.description = categoryDescription;
					cInfos.add(ci);
				}
					
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//end
	
}
