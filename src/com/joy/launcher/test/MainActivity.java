//package com.joy.launcher.test;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.joy.launcher.network.impl.Service.CallBack;
//import com.joy.launcher.util.Constants;
//import com.joy.launcher.util.Util;
//
//public class MainActivity extends Activity {
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		 
//		setContentView(R.layout.activity_main);
//		
//		final ImageView img = (ImageView)findViewById(R.id.imageView1);
//		
//		MApplication.mBcache.getBitmap(Constants.BASE_URL, img, null);
//
//		MApplication.mService.GotoNetwork(new CallBack() {
//			Bitmap bitmap = null;
//			@Override
//			public void onPreExecute() {
//				// TODO Auto-generated method stub
//				img.setVisibility(View.GONE);
//				Util.toast("开始下载");
//			}
//
//			@Override
//			public void onPostExecute() {
//				// TODO Auto-generated method stub
//				String str = null;
//				if(bitmap != null){
//					img.setVisibility(View.VISIBLE);
//					img.setImageBitmap(bitmap);
//					str = "下载成功！";
//				}else{
//					str = "下载失败！";
//				}
//				Util.toast(str);
//			}
//
//			@Override
//			public void doInBackground() {
//				// TODO Auto-generated method stub
//				try {
////					MApplication.mService.getTestData();
//					bitmap =  MApplication.mService.getBitmapByUrl(Constants.BASE_URL, null);
//				} catch (Exception e) {
//					// TODO Auto-generated ca= tch block
//					e.printStackTrace();
//				}
//			}
//		});
//	}
//}
