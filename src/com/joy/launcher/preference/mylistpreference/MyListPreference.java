package com.joy.launcher.preference.mylistpreference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.joy.launcher.R;
import com.joy.launcher.preference.PreferencesProvider;
import com.joy.launcher.preference.mylistpreference.DialogListView.CallBack;
public class MyListPreference extends Preference {

	DialogListView layout;
	AlertDialog dialog;
	CharSequence[] entries = null;
	CharSequence[] entryValues = null;
	TypedArray entryDrawables;
	private int initCurrentID = 0;

	public MyListPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	public MyListPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.MyListPrefrence);
		entries = a.getTextArray(R.styleable.MyListPrefrence_entries);
		entryValues = a.getTextArray(R.styleable.MyListPrefrence_entryValues);
		int drawid = a.getResourceId(R.styleable.MyListPrefrence_entryDrawables, -1);

		entryDrawables = context.getResources().obtainTypedArray(drawid);
		a.recycle();

	}

	@Override
	protected void onClick() {
		// TODO Auto-generated method stub
		Context context = this.getContext();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = (DialogListView) inflater.inflate(
				R.layout.dialoglistview, null);

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
		String stringData = sharedpre.getString(this.getKey(), "null");
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
		edit.putString(MyListPreference.this.getKey(),
				dataString);
		edit.commit();
		System.out.println("dataString : " + dataString);
	}

}
