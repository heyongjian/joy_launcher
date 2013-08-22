package com.joy.launcher.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.joy.launcher.R;


/**
 * 
 * implement a custom dialog use by PopupWindow.
 * use it should like this:
 * JoyPopupWindowForDialog dialog = new JoyPopupWindowForDialog(getApplicationContext());
 * dialog.setViewParent(parent);
 * dialog.setTextView(s);
 * dialog.setOnClickListener(l);
 * dialog.showPopupWindow(x, y, 0, 0);
 * @author yongjian.he
 */
public class JoyPopupWindowForDialog extends ViewGroup{
	private Context context;
	private Button positive_button;
	private Button negative_button;
	private TextView textView;
	private LinearLayout layout;
	private ViewGroup parent;
	private OnClickListener l;
	private PopupWindow popupWindow;
	static final String TAG = "JoyPopupWindowForDialog"; 
	
	public JoyPopupWindowForDialog(Context context) {
		this(context, null);
	}
	public JoyPopupWindowForDialog(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	public JoyPopupWindowForDialog(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    this.context = context;
	    init();
	}
	   
	private void init() {
		// TODO Auto-generated method stub
		layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_launcher_custom, null);
		positive_button = (Button) layout.findViewById(R.id.positive_button);
		negative_button = (Button) layout.findViewById(R.id.negative_button);
		textView = (TextView) layout.findViewById(R.id.dialog_text);
		textView.setText("要运行 推荐应用(2.23MB)，需要下载安装，是否继续?");
		
	}
	
	/**
	 * 
	 * @author yongjian.he
	 * @param x,the x coordinate of view location
	 * @param y,the y coordinate of view location
	 * @param width 
	 * @param hight
	 */
	public void showPopupWindow(int x, int y, int width, int hight) {
		if (width == 0 || hight == 0){
			width = context.getResources().getDisplayMetrics().widthPixels;
			hight = context.getResources().getDisplayMetrics().heightPixels;
//			Log.e(TAG, "----showPopupWindow,width: " + width + "hight: " + hight);
		}
		
		positive_button.setOnClickListener(l);
		negative_button.setOnClickListener(l);
		popupWindow = new PopupWindow(context);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
		popupWindow.setWidth(width);
		popupWindow.setHeight(hight);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setFocusable(true);
		popupWindow.setContentView(layout);
		popupWindow.setAnimationStyle(R.style.mypopwindow_animation);
		popupWindow.showAtLocation(parent, Gravity.LEFT
				| Gravity.TOP, x, y);

	}
	public void setOnClickListener(OnClickListener mListener){
		l = mListener;
	}
	public void setViewParent(ViewGroup v){
		parent = v;
	}
	public void setTextView(String s){
		textView.setText(s);
	}
	public void dismiss(){
		popupWindow.dismiss();
	}
	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}

}
