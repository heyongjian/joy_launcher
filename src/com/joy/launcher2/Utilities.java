/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.joy.launcher2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import java.util.ArrayList;

import com.joy.launcher2.R;
import com.joy.launcher2.preference.PreferencesProvider;

import android.util.Log;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
final class Utilities {
    @SuppressWarnings("unused")
    private static final String TAG = "Joy.Utilities";

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;
    private static int sStandard = -1;

    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final int sIconBgTypeWhite = 0;
    private static final int sIconBgTypeYellow = 1;
    private static final int sIconBgTypeGreen = 2;
    private static final int sIconBgTypeBlue = 3;
    //add by xiong.chen for bug wxy-572 at 2013-07-16
    //if the replace_apk_icon_array was changed we should also modify sIconDrawableIds 
    private static final int[]  sIconDrawableIds = new int[]{R.drawable.com_geoai_duzhereader,
		};

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;
    private static ArrayList<String> mFilter = new ArrayList<String>();
    
    //add by yongjian.he for show appBackground switch.
    private static boolean mShowAppBackground = true ;
    		

    /**
     * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
     * icon bitmaps that are stored in the database (which were 74x74 pixels at hdpi size)
     * to the proper size (48dp)
     */
    static Bitmap createIconBitmap(Bitmap icon, Context context, String packageName) {
        int textureWidth = sIconTextureWidth;
        int textureHeight = sIconTextureHeight;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
            return createIconBitmap(new BitmapDrawable(icon), context, packageName);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
            return icon;
        } else {
            // Icon is too small, render to a larger bitmap
            return createIconBitmap(new BitmapDrawable(icon), context, packageName);
        }
    }

    //add by huangming for icon size
    static Bitmap createIconBitmap(Drawable icon, Context context)
    {
    	return createIconBitmap(icon, context, null);
    }
    //end
    
    /**
     * Returns a bitmap suitable for the all apps view.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context, String packageName) {

        synchronized (sCanvas) { // we share the statics :-(
            int bgType = 0;
            //modify by xiong.chen for bug wxy-572 at 2013-07-16
            Resources res = context.getResources();
            String[] apkIcon = res.getStringArray(R.array.replace_apk_icon_array); 
            if(packageName != null){
                bgType = stringToAsicll(packageName) % 3;
                int count = apkIcon.length;
                for (int i = 0; i < count; i++) {
                	if (packageName.equals(apkIcon[i])) {
                		icon = res.getDrawable(sIconDrawableIds[i]);
                		break;
                	}
                }
            }
            if (sIconWidth == -1) {
                initStatics(context);
            }
           
            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    // width = sourceWidth;
                    // height = sourceHeight;
                }
            }
            Drawable bground;
            mShowAppBackground = PreferencesProvider.Interface.General.getAppBackground(
            		res.getBoolean(R.bool.general_show_appbackground));
            sOldBounds.set(icon.getBounds());
//            bground = res.getDrawable(R.drawable.icon_background_white);
            switch(bgType){
                case sIconBgTypeWhite:
                    bground = res.getDrawable(R.drawable.icon_background_white);
                    break;
                case sIconBgTypeYellow:
                    bground = res.getDrawable(R.drawable.icon_background_black);
                    break;
                case sIconBgTypeGreen:
                    bground = res.getDrawable(R.drawable.icon_background_green);
                    break;
//                case sIconBgTypeBlue:
//                    bground = res.getDrawable(R.drawable.icon_background_blue);
//                    break;
                default:
                    bground = res.getDrawable(R.drawable.icon_background_green);
                    break;
            }
            bground.setBounds(0, 0, sIconWidth, sIconHeight);
            String[] systemIcon = context.getResources().getStringArray(R.array.system_icon_array);
            
            //modify by yongjian.he for appbackground switch 2013-5-28.
            if(!(mFilter.size() > 0) && packageName != null){
                for (int i = 0; i < systemIcon.length; i++) {
//                	Log.d("Utilities","----createIconBitmap----systemIcon[i]:"+systemIcon[i]);
                    mFilter.add(systemIcon[i]);
                }
            }

            if(mShowAppBackground && !(mFilter.contains(packageName)) && packageName != null){
            	Log.e("Utilities","----createIconBitmap----packageName:"+packageName + "width" + width);
                if(width <= sStandard){
                    width = height = (int)(sStandard * 0.75);
                }else if(width > sStandard || height > sStandard){
                    width = height = (int)(sStandard * 0.6);
                }
            }//END
            
            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            if(mShowAppBackground && !mFilter.contains(packageName) && packageName != null){
                bground.draw(canvas);
            }
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    private static int stringToAsicll(String packageName){
        int asicllSum = 0;
        char[] chars = packageName.toCharArray();
        for(int i = 0; i < chars.length; i++){
            asicllSum += (int)chars[i];
        }
        return asicllSum;
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                return createIconBitmap(new BitmapDrawable(bitmap), context, null);
            }
        }
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            canvas.setBitmap(null);

            return disabled;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;
        sStandard = sIconWidth;
        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorFocusedPaint.setColor(0xffff8e00);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }
}
