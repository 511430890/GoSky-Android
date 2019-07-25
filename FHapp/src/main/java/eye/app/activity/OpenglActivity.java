package eye.app.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.gl.MyGLSurfaceView;
import com.android.opengles.GLFrameRenderer;
import com.android.opengles.GLFrameSurface;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;
import com.research.GLRecorder.GLRecorder;


public class OpenglActivity extends Activity {
	private String TAG = "OpenglActivity";
	protected static ViewGroup mLayout;
	//
	private GLFrameSurface glFrameSurface;
	public GLFrameRenderer mFrameRender;
	//
	private MyGLSurfaceView glSurface;

	public Context mContext;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.mediacodec);
		mContext = this;
		DisplayMetrics dm = new DisplayMetrics();
		dm = getApplicationContext().getResources().getDisplayMetrics();

		if(false/*PlayInfo.decodeType == FHSDK.DECODE_TYPE_MEDIACODEC2OPENGL*/)
		{

			glSurface = new MyGLSurfaceView(this);
	        mLayout = new FrameLayout(this);
	        mLayout.addView(glSurface);
		}
		else
		{
			glFrameSurface = new GLFrameSurface(this);
			glFrameSurface.setEGLContextClientVersion(2);
			glFrameSurface.setEGLConfigChooser(GLRecorder.getEGLConfigChooser());
			mFrameRender = new GLFrameRenderer(mContext, glFrameSurface, dm);
			glFrameSurface.setRenderer(mFrameRender);
			//mSurface = new SurfaceView(this);
			mLayout = new FrameLayout(this);
			mLayout.addView(glFrameSurface);
		}

		// mLayout.setLayoutParams(params);

		setContentView(mLayout);

		MyApplication.getInstance().addActivity(this);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//
	}

	protected void onResume() {
		Log.v(TAG, "onResume()");
		super.onResume();
		//glFrameSurface.bringToFront();// 设置glSurface为最顶层，此处设置后导致其他layout被覆盖，去掉
		if (null != glFrameSurface)
			glFrameSurface.rigisterListener();
	}
	protected void onPause() {
		Log.v(TAG, "onPause()");

		super.onPause();
		// glFrameSurface.onPause();
		if (null != glFrameSurface)
			glFrameSurface.unRigisterListener();
	}
	protected void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
	}

}
