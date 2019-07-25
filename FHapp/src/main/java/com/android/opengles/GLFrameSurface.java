package com.android.opengles;

import com.app.util.log;
import com.app.view.VideoPlayView;
import com.fh.lib.Define;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class GLFrameSurface extends GLSurfaceView implements View.OnTouchListener, OnGestureListener, OnDoubleTapListener {
	private static final String TAG = "XX";
	private static final float SCALE_STEP = 0.1f;
	private GestureDetector detector;
	private GLFrameRenderer mFrameRender;
	private float hOffset = -1;
	private float hDegrees = -1;
	private float vDegrees = -1;
	private float baseValue = -1;
	private float[] hEyeDegrees = new float[4];
	private int curIndex = 0;
	private boolean isScaleMode = false;

	SensorManager mySensorManager;	//SensorManager对象引用
	Sensor myGyroscope; 	//传感器类型
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private float angle[] = new float[3];

	private Context mContext;

	private int lastRot = -1;
	private Sensor aSensor;
	private Sensor mSensor;
	float[] accelerometerValues = new float[3];
	float[] magneticFieldValues = new float[3];
	private float startVDegrees = 0;
	private float startHDegrees = 0;
	private VideoPlayView mPlayView;
	public GLFrameSurface(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		mContext = context;

		mPlayView = new VideoPlayView(mContext);

		mFrameRender = GLFrameRenderer.getInstance();
		detector = new GestureDetector(this);
		detector.setIsLongpressEnabled( true );
		detector.setOnDoubleTapListener(this);
		setOnTouchListener(this);



		//获得SensorManager对象
		mySensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
		//传感器的类型
		myGyroscope=mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


		aSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);





	}

	final SensorEventListener myListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				magneticFieldValues = sensorEvent.values;
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				accelerometerValues = sensorEvent.values;

			calculateOrientation();
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};


	public void rigisterListener()
	{
		try{
			mySensorManager.registerListener(
					mySensorListener, 		//添加监听
					myGyroscope, 		//传感器类型
					SensorManager.SENSOR_DELAY_GAME	//传感器事件传递的频度
			);

			mySensorManager.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
			mySensorManager.registerListener(myListener, mSensor,SensorManager.SENSOR_DELAY_GAME);

		}catch(Exception e){
			//e.printStackTrace();
		}



	}

	public void unRigisterListener()
	{
		try{

			mySensorManager.unregisterListener(mySensorListener, myGyroscope);
			mySensorManager.unregisterListener(myListener, aSensor);
			mySensorManager.unregisterListener(myListener, mSensor);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public GLFrameSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onAttachedToWindow() {
		//Utils.LOGD("surface onAttachedToWindow()");
		super.onAttachedToWindow();
		// setRenderMode() only takes effectd after SurfaceView attached to window!
		// note that on this mode, surface will not render util GLSurfaceView.requestRender() is
		// called, it's good and efficient -v-

		//setRenderMode(RENDERMODE_WHEN_DIRTY);
		//Utils.LOGD("surface setRenderMode RENDERMODE_WHEN_DIRTY");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub

		if(PlayInfo.displayMode == 0)
		{
			return super.onTouchEvent(event);
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mFrameRender.setvelocityX(0);
			mFrameRender.setvelocityY(0);
			baseValue = 0;

			mPlayView.setLayoutMenuShow();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (isScaleMode)
			{
				isScaleMode = false;
				return false;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (event.getPointerCount() == 1) {//防止不同时离开屏幕执行onScroll事件
				if (isScaleMode)
					return false;
			}
			if (event.getPointerCount() == 2) {
				isScaleMode = true;

				float x = event.getX(0) - event.getX(1);
				float y = event.getY(0) - event.getY(1);
				float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
				if (baseValue == 0) {
					baseValue = value;
				} else {
					float step = 0;
					float scale = value / baseValue;// // // 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例
					if (scale > 1)
					{
						step = SCALE_STEP;
					}
					else if (scale < 1)
					{
						step = -SCALE_STEP;
					}
					mFrameRender.depth = mFrameRender.depth + step;
					if (mFrameRender.depth < FHSDK.getMaxZDepth(mFrameRender.hWin))
						mFrameRender.depth = FHSDK.getMaxZDepth(mFrameRender.hWin);
					else if (mFrameRender.depth > 0)
						mFrameRender.depth = 0;
				}
				return false; // 缩放操作时不执行手势
			}
		}


		return detector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onDown");
		int i;


		if (3 == mFrameRender.eyeMode)
		{
			return false;
		}

		hOffset = mFrameRender.hOffset;
		vDegrees = mFrameRender.vDegrees;
		hDegrees = mFrameRender.hDegrees;
		for ( i = 0; i < 4; i++)
		{
			hEyeDegrees[i] = mFrameRender.hEyeDegrees[i];
		}
		if (e.getX() <= mFrameRender.mScreenWidth/2 && e.getY() <= mFrameRender.mScreenHeight/2)
			curIndex = 2;
		else if (e.getX() <= mFrameRender.mScreenWidth
				&&e.getX() > mFrameRender.mScreenWidth /2
				&& e.getY() <= mFrameRender.mScreenHeight/2)
			curIndex = 3;
		else if (e.getX() <= mFrameRender.mScreenWidth/2
				&& e.getY() <= mFrameRender.mScreenHeight
				&& e.getY() > mFrameRender.mScreenHeight/2)
			curIndex = 0;
		else if (e.getX() <= mFrameRender.mScreenWidth
				&&e.getX() > mFrameRender.mScreenWidth /2
				&& e.getY() <= mFrameRender.mScreenHeight
				&& e.getY() > mFrameRender.mScreenHeight/2)
			curIndex = 1;

		mFrameRender.curIndex = curIndex;



		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub

		//log.e("onFling: e1.getX() = " + e1.getX() + "  e2.getX() =" + e2.getX() + " velocityX =" + velocityX+ " velocityY =" + velocityY);
		if (Math.abs(velocityX) > 2000)//解决慢速滑动依然触发onFling导致的手势滑动问题
		{
			mFrameRender.setvelocityX(velocityX);
		}
		if (Math.abs(velocityY) > 2000)//解决慢速滑动依然触发onFling导致的手势滑动问题
			mFrameRender.setvelocityY(velocityY);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		//log.e("onScroll: e1.getX() = " + e1.getX() + "  e2.getX() =" + e2.getX() + " distanceX =" + distanceX+ " distanceY =" + distanceY);
		if (0 == mFrameRender.displayMode || 6 == mFrameRender.displayMode)
		{
			float offsetX = (e2.getX() - e1.getX());
			float offsetY = (e2.getY() - e1.getY());
			if (Math.abs(offsetX) < 2 && Math.abs(offsetY) < 2) // 防止误操作  设大点会不灵敏
				return false;

			if (0 ==mFrameRender.eyeMode || 1 == mFrameRender.eyeMode)
			{
				mFrameRender.vDegrees = vDegrees - offsetY/10;
				mFrameRender.hDegrees = hDegrees - offsetX/10;

/*
				if (6 == mFrameRender.displayMode)
				{
					if (mFrameRender.hDegrees >= FHSDK.getMaxHDegress(mFrameRender.hWin))
						mFrameRender.hDegrees = FHSDK.getMaxHDegress(mFrameRender.hWin);
					if (mFrameRender.hDegrees <= FHSDK.getMinHDegress(mFrameRender.hWin))
						mFrameRender.hDegrees = FHSDK.getMinHDegress(mFrameRender.hWin);
				}
				if (mFrameRender.vDegrees < FHSDK.getMaxVDegress(mFrameRender.hWin))
					mFrameRender.vDegrees = FHSDK.getMaxVDegress(mFrameRender.hWin);
				else if (mFrameRender.vDegrees > FHSDK.getMinVDegress(mFrameRender.hWin))
					mFrameRender.vDegrees = FHSDK.getMinVDegress(mFrameRender.hWin);
*/
			}
			else if (2 == mFrameRender.eyeMode)
			{
				mFrameRender.hEyeDegrees[curIndex] = hEyeDegrees[curIndex] - offsetX/10;
			}
		}
		else
		{
			final float base = 500;
			float offset = (e2.getX() - e1.getX())/base;
			mFrameRender.hOffset = hOffset - offset;
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onShowPress");

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onSingleTapUp");
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onDoubleTap");
//		if (mFrameRender.depth != 0)
//			mFrameRender.depth = 0;
//		else
//			mFrameRender.depth = FHSDK.getMaxZDepth();

		mFrameRender.isDoubleClick = true;

		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onDoubleTapEvent");
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		//log.e("onSingleTapConfirmed");
		return false;
	}
	private void calculateOrientation()
	{
		float[] values = new float[3];
		float[] R = new float[9];
		SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
		SensorManager.getOrientation(R, values);

		//mySensorManager.getOrientation(R, values);
		// 要经过一次数据格式的转换，转换为度
		values[0] = (float) Math.toDegrees(values[0]);

		values[1] = (float) Math.toDegrees(values[1]);
		values[2] = (float) Math.toDegrees(values[2]);


		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int uiRot = wm.getDefaultDisplay().getRotation();

		//Log.i(TAG, values[0]+", " + values[1]+", "+ values[2]+"");
		if (0 == startVDegrees)
		{
			if (0 == uiRot)
				startVDegrees = values[1];
			else if (1 == uiRot)
				startVDegrees = values[2];
			else if (2 == uiRot)
				startVDegrees = -values[1];
			else if (3 == uiRot)
				startVDegrees = -values[2];
		}
		if (0 == startHDegrees)
		{
			if (0 == uiRot)
				startHDegrees = -values[2];
			else if (1 == uiRot)
				startHDegrees = values[0];
			else if (2 == uiRot)
				startHDegrees = values[2];
			else if (3 == uiRot)
				startHDegrees = values[0]-180;
		}

          /*
        if(values[0] >= -5 && values[0] < 5){
           Log.i(TAG, "正北");
        }
        else if(values[0] >= 5 && values[0] < 85){
            Log.i(TAG, "东北");
        }
        else if(values[0] >= 85 && values[0] <=95){
            Log.i(TAG, "正东");
        }
        else if(values[0] >= 95 && values[0] <175){
            Log.i(TAG, "东南");
        }
        else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
            Log.i(TAG, "正南");
        }
        else if(values[0] >= -175 && values[0] <-95){
            Log.i(TAG, "西南");
        }
        else if(values[0] >= -95 && values[0] < -85){
            Log.i(TAG, "正西");
        }
        else if(values[0] >= -85 && values[0] <-5){
            Log.i(TAG, "西北");
        }
        */
	}

	private SensorEventListener mySensorListener = new SensorEventListener(){//开发实现了SensorEventListener接口的传感器监听器
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy){

//				vDegrees = mFrameRender.vDegrees;
//				hDegrees = mFrameRender.hDegrees;
		}
		@Override
		public void onSensorChanged(SensorEvent event){

			WindowManager wm = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			int uiRot = wm.getDefaultDisplay().getRotation();
			if (lastRot != uiRot)
			{
				timestamp = 0;
				angle[0] = 0;
				angle[1] = 0;
				angle[2] = 0;
				lastRot = uiRot;
				//return;
				startVDegrees = 0;
				startHDegrees = 0;
			}

			float anglex = 0, angley = 0, anglez = 0;
			float []values=event.values;//获取三个轴方向上的加速度值

			if (timestamp != 0)
			{
				// event.timesamp表示当前的时间，单位是纳秒（1百万分之一毫秒）
				final float dT = (event.timestamp - timestamp) * NS2S;
				angle[0] += event.values[0] * dT;
				angle[1] += event.values[1] * dT;
				angle[2] += event.values[2] * dT;


				anglex = (float) Math.toDegrees(angle[0]);
				angley = (float) Math.toDegrees(angle[1]);
				anglez = (float) Math.toDegrees(angle[2]);

			}
			timestamp = event.timestamp;

			if (1 == uiRot)
			{
				float tmp = anglex;
				anglex = -angley;
				angley = tmp;
			}
			else if (2 == uiRot)
			{
				anglex = -anglex;
				angley = -angley;
			}
			else if (3 == uiRot)
			{
				float tmp = anglex;
				anglex = angley;
				angley = -tmp;
			}

			//log.e("startVDegrees = " + startVDegrees + " uiRot = " + uiRot + " x："+anglex + "  y: " + angley + "  z: " + anglez);
			float offsetX = anglex;
			float offsetY = angley;
			//if (Math.abs(offsetX - lastOffsetX) < 0.1 && Math.abs(offsetY - lastOffsetY) < 0.1)
			{
				//	return;
			}
			if (3 == mFrameRender.eyeMode)
			{
				if (0 == mFrameRender.displayMode)
				{
					vDegrees = startVDegrees - offsetX;
					hDegrees = startHDegrees - offsetY;

					if (vDegrees < FHSDK.getMaxVDegress(mFrameRender.hWin))
						vDegrees = FHSDK.getMaxVDegress(mFrameRender.hWin);
					else if (vDegrees > 0)
						vDegrees = 0;

					mFrameRender.vDegrees = vDegrees;
					mFrameRender.hDegrees = hDegrees;

					//log.e("vDegrees = " + mFrameRender.vDegrees + "hDegrees = " + mFrameRender.hDegrees);

				}
				else if (6 == mFrameRender.displayMode)
				{
					vDegrees = startVDegrees - offsetX + 90;
					hDegrees = startHDegrees - offsetY;

					if (vDegrees < FHSDK.getMaxVDegress(mFrameRender.hWin))
						vDegrees = FHSDK.getMaxVDegress(mFrameRender.hWin);
					else if (vDegrees > FHSDK.getMinVDegress(mFrameRender.hWin))
						vDegrees = FHSDK.getMinVDegress(mFrameRender.hWin);

					mFrameRender.vDegrees = vDegrees;
					mFrameRender.hDegrees = hDegrees;

					//log.e("vDegrees = " + mFrameRender.vDegrees + "hDegrees = " + mFrameRender.hDegrees);
				}
			}
		}
	};

}
