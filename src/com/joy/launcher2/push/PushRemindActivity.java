package com.joy.launcher2.push;

import com.joy.launcher2.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PushRemindActivity extends Activity implements OnClickListener
{
	
	ImageView pushImageCancel;
	TextView pushTitle;
	TextView pushDescription;
	Button pushDownloadBtn;
	ImageView pushIcon;
	
	String title;
	Bitmap icon;
	String description;
	String url;
	int id;
	int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_remind);
		
		Intent data = getIntent();
		Bundle bundle = data.getBundleExtra(PushUtils.PUSH_DETAIL_INFO);
		if(bundle == null)
		{
			finish();
			return;
		}
		
		init(bundle);
		
	}

	private void init(Bundle bundle)
	{
		title = bundle.getString(PushUtils.PUSH_DETAIL_TITLE);
		icon = (Bitmap)bundle.getParcelable(PushUtils.PUSH_DETAIL_ICON);
		description = bundle.getString(PushUtils.PUSH_DETAIL_DESCRIPTION);
		url = bundle.getString(PushUtils.PUSH_DETAIL_URL);
		id = bundle.getInt(PushUtils.PUSH_DETAIL_ID);
		type = bundle.getInt(PushUtils.PUSH_DETAIL_TYPE);
		
		pushImageCancel = (ImageView)findViewById(R.id.push_image_cancel);
		pushImageCancel.setOnClickListener(this);
		
		pushTitle = (TextView)findViewById(R.id.push_title);
		pushTitle.setText(title);
		pushIcon = (ImageView)findViewById(R.id.push_remind_icon);
		if(icon != null)
		{
			pushIcon.setImageBitmap(icon);
		}
		else
		{
			pushIcon.setImageResource(R.drawable.ic_launcher);
		}
		pushDescription = (TextView)findViewById(R.id.push_description);
		pushDescription.setText(description);
		
		pushDownloadBtn = (Button)findViewById(R.id.push_download_btn);
		if(type == PushUtils.PUSH_DETAIL_DOWNLOAD_REMIND)
		{
			
			pushDownloadBtn.setVisibility(View.VISIBLE);
			pushDownloadBtn.setOnClickListener(this);
		}
		else
		{
			pushDownloadBtn.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		Bundle bundle = getIntent().getBundleExtra(PushUtils.PUSH_DETAIL_INFO);
		if(bundle == null)
		{
			finish();
			return;
		}
		
		init(bundle);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.push_image_cancel:
			finish();
			break;

        case R.id.push_download_btn:
        	
        	Bundle bundle = getIntent().getBundleExtra(PushUtils.PUSH_DETAIL_INFO);
        	Intent download = new Intent(PushUtils.PUSH_DOWNLOAD_ACTION);
			download.putExtra(PushUtils.PUSH_DETAIL_INFO, bundle);
			PushUtils.startPushService(this, download);
        	finish();
			break;
		default:
			break;
		}
	}
	
}
