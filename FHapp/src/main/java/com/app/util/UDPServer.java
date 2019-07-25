package com.app.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.app.util.log;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPServer {
	public static final int UDP_RECEIVE_DATA = 10001;
	public static final int UDP_SERVER_INIT = 10002;
	public static final String TAG = "UDPServer";
	private DatagramSocket mUDPSocket;
	private boolean isWork = true;
	private Thread mReceiveThread;
	private Thread mWriteThread;
	private Handler mHandler = new Handler();
	private Handler mAccesser;
	private List<DatagramPacket> mCmdPool = new ArrayList<DatagramPacket>();
	Runnable runUDPReceiveThread = new Runnable() {
		@Override
		public void run() {
			mReceiveThread = new Thread(new UDPReceiveThread());
			mReceiveThread.start();
		}
	};
	Runnable runUDPWriteThread = new Runnable() {
		@Override
		public void run() {
			mWriteThread = new Thread(new UDPWriteThread());
			mWriteThread.start();
		}
	};
	public void Start() {
		isWork = true;
		StartServer();
		mReceiveThread = new Thread(new UDPReceiveThread());
		mReceiveThread.start();
		mWriteThread = new Thread(new UDPWriteThread());
		mWriteThread.start();
	}

	private void StartServer() {
		try {
			mUDPSocket = new DatagramSocket();
			return;
		} catch (IOException ex) {
			isWork = false;
			System.out.print(ex.toString());
		}
	}

	public boolean isRun() {
		return !(mUDPSocket == null || mUDPSocket.isClosed());
	}

	public void Stop() {
		isWork = false;
		mHandler.removeCallbacks(runUDPReceiveThread);
		mHandler.removeCallbacks(runUDPWriteThread);
		if (mReceiveThread != null) {
			mReceiveThread.interrupt();
			mReceiveThread = null;
		}
		if (mWriteThread != null) {
			mWriteThread.interrupt();
			mWriteThread = null;
		}
		if (mUDPSocket != null) {
			mUDPSocket.close();
			mUDPSocket = null;
		}
	}

	class UDPReceiveThread implements Runnable {		

		@Override
		public void run() {
			byte[] message = new byte[262144];
			DatagramPacket dataPacket = new DatagramPacket(message,
					message.length);

			if(mAccesser!=null)
			{
				Message msg = new Message();
				msg.what = UDP_SERVER_INIT;
				mAccesser.sendMessage(msg);
			}
			try {
				while (isWork) {
					mUDPSocket.receive(dataPacket);
					byte[] recData = new byte[dataPacket.getLength()];
					System.arraycopy(dataPacket.getData(),0,recData,0,recData.length);
//					Log.i(TAG, "receive recData Lenght:"+dataPacket.getLength());
					if(mAccesser!=null)
					{
						Message msg = new Message();
						msg.obj = recData;
						msg.what = UDP_RECEIVE_DATA;
						mAccesser.sendMessage(msg);
					}
					String ipAddr = dataPacket.getAddress().getHostAddress().toString();
					Log.i(TAG, "UDPReceiveThread  IP:" + ipAddr + "   Port:"
							+ dataPacket.getPort() + "   RecData:"
							+ recData);
				}
			} catch (Exception e) {
				mHandler.postDelayed(runUDPReceiveThread, 1000);
				System.out.print(e.toString());
			}
		}
	}
	class UDPWriteThread implements Runnable {		
		@Override
		public void run() {
			try {
				while (isWork) {
					if(mCmdPool.size()>0)
					{
						Log.i(TAG, "WriteThread  IP:" + mCmdPool.get(0).getAddress().toString() + "   Port:"
								+  mCmdPool.get(0).getPort() + "   Data:"
								+ ActivtyUtil.byte2HexStr(mCmdPool.get(0).getData()));
						mUDPSocket.send(mCmdPool.get(0));
						mCmdPool.remove(0);
					}
					//Thread.sleep(1);
				}
			} catch (Exception e) {
				mHandler.postDelayed(runUDPWriteThread, 1000);
				System.out.print(e.toString());
			}
		}
	}
	public void setAccesser(Handler accesser) {
		this.mAccesser = accesser;
	}

	public void addCmdP(byte[] Msg,InetAddress addr,int port) {
		if (mUDPSocket != null && addr != null && Msg != null && Msg.length > 0) {
			mCmdPool.add(new DatagramPacket(Msg, Msg.length,addr, port));
		}
	}
}
