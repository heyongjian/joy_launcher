package com.joy.launcher2.util;

import static android.os.Environment.MEDIA_MOUNTED;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileUtil 
{
	
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
	private static final String CACHDIR = "ImgCach";
	private static final int MB = 1024*1024;
	
	public static File getCacheDirectory(Context context)
	{
		File appCacheDir = null;
		if (Environment.getExternalStorageState().equals(MEDIA_MOUNTED) && hasExternalStoragePermission(context)) {
			appCacheDir = getExternalCacheDir();
		}
		if (appCacheDir == null) {
			appCacheDir = context.getCacheDir();
		}
		if (appCacheDir == null) {
			appCacheDir = context.getCacheDir(); // retry
		}
		//Log.e("file ", "" + appCacheDir);
		return appCacheDir;
	}
	
	private static File getExternalCacheDir() {
		File appCacheDir = new File(Environment.getExternalStorageDirectory(), CACHDIR);
		if (!appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {				
				return null;
			}
		}
		return appCacheDir;
	}
    
    private static boolean hasExternalStoragePermission(Context context) {
		int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
		return perm == PackageManager.PERMISSION_GRANTED;
	}
	
    
    public static int calculateRemainingSpace(Context context)
    {
    	if(Environment.getExternalStorageState().equals(MEDIA_MOUNTED) && hasExternalStoragePermission(context))
    	{
    		File dirSD = Environment.getExternalStorageDirectory();
    		if(dirSD.exists())
    		{
    			String pathSD = dirSD.getPath();
    			if(pathSD != null)
    			{
    				StatFs stat = new StatFs(pathSD);
    		        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
    		        Log.i("SD available size", (int) sdFreeMB + "M");
    		        return (int) sdFreeMB;
    			}
    		}
    	}
    	int availableMemory = Runtime.getRuntime().availableProcessors();
    	Log.i("Memory available size", availableMemory + "M");
    	return availableMemory;
    }
    
    public static boolean isSDAvailable()
    {
    	boolean available = false;
    	if(Environment.getExternalStorageState().equals(MEDIA_MOUNTED))
    	{
    		available = true;
    	}
    	return available;
    }
    
    /*public static String getFiles(String dir)
    {
    	
    }*/
    

}
