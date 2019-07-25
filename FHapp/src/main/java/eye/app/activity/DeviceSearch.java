package eye.app.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.app.permission.PermissionHelper;
import com.app.permission.PermissionInterface;
import com.app.util.ActivtyUtil;
import com.app.util.WLANCfg;
import com.app.util.log;
import com.app.view.VideoPlayView;
import com.fh.lib.Define;
import com.fh.lib.Define.DevSearch;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
//import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DeviceSearch extends Activity implements PermissionInterface {
	private static String TAG = "SearchActivity";
	private long handle;
	private Button btnSearch, btnManual, btnOneKey,btnWIFI;
	private TextView tv;
	private ListView myListView;
	private MulticastLock multicastLock = null;
	private SimpleAdapter mySimpleAdapter;
	private String SSID = null;
	private WLANCfg mWifiCfg;
	private Handler handler, handler2;
	//private int isAlive;
	private static final int UPDATE = 1;
	private View myInputView;
	private EditText edtIpAddr, edtPort;
	private RadioGroup devType = null;
	private LinearLayout linear;
	private
	DevSearch searchResult = null;
	private String[] strDevType = {"FH8610","FH8620","FH8810"};
	private ArrayAdapter<String> devTypeAdapter;
	ArrayList<HashMap<String,String>> myArrayList=new ArrayList<HashMap<String,String>>();

	private PermissionHelper mPermissionHelper;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		Log.i(TAG,"onCreate()");
		setTitle(getString(R.string.title_deviceSearch));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		MyApplication.getInstance().addActivity(this);
		allowMulticast();
		Init();
		findView();
		//searchDevList();

		mPermissionHelper = new PermissionHelper(this, this);
		mPermissionHelper.requestPermissions();



		handler = new Handler(){

			public void handleMessage(Message msg) {
				switch(msg.what){
					case UPDATE:
					{
						//Log.i(TAG, "searchDevList()");
						searchDevList();
						break;
					}
					default:
						break;
				}
				super.handleMessage(msg);
			}
		};
		handler2 = new Handler();
		handler2.post(updateThread);
	}
	Runnable updateThread = new Runnable(){
		@Override
		public void run() {
			//Log.i(TAG, String.valueOf(SDLActivity.getDevStatus()));
			if(FHSDK.getDevStatus() > 0){
				Message msg = handler.obtainMessage();
				msg.what = UPDATE;
				handler.sendMessage(msg);
			}
			handler2.postDelayed(updateThread, 50);
		}
	};

	public void findView(){
		btnSearch = (Button)findViewById(R.id.btnSearch);
		btnManual = (Button)findViewById(R.id.btnManual);
		btnOneKey = (Button)findViewById(R.id.btnOneKey);
		btnWIFI = (Button)findViewById(R.id.btnWIFI);
		btnSearch.setOnClickListener(btnSearchListener);
		btnManual.setOnClickListener(btnManualListener);
		btnOneKey.setOnClickListener(btnOneKeyListener);
		btnWIFI.setOnClickListener(btnWIFIListener);

		myListView = (ListView)findViewById(R.id.listView);
		myListView.setOnItemClickListener(listItemListener);

		//tv = (TextView)findViewById(R.id.textView);
		//tv.setText("点击搜索按钮进行搜索");
		tv = (TextView)findViewById(R.id.tvDNS);
		linear = (LinearLayout)findViewById(R.id.menu);

	}

/*
	public boolean onCreateOptionsMenu(Menu menu) {  
	    // TODO Auto-generated method stub  
	    super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();  
        inflater.inflate(R.menu.menu, menu);  
        return true;  
        menu.add(Menu.NONE, Menu.FIRST + 6, 6, "退出").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();  
		Intent intent;  
	    switch (item_id) {  
	    case R.id.exit:  
	        {
            	multicastLock.release();//
            	//MyApplication.getInstance().exit();
				for (int i = 0; i < MyApplication.getInstance().activityList.size(); i++){
					if (null != MyApplication.getInstance().activityList.get(i)) {
						Log.v(TAG, "close Activity:" + MyApplication.getInstance().activityList.get(i));
						MyApplication.getInstance().activityList.get(i).finish();
					}
				}
				System.exit(0);
	        }
	        return true; 
	    }  
	    return false;  
	}*/

	public void Init(){
		FHSDK.searchCleanup();
		FHSDK.searchInit();

		FHSDK.registerDevStateFun();
		mWifiCfg = new WLANCfg(DeviceSearch.this);
		SSID = mWifiCfg.getSSID();
		searchResult = (new Define()).new DevSearch();
	}
	public void searchDevList(){
		myArrayList.clear();
		myListView.setAdapter(mySimpleAdapter);
		HashMap<String, String> map;

		handle = FHSDK.searchDev();

		while(true){
			if (FHSDK.searchNextDev(handle, searchResult))
			{
				if (searchResult.isAlive > 0)
				{
					map = new HashMap<String, String>();
					Log.i(TAG, "devName = " + searchResult.devName+ "|ip = " + searchResult.devIP);
					map.put("name", searchResult.devName);  //map.put(参数名字,参数值)
					map.put("ip", searchResult.devIP);
					map.put("port", searchResult.port);
					myArrayList.add(map);
				}
			}
			else
			{
				FHSDK.searchDevClose(handle);
				break;
			}

			mySimpleAdapter=new SimpleAdapter(this, myArrayList, R.layout.search_item, new String[]{"name","ip"},  new int[]{R.id.name,R.id.ip});
			myListView.setAdapter(mySimpleAdapter);
		}


		if (0 == myListView.getCount())
		{
			tv.setText("搜索结果为空");
			//tv2.setVisibility(View.INVISIBLE);
		}
		else
		{
			tv.setText("设备列表:");
			//tv2.setVisibility(View.VISIBLE);
		}


	}
    /*
    protected void onPause() {
        Log.e(TAG, "onPause()");
        super.onPause();
    }
	protected void onResume(){
        Log.e(TAG, "onResume()");
        super.onResume();
		mWifiCfg = new WLANCfg(SearchActivity.this);
        String SSID = mWifiCfg.getSSID();
        Log.e(TAG, "SSID = " + SSID);
        int status = mWifiCfg.checkState();
        Log.e(TAG, "status = " + status);
        while((SSID = mWifiCfg.getSSID()) == null
        		&& (mWifiCfg.checkState() == WifiManager.WIFI_STATE_ENABLED
        		   || mWifiCfg.checkState() == WifiManager.WIFI_STATE_ENABLING)){
   		   try {
   			   mWifiCfg = new WLANCfg(SearchActivity.this);
    			 Log.e(TAG, "OPEN WLAN ing");
   			    //tv.setText("正在打开WLAN, 请稍候...");
  			    Thread.currentThread();
  			    Thread.sleep(100);
  		   } catch (InterruptedException ie) {
  		   }
        }
        if(SSID != null && !SSID.equals(this.SSID) && mWifiCfg.checkState() == WifiManager.WIFI_STATE_ENABLED)
        {
        	ActivtyUtil.openToast(SearchActivity.this, "已连接到" + SSID);
        	Log.e(TAG, "have change");
			String ip = "172.16.10.1";
			String name = "admin";
			String pwd = "admin";
			int port = 8888;
			SDLActivity.userID = SDLActivity.login(ip, port, name, pwd);
			if (SDLActivity.userID !=0 ){
				//Intent intent = new Intent(SearchActivity.this, SDLActivity.class);
				//startActivity(intent);
				WifiSetActivity wifiConfig = new WifiSetActivity();
				if (0 == SDLActivity.getWifiConfig(SDLActivity.userID, wifiConfig))
				{
					wifiConfig.sSSID = this.SSID;
					wifiConfig.wifiType = 0;//typeSeldID;
					//Log.i(TAG, wifiConfig.sSSID +"/" + wifiConfig.sPSK);
					if (0 == SDLActivity.setWifiConfig(SDLActivity.userID, wifiConfig))
					{
						//ActivtyUtil.openToast(WifiSetActivity.this, "设置成功，重启设备生效");
						//Intent intent = new Intent(WifiSetActivity.this, CfgActivity.class);
						//startActivity(intent);
					}
					//else
						//ActivtyUtil.openToast(WifiSetActivity.this, "设置失败！");
				}
				else
				{
					ActivtyUtil.openToast(SearchActivity.this, "获取配置信息失败，请重试！");
				}
			}
			else{
				Log.e(TAG, String.valueOf(SDLActivity.userID));
				ActivtyUtil.openToast(SearchActivity.this, "登陆失败...");
			}
        }
        else
        {
        	Log.e(TAG, "not change");
        }
	}
	*/

	private View.OnClickListener btnWIFIListener = new View.OnClickListener(){
		public void onClick(View v) {
			//handler2.removeCallbacks(updateThread);
			Intent intent = new Intent(DeviceSearch.this, DevWifiSet.class);
			//Intent intent = new Intent("android.settings.WIFI_SETTINGS");
			startActivity(intent);
		}
	};
	private View.OnClickListener btnSearchListener = new View.OnClickListener(){
		public void onClick(View v) {
			VideoPlayView.getSDCardPath();
			searchDevList();
		}
	};
	private View.OnClickListener btnManualListener = new View.OnClickListener(){
		public void onClick(View v) {
			//handler2.removeCallbacks(updateThread);
			Intent intent = new Intent(DeviceSearch.this, Login.class);
			startActivity(intent);
		}
	};
	private RadioGroup.OnCheckedChangeListener devTypeCheckListener = new RadioGroup.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			int radioBtnId = arg0.getCheckedRadioButtonId();
			//RadioButton btn = (RadioButton)myInputView.findViewById(radioBtnId);
			if (radioBtnId == R.id.radio0) {
				PlayInfo.udpDevType = 0x01;

			} else if (radioBtnId == R.id.radio1) {
				PlayInfo.udpDevType = 0x02;

			} else if (radioBtnId == R.id.radio2) {
				PlayInfo.udpDevType = 0x03;

			}
		}

	};
	private View.OnClickListener btnOneKeyListener = new View.OnClickListener(){
		public void onClick(View v) {
			Intent mainIntent = new Intent(DeviceSearch.this,  DeviceType.class);
			startActivity(mainIntent);
            /*
			DeviceSearch.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
			LayoutInflater layoutInflater = LayoutInflater.from(DeviceSearch.this);
			myInputView = layoutInflater.inflate(R.layout.udp_input, null);
			devType = (RadioGroup)myInputView.findViewById(R.id.radioGroup1);
			edtIpAddr = (EditText)myInputView.findViewById(R.id.editIpAddr);
			edtPort = (EditText)myInputView.findViewById(R.id.edtDNS);


			PlayInfo.udpDevType = 0x01;

			//devType.setOnCheckedChangeListener(devTypeCheckListener);

			final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSearch.this);
			builder.setTitle("UDP点播");
			builder.setView(myInputView).setPositiveButton("点播", OnUDPClickLister);
			builder.setView(myInputView).setNegativeButton("取消", OnCancelClickLister);
			AlertDialog alert = builder.create();
			alert.show();
			*/

			/*
			Context context;
			//setSysStaticIP();
			String ip = "172.16.10.1";
			String name = "admin";
			String pwd = "admin";
			int port = 8888;
			SDLActivity.userID = SDLActivity.login(ip, port, name, pwd);
			if (SDLActivity.userID !=0 ){
				Intent intent = new Intent(SearchActivity.this, SDLActivity.class);
				startActivity(intent);
			}
			else{
				ActivtyUtil.openToast(SearchActivity.this, "登陆失败...");
			}*/
		}
	};

	private DialogInterface.OnClickListener OnUDPClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			String sPort = edtPort.getText().toString();
			if (sPort == null || sPort.length() <= 0)
			{
				return;
			}
			String sIp = edtIpAddr.getText().toString();
			if (sIp == null || sIp.length() <= 0)
			{
				return;
			}

			PlayInfo.udpIpAddr = sIp;
			PlayInfo.udpPort = Integer.parseInt(sPort);
			PlayInfo.playType = FHSDK.PLAY_TYPE_UDP;
			Intent intent = new Intent(DeviceSearch.this, VideoPlayByOpengl.class);
			startActivity(intent);
		}
	};

	private DialogInterface.OnClickListener OnCancelClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
		}
	};
	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			HashMap<String,String> map=(HashMap<String,String>)myListView.getItemAtPosition(arg2);

			Bundle bundle = new Bundle();
			bundle.putBoolean("isItemSelected", true);
			bundle.putString("ipAddr", map.get("ip"));
			bundle.putString("port", map.get("port"));


			//handler2.removeCallbacks(updateThread);
			Intent intent = new Intent(DeviceSearch.this, Login.class);

			intent.putExtras(bundle);
			startActivity(intent);
			//multicastLock.release();//
		}
	};

	private void allowMulticast(){
		if (multicastLock == null)
		{
			@SuppressLint("WifiManagerLeak")
			WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
			multicastLock = wifiManager.createMulticastLock("multicast.test");
			multicastLock.acquire();
			//SystemClock.sleep(2000);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			handler2.removeCallbacks(updateThread);
			multicastLock.release();
			multicastLock = null;
			MyApplication.getInstance().exit();
			return true;

		}else if (keyCode == KeyEvent.KEYCODE_MENU){
			if(linear.getVisibility()==View.GONE) //查看现在隐身与否
				linear.setVisibility(View.VISIBLE);
			else
				linear.setVisibility(View.GONE);
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)){
			//权限请求结果，并已经处理了该回调
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public int getPermissionsRequestCode() {

		//设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
		return 10000;
	}

	@Override
	public String[] getPermissions() {


		return new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};


	}

	@Override
	public void requestPermissionsSuccess() {

	}

	@Override
	public void requestPermissionsFail() {
	    log.e("requestPermissionsFail");
		MyApplication.getInstance().exit();
	}
}
