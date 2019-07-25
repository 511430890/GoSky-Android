package eye.app.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.app.util.*;
import com.app.util.WLANCfg;
import com.fh.lib.*;
import com.fh.lib.Define.DeviceTime;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainMenu extends Activity{
	private static final String TAG = "DevCfgActivity";
	public ListView myList;
	private SimpleAdapter simpleAdapter;
	private SharedPreferences sp = null;
	private long mExitTime;
	private Handler handler, hDevNotify, hWifiLost;
	private static final int NOTIFY_MD_HAPPEN = 0;
	private static final int NOTIFY_MD_STOP   = 1; 
	private static final int NOTIFY_QUIT = 2;
	private static final int NOTIFY_WIFI_RECONNECT = 3;
	private int notifyID = 0;
	//private Thread mThread = null;
	//private boolean isRecording = true;
	private MoreCfg moreCfgObj = new MoreCfg();
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
	public Context mContext;
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		
		mContext = this;
		setTitle(getString(R.string.title_mainMenu));
		setContentView(R.layout.dev_cfg_list);
		MyApplication.getInstance().addActivity(this);
		initView();
		//getUserID();
		handler = new Handler(){
			
			public void handleMessage(Message msg) {
				switch(msg.what){
					case NOTIFY_QUIT:
					{
						break;
					}
					
					case NOTIFY_MD_HAPPEN:
					{
						//showNotification();
						break;
					}
					case NOTIFY_MD_STOP:
					{
						//showNotification();
						break;
					}
					case NOTIFY_WIFI_RECONNECT:
					{
						//relogin();
						break;
					}
					default:
						break;
				}
				super.handleMessage(msg);
			}
		};
		hDevNotify = new Handler();
		hDevNotify.post(notifyMDThread);
		if (PlayInfo.userID != 0){
			FHSDK.registerDevNotifyFun();
		}		
	}
	public int getMDStatus(){
		int mdStatus = 0;
		SharedPreferences sp = null;
		sp = getSharedPreferences(moreCfgObj.FILE, MODE_PRIVATE);
		if (null != sp)
		{
			if ("" == sp.getString("MDStatus",""))
			{
				mdStatus = 0;
			}
			else
			{
				mdStatus = Integer.parseInt(sp.getString("MDStatus",""));
			}
		}
		return mdStatus;
	}
	Runnable notifyMDThread = new Runnable(){ 
		
		@Override
		public void run() {
			if (1 == getMDStatus()){
				if(1 == FHSDK.getMDAlarm()){
					Message msg = handler.obtainMessage(); 
					msg.what = NOTIFY_MD_HAPPEN;
					handler.sendMessage(msg);
				}
				else if (0 == FHSDK.getMDAlarm())
				{
					Message msg = handler.obtainMessage(); 
					msg.what = NOTIFY_MD_STOP;
					handler.sendMessage(msg);
				}
			}
			if(FHSDK.getInterruptFlag()){
				Message msg = handler.obtainMessage(); 
				msg.what = NOTIFY_QUIT;
				handler.sendMessage(msg);
				return;
			}
			
			hDevNotify.postDelayed(notifyMDThread, 100);
		}
	};	
	Runnable wifiListenerThread = new Runnable(){ 
		WLANCfg wlan;
		List<ScanResult> list;
		@Override
		public void run() {
			wlan = new WLANCfg(MainMenu.this);
			if ( null != LocateInfo.wifiName && !LocateInfo.wifiName.equals(wlan.getSSID()))
			{
				wlan.startScan();
				list = wlan.getWifiList();
				if (null != list)
				{
					for (int i = 0; i < list.size(); i++)
					{
						if (LocateInfo.wifiName.equals(list.get(i).SSID))
						{
							int ip = wlan.getIpAddress();
							if (ip != 0)
							{
								wlan.disConnectionWifi(wlan.getNetWordId());
								wlan.openWifi();
						        WifiConfiguration wifiConfig = wlan.CreateWifiInfo(LocateInfo.wifiName, LocateInfo.wifiPwd, 3);
						        WifiConfiguration tempConfig = wlan.IsExsits(LocateInfo.wifiName);
						        if (tempConfig != null) {
						        	wlan.removeNetWork(tempConfig.networkId);
						        }
						        wlan.addNetWork(wifiConfig);
							}
						
						}
					}
				}
			}
			hWifiLost.postDelayed(wifiListenerThread, 1000);
		}
	};	


	public void initView(){
		myList = (ListView)findViewById(R.id.listView1);
		myList.setOnItemClickListener(listItemListener);
		addListItem();

		hWifiLost = new Handler();
		hWifiLost.postDelayed(wifiListenerThread, 100);
	}
	public void addListItem(){
        HashMap<String, Object> map;
        //listItems itemIndex = listItems.PREIVIEW;


		String[] strCfgItem = mContext.getResources().getStringArray(R.array.mainMunuItem);

        
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.menu_preview);
        map.put("tvItemName", strCfgItem[0]);
        listItem.add(map);
		
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.menu_rec_remote);
        map.put("tvItemName", strCfgItem[1]);
        listItem.add(map);
        
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.menu_rec_locate);
        map.put("tvItemName", strCfgItem[2]);
        listItem.add(map);
        
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.sdcard_48px); 
        map.put("tvItemName", strCfgItem[3]);
        listItem.add(map);

        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.wifi_48px); 
        map.put("tvItemName", strCfgItem[4]);
        listItem.add(map);
        
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.net_48px);
        map.put("tvItemName", strCfgItem[5]);
        listItem.add(map);
		
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.net_48px);
        map.put("tvItemName", strCfgItem[6]);
        listItem.add(map);  
		
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.reboot_48px);
        map.put("tvItemName", strCfgItem[7]);
        listItem.add(map);
        
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.reset_48px);
        map.put("tvItemName", strCfgItem[8]);
        listItem.add(map);
		
        map = new HashMap<String, Object>(); 
        map.put("ItemImage", R.drawable.more_48px);
        map.put("tvItemName", strCfgItem[9]);
        listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.more_48px);
		map.put("tvItemName", strCfgItem[10]);
		listItem.add(map);

        simpleAdapter=new SimpleAdapter(this, listItem, R.layout.cfg_item, new String[]{"ItemImage","tvItemName"},  new int[]{R.id.ItemImage,R.id.tvItemName});
        myList.setAdapter(simpleAdapter);
	}	
	
	public enum listItems{
		PREIVIEW(0), 
		REMOTE_RECORD_LIST(1), 
		LOCATE_RECORD_LIST(2), 
		REMOTE_SDCARD(3), 
		WIFI_CONFIG(4), 
		NET_CONFIG(5), 
		DEVICE_REBOOT(6), 
		DEVICE_RESET(7), 
		MORE_CONFIG(8);
		private int mIndex = 0;
		
		private listItems(int index)
		{
			mIndex = index;
		}
		public int getIndex()
		{
			return mIndex;
		}
	}
	

	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (0 == PlayInfo.userID &&  (2 != arg2 && 9 != arg2))
				return;
			
			Intent intent = null;

			switch(arg2){
			case 0:
				
				PlayInfo.playType      = FHSDK.PLAY_TYPE_PREVIEW;	//0 preview 
				PlayInfo.transMode     = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_TRANS_MODE);
				PlayInfo.streamType    = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_STREAM_TYPE);
				PlayInfo.frameCacheNum = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_FRAME_CACHE);
				PlayInfo.locateRecType = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_REC_TYPE);
				PlayInfo.decodeType    = moreCfgObj.getDecodecType(mContext);
				PlayInfo.displayMode   = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_DISPLAY_MODE);

				intent = new Intent(mContext, VideoPlayByOpengl.class);
				startActivity(intent);

				break;
			case 1:
				intent = new Intent(MainMenu.this, RemoteRecSearch.class);
				startActivity(intent);
				break;
			case 2:
			    intent = new Intent(MainMenu.this,  LocateRecList.class);  
	            startActivity(intent); 
				break;
			case 3:
				intent = new Intent(MainMenu.this, SDManage.class);
				startActivity(intent);
				break;	
			case 4:
				intent = new Intent(MainMenu.this, WifiCfg.class);
				startActivity(intent);
				break;
			case 5:
				intent = new Intent(MainMenu.this, IpCfg.class);
				startActivity(intent);
				break;
			case 6:
				intent = new Intent(MainMenu.this, VideoEncodeCfg.class);
				startActivity(intent);
				break;
			case 7:
				intent = new Intent(MainMenu.this, SerialCfg.class);
				startActivity(intent);
				break;
			case 8:
				new AlertDialog.Builder(MainMenu.this).setTitle(mContext.getString(R.string.btn_restart))
				.setMessage(mContext.getString(R.string.str_restartDevice)).setPositiveButton(
						mContext.getString(R.string.id_sure), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								try {
									FHSDK.restartDev(PlayInfo.userID);
									ActivtyUtil.openToast(MainMenu.this, mContext.getString(R.string.str_restartOK));
									Intent intent = new Intent(MainMenu.this, Login.class);
									startActivity(intent);
								} catch (Exception e) {
									//Log.e(TAG, e.getMessage(), e);
									ActivtyUtil.openToast(MainMenu.this, e
											.getMessage());
								}
							}
						}).setNegativeButton(mContext.getString(R.string.id_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}

						}).show();
				break;	
			case 9:
				
				new AlertDialog.Builder(MainMenu.this).setTitle(mContext.getString(R.string.btn_reset))
				.setMessage(mContext.getString(R.string.str_resetDevice)).setPositiveButton(
						mContext.getString(R.string.id_sure), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								try {
									FHSDK.resetDev(PlayInfo.userID);
									ActivtyUtil.openToast(MainMenu.this, mContext.getString(R.string.str_resetOK));
									Intent intent = new Intent(MainMenu.this, Login.class);
									startActivity(intent);
								} catch (Exception e) {
									//Log.e(TAG, e.getMessage(), e);
									ActivtyUtil.openToast(MainMenu.this, e
											.getMessage());
								}
							}
						}).setNegativeButton(mContext.getString(R.string.id_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}

						}).show();
				/*		
		        if (mThread == null) {
		            mThread = new Thread(new testAu(), "TestThread");
		            mThread.start();
		        }
		        */
				break;
			case 10:
				intent = new Intent(MainMenu.this, MoreCfg.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK &&  0 != PlayInfo.userID) {
                        if ((System.currentTimeMillis() - mExitTime) > 2000) {
                                //Object mHelperUtils;
                                ActivtyUtil.openToast(MainMenu.this, mContext.getString(R.string.str_doubleClickToExit));
                                mExitTime = System.currentTimeMillis();
								return true;

                        } else {
                        	FHSDK.logout(PlayInfo.userID);
							Intent intent = new Intent(MainMenu.this, Login.class);
							startActivity(intent);
							return true;
                        }
                }
                return super.onKeyDown(keyCode, event);
    }

}

