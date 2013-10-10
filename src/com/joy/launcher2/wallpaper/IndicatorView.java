package com.joy.launcher2.wallpaper;

import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 指示器
 * @author huangming
 *
 */
public class IndicatorView extends View{

	
	public IndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public IndicatorView(Context context) {
		super(context, null);
		// TODO Auto-generated constructor stub
	}
	public IndicatorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		Path path = new Path();
		path.moveTo(width / 2, 0);
		path.lineTo(0, height);
		path.lineTo(width, height);
		canvas.drawPath(path, paint);
		
		
	}

	

}
