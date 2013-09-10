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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * A preference type that allows a user to choose a time
 * @author Sergey Margaritov
 */
public class ColorPickerPreference extends Preference implements
		Preference.OnPreferenceClickListener, ColorPickerDialog.OnColorChangedListener {

	View mView;
	ColorPickerDialog mDialog;
	private int mValue = Color.BLACK;
	private float mDensity = 0;
	private boolean mAlphaSliderEnabled = false;
	//add by chenxiong at 2013-04-26

    private static final String ACTION_SET_PHOTO_TO_APP_BACKGROUND = "action_start_photo_piker";
    private static final int CURRENT_THEME_BACKGROUND = 0;
    private static final int CUSTOM_BACKGROUND = 1;
    private static final int CUSTOM_BACKGROUND_COLOR = 2;
    private SharedPreferences mSharedPreferences;
    private Context mContext = getContext();
	private Resources mResources = getContext().getResources();
    //add end

	public ColorPickerPreference(Context context) {
		super(context);
		init(context, null);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getColor(index, Color.BLACK);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		onColorChanged(restoreValue ? getPersistedInt(mValue) : mSharedPreferences.getInt("appBackgroundColor", (Integer) defaultValue));//(Integer) defaultValue);
	}

	private void init(Context context, AttributeSet attrs) {
		mDensity = mResources.getDisplayMetrics().density;

        mSharedPreferences = mContext.getSharedPreferences("app_background__info", 0);
		setOnPreferenceClickListener(this);
		if (attrs != null) {
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}

	private void setPreviewColor() {
		if (mView == null) return;
		ImageView iView = new ImageView(getContext());
        //delete the previewColor by xiong.chen at 2013-07-01
		iView.setVisibility(View.INVISIBLE);
		LinearLayout widgetFrameView = ((LinearLayout)mView.findViewById(android.R.id.widget_frame));
		if (widgetFrameView == null) return;
		widgetFrameView.setVisibility(View.VISIBLE);
		widgetFrameView.setPadding(
			widgetFrameView.getPaddingLeft(),
			widgetFrameView.getPaddingTop(),
			(int)(mDensity * 8),
			widgetFrameView.getPaddingBottom()
		);
		// remove already create preview image
		int count = widgetFrameView.getChildCount();
		if (count > 0) {
			widgetFrameView.removeViews(0, count);
		}
		widgetFrameView.addView(iView);
		widgetFrameView.setMinimumWidth(0);
		iView.setBackgroundDrawable(new AlphaPatternDrawable((int)(5 * mDensity)));
		iView.setImageBitmap(getPreviewBitmap());
	}

	private Bitmap getPreviewBitmap() {
		int d = (int) (mDensity * 31); //30dip
		int color = mValue;
		Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		int w = bm.getWidth();
		int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++) {
			for (int j = i; j < h; j++) {
				c = (i <= 1 || j <= 1 || i >= w-2 || j >= h-2) ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				if (i != j) {
					bm.setPixel(j, i, c);
				}
			}
		}

		return bm;
	}

	@Override
	public void onColorChanged(int color) {
		if (isPersistent()) {
			persistInt(color);
		}
		mValue = color;
		setPreviewColor();

		//add by chenxiong at 2013-04-27
		//setAppBackgroundColorValue(color);
		//end
		try {
			getOnPreferenceChangeListener().onPreferenceChange(this, color);
		} catch (NullPointerException e) {

		}
	}

	public boolean onPreferenceClick(Preference preference) {
		showDialog(null);
		return false;
	}
	
	
	protected void showDialog(Bundle state) {
		final Bundle currentState = state;
		int selected = mSharedPreferences.getInt("appBackgroundType", 0);
		new AlertDialog.Builder(mContext).setTitle(mResources.getString(R.string.all_app_view_background_color_title))
		.setSingleChoiceItems(R.array.app_background_order_entries, selected, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case CURRENT_THEME_BACKGROUND:
					setAppBackgroundType(which);
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.set_all_app_view_background_success), 
							Toast.LENGTH_SHORT).show();
					break;
                case CUSTOM_BACKGROUND:
                	mContext.sendBroadcast(new Intent(ACTION_SET_PHOTO_TO_APP_BACKGROUND));
					break;
                case CUSTOM_BACKGROUND_COLOR:
                	mDialog = new ColorPickerDialog(mContext, mSharedPreferences.getInt("appBackgroundColor", mValue));
            		mDialog.setOnColorChangedListener(ColorPickerPreference.this);
            		mDialog.setAlphaSliderVisible(true);
            		if (currentState != null) {
            			mDialog.onRestoreInstanceState(currentState);
            		}
            		mDialog.show();
                	break;

				default:
					break;
				}
				dialog.dismiss();
			}
			
		}).setNegativeButton( mResources.getString(R.string.all_app_view_background_cancle), null).show();
	}
	
	/**
	 * set the app background type
	 * @param which
	 */
	private void setAppBackgroundType(int which) {
    	Editor editor = mSharedPreferences.edit();
        editor.putInt("appBackgroundType", which);
        editor.commit();
	}
	
	
	/**
	 * Toggle Alpha Slider visibility (by default it's disabled)
	 * @param enable
	 */
	public void setAlphaSliderEnabled(boolean enable) {
		mAlphaSliderEnabled = enable;
	}

	/**
	 * For custom purposes. Not used by ColorPickerPreferrence
	 * @param color
	 * @author Unknown
	 */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

    	if (argb.startsWith("#")) {
    		argb = argb.replace("#", "");
    	}

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.dialogBundle = mDialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        showDialog(myState.dialogBundle);
    }

    private static class SavedState extends BaseSavedState {
        Bundle dialogBundle;
        
        public SavedState(Parcel source) {
            super(source);
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
        
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
