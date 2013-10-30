package com.joy.launcher2.push;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.joy.launcher2.LauncherApplication;
/**
 * 数据库相关操作的类
 * 
 * @author wanghao
 */
public class PushDownLoadDBHelper {
	// 数据库名
	private static final String DATABASE_NAME = "push_download.db";

	//数据表名
	private static final String DATABASE_TABLE = "push_download";

	//数据库版本
	private static final int DATABASE_VERSION = 1;

	//id 指定对于的apk
	private static final String ID = "id";

	//文件名
	private static final String NAME = "name";

	//本地名
	private static final String LOCAL_NAME = "local_name";
	
	//apk下载地址
	private static final String URL = "url";
	
	//文件大小
	private static final String FILE_SIZE = "file_size";

	//文件已下载大小
	private static final String COMPLETE_SIZE = "complete_size";
	
	private static final String TITLE = "title";
	
	private static final String DOWNLOAD_TYPE = "downloadType";
	
	private static final String APK_ICON = "apkIcon";
 
	private final Context context;
 
	private DatabaseHelper mDBHelper;
 
	private SQLiteDatabase db;
	
	static PushDownLoadDBHelper dbHelper;
	public PushDownLoadDBHelper(Context ctx) {
		context = ctx;
		mDBHelper = new DatabaseHelper(context);
	}

	static public PushDownLoadDBHelper getInstances() {
		
		if (dbHelper == null) {
			dbHelper = new PushDownLoadDBHelper(LauncherApplication.mContext);
		}
		return dbHelper;
	}
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

		 String DATABASE_CREATE = "create table "+DATABASE_TABLE+"(id INTEGER PRIMARY KEY, "
					+ "name TEXT, "
					+ "local_name TEXT, "
					+ "url TEXT, "
					+ "file_size INTEGER, "
					+ "complete_size INTEGER, "
					+ "title TEXT, "
					+ "downloadType INTEGER, "
					+ "apkIcon BLOB"
					+");";
		 
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	/**
	 * 打开数据库
	 * 
	 * @return
	 * @throws SQLException
	 */
	public synchronized SQLiteDatabase open() throws SQLException {
		
		db = mDBHelper.getWritableDatabase();

		return db;
	}

	/**
	 * 关闭数据库
	 */
	public synchronized void close() throws SQLException {
		mDBHelper.close();
	}

	/**
	 * 向数据库中插入数据
	 */
	public synchronized void insert(PushDownloadInfo info) {

		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, info.getId());
		initialValues.put(NAME, info.getFilename());
		initialValues.put(LOCAL_NAME, info.getLocalname());
		initialValues.put(URL, info.getUrl());
		initialValues.put(FILE_SIZE, info.getFilesize());
		initialValues.put(COMPLETE_SIZE, info.getCompletesize());
		
		initialValues.put(APK_ICON, info.getApkIconBuffer());
		initialValues.put(TITLE, info.getTitle());
		initialValues.put(DOWNLOAD_TYPE, info.getDownloadType());
		
		db.insert(DATABASE_TABLE, null, initialValues);
		
		close();
	}
	/**
	 * 删除数据,根据指定id删除
	 */
	public synchronized void delete(PushDownloadInfo info) {
		delete(info.getId());
	}
	/**
	 * 删除数据,根据指定id删除
	 */
	public synchronized void delete(int id) {

		 open();
		 db.delete(DATABASE_TABLE, ID + "=" + id, null);
		 close();
	}

	/**
	 * 更改数据，根据指定id更改
	 */
	public synchronized void update(PushDownloadInfo info) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, info.getId());
		initialValues.put(NAME, info.getFilename());
		initialValues.put(LOCAL_NAME, info.getLocalname());
		initialValues.put(URL, info.getUrl());
		initialValues.put(FILE_SIZE, info.getFilesize());
		initialValues.put(COMPLETE_SIZE, info.getCompletesize());
		
		initialValues.put(APK_ICON, info.getApkIconBuffer());
		initialValues.put(TITLE, info.getTitle());
		initialValues.put(DOWNLOAD_TYPE, info.getDownloadType());
		
		int row = db.update(DATABASE_TABLE, initialValues, ID + "="+ info.getId(), null);
		if(row <= 0)
		{
			db.insert(DATABASE_TABLE, null, initialValues);
		}
		close();
	}

	public synchronized Cursor getAll() {

		Cursor cur = db.query(DATABASE_TABLE, null, null, null, null, null,null);
		return cur;
	}

	/**
	 * 根据当前id获取下载信息
	 * @param id
	 * @return
	 */
	public synchronized PushDownloadInfo get(int id){
		PushDownloadInfo info = null;
		open();
		Cursor cur = db.query( DATABASE_TABLE,null, ID + " = " + id, null, null, null, null);
		Log.e("PushDownloadService", "-----dbHelper---> "+cur.getCount());
		if (cur != null && cur.moveToFirst()) {
			info = new PushDownloadInfo();
			info.setId(cur.getInt(cur.getColumnIndex(ID)));
			info.setFilename(cur.getString(cur.getColumnIndex(NAME)));
			info.setLocalname(cur.getString(cur.getColumnIndex(LOCAL_NAME)));
			info.setUrl(cur.getString(cur.getColumnIndex(URL)));
			info.setFilesize(cur.getInt(cur.getColumnIndex(FILE_SIZE)));
			info.setCompletesize(cur.getInt(cur.getColumnIndex(COMPLETE_SIZE)));
			
			info.setTitle(cur.getString(cur.getColumnIndex(TITLE)));
			info.setDownloadType(cur.getInt(cur.getColumnIndex(DOWNLOAD_TYPE)));
			info.setApkIconBuffer(cur.getBlob(cur.getColumnIndex(APK_ICON)));

			Log.e("PushDownloadService", "-----dbHelper---> "+666);
		}
		close();

		return info;
	}

}
