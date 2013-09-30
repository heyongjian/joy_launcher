package com.joy.launcher2.joyfolder;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.joy.launcher2.LauncherApplication;
import com.joy.launcher2.R;
import com.joy.launcher2.Utilities;
import com.joy.launcher2.cache.ImageDownLoadCallback;
import com.joy.launcher2.download.DownLoadDBHelper;
import com.joy.launcher2.download.DownloadInfo;
import com.joy.launcher2.download.DownloadManager;
import com.joy.launcher2.download.DownloadManager.DownLoadTask;

/**
 * 在线文件夹中应用推荐列表
 * @author wanghao
 *
 */
public class JoyFolderAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	List<Map<String, Object>> list;
	Context c;
	Drawable defaultDrawable;
	public JoyFolderAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		this.c = context;
		init();
	}
	public void setList(List<Map<String, Object>> list2){
		this.list = list2;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private void init(){
		Drawable tempbd = LauncherApplication.mContext.getResources().getDrawable(R.drawable.joy_folder_app_icon_default);
		Bitmap icon_bitmap = Utilities.createIconBitmap(tempbd, LauncherApplication.mContext);
		defaultDrawable = new BitmapDrawable(LauncherApplication.mContext.getResources(), icon_bitmap); 
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final String iconUrl = list.get(position).get("icon").toString();
		final int id = (Integer)list.get(position).get("id");
		final String title = list.get(position).get("name").toString();
		if (convertView == null) {
			convertView = (JoyIconView)inflater.inflate(R.layout.joy_folder_app_icon, null);
		}
		final JoyIconView view = (JoyIconView)convertView;
		view.setText(title);
		
		if (DownloadManager.getInstances().isCompleted(id)) {
			String Localname = DownLoadDBHelper.getInstances().get(id).getLocalname();
			view.showProgressBar(false);
		}else{
			view.showProgressBar(true);
		}
		
		DownLoadTask task = DownloadManager.getInstances().getDowmloadingTask(id);
		if (task != null&&!task.isSecretly()) {
			DownloadInfo dinfo = task.getDownloadInfo();
			view.setDownloadInfo(dinfo);
			dinfo.setView(view);
		}else{
			view.setDownloadInfo(null);
		}
		view.setCompoundDrawablesWithIntrinsicBounds(null,defaultDrawable,null, null);

		LauncherApplication.mBcache.getBitmap(iconUrl, null, new ImageDownLoadCallback() {
			
			@Override
			public void imageDownLoaded(Bitmap bm) {
				// TODO Auto-generated method stu
				BitmapDrawable bd = null;
				if (bm != null) {
					BitmapDrawable tempbd = new BitmapDrawable(LauncherApplication.mContext.getResources(), bm);
					Bitmap icon_bitmap = Utilities.createIconBitmap(tempbd, LauncherApplication.mContext);
					bd= new BitmapDrawable(LauncherApplication.mContext.getResources(), icon_bitmap);
					view.setCompoundDrawablesWithIntrinsicBounds(null,bd,null, null);
				}
			}
		}, null);
		return view;
	}
}
