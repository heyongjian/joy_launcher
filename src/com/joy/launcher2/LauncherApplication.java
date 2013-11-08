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

import java.lang.ref.WeakReference;

import com.joy.launcher2.R;
import com.joy.launcher2.preference.PreferencesProvider;

import android.app.Application;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;

import com.joy.launcher2.cache.BitmapCache;
import com.joy.launcher2.network.impl.Service;
import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.push.PushUtils;
import com.joy.launcher2.util.SystemInfo;
import com.joy.launcher2.util.SystemInfo.PushListenner;
public class LauncherApplication extends Application {
	public static Service mService;
	public static Context mContext;
	public static BitmapCache mBcache ;
	public static SystemInfo mSystemInfo;
	
    public LauncherModel mModel;
    public IconCache mIconCache;
    //add by huangming for launcher crash.
    private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    //end
    private static boolean sIsScreenLarge;
    private static float sScreenDensity;
    private static int sLongPressTimeout = 300;
    private static final String sSharedPreferencesKey = "com.joy.launcher2.prefs";
    WeakReference<LauncherProvider> mLauncherProvider;
    
    
    public final static int THEME_DEFAULT = 0;
    public final static int THEME_IOS = 1;
    public final static int THEME_SAMSUNG = 2;
    public final static int THEME_CUSTOM = 3;
    public final static int THEME_MI = 4;
    public static int sTheme = THEME_DEFAULT;
    public static boolean sIsRealIos = false;
    
    @Override
    public void onCreate() {
        super.onCreate();

        // set sIsScreenXLarge and sScreenDensity *before* creating icon cache
        sIsScreenLarge = getResources().getBoolean(R.bool.is_large_screen);
        sScreenDensity = getResources().getDisplayMetrics().density;
     
        //add by huangming for launcher crash.
        mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(this);
        //end
        // Load all preferences  by yongjian.he on 2013-5-28
        PreferencesProvider.load(this);
        //add by huangming for theme
        sTheme = PreferencesProvider.getTheme();
        sIsRealIos = (sTheme == THEME_IOS);
        sTheme = sTheme >= THEME_CUSTOM ? THEME_IOS : sTheme;
        //END
        mIconCache = new IconCache(this);
        mModel = new LauncherModel(this, mIconCache);

        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
        registerReceiver(mModel, filter);
        
        // add by yongjian.he for theme change
//        filter = new IntentFilter();
//        filter.addAction("com.joy.theme.CHANGE");
//        registerReceiver(mModel, filter);
        //add end 
        
        // Register for changes to the favorites
        ContentResolver resolver = getContentResolver(); 
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
        initLauncher();
    }
    
    public static boolean isDefaultTheme()
    {
    	return sTheme == THEME_DEFAULT;
    }
    
    public void initLauncher(){
		mContext = this;
		mBcache = BitmapCache.getInstance();
		try {
			mService = Service.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mSystemInfo = SystemInfo.getInstance();
		
		SharedPreferences sp = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, 0);
		SystemInfo.channel = sp.getString("channel", "");
		mSystemInfo.setPushListenner(new PushListenner() {
			
			@Override
			public void onReceiveCompleted() {
				// TODO Auto-generated method stub
				 //add by huangming for push.
		        PushUtils.startPushBroacast(LauncherApplication.mContext, -1, PushUtils.PUSH_ACTION, PushUtils.PUSH_SETTINGS_TYPE);
		        //end
			}
			@Override
			public void onReceiveFailed() {
				// TODO Auto-generated method stub
//				mSystemInfo.initSystemInfo();
			
			}
		});
		
    }

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(mModel);

        ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
            mModel.resetLoadedState(false, true);
            mModel.startLoaderFromBackground();
        }
    };
    
    //add by huangming for launcher crash.
    WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
    	return mWidgetPreviewCacheDb;
    }
    //end

    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        return mModel;
    }

    IconCache getIconCache() {
        return mIconCache;
    }

    LauncherModel getModel() {
        return mModel;
    }

    void setLauncherProvider(LauncherProvider provider) {
        mLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    public LauncherProvider getLauncherProvider() {
        return mLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return sSharedPreferencesKey;
    }

    public static boolean isScreenLarge() {
        return sIsScreenLarge;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }

    public static int getLongPressTimeout() {
        return sLongPressTimeout;
    }
}
