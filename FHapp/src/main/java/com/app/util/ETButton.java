package com.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ETButton extends ImageButton {
	public static final float[] ETBUTTON_SELECTED = { 0.6F, 0.0F, 0.0F, 0.0F,
			0.0F, 0.0F, 0.6F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 0.0F, 0.0F,
			0.0F, 0.0F, 0.0F, 0.6F, 0.0F };

	public static final float[] ETBUTTON_NOT_SELECTED = { 1.0F, 0.0F, 0.0F,
			0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F,
			0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F };

	public ETButton(Context context) {
		super(context);
	}

	@SuppressLint("NewApi")
	public ETButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == 0) {
					v.getBackground().setColorFilter(
							new ColorMatrixColorFilter(
									ETButton.ETBUTTON_SELECTED));
					v.setBackground(v.getBackground());
					return false;
				}
				if (event.getAction() == 1) {
					v.getBackground().setColorFilter(
							new ColorMatrixColorFilter(
									ETButton.ETBUTTON_NOT_SELECTED));
					v.setBackground(v.getBackground());
					return false;
				}
				return false;
			}
		});
	}

	public ETButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public void setStatus(boolean val)
	{
		if(val)
		{
			getBackground().setColorFilter(
					new ColorMatrixColorFilter(
							ETButton.ETBUTTON_NOT_SELECTED));
		}
		else
		{
			getBackground().setColorFilter(
					new ColorMatrixColorFilter(
							ETButton.ETBUTTON_SELECTED));
		}
	}
}
