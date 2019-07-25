package eye.app.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import com.app.util.*;
import com.app.util.WLANCfg;
import com.fh.lib.*;
import com.fh.lib.Define.IpConfig;
import com.fh.lib.Define.WifiConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class DevWifiSet extends Activity {
	private static final String TAG = "WifiListActivity";
	private static final String DevSSID = "fh8610cam";
	private static final String DevPassword = "12345678";
	private static final int WifiMode = 0;
	private static final int ConnectCount = 15;
	private static final String IP = "172.16.10.1";
	private static final String UserName = "admin";
	private static final String Password = "admin";
	private static final int Port = 8888;
	private static final int SET_SUCCESS    = 0;
	private static final int SET_FAILED     = 1;
	private static final int LOGIN_SUCCESS  = 2;
	private static final int LOGIN_FAILED   = 3;
	private static final String btnText1 = "开始配置设备";
	private static final String btnText2 = "进入WIFI设置";
	private WLANCfg mWifiCfg;
	private String localSSID;
	private List<ScanResult> list;
	private View myInputView;
	private EditText edtPassword, edtDevName;
	private ProgressBar bar;
	private String FILE = "WifiPassword";
	private String Pwd = null;
	private String DevName = "fh8610cam";
	private TextView tvTip;
	private Button btnSet;
	private String foundId = null;
	private Handler handler;
	private Handler handler2;
	private Handler handler3;
	private int falseCount = 0;
	private int falseCount2 = 0;
	private String SysSSID= null;
	private String SysPsk= null;
	//ArrayList<HashMap<String,String>> myArrayList=new ArrayList<HashMap<String,String>>();
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dev_cfg);
		MyApplication.getInstance().addActivity(this);
		mWifiCfg = new WLANCfg(DevWifiSet.this);
		findView();
		SearchDev();
		handler3 = new Handler();
		handler = new Handler();
		handler2 = new Handler(){

			public void handleMessage(Message msg) {
				Log.i(TAG, "handleMessage");
				switch(msg.what){
					case SET_SUCCESS:
					{
						WifiConfig wifiConfig = (new Define()).new WifiConfig();
						IpConfig ipConfig = (new Define()).new IpConfig();
						if (FHSDK.getWifiConfig(PlayInfo.userID, wifiConfig)
								&& FHSDK.getIPConfig(PlayInfo.userID, ipConfig))
						{
							FHSDK.setDevName(PlayInfo.userID, DevName);//设置设备名称

							wifiConfig.sSSID = SysSSID;
							wifiConfig.sPSK = SysPsk;
							wifiConfig.wifiMode = WifiMode;
							ipConfig.isAutoIP = 1;
							if (FHSDK.setWifiConfig(PlayInfo.userID, wifiConfig)
									&&	FHSDK.setIPConfig(PlayInfo.userID, ipConfig))
							{
								tvTip.setText("配置完成！");
								FHSDK.restartDev(PlayInfo.userID);

								ActivtyUtil.openToast(DevWifiSet.this, "配置成功，正在重启设备, 请稍候");
								Intent intent = new Intent(DevWifiSet.this, DeviceSearch.class);
								startActivity(intent);
							}
						}
						else
						{
							//ActivtyUtil.openToast(WifiListActivity.this, "获取配置信息失败，请重试！");
							tvTip.setText("配置失败，请检查密码，重新连接路由器");
							WifiConfiguration tempConfig = mWifiCfg.IsExsits(foundId);
							if (tempConfig != null) {
								//Log.i(TAG,"removeNetWork");
								mWifiCfg.removeNetWork(tempConfig.networkId);
							}
							btnSet.setText(btnText2);
							btnSet.setVisibility(View.VISIBLE);
							bar.setVisibility(View.INVISIBLE);
						}
						FHSDK.apiCleanup();
						Connect(SysSSID, SysPsk, 3);
						break;
					}
					case SET_FAILED:
					{
						tvTip.setText("配置失败，请检查密码，重新连接路由器");
						WifiConfiguration tempConfig = mWifiCfg.IsExsits(foundId);
						if (tempConfig != null) {
							//Log.i(TAG,"removeNetWork");
							mWifiCfg.removeNetWork(tempConfig.networkId);
						}
						btnSet.setText(btnText2);
						btnSet.setVisibility(View.VISIBLE);
						bar.setVisibility(View.INVISIBLE);
						break;
					}
					case LOGIN_SUCCESS:
					{
						//tvTip.setText("正在测试, 请稍候...");
						handler.postDelayed(runnable, 2000);
						break;
					}
					case LOGIN_FAILED:
					{
						Log.e(TAG, String.valueOf(PlayInfo.userID));
						//ActivtyUtil.openToast(WifiListActivity.this, "登录失败...");
						tvTip.setText("配置失败，请检查路由器连接");
						WifiConfiguration tempConfig = mWifiCfg.IsExsits(foundId);
						if (tempConfig != null) {
							mWifiCfg.removeNetWork(tempConfig.networkId);
						}
						btnSet.setText(btnText2);
						btnSet.setVisibility(View.VISIBLE);
						bar.setVisibility(View.INVISIBLE);
						break;
					}
					default:
						break;
				}
				super.handleMessage(msg);
			}
		};
	}

	public void findView(){
		tvTip = (TextView)findViewById(R.id.tvPsk);
		int color = Color.GREEN;//Color.argb(255,255,255,255);
		tvTip.setTextColor(color);
		tvTip.setText("未找到设备");
		btnSet = (Button)findViewById(R.id.btnreset);
		btnSet.setText(btnText1);
		btnSet.setOnClickListener(btnSetListener);
		btnSet.setVisibility(View.INVISIBLE);

		bar = (ProgressBar)findViewById(R.id.progressBar1);
		bar.setVisibility(View.INVISIBLE);
	}
	private DialogInterface.OnClickListener OnSureClickLister2 = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Pwd = edtPassword.getText().toString();
		}
	};
	private DialogInterface.OnClickListener OnCancelClickLister2 = new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog,
							int which) {
			//dlgCloseFlag = true;
		}
	};

	private DialogInterface.OnClickListener OnSureClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			tvTip.setText("配置过程需要一段时间，请耐心等待...");
			btnSet.setVisibility(View.INVISIBLE);
			bar.setVisibility(View.VISIBLE);

			Pwd = edtPassword.getText().toString();
			if (edtDevName.getText().toString() != null)
				DevName = edtDevName.getText().toString();
			//Log.i(TAG, "password = "+ Pwd + " DevName = " + DevName);
			try {
				localSSID = mWifiCfg.getSSID();
				//Log.i(TAG, "localSSID = "+ localSSID);
				SysSSID = localSSID;
				SysPsk = Pwd;
				if (Connect(foundId, DevPassword, 3)){
					handler3.postDelayed(runnable2, 2000);
				}else{
					//ActivtyUtil.openToast(WifiListActivity.this, "");
					tvTip.setText("无法连接到网络，请检查路由器连接");
					btnSet.setText(btnText2);
					bar.setVisibility(View.INVISIBLE);
				}

			} catch (Exception e) {
				//Log.e(TAG, e.getMessage(), e);
				ActivtyUtil.openToast(DevWifiSet.this, e
						.getMessage());
			}

		}
	};

	private DialogInterface.OnClickListener OnCancelClickLister = new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog,
							int which) {
			//dlgCloseFlag = true;
		}
	};

	private View.OnClickListener btnSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			if (btnSet.getText().equals(btnText1)){
				LayoutInflater layoutInflater = LayoutInflater.from(DevWifiSet.this);
				myInputView = layoutInflater.inflate(R.layout.dlg_input, null);
				edtPassword = (EditText)myInputView.findViewById(R.id.edtDNS);
				edtDevName = (EditText)myInputView.findViewById(R.id.editText2);
				final String DevSSID = foundId;
				final AlertDialog.Builder builder = new AlertDialog.Builder(DevWifiSet.this);
				builder.setTitle("配置设备");
				builder.setView(myInputView).setPositiveButton("确定", OnSureClickLister);
				builder.setView(myInputView).setNegativeButton("取消", OnCancelClickLister);
				AlertDialog alert = builder.create();
				alert.show();
			}
			else if (btnSet.getText().equals(btnText2)){
				//btnSet.setText(btnText1);
				Intent intent = new Intent("android.settings.WIFI_SETTINGS");
				startActivity(intent);
			}
		}
	};
	Runnable runnable2=new Runnable(){
		@Override
		public void run() {

			FHSDK.logout(PlayInfo.userID);
			FHSDK.apiInit();
			PlayInfo.userID = FHSDK.login(IP, Port, UserName, Password);
			if (PlayInfo.userID !=0)
			{
				//Log.i(TAG, "login success , FHSDK.userID = " + String.valueOf(PlayInfo.userID));
				//return FHSDK.userID;
				handler3.removeCallbacks(this);
				Message msg = handler2.obtainMessage();
				msg.what = LOGIN_SUCCESS;
				handler2.sendMessage(msg);
				return;
			}
			else if(++falseCount2 > ConnectCount)
			{
				handler3.removeCallbacks(this);
				Message msg = handler2.obtainMessage();
				msg.what = LOGIN_FAILED;
				handler2.sendMessage(msg);
				return;
			}
			handler3.postDelayed(this, 2000);
		}
	};

	Runnable runnable=new Runnable(){
		@Override
		public void run() {
			//
			WifiConfig wifiConfig = (new Define()).new WifiConfig();
			wifiConfig.sSSID = SysSSID;
			wifiConfig.sPSK = SysPsk;
			wifiConfig.sChan = "0";
			wifiConfig.wifiMode = WifiMode;
			wifiConfig.wifiType = 4;
			//Log.i(TAG, SysSSID+"/"+ SysPsk+"/"+WifiMode);
			boolean isSuccess = FHSDK.testWifiConfig(PlayInfo.userID, wifiConfig);
			//Log.i(TAG,"ret = " + String.valueOf(ret) + "  wifiConfig.Status = " + String.valueOf(wifiConfig.status));

			if (!isSuccess)
			{
				if (++falseCount > 10)
				{
					handler.removeCallbacks(this);
					Message msg = handler2.obtainMessage();
					msg.what = SET_FAILED;
					handler2.sendMessage(msg);
					return;
				}
			}
			if (isSuccess)
			{
				falseCount = 0;
				//ActivtyUtil.openToast(WifiListActivity.this, "正在测试WIFI坏境，请稍候...");
				if (0 == wifiConfig.status)
				{
					//ActivtyUtil.openToast(WifiListActivity.this, "测试失败");
					Log.i(TAG, "test failed");
					handler.removeCallbacks(this);
					Message msg = handler2.obtainMessage();
					msg.what = SET_FAILED;
					handler2.sendMessage(msg);
					return;
				}
				else if (1 == wifiConfig.status)
				{
					//SystemClock.sleep(5000);
					//continue;
					Log.i(TAG, "test continue");
				}
				else
				{
					Log.i(TAG, "test success");
					handler.removeCallbacks(this);
					Message msg = handler2.obtainMessage();  //new Message();
					msg.what = SET_SUCCESS;
					handler2.sendMessage(msg);
					return;
				}
			}

			handler.postDelayed(this, 5000);
		}
	};

	public boolean Connect(String SSID, String Password, int Type){
		String ssid = mWifiCfg.getSSID();
		if (null == ssid)
			return false;

		mWifiCfg.disConnectionWifi(mWifiCfg.getNetWordId());
		mWifiCfg.openWifi();
		WifiConfiguration wifiConfig = mWifiCfg.CreateWifiInfo(SSID, Password, Type);
		//

		if (wifiConfig == null) {
			return false;
		}
		WifiConfiguration tempConfig = mWifiCfg.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiCfg.removeNetWork(tempConfig.networkId);
		}
		boolean bRet = mWifiCfg.addNetWork(wifiConfig);
		ssid = mWifiCfg.getSSID();
		return bRet;
	}
	public void SearchDev(){
		while (mWifiCfg.checkState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				tvTip.setText("正在打开WLAN, 请稍候...");
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
		mWifiCfg.startScan();
		list = mWifiCfg.getWifiList();

		boolean isGetDev = false;
		if (null == list)
		{
			tvTip.setText("没有找到设备");
			return;
		}

		for (int i = 0; i < list.size(); i++)
		{
			//if (list.get(i).SSID.equals())
			if (isPartSame(DevSSID, list.get(i).SSID))
			{
				foundId = list.get(i).SSID;
				isGetDev = true;
				break;
			}
		}
		if (!isGetDev){
			tvTip.setText("没有找到设备");
		}else{
			tvTip.setText("发现设备："+ foundId);
			btnSet.setVisibility(View.VISIBLE);
		}

	}

	public boolean isPartSame(String str1, String str2){
		if (str2.indexOf(str1) != -1)
			return true;
		return false;
	}
}
