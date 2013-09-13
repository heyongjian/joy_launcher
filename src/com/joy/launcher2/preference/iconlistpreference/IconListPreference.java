package com.joy.launcher2.preference.iconlistpreference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.joy.launcher2.R;
import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.preference.iconlistpreference.IconListView.CallBack;
public class IconListPreference extends Preference {

	IconListView layout;
	AlertDialog dialog;
	CharSequence[] entries = null;
	CharSequence[] entryValues = null;
	CharSequence defaultValue;
	TypedArray entryDrawables;
	private int initCurrentID = 0;

	public IconListPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public IconListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	public IconListPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.IconListPrefrence);
		entries = a.getTextArray(R.styleable.IconListPrefrence_list_entries);
		entryValues = a.getTextArray(R.styleable.IconListPrefrence_list_entryValues);
		defaultValue = a.getText(R.styleable.IconListPrefrence_list_defaultValue);
	 
		defaultValue = (defaultValue==null)?"null":defaultValue;
		int drawid = a.getResourceId(R.styleable.IconListPrefrence_list_entryDrawables, -1);

		if(drawid > 0)
		{
			entryDrawables = context.getResources().obtainTypedArray(drawid);
		}
		a.recycle();
	}

	@Override
	protected void onClick() {
		// TODO Auto-generated method stub
		Context context = this.getContext();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = (IconListView) inflater.inflate(
				R.layout.iconlistview, null);

		layout.initListView(context, entries, entryValues, entryDrawables,
				new CallBack() {

					@Override
					public void OnMyClickListener() {
						// TODO Auto-generated method stub
						dialog.dismiss();
						saveData();
					}
				});

		String canclString = context.getResources().getString(R.string.cancel_action);

		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialogtype);
		builder.setTitle(getTitle());
		builder.setView(layout);
		builder.setNegativeButton(canclString, null);
		dialog = builder.show();
		
		readData();
	}

	private void readData() {
		SharedPreferences sharedpre = this.getContext().getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_WORLD_READABLE);
		String stringData = sharedpre.getString(this.getKey(), (String) defaultValue);
		for (int i = 0; i < entryValues.length; i++) {
			if (stringData.equals(entryValues[i])) {
				initCurrentID = i;
				break;
			}
		}
		layout.setCurrentID(initCurrentID);
	}
	
	private void saveData(){
		SharedPreferences sharedpre = this.getContext().getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_WORLD_WRITEABLE);
		Editor edit = sharedpre.edit();
		String dataString = layout.getStringData();
		edit.putString(IconListPreference.this.getKey(),
				dataString);
		edit.commit();
	}

}
