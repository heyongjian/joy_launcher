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
import android.content.SharedPreferences;

import com.joy.launcher.AppsCustomizePagedView;
import com.joy.launcher.LauncherApplication;
import com.joy.launcher.Workspace;
public final class PreferencesProvider {
    public static final String PREFERENCES_KEY = "com.joy.launcher_preferences"; 

    public static final String PREFERENCES_CHANGED = "preferences_changed";
    
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
    
    public static final String ICON_STYLE_KEY = "ui_homescreen_icon_style";
    public static final String ICON__TEXT_STYLE_KEY = "ui_homescreen_icon_text_style";
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

    //activate launcher add by wanghao
    public static class LauncherActivate{
    	
    	private final static String LAUNCHER_ACTIVATE = "launcher_activate";
    	private final static String LAUNCHER_ACTIVATE_KEY = "launcher_activate_key";
    	
    	public static boolean getLauncherIsActivate(Context context){
    		final SharedPreferences preferences = context.getSharedPreferences(LAUNCHER_ACTIVATE, 0);
        	return preferences.getBoolean(LAUNCHER_ACTIVATE_KEY, false);
    	}
    	public static void setLauncherIsActivate(Context context, boolean value){
    		final SharedPreferences preferences = context.getSharedPreferences(LAUNCHER_ACTIVATE, 0);
    		SharedPreferences.Editor editor = preferences.edit();
        	editor.putBoolean(LAUNCHER_ACTIVATE_KEY, value);
        	editor.commit();
    	}
    }
    public static class Interface {
        public static class Homescreen {
        	//add by huangming for desktop appearance
        	public static Size getIconSize(Context context, String def)
        	{
        		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        		return Size.valueOf(preferences.getString("ui_homescreen_icon_size", def));
        	}
        	
        	public static Size getIconTextSize(Context context, String def)
        	{
        		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        		return Size.valueOf(preferences.getString("ui_homescreen_icon_text_size", def));
        	}
        	
        	public static String getIconStyle(Context context, String def)
        	{
        		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        		return preferences.getString(ICON_STYLE_KEY, def);
        	}
        	
        	public static TextStyle getIconTextStyle(Context context, String def)
        	{
        		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        		return TextStyle.valueOf(preferences.getString(ICON__TEXT_STYLE_KEY, def));
        	}
        	//end
        	
            public static int getNumberHomescreens(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getInt("ui_homescreen_screens", 5);
            }
            public static int getDefaultHomescreen(Context context, int def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getInt("ui_homescreen_default_screen", def + 1) - 1;
            }
            public static int getCellCountX(Context context, int def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                String[] values = preferences.getString("ui_homescreen_grid", "0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY(Context context, int def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                String[] values = preferences.getString("ui_homescreen_grid", def + "|0").split("\\|");;
                try {
                    return Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getScreenPaddingVertical(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return (int)((float) preferences.getInt("ui_homescreen_screen_padding_vertical", 0) * 3.0f *
                        LauncherApplication.getScreenDensity());
            }
            public static int getScreenPaddingHorizontal(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return (int)((float) preferences.getInt("ui_homescreen_screen_padding_horizontal", 0) * 3.0f *
                        LauncherApplication.getScreenDensity());
            }
            public static boolean getShowSearchBar(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_homescreen_general_search", true);
            }
            public static boolean getResizeAnyWidget(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_homescreen_general_resize_any_widget", false);
            }
            public static boolean getHideIconLabels(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_homescreen_general_hide_icon_labels", false);
            }
            public static class Scrolling {
                public static boolean getScrollWallpaper(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_homescreen_scrolling_scroll_wallpaper", true);
                }
                public static Workspace.TransitionEffect getTransitionEffect(Context context, String def) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0); 
                    return Workspace.TransitionEffect.valueOf(
                            preferences.getString("ui_homescreen_scrolling_transition_effect", def));
                }
                public static boolean getFadeInAdjacentScreens(Context context, boolean def) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_homescreen_scrolling_fade_adjacent_screens", def);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_homescreen_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_homescreen_indicator_fade", true);
                }
                public static boolean getShowDockDivider(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_homescreen_indicator_background", true);
                }
            }
        }

        public static class Drawer {
            public static boolean getJoinWidgetsApps(Context context) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_drawer_widgets_join_apps", true);
            }
            public static class Scrolling {
                public static AppsCustomizePagedView.TransitionEffect getTransitionEffect(Context context, String def) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return AppsCustomizePagedView.TransitionEffect.valueOf(
                            preferences.getString("ui_drawer_scrolling_transition_effect", def));
                }
                public static boolean getFadeInAdjacentScreens(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_drawer_scrolling_fade_adjacent_screens", false);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator(Context context) {
                   final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_drawer_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator(Context context) {
                    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                    return preferences.getBoolean("ui_drawer_indicator_fade", true);
                }
            }
          //add by wanghao
        	public static int getDrawerTransparency(Context context,int defvalue) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getInt("ui_drawer_transparency", defvalue);
            }
        	 public static int getCellCountX(Context context, int def) {
                 final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                 String[] values = preferences.getString("ui_drawer_grid", "0|" + def).split("\\|");
                 try {
                     return Integer.parseInt(values[1]);
                 } catch (NumberFormatException e) {
                     return def;
                 }
             }
             public static int getCellCountY(Context context, int def) {
                 final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                 String[] values = preferences.getString("ui_drawer_grid", def + "|0").split("\\|");;
                 try {
                     return Integer.parseInt(values[0]);
                 } catch (NumberFormatException e) {
                     return def;
                 }
             }
        }

        public static class Dock {

        }

        public static class Icons {

        }

        public static class General {
            public static boolean getAutoRotate(Context context, boolean def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_general_orientation", def);
            }
            
            public static boolean getCycleScrollMode(Context context, boolean def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                return preferences.getBoolean("ui_general_screen_cycle", def);
            }
        }
    }

    public static class Application {

    }
}
