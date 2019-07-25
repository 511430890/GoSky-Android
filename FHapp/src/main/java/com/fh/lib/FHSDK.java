package com.fh.lib;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.fh.lib.Define.*;

public class FHSDK {


	public static final int PLAY_TYPE_PREVIEW         = 0;
	public static final int PLAY_TYPE_UDP             = 1;
	public static final int PLAY_TYPE_REMOTE_PLAYBACK = 2;
	public static final int PLAY_TYPE_LOCATE_PLAYBACK = 3;    // Keep track of the paused state
	public static final int PLAY_TYPE_MP4FILE = 4;    // Keep track of the paused state

	public static final int DECODE_TYPE_FFMPEG2OPENGL         = 0;
	public static final int DECODE_TYPE_MEDIACODEC2OPENGL     = 1;

	static {
		// System.loadLibrary("SDL");
		System.loadLibrary("FHComponent");
		System.loadLibrary("FHDEV_Discover");
		System.loadLibrary("FHDEV_Net");
		//System.loadLibrary("FHAVI");
		System.loadLibrary("FHMP4");
		//System.loadLibrary("FHMP4_Android_armeabi");
		System.loadLibrary("main");
	}




	//search device
	public native static boolean searchInit();
	public native static boolean searchCleanup();
	public native static long searchDev();
	public native static boolean searchNextDev(long handle, DevSearch searchResult);
	public native static boolean searchDevClose(long handle);
	public native static boolean registerDevStateFun();
	public native static int getDevStatus();

	//net init
	public native static boolean apiInit();
	public native static boolean apiCleanup();

	//aes key
	public native static boolean setCryptKey(String aesKey);

	//login/logout
	public native static long login(String devIP, int devPort, String userName, String password);
	public native static long getCurrentTime();
	public native static boolean logout(long userID);

	//play . setPlayInfo first
	public native static boolean setPlayInfo(PlayInfo playInfo);
	public native static boolean startPlay();
	public native static boolean stopPlay();

	//mirror
	public native static boolean mirrorCtrl(long userID, int type);

	// rigister notify
	public native static void registerNotifyCallBack(CbDataInterface dataFun);
	// rigister yuv data callback for opengl renderer
	public native static void registerUpdateCallBack(YUVDataCallBackInterface dataFun);
	//rigister raw data callback  for MediaCodec2opengl
	public native static void registerStreamDataCallBack(StreamDataCallBackInterface dataFun);


	//locate record  // recType: 0 h264 1 avi 2 mp4
	public native static boolean startLocalRecord(int recType, String path);
	public native static boolean stopLocalRecord();
	public native static boolean getRecPlayTimeInfo(PBRecTime recTime);
	public native static int     getRecPlayProgress();

	//locate playback control
	public native static boolean setLocatePBSpeed(int speed);
	public native static boolean locatePausePBPlay();
	public native static boolean locatePlayFrame();
	public native static boolean locateContinuePBPlay();
	public native static boolean locateJumpPlayBack(int pos);

	public native static boolean mp4SeekTo(int startSec); // sec
	public native static int mp4GetCurSec();
	public native static int mp4GetFileDuration();
	public native static boolean mp4SetPlayStatus(int status); // 0 play  1 pause
	public native static int mp4GetPlayStatus(); // 0 play  1 pause

	//remote record
	public native static boolean startRemoteRecord(long userID);
	public native static boolean stopRemoteRecord(long userID);
	public native static boolean getRemoteRecordState(long userID);

	//search remote records
	public native static long searchRecord(long userID, RecSearch search);
	public native static boolean searchNextRecord(long handle, Record result);
	public native static boolean closeSearchRecord(long handle);

	//search remote records
	public native static long searchPicture(long userID, PicSearch search);
	public native static boolean searchNextPicture(long handle, Picture result);
	public native static boolean closeSearchPicture(long handle);

	// do registerStreamDataCallBack first
	public native static long createPicDownload(long userID, Picture pic);
	public native static boolean destoryPicDownload(long downloadHandle);


	//remote playback control
	public native static boolean jumpPlayBack(long pts);
	public native static boolean setPBSpeed(int speed);
	public native static boolean playFrame();
	public native static boolean pausePBPlay();
	public native static boolean continuePBPlay();


	// audio
	public native static boolean openAudio(int audioType);//0 preview 1 remotePlayback
	public native static boolean closeAudio(int audioType);
	public native static boolean getRealAudioState();

	// talk
	public native static boolean startTalk(long userID);
	public native static boolean stopTalk();
	public native static int getTalkUnitSize(long userID);
	public native static boolean sendTalkData(byte[] data, int dataSize, int sampleRate, int sendFormat);


	//ip/wifi/encode/hue/devTime config
	public native static boolean getIPConfig(long userID, IpConfig config);
	public native static boolean setIPConfig(long userID, IpConfig config);
	public native static boolean getWifiConfig(long userID, WifiConfig config);
	public native static boolean setWifiConfig(long userID, WifiConfig config);
	public native static boolean setDevName(long userID, String devName);
	public native static boolean testWifiConfig(long userID, WifiConfig wifiCfg);
	public native static boolean getEncodeVideoConfig(long userID, int encId, VideoEncode encodeVideo);
	public native static boolean setEncodeVideoConfig(long userID, int encId, VideoEncode encodeVideo);
	public native static boolean getVideoBCSS(long userID, BCSS obj);
	public native static boolean setVideoBCSS(long userID, BCSS obj);
	public native static boolean getDeviceTime(long userID, DeviceTime time);
	public native static boolean setDeviceTime(long userID, DeviceTime time);

	// reboot
	public native static boolean restartDev(long userID);
	// reset
	public native static boolean resetDev(long userID);

	// SDCard manage
	public native static boolean getSDCardInfo(long userID, SDCardInfo sdcardInfo);
	public native static boolean startSDCardFormat(long userID, int formatType);
	public native static boolean getSDCardFormatState(SDCardFormat sdcardInfo);
	public native static boolean stopSDCardFormat();
	public native static boolean loadSDCard(long userID);
	public native static boolean unLoadSDCard(long userID);

	//serial
	public native static long startSerialEx(long userID, int serialPort, int serialIndex, int transMode, boolean bEnableRecon, SerialDataCallBackInterface fun);
	public native static long startSerial(long userID, int serialPort, int serialIndex, SerialDataCallBackInterface fun);
	public native static boolean sendSerial(long serialHandle, byte[] sendBuf, int bufLen);
	public native static boolean stopSerial(long serialHandle);
	public native static boolean sendToSerialPort(long userID, int serialPort, int serialIndex, byte[] sendBuf, int bufLen);
	public native static boolean getSerialPortConfig(long userID, SerialPortCfg config);
	public native static boolean setSerialPortConfig(long userID, SerialPortCfg config);
	//save config to flash
	public native static boolean saveDevConfig(long userID);


	// get device type //1:8610 //2:8620 //3: 8810
	public native static int getDeviceFlag(long userID);


	// locate shot, do setShotPath() first
	public native static void setShotOn();//for test
	public native static void setShotPath(String path);
	//device shot, bLocate: if true, save SD . if false, save in "filePath"
	public native static boolean shot(long userID, String filePath, boolean bLocate);

	// system notify
	public native static boolean registerDevNotifyFun();
	public native static boolean getInterruptFlag();
	public native static int  getMDAlarm();
	public native static long getCurrentPts();

	//others
	// convert ms to format "xxxx-xx-xx xx:xx:xx"
	public native static String timeConvert(long userID, long millisecond);
	//save bmp when do setShotOn();
	public native static boolean saveBMP(byte[] y, byte[] u, byte[] v, int videoWidth, int videoHeight);
	public native static boolean send2Sdl(byte[] y, byte[] u, byte[] v, int videoWidth, int videoHeight);
	//use when play by SDL. isFill: if true, fill the rect
	public native static boolean setShowRect(Rect rect, boolean isFill);

	// convert .H264 file to .avi
	public native static long startConvertRecFormat(String srcPath, String dstPath);
	public native static int getConvertProgress(long hRecConvert);
	public native static boolean stopConvertRecFormat(long hRecConvert);
	public native static boolean yuv420sp2yuv420p(byte[] input, int width, int height, byte[] output);
	public native static boolean yuv420sp2yuv(byte[] input, int width, int height, byte[] outy, byte[] outu, byte[] outv);


	//public native static int thumbnailStart();
	//public native static boolean thumbnailAddLocateFile(int handle, String fileName, int fileType);
	//public native static boolean thumbnailAddRemoteFile(int handle, long startTime, long stopTime, int recType, int fileType);
	//public native static boolean thumbnailStop(int handle);

	//eye api
	public native static boolean init(int viewWidth, int viewHeight);
	public native static boolean unInit();


	public native static long createBuffer(int bufferType);//bufferType: 0 YUV 1 RGB
	public native static boolean destroyBuffer(long hBuffer);

	public native static boolean bind(long hWin, long hBuffer);
	public native static boolean isBind(long hWin);
	public native static boolean unbind(long hWin);


	public native static long createWindow(int displayMode);
	public native static int getDisplayMode(long hWin);
	public native static boolean destroyWindow(long hWin);


	public native static boolean setDisplayType(long hWin, int displayType);
	public native static int getDisplayType(long hWin);



	public native static boolean setDebugMode(long hBuffer, byte[] rgbData, int width, int height);
	public native static boolean update(long hBuffer, byte[] data, int videoWidth, int videoHeight); // data: YUV420/RGB

	public native static boolean clear();
	public native static boolean viewport(int x, int y, int width, int height); // 窗口坐标
	public native static boolean draw(long hWin);



	public native static boolean eyeLookAt(long hWin, float vDegrees, float hDegrees, float depth);
	public native static boolean eyeLookAtEx(long hWin, float vDegrees, float hDegrees, float depth, float angle);
	public native static boolean expandLookAt(long hWin,  float hOffset);
	public native static byte[] snapshot(int x, int y, int w, int h, boolean bTrans);
	public native static boolean frameParse(long hBuffer, byte[] frameData, int frameLen);

	public native static float getViewAngle(long hWin);       //观察视角, default 60.0,暂不支持更改
	public native static float getMaxZDepth(long hWin);       //远近深度, MaxZDepth(负)-最远, 0-最近

	public native static float getMinVDegress(long hWin);     //纵向幅度, MaxVDeress(负)-向下, MinVDegress(正)-向上, 0-正
	public native static float getMaxVDegress(long hWin);     //纵向幅度, MaxVDeress(负)-向下, MinVDegress(正)-向上, 0-正

	public native static float getMinHDegress(long hWin);     //横向幅度, [MinHFegress, MaxHDegress], [0,0]无限制
	public native static float getMaxHDegress(long hWin);     //横向幅度, [MinHFegress, MaxHDegress], [0,0]无限制

	public native static int getFieldOfView(long hWin);
	public native static boolean setFieldOfView(long hWin, int fieldOfView);
	public native static boolean setStandardCircle(long hWin, float circleCenterX, float circleCenterY, float r);
	public native static boolean resetStandardCircle(long hWin);
	public native static boolean setImagingType(long hWin, int type);
	public native static int getImagingType(long hWin);
	public native static boolean resetEyeView(long hBuffer);
	public native static float getVerticalCutRatio(long hWin);
	public native static boolean setVerticalCutRatio(long hWin, float verticalCutRatio);

	public native static boolean adjustCircle(byte[] yuv, int width, int height, int luminance, Circle mCircle);

	
}
