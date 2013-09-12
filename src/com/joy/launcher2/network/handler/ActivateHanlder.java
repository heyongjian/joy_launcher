package com.joy.launcher2.network.handler;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.util.Constants;

public class ActivateHanlder {

	public boolean isActivate(JSONObject result){
		int state = 0;
		try {
			state = result.getInt("state");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return state==1;
	}
}
