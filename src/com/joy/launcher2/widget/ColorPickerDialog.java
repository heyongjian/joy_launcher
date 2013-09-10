/*
 * Copyright (C) 2013 joy
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

package com.joy.launcher2.widget;

import com.joy.launcher2.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ColorPickerDialog extends Dialog implements ColorPickerView.OnColorChangedListener,
		View.OnClickListener {


    private static final int CUSTOM_BACKGROUND_COLOR = 2;
    private static final String ACTION_APP_BACKGROUND_COLOR_CHANGED = "action_app_background_color_changed";
	private ColorPickerView mColorPicker;

	private ColorPickerPanelView mOldColor;
	private ColorPickerPanelView mNewColor;

	private OnColorChangedListener mListener;
	
	private Button mSubmitButton;
	private Button mCancleButton;
	private Button mFirstColorButton;
	private Button mSecondColorButton;
	private Button mThirdColorButton;
	private Button mFourthColorButton;
	private Button mFifthColorButton;
	private Button mSixthColorButton;
	private Resources mResources;

    private SharedPreferences mSharedPreferences;

	public interface OnColorChangedListener {
		public void onColorChanged(int color);
	}
	
	public ColorPickerDialog(Context context, int initialColor) {
		super(context);
        mSharedPreferences = context.getSharedPreferences("app_background__info", 0);

		init(initialColor);
	}

	private void init(int color) {
		// To fight color banding.
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setUp(color);

	}

	public ColorPickerPanelView getNewColor()
    {
        return mNewColor;
    }

    private void setUp(int color) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		setContentView(layout);

		setTitle(R.string.dialog_color_picker);
		
		mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);
		mSubmitButton = (Button) layout.findViewById(R.id.btn_color_submit);
		mCancleButton = (Button) layout.findViewById(R.id.btn_color_cancle);
		mFirstColorButton = (Button) layout.findViewById(R.id.standard_color_first);
		mSecondColorButton = (Button) layout.findViewById(R.id.standard_color_second);
		mThirdColorButton = (Button) layout.findViewById(R.id.standard_color_third);
		mFourthColorButton = (Button) layout.findViewById(R.id.standard_color_fourth);
		mFifthColorButton = (Button) layout.findViewById(R.id.standard_color_fifth);
		mSixthColorButton = (Button) layout.findViewById(R.id.standard_color_sixth);
		
		mResources = getContext().getResources();
		
		((LinearLayout) mOldColor.getParent()).setPadding(
			Math.round(mColorPicker.getDrawingOffset()), 
			0, 
			Math.round(mColorPicker.getDrawingOffset()), 
			0
		);	
		
		mOldColor.setOnClickListener(this);
		mNewColor.setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);
		mCancleButton.setOnClickListener(this);
		mFirstColorButton.setOnClickListener(this);
		mSecondColorButton.setOnClickListener(this);
		mThirdColorButton.setOnClickListener(this);
		mFourthColorButton.setOnClickListener(this);
		mFifthColorButton.setOnClickListener(this);
		mSixthColorButton.setOnClickListener(this);
		
		mColorPicker.setOnColorChangedListener(this);
		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);

	}

	@Override
	public void onColorChanged(int color) {

		mNewColor.setColor(color);

		setAppBackgroundColorValue(color);
		/*
		if (mListener != null) {
			mListener.onColorChanged(color);
		}
		*/

	}

	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
	}
	
	/**
	 * Set a OnColorChangedListener to get notified when the color
	 * selected by the user has changed.
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mListener = listener;
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
	    switch (v.getId())
        {
            case R.id.btn_color_submit:
                if (mListener != null) {
                    mListener.onColorChanged(mNewColor.getColor());
                }
                setAppBackgroundType(CUSTOM_BACKGROUND_COLOR);
        		getContext().sendBroadcast(new Intent(ACTION_APP_BACKGROUND_COLOR_CHANGED));

                break;
            case R.id.btn_color_cancle:
                break;

            case R.id.standard_color_first:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color0));
            	break;
            case R.id.standard_color_second:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color1));
            	break;
            case R.id.standard_color_third:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color2));
            	break;
            case R.id.standard_color_fourth:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color3));
            	break;
            case R.id.standard_color_fifth:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color4));
            	break;
            case R.id.standard_color_sixth:
            	setStandardBackgroundColorValue(mResources.getColor(R.color.all_app_view_standar_color5));
            	break;
            	
            default:
                break;
        }

        dismiss();
	}
	
	private void setStandardBackgroundColorValue (int color) {
		if (mListener != null) {
            mListener.onColorChanged(color);
        }
    	setAppBackgroundType(CUSTOM_BACKGROUND_COLOR);
    	setAppBackgroundColorValue(color);
		getContext().sendBroadcast(new Intent(ACTION_APP_BACKGROUND_COLOR_CHANGED));
		Toast.makeText(getContext(), getContext().getResources().getString(R.string.set_all_app_view_background_success), 
				Toast.LENGTH_SHORT).show();
	}
	
	private void setAppBackgroundColorValue(int color) {
    	Editor editor = mSharedPreferences.edit();
        editor.putInt("appBackgroundColor", color);
        editor.commit();
	}
        
        private void setAppBackgroundType(int which) {
    	Editor editor = mSharedPreferences.edit();
        editor.putInt("appBackgroundType", which);
        editor.commit();
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt("old_color", mOldColor.getColor());
		state.putInt("new_color", mNewColor.getColor());
		return state;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mOldColor.setColor(savedInstanceState.getInt("old_color"));
		mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
	}
}
