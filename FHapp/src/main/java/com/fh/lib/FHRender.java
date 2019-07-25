package com.fh.lib;

import android.graphics.Rect;

import com.fh.lib.Define.BCSS;
import com.fh.lib.Define.CbDataInterface;
import com.fh.lib.Define.Circle;
import com.fh.lib.Define.DevSearch;
import com.fh.lib.Define.DeviceTime;
import com.fh.lib.Define.IpConfig;
import com.fh.lib.Define.PBRecTime;
import com.fh.lib.Define.PicSearch;
import com.fh.lib.Define.Picture;
import com.fh.lib.Define.RecSearch;
import com.fh.lib.Define.Record;
import com.fh.lib.Define.SDCardFormat;
import com.fh.lib.Define.SDCardInfo;
import com.fh.lib.Define.SerialDataCallBackInterface;
import com.fh.lib.Define.StreamDataCallBackInterface;
import com.fh.lib.Define.VideoEncode;
import com.fh.lib.Define.WifiConfig;
import com.fh.lib.Define.YUVDataCallBackInterface;

public class FHRender {

	public native static boolean init(int viewWidth, int viewHeight);
	public native static boolean unInit();


	public native static int createBuffer(int bufferType);//bufferType: 0 YUV420p 1 RGB YUV420SP
	public native static int destroyBuffer(int hBuffer);

	public native static boolean bind(int hWin, int hBuffer);
	public native static boolean isBind(int hWin);
	public native static boolean unbind(int hWin);


	public native static int createWindow(int displayMode);
	public native static int getDisplayMode(int hWin);
	public native static boolean destroyWindow(int hWin);


	public native static boolean setDisplayType(int hWin, int displayType);
	public native static int getDisplayType(int hWin);


	public native static boolean setDebugMode(int hBuffer, byte[] rgbData, int width, int height);

	public native static boolean update(int hBuffer, byte[] data, int videoWidth, int videoHeight); // data: YUV420/RGB

	public native static boolean clear();
	public native static boolean viewport(int x, int y, int width, int height); // 窗口坐标
	public native static boolean draw(int hWin);

	public native static float getMaxZDepth(int hWin);       //远近深度, MaxZDepth(负)-最远, 0-最近

}
