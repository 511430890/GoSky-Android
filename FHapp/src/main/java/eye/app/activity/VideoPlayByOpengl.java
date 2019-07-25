package eye.app.activity;

import java.io.File;

import com.app.util.*;
import com.app.util.log;
import com.app.view.VideoPlayView;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;


public class VideoPlayByOpengl extends OpenglActivity {
	private long mExitTime;
	public Context mContext;
	private VideoPlayView mPreviewView;
	protected void onCreate(Bundle savedInstanceState) {

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//  需放在setContentView()之前

		super.onCreate(savedInstanceState);

		mContext = this;
		MyApplication.getInstance().addActivity(this);


		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		mPreviewView = new VideoPlayView(this);
		mPreviewView.layoutInit(this.mLayout);

		playInit();

	}

	public void playInit()
	{
		createFilePath();
		FHSDK.setPlayInfo(new PlayInfo());

		FHSDK.startPlay();
		
	}
	private void createFilePath()
	{
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File sd = Environment.getExternalStorageDirectory();
			String path = sd.getPath() + mPreviewView.getSettingPath();
			File file = new File(path);
			if (!file.exists())
			{
				file.mkdir();
			}
			FHSDK.setShotPath(file.getAbsolutePath());
		}
		else
			ActivtyUtil.openToast(mContext, getString(R.string.str_nofoundSDCard));
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				//Object mHelperUtils;
				ActivtyUtil.openToast(mContext, (String)getText(R.string.str_doubleClickToExit));
				mExitTime = System.currentTimeMillis();
				return true;
			}
		}else if (keyCode == KeyEvent.KEYCODE_MENU){
				mPreviewView.setLayoutMenuShow();
		}

		return super.onKeyDown(keyCode, event);
	}
	protected void onDestroy()
	{
		FHSDK.stopPlay();

		mPreviewView.layoutUnInit(this.mLayout);
		mPreviewView = null;
		super.onDestroy();
	}
}



