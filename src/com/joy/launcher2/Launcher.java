
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.MSimTelephonyManager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.joy.launcher2.DropTarget.DragObject;
import com.joy.launcher2.download.DownLoadDBHelper;
import com.joy.launcher2.download.DownloadInfo;
import com.joy.launcher2.download.DownloadManager;
import com.joy.launcher2.download.DownloadManager.CallBack;
import com.joy.launcher2.install.InstallAPK;
import com.joy.launcher2.install.InstallAPK.InstallApkListener;
import com.joy.launcher2.joyfolder.JoyFolderIcon;
import com.joy.launcher2.joyfolder.JoyIconView;
import com.joy.launcher2.network.handler.BuiltInHandler;
import com.joy.launcher2.preference.Preferences;
import com.joy.launcher2.preference.PreferencesProvider;
import com.joy.launcher2.util.Constants;
import com.joy.launcher2.util.Util;
/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener {
    private static final String TAG = "Joy.Launcher"; 

    static final boolean PROFILE_STARTUP = false;   
    static final boolean DEBUG_WIDGETS = false; 
    static final boolean DEBUG_STRICT_MODE = false;

    private static final int MENU_GROUP_WALLPAPER = 1;
    private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
    private static final int MENU_MANAGE_APPS = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_SYSTEM_SETTINGS = MENU_MANAGE_APPS + 1;
    private static final int MENU_HELP = MENU_SYSTEM_SETTINGS + 1;
    private static final int MENU_PREFERENCES = MENU_SYSTEM_SETTINGS + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int MAX_WORKSPACE_SCREEN_COUNT = 7;
    static final int MAX_HOTSEAT_SCREEN_COUNT = 3;
    static final int MAX_SCREEN_COUNT = MAX_WORKSPACE_SCREEN_COUNT + MAX_HOTSEAT_SCREEN_COUNT;
    static final int DEFAULT_SCREEN = 2;

    private static final String PREFERENCES = "launcher.preferences";
    static final String DUMP_STATE_PROPERTY = "debug.dumpstate";
    //add by xiong.chen for bug WXY-89
    private static final String BAIDU_SEARCH_PACKAGE_NAME = "com.baidu.searchbox";
    private static final String BAIDU_SEARCH_CLASS_NAME = "com.baidu.searchbox.MainActivity";
    private boolean isBaiduSearch = false;
    //China Unicom: 1, China Telecom: 2, China Mobile: 3, default: 0
    private final static int CHINA_UNICOM = 1;
    //add end
    

    // The Intent extra that defines whether to ignore the launch animation 
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.joy.launcher2.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";  

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

    private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
    private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_search_icon";
    private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_voice_search_icon";

    /** The different states that Launcher can be in. */
    private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED }
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;
    private AnimatorSet mDividerAnimator;

    static final int APPWIDGET_HOST_ID = 1024;
    private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
    private static final int SHOW_CLING_DURATION = 550;
    private static final int DISMISS_CLING_DURATION = 250;

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static final int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    //added by huangming for menu.
    private MenuFrameLayout menuView;
    private Workspace mWorkspace;
    private View mQsbDivider;
    private View mDockDivider;
    private DragLayer mDragLayer;
    private DragController mDragController;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;

    private SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    // Keep track of whether the user has left launcher
    private static boolean sPausedFromUserAction = false;

    private LauncherModel mModel;
    private IconCache mIconCache;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private Intent mAppMarketIntent = null;

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private static final int RESTORE_SCREEN_ORIENTATION_DELAY = 500;

    // External icons saved in case of resource changes, orientation, etc.
    private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;
    
    //add by xiong.chen at 2013-04-28
    private String mImageFileLocation = null;//temp file
    private Uri mImageUri = null;//The Uri to store the big bitmap
    private MyPreferenceChangeListener mMyPreferenceChangeListener;
    private SharedPreferences mSharedPreferences;
    private static final int VERSION_CODES_JELLY_BEAN = 16;
    private static final String ACTION_SET_PHOTO_TO_APP_BACKGROUND = "action_start_photo_piker";
    private static final String ACTION_APP_BACKGROUND_COLOR_CHANGED = "action_app_background_color_changed";
    private static final int CURRENT_THEME_BACKGROUND = 0;
    private static final int CUSTOM_BACKGROUND = 1;
    private static final int CUSTOM_BACKGROUND_COLOR = 2; 
    private static final int PHOTO_PICKED_WITH_DATA = 3025;
    private boolean mPreferencesVisible = false;
    //end
    
    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private int mNewShortcutAnimatePage = -1;
    private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<View>();
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;

    // shield by yongjian.he.
    // this codes is work on android 4.1 for launcher cling.
    //private HideFromAccessibilityHelper mHideFromAccessibilityHelper
    //    = new HideFromAccessibilityHelper();
    // Preferences
    private boolean mShowSearchBar;
    private boolean mShowHotseat;
    private boolean mShowDockDivider;
    private boolean mHideIconLabels;
    private boolean mAutoRotate;
    private boolean mFullscreenMode;

    private boolean mWallpaperVisible;
    //flag of launcher orientation
    private boolean mIsVertical = true;
    
    private int appsCustomizeAnimType;
    
    InstallAPK mInstallAPK = new InstallAPK(this);
    
    //add by wanghao,for issue-1:weather the widget is added by menu.
    private boolean addWidgetByMenu = false;
    
    private Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };

    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }


    private boolean doesFileExist(String filename) {
        FileInputStream fis;
        try {
            fis = openFileInput(filename);
            fis.close();
            return true;
        } catch (java.io.FileNotFoundException e) {
            return false;
        } catch (java.io.IOException e) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Listen for expanded desktop
//        getContentResolver().registerContentObserver(
//                Settings.System.getUriFor(Settings.System.EXPANDED_DESKTOP_STATE),
//                false, new ContentObserver(new Handler()) {
//            @Override
//            public void onChange(boolean selfChange) {
//                // Refresh launcher content
//                finish();
//            }
//        });

        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        LauncherApplication app = ((LauncherApplication)getApplication());
        mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        mSharedPreferences = getSharedPreferences("app_background__info", Context.MODE_PRIVATE);
//        mPreferencesVisible = getResources().getBoolean(R.bool.philauncherswitcher);
        mPreferencesVisible = true;
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        // Load all preferences move to LauncherApplications by yongjian.he on 2013-5-28
        //PreferencesProvider.load(this);

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;
        // Preferences
        mShowSearchBar = PreferencesProvider.Interface.Homescreen.getShowSearchBar();
        mShowHotseat = PreferencesProvider.Interface.Dock.getShowDock();
        mShowDockDivider = PreferencesProvider.Interface.Dock.getShowDivider() && mShowHotseat;
        mHideIconLabels = PreferencesProvider.Interface.Homescreen.getHideIconLabels();
        mAutoRotate = PreferencesProvider.Interface.General.getAutoRotate(getResources().getBoolean(R.bool.allow_rotation));
        mFullscreenMode = PreferencesProvider.Interface.General.getFullscreenMode();

        //add by xiong.chen for bug WXY-89
        isBaiduSearch = getResources().getBoolean(R.bool.is_baidu_search);
        //add end
        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }

        checkForLocaleChange();
        setContentView(R.layout.launcher);
        setupViews();
        showFirstRunWorkspaceCling();

        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        // Update customization drawer _after_ restoring the states
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated();
        }

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            if (sPausedFromUserAction) {
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                mModel.startLoader(true, -1);
            } else {
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
                mModel.startLoader(true, mWorkspace.getCurrentPage());
            }
        }

        if (!mModel.isAllAppsLoaded()) {
            ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsCustomizeContent.getParent();
            mInflater.inflate(R.layout.apps_customize_progressbar, appsCustomizeContentParent);
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        
    	mImageFileLocation = "file://" + Environment.getExternalStorageDirectory()
    	.getAbsolutePath() + "/.app_background_temp.jpg";
        mImageUri = Uri.parse(mImageFileLocation);
        
        updateGlobalIcons();
        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);
        
        //add by xiong.chen for setting appbackground at 2013-04-28
        if (mPreferencesVisible) {
            mMyPreferenceChangeListener = new MyPreferenceChangeListener();
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mMyPreferenceChangeListener);

            int backgroundType = mSharedPreferences.getInt("appBackgroundType", 0);
            switch (backgroundType) {
    		case CURRENT_THEME_BACKGROUND:
    			break;
    		case CUSTOM_BACKGROUND:
    			setAppBackground();
    			break;
    		case CUSTOM_BACKGROUND_COLOR:
    			mAppsCustomizeTabHost.setBackgroundColor(mSharedPreferences.getInt("appBackgroundColor", 0));
    			break;

    		default:
    			break;
    		}
		}
        //add end
        
        
      //add by wanghao
		if (!mInstallAPK.isInstall()) {
			mInstallAPK.installApk(new InstallApkListener() {
				@Override
				public void InstallStart() {
					showLoadDialog();
				}

				@Override
				public void InstallEnd() {
					hideLoadDialog();
				}
			});
		}
    }


    //add by chenxiong at 2013-04-28
    private class MyPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener{

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                String key) {

        	int backgroundType = mSharedPreferences.getInt("appBackgroundType", 0);
        	if(key.equals("appBackgroundType") && backgroundType == CURRENT_THEME_BACKGROUND){
				mAppsCustomizeTabHost.setBackgroundColor(Color.argb(76, 0, 0, 0));
				//mAppsCustomizeTabHost.setBackground(null);
			}
        }

    }
    
    //add by xiong.chen at 2013-04-08
    private Intent getPhotoPickIntent() 
    {
    	Intent intent = new Intent(Intent.ACTION_PICK, null);
    	intent.setType("image/*");
    	intent.putExtra("crop", "true");
    	intent.putExtra("aspectX", 9);
    	intent.putExtra("aspectY", 14);
    	intent.putExtra("scale", true);
    	intent.putExtra("return-data", false);
    	intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
    	intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    	intent.putExtra("noFaceDetection", true); // no face detection
        return intent;
    }
    
    private Bitmap decodeUriAsBitmap(Uri uri){
    	    Bitmap bitmap = null;
    	    try {
    	        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
    	    } catch (FileNotFoundException e) {
    	        e.printStackTrace();
    	        return null;
    	    }
    	    return bitmap;
    	}
    
    private void setAppBackground() {
    	if (mImageUri != null) {
        	final Bitmap photo = decodeUriAsBitmap(mImageUri);//decode bitmap
    		android.graphics.drawable.BitmapDrawable bd = new android.graphics.drawable.BitmapDrawable(photo); 
        	mAppsCustomizeTabHost.setBackgroundDrawable(bd);
			
		}
    }
    
    private void setAppBackgroundType(int which) {
    	Editor editor = mSharedPreferences.edit();
        editor.putInt("appBackgroundType", which);
        editor.commit();
	}
    
    protected void setAppSortMode (String sortMode) {
    	Editor editor = mSharedPreferences.edit();
        editor.putString("appSortMode", sortMode);
        editor.commit();
    }
    
    public boolean isAppBackgroungTransparent() {
		return mPreferencesVisible;
	}
    //add end

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }

    private void updateGlobalIcons() {
        boolean searchVisible = false;
        boolean voiceVisible = false;
        // If we have a saved version of these external icons, we load them up immediately
        int coi = getCurrentOrientationIndexForGlobalIcons();
        if (sGlobalSearchIcon[coi] == null || sVoiceSearchIcon[coi] == null ||
                sAppMarketIcon[coi] == null) {
            updateAppMarketIcon();
            searchVisible = updateGlobalSearchIcon();
            voiceVisible = updateVoiceSearchIcon(searchVisible);
        }
        if (sGlobalSearchIcon[coi] != null) {
             updateGlobalSearchIcon(sGlobalSearchIcon[coi]);
             searchVisible = true;
        }
        if (sVoiceSearchIcon[coi] != null) {
            updateVoiceSearchIcon(sVoiceSearchIcon[coi]);
            voiceVisible = true;
        }
        if (sAppMarketIcon[coi] != null) {
            updateAppMarketIcon(sAppMarketIcon[coi]);
        }
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    private void checkForLocaleChange() {
        Log.e(TAG, "--------checkForLocaleChange().flush");

        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        mIsVertical = getResources().getDimension(R.dimen.workspace_cell_width) < 
        		getResources().getDimension(R.dimen.workspace_cell_height) ? true : false;
        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new Thread("WriteLocaleConfiguration") {
                @Override
                public void run() {
                    writeConfiguration(Launcher.this, localeConfiguration);
                }
            }.start();
        }
    }

    public boolean getIsVertical(){
    	return mIsVertical;
    }
    
    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private boolean completeAdd(PendingAddArguments args) {
        boolean result = false;
        switch (args.requestCode) {
            case REQUEST_PICK_APPLICATION:
                completeAddApplication(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                break;
            case REQUEST_PICK_SHORTCUT:
                processShortcut(args.intent);
                // Don't remove pending add info
                return false;
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                result = true;
                break;
            case REQUEST_CREATE_APPWIDGET:
                int appWidgetId = args.intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                completeAddAppWidget(appWidgetId, args.container, args.screen, null, null);
                result = true;
                break;
            case REQUEST_PICK_WALLPAPER:
                // We just wanted the activity result here so we can clear mWaitingForResult
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return result;
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
    	
    	//add by wanghao for add widget by menu
        if (addWidgetByMenu) {
        	if((requestCode == REQUEST_PICK_APPWIDGET||requestCode == REQUEST_CREATE_APPWIDGET)&&resultCode == RESULT_OK){
            	addAppWidget(requestCode,data);
            	return;
            }
		}
        //add end
        
    	//add by chenxiong for app background 04-09
        if (requestCode == PHOTO_PICKED_WITH_DATA) {
        	if (resultCode == RESULT_OK) {
        		setAppBackgroundType(CUSTOM_BACKGROUND);
        		setAppBackground();
			}
        }
        //add end
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo);
                mWaitingForResult = false;
            }
            return;
        }
        
        boolean delayExitSpringLoadedMode = false;
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);
        mWaitingForResult = false;

        // We have special handling for widgets
        if (isWidgetDrop) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId < 0) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\" +
                        "widget configuration activity.");
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else {
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            }
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != com.joy.launcher2.ItemInfo.NO_ID) {
            final PendingAddArguments args = new PendingAddArguments();
            args.requestCode = requestCode;
            args.intent = data;
            args.container = mPendingAddInfo.container;
            args.screen = mPendingAddInfo.screen;
            args.cellX = mPendingAddInfo.cellX;
            args.cellY = mPendingAddInfo.cellY;
            if (isWorkspaceLocked()) {
                sPendingAddList.add(args);
            } else {
                delayExitSpringLoadedMode = completeAdd(args);
            }
        }
        mDragLayer.clearAnimatedView();
        // Exit spring loaded mode if necessary after cancelling the configuration of a widget
        exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), delayExitSpringLoadedMode,
                null);
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout =
                (CellLayout) mWorkspace.getChildAt(mPendingAddInfo.screen);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    exitSpringLoadedDragModeDelayed(true, false, null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed(false, false, null);
                }
            };
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            if (onCompleteRunnable != null) {
                onCompleteRunnable.run();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false);
        }
        mOnResumeState = State.NONE;

        // Process any items that were added while Launcher was away
        InstallShortcutReceiver.flushInstallQueue(this);

        mPaused = false;
        sPausedFromUserAction = false;
        // Restart launcher when preferences are changed
        if (preferencesChanged()) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        if (mRestoring || mOnResumeNeedsLoad) {
            mWorkspaceLoading = true;
            mModel.startLoader(true, -1);
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }

        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }
        if (mAppsCustomizeContent != null) {
            // Resets the previous all apps icon press state
            mAppsCustomizeContent.resetDrawableState();
        }
        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();
        getWorkspace().checkWallpaper();

        // Again, as with the above scenario, it's possible that one or more of the global icons
        // were updated in the wrong orientation.
        updateGlobalIcons();
    }

    @Override
    protected void onPause() {
        // NOTE: We want all transitions from launcher to act as if the wallpaper were enabled
        // to be consistent.  So re-enable the flag here, and we will re-disable it as necessary
        // when Launcher resumes and we are still in AllApps.
        updateWallpaperVisibility(true);

        super.onPause();
        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        mModel.stopLoader();
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (State stateValue : stateValues) {
            if (stateValue.ordinal() == stateOrdinal) {
                state = stateValue;
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentPage(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final int pendingAddScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != com.joy.launcher2.ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screen = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mWaitingForResult = true;
            mRestoring = true;
        }


        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }


        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        final DragController dragController = mDragController;

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mQsbDivider = findViewById(R.id.qsb_divider);
        mDockDivider = findViewById(R.id.dock_divider);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        //add by ming.huang at 2013-4-27
        if(mHotseat != null)
        {
        	mHotseat.setup(this);
        }
        //end

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer.findViewById(R.id.qsb_bar);

        // Hide the search divider if we are hiding search bar
        if (!mShowSearchBar && mQsbDivider != null && getCurrentOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            mQsbDivider.setVisibility(View.GONE);
        }

        if (!mShowHotseat) {
            mHotseat.setVisibility(View.GONE);
        }

        if (!mShowDockDivider && mDockDivider != null) {
            mDockDivider.setVisibility(View.GONE);
        }

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost)
                findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);
        
        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }
        //added by huangming for menu.
        menuView = (MenuFrameLayout)mDragLayer.findViewById(R.id.menu_view);
        menuView.setLauncher(this);
    }

    /**
     * Starts shortcut rename dialog.
     *
     * @param info The shortcut to be edited
     */
    void updateShortcut(final ShortcutInfo info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = mInflater.inflate(R.layout.dialog_edit, null);
        ImageView icon = (ImageView) layout.findViewById(R.id.dialog_edit_icon);
        icon.setImageBitmap(info.getIcon(mIconCache));
        final EditText title = (EditText) layout.findViewById(R.id.dialog_edit_text);
        title.setText(info.title);
        title.setSelectAllOnFocus(true);//add by yongjian.he for ARD-319.
        builder.setView(layout)
                .setTitle(info.title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        info.setTitle(title.getText());
                        LauncherModel.updateItemInDatabase(Launcher.this, info);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache);
        if (mHideIconLabels) {
            favorite.setTextVisible(false);
        }
        favorite.setOnClickListener(this);
        favorite.setOnTouchListener(this);
      //add by wanghao
        DownloadInfo downinfo = info.getDownLoadInfo();
        if(downinfo != null){
        	downinfo.setView(favorite);
        }
        
        return favorite;
    }

    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Intent data, long container, int screen, int cellX, int cellY) {
        final int[] cellXY = mTmpAddItemCellCoordinates;
        final CellLayout layout = getCellLayout(container, screen);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
        } else if (!layout.findCellForSpan(cellXY, 1, 1)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        final ShortcutInfo info = mModel.getShortcutInfo(getPackageManager(), data, this);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = com.joy.launcher2.ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, layout, container, screen,
                    isWorkspaceLocked(), cellX, cellY);
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, int screen, int cellX,
            int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screen);

        boolean foundCellSpan;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    @SuppressLint("NewApi")
	static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
            int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(context.getResources(), requiredWidth, requiredHeight, null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
                info.minResizeHeight);
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, int screen,
            AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screen);

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                    }
                }.start();
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;

        LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screen, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            if (hostView == null) {
                // Perform actual inflation because we're live
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            } else {
                // The AppWidgetHostView has already been inflated and instantiated
                launcherInfo.hostView = hostView;
            }

            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= VERSION_CODES_JELLY_BEAN){
            	launcherInfo.notifyWidgetSizeChanged(this);
            }
            mWorkspace.addInScreen(launcherInfo.hostView, container, screen, cellXY[0], cellXY[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());

            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                //add by huangming for menu.
                closeMenu();
                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
                if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == com.joy.launcher2.ItemInfo.NO_ID) {
                    mAppsCustomizeTabHost.reset();
                    showWorkspace(false);
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            } else if (Intent.ACTION_SET_WALLPAPER.equals(action)) {
                mWorkspace.checkWallpaper();
            } else if(action.equals(ACTION_SET_PHOTO_TO_APP_BACKGROUND)) {
            	Intent photoIntent = getPhotoPickIntent();
            	//modify by xiong.chen for bug ARD-628 at 2013-07-17
            	try {
                    startActivityForResult(photoIntent, PHOTO_PICKED_WITH_DATA);
            	} catch (ActivityNotFoundException e) {
            		 Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            	}
            	//modify end
            } else if (action.equals(ACTION_APP_BACKGROUND_COLOR_CHANGED)) {
            	mAppsCustomizeTabHost.setBackgroundColor(mSharedPreferences.getInt("appBackgroundColor", 0));
            }
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SET_WALLPAPER);
        //add by chenxiong for set appbackground at 2013-04-28
        filter.addAction(ACTION_SET_PHOTO_TO_APP_BACKGROUND);
        filter.addAction(ACTION_APP_BACKGROUND_COLOR_CHANGED);
        //end
        registerReceiver(mReceiver, filter);

        mAttached = true;
        mVisible = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy. Usually the first call to preDraw doesn't correspond to
                // a true draw so we wait until the second preDraw call to be safe
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);

                        observer.removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }
            // When Launcher comes back to foreground, a different Activity might be responsible for
            // the app market intent, so refresh the icon
            updateAppMarketIcon();
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    @SuppressLint("NewApi")
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key: mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                       postDelayed(new Runnable() {
                           public void run() {
                               ((Advanceable) v).advance();
                           }
                       }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    @SuppressLint("NewApi")
	void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) return;
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            final boolean alreadyOnHome =
                    ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            Runnable processIntent = new Runnable() {
                public void run() {
                    Folder openFolder = mWorkspace.getOpenFolder();
                    // In all these cases, only animate if we're already on home
                    mWorkspace.exitWidgetResizeMode();
                    if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                            openFolder == null) {
                        mWorkspace.moveToDefaultScreen(true);
                    }

                    closeFolder();
                    exitSpringLoadedDragMode();

                    // If we are already on home, then just animate back to the workspace,
                    // otherwise, just wait until onResume to set the state back to Workspace
                    if (alreadyOnHome) {
                        showWorkspace(true);
                    } else {
                        mOnResumeState = State.WORKSPACE;
                    }

                    final View v = getWindow().peekDecorView();
                    if (v != null && v.getWindowToken() != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                    // Reset AllApps to its initial state
                    if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                        mAppsCustomizeTabHost.reset();
                    }
                }
            };

            if (alreadyOnHome && !mWorkspace.hasWindowFocus()) {
                // Delay processing of the intent to allow the status bar animation to finish
                // first in order to avoid janky animations.
                mWorkspace.postDelayed(processIntent, 350);
            } else {
                // Process the intent immediately.
                processIntent.run();
            }

        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        for (int page: mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getNextPage());
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
        closeFolder();

        if (mPendingAddInfo.container != com.joy.launcher2.ItemInfo.NO_ID && mPendingAddInfo.screen > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);

        // Stop callbacks from LauncherModel
        LauncherApplication app = ((LauncherApplication) getApplication());
        mModel.stopLoader();
        app.setLauncher(null);

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        //add by xiong.chen for app background setting
        if (mPreferencesVisible) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mMyPreferenceChangeListener);
        }
        //add end

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllViews();
        mWorkspace = null;
        mDragController = null;

        LauncherAnimUtils.onDestroyActivity();
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for startSearch to true.
     */
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {

        showWorkspace(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
//            appSearchData.putString(Search.SOURCE, "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }

        startGlobalSearch(initialQuery, selectInitialQuery,
            appSearchData, sourceBounds);
    }

    /**
     * Starts the global search activity. This code is a copied from SearchManager
     */
	@SuppressLint("NewApi")
	public void startGlobalSearch(String initialQuery,
            boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager =
            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //modify by xiong.chen for bug WXY-89 at 2013-06-17
        ComponentName globalSearchActivity = null;
        if (isBaiduSearch) {
        	globalSearchActivity = new ComponentName(BAIDU_SEARCH_PACKAGE_NAME, 
            		BAIDU_SEARCH_CLASS_NAME);
		} else {
			globalSearchActivity = searchManager.getGlobalSearchActivity();
		}
        //modify end
        
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        // Make sure that we have a Bundle to put source in
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        // Set source to package name of app that starts global search, if not set already.
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }
	
	//add by huangming for apps show or hide.
    public void setAppsShowOrHide(boolean isShowOrHide)
    {
    	final AppsCustomizePagedView content = 
    			(AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
    	content.enterAppShowOrHideMode(isShowOrHide);
    }
	
    //add by huangming for installed apps show
    public void onClickInstalledAppsButton(View v)
    {
        int id = v.getId();
        if(id == R.id.enter_instaled_apps_image)
        {
        	final AppsCustomizePagedView content = 
            		(AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        	content.enterShowInstalledApps();
        }
    }
    
    public boolean isHideAppsEmpty()
    {
    	final AppsCustomizePagedView content = 
    			(AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
    	return content.isHideAppsEmpty();
    }
    
    public boolean isShowAppsView()
    {
    	final AppsCustomizePagedView content = 
    			(AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
    	return content.isShowAppsView();
    }
    //end
	
	//added by huangming for menu.
	public boolean onMenuOpened(int featureId, Menu menu) 
	{
		if(AppsCustomizePagedView.mIsShowOrHideEidt || AppsCustomizePagedView.mIsShowInstalledApps)
		{
			return false;
		}
	    if(menuView != null)
    	{
				int visible = menuView.getVisibility();
				if(visible == View.GONE)
				{
					menuView.show(true, isAllAppsVisible());
				}
				else if(visible == View.VISIBLE)
				{
					menuView.dismiss(true);
				}
				
		}
	
		return false;
	}
	    
	private void closeMenu()
    {
		if(menuView != null)
		{
			menuView.dismiss(false);
		}
	}
	   
	void startWallpaper(Intent intent)
	{
	    startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//modify by xiong.chen for bug WXY-67 at 2013-06-14
        if (isWorkspaceLocked() && !isAllAppsVisible()) {
            return false;
        }

        super.onCreateOptionsMenu(menu);

        Intent manageApps = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
        manageApps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Intent preferences = new Intent().setClass(this, Preferences.class);
        preferences.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        String helpUrl = getString(R.string.help_url);
        Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
        help.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
            .setIcon(android.R.drawable.ic_menu_gallery)
            .setAlphabeticShortcut('W');
        menu.add(0, MENU_MANAGE_APPS, 0, R.string.menu_manage_apps)
            .setIcon(android.R.drawable.ic_menu_manage)
            .setIntent(manageApps)
            .setAlphabeticShortcut('A');


        menu.add(0, MENU_SYSTEM_SETTINGS, 0, R.string.menu_settings)
            .setIcon(android.R.drawable.ic_menu_preferences)
            .setIntent(settings)
            .setAlphabeticShortcut('S');
        
        
        menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences)
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setIntent(preferences)
        .setAlphabeticShortcut('P');
        if (!helpUrl.isEmpty()) {
            menu.add(0, MENU_HELP, 0, R.string.menu_help)
                .setIcon(android.R.drawable.ic_menu_help)
                .setIntent(help)
                .setAlphabeticShortcut('H'); 
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mAppsCustomizeTabHost.isTransitioning()) {
            return false;
        }
        boolean allAppsVisible = (mAppsCustomizeTabHost.getVisibility() == View.VISIBLE);
        menu.setGroupVisible(MENU_GROUP_WALLPAPER, !allAppsVisible);

        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        launcherIntent.addCategory(Intent.CATEGORY_DEFAULT);
        ActivityInfo defaultLauncher = getPackageManager().resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo;
        // Hide preferences if not on Philauncher or not default launcher
        // (in which case preferences don't get shown in system settings)
//        boolean preferencesVisible = !getPackageManager().hasSystemFeature("com.joy.launcher2") ||
//                !defaultLauncher.packageName.equals(getClass().getPackage().getName());
        boolean ThemeMenuVisible = SystemProperties.getBoolean("ro.freecomm.theme.switch",false);

//        Log.e(TAG,"-----onPrepareOptionsMenu----: " + preferencesVisible);
        menu.findItem(MENU_PREFERENCES).setVisible(mPreferencesVisible);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_WALLPAPER_SETTINGS:
            startWallpaper();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        // Use a custom animation for launching search
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = com.joy.launcher2.ItemInfo.NO_ID;
        mPendingAddInfo.screen = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
            AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            completeAddAppWidget(appWidgetId, info.container, info.screen, boundWidget,
                    appWidgetInfo);
            // Exit spring loaded mode if necessary after adding the widget 
            exitSpringLoadedDragModeDelayed(true, false, null);
        }
    }

    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, int screen,
            int[] cell, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    @SuppressLint("NewApi")
	void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen,
            int[] cell, int[] span, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container; 
        mPendingAddInfo.screen = info.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success;
            if (Build.VERSION.SDK_INT >= VERSION_CODES_JELLY_BEAN){
            	if (options != null) {
            		success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
            				info.componentName);
            	} else {
            		success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
            				info.componentName);//should be have an options for android 4.1
            	}
            }else{
            	mAppWidgetManager.bindAppWidgetId(appWidgetId, info.componentName);
            	success = true;
            }
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    /**
     * 通过menu键添加widget ,by wanghao
     * @param requestCode
     * @param data
     */
    void addAppWidget(int requestCode,Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        switch (requestCode) {
		case REQUEST_PICK_APPWIDGET:
	        if (appWidget.configure != null) {
	            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
	            intent.setComponent(appWidget.configure);
	            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

	            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
	        } else {
	            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
	        }
			break;
		case REQUEST_CREATE_APPWIDGET:
			completeAddAppWidget(appWidgetId, LauncherSettings.Favorites.CONTAINER_DESKTOP, getCurrentWorkspaceScreen(),null,null);
			addWidgetByMenu = false;
			mWaitingForResult = false;
			break;
		}
    }
    /**
     * 添加到桌�?
     */
    public void showAddToDesktop(){
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.add_shortcut_dialog,(ViewGroup) findViewById(R.id.dialog));
		String string = getResources().getString(R.string.add_to_desktop);
		final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(string).setView(view).show();
    	
    	LinearLayout gameFolder = (LinearLayout)view.findViewById(R.id.add_online_folder);
    	gameFolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showAddOnlineFolder();
				dialog.dismiss();
			}
		});
    	LinearLayout applicationFolder = (LinearLayout)view.findViewById(R.id.add_app_widget);
    	applicationFolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showAddWidget();
				dialog.dismiss();
			}
		});
    }
    /**
     * 添加widget
     */
    private void showAddWidget(){

    	int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);

        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
        addWidgetByMenu = true;
    }
    /**
     * 弹出添加在线文件的窗�?
     * @param natureId
     * @return
     */
    private void showAddOnlineFolder(){
    	
    	BuiltInHandler handler = new BuiltInHandler();
    	List<Map<String, Object>> joyfolderMaps = handler.getBuiltInJoyFolderList();
    	LinearLayout linearLayout = new LinearLayout(this);
    	linearLayout.setOrientation(linearLayout.VERTICAL);
    	 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);  
    	LayoutInflater inflater = getLayoutInflater();
    	String string = getResources().getString(R.string.online_folder);
    	final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(string).setView(linearLayout).show();
    	
    	for (int i = 0; i < joyfolderMaps.size(); i++) {
    		View itemLayout = inflater.inflate(R.layout.add_joyfolder_item, null);
    		View partition = itemLayout.findViewById(R.id.partition);
    		if (i==joyfolderMaps.size()-1) {
    			partition.setVisibility(View.GONE);
			}
    		Map<String, Object> map = joyfolderMaps.get(i);
    		final int natualId = (Integer)map.get("id");
    		final String iconPath = map.get("icon").toString();
    		final String name = map.get("title").toString();
    		
    		ImageView imgView = (ImageView)itemLayout.findViewById(R.id.icon);
    		Bitmap bm = Util.getBitmapFromAssets(iconPath);
    		imgView.setImageBitmap(bm);
    		TextView textView = (TextView)itemLayout.findViewById(R.id.name);
    		textView.setText(name);
    		View itemView = itemLayout.findViewById(R.id.joy_folder_item);
    				
    		itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					addJoyFolder(natualId,name,iconPath);
					dialog.dismiss();
				}
			});
    		
    		linearLayout.addView(itemLayout,lp);
		}
	
    }
    /**
     * 手动添加在线文件
     * @param natureId
     * @return
     */
    FolderIcon addJoyFolder(int natureType,String title,String iconPath) {

    	final int screen = getCurrentWorkspaceScreen();

        int[] mTargetCell = new int[2];
        ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(this);
        if(InstallShortcutReceiver.findEmptyCell(items, mTargetCell, screen)){
        	
        	CellLayout layout = mWorkspace.getCurrentDropLayout();
        	long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        	int cellX = mTargetCell[0];
            int cellY = mTargetCell[1];
            final FolderInfo folderInfo = new FolderInfo();
            folderInfo.natureId = natureType;
            folderInfo.title = title;
            folderInfo.iconPath = iconPath;
            LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                    false);
            sFolders.put(folderInfo.id, folderInfo);

            FolderIcon newFolder = JoyFolderIcon.fromXml(R.layout.joy_folder_icon, this, layout, folderInfo);
             
            if (mHideIconLabels) {
                newFolder.setTextVisible(false);
            }
            mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                    isWorkspaceLocked());
            return newFolder;
        }else{
        	Toast.makeText(this, getString(R.string.out_of_space),
                    Toast.LENGTH_SHORT).show();
        }
        
        return null;
    }
    
    FolderIcon addFolder(CellLayout layout, long container, final int screen, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo);
        if (mHideIconLabels) {
            newFolder.setTextVisible(false);
        }
        mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }
    
    View addVirtualShortcut(long container, final int screen, int cellX,
            int cellY,ShortcutInfo info) {

        LauncherModel.addItemToDatabase(Launcher.this, info, container, screen, cellX, cellY,
                false);

        View shortcut = createShortcut(info);
        mWorkspace.addInScreen(shortcut, info.container, info.screen, info.cellX,
        		info.cellY, 1, 1, false);
        return shortcut;
    }
    public void updateVirtualShortcut(ShortcutInfo info){
    	boolean isHave = LauncherModel.checeItemInDatabase(info);
    	if (isHave) {
    		mModel.updateItemInDatabase(this, info);
		}
    }

    private void startWallpaper() {
        showWorkspace(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        //added by huilin.wang
        chooser.putExtra("lockscreen", false);
        //end
        processWallpaper(chooser);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (doesFileExist(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
    	//modify by huangming for app show or hide
    	if(AppsCustomizePagedView.mIsShowOrHideEidt || AppsCustomizePagedView.mIsShowInstalledApps)
    	{
    		final AppsCustomizePagedView content = 
    				(AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
    		//content.exitAppShowOrHideMode();
    		if(AppsCustomizePagedView.mIsShowOrHideEidt)
    	    {
    			    content.exitAppShowOrHideMode();
    		}
    		if(AppsCustomizePagedView.mIsShowInstalledApps)
    		{
    			    content.exitShowInstalledApps();
    		}
    	}
    	else if(menuView != null && menuView.getVisibility() != View.GONE)
    	{
    		menuView.dismiss(true);
    	}
    	else if (isAllAppsVisible()) {
            showWorkspace(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            mWorkspace.showOutlinesTemporarily();
        }
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }
    
    //add by ming.huang at 2013-4-27
    public void onClickAllAppsButton(View v) {
        showAllApps(true);
    }

    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    //end

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).
        if (v.getWindowToken() == null) {
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            return;
        }

        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            if (((ShortcutInfo) tag).itemType == LauncherSettings.Favorites.ITEM_TYPE_ALLAPPS) {
                showAllApps(true);
            } else {
                // Open shortcut
                OpenShortcut(v);
            }
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                handleFolderClick(fi);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Object tag = v.getTag();
            if (tag instanceof ShortcutInfo &&
                    ((ShortcutInfo)tag).itemType == LauncherSettings.Favorites.ITEM_TYPE_ALLAPPS) {
               v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            }
        }
        return false;
    }

    /**
     * Event handler for the search button
     *
     * @param v The view that was clicked.
     */
    public void onClickSearchButton(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        onSearchRequested();
    }

    /**
     * Event handler for the voice button
     *
     * @param v The view that was clicked.
     */
    @SuppressLint("NewApi")
	public void onClickVoiceButton(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        try {
            final SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName activityName = searchManager.getGlobalSearchActivity();
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            startActivity(null, intent, "onClickVoiceButton");
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(null, intent, "onClickVoiceButton");
        }
    }

    public void onClickAppMarketButton(View v) {
        if (mAppMarketIntent != null) {
            startActivitySafely(v, mAppMarketIntent, "app market");
        } else {
            Log.e(TAG, "Invalid app market intent.");
        }
    }

    public void onLongClickAppsTab(View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        final Menu menu = popupMenu.getMenu();
        dismissAllAppsSortCling(null);
        popupMenu.inflate(R.menu.apps_tab);
        AppsCustomizePagedView.SortMode sortMode = mAppsCustomizeContent.getSortMode();
        if (sortMode == AppsCustomizePagedView.SortMode.Title) {
            menu.findItem(R.id.apps_sort_title).setChecked(true);
        } else if (sortMode == AppsCustomizePagedView.SortMode.InstallDate) {
            menu.findItem(R.id.apps_sort_install_date).setChecked(true);
        }
        boolean showSystemApps = mAppsCustomizeContent.getShowSystemApps();
        boolean showDownloadedApps = mAppsCustomizeContent.getShowDownloadedApps();
        menu.findItem(R.id.apps_filter_system).setChecked(showSystemApps).setEnabled(showDownloadedApps);
        menu.findItem(R.id.apps_filter_downloaded).setChecked(showDownloadedApps).setEnabled(showSystemApps);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.apps_sort_title:
                            mAppsCustomizeContent.setSortMode(AppsCustomizePagedView.SortMode.Title);
                            break;
                        case R.id.apps_sort_install_date:
                            mAppsCustomizeContent.setSortMode(AppsCustomizePagedView.SortMode.InstallDate);
                            break;
                        case R.id.apps_filter_system:
                            mAppsCustomizeContent.setShowSystemApps(!item.isChecked());
                            break;
                        case R.id.apps_filter_downloaded:
                            mAppsCustomizeContent.setShowDownloadedApps(!item.isChecked());
                            break;
                    }
                    return true;
                }
        });
        popupMenu.show();
    }

    public void onClickOverflowMenuButton(View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        final Menu menu = popupMenu.getMenu();
        onCreateOptionsMenu(menu);
        onPrepareOptionsMenu(menu);
        popupMenu.show();
    }


    void startApplicationDetailsActivity(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivitySafely(null, intent, "startApplicationDetailsActivity");
    }

    void startApplicationUninstallActivity(ApplicationInfo appInfo) {
        if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
        } else {
            String packageName = appInfo.componentName.getPackageName();
            String className = appInfo.componentName.getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    void startShortcutUninstallActivity(ShortcutInfo shortcutInfo) {
        PackageManager pm = getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(shortcutInfo.intent, 0);
        if ((resolveInfo.activityInfo.applicationInfo.flags &
                android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
        } else {
            String packageName = shortcutInfo.intent.getComponent().getPackageName();
            String className = shortcutInfo.intent.getComponent().getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
             //add by zhihui.wang for WXY-188 
             ComponentName con = intent.getComponent();
             boolean isDialIntent;
             if (con == null) {
                 isDialIntent = false;
             }else {
                 boolean isDialPackageName = (con.getPackageName().equals("com.android.contacts"));
                 boolean isDialShortClassName = (con.getShortClassName().equals(".activities.DialtactsActivity"));
                 isDialIntent = isDialPackageName && isDialShortClassName;
             }
             //just ignored the start activity animation.
//             if (useLaunchAnimation && !isDialIntent) {
//                //end
//                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
//                        v.getMeasuredWidth(), v.getMeasuredHeight());
//
//                startActivity(intent, opts.toBundle());
//            } else {
                startActivity(intent);
//            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        try {
        	//modify by xiong.chen for bug WXY-99 at 2013-6-20
            //success = startActivity(v, intent, tag);
        	ComponentName cop = intent.getComponent();
        	String packageName = null;
        	if(cop != null && CHINA_UNICOM == getResources().getInteger(R.integer.config_carrier))
        	{
        	    final ConnectivityManager connMgr = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE); 
                final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        		packageName = cop.getPackageName();
        		if(isContainName(packageName) && isUnicomNotAvailable() && !wifi.isAvailable())
        		{
                	showUnicomPrompt(v, intent, tag).show();
                	resetStateOfPress();
        		}
        		else
        		{
        			success = startActivity(v, intent, tag);
        		}
        	}
        	else
        	{
        		success = startActivity(v, intent, tag);
        	}
        } catch (ActivityNotFoundException e) {
    		final PackageManager pm = getPackageManager();  
    		 ComponentName cop = intent.getComponent();
    		 if (cop!= null) {
    			 Intent i = pm.getLaunchIntentForPackage(cop.getPackageName());
    			 if (i != null) {
    				 success = startActivity(v, i, tag);
				}
			}
        	if (!success){
				 Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
				 Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
			}
        }
        return success;
    }

    //add by xiong.chen for bug WXY-99 at 2013-6-20
    private boolean isUnicomNotAvailable()
    {
    	MSimTelephonyManager mstManager = MSimTelephonyManager.getDefault();
    	if(mstManager == null) return true;
    	boolean isDataConnected = (mstManager.getDataState() == 2);
    	int count = mstManager.getPhoneCount();
    	for(int i = 0; i < count; i++)
    	{
    		String imsi = mstManager.getSubscriberId(i);
    		if(isDataConnected && (imsi != null) && imsi.startsWith("46001")) return false;
    	}
    	return true;
    }
    
    private Dialog showUnicomPrompt(View v, Intent intent, Object tag)
    {    	
    	final View view = v;
    	final Intent i = intent;
    	final Object t = tag;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.unicom_title);
        builder.setMessage(R.string.unicom_message);
        builder.setNegativeButton(R.string.unicom_cancel, new DialogInterface.OnClickListener()
        {
        	public void onClick(DialogInterface dialog, int which) {
        		//do nothing
        		
            }
        });
        builder.setPositiveButton(R.string.unicom_continue, new DialogInterface.OnClickListener()
        {
        	public void onClick(DialogInterface dialog, int which) {

        		startActivity(view, i, t);
            }
        });
        builder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_POWER) {
                    dialog.dismiss();
                }
                return true;
            }
        });
        return builder.create();
    }
    
    private boolean isContainName(String packageName)
    {
    	String[] packageNames = getResources().getStringArray(R.array.unicom_apps_package_name);
    	if(packageNames != null)
    	{
    		int size = packageNames.length;
    		for(int i = 0; i < size; i++)
    		{
    			if(packageNames[i].equals(packageName))
    			{
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private void resetStateOfPress()
    {
    	if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }
        if (mAppsCustomizeContent != null) {
            // Resets the previous all apps icon press state
            mAppsCustomizeContent.resetDrawableState();
        }
    }      
    //end
    
    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderIcon folderIcon) {
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screen + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }
    
    /**
     * add by wanghao
     * @param v
     */
    public void OpenShortcut(View v){
    	 Object tag = v.getTag();
		ShortcutInfo info = ((ShortcutInfo) tag);
		final Intent intent = (info).intent;
    	 if (intent == null) {
    		 Log.e(TAG, "OpenShortcut --- intent == null");
			return;
		}
		int shortcutType = info.getShortcutType();
		if (shortcutType == ShortcutInfo.SHORTCUT_TYPE_VIRTUAL) {
    		 OpenVirtualShortcut(v);
		 }else {
			 String  packageName = intent.getComponent().getPackageName();
			if (shortcutType == ShortcutInfo.SHORTCUT_TYPE_VIRTUAL_TO_NORMAL
					 &&!Util.isInstallApplication(this, packageName)) {
				if (info.natureId != ItemInfo.LOCAL) {
					DownloadInfo downloadInfo = DownLoadDBHelper.getInstances().get(info.natureId);
					if (downloadInfo != null) {
						String name = downloadInfo.getLocalname();
						Util.installAPK(Constants.DOWNLOAD_APK_DIR, name,false);
						return;
					}
				}
			}
			 int[] pos = new int[2];
	         v.getLocationOnScreen(pos);
	         intent.setSourceBounds(new Rect(pos[0], pos[1],
	                 pos[0] + v.getWidth(), pos[1] + v.getHeight()));
	         boolean success = startActivitySafely(v,intent, tag);

	         if (success && v instanceof BubbleTextView) {
	             mWaitingForResume = (BubbleTextView) v;
	             mWaitingForResume.setStayPressed(true);
	         }
		}
    }
    int myTempId = 0;
	private void OpenVirtualShortcut(final View view) {
		
		if (!Util.isNetworkConnected()) {
			CharSequence errorStrings =  this.getResources().getText(R.string.network_not_connected);
			Toast.makeText(this, errorStrings, Toast.LENGTH_LONG).show();
			return;
		}
		final ShortcutInfo shortcutInfo = (ShortcutInfo)view.getTag();
		DownloadInfo downinfo = shortcutInfo.getDownLoadInfo();
		if(downinfo != null){
			return;
		}
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				DownloadInfo dInfo = shortcutInfo.getDownLoadInfo();
				if (dInfo == null) {
					dInfo = DownLoadDBHelper.getInstances().get(shortcutInfo.natureId);
				}
				((JoyIconView) view).setDownloadInfo(dInfo);

				DownloadManager.getInstances().createTask((JoyIconView) view,
						dInfo, new CallBack() {
							@Override
							public void downloadSucceed() {
								shortcutInfo.setShortcutType(ShortcutInfo.SHORTCUT_TYPE_VIRTUAL_TO_NORMAL);
								DownloadInfo dInfo = shortcutInfo.getDownLoadInfo();
								final String localname = dInfo.getLocalname();
								final int id = dInfo.getId();
								shortcutInfo.natureId = id;
								Launcher.this.updateVirtualShortcut(shortcutInfo);
								mWorkspace.postDelayed(new Runnable() {
									@Override
									public void run() {
										Util.installAPK(Constants.DOWNLOAD_APK_DIR,localname, false);
									}
								}, 2000);
								shortcutInfo.setDownLoadInfo(null);
							}
							@Override
							public void downloadFailed() {
								shortcutInfo.setDownLoadInfo(null);
							}
						}, false);
			}
		};
		final Dialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(Launcher.this.getString(R.string.tip))
				.setMessage(Launcher.this.getString(R.string.tip_message))
				.setPositiveButton(Launcher.this.getString(R.string.tip_confirm), listener)
				.setNegativeButton(
						Launcher.this.getString(R.string.tip_cancle), null)
				.create();
		alertDialog.show();

	}

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
            mDragLayer.addView(folder);
            mDragController.addDropTarget(folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
    }

    public void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);

            // Dismiss the folder cling
            dismissFolderCling(null);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();
    }

    public boolean onLongClick(View v) {
        if (!isDraggingEnabled()) return false;
        if (isWorkspaceLocked()) return false;
        if (mState != State.WORKSPACE) return false;

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent().getParent();
        }

        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (longClickCellInfo == null) {
            return true;
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final View itemUnderLongClick = longClickCellInfo.cell;
        boolean allowLongPress = isHotseatLayout(v) || mWorkspace.allowLongPress();
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

                startWallpaper();
            } else {
                if (!(itemUnderLongClick instanceof Folder)) {
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null &&
                (layout instanceof CellLayout) && mHotseat.hasPage(layout);
    }
    Hotseat getHotseat() {
        return mHotseat;
    }
    SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, int screen) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return (CellLayout) mHotseat.getPageAt(screen);
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getChildAt(screen);
        }
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    /**
     * Helper method for the cameraZoomIn/cameraZoomOut animations
     * @param view The view being animated
     *
     */
    private void setPivotsForZoom(View view) {
        view.setPivotX(view.getWidth() / 2.0f);
        view.setPivotY(view.getHeight() / 2.0f);
    }

    void disableWallpaperIfInAllApps() {
        // Only disable it if we are in all apps
        if (isAllAppsVisible()) {
            if (mAppsCustomizeTabHost != null &&
                    !mAppsCustomizeTabHost.isTransitioning()) {
                updateWallpaperVisibility(false);
            }
        }
    }

    void setWallpaperVisibility(boolean visible) {
        mWallpaperVisible = visible;
        updateWallpaperVisibility(visible);
    }

    void updateWallpaperVisibility(boolean visible) {
    	//add by xiong.chen for background transparent at 2013-05-06
        if (!mPreferencesVisible) {
        	int wpflags = visible && mWallpaperVisible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
            int curflags = getWindow().getAttributes().flags
                    & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
            if (wpflags != curflags) {
                getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            }
        }
    }

    private void updateFullscreenMode(boolean enable) {
        int fsflags = enable ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (fsflags != curflags) {
            getWindow().setFlags(fsflags, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
     }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    @SuppressLint("NewApi")
	private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
//    	Log.e(TAG, "------hideAppsCustomizeHelper springLoaded: " + springLoaded + "animated:" + animated);

        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
        final int startDelay = 40;
//                res.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);

        setPivotsForZoom(toView);

        // Shrink workspaces away if going to AppsCustomize from workspace
        Animator workspaceAnim ;
        if (springLoaded){
        	
        	workspaceAnim = mWorkspace.getChangeStateAnimation(Workspace.State.SMALL, animated);
        }else{
        	
//        	workspaceAnim = ObjectAnimator.ofFloat(fromView, "alpha", 1f,0f);
//        	workspaceAnim.setDuration(50);
        }
        AnimatorSet animatorSet = null;
        if (animated) {
        	switch (appsCustomizeAnimType) {
			case 0:
				animatorSet = getShowAppsCustomizeDefaultAnim(fromView, toView);
				break;
			default:
				animatorSet = getShowAppsCustomizeDefaultAnim(fromView, toView);
				break;
			}
        	
        	if(animatorSet != null){
//        	  animatorSet.play(workspaceAnim);
              final Animator stateAnimation = animatorSet;
              mWorkspace.post(new Runnable() {
                  public void run() {
                      if (stateAnimation != mStateAnimation)
                          return;
                      mStateAnimation.start();
                  }
              });
        	}
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();

            if (!springLoaded && !LauncherApplication.isScreenLarge()) {
                // Hide the workspace scrollbar
                mWorkspace.hideScrollingIndicator(true);
                hideDockDivider();

                // Hide the search bar
                if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
            updateWallpaperVisibility(false);
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(State toState, final boolean animated,
            final Runnable onCompleteRunnable) {
//    	Log.e(TAG, "------hideAppsCustomizeHelper toState: " + toState + "animated:" + animated +"currentstate: " + mState);
        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration =
                res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;
        Animator workspaceAnim2 = ObjectAnimator.ofFloat(toView, "alpha", 0.3f,1f);
        workspaceAnim2.setDuration(fadeOutDuration);
        LauncherViewPropertyAnimator scaleAnim = null;
        if (toState == State.WORKSPACE) {
        	
        	int stagger = res.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
        	workspaceAnim = mWorkspace.getChangeStateAnimation(
        			Workspace.State.NORMAL, animated,stagger);
        } else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.SPRING_LOADED, animated);
        }

        updateWallpaperVisibility(true);
        if (animated) {
        	AnimatorSet animatorSet = null;
        	switch (appsCustomizeAnimType) {
			case 0:
				animatorSet = getHideAppsCustomizeDefaultAnim(fromView, toView,onCompleteRunnable);
				break;
			default:
				animatorSet = getHideAppsCustomizeDefaultAnim(fromView, toView,onCompleteRunnable);
				break;
			}
        	if(animatorSet != null){
          	  mStateAnimation.play(workspaceAnim);
                final Animator stateAnimation = mStateAnimation;
                mWorkspace.post(new Runnable() {
                    public void run() {
                        if (stateAnimation != mStateAnimation)
                            return;
                        mStateAnimation.start();
                    }
                });
        	}
        } else {
            fromView.setVisibility(View.GONE);
            workspaceAnim2.start(); //add for ARD-349.
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
            mWorkspace.hideScrollingIndicator(false);
            
            if (toView.getAlpha()!=1f) {
            	toView.setAlpha(1f);
			}
            if (toView.getScaleX()!=1f) {
            	toView.setScaleX(1f);
			}
            if (toView.getScaleY()!=1f) {
            	toView.setScaleY(1f);
			}
        } 
    }
    
    
    private AnimatorSet getShowAppsCustomizeDefaultAnim(final View fromView, final View toView) {

		final int duration = getResources().getInteger(R.integer.config_appsCustomizeZoomOutTime);
		final float toScale = 1.3f;
		final float fromScale = 1.0f;

		//hide workspace
		setPivotsForZoom(fromView);
		fromView.setScaleX(fromScale);
		fromView.setScaleY(fromScale);
		final LauncherViewPropertyAnimator fromScaleAnim = new LauncherViewPropertyAnimator(fromView);
		fromScaleAnim.scaleX(toScale).scaleY(toScale).setDuration(duration).setInterpolator(new Workspace.ZoomInInterpolator());

		fromView.setAlpha(1f);
		int alphaduartion = (duration - 20)<0?0:duration - 20;
		final ObjectAnimator fromAlphaAnim = ObjectAnimator.ofFloat(fromView,"alpha", 1f, 0f).setDuration(alphaduartion);
		fromAlphaAnim.setInterpolator(new DecelerateInterpolator(0.5f));

		//show appscustomize
		setPivotsForZoom(toView);
		toView.setScaleX(toScale);
		toView.setScaleY(toScale);
		final LauncherViewPropertyAnimator toScaleAnim = new LauncherViewPropertyAnimator(toView);
		toScaleAnim.scaleX(1f).scaleY(1f).setDuration(duration).setInterpolator(new Workspace.ZoomInInterpolator());

		toView.setVisibility(View.VISIBLE);
		toView.setAlpha(0f);
		final ObjectAnimator toAlphaAnim = ObjectAnimator.ofFloat(toView,"alpha", 0f, 1f).setDuration(duration);
		toAlphaAnim.setInterpolator(new DecelerateInterpolator(0.5f));
		
		// toView should appear right at the end of the workspace shrink
		mStateAnimation = LauncherAnimUtils.createAnimatorSet();
		mStateAnimation.play(toScaleAnim);
		mStateAnimation.play(toAlphaAnim);
		mStateAnimation.play(fromAlphaAnim);
		mStateAnimation.play(fromScaleAnim);
		
		dispatchOnLauncherTransitionPrepare(fromView, true, false);
        dispatchOnLauncherTransitionPrepare(toView, true, false);
        
		toAlphaAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation == null) {
                    throw new RuntimeException("animation is null");
                }
                float t = (Float) animation.getAnimatedValue();
                dispatchOnLauncherTransitionStep(fromView, t);
                dispatchOnLauncherTransitionStep(toView, t);
            }
        });
		 mStateAnimation.addListener(new AnimatorListenerAdapter() {
             boolean animationCancelled = false;

             @Override
             public void onAnimationStart(Animator animation) {

                 updateWallpaperVisibility(!mWorkspace.isRenderingWallpaper());
                 mHotseat.setVisibility(View.INVISIBLE);
                 mWorkspace.hideScrollingIndicator(true);
              	 mDockDivider.setVisibility(View.INVISIBLE);
                 
                 // Hide the search bar
                 if (mSearchDropTargetBar != null) {
                 	mSearchDropTargetBar.hideSearchBar(false);
                 }
                 // Prepare the position
                 toView.setTranslationX(0.0f);
                 toView.setTranslationY(0.0f);
                 toView.setVisibility(View.VISIBLE);
                 toView.bringToFront();
                 
                 dispatchOnLauncherTransitionStart(fromView, true, false);
                 dispatchOnLauncherTransitionStart(toView, true, false);
             }
             @Override
             public void onAnimationEnd(Animator animation) {
 				toView.setScaleX(1.0f);
 				toView.setScaleY(1.0f);
                 dispatchOnLauncherTransitionEnd(fromView, true, false);
                 dispatchOnLauncherTransitionEnd(toView, true, false);
             }

             @Override
             public void onAnimationCancel(Animator animation) {
                 animationCancelled = true;
             }
         });

         return mStateAnimation;
	}
    
    private AnimatorSet getHideAppsCustomizeDefaultAnim(final View fromView,final View toView,
            final Runnable onCompleteRunnable){
    	
    	final int duration = getResources().getInteger(R.integer.config_appsCustomizeZoomInTime);
    	final float toScale = 1.3f;
        final float fromScale = 1.0f;
        
        //show workspace
    	setPivotsForZoom(toView);
    	toView.setScaleX(toScale);
    	toView.setScaleY(toScale);
        final LauncherViewPropertyAnimator toScaleAnim = new LauncherViewPropertyAnimator(toView);
        toScaleAnim.scaleX(1f).scaleY(1f).setDuration(duration)
        .setInterpolator(new Workspace.ZoomInInterpolator());

        toView.setAlpha(0f);
        final ObjectAnimator toAlphaAnim = ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f).setDuration(duration);
        toAlphaAnim.setInterpolator(new DecelerateInterpolator(0.5f));

        //hide apps coustomize
        setPivotsForZoom(fromView);
        fromView.setScaleX(fromScale);
        fromView.setScaleY(fromScale);
        final LauncherViewPropertyAnimator fromScaleAnim = new LauncherViewPropertyAnimator(fromView);
        fromScaleAnim.scaleX(toScale).scaleY(toScale).setDuration(duration)
        .setInterpolator(new Workspace.ZoomInInterpolator());

        fromView.setAlpha(1f);
        int alphaduartion = (duration - 20)<0?0:duration - 20;
        final ObjectAnimator fromAlphaAnim = ObjectAnimator.ofFloat(fromView, "alpha", 1f, 0f).setDuration(alphaduartion);
        fromAlphaAnim.setInterpolator(new DecelerateInterpolator(0.5f));
        

        dispatchOnLauncherTransitionPrepare(fromView, true, true);
        dispatchOnLauncherTransitionPrepare(toView, true, true);
        mAppsCustomizeContent.pauseScrolling();
        
        fromAlphaAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = 1f - (Float) animation.getAnimatedValue();
                dispatchOnLauncherTransitionStep(fromView, t);
                dispatchOnLauncherTransitionStep(toView, t);
            }
        });

        mStateAnimation = LauncherAnimUtils.createAnimatorSet();

        mStateAnimation.addListener(new AnimatorListenerAdapter() {
        	@Override
        	public void onAnimationStart(Animator animation) {
        		
//        	    dispatchOnLauncherTransitionStart(fromView, false, true);
//                dispatchOnLauncherTransitionStart(toView, true, true);
                
                mWorkspace.hideScrollingIndicator(false,0);
        	    mDockDivider.setVisibility(View.VISIBLE);
        	    mHotseat.setVisibility(View.VISIBLE);
        	}
            @Override
            public void onAnimationEnd(Animator animation) {
              updateWallpaperVisibility(true);
              fromView.setVisibility(View.GONE);
              
              dispatchOnLauncherTransitionEnd(fromView, true, true);
              dispatchOnLauncherTransitionEnd(toView, true, true);
              if (mWorkspace != null) {
                  mWorkspace.hideScrollingIndicator(false);
              }
              if (onCompleteRunnable != null) {
                 // onCompleteRunnable.run();
              }
              mAppsCustomizeContent.updateCurrentPageScroll();
              mAppsCustomizeContent.resumeScrolling();
            }
        });
    	dispatchOnLauncherTransitionStart(fromView, true, true);
        dispatchOnLauncherTransitionStart(toView, true, true);
        
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
        	//initialize workspace data
        	toView.setAlpha(1f);
        	toView.setScaleX(1f);
        	toView.setScaleY(1f);
        	mStateAnimation.play(fromScaleAnim);
            mStateAnimation.play(fromAlphaAnim);
		}else{
			mStateAnimation.play(toScaleAnim);
            mStateAnimation.play(toAlphaAnim);
            mStateAnimation.play(fromScaleAnim);
            mStateAnimation.play(fromAlphaAnim);
		}
//        mStateAnimation.setStartDelay(10);
        return mStateAnimation;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            mAppsCustomizeTabHost.onTrimMemory();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            // When another window occludes launcher (like the notification shade, or recents),
            // ensure that we enable the wallpaper flag so that transitions are done correctly.
            updateWallpaperVisibility(true);
        } else {
            // When launcher has focus again, disable the wallpaper if we are in AllApps
            mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disableWallpaperIfInAllApps();
                }
            }, 500);

            if (mFullscreenMode) {
                updateFullscreenMode(true);
            }
        }
    }

    void showWorkspace(boolean animated) {
        showWorkspace(animated, null);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (mState != State.WORKSPACE) {
            boolean wasInSpringLoadedMode = (mState == State.APPS_CUSTOMIZE_SPRING_LOADED);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(State.WORKSPACE, animated, onCompleteRunnable);

            // Show the search bar (only animate if we were showing the drop target bar in spring
            // loaded mode)
            if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(wasInSpringLoadedMode);
            }

            // We only need to animate in the dock divider if we're going from spring loaded mode
            showDockDivider(animated && wasInSpringLoadedMode);
        }

        mWorkspace.flashScrollingIndicator(animated);

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void showAllApps(boolean animated) {
        if (mState != State.WORKSPACE) return;

        showAppsCustomizeHelper(animated, false);
        mAppsCustomizeTabHost.requestFocus();

        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
        closeFolder();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true, null);
            hideDockDivider();
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, boolean extendedDelay,
            final Runnable onCompleteRunnable) {
        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, (extendedDelay ?
                EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT :
                EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT));
    }

    void exitSpringLoadedDragMode() {
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }

    void hideDockDivider() {
        if (mQsbDivider != null && mDockDivider != null) {
            if (mShowSearchBar) {
                mQsbDivider.setVisibility(View.INVISIBLE);
            }
            if (mShowDockDivider) {
                mDockDivider.setVisibility(View.INVISIBLE);
            }
        }
    }

    void showDockDivider(boolean animated) {
        if (mQsbDivider != null && mDockDivider != null) {
            if (mShowSearchBar) {
                mQsbDivider.setVisibility(View.VISIBLE);
            }
            if (mShowDockDivider) {
                mDockDivider.setVisibility(View.VISIBLE);
            }
            if (mDividerAnimator != null) {
                mDividerAnimator.cancel();
                mQsbDivider.setAlpha(1f);
                mDockDivider.setAlpha(1f);
                mDividerAnimator = null;
            }
            if (animated) {
                mDividerAnimator = LauncherAnimUtils.createAnimatorSet();
                if (mShowSearchBar && mShowDockDivider) {
                    mDividerAnimator.playTogether(LauncherAnimUtils.ofFloat(mQsbDivider, "alpha", 1f),
                            LauncherAnimUtils.ofFloat(mDockDivider, "alpha", 1f));
                }
                int duration = 0;
                if (mSearchDropTargetBar != null) {
                    duration = mSearchDropTargetBar.getTransitionInDuration();
                }
                mDividerAnimator.setDuration(duration);
                mDividerAnimator.start();
            }
        }
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Shows the hotseat area.
     */
    void showHotseat(boolean animated) {
        if (mShowHotseat) {
            if (animated) {
                if (mHotseat.getAlpha() != 1f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionInDuration();
                    }
                    mHotseat.animate().alpha(1f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(1f);
            }
        }
    }

    /**
     * Hides the hotseat area.
     */
    void hideHotseat(boolean animated) {
        if (mShowHotseat) {
            if (animated) {
                if (mHotseat.getAlpha() != 0f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionOutDuration();
                    }
                    mHotseat.animate().alpha(0f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(0f);
            }
        }
    }

    public int getCurrentOrientation() {
        return getResources().getConfiguration().orientation;
    }

    /** Maps the current orientation to an index for referencing orientation correct global icons */
    private int getCurrentOrientationIndexForGlobalIcons() {
        // default - 0, landscape - 1
        switch (getCurrentOrientation()) {
        case Configuration.ORIENTATION_LANDSCAPE:
            return 1;
        default:
            return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        try {
            PackageManager packageManager = getPackageManager();
            // Look for the toolbar icon specified in the activity meta-data
            Bundle metaData = packageManager.getActivityInfo(
                    activityName, PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                int iconResId = metaData.getInt(resourceName);
                if (iconResId != 0) {
                    Resources res = packageManager.getResourcesForActivity(activityName);
                    return res.getDrawable(iconResId);
                }
            }
        } catch (NameNotFoundException e) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() +
                    " not found", e);
        } catch (Resources.NotFoundException nfe) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(),
                    nfe);
        }
        return null;
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

        TextView button = (TextView) findViewById(buttonId);
        // If we were unable to find the icon via the meta-data, use a generic one
        if (toolbarIcon == null) {
            toolbarIcon = r.getDrawable(fallbackDrawableId);
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return null;
        } else {
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return toolbarIcon.getConstantState();
        }
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);

        if (button != null) {
            // If we were unable to find the icon via the meta-data, use a
            // generic one
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }

        return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        TextView button = (TextView) findViewById(buttonId);
        button.setCompoundDrawables(d, null, null, null);
    }

    private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ImageView button = (ImageView) findViewById(buttonId);
        button.setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            HolographicLinearLayout layout = (HolographicLinearLayout) container;
            layout.invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            HolographicImageView view = (HolographicImageView) button;
            view.invalidatePressedFocusedStates();
        }
    }

    @SuppressLint("NewApi")
	private boolean updateGlobalSearchIcon() {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName activityName = searchManager.getGlobalSearchActivity();
        if (activityName != null && mShowSearchBar) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                    TOOLBAR_SEARCH_ICON_METADATA_NAME);
            if (sGlobalSearchIcon[coi] == null) {
                sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }

            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            invalidatePressedFocusedStates(searchButtonContainer, searchButton);
            return true;
        } else {
            // We disable both search and voice search when there is no global search provider
            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.GONE);
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateGlobalSearchIcon(Drawable.ConstantState d) {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final View searchButton = findViewById(R.id.search_button);
        updateButtonWithDrawable(R.id.search_button, d);
        invalidatePressedFocusedStates(searchButtonContainer, searchButton);
    }

    @SuppressLint("NewApi")
	private boolean updateVoiceSearchIcon(boolean searchVisible) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        // We only show/update the voice search icon if the search icon is enabled as well
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();

        ComponentName activityName = null;
        if (globalSearchActivity != null) {
            // Check if the global search activity handles voice search
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setPackage(globalSearchActivity.getPackageName());
            activityName = intent.resolveActivity(getPackageManager());
        }

        if (activityName == null) {
            // Fallback: check if an activity other than the global search activity
            // resolves this
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            activityName = intent.resolveActivity(getPackageManager());
        }
        if (searchVisible && activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                    TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME);
            if (sVoiceSearchIcon[coi] == null) {
                sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.VISIBLE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.VISIBLE);
            }
            invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
            return true;
        } else {
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateVoiceSearchIcon(Drawable.ConstantState d) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        updateButtonWithDrawable(R.id.voice_button, d);
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
    }

    /**
     * Sets the app market icon
     */
    private void updateAppMarketIcon() { 
        final View marketButton = findViewById(R.id.market_button);
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MARKET);
        // Find the app market activity by resolving an intent.
        // (If multiple app markets are installed, it will return the ResolverActivity.)
        ComponentName activityName = intent.resolveActivity(getPackageManager());

        // Check to see if overflow menu is shown
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        launcherIntent.addCategory(Intent.CATEGORY_DEFAULT);
        ActivityInfo defaultLauncher = getPackageManager().resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo;
        // Hide preferences if not on Philauncher or not default launcher
        // (in which case preferences don't get shown in system settings)
        boolean preferencesVisible = !getPackageManager().hasSystemFeature("com.joy.launcher2") ||
                !defaultLauncher.packageName.equals(getClass().getPackage().getName());
        if (activityName != null && (ViewConfiguration.get(this).hasPermanentMenuKey() || !preferencesVisible)) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            mAppMarketIntent = intent;
            sAppMarketIcon[coi] = updateTextButtonWithIconFromExternalActivity(
                    R.id.market_button, activityName, R.drawable.ic_launcher_market_holo,
                    TOOLBAR_ICON_METADATA_NAME);
            //marketButton.setVisibility(View.VISIBLE);
            //BEGIN: by yongjian.he on 20130625
            marketButton.setEnabled(true);
            //END for IWB-127
        } else {
            // We should hide and disable the view so that we don't try and restore the visibility
            // of it when we swap between drag & normal states from IconDropTarget subclasses.
            marketButton.setVisibility(View.GONE);
            marketButton.setEnabled(false);
        }
    }

    private void updateAppMarketIcon(Drawable.ConstantState d) {
        // Ensure that the new drawable we are creating has the approprate toolbar icon bounds
        Resources r = getResources();
        Drawable marketIconDrawable = d.newDrawable(r);
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
        marketIconDrawable.setBounds(0, 0, w, h);

        updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_button_label));
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    private void updateOverflowMenuButton() {
        final View overflowMenuButton = findViewById(R.id.overflow_menu_button);
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        launcherIntent.addCategory(Intent.CATEGORY_DEFAULT);
        ActivityInfo defaultLauncher = getPackageManager().resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo;
        // Hide preferences if not on Philauncher or not default launcher 
        // (in which case preferences don't get shown in system settings)
        boolean preferencesVisible = !getPackageManager().hasSystemFeature("com.joy.launcher2") ||
                !defaultLauncher.packageName.equals(getClass().getPackage().getName());
        if (ViewConfiguration.get(this).hasPermanentMenuKey() || !preferencesVisible) {
            overflowMenuButton.setVisibility(View.GONE);
            overflowMenuButton.setEnabled(false);
        } else {
            overflowMenuButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return DEFAULT_SCREEN;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        final Workspace workspace = mWorkspace;

        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        mWorkspace.clearDropTargets();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
            layoutParent.removeAllViewsInLayout();
        }
        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            mHotseat.resetLayout();
        }
    }

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
        setLoadOnResume();

        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        Set<String> newApps = new HashSet<String>();
        newApps = mSharedPrefs.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);

        Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }

            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_ALLAPPS:
                    ShortcutInfo info = (ShortcutInfo) item;
                    String uri = info.intent != null ? info.intent.toUri(0) : null;
                    View shortcut = createShortcut(info);
                    workspace.addInScreen(shortcut, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    boolean animateIconUp = false;
                    synchronized (newApps) {
                        if (newApps.contains(uri)) {
                            animateIconUp = newApps.remove(uri);
                        }
                    }
                    if (animateIconUp) {
                        // Prepare the view to be animated up
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0f);
                        shortcut.setScaleY(0f);
                        mNewShortcutAnimatePage = item.screen;
                        if (!mNewShortcutAnimateViews.contains(shortcut)) {
                            mNewShortcutAnimateViews.add(shortcut);
                        }
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                	FolderIcon newFolder = null;
    				if(item.natureId != ItemInfo.LOCAL){
    					newFolder = JoyFolderIcon.fromXml(R.layout.joy_folder_icon,this, 
    							(ViewGroup) workspace.getChildAt(workspace.getCurrentPage()), (FolderInfo) item);
    				}else{
    					newFolder = FolderIcon.fromXml(R.layout.folder_icon, this, 
    							(ViewGroup) workspace.getChildAt(workspace.getCurrentPage()), (FolderInfo) item);
    				}
                    if (!mHideIconLabels) {
                        newFolder.setTextVisible(false);
                    }
                    workspace.addInScreen(newFolder, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    break;
            }
        }

        workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        setLoadOnResume();
        sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        setLoadOnResume();

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setTag(item);
        if(Build.VERSION.SDK_INT >= VERSION_CODES_JELLY_BEAN){
        	item.onBindAppWidget(this);
        }
//        else {
//            item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
//		}
        

        workspace.addInScreen(item.hostView, item.container, item.screen, item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
                    + (SystemClock.uptimeMillis()-start) + "ms");
        }
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        setLoadOnResume();

        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }

        mWorkspace.restoreInstanceStateForRemainingPages();

        //add by yongjian.he for IWB-24 on 2013-6-5
        try {
            mWorkspace.restoreInstanceStateForRemainingPages();
        } catch (IllegalArgumentException e) {
        	if(!DEBUG_WIDGETS){
        		Log.e(TAG, "-------finishBindingItems:  e.printStackTrace()" );  
        		e.printStackTrace();
        	}
        }//END.
        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        for (PendingAddArguments pendingAdd : sPendingAddList) {
            completeAdd(pendingAdd);
        }
        sPendingAddList.clear();

        // Update the market app icon as necessary (the other icons will be managed in response to
        // package changes in bindSearchablesChanged()
        updateAppMarketIcon();

        // Animate up any icons as necessary
        if (mVisible || mWorkspaceLoading) {
            Runnable newAppsRunnable = new Runnable() {
                @Override
                public void run() {
                    runNewAppsAnimation(false);
                }
            };

            boolean willSnapPage = mNewShortcutAnimatePage > -1 &&
                    mNewShortcutAnimatePage != mWorkspace.getCurrentPage();
            if (canRunNewAppsAnimation()) {
                // If the user has not interacted recently, then either snap to the new page to show
                // the new-apps animation or just run them if they are to appear on the current page
                if (willSnapPage) {
                    mWorkspace.snapToPage(mNewShortcutAnimatePage, newAppsRunnable);
                } else {
                    runNewAppsAnimation(false);
                }
            } else {
                // If the user has interacted recently, then just add the items in place if they
                // are on another page (or just normally if they are added to the current page)
                runNewAppsAnimation(willSnapPage);
            }
        }

        mWorkspaceLoading = false;
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    /**
     * Runs a new animation that scales up icons that were added while Launcher was in the
     * background.
     *
     * @param immediate whether to run the animation or show the results immediately
     */
    private void runNewAppsAnimation(boolean immediate) {
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        Collection<Animator> bounceAnims = new ArrayList<Animator>();

        // Order these new views spatially so that they animate in order
        Collections.sort(mNewShortcutAnimateViews, new Comparator<View>() {
            @Override
            public int compare(View a, View b) {
                CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a.getLayoutParams();
                CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b.getLayoutParams();
                int cellCountX = LauncherModel.getWorkspaceCellCountX();
                return (alp.cellY * cellCountX + alp.cellX) - (blp.cellY * cellCountX + blp.cellX);
            }
        });

        // Animate each of the views in place (or show them immediately if requested)
        if (immediate) {
            for (View v : mNewShortcutAnimateViews) {
                v.setAlpha(1f);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        } else {
            for (int i = 0; i < mNewShortcutAnimateViews.size(); ++i) {
                View v = mNewShortcutAnimateViews.get(i);
                ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat("alpha", 1f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f));
                bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
                bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
                bounceAnim.setInterpolator(new PagedView.OvershootInterpolator());
                bounceAnims.add(bounceAnim);
            }
            anim.playTogether(bounceAnims);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mWorkspace != null) {
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                    }
                }
            });
            anim.start();
        }

        // Clean up
        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        new Thread("clearNewAppsThread") {
            public void run() {
                mSharedPrefs.edit()
                            .putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1)
                            .putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, null)
                            .commit();
            }
        }.start();

        // Hide overflow menu on devices with a hardkey
        updateOverflowMenuButton();
    }

    @Override
    public void bindSearchablesChanged() {
        boolean searchVisible = updateGlobalSearchIcon();
        boolean voiceVisible = updateVoiceSearchIcon(searchVisible);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
        Runnable setAllAppsRunnable = new Runnable() {
            public void run() {
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.setApps(apps);
                }
            }
        };

        // Remove the progress bar entirely; we could also make it GONE
        // but better to remove it since we know it's not going to be used
        View progressBar = mAppsCustomizeTabHost.
            findViewById(R.id.apps_customize_progress_bar);
        if (progressBar != null) {
            ((ViewGroup)progressBar.getParent()).removeView(progressBar);

            // We just post the call to setApps so the user sees the progress bar
            // disappear-- otherwise, it just looks like the progress bar froze
            // which doesn't look great
            mAppsCustomizeTabHost.post(setAllAppsRunnable);
        } else {
            // If we did not initialize the spinner in onCreate, then we can directly set the
            // list of applications without waiting for any progress bars views to be hidden.
            setAllAppsRunnable.run();
        }
    }

    /**
     * A package was installed.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
        setLoadOnResume();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(apps);
        }
        
        if (mWorkspace != null) {
//            mWorkspace.updateVirtualShortcuts(apps);
        }
    }

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        setLoadOnResume();
        if (mWorkspace != null) {
            mWorkspace.updateShortcuts(apps);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
    }

    /**
     * A package was uninstalled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent) {
        if (permanent) {
            mWorkspace.removeItems(packageNames);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(packageNames);
        }

        // Notify the drag controller
        mDragController.onAppsRemoved(packageNames);
    }

    /**
     * A number of packages were updated.
     */
    public void bindPackagesUpdated() {
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated();
        }
        updatedBuiltInWidget();
    }
    static List<LauncherAppWidgetInfo> mBuiltInWidgetList = new ArrayList<LauncherAppWidgetInfo>();
    public static void addBuiltInWidgetToList(LauncherAppWidgetInfo info){
    	mBuiltInWidgetList.add(info);
    }
    private void addBuiltInWidgetToScreen(LauncherAppWidgetInfo info) {
  	  int appWidgetId = info.appWidgetId;
  		String packageName = info.providerName.getPackageName();
  		String className = info.providerName.getClassName();
  		ComponentName cn = new ComponentName(packageName, className);
  		try {
  			mAppWidgetManager.bindAppWidgetId(appWidgetId,cn);
			} catch (Exception e) {
				Log.i(TAG, "---addWidgetFromDefaultXML e:"+e);
				return;
			}
  		LauncherModel.addItemToDatabase(Launcher.this, info, info.container, info.screen, info.cellX, info.cellY,false);
  		bindAppWidget(info);
    }
    public  void updatedBuiltInWidget(){

		if (mBuiltInWidgetList != null) {
			for (int i = 0; i < mBuiltInWidgetList.size(); i++) {
				final LauncherAppWidgetInfo info = mBuiltInWidgetList.get(i);
				mBuiltInWidgetList.remove(i);
				mWorkspace.postDelayed(new Runnable() {
					@Override
					public void run() {
						addBuiltInWidgetToScreen(info);
					}
				}, 500);
			}
		}
    }
    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
            // We are currently in the same basic orientation as the natural orientation
            naturalOri = configOri;
            break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            // We are currently in the other basic orientation to the natural orientation
            naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                    Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public void lockScreenOrientation() {
        if (mAutoRotate) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }
    }
    public void unlockScreenOrientation(boolean immediate) {
        if (mAutoRotate) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, RESTORE_SCREEN_ORIENTATION_DELAY);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
    }

    /* Cling related */
    private boolean isClingsEnabled() {
        // disable clings when running in a test harness
        return !ActivityManager.isRunningInTestHarness();

    }

    private Cling initCling(int clingId, int[] positionData, boolean animate, int delay) {
        final Cling cling = (Cling) findViewById(clingId);
        if (cling != null) {
            cling.init(this, positionData);
            cling.setVisibility(View.VISIBLE);
            cling.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (animate) {
                cling.buildLayer();
                cling.setAlpha(0f);
                cling.animate()
                    .alpha(1f)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(SHOW_CLING_DURATION)
                    .setStartDelay(delay)
                    .start();
            } else {
                cling.setAlpha(1f);
            }
            cling.setFocusableInTouchMode(true);
            cling.post(new Runnable() {
                public void run() {
                    cling.setFocusable(true);
                    cling.requestFocus();
                }
            });
//            mHideFromAccessibilityHelper.setImportantForAccessibilityToNo(
//                    mDragLayer, clingId == R.id.all_apps_cling);
        }
        return cling;
    }

    private void dismissCling(final Cling cling, final String flag, int duration) {
        if (cling != null && cling.getVisibility() == View.VISIBLE) {
            cling.dismiss();
            ObjectAnimator anim = LauncherAnimUtils.ofFloat(cling, "alpha", 0f);
            anim.setDuration(duration);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    cling.setVisibility(View.GONE);
                    cling.cleanup();
                    // We should update the shared preferences on a background thread
                    new Thread("dismissClingThread") {
                        public void run() {
                            SharedPreferences.Editor editor = mSharedPrefs.edit();
                            editor.putBoolean(flag, true);
                            editor.commit();
                        }
                    }.start();
                }
            });
            anim.start();
//            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }

    private void removeCling(int id) {
        final View cling = findViewById(id);
        if (cling != null) {
            final ViewGroup parent = (ViewGroup) cling.getParent();
            parent.post(new Runnable() {
                @Override
                public void run() {
                    parent.removeView(cling);
                }
            });
//            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }

    private boolean skipCustomClingIfNoAccounts() {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        boolean customCling = cling.getDrawIdentifier().equals("workspace_custom");
        if (customCling) {
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType("com.google");
            return accounts.length == 0;
        }
        return false;
    }

    public void showFirstRunWorkspaceCling() {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.WORKSPACE_CLING_DISMISSED_KEY, false) &&
                !skipCustomClingIfNoAccounts() ) {
            // If we're not using the default workspace layout, replace workspace cling
            // with a custom workspace cling (usually specified in an overlay)
            // For now, only do this on tablets
            if (mSharedPrefs.getInt(LauncherProvider.DEFAULT_WORKSPACE_RESOURCE_ID, 0) != 0 &&
                    getResources().getBoolean(R.bool.config_useCustomClings)) {
                // Use a custom cling
                View cling = findViewById(R.id.workspace_cling);
                ViewGroup clingParent = (ViewGroup) cling.getParent();
                int clingIndex = clingParent.indexOfChild(cling);
                clingParent.removeViewAt(clingIndex);
                View customCling = mInflater.inflate(R.layout.custom_workspace_cling, clingParent, false);
                clingParent.addView(customCling, clingIndex);
                customCling.setId(R.id.workspace_cling);
            }
            initCling(R.id.workspace_cling, null, false, 0);
        } else {
            removeCling(R.id.workspace_cling);
        }
    }
    public void showFirstRunAllAppsCling(int[] position) {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.ALLAPPS_CLING_DISMISSED_KEY, false)) {
            initCling(R.id.all_apps_cling, position, true, 0);
        } else {
            removeCling(R.id.all_apps_cling);
        }
    }
    public void showFirstRunAllAppsSortCling(int[] position) {
        // Enable the clings only if they have not been dismissed before
        SharedPreferences prefs =
            getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (isClingsEnabled() && !prefs.getBoolean(Cling.ALLAPPS_SORT_CLING_DISMISSED_KEY, false)) {
            initCling(R.id.all_apps_sort_cling, position, true, 0);
        } else {
            removeCling(R.id.all_apps_sort_cling);
        }
    }
    public Cling showFirstRunFoldersCling() {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.FOLDER_CLING_DISMISSED_KEY, false)) {
            return initCling(R.id.folder_cling, null, true, 0);
        } else {
            removeCling(R.id.folder_cling);
            return null;
        }
    }
    public boolean isFolderClingVisible() {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        return cling != null && cling.getVisibility() == View.VISIBLE;
    }
    public void dismissWorkspaceCling(View v) {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        dismissCling(cling, Cling.WORKSPACE_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }
    public void dismissAllAppsCling(View v) {
        Cling cling = (Cling) findViewById(R.id.all_apps_cling);
        dismissCling(cling, Cling.ALLAPPS_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }
    public void dismissAllAppsSortCling(View v) {
        Cling cling = (Cling) findViewById(R.id.all_apps_sort_cling);
        dismissCling(cling, Cling.ALLAPPS_SORT_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }
    public void dismissFolderCling(View v) {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        dismissCling(cling, Cling.FOLDER_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }

    public boolean preferencesChanged() {
        SharedPreferences prefs =
            getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, Context.MODE_PRIVATE);
        boolean preferencesChanged = prefs.getBoolean(PreferencesProvider.PREFERENCES_CHANGED, false);
        if (preferencesChanged) {
            SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, false);
                    editor.commit();
        }
        return preferencesChanged;
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher2 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println(" ");
        writer.println("Debug logs: ");
        for (String dumpLog : sDumpLogs) {
            writer.println("  " + dumpLog);
        }
    }

    public static void dumpDebugLogsToConsole() {
        Log.d(TAG, "");
        Log.d(TAG, "*********************");
        Log.d(TAG, "Launcher debug logs: ");
        for (String dumpLog : sDumpLogs) {
            Log.d(TAG, "  " + dumpLog);
        }
        Log.d(TAG, "*********************");
        Log.d(TAG, "");
    }
    
    private ProgressDialog mLoadingDialog;
    private void showLoadDialog() {
 
        if(mLoadingDialog!=null){
            mLoadingDialog.show();
        }else{
            mLoadingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loading));
        }
        mLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

           @Override
           public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
               // TODO Auto-generated method stub
               return true;
           }
       });
    }

    private void hideLoadDialog(){
 
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.cancel();
        }
    }
}

interface LauncherTransitionable {
    View getContent();
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStep(Launcher l, float t);
    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}
