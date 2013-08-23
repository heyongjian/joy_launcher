package com.joy.launcher;

import java.util.ArrayList;
import java.util.HashMap;

import com.joy.launcher.preference.PreferencesProvider;
import com.joy.launcher.preference.PreferencesProvider.TextStyle;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class IconStyleActivity extends Activity implements OnClickListener{

	HVFrameLayout hvFramelayout;
	Button sure;
	Button cancel;
	
	ArrayList<Integer> backgrounds = new ArrayList<Integer>();
	HashMap<Integer, String> icons = new HashMap<Integer, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.icon_style);
		
		Resources res = getResources();
		String packageName = getPackageName();
		int hItem = 0;
		int vItem = 0;
		String name = PreferencesProvider.Interface.Homescreen.getIconStyle(this, "");
		TextStyle textStyle = PreferencesProvider.Interface.Homescreen.getIconTextStyle(this, TextStyle.Marquee.toString());
		TextStyle[] textStyles = TextStyle.values();
		for(int i = 0; i < textStyles.length; i++)
		{
			TextStyle ts = textStyles[i];
			if(ts.toString().equals(textStyle.toString()))
			{
				hItem = i;
				break;
			}
		}
		String[] iconBgNames = getResources().getStringArray(R.array.icon_bg_names);
		for(int j = 0; j < iconBgNames.length; j++)
		{
			String iconName = iconBgNames[j];
			int iconId = res == null ?0 : res.getIdentifier(iconName, "drawable", packageName);
			if(iconId > 0)
			{
				backgrounds.add(iconId);
				icons.put(iconId, iconName);
				if(iconName.equals(name))
				{
					vItem = j;
				}
			}

		}
	
		DisplayMetrics dm  = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int iconWidth = width / 3;
		int iconHeight = iconWidth;
		hvFramelayout = (HVFrameLayout)findViewById(R.id.hv_scroll);		
		hvFramelayout.setContent(iconWidth, iconHeight, backgrounds);
		hvFramelayout.setHVItems(hItem, vItem);
		
		sure = (Button)findViewById(R.id.icon_style_sure);
		cancel = (Button)findViewById(R.id.icon_style_cancel);
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id)
		{
		case R.id.icon_style_sure:
			int[] items = new int[2];
			if(hvFramelayout.getHVItem(items))
			{
				SharedPreferences sp = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				String iconName =  icons.get(backgrounds.get(items[1]));
				editor.putString(PreferencesProvider.ICON_STYLE_KEY, iconName);
				
				PreferencesProvider.TextStyle testSyle = PreferencesProvider.TextStyle.values()[items[0]];
				editor.putString(PreferencesProvider.ICON__TEXT_STYLE_KEY, testSyle.toString());
				editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
				editor.commit();
			}
			finish();
			break;
		case R.id.icon_style_cancel:
			finish();
			break;
			
		}
	}
}
