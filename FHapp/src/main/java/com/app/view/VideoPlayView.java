package com.app.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import eye.app.activity.BuildConfig;
import eye.app.activity.MoreCfg;
import eye.app.activity.R;
import com.android.opengles.GLFrameRenderer;
import com.android.opengles.GLFrameSurface;
import com.app.util.ActivtyUtil;
import com.app.util.ETButton;
import com.app.util.IndicatorView;
import com.app.util.log;
import com.fh.lib.Define;
import com.fh.lib.Define.BCSS;
import com.fh.lib.Define.CbDataInterface;
import com.fh.lib.Define.PBRecTime;
import com.fh.lib.Define.SerialDataCallBackInterface;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

public class VideoPlayView  {

	private static final int NOTIFY_TYPE_SHOT_FileName = 0x00;
	private static final int NOTIFY_TYPE_SHOT_Fail     = 0x01;
	private static final int NOTIFY_TYPE_STREAM_INFO   = 0x02;
	private static final int NOTIFY_TYPE_PTS           = 0x03;

	private static int DEVICE_TYPE_FH8610 = 0x01;
	private static int DEVICE_TYPE_FH8620 = 0x02;
	private static int DEVICE_TYPE_FH8810 = 0x03;
	private static final String REC_PATH = "/FHVideo/";
	private static final int[] audioType = {0,1};


	private static final int NOTIFY_QUIT = 0;
	private static final int UPDATE_PROGRESS = 1;
	private static final int MENU_GONE = 2;
	private static final int NOTIFY_UPDATE_STREAM_INFO = 3;
	private static final int SHOT_FileName = 2001;
	private static final int SHOT_Fail = 2002;

	private static final int SEEKBAR_MAX_NUM = 10000;
	private static final int SPEED_1   = 0;
	private static final int SPEED_2   = 1;
	private static final int SPEED_4   = 2;
	private static final int SPEED_8   = 3;
	private static final int SPEED_16  = 4;
	private static final int SPEED_1_2 = 7;
	private static final int SPEED_1_4 = 8;
	private static final int SPEED_1_8 = 9;
	private static final int SPEED_1_16 = 10;
	private static String[] strPBSpeed = {"","x2","x4","x8", "x16","","","1/2", "1/4", "1/8", "1/16"};
	private int PBCurrentSpeed = SPEED_1;
	private Handler handler, hDevNotify, hRecTime;


	private static TalkThread mTalk = null;

	private boolean isPBPause = false;
	private boolean isStopSendMsg = false;
	private boolean isPBAudioOn = false;
	private boolean isRecOn = false;
	private boolean isAudioOpened = false;
	private boolean isTalkOpened = false;
	private int secondCount = 0;
	private long mExitTime;

	public static long PBStartTime = 0;
	public static long PBStopTime = 0;
	private long PBVideoLen = 0;
	private long PBVideoCurLen = 0;
	private long CurrentTime = PBStartTime;

	public static int talkFormat = 0;
	public static int talkSample = 0;

	//preview
	private View view;
	private LayoutInflater inflater;
	private RelativeLayout layoutMenu, layoutEyeMode;
	private LinearLayout layoutEyeMenu, btnEyeMenuParent;
	private ETButton btnRemoteRec, btnRemoteShot, btnLocateRec, btnLocateShot, btnAudio, btnTalk, btnSerial, btnBCSS, btnEyeMenu;
	//locate playback
	private SeekBar mSeekBar;
	private TextView mPBCurrentTime, mPBStopTime, tvPBSpeed, tvPBStatus, tvRecTime;
	private ImageView btnPBPlay, btnPBSpeedDown, btnPBSpeedUp;
	private ImageButton btPBPlay, btPBFrame, btPBSpeedDown, btPBSpeedUp, btPBAudio;
	private RelativeLayout controlLayout;

	private BCSS bcssObj, bcssDefObj;
	private SeekBar seekBarBrightness,seekBarContrast, seekBarSaturation, seekBarSharpness;
	private TextView tvBrightnessVal, tvContrastVal, tvSaturationVal, tvSharpnessVal;
	private EditText edtSerialInput;
	private long serialHandle;
	private TextView tvStreamInfo;
	public Context mContext;
	private MoreCfg moreCfgObj = new MoreCfg();

	private GLFrameRenderer mFrameRender;
	private ETButton btnShot, btnCorrect, btnEyeType;
	private ETButton btnTopViewEye;         //顶视鱼眼
	private ETButton btnTopViewEye4Screen;  //顶视4屏
	private ETButton btnTopViewEyeVR;       //顶视VR
	private ETButton btnTopViewCylinder;    //顶视圆筒
	private ETButton btnTopViewSingleExpand;//顶视单展
	private ETButton btnTopViewDoubleExpand;//顶视双展
	private ETButton btnHangingEye;         //壁挂鱼眼
	private ETButton btnHangingCorrect;     //壁挂矫正
	private ETButton btnHangingEyeVR;       //壁挂VR
	private ETButton btnHangingWide;        //壁挂广角
	private ETButton btnHangingWideCorrect; //壁挂广矫
	private ETButton btnPlotEye;            //标图鱼眼
	private ETButton btnPlotCorrect;        //标图矫正
	private ETButton btnMultipleMode;       //混杂模式
	private Chronometer mChronometer;

	private LinearLayout eyeviewMenu;
	private String[] ModeList;
	private IndicatorView indicatorView;
	public Define myDefine;
	Define.SerialPortCfg SerialCfg;
	public VideoPlayView(Context mContext){
		this.mContext =  mContext;
	}

	public void layoutUnInit(ViewGroup mLayout)
	{
		//mLayout.removeView(mLayout);
		stopTalkThread();
	}
	public void layoutInit(ViewGroup mLayout)
	{
		inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (FHSDK.PLAY_TYPE_REMOTE_PLAYBACK == PlayInfo.playType)
		{
			view = inflater.inflate(R.layout.playback_remote_video, null);
			mLayout.addView(view);


			mSeekBar = (SeekBar)view.findViewById(R.id.SeekBarBrightNess);
			mSeekBar.setMax(SEEKBAR_MAX_NUM);
			mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

			mPBCurrentTime = (TextView)view.findViewById(R.id.pbCurrentTime);
			mPBStopTime = (TextView)view.findViewById(R.id.pbStopTime);
			mPBCurrentTime.setText(FHSDK.timeConvert(PlayInfo.userID, PBStartTime));
			mPBStopTime.setText(FHSDK.timeConvert(PlayInfo.userID, PBStopTime));

			tvPBSpeed = (TextView)view.findViewById(R.id.tvPBSpeed);
			tvPBStatus = (TextView)view.findViewById(R.id.tvPBStatus);
			btPBPlay = (ImageButton)view.findViewById(R.id.btPBPlay);
			btPBFrame = (ImageButton)view.findViewById(R.id.btPBFrame);
			btPBSpeedDown = (ImageButton)view.findViewById(R.id.btPBSpeedDown);
			btPBSpeedUp = (ImageButton)view.findViewById(R.id.btPBSpeedUp);
			btPBAudio = (ImageButton)view.findViewById(R.id.btPBAudio);

			btPBPlay.setOnClickListener(pbPlayListener);
			btPBFrame.setOnClickListener(pbFrameListener);
			btPBSpeedDown.setOnClickListener(pbSpeedDownListener);
			btPBSpeedUp.setOnClickListener(pbSpeedUpListener);
			btPBAudio.setOnClickListener(pbAudioListener);

		}
		else if (FHSDK.PLAY_TYPE_PREVIEW == PlayInfo.playType || FHSDK.PLAY_TYPE_UDP == PlayInfo.playType)
		{

			view = inflater.inflate(R.layout.layout_play_main, null);
			//view = inflater.inflate(R.layout.control_panel_top_menubar, null);

			// TODO: //去掉按键布局
			mLayout.addView(view);


			//GLFrameSurface s = (GLFrameSurface) mLayout.getChildAt(0);
			//s.setOnClickListener(surfaceTouchListener);

			btnRemoteRec = (ETButton)view.findViewById(R.id.btnRemoteRec);
			btnRemoteShot = (ETButton)view.findViewById(R.id.btnRemoteShot);
			btnLocateRec = (ETButton)view.findViewById(R.id.btnLocateRec);
			btnLocateShot = (ETButton)view.findViewById(R.id.btnLocateShot);
			btnAudio = (ETButton)view.findViewById(R.id.btnAudio);
			btnTalk = (ETButton)view.findViewById(R.id.btnTalk);
			btnSerial = (ETButton)view.findViewById(R.id.btnSerial);
			btnBCSS  = (ETButton)view.findViewById(R.id.btnBCSS);
			btnEyeMenu  = (ETButton)view.findViewById(R.id.btnEyeMenu);

			btnEyeType = (ETButton)view.findViewById(R.id.btnEyeType);


			btnShot = (ETButton)view.findViewById(R.id.btnEyeShot);
			btnCorrect = (ETButton)view.findViewById(R.id.btnCorrect);


			btnTopViewEye = (ETButton)view.findViewById(R.id.btnTopViewEye);
			btnTopViewEye4Screen = (ETButton)view.findViewById(R.id.btnTopViewEye4Screen);
			btnTopViewEyeVR = (ETButton)view.findViewById(R.id.btnTopViewEyeVR);
			btnTopViewCylinder = (ETButton)view.findViewById(R.id.btnTopViewCylinder);
			btnTopViewSingleExpand = (ETButton)view.findViewById(R.id.btnTopViewSingleExpand);
			btnTopViewDoubleExpand = (ETButton)view.findViewById(R.id.btnTopViewDoubleExpand);
			btnHangingEye = (ETButton)view.findViewById(R.id.btnHangingEye);
			btnHangingCorrect = (ETButton)view.findViewById(R.id.btnHangingCorrect);
			btnHangingEyeVR = (ETButton)view.findViewById(R.id.btnHangingEyeVR);
			btnHangingWide = (ETButton)view.findViewById(R.id.btnHangingWide);
			btnHangingWideCorrect = (ETButton)view.findViewById(R.id.btnHangingWideCorrect);
			btnPlotEye = (ETButton)view.findViewById(R.id.btnPlotEye);
			btnPlotCorrect = (ETButton)view.findViewById(R.id.btnPlotCorrect);
			btnMultipleMode = (ETButton)view.findViewById(R.id.btnMultipleMode);

			tvRecTime = (TextView)view.findViewById(R.id.tvRecTime);
			tvRecTime.setVisibility(View.INVISIBLE);


			btnRemoteRec.setOnClickListener(btnRemoteRecSetListener);
			btnRemoteShot.setOnClickListener(btnRemoteShotSetListener);
			btnLocateRec.setOnClickListener(btnLocateRecSetListener);
			btnLocateShot.setOnClickListener(btnLocateShotSetListener);
			btnAudio.setOnClickListener(btnAudioSetListener);
			btnTalk.setOnClickListener(btnTalkSetListener);
			btnSerial.setOnClickListener(btnSerialSetListener);
			btnBCSS.setOnClickListener(btnBCSSListener);
			btnEyeMenu.setOnClickListener(btnEyeMenuListener);
			btnEyeMenu.setTag(0);

			btnEyeType.setOnClickListener(btnEyeTypeClickListener);
			btnShot.setOnClickListener(bntShotClickListener);
			btnCorrect.setOnClickListener(btnCorrectClickListener);


			btnTopViewEye.setOnClickListener(btnEyeModeClickListener);
			btnTopViewEye4Screen.setOnClickListener(btnEyeModeClickListener);
			btnTopViewEyeVR.setOnClickListener(btnEyeModeClickListener);
			btnHangingEyeVR.setOnClickListener(btnEyeModeClickListener);
			btnTopViewCylinder.setOnClickListener(btnEyeModeClickListener);
			btnTopViewSingleExpand.setOnClickListener(btnEyeModeClickListener);
			btnTopViewDoubleExpand.setOnClickListener(btnEyeModeClickListener);
			btnHangingEye.setOnClickListener(btnEyeModeClickListener);
			btnHangingCorrect.setOnClickListener(btnEyeModeClickListener);
			btnHangingWide.setOnClickListener(btnEyeModeClickListener);
			btnHangingWideCorrect.setOnClickListener(btnEyeModeClickListener);
			btnPlotEye.setOnClickListener(btnEyeModeClickListener);
			btnPlotCorrect.setOnClickListener(btnEyeModeClickListener);
			btnMultipleMode.setOnClickListener(btnEyeModeClickListener);



			//tvBrightnessVal, tvContrastVal, tvSaturationVal, tvSharpnessVal;

			tvStreamInfo = (TextView)view.findViewById(R.id.tvPreStreamInfo);
			mChronometer = view.findViewById(R.id.control_panel_chronometer);

			bcssObj = (new Define()).new BCSS();
			bcssDefObj = (new Define()).new BCSS();

			layoutMenu=(RelativeLayout)view.findViewById(R.id.layout_top);
			layoutMenu.setVisibility(View.VISIBLE);

			layoutEyeMenu = (LinearLayout)view.findViewById(R.id.layout_right);
			layoutEyeMenu.setVisibility(View.GONE);
			if(PlayInfo.displayMode == 0) {
				btnEyeMenuParent = (LinearLayout) view.findViewById(R.id.btnEyeMenuParent);
				btnEyeMenuParent.setVisibility(View.GONE);
			}


			layoutEyeMode = (RelativeLayout)view.findViewById(R.id.layout_bottom);
			layoutEyeMode.setVisibility(View.GONE);

		}
		else
		{
			view = inflater.inflate(R.layout.playback_locate_video, null);
			mLayout.addView(view);

			mSeekBar = (SeekBar)view.findViewById(R.id.seekBarProgress);
			mPBCurrentTime = (TextView)view.findViewById(R.id.pbCurrentTime);
			mPBStopTime = (TextView)view.findViewById(R.id.pbStopTime);
			tvPBSpeed = (TextView)view.findViewById(R.id.tvPBSpeed);
			btnPBPlay = (ImageView)view.findViewById(R.id.btnPBPlay);
			btnPBSpeedDown = (ImageView)view.findViewById(R.id.btnPBSpeedDown);
			btnPBSpeedUp = (ImageView)view.findViewById(R.id.btnPBSpeedUp);

			mSeekBar.setMax(SEEKBAR_MAX_NUM);
			mSeekBar.setOnSeekBarChangeListener(locateSeekBarChangeListener);
			btnPBPlay.setOnClickListener(locatePBPlayListener);
			btnPBSpeedDown.setOnClickListener(locatePBSpeedDownListener);
			btnPBSpeedUp.setOnClickListener(locatePBSpeedUpListener);
			layoutMenu=(RelativeLayout)view.findViewById(R.id.control);
			tvStreamInfo = (TextView)view.findViewById(R.id.tvPBStreamInfo);

		}
		createHandler();
		FHSDK.registerNotifyCallBack(dataFun);
	}

	public void senddata(String s){

		int serialPort = 1;
		int serialIndex = 1;
		int transMode = 1; // 0 tcp  1 udp
		serialHandle = FHSDK.startSerialEx(PlayInfo.userID, serialPort, serialIndex, transMode, true, serialFun);
		if (0 == serialHandle)
		{
			ActivtyUtil.openToast(mContext,"创建句柄失败");
			return;
		}
		byte[] sendData1 = new byte[0];
		try {
			sendData1 = HexStringToBytes(s);
		} catch (Exception e) {
			e.printStackTrace();

		}

		myDefine = new Define();
		SerialCfg =  myDefine.new SerialPortCfg();
        FHSDK.getSerialPortConfig(PlayInfo.userID, SerialCfg);
		SerialCfg.baudRate=115200;
		FHSDK.setSerialPortConfig(PlayInfo.userID, SerialCfg);
		FHSDK.sendSerial(serialHandle, sendData1, sendData1.length);

	}

	private View.OnClickListener btnEyeTypeClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (0 == FHSDK.getDisplayType(mFrameRender.hWin))
			{
				mFrameRender.displayType = 1;
				v.setBackgroundResource(R.drawable.btn_eye_type_below);
			}
			else
			{
				mFrameRender.displayType = 0;
				v.setBackgroundResource(R.drawable.btn_eye_type_top);
			}
		}

	};
	private View.OnClickListener btnHideClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.btnHide)
			{
				layoutEyeMenu.setVisibility(View.GONE);
				layoutEyeMode.setVisibility(View.GONE);
			}
			else
			{
				layoutEyeMenu.setVisibility(View.GONE);
				layoutEyeMode.setVisibility(View.GONE);
			}
		}

	};


	public View.OnClickListener bntShotClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			mFrameRender.bSnapshot = true;
		}
	};

	public View.OnClickListener btnModeClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(0 == (Integer) v.getTag())
			{
				layoutEyeMode.setVisibility(View.VISIBLE);
				v.setTag(1);
			}else
			{
				layoutEyeMode.setVisibility(View.GONE);
				v.setTag(0);
			}
		}
	};


	public View.OnClickListener btnEyeModeClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//mFrameRender.ctrlIndex = position;
			mFrameRender.bMixMode = false;
			FHSDK.setImagingType(mFrameRender.hWin, 0);
			//FHSDK.setStandardCircle(mFrameRender.hWin, 0.0f, 0.0f, 0.0f);
			mFrameRender.eyeMode = 0;
			mFrameRender.vDegrees = 0;
			mFrameRender.hDegrees = 0;
			int i = v.getId();
			if (i == R.id.btnTopViewEye) {
				mFrameRender.displayMode = 0;
				mFrameRender.depth = FHSDK.getMaxZDepth(mFrameRender.hWin);

			} else if (i == R.id.btnTopViewEye4Screen) {
				mFrameRender.displayMode = 0;
				mFrameRender.eyeMode = 2;

				mFrameRender.hEyeDegrees[0] = 0;
				mFrameRender.hEyeDegrees[1] = 90;
				mFrameRender.hEyeDegrees[2] = 180;
				mFrameRender.hEyeDegrees[3] = 270;

			} else if (i == R.id.btnTopViewEyeVR) {
				mFrameRender.displayMode = 0;
				mFrameRender.eyeMode = 3;
				mFrameRender.vDegrees = FHSDK.getMaxVDegress(mFrameRender.hWin);

			} else if (i == R.id.btnTopViewCylinder) {
				mFrameRender.displayMode = 5;

			} else if (i == R.id.btnTopViewSingleExpand) {
				mFrameRender.displayMode = 3;

			} else if (i == R.id.btnTopViewDoubleExpand) {
				mFrameRender.displayMode = 4;

			} else if (i == R.id.btnHangingEye) {
				mFrameRender.displayMode = 6;
				mFrameRender.depth = FHSDK.getMaxZDepth(mFrameRender.hWin);
				//FHSDK.setImagingType(mFrameRender.hWin, 1);

			} else if (i == R.id.btnHangingEyeVR) {
				mFrameRender.displayMode = 6;
				mFrameRender.eyeMode = 3;
				//mFrameRender.vDegrees = FHSDK.getMaxVDegress(mFrameRender.hWin);

			} else if (i == R.id.btnHangingCorrect) {
				mFrameRender.displayMode = 7;

			} else if (i == R.id.btnHangingWide) {
				FHSDK.setImagingType(mFrameRender.hWin, 1);
				mFrameRender.displayMode = 6;
				mFrameRender.depth = FHSDK.getMaxZDepth(mFrameRender.hWin);


			} else if (i == R.id.btnHangingWideCorrect) {
				FHSDK.setImagingType(mFrameRender.hWin, 1);
				mFrameRender.displayMode = 7;

			} else if (i == R.id.btnPlotEye) {
				mFrameRender.displayMode = 6;
				mFrameRender.depth = FHSDK.getMaxZDepth(mFrameRender.hWin);
				FHSDK.setStandardCircle(mFrameRender.hWin, 547.0f, 500.0f - 216.0f, 200.0f);

			} else if (i == R.id.btnPlotCorrect) {
				mFrameRender.displayMode = 7;
				FHSDK.setStandardCircle(mFrameRender.hWin, 547.0f, 500.0f - 216.0f, 200.0f);

			} else if (i == R.id.btnMultipleMode) {
				mFrameRender.bMixMode = true;

			} else {
			}
		}
	};
	//	private View.OnClickListener OnSetClickLister = new View.OnClickListener(){
//		@Override
//		public void onClick(View v) {
//			//String x = edtCircleX.getText().toString();
//			//String y = edtCircleY.getText().toString();
//			///String r = edtCircleR.getText().toString();
//			if (edtCircleX.getText().toString().trim().length() <= 0
//					|| edtCircleY.getText().toString().trim().length() <= 0
//					|| edtCircleR.getText().toString().trim().length() <= 0)
//			{
//				ActivtyUtil.showAlert(mContext, mContext.getText(R.string.error), mContext.getText(R.string.error_input), mContext.getText(R.string.id_sure));
//				return;
//			}
//			float x = Float.parseFloat(edtCircleX.getText().toString().trim());
//			float y = Float.parseFloat(edtCircleY.getText().toString().trim());
//			float r = Float.parseFloat(edtCircleR.getText().toString().trim());
//			log.e("x=" + x + " y= " + y + " r = " + r);
//			FHSDK.setStandardCircle(mFrameRender.hWin, x, y, r);
//
//		}
//	};
	private DialogInterface.OnClickListener OnSetClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			if (edtCircleX.getText().toString().trim().length() <= 0
					|| edtCircleY.getText().toString().trim().length() <= 0
					|| edtCircleR.getText().toString().trim().length() <= 0)
			{
				ActivtyUtil.showAlert(mContext, mContext.getText(R.string.error), mContext.getText(R.string.error_input), mContext.getText(R.string.id_sure));
				return;
			}
			float x = Float.parseFloat(edtCircleX.getText().toString().trim());
			float y = Float.parseFloat(edtCircleY.getText().toString().trim());
			float r = Float.parseFloat(edtCircleR.getText().toString().trim());
			//log.e("x=" + x + " y= " + y + " r = " + r);

			mFrameRender.circleX = x;
			mFrameRender.circleY = y;
			mFrameRender.circleR = r;
		}
	};

	private DialogInterface.OnClickListener OnAutoSetClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			mFrameRender.bAdjust = true;
		}
	};

	private DialogInterface.OnClickListener OnSetCancelClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
		}
	};
	private Spinner sp;
	private EditText edtCircleX, edtCircleY, edtCircleR;
	private ArrayAdapter<String> TypeAdapter;
	private String[] strSensorType = {"自定义", "0130A", "0130B", "9750A", "9750B", "5130", "9750"};
	protected int sensorTypeSeldID;
	public View.OnClickListener btnCorrectClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			View myInputView = layoutInflater.inflate(R.layout.dlg_circle, null);

			sp  = (Spinner)myInputView.findViewById(R.id.spinner1);
			edtCircleX = (EditText)myInputView.findViewById(R.id.editText1);// editText1 editText2 editText3
			edtCircleY = (EditText)myInputView.findViewById(R.id.editText2);
			edtCircleR = (EditText)myInputView.findViewById(R.id.editText3);
			TypeAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, strSensorType);
			TypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp.setAdapter(TypeAdapter);
			sp.setOnItemSelectedListener(new SensorTypeSelectedListener());

			final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("图像校准");
			builder.setView(myInputView).setPositiveButton("设置", OnSetClickLister);//防止对话框自动关闭
			builder.setView(myInputView).setNeutralButton("自动设置", OnAutoSetClickLister);
			builder.setView(myInputView).setNegativeButton("取消", OnSetCancelClickLister);
			builder.setCancelable(false);//禁止通过点击返回键，对话框外区域关闭对话框。
			AlertDialog alert = builder.create();
			alert.show();
			//Button btnSet = alert.getButton(AlertDialog.BUTTON_POSITIVE);//
			//btnSet.setOnClickListener(OnSetClickLister);//
		}
	};
	class SensorTypeSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			sensorTypeSeldID = arg2;
			Log.e("sensorTypeSeldID", "sensorTypeSeldID = " + sensorTypeSeldID);
			if (sensorTypeSeldID == 0)
			{
				edtCircleX.setText(String.valueOf(0.0f));
				edtCircleY.setText(String.valueOf(0.0f));
				edtCircleR.setText(String.valueOf(0.0f));
			}
			else if (sensorTypeSeldID == 1)
			{
				edtCircleX.setText(String.valueOf(464.0f));
				edtCircleY.setText(String.valueOf(440.0f));
				edtCircleR.setText(String.valueOf(440.0f));
			}
			else if (sensorTypeSeldID == 2)
			{
				edtCircleX.setText(String.valueOf(508.0f));
				edtCircleY.setText(String.valueOf(470.0f));
				edtCircleR.setText(String.valueOf(452.0f));
			}
			else if (sensorTypeSeldID == 3)
			{
				edtCircleX.setText(String.valueOf(517.0f));
				edtCircleY.setText(String.valueOf(484.0f));
				edtCircleR.setText(String.valueOf(408.0f));
			}
			else if (sensorTypeSeldID == 4)
			{
				edtCircleX.setText(String.valueOf(496.0f));
				edtCircleY.setText(String.valueOf(497.0f));
				edtCircleR.setText(String.valueOf(408.0f));
			}
			else if (sensorTypeSeldID == 5)
			{
				edtCircleX.setText(String.valueOf(470.0f));
				edtCircleY.setText(String.valueOf(488.0f));
				edtCircleR.setText(String.valueOf(455.0f));
			}
			else if (sensorTypeSeldID == 6)
			{
				edtCircleX.setText(String.valueOf(491.0f));
				edtCircleY.setText(String.valueOf(446.0f));
				edtCircleR.setText(String.valueOf(436.0f));
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}


	public void setLayoutMenuShow()
	{
		if (null ==layoutMenu)
		{
			return;
		}
		if (layoutMenu.getVisibility()==View.GONE)
			layoutMenu.setVisibility(View.VISIBLE);
		else
			layoutMenu.setVisibility(View.GONE);
	}

	public void createHandler()
	{
		if (FHSDK.PLAY_TYPE_PREVIEW == PlayInfo.playType)
		{
			hRecTime =  new Handler();
		}
		handler = new Handler(){
			boolean bGetRecInfo = true;
			public void handleMessage(Message msg) {
				switch(msg.what){
					case NOTIFY_QUIT:
					{
						//quitVideoPlay();
						break;
					}

					case UPDATE_PROGRESS:
					{
						if (FHSDK.PLAY_TYPE_REMOTE_PLAYBACK == PlayInfo.playType)
						{
							int progress;
							CurrentTime = FHSDK.getCurrentPts();
							if (mPBCurrentTime != null)
								mPBCurrentTime.setText(FHSDK.timeConvert(PlayInfo.userID, CurrentTime));
							progress =(int)((CurrentTime - PBStartTime)*SEEKBAR_MAX_NUM/(PBStopTime - PBStartTime));
							mSeekBar.setProgress(progress);
						}
						else if (FHSDK.PLAY_TYPE_LOCATE_PLAYBACK == PlayInfo.playType)
						{
							CurrentTime = FHSDK.getCurrentPts();
							if (0 == CurrentTime)
								break;

							if (bGetRecInfo)
							{
								PBRecTime recTime = (new Define()).new PBRecTime();

								FHSDK.getRecPlayTimeInfo(recTime);
								PBStopTime = recTime.pbStopTime;
								PBStartTime = recTime.pbStartTime;
								PBVideoLen = (PBStopTime - PBStartTime)/1000;
								if (null != mPBStopTime)
									mPBStopTime.setText(ActivtyUtil.formatTime(PBVideoLen));

								bGetRecInfo = false;
							}

							int progress = 0;

							PBVideoCurLen = CurrentTime - PBStartTime/1000;
							if (PBVideoLen> 0)
								progress = (int)(PBVideoCurLen*SEEKBAR_MAX_NUM/PBVideoLen);
							if (null != mPBCurrentTime)
								mPBCurrentTime.setText(ActivtyUtil.formatTime(PBVideoCurLen));

							mSeekBar.setProgress(progress);
							//mSeekBar.setProgress(FHSDK.getRecPlayProgress());
						}
						else if (PlayInfo.playType == FHSDK.PLAY_TYPE_MP4FILE)
						{
							int curSec = FHSDK.mp4GetCurSec();
							int duration = FHSDK.mp4GetFileDuration();
							if (duration <= 0)
								break;
							int progress = (int)(curSec*SEEKBAR_MAX_NUM/duration);
							if (null != mPBCurrentTime)
							{
								mPBCurrentTime.setText(ActivtyUtil.formatTime(curSec*1000));
								mPBStopTime.setText(ActivtyUtil.formatTime(FHSDK.mp4GetFileDuration()*1000));
							}
							mSeekBar.setProgress(progress);
						}
						break;
					}
					case NOTIFY_UPDATE_STREAM_INFO:
					{
						String s = (String) msg.obj;
						if (null != tvStreamInfo)
							tvStreamInfo.setText(s);
						break;
					}
					case SHOT_FileName:
					{
						Bundle b = msg.getData();
						String fileName = b.getString("fileName");
						ActivtyUtil.openToast(mContext, fileName);
						break;
					}
					case SHOT_Fail:
					{
						String s = mContext.getString(R.string.id_fail);
						ActivtyUtil.openToast(mContext, s);
					}
					default:
						break;
				}
				super.handleMessage(msg);
			}
		};
	}
	public static String getSettingPath()
	{
		return REC_PATH;
	}

	public static void saveImageToGallery(Context context, String fileName, Bitmap bmp) {

		File appDir = new File(Environment.getExternalStorageDirectory(), getSettingPath());
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		//String fileName = System.currentTimeMillis() + ".bmp";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" +  file.getAbsoluteFile())));
	}
	private CbDataInterface dataFun = new CbDataInterface(){
		public void cb_data(int type, byte[] data, int len){

			switch(type)
			{
				case NOTIFY_TYPE_SHOT_FileName:
				{
					String filePath = new String(data,0, len);
					//Log.e("XXX", "path="+ filePath);
					String[] m = filePath.split("/");
					String fileName = m[m.length-1];
					//fileScan(path);

					Bundle bundle=new Bundle();
					bundle.putString("fileName", fileName);
					Message msg = handler.obtainMessage();
					msg.what = SHOT_FileName;
					msg.setData(bundle);
					handler.sendMessage(msg);

					FileInputStream fis = null;
					try {
						fis = new FileInputStream(filePath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//压缩图片
					Bitmap bitmap  = BitmapFactory.decodeStream(fis);
					//修改尺寸
					//Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bitmap, 1280, 1024);
					saveImageToGallery(mContext, fileName, bitmap);
					break;
				}
				case NOTIFY_TYPE_SHOT_Fail:
				{

					//Log.v("xxx"," has no sd card");
					//Context  context = mContext;
					//String s = mContext.getString(R.string.ERR_NO_SDCARD);
					//Log.v("xxx", s);
					//ActivtyUtil.openToast(context, s);
					Message msg = handler.obtainMessage();
					msg.what = SHOT_Fail;
					handler.sendMessage(msg);

					break;
				}
				case NOTIFY_TYPE_STREAM_INFO:
				{
					String s = null;
					try {
						s = new String(data, "GB2312");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//Log.e("xxx","type = " + type + "| len = " + len + " |data = " + s);

					Message msg = handler.obtainMessage();
					msg.what = NOTIFY_UPDATE_STREAM_INFO;
					msg.obj  = s;
					handler.sendMessage(msg);
				}
				case NOTIFY_TYPE_PTS:
				{
					if (PlayInfo.playType != FHSDK.PLAY_TYPE_LOCATE_PLAYBACK
							&& PlayInfo.playType != FHSDK.PLAY_TYPE_REMOTE_PLAYBACK
							&& PlayInfo.playType != FHSDK.PLAY_TYPE_MP4FILE)
						break;
					if (!isStopSendMsg)
					{
						Message msg = handler.obtainMessage();
						msg.what = UPDATE_PROGRESS;
						handler.sendMessage(msg);
					}
				}
				default:
					break;
			}



		}
	};
	public View.OnClickListener surfaceTouchListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

			setLayoutMenuShow();
		}

	};



	private SeekBar.OnSeekBarChangeListener  seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		int progress = 0;
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			progress = arg1;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			isStopSendMsg = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			//long CurrentPts = 0;
			//CurrentPts =PBStartTime + (PBStopTime - PBStartTime)*progress/SEEKBAR_MAX_NUM;
			//FHSDK.jumpPlayBack(CurrentPts*1000);

			int pos = progress*100/SEEKBAR_MAX_NUM;
			FHSDK.jumpPlayBack(pos);

			if (!isPBPause)
				isStopSendMsg = false;
			//isStopSendMsg = false;
		}

	};

	private View.OnClickListener pbPlayListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (isPBPause && FHSDK.continuePBPlay()){
				isPBPause = false;
				tvPBStatus.setText("");
				btPBPlay.setImageResource(R.drawable.pb_play);
			}
			else if (!isPBPause && FHSDK.pausePBPlay())
			{
				isPBPause = true;
				String str = (String)mContext.getText(R.string.id_pause);
				tvPBStatus.setText(str);
				btPBPlay.setImageResource(R.drawable.pb_pause);
			}
		}

	};

	private View.OnClickListener pbFrameListener  = new View.OnClickListener (){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!isPBPause){
				isPBPause = true;
				String str = (String)mContext.getText(R.string.id_framePlay);
				tvPBStatus.setText(str);
				btPBPlay.setImageResource(R.drawable.pb_pause);
			}
			FHSDK.playFrame();
		}

	};
	private View.OnClickListener pbSpeedDownListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (SPEED_2 == PBCurrentSpeed || SPEED_4 == PBCurrentSpeed
					|| SPEED_8 == PBCurrentSpeed || SPEED_16 == PBCurrentSpeed)
				PBCurrentSpeed--;
			else if (SPEED_1 == PBCurrentSpeed)
				PBCurrentSpeed = SPEED_1_2;
			else if (SPEED_1_2 == PBCurrentSpeed || SPEED_1_4 == PBCurrentSpeed
					|| SPEED_1_8 == PBCurrentSpeed)
				PBCurrentSpeed++;
			else if (SPEED_1_16 == PBCurrentSpeed)
				return;

			tvPBSpeed.setText(strPBSpeed[PBCurrentSpeed]);
			FHSDK.setPBSpeed(PBCurrentSpeed);
		}

	};
	private View.OnClickListener pbSpeedUpListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (SPEED_1 == PBCurrentSpeed || SPEED_2 == PBCurrentSpeed
					|| SPEED_4 == PBCurrentSpeed || SPEED_8 == PBCurrentSpeed)
				PBCurrentSpeed++;
			else if (SPEED_16 == PBCurrentSpeed)
				return;
			else if (SPEED_1_4 == PBCurrentSpeed || SPEED_1_8 == PBCurrentSpeed
					|| SPEED_1_16 == PBCurrentSpeed)
				PBCurrentSpeed--;
			else if (SPEED_1_2 == PBCurrentSpeed)
				PBCurrentSpeed = SPEED_1;

			tvPBSpeed.setText(strPBSpeed[PBCurrentSpeed]);
			FHSDK.setPBSpeed(PBCurrentSpeed);

		}

	};
	private View.OnClickListener pbAudioListener  = new View.OnClickListener (){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (isPBAudioOn && FHSDK.closeAudio(audioType[1]))
			{
				isPBAudioOn = false;
				btPBAudio.setImageResource(R.drawable.pb_audio_off);
			}
			else if (!isPBAudioOn && FHSDK.openAudio(audioType[1]))
			{
				isPBAudioOn = true;
				btPBAudio.setImageResource(R.drawable.pb_audio_on);
			}

		}


	};
	private View.OnClickListener btnRemoteRecSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			String str = null;
			if (FHSDK.getRemoteRecordState(PlayInfo.userID))
			{
				str = (String)mContext.getText(R.string.id_remoteRec) + (String)mContext.getText(R.string.id_stop);
				FHSDK.stopRemoteRecord(PlayInfo.userID);
				showChronometer(false);
				//ActivtyUtil.openToast(mContext,str);
				btnRemoteRec.setBackgroundResource(R.drawable.btn_remote_rec_off);

			}
			else
			{
				if (FHSDK.startRemoteRecord(PlayInfo.userID))
				{
					str = (String)mContext.getText(R.string.id_remoteRec) + (String)mContext.getText(R.string.id_start);
					btnRemoteRec.setBackgroundResource(R.drawable.btn_remote_rec_on);
					showChronometer(true);
				}
				else
				{
					str = (String)mContext.getText(R.string.id_remoteRec) + (String)mContext.getText(R.string.id_fail);
					showChronometer(false);
				}
			}
			ActivtyUtil.openToast(mContext,str);
		}
	};

	/**
	 * 显示或者隐藏Chronometer
	 * @param bShow 显示开关
	 */
	private void showChronometer(boolean bShow) {
		if (bShow) {
			mChronometer.setVisibility(View.VISIBLE);
			mChronometer.setBase(SystemClock.elapsedRealtime());
			mChronometer.start();
		} else {
			mChronometer.stop();
			mChronometer.setVisibility(View.INVISIBLE);
			mChronometer.setText("");
		}
	}
	private View.OnClickListener btnRemoteShotSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			String str = (String)mContext.getText(R.string.id_remoteShot);
			if(FHSDK.shot(PlayInfo.userID, null, false))
				str += (String)mContext.getText(R.string.id_success);
			else
				str += (String)mContext.getText(R.string.id_fail);

			ActivtyUtil.openToast(mContext,str);
		}
	};
	private static String getCurrentDay() {


		String time =  new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date().getTime());


		return time;
	}
	public static String getSDCardPath() {
		String sdcard_path = null;
		String sd_default = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		Log.d("text", sd_default);
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// 得到路径
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;
				if (line.contains("fat") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				} else if (line.contains("fuse") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    /*
    if (null == sdcard_path)
    {
    	Log.e("xx", "xxxxxxxxxxxxxx");
    	return null;
    }
    Log.d("text", sdcard_path);
    */
		return sdcard_path;
	}

	private View.OnClickListener btnLocateRecSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			getSDCardPath();
			if (isRecOn){
				FHSDK.stopLocalRecord();
				isRecOn = false;
				//ActivtyUtil.openToast(Preview.this, "stop record");
				btnLocateRec.setBackgroundResource(R.drawable.btn_locate_rec_off);
				tvRecTime.setVisibility(View.INVISIBLE);
				hRecTime.removeCallbacks(recTimeThread);
				secondCount = 0;
			}
			else{
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					File sd = Environment.getExternalStorageDirectory();
					String path = sd.getPath() + getSettingPath();
					String fileName = "VIDEO_"+ActivtyUtil.getCurSysDate();//SDLActivity.timeConvert2(SDLActivity.userID, getCurrentPts());
					File file = new File(path);
					if (!file.exists())
					{
						file.mkdir();
					}
					if (0 == PlayInfo.locateRecType) // H264
						path = path + "/"+ fileName + ".H264";
					else if (1 == PlayInfo.locateRecType) // AVI
						path = path + "/"+ fileName + ".avi";
					else if (2 == PlayInfo.locateRecType) // MP4
						path = path + "/"+ fileName + ".mp4";

					if(FHSDK.startLocalRecord(PlayInfo.locateRecType, path))
					{
						isRecOn = true;
						//ActivtyUtil.openToast(SDLActivity.this, "start record");
						btnLocateRec.setBackgroundResource(R.drawable.btn_locate_rec_on);
						tvRecTime.setVisibility(View.VISIBLE);
						hRecTime.post(recTimeThread);
					}else{
						ActivtyUtil.openToast(mContext,"record fail");
					}
				}else{
					ActivtyUtil.openToast(mContext, "have no SD card, record fail");
				}
			}
		}
	};

	private View.OnClickListener btnLocateShotSetListener = new View.OnClickListener(){
		public void onClick(View v) {

			//FHSDK.setShotOn();

			String filename = getCurrentDay();
			String filePath = Environment.getExternalStorageDirectory().getPath() + VideoPlayView.getSettingPath() +	filename+".jpeg";
			String str = (String)mContext.getText(R.string.id_remoteShot);
			if(FHSDK.shot(PlayInfo.userID, filePath, true))
				str += (String)mContext.getText(R.string.id_success);
			else
				str += (String)mContext.getText(R.string.id_fail);

			ActivtyUtil.openToast(mContext,str);

		}
	};

	boolean isreturn = false;
	private View.OnClickListener btnAudioSetListener = new View.OnClickListener(){
		public void onClick(View v) {
//			String str = null;
//			isAudioOpened = FHSDK.getRealAudioState();
//			if (isAudioOpened)
//			{
//				FHSDK.closeAudio(audioType[0]);
//				btnAudio.setBackgroundResource(R.drawable.btn_a_on);
//			}
//			else
//			{
//				FHSDK.openAudio(audioType[0]);
//				btnAudio.setBackgroundResource(R.drawable.btn_a_off);
//			}

			//更改成翻转

			if(!isreturn) {
				FHSDK.mirrorCtrl(PlayInfo.userID, 3);
				isreturn = true;
			}
			else
			{
				FHSDK.mirrorCtrl(PlayInfo.userID, 0);
				isreturn = false;
			}

		}
	};
	private View.OnClickListener btnTalkSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			//	FHSDK.setShotOn();
			String str = null;

			talkSample = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_SAMPLERATE);
			talkFormat = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_AUDIO_FORMAT);

			if (isTalkOpened)
			{
				isTalkOpened = false;
				stopTalkThread();
				btnTalk.setBackgroundResource(R.drawable.btn_talk_off);
			}
			else
			{
				isTalkOpened = true;
				startTalkThread();
				btnTalk.setBackgroundResource(R.drawable.btn_talk_on);
			}

		}
	};

	private View.OnClickListener btnStreamChangeSetListener = new View.OnClickListener(){

		int streamType = 1;
		String[] strType = {"第一码流", "第二码流", "第三码流"};
		public void onClick(View v) {
			//Log.v("xx", "onClick");

			//if (true)
			//{
			//streamType++;
			//if (streamType > 2)
			//	streamType = 1;
			//if(FHSDK.changeStreamType(PlayInfo.userID, streamType))
			//	ActivtyUtil.openToast(mContext, "切换至 "+ strType[streamType-1]);
			//else
			//	ActivtyUtil.openToast(mContext, "切换出错");
			//}
			//else
			//{
			//test jump activity
			//    Intent intent = new Intent(VideoPlay.this,  LocateRecList.class);
			//    startActivity(intent);
			//    onBackPressed();
			//}
		}
	};
	private View.OnClickListener btnSerialSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);

			View myInputView = layoutInflater.inflate(R.layout.serial_input, null);

			edtSerialInput = (EditText)myInputView.findViewById(R.id.edtSerialInput);

			final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("数据输入");
			builder.setView(myInputView).setPositiveButton("发送", null);//防止对话框自动关闭
			builder.setView(myInputView).setNegativeButton("取消", OnSerialCancelClickLister);
			builder.setCancelable(false);//禁止通过点击返回键，对话框外区域关闭对话框。
			AlertDialog alert = builder.create();
			alert.show();
			Button btnSend = alert.getButton(AlertDialog.BUTTON_POSITIVE);//
			btnSend.setOnClickListener(OnSerialSendClickLister);//


			int serialPort = 1;
			int serialIndex = 1;
			int transMode = 1; // 0 tcp  1 udp
			serialHandle = FHSDK.startSerialEx(PlayInfo.userID, serialPort, serialIndex, transMode, true, serialFun);
			if (0 == serialHandle)
			{
				ActivtyUtil.openToast(mContext,"创建句柄失败");
				return;
			}


		}

	};

  public static byte[] HexStringToBytes(String hexString) throws Exception {
    if ((hexString == null) || (hexString.equals(""))) {
      return null;
    }
    hexString = hexString.toUpperCase(Locale.getDefault());
    int length = hexString.length() / 2;
    char[] hexChars = hexString.toCharArray();
    byte[] d = new byte[length];
    for (int i = 0; i < length; i++) {
      int pos = i * 2;
      d[i] = ((byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)])));
    }
    return d;
  }
	private static byte charToByte(char c) {
		return (byte)"0123456789ABCDEF".indexOf(c);
	}
	private SerialDataCallBackInterface serialFun = new SerialDataCallBackInterface(){
		public int SerialDataCallBack(int serialHandle, byte[] buffer, int bufferLen){
			Log.e("xxx","serialHandle = " + serialHandle + "| len = " + bufferLen);
			//ActivtyUtil.openToast(mContext, "" + buffer);
			 Log.d("VideoPlayView", "buffer:" + bytes2hex(buffer) );
			return 0;
		}
	};
	public static String bytes2hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (byte b : bytes) {
			// 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1) {
				tmp = "0" + tmp;
			}
			sb.append(" ");   // 这里加空格是为了看清每个数组，分开。美观
			sb.append(tmp);
		}
		return sb.toString();
	}
	private View.OnClickListener OnSerialSendClickLister = new View.OnClickListener(){
		@Override
		public void onClick(View v) {

			String str = edtSerialInput.getText().toString();

			byte[] sendData = new byte[0];
			try {
				sendData = HexStringToBytes(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (sendData == null || sendData.length <= 0)
			{
				ActivtyUtil.openToast(mContext,"数据不可为空");
				return;
			}

			if(FHSDK.sendSerial(serialHandle, sendData, sendData.length))
			{
				ActivtyUtil.openToast(mContext,"success");
			}
			else
			{
				ActivtyUtil.openToast(mContext,"failed");
			}
			//FHSDK.stopSerial(serialHandle);

			//UDPSend.sleepTime = Integer.valueOf(sendData);
			//log.e("UDPSend.sleepTime = " + UDPSend.sleepTime );
		}
	};

	private DialogInterface.OnClickListener OnSerialCancelClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			FHSDK.stopSerial(serialHandle);
		}
	};

	private View.OnClickListener btnBCSSListener = new View.OnClickListener(){
		public void onClick(View v) {

			LayoutInflater layoutInflater = LayoutInflater.from(mContext);

			View myInputView = layoutInflater.inflate(R.layout.bcss, null);

			seekBarBrightness = (SeekBar)myInputView.findViewById(R.id.SeekBarBrightNess);
			seekBarContrast = (SeekBar)myInputView.findViewById(R.id.SeekBarContrast);
			seekBarSaturation = (SeekBar)myInputView.findViewById(R.id.SeekBarSaturation);
			seekBarSharpness = (SeekBar)myInputView.findViewById(R.id.SeekBarSharpness);

			tvBrightnessVal = (TextView)myInputView.findViewById(R.id.tvBrightnessVal);
			tvContrastVal = (TextView)myInputView.findViewById(R.id.tvContrastVal);
			tvSaturationVal = (TextView)myInputView.findViewById(R.id.tvSaturationVal);
			tvSharpnessVal = (TextView)myInputView.findViewById(R.id.tvSharpnessVal);


			if (FHSDK.getVideoBCSS(PlayInfo.userID, bcssObj))
			{
				//Log.e("xx", "get bscc = " + bcssObj.brightness + "/" + bcssObj.contrast + "/" + bcssObj.saturation + "/" + bcssObj.sharpness);

				//

				bcssDefObj.brightness = bcssObj.brightness;
				bcssDefObj.contrast   = bcssObj.contrast;
				bcssDefObj.saturation = bcssObj.saturation;
				bcssDefObj.sharpness  = bcssObj.sharpness;

				int devFlag = FHSDK.getDeviceFlag(PlayInfo.userID);

				if(DEVICE_TYPE_FH8610 == devFlag)
				{
					seekBarBrightness.setMax(255);
					seekBarContrast.setMax(63);
					seekBarSaturation.setMax(63);
					seekBarSharpness.setMax(15);
				}
				else if (DEVICE_TYPE_FH8620 == devFlag || DEVICE_TYPE_FH8810 == devFlag)
				{
					seekBarBrightness.setMax(255);
					seekBarContrast.setMax(255);
					seekBarSaturation.setMax(255);
					seekBarSharpness.setMax(255);
				}

				seekBarBrightness.setProgress(bcssObj.brightness);
				seekBarBrightness.setOnSeekBarChangeListener(seekBarBrightnessChangeListener);
				tvBrightnessVal.setText(""+bcssObj.brightness);

				seekBarContrast.setProgress(bcssObj.contrast);
				seekBarContrast.setOnSeekBarChangeListener(seekBarContrastChangeListener);
				tvContrastVal.setText(""+bcssObj.contrast);

				seekBarSaturation.setProgress(bcssObj.saturation);
				seekBarSaturation.setOnSeekBarChangeListener(seekBarSaturationChangeListener);
				tvSaturationVal.setText(""+bcssObj.saturation);

				seekBarSharpness.setProgress(bcssObj.sharpness);
				seekBarSharpness.setOnSeekBarChangeListener(seekBarSharpnessChangeListener);
				tvSharpnessVal.setText(""+bcssObj.sharpness);
			}
			else
			{
				ActivtyUtil.openToast(mContext,"获取失败");
			}
			//seekBarSharpness.SET
			final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("色彩调节");
			builder.setView(myInputView).setPositiveButton("保存", OnSaveClickLister);
			builder.setView(myInputView).setNegativeButton("取消", OnCancelClickLister);
			AlertDialog alert = builder.create();
			alert.show();

		}
	};


	private View.OnClickListener btnEyeMenuListener = new View.OnClickListener(){
		public void onClick(View v) {
			if(0 == (Integer) v.getTag())
			{
				layoutEyeMenu.setVisibility(View.VISIBLE);
				layoutEyeMode.setVisibility(View.VISIBLE);
				v.setTag(1);
			}else
			{
				layoutEyeMenu.setVisibility(View.GONE);
				layoutEyeMode.setVisibility(View.GONE);
				v.setTag(0);
			}
		}
	};

	private DialogInterface.OnClickListener OnSaveClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {

			if(FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj))
			{
				FHSDK.saveDevConfig(PlayInfo.userID);
			}
			else
			{
				ActivtyUtil.openToast(mContext,  "保存失败");
			}
		}
	};

	private DialogInterface.OnClickListener OnCancelClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarBrightnessChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.brightness= arg1;
			tvBrightnessVal.setText(""+arg1);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}

	};
	private SeekBar.OnSeekBarChangeListener  seekBarContrastChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.contrast= arg1;
			tvContrastVal.setText(""+arg1);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarSaturationChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.saturation= arg1;
			tvSaturationVal.setText(""+arg1);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarSharpnessChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.sharpness= arg1;
			tvSharpnessVal.setText(""+arg1);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
	private SeekBar.OnSeekBarChangeListener  locateSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {


		int progress = 0;
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			progress = arg1;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			isStopSendMsg = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			//long CurrentPts = 0;
			//CurrentPts =PBStartTime + (PBStopTime - PBStartTime)*progress/SEEKBAR_MAX_NUM;
			int pos = progress*100/SEEKBAR_MAX_NUM;
			FHSDK.locateJumpPlayBack(pos);
			if (PlayInfo.playType == FHSDK.PLAY_TYPE_MP4FILE)
			{
				int sec = FHSDK.mp4GetFileDuration()*progress/SEEKBAR_MAX_NUM;
				FHSDK.mp4SeekTo(sec);
			}

			
			if (!isPBPause)
				isStopSendMsg = false;
		}
	};

	private View.OnClickListener locatePBPlayListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (PlayInfo.playType == FHSDK.PLAY_TYPE_MP4FILE)
			{
				if (isPBPause && FHSDK.mp4SetPlayStatus(0)){
					isPBPause = !isPBPause;
					btnPBPlay.setImageResource(android.R.drawable.ic_media_play);
					isStopSendMsg = false;
				}
				else if (!isPBPause && FHSDK.mp4SetPlayStatus(1))
				{
					isPBPause = !isPBPause;
					btnPBPlay.setImageResource(android.R.drawable.ic_media_pause);
				}
			}
			else
			{
				if (isPBPause && FHSDK.locateContinuePBPlay()){
					isPBPause = !isPBPause;
					btnPBPlay.setImageResource(android.R.drawable.ic_media_play);
					isStopSendMsg = false;
				}
				else if (!isPBPause && FHSDK.locatePausePBPlay())
				{
					isPBPause = !isPBPause;
					btnPBPlay.setImageResource(android.R.drawable.ic_media_pause);
				}
			}
		}

	};

	private View.OnClickListener locatePBSpeedDownListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (SPEED_2 == PBCurrentSpeed || SPEED_4 == PBCurrentSpeed)
				PBCurrentSpeed--;
			else if (SPEED_1 == PBCurrentSpeed)
				PBCurrentSpeed = SPEED_1_2;
			else if (SPEED_1_2 == PBCurrentSpeed)
				PBCurrentSpeed++;
			else if (SPEED_1_4 == PBCurrentSpeed)
				return;

			if (FHSDK.setLocatePBSpeed(PBCurrentSpeed))
				tvPBSpeed.setText(strPBSpeed[PBCurrentSpeed]);
		}

	};
	private View.OnClickListener locatePBSpeedUpListener  = new View.OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (SPEED_1 == PBCurrentSpeed || SPEED_2 == PBCurrentSpeed)
				PBCurrentSpeed++;
			else if (SPEED_4 == PBCurrentSpeed)
				return;
			else if (SPEED_1_4 == PBCurrentSpeed)
				PBCurrentSpeed--;
			else if (SPEED_1_2 == PBCurrentSpeed)
				PBCurrentSpeed = SPEED_1;


			if (FHSDK.setLocatePBSpeed(PBCurrentSpeed))
				tvPBSpeed.setText(strPBSpeed[PBCurrentSpeed]);
		}

	};
	Runnable recTimeThread = new Runnable(){

		public void run() {
			long  ms = (secondCount++) * 1000 ;
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
			String hms = formatter.format(ms);
			tvRecTime.setText(hms);

			hRecTime.postDelayed(recTimeThread, 1000);
		}

	};

	public static void startTalkThread(){
		mTalk = new TalkThread();
		mTalk.startRecording();
	}
	public static void stopTalkThread(){
		if (null != mTalk)
		{
			mTalk.stopRecording();
			mTalk = null;
		}
		FHSDK.stopTalk();
	}
}


class TalkThread implements Runnable{
	String LOG = "Recorder ";
	private boolean isRecording = false;
	private AudioRecord audioRecord;

	private static final int audioSource = MediaRecorder.AudioSource.MIC;
	private static int sampleRate;// = 16000;
	private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private static final int BUFFER_FRAME_SIZE =2048;//960;
	private int audioBufSize = 0;
	private static int sendFormat = 0;
	//
	private byte[] samples;//
	//private short[] samples;
	private int bufferRead = 0;//
	private int bufferSize = 0;//
	public void startRecording() {
		//bufferSize = BUFFER_FRAME_SIZE;

		sampleRate = (0 == VideoPlayView.talkSample)?8000:16000;

		audioBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
				audioFormat);

		if (audioBufSize == AudioRecord.ERROR_BAD_VALUE) {
			Log.e(LOG, "audioBufSize error");
			return;
		}
		Log.e(LOG, "audioBufSize = "+ audioBufSize);

		//samples = new short[audioBufSize];

		if (null == audioRecord) {
			audioRecord = new AudioRecord(audioSource, sampleRate,
					channelConfig, audioFormat, audioBufSize);
		}
		new Thread(this).start();
	}


	public void stopRecording() {
		this.isRecording = false;
	}

	public boolean isRecording() {
		return isRecording;
	}
	// run
	public void run() {
		Log.e(LOG, "audioRecord startRecording()");

		bufferSize = FHSDK.getTalkUnitSize(PlayInfo.userID);
		if (bufferSize <= 0)
			bufferSize = BUFFER_FRAME_SIZE;
		if (bufferSize > audioBufSize)
			bufferSize = audioBufSize;

		samples = new byte[audioBufSize];
		audioRecord.startRecording();
		this.isRecording = true;
		if (!FHSDK.startTalk(PlayInfo.userID))
		{
			//
		}

		sendFormat = VideoPlayView.talkFormat;

		while (isRecording) {
			bufferRead = audioRecord.read(samples, 0, bufferSize);
			if (bufferRead > 0) {
				byte[] tempData = new byte[bufferRead];
				System.arraycopy(samples, 0, tempData, 0, bufferRead);
				FHSDK.sendTalkData(tempData, bufferRead, sampleRate, sendFormat);
			}
		}
		System.out.println(LOG + "stop");
		audioRecord.stop();
	}

}
