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

package com.joy.launcher2.preference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.joy.launcher2.AppsCustomizePagedView;
import com.joy.launcher2.LauncherModel;
import com.joy.launcher2.R;
import com.joy.launcher2.Workspace;
import com.joy.launcher2.util.Util;

public final class PreferencesProvider {
    public static final String PREFERENCES_KEY = "com.joy.launcher2_preferences";

    public static final String PREFERENCES_CHANGED = "preferences_changed";

    private static Map<String, ?> sKeyValues;
    
    //add by huangming for main menu show or hide.
    private final static String APPS_HIDE_PREFERENCES = "apps_hide_preferences";
    public static boolean getAppIsHide(Context context, String key)
    {
    	final SharedPreferences preferences = context.getSharedPreferences(APPS_HIDE_PREFERENCES, 0);
    	return preferences.getBoolean(key, false);
    }
    
    public final static void putAppHide(Context context, String key, boolean value)
    {
    	final SharedPreferences preferences = context.getSharedPreferences(APPS_HIDE_PREFERENCES, 0);
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean(key, value);
    	editor.commit();
    }
    //end
    
    public enum Size {
    	Large,
    	Medium,
    	Small
    }
    
    public enum TextStyle {
    	Marquee,
    	Ellipsis,
    	TwoLines
    }
    
    
    //add by huangming for backup and recover function.
  	public static final String PREFERENCES_BACKUP = "joy_laucher_backup_preferences";
  	public static final String BACKUP_KEY = "preferences_bakcup";
  	public static final String RECOVER_KEY = "preferences_recover";
  	private static boolean sClearDataOrFirst = true;
  	private static final Object sLock = new Object();
  	private static final String TAG = "PreferencesProvider";
  	private static final boolean DEBUG = true;
  	//end
    
    public static final String ICON_STYLE_KEY = "ui_homescreen_icon_style";
    public static final String ICON__TEXT_STYLE_KEY = "ui_homescreen_icon_text_style";

    
    //add by huangming for backup and recover function.
    private static void initPreferencesFile(Context context)
    {
    	synchronized (sLock)
    	{
    		if(sClearDataOrFirst)
        	{
        		//judge whether clear data or first by preferences exists.
    			if(DEBUG)Log.e(TAG, "init preferences file step 1:judge whether clear data or first ");
    			final SharedPreferences sp = context.getSharedPreferences(PREFERENCES_KEY, 0);
    		    Map<String, ?> map = sp.getAll();
    		    if(map != null && map.size() > 0)
    		    {
    		    	sClearDataOrFirst = false;
    		    }
    		    else
    		    {
    		    	if(DEBUG)Log.e(TAG, "init preferences file step 2:copy assets backup file to data/data sp.");
    		    	AssetManager am = context.getAssets();
        			InputStream is = null;
        			FileOutputStream fos = null;
        			File backupFile = context.getSharedPrefsFile(PREFERENCES_BACKUP);
        			try {
    					is = am.open(PREFERENCES_BACKUP + ".xml");
    					File parent = backupFile.getParentFile();
    					if(!parent.exists())
    					{
    						parent.mkdirs();
    					}
    					if(!backupFile.exists())
    					{
    						backupFile.createNewFile();
    						fos = new FileOutputStream(backupFile);
        					int length = -1;
                            byte[] buf = new byte[1024];
                            while ((length = is.read(buf)) != -1)
                            {
                              fos.write(buf, 0, length);
                            }
                            fos.flush();
    					}
    					sClearDataOrFirst = false;
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} finally {
    					if(sClearDataOrFirst)
            			{
            				if(backupFile != null && backupFile.exists())
            				{
            					backupFile.delete();
            				}
            			}
    					try {
    						if(is != null)is.close();
    						if(fos != null)fos.close();
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    					
    				}
        			if(DEBUG)Log.e(TAG, "init preferences file step 3:copy data/data backup file content to config file.");
        			if(!sClearDataOrFirst)
        			{
        				Resources res = context.getResources();
            			String[] keys = res.getStringArray(R.array.backup_keys);
                    	String[] attrs = res.getStringArray(R.array.backup_keys_attrs);
                    	recoverData(context, keys, attrs);
        			}
        			sClearDataOrFirst = false;
    		    }
        	}
    	}
    	
    }
    
    public static final String PREFERENCES_DESKTOP_BAKCUP_KEY = "preferences_desktop_bakcup_key";
    public static final String PREFERENCES_DESKTOP_IS_RECOVER_KEY = "preferences_desktop_is_recover_key";
    public static boolean setRecoverMode(Context context)
    {
    	boolean success = false;
    	Resources res = context.getResources();
    	if(res != null)
    	{
    		String[] keys = null;
        	String[] attrs = null;
        	boolean isOrdinaryUser = res.getBoolean(R.bool.config_ordinary_user);
        	if(isOrdinaryUser)
        	{
        		keys = res.getStringArray(R.array.backup_ordinary_keys);
            	attrs = res.getStringArray(R.array.backup_ordinary_keys_attrs);
        	}
        	else
        	{
        		keys = res.getStringArray(R.array.backup_keys);
            	attrs = res.getStringArray(R.array.backup_keys_attrs);
        	}
        	
        	File backupFile = context.getSharedPrefsFile(PREFERENCES_BACKUP);
			
        	File sdBackupFile = Util.getSdBackupFile(PREFERENCES_BACKUP + ".xml");
        	if(backupFile != null && backupFile.exists() && sdBackupFile != null && sdBackupFile.exists())
        	{
        		if(Util.copyFile(sdBackupFile, backupFile))
        		{
        			success = recoverData(context, keys, attrs);
        		}
        		
        	}
    	}
    	return success;
    	
    }
    
    public static boolean setBackupMode(Context context)
    {
    	boolean success = false;
    	Resources res = context.getResources();
    	if(res != null)
    	{
    		String[] keys = null;
        	String[] attrs = null;
        	boolean isOrdinaryUser = res.getBoolean(R.bool.config_ordinary_user);
        	if(isOrdinaryUser)
        	{
        		keys = res.getStringArray(R.array.backup_ordinary_keys);
            	attrs = res.getStringArray(R.array.backup_ordinary_keys_attrs);
        	}
        	else
        	{
        		keys = res.getStringArray(R.array.backup_keys);
            	attrs = res.getStringArray(R.array.backup_keys_attrs);
        	}
        	if(DEBUG)Log.e(TAG, "back up step 1:copy config file content to data/data sp.");
        	boolean isNeedBackup = backupData(context, keys, attrs);
        	
        	File backupFile = context.getSharedPrefsFile(PREFERENCES_BACKUP);
        	File sdBackupFile = Util.getSdBackupFile(PREFERENCES_BACKUP + ".xml");
        	if(backupFile != null && backupFile.exists() && sdBackupFile != null && sdBackupFile.exists() && isNeedBackup)
        	{
        		if(DEBUG)Log.e(TAG, "back up step 2:copy data/data file  to sdcard.");
        		success = Util.copyFile(backupFile, sdBackupFile);
        	}
    	}
    	return success;
    }
    
    public static boolean backupData(Context context, String[] keys, String[] attrs)
    {
    	if(keys == null || attrs == null)return false;
    	if(keys.length == 0 || attrs.length == 0)return false;
    	if(keys.length != attrs.length)return false;
    	final SharedPreferences sp = context.getSharedPreferences(PREFERENCES_KEY, 0);
    	final SharedPreferences backupSp = context.getSharedPreferences(PREFERENCES_BACKUP, 0);
    	SharedPreferences.Editor editor = backupSp.edit();
    	for(int i = 0; i < keys.length; i++)
    	{
    		String key = keys[i];
    		String attr = attrs[i];
    		if(sp.contains(key))
    		{
    			if("string".equals(attr))
    			{
    				String value = sp.getString(key, "");
    				if(!value.equals(""))
    				{
    					editor.putString(key, value);
    				}
    			}
    			else if("int".equals(attr))
    			{
    				int value = sp.getInt(key, Integer.MIN_VALUE);
    				if(value != Integer.MIN_VALUE)
    				{
    					editor.putInt(key, value);
    				}
    			}
    			else if("boolean".equals(attr))
    			{
    				boolean value = sp.getBoolean(key, false);
    				editor.putBoolean(key, value);
    			}
    		}
    	}
    	//backup desktop info
    	String desktopinfo = LauncherModel.getDataBase(context);
    	editor.putString(PREFERENCES_DESKTOP_BAKCUP_KEY, desktopinfo);
    	return editor.commit();
    }
    
    public static boolean recoverData(Context context, String[] keys, String[] attrs)
    {
    	if(keys == null || attrs == null)return false;
    	if(keys.length == 0 || attrs.length == 0)return false;
    	if(keys.length != attrs.length)return false;
    	final SharedPreferences sp = context.getSharedPreferences(PREFERENCES_KEY, 0);
    	final SharedPreferences backupSp = context.getSharedPreferences(PREFERENCES_BACKUP, 0);
    	SharedPreferences.Editor editor = sp.edit();
    	for(int i = 0; i < keys.length; i++)
    	{
    		String key = keys[i];
    		String attr = attrs[i];
    		if(backupSp.contains(key))
    		{
    			if("string".equals(attr))
    			{
    				String value = backupSp.getString(key, "");
    				if(!value.equals(""))
    				{
    					editor.putString(key, value);
    				}
    			}
    			else if("int".equals(attr))
    			{
    				int value = backupSp.getInt(key, Integer.MIN_VALUE);
    				if(value != Integer.MIN_VALUE)
    				{
    					editor.putInt(key, value);
    				}
    			}
    			else if("boolean".equals(attr))
    			{
    				boolean value = backupSp.getBoolean(key, false);
    				editor.putBoolean(key, value);
    			}
    		}
    	}
    	//recover desktop info
    	String desktopinfo = backupSp.getString(PREFERENCES_DESKTOP_BAKCUP_KEY, "");
    	if (!desktopinfo.equals("")) {
    		editor.putString(PREFERENCES_DESKTOP_BAKCUP_KEY, desktopinfo);
    		editor.putBoolean(PREFERENCES_DESKTOP_IS_RECOVER_KEY, true);
		}
    	return editor.commit();
    }
    //end
    
    public static void load(Context context) {
    	initPreferencesFile(context);
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        sKeyValues = preferences.getAll();
    }

    private static int getInt(String key, int def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Integer ?
                (Integer) sKeyValues.get(key) : def;
    }

    private static boolean getBoolean(String key, boolean def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Boolean ?
                (Boolean) sKeyValues.get(key) : def;
    }

    private static String getString(String key, String def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof String ?
                (String) sKeyValues.get(key) : def;
    }

    public static class Interface {
        public static class Homescreen {
        	
        	//add by huangming for desktop appearance
        	public static Size getIconSize(Context context, String def)
        	{
        		return Size.valueOf(getString("ui_homescreen_icon_size", def));
        	}
        	
        	public static Size getIconTextSize(Context context, String def)
        	{
        		return Size.valueOf(getString("ui_homescreen_icon_text_size", def));
        	}
        	
        	public static String getIconStyle(Context context, String def)
        	{
        		return getString(ICON_STYLE_KEY, def);
        	}
        	
        	public static TextStyle getIconTextStyle(Context context, String def)
        	{
        		return TextStyle.valueOf(getString(ICON__TEXT_STYLE_KEY, def));
        	}
        	
        	//end
        	
            public static int getNumberHomescreens() {
                return getInt("ui_homescreen_screens", 5);
            }
            public static int getDefaultHomescreen(int def) {
                return getInt("ui_homescreen_default_screen", def + 1) - 1;
            }
            public static int getCellCountX(int def) {
                String[] values = getString("ui_homescreen_grid", "0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY(int def) {
                String[] values = getString("ui_homescreen_grid", def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getStretchScreens() {
                return getBoolean("ui_homescreen_stretch_screens", false);
            }
            public static boolean getShowSearchBar() {
                return getBoolean("ui_homescreen_general_search", true);
            }
            public static boolean getHideIconLabels() {
                return getBoolean("ui_homescreen_general_hide_icon_labels", false);
            }
            public static class Scrolling {
                public static Workspace.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return Workspace.TransitionEffect.valueOf(
                                getString("ui_homescreen_scrolling_transition_effect", def));
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    try {
                        return Workspace.TransitionEffect.valueOf(def);
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    return Workspace.TransitionEffect.Standard;
                }
                public static boolean getScrollWallpaper() {
                    return getBoolean("ui_homescreen_scrolling_scroll_wallpaper", false);
                }
                public static boolean getWallpaperHack(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_wallpaper_hack", def);
                }
                public static int getWallpaperSize() {
                    return getInt("ui_homescreen_scrolling_wallpaper_size", 2);
                }
                public static boolean getFadeInAdjacentScreens(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_fade_adjacent_screens", def);
                }
                public static boolean getShowOutlines(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_show_outlines", def);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("ui_homescreen_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("ui_homescreen_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("ui_homescreen_indicator_position", "0"));
                }
            }
        }

        public static class Drawer {
            public static boolean getVertical() {
                return getString("ui_drawer_orientation", "horizontal").equals("vertical");
            }
            public static boolean getJoinWidgetsApps() {
                return getBoolean("ui_drawer_widgets_join_apps", true);
            }
            public static String getHiddenApps() {
                return getString("ui_drawer_hidden_apps", "");
            }
            public static class Scrolling {
                public static AppsCustomizePagedView.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return AppsCustomizePagedView.TransitionEffect.valueOf(
                                getString("ui_drawer_scrolling_transition_effect", def));
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    try {
                        return AppsCustomizePagedView.TransitionEffect.valueOf(def);
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    return AppsCustomizePagedView.TransitionEffect.Standard;
                }
                public static boolean getFadeInAdjacentScreens() {
                    return getBoolean("ui_drawer_scrolling_fade_adjacent_screens", false);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("ui_drawer_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("ui_drawer_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("ui_drawer_indicator_position", "0"));
                }
            }
            
            //add by wanghao
        	 public static int getCellCountX(Context context, int def) {
        		 initPreferencesFile(context);
                 String[] values = getString("ui_drawer_grid", "0|" + def).split("\\|");
                 try {
                     return Integer.parseInt(values[1]);
                 } catch (NumberFormatException e) {
                     return def;
                 }
             }
             public static int getCellCountY(Context context, int def) {
            	 initPreferencesFile(context);
                 String[] values = getString("ui_drawer_grid", def + "|0").split("\\|");;
                 try {
                     return Integer.parseInt(values[0]);
                 } catch (NumberFormatException e) {
                     return def;
                 }
             }
        }

        public static class Dock {
            public static boolean getShowDock() {
                return getBoolean("ui_dock_enabled", true);
            }
            public static int getNumberPages() {
                return getInt("ui_dock_pages", 1);
            }
            public static int getDefaultPage(int def) {
                return getInt("ui_dock_default_page", def + 1) - 1;
            }
            public static int getNumberIcons(int def) {
                return getInt("ui_dock_icons", def);
            }
            public static int getIconScale(int def) {
                return getInt("ui_dock_icon_scale", def);
            }
            public static boolean getShowDivider() {
                return getBoolean("ui_dock_divider", true);
            }
        }

        public static class Icons {

        }

        public static class General {
            public static boolean getAutoRotate(boolean def) {
                return getBoolean("ui_general_orientation", def);
            }
            public static boolean getFullscreenMode() {
                return getBoolean("ui_general_fullscreen", false);
            }
            public static boolean getAppBackground(boolean def) {
                return getBoolean("ui_general_background", def);
            }
            //add by xiong.chen for bug wxy-432 at 2013-07-08
            public static boolean getCycleScrollMode() {
                return getBoolean("ui_general_cycle_scroll", true);
            }
        }
    }

    public static class Application {

    }
}
