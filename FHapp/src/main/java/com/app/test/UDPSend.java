package com.app.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.app.util.ActivtyUtil;
import com.app.util.UDPServer;
import com.app.util.log;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;
import com.fh.lib.Define.SerialDataCallBackInterface;

import android.util.Log;

public class UDPSend {
	private UDPServer mUdpServer = new  UDPServer();
	private InetAddress mDevAddr;
	private final String DEV_UDP_IP = "172.16.1.177";
	private final int DEV_UDP_PORT = 8888;
	private boolean bStart = false;
	private long serialHandle;
	public static int sleepTime = 50;
	class SendThread implements Runnable {
	    public void run() {
	    	while(true)
	    	{
	    		if (!bStart)
	    			break;
	    		
	    		writeUDPCmd(new byte[]{0x66, 0x00, 0x00,  (byte)0xff, 0x00, 0x00, 0x00, (byte) 0x99});
	    		
	    		try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	}

	private SerialDataCallBackInterface serialFun = new SerialDataCallBackInterface(){
		public int SerialDataCallBack(int serialHandle, byte[] buffer, int bufferLen){
			Log.e("xxx","serialHandle = " + serialHandle + "| len = " + bufferLen);
			return 0;
		}
	};
	public void startSend()
	{
//		if(!mUdpServer.isRun())
//		{
//			mUdpServer.Start();
//		}

		int serialPort = 1;
		int serialIndex = 1;
		int transMode = 1; // 0 tcp  1 udp
		serialHandle = FHSDK.startSerialEx(PlayInfo.userID, serialPort, serialIndex, transMode, true, serialFun);
		if (0 == serialHandle)
		{
			Log.e("xxx","startSerial() return ERROR!");
			return;
		}
		
		bStart = true;
    	Thread mSendThread = new Thread(new SendThread(), "SendThread");
    	mSendThread.start();
		
	}
	public void stopSend()
	{
		bStart = false;
//		if(mUdpServer.isRun())
//		{
//			mUdpServer.Stop();
//		}
		if (0 != serialHandle)
			FHSDK.stopSerial(serialHandle);
	}
	
	
	private void writeUDPCmd(byte[] data)
	{
//		try {
//			mDevAddr = InetAddress.getByName(DEV_UDP_IP);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		mUdpServer.addCmdP(data, mDevAddr, DEV_UDP_PORT);
		FHSDK.sendSerial(serialHandle, data, data.length);
	}	
}
