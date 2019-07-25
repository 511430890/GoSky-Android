package com.android.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.app.util.log;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

public class MyGLSurfaceView extends GLSurfaceView implements Renderer, SurfaceTexture.OnFrameAvailableListener {
	private static final String TAG = "MyGLSurfaceView";
	Context mContext;
	SurfaceTexture mSurface;
	Surface mySurface;
	int mTextureID = -1;
	DirectDrawer mDirectDrawer;
	private int screenWidth, screenHeight;

	public MyGLSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceCreated...");
		mTextureID = createTextureID();
		mSurface = new SurfaceTexture(mTextureID);
		mSurface.setOnFrameAvailableListener(this);
		mDirectDrawer = new DirectDrawer(mTextureID);
		//CameraInterface.getInstance().doOpenCamera(null);
		
		mySurface = new Surface(mSurface);
		//MyMediaCodec.getInstance().startPlay(mySurface);
		PlayInfo.surface = mySurface;
		log.e("PlayInfo.surface = " + PlayInfo.surface);


	}
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceChanged...");
		GLES20.glViewport(0, 0, width, height);

		screenWidth = width;
		screenHeight = height;
	}
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "onDrawFrame...");
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mSurface.updateTexImage();
		float[] mtx = new float[16];
		mSurface.getTransformMatrix(mtx);


			int[][] viewPort = new int [2][4];
    		viewPort[0][0] = 0;
    		viewPort[0][1] = 0;
    		viewPort[0][2] = screenWidth;
    		viewPort[0][3] = screenHeight;

    		mDirectDrawer.draw(mtx, viewPort);


	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	private int createTextureID()
	{
		int[] texture = new int[1];

		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);        
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		return texture[0];
	}
	public SurfaceTexture _getSurfaceTexture(){
		return mSurface;
	}
	
	static long lastTime = System.currentTimeMillis();
	static int frameNo = 0;
	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		this.requestRender();
	}

}
