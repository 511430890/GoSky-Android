package com.android.opengles;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.app.util.log;
import com.fh.lib.Define;
import com.fh.lib.Define.Circle;
import com.fh.lib.Define.YUVDataCallBackInterface;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;
import com.research.GLRecorder.GLRecorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;

import eye.app.activity.R;

public class GLFrameRenderer implements Renderer{
	private static final float H_OFFSET_BASE = 2000.0f;
	private static final float STEP_BASE_FAST = 200.0f;
	private static final float STEP_BASE_SLOW = 100.0f;
	private static final float STEP_OFFSET = 3.0f;
	
	private static final int DISPLAY_TYPE_YUV420P = 0;
	private static final int DISPLAY_TYPE_RGB = 1;
	private static final int DISPLAY_TYPE_YUV420SP = 2;
	private static final int DISPLAY_TYPE_IMAGE = 3;
    private GLSurfaceView mTargetSurface;
    //private GLProgram prog = new GLProgram(0);
    public static int mScreenWidth, mScreenHeight;
    public static int mDrawWidth, mDrawHeight;
    private int view_x, view_y, view_w, view_h;
    private int mVideoWidth, mVideoHeight;
    private byte[] y;
    private byte[] u;
    private byte[] v;
    private byte[] yuv;
    
    public class RGBRes{
    	public byte[] rgb;
    	public int width;
    	public int height;
    }
    public static RGBRes[] mRgbRes = new RGBRes[24];
    
    public static int rgbResIndex = 0;
    //public static byte[] rgb, rgb_hik, rgb_hik4_3, rgb_hik16_9, rgb_xyz;
    private boolean bSurfaceCreate = false;
	private boolean bSurfaceChanged = false;
	private int lastShowMode = -1;
	private Handler mHandler = null;
	private Context mContext;
	private static GLFrameRenderer instance;
	private int drawCount = 0;
	private byte[] frameBuf = null;
	public final static boolean isDebugMode = false; 
	public static int displayMode = 0;
	public static boolean bSnapshot = false;
	
	public static float vDegrees = 0;
	public static float hDegrees = 0;
	public static float depth = 0;
	
	public static float hOffset = 0; 
	private static float velocityX = 0;
	private static float velocityY = 0;
	public static float scrollStep = 0;
	private SnapshotThread mSnapshotThread;
	public static int modeOffset = 0;
	public static int eyeMode = 0; //  0 default, 1 four screen 2 four alone screen 3 vr
	public static float[] hEyeDegrees = new float[]{0, 90, 180, 270};
	
	public static int curIndex = 0;
	public static boolean isDoubleClick = false;
	public static boolean isZoomIn = false;
	public static long hWin = 0, hBuffer = 0;
	public static long hImageWin = 0, hImageBuffer = 0;
	public static long hWinMixMode[]  = new long[4];
	private boolean bUpdated = false;
	public static boolean  bMixMode = false;
	public static int ctrlIndex = 0;
	public static boolean resChanged = false;
	private float lastVDegrees = -1;
	private float lastHDegrees = -1;
	private float lastDepth = -1;
	private float lastHOffset = -1;
	public static float circleX, circleY, circleR;
	public static boolean bAdjust = false;
	public static int displayType = 0;
	private EGLConfig mEGLConfig;
	private byte[] imageBuffer = null;
	private int imageWidth, imageHeight;
	//public DisplayWindows[] mWindows = new DisplayWindows[4];
    public GLFrameRenderer(Context context, GLSurfaceView surface, DisplayMetrics dm) {
    	mContext = context;
        mTargetSurface = surface;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        FHSDK.registerUpdateCallBack(dataFun);
        //loadRgbFile();
		
		
		mHandler = new Handler();
		mHandler.post(requestRender);
      
		mSnapshotThread = new SnapshotThread();
    }
    public GLFrameRenderer()
    {
    	
    }
    
    public static GLFrameRenderer getInstance()
    {
        if(null == instance)
        {
            instance = new GLFrameRenderer();
        }
        return instance;             
    }
    Runnable scaleView = new Runnable()
	{
		@Override
		public void run() {
			
			final float totalTime = 2*100;
			float vDegreesStep = Math.abs((FHSDK.getMaxVDegress(hWin) - FHSDK.getMinVDegress(hWin)) /totalTime);//Math.abs(FHSDK.getMaxVDegress(hWin)/totalTime);
			float depthStep = Math.abs(FHSDK.getMaxZDepth(hWin)/totalTime);
			float hDegreesStep = 90/totalTime;
			
			if (isZoomIn)
			{
				if(0 == FHSDK.getDisplayType(hWin))
					vDegrees -= vDegreesStep;
				else if(1 == FHSDK.getDisplayType(hWin))
					vDegrees += vDegreesStep;
				
				hDegrees += hDegreesStep;
				depth += depthStep;
			}
			else
			{
				float speed = 4.0f;
				
				if(0 == FHSDK.getDisplayType(hWin))
					vDegrees += vDegreesStep*speed;
				else if(1 == FHSDK.getDisplayType(hWin))
					vDegrees -= vDegreesStep*speed;
				
				
				
				hDegrees -= hDegreesStep*speed;
				depth -= depthStep*speed;
			}
			
			if (vDegrees < FHSDK.getMaxVDegress(hWin))
				vDegrees = FHSDK.getMaxVDegress(hWin);
			else if (vDegrees > FHSDK.getMinVDegress(hWin))
				vDegrees = FHSDK.getMinVDegress(hWin);


			if (depth < FHSDK.getMaxZDepth(hWin))
				depth = FHSDK.getMaxZDepth(hWin);
			else if (depth > 0)
				depth = 0;

			
			if ((isZoomIn && depth != 0)||  (!isZoomIn && depth != FHSDK.getMaxZDepth(hWin)))
				mHandler.postDelayed(scaleView, 10);
		}
	};
	
    Runnable requestRender = new Runnable()
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (0 != displayMode && 6 != displayMode)
				eyeMode = 0;
//			
			if (isDoubleClick && ((0 == displayMode && 0 == eyeMode) ||6 == displayMode))
			{
				isDoubleClick = false;
				
				if (depth != FHSDK.getMaxZDepth(hWin))
				{
					isZoomIn = false;
				}
				else
				{
					isZoomIn = true;
				}
				mHandler.post(scaleView);

			}

				
				//log.e("vDegrees = " + vDegrees + "  hDegrees = " + hDegrees + "  depth = " + depth );
			
			
			if (velocityX > 0)
			{
				velocityX -= scrollStep;
				if (velocityX < 0 || scrollStep/H_OFFSET_BASE < 0.001f)
					velocityX = 0;
					
				if (0 == displayMode || 6 == displayMode)
				{
					if (0 == eyeMode || 1 == eyeMode)
					{
						hDegrees -= (scrollStep/H_OFFSET_BASE)*50;
					}
					else if (2 == eyeMode)
					{
						hEyeDegrees[curIndex] -= (scrollStep/H_OFFSET_BASE)*50;
					}
				}
				else
					hOffset -= scrollStep/H_OFFSET_BASE;


				//log.e("scrollStep = "  + scrollStep);
				//log.e("scrollStep/H_OFFSET_BASE; = "  + scrollStep/H_OFFSET_BASE);
			}
			else if (velocityX < 0)
			{
				velocityX += scrollStep;
				if (velocityX > 0 || scrollStep/H_OFFSET_BASE < 0.005f)
					velocityX = 0;
				
				if (0 == displayMode || 6 == displayMode)
				{
					if (0 == eyeMode || 1 == eyeMode)
					{
						hDegrees += (scrollStep/H_OFFSET_BASE)*50;
					}
					else if (2 == eyeMode)
					{
						hEyeDegrees[curIndex] += (scrollStep/H_OFFSET_BASE)*50;
					}
				}
				else
					hOffset += scrollStep/H_OFFSET_BASE;
				
			}
			
			if (velocityY > 0)
			{
				velocityY -= scrollStep;
				if (velocityY < 0 || scrollStep/H_OFFSET_BASE < 0.005f)
					velocityY = 0;
					
				if (0 == displayMode || 6 == displayMode)
				{
					vDegrees -= (scrollStep/H_OFFSET_BASE)*50;
					
					if (vDegrees < FHSDK.getMaxVDegress(hWin))
						vDegrees = FHSDK.getMaxVDegress(hWin);
					else if (vDegrees > FHSDK.getMinVDegress(hWin))
						vDegrees = FHSDK.getMinVDegress(hWin);
					
				}
				else
					hOffset -= scrollStep/H_OFFSET_BASE;


				//log.e("scrollStep = "  + scrollStep);
				//log.e("scrollStep/H_OFFSET_BASE; = "  + scrollStep/H_OFFSET_BASE);
			}
			else if (velocityY < 0)
			{
				velocityY += scrollStep;
				if (velocityY > 0 || scrollStep/H_OFFSET_BASE < 0.005f)
					velocityY = 0;
				
				if (6 == displayMode)
				{
					vDegrees += (scrollStep/H_OFFSET_BASE)*50;
				}
				else
					hOffset += scrollStep/H_OFFSET_BASE;
				
			}
			
			
			
			if (scrollStep > 0)
				scrollStep -= STEP_OFFSET;
			

			//if (bSurfaceCreate)
			//	mTargetSurface.requestRender();


			
			
			mHandler.postDelayed(requestRender, 10);
		}
    	
	};
    

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	log.d("GLFrameRenderer :: onSurfaceCreated");
		mEGLConfig = config;
        bSurfaceCreate = true;

		if(0 == PlayInfo.displayMode)
			displayMode = 9;
		else
			displayMode = 0;

        eyeMode = 0;
        
    	vDegrees = 0;
    	hDegrees = 0;
    	depth = 0;
        
        
    }
    
EGLContext _context = null;  
EGLDisplay _display = null;
EGLSurface _surface = null;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        log.d("GLFrameRenderer :: onSurfaceChanged()(" + width + ","  + height + ")");
		//GLRecorder.init(width, height, mEGLConfig/*Assign in onSurfaceCreated method*/);
		//GLRecorder.setRecordOutputFile("/sdcard/glrecord.mp4");     // Set output file path
		//GLRecorder.startRecording();
        
        //_display = EGL14.eglGetCurrentDisplay();
        //_surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
       
        
        
		mScreenWidth = width;
		mScreenHeight = height;


		//if (mScreenWidth < mScreenHeight)
		{
		//	mDrawWidth = mDrawHeight = mScreenWidth;
		}
		//else
		{
			mDrawWidth = mScreenWidth;
			mDrawHeight = mScreenHeight;
		}


		bSurfaceChanged = true;

		view_x = (mScreenWidth - mDrawWidth)/2;
		view_y = (mScreenHeight - mDrawHeight)/2;
		view_w = mDrawWidth;
		view_h = mDrawHeight;
		FHSDK.init(mDrawWidth, mDrawHeight);
		if (0 != hBuffer)
		{
			FHSDK.destroyBuffer(hBuffer);
		}
		
		hBuffer = FHSDK.createBuffer(DISPLAY_TYPE_YUV420P);

		if (0 != hWin)
		{
			FHSDK.destroyWindow(hWin);
		}

		hWin = FHSDK.createWindow(displayMode);

		FHSDK.bind(hWin, hBuffer);

		int i;
		for (i = 0; i < 4; i++)
		{
			if(displayMode == -1)
				break;

			if (0 == hWinMixMode[i])
			{
				if (1 == i)
					hWinMixMode[i] = FHSDK.createWindow(6);
				else if (2 == i)
					hWinMixMode[i] = FHSDK.createWindow(5);
				else
					hWinMixMode[i] = FHSDK.createWindow(i);
			}
			FHSDK.unbind(hWinMixMode[i]);
			FHSDK.bind(hWinMixMode[i], hBuffer);
		}



		depth = FHSDK.getMaxZDepth(hWin);

/**********************************/
		@SuppressWarnings("ResourceType")
        InputStream is = mContext.getResources().openRawResource(R.drawable.logo);
        Bitmap bitmapTmp;
        try 
        {
        	bitmapTmp = BitmapFactory.decodeStream(is);
        } 
        finally 
        {
            try 
            {
                is.close();
            } 
            catch(IOException e) 
            {
                e.printStackTrace();
            }
        }

		if (0 != hImageBuffer)
		{
			FHSDK.destroyBuffer(hImageBuffer);
		}
		hImageBuffer = FHSDK.createBuffer(DISPLAY_TYPE_IMAGE);
		if (0 != hImageWin)
		{
			FHSDK.destroyWindow(hImageWin);
		}
		hImageWin = FHSDK.createWindow(9);
		FHSDK.bind(hImageWin, hImageBuffer);
		
		imageWidth = bitmapTmp.getWidth();
		imageHeight = bitmapTmp.getHeight();

		ByteBuffer dst = ByteBuffer.allocate(imageWidth*imageHeight*4);
		bitmapTmp.copyPixelsToBuffer(dst);
		imageBuffer = dst.array();
		FHSDK.update(hImageBuffer, imageBuffer, imageWidth, imageHeight);
		
		bitmapTmp.recycle();
		bitmapTmp = null;
/**************************************/




    }
    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
        	int i;
			//GLRecorder.beginDraw();
			//GLRecorder.endDraw();
    		if (displayMode != FHSDK.getDisplayMode(hWin))
    		{
    			FHSDK.unbind(hWin);
    			FHSDK.destroyWindow(hWin);
    			hWin = FHSDK.createWindow(displayMode);
    			FHSDK.bind(hWin, hBuffer);
    		}

			FHSDK.setDisplayType(hWin, displayType);
    		FHSDK.setStandardCircle(hWin, circleX, circleY, circleR);
    		
    		if (!isDebugMode && yuv != null && bUpdated)// bUpdated : just update when data changed
    		{
    			FHSDK.update(hBuffer, yuv, mVideoWidth, mVideoHeight);
    			
    			bUpdated = false;
    			
    			if (bAdjust)
    			{
    				bAdjust = false;
    				Circle mCircle = (new Define()).new Circle();
    				FHSDK.adjustCircle(yuv, mVideoWidth, mVideoHeight, 30, mCircle);
    				
    				circleX = mCircle.x;
    				circleY = mCircle.y;
    				circleR = mCircle.r;
    				log.e("circleX =" + circleX + ", circleY = " + circleY + ", circler = " + circleR);
    			}
    			
    		}
    		else if (isDebugMode)
    		{}
    		//long before = System.currentTimeMillis();
    		FHSDK.clear();
    		//long after = System.currentTimeMillis();
    		//log.e("time = " + (after - before));
    		
    		if (bMixMode)//需要建立多个hWin
    		{
                int[] x = {view_x, view_x+ view_w/2, view_x, view_x+view_w/2};
                int[] y = {view_y, view_y, view_y + view_h/2, view_y + view_h/2};
                for (i = 0; i < 4; i++)
                {
                	FHSDK.setStandardCircle(hWinMixMode[i], circleX, circleY, circleR);
	                FHSDK.viewport(x[i], y[i], view_w/2, view_h/2);
	                if (1 == i)
	                {
	                	FHSDK.setImagingType(hWinMixMode[i], 1);
	                	//FHSDK.setDebugMode(hWinMixMode[i], mRgbRes[13].rgb , mRgbRes[13].width, mRgbRes[13].height);
	                }
	                else
	                {
	                	//FHSDK.setDebugMode(hWinMixMode[i], mRgbRes[14].rgb , mRgbRes[14].width, mRgbRes[14].height);
	                }
	                FHSDK.draw(hWinMixMode[i]);
                }
    			//return;
    		}
    		else 
    		{
				if (0 == displayMode || 6 == displayMode)
				{
					if (0 == eyeMode)
					{
						FHSDK.viewport(view_x, view_y, view_w, view_h);
						//if (lastVDegrees != vDegrees || lastHDegrees != hDegrees || lastDepth != depth)
						{

							//FHSDK.eyeLookAt(hWin, vDegrees, hDegrees, depth);
						float va = 0;
						if(mScreenWidth > mScreenHeight)
							va = FHSDK.getViewAngle(hWin);//(float)(Math.atan((double)((float)mScreenHeight/(float)mScreenWidth))*2.0f*180.0f/Math.PI);
						else
							va = 2*(float)(180.0f/Math.PI)*(float)Math.atan((float)mScreenHeight/(float)mScreenWidth*(float)Math.tan(FHSDK.getViewAngle(hWin)/2*(float)(Math.PI/180.0f)));
						//float limit = 90 - va/2;
						if (vDegrees < -180.0f)
							vDegrees = -180.0f;
						if (vDegrees > 180.0f)
							vDegrees = 180.0f;
						if (displayMode == 6) {
							if (hDegrees > 180.0f)
								hDegrees = 180.0f;
							if (hDegrees < -180.0f)
								hDegrees = -180.0f;
						}
						
						va += Math.abs(vDegrees)/3.0f;

						//log.e("[" + mScreenHeight + "," + mScreenWidth + "]" + vDegrees + "," + hDegrees + "," + va);
						depth = FHSDK.getMaxZDepth(hWin);
						float step = ((-1) - depth)/90.0f;

						depth = FHSDK.getMaxZDepth(hWin) + Math.abs(vDegrees)*step;	
						if(depth > -1)
							depth = -1;
						
						FHSDK.eyeLookAtEx(hWin, vDegrees, hDegrees, depth, va);
							
						}
						
						FHSDK.draw(hWin);
					}
					else if (1 == eyeMode)
					{
		                int[] x = {view_x, view_x+ view_w/2, view_x, view_x+view_w/2};
		                int[] y = {view_y, view_y, view_y + view_h/2, view_y + view_h/2};
		                for (i = 0; i < 4; i++)
		                {
		                	FHSDK.viewport(x[i], y[i], view_w/2, view_h/2);
							//if (lastHDegrees != hDegrees)
							{
								FHSDK.eyeLookAt(hWin, FHSDK.getMaxVDegress(hWin), hDegrees + 90*i, 0);
							}
		                	
			        	    FHSDK.draw(hWin);
		                }
					}
					else if (2 == eyeMode)
					{
		                int[] x = {view_x, view_x+ view_w/2, view_x, view_x+view_w/2};
		                int[] y = {view_y, view_y, view_y + view_h/2, view_y + view_h/2};
		                for (i = 0; i < 4; i++)
		                {
		                	FHSDK.viewport(x[i], y[i], view_w/2, view_h/2);
		                	FHSDK.eyeLookAt(hWin, FHSDK.getMaxVDegress(hWin), hEyeDegrees[i], 0);
		                	FHSDK.draw(hWin);
		                }
					}
					else if (3 == eyeMode)
					{
		                int[] x = {view_x, view_x+ view_w/2};
		                int[] y = {view_y, view_y};
		                for (i = 0; i < 2; i++)
		                {
		                	FHSDK.viewport(x[i], y[i], view_w/2, view_h);
		                	//if (lastVDegrees != vDegrees || lastHDegrees != hDegrees)
		                	{
		                		FHSDK.eyeLookAt(hWin, vDegrees, hDegrees, 0);
								lastVDegrees = vDegrees;
								lastHDegrees = hDegrees;
		                	}
		                	FHSDK.draw(hWin);
		                }
					}
				}
				else
				{
					FHSDK.viewport(view_x, view_y, view_w, view_h);
					//if (lastHOffset != hOffset)
					{
						FHSDK.expandLookAt(hWin, hOffset);
						//lastHOffset = hOffset;
					}
					FHSDK.draw(hWin);
				}
    		}

			FHSDK.viewport(view_w - 155, 0, 150, 50);
			FHSDK.draw(hImageWin);

    	    if (bSnapshot)
    	    {
    	    	bSnapshot = false;
//    	    	if (0 == displayMode)
//    	    	{
//	    	    	FHSDK.setDisplayMode(hWin[0], 5);
//					FHSDK.clear();
//	                FHSDK.viewport(view_x, view_y, view_w, view_h);
//	    	    	FHSDK.expandLookAt(hWin[0], hOffset);
//	    	    	FHSDK.draw(hWin[0]);
//	    	    	frameBuf = FHSDK.snapshot(view_x, view_y, view_w, view_h);
//	    	    	mSnapshotThread.start();
//	    	    	
//	                FHSDK.clear();
//	    	    	FHSDK.setDisplayMode(hWin[0], displayMode);
//	    	    	FHSDK.viewport(view_x, view_y, view_w, view_h);
//	    	    	FHSDK.draw(hWin[0]);
//    	    	}
//    	    	else
//    	    	{
	    	    	frameBuf = FHSDK.snapshot(view_x, view_y, view_w, view_h, true);
	    	    	mSnapshotThread.start();
//    	    	}
    	    }
    	    //EGL14.eglSwapBuffers(_display, _surface);
    	    return;
		}
    }
    
    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */

	public YUVDataCallBackInterface dataFun = new YUVDataCallBackInterface(){
	    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
	        synchronized (this) {
	  
		    	if (y != null)
		    	{
		    		System.arraycopy(ydata, 0, y, 0, ydata.length);
		    		System.arraycopy(udata, 0, u, 0, udata.length);
		    		System.arraycopy(vdata, 0, v, 0, vdata.length);
		    		bUpdated = true;
		    	}
	        }

	        // request to render
	        //mTargetSurface.requestRender();
	    }


	    /**
	     * this method will be called from native code, it happens when the video is about to play or
	     * the video size changes.
	     */
	    public void update(int w, int h) {
	        if (w > 0 && h > 0) {
	            if (mScreenWidth > 0 && mScreenHeight > 0) {}

	            if (w != mVideoWidth && h != mVideoHeight) {
	                mVideoWidth = w;
	                mVideoHeight = h;
	                int yarraySize = w * h;
	                int uvarraySize =yarraySize / 4;
					int yuvarraySize =w * h *3/2;
	                synchronized (this) {
	                	//y = new byte[yarraySize];
	                	//u = new byte[uvarraySize];
	                	//v = new byte[uvarraySize];
	                	yuv = new byte[yuvarraySize];
	                	
	                }
	            }
	        }

	    }


		@Override
		public void update(byte[] yuvdata) {
			// TODO Auto-generated method stub
			if (null == yuvdata || null == yuv)
				return;

    		System.arraycopy(yuvdata, 0, yuv, 0, yuvdata.length);
    		bUpdated = true;
	    	
		}
    };

    /**
     * this method will be called from native code, it's used for passing play state to activity.
     */
//    public void updateState(int state) {
//    	log.d("updateState E = " + state);
//        if (mParentAct != null) {
//           // mParentAct.onReceiveState(state);
//        }
//        log.d("updateState X");
//    }
	class SnapshotThread implements Runnable {
		private boolean isShoting = false;
	    public void start() {  
	        new Thread(this).start();
	    }  
	    public boolean isShoting() {  
	    	return isShoting;
	    }  
		
	    public void run() {
	    	synchronized (this) {
				/*
	    		int i, j;
	    		int offset1, offset2;
				
	    		byte [] outBuffer = new byte[frameBuf.length];
	    		for (i = 0; i < view_h; i++){
	    			offset1 = i * view_w*4;
	    			offset2 = (view_h - i - 1) * view_w*4;
	    			for (j = 0; j < view_w*4; j++){
	    				outBuffer[offset2 + j] = frameBuf[offset1 + j];
	    			}
	    		}
	    		*/

        	    ByteBuffer btBuf = ByteBuffer.wrap(frameBuf);
        	    btBuf.order(ByteOrder.LITTLE_ENDIAN);
        	    btBuf.rewind();
        	    File sd = Environment.getExternalStorageDirectory();
        	    String tmpfile = sd.getPath() + "/" + System.currentTimeMillis()+".jpg";

                BufferedOutputStream bos = null;
                try {
                    try {
						bos = new BufferedOutputStream(new FileOutputStream(tmpfile));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    Bitmap bmp = Bitmap.createBitmap(view_w, view_h, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(btBuf);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                    bmp.recycle();
                } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
                    if (bos != null)
						try {
							bos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                }
	    	}
	    }
	}

    public void setvelocityX(float velocityX)
    {
    	this.velocityX = velocityX;
    	if (Math.abs(velocityX) > 3000)
    		scrollStep = STEP_BASE_FAST;
    	else
    		scrollStep = STEP_BASE_SLOW;
    }
    
    public void setvelocityY(float velocityY)
    {
    	this.velocityY = velocityY;
    	if (Math.abs(velocityY) > 3000)
    		scrollStep = STEP_BASE_FAST;
    	else
    		scrollStep = STEP_BASE_SLOW;
    }
	
	
    private void loadRgbFile()
    {
 
		InputStream is = null;
		try {
			is = mContext.getAssets().open("office_topView_956_919_yes.rgb");
			int length = is.available();
			mRgbRes[0] = new RGBRes();
			mRgbRes[0].rgb = new byte[length]; 
			mRgbRes[0].width = 956;
			mRgbRes[0].height = 919;
			is.read(mRgbRes[0].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("hik_wallHanging_1124_1124_no.rgb");
			int length = is.available();
			mRgbRes[1] = new RGBRes();
			mRgbRes[1].rgb = new byte[length]; 
			mRgbRes[1].width = 1124;
			mRgbRes[1].height = 1124;
			is.read(mRgbRes[1].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("hik_wallHanging2_1124_844_no.rgb");
			int length = is.available();
			mRgbRes[2] = new RGBRes();
			mRgbRes[2].rgb = new byte[length]; 
			mRgbRes[2].width = 1124;
			mRgbRes[2].height = 844;
			is.read(mRgbRes[2].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("hik_wallHanging2_1124_632_no.rgb");
			int length = is.available();
			mRgbRes[3] = new RGBRes();
			mRgbRes[3].rgb = new byte[length]; 
			mRgbRes[3].width = 1124;
			mRgbRes[3].height = 632;
			is.read(mRgbRes[3].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		try {
			is = mContext.getAssets().open("A_correct_500_500_no.rgb");
			int length = is.available();
			mRgbRes[4] = new RGBRes();
			mRgbRes[4].rgb = new byte[length]; 
			mRgbRes[4].width = 500;
			mRgbRes[4].height = 500;
			is.read(mRgbRes[4].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		try {
			is = mContext.getAssets().open("B_correct_800_286_no.rgb");
			int length = is.available();
			mRgbRes[5] = new RGBRes();
			mRgbRes[5].rgb = new byte[length]; 
			mRgbRes[5].width = 800;
			mRgbRes[5].height = 286;
			is.read(mRgbRes[5].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			is = mContext.getAssets().open("C_correct_800_286_no.rgb");
			int length = is.available();
			mRgbRes[6] = new RGBRes();
			mRgbRes[6].rgb = new byte[length]; 
			mRgbRes[6].width = 800;
			mRgbRes[6].height = 286;
			is.read(mRgbRes[6].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			is = mContext.getAssets().open("ja_topView_1008_1002_yes.rgb");
			int length = is.available();
			mRgbRes[7] = new RGBRes();
			mRgbRes[7].rgb = new byte[length]; 
			mRgbRes[7].width = 1008;
			mRgbRes[7].height = 1002;
			is.read(mRgbRes[7].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			is = mContext.getAssets().open("ja_wallHanging2_1580_1080_no.rgb");
			int length = is.available();
			mRgbRes[8] = new RGBRes();
			mRgbRes[8].rgb = new byte[length]; 
			mRgbRes[8].width = 1580;
			mRgbRes[8].height = 1080;
			is.read(mRgbRes[8].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			is = mContext.getAssets().open("ja_wallHanging_1000_938_yes.rgb");
			int length = is.available();
			mRgbRes[9] = new RGBRes();
			mRgbRes[9].rgb = new byte[length]; 
			mRgbRes[9].width = 1000;
			mRgbRes[9].height = 938;
			is.read(mRgbRes[9].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		try {
			is = mContext.getAssets().open("xyz2_correct_800_500_no.rgb");
			int length = is.available();
			mRgbRes[10] = new RGBRes();
			mRgbRes[10].rgb = new byte[length]; 
			mRgbRes[10].width = 800;
			mRgbRes[10].height = 500;
			is.read(mRgbRes[10].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("xyz_correct_400_400_no.rgb");
			int length = is.available();
			mRgbRes[11] = new RGBRes();
			mRgbRes[11].rgb = new byte[length]; 
			mRgbRes[11].width = 400;
			mRgbRes[11].height = 400;
			is.read(mRgbRes[11].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("fh_wallHanging_1440_1440.rgb");
			int length = is.available();
			mRgbRes[12] = new RGBRes();
			mRgbRes[12].rgb = new byte[length]; 
			mRgbRes[12].width = 1440;
			mRgbRes[12].height = 1440;
			is.read(mRgbRes[12].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			is = mContext.getAssets().open("c815_wallHanging_1064_672.rgb");
			int length = is.available();
			mRgbRes[13] = new RGBRes();
			mRgbRes[13].rgb = new byte[length]; 
			mRgbRes[13].width = 1064;
			mRgbRes[13].height = 672;
			is.read(mRgbRes[13].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			is = mContext.getAssets().open("home.rgb");
			int length = is.available();
			mRgbRes[14] = new RGBRes();
			mRgbRes[14].rgb = new byte[length]; 
			mRgbRes[14].width = 764;
			mRgbRes[14].height = 764;
			is.read(mRgbRes[14].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("s45_2_wallHanging_1160_1160.rgb");
			int length = is.available();
			mRgbRes[15] = new RGBRes();
			mRgbRes[15].rgb = new byte[length]; 
			mRgbRes[15].width = 1160;
			mRgbRes[15].height = 1160;
			is.read(mRgbRes[15].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			is = mContext.getAssets().open("yuv420spData.yuv");
			int length = is.available();
			mRgbRes[16] = new RGBRes();
			mRgbRes[16].rgb = new byte[length]; 
			mRgbRes[16].width = 1024;
			mRgbRes[16].height = 1024;
			is.read(mRgbRes[16].rgb);
			is.close();  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
    }
}
