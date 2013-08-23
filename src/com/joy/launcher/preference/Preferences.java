/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joy.launcher.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.joy.launcher.IconStyleActivity;
import com.joy.launcher.LauncherApplication;
import com.joy.launcher.R;

public class Preferences extends PreferenceActivity 
            implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Launcher.Preferences";
	private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefs =
            getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//                editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
//                editor.commit();

        // Remove some preferences on large screens
        if (LauncherApplication.isScreenLarge()) {
            PreferenceGroup homescreen = (PreferenceGroup) findPreference("ui_homescreen");
            homescreen.removePreference(findPreference("ui_homescreen_grid"));
            homescreen.removePreference(findPreference("ui_homescreen_screen_padding_vertical"));
            homescreen.removePreference(findPreference("ui_homescreen_screen_padding_horizontal"));
            homescreen.removePreference(findPreference("ui_homescreen_indicator"));

            PreferenceGroup drawer = (PreferenceGroup) findPreference("ui_drawer");
            drawer.removePreference(findPreference("ui_drawer_indicator"));
        }

        Preference version = findPreference("application_version");
        version.setTitle(getString(R.string.application_name));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
    	String key = preference.getKey();
    	if(PreferencesProvider.ICON_STYLE_KEY.equals(key))
    	{
    		Intent intent = new Intent();
    		intent.setClass(this, IconStyleActivity.class);
    		startActivity(intent);
    	}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//    	Log.e(TAG, "----------onSharedPreferenceChanged() : " ); 
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
        editor.commit();

    }
}
