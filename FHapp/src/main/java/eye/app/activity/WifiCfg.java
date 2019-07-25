package eye.app.activity;

import com.app.util.*;
import com.fh.lib.Define;
import com.fh.lib.Define.WifiConfig;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class WifiCfg extends Activity {
	private static String TAG = "WifiSetActivity";
	private String[] strWifiMode = {"STATION","UAP"};
	private String[] strWifiType = {"NONE","WEP_OPEN","WEP_SHARED","WPA","WPA2"};

	protected EditText edtSSID, edtDummy, edtChan, edtPSK;
	protected Spinner spWifiMode, spWifiType;
	protected String sSSID, sDummy, sChan, sPSK;
	protected int wifiMode, wifiType, modeSeldID, typeSeldID;
	protected Button btnSet, btnCancel;
	public int Status = 0;
	private ArrayAdapter<String> ModeAdapter, TypeAdapter;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_wifi);
		MyApplication.getInstance().addActivity(this);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
		findView();
		getWifiInfo();
	}

	public void findView(){
		edtSSID = (EditText)findViewById(R.id.etSSID);
		//edtDummy = (EditText)findViewById(R.id.EditText03);
		edtChan = (EditText)findViewById(R.id.EditText02);
		edtPSK = (EditText)findViewById(R.id.edtDNS);

		spWifiMode = (Spinner)findViewById(R.id.spinnerDevType);
		spWifiType = (Spinner)findViewById(R.id.spinner2);
		ModeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strWifiMode);
		TypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strWifiType);
		ModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  //风格
		TypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spWifiMode.setAdapter(ModeAdapter);
		spWifiType.setAdapter(TypeAdapter);

		spWifiMode.setOnItemSelectedListener(new WifiModeSelectedListener());
		spWifiType.setOnItemSelectedListener(new WifiTypeSelectedListener());

		spWifiMode.setVisibility(View.VISIBLE);
		spWifiType.setVisibility(View.VISIBLE);

		btnSet = (Button)findViewById(R.id.btnSet);
		btnSet.setOnClickListener(btnSetListener);
	}
	private View.OnClickListener btnSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			WifiConfig wifiConfig = (new Define()).new WifiConfig();
			if (FHSDK.getWifiConfig(PlayInfo.userID, wifiConfig))
			{
				wifiConfig.sSSID = edtSSID.getText().toString();
				//wifiConfig.sDummy = edtDummy.getText().toString();
				wifiConfig.sChan = edtChan.getText().toString();
				wifiConfig.sPSK = edtPSK.getText().toString();

				wifiConfig.wifiMode = modeSeldID;
				wifiConfig.wifiType = typeSeldID;
				//Log.i(TAG, wifiConfig.sSSID +"/" + wifiConfig.sPSK);
				if (FHSDK.setWifiConfig(PlayInfo.userID, wifiConfig))
				{
					ActivtyUtil.openToast(WifiCfg.this, "设置成功，重启设备生效");
					Intent intent = new Intent(WifiCfg.this, MainMenu.class);
					startActivity(intent);
				}
				else
					ActivtyUtil.openToast(WifiCfg.this, "设置失败！");
			}
			else
				ActivtyUtil.openToast(WifiCfg.this, "获取配置信息失败，请重试！");
		}
	};
	private View.OnClickListener btnCancelListener = new View.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(WifiCfg.this, MainMenu.class);
			startActivity(intent);
		}
	};
	public void getWifiInfo(){
		WifiConfig wifiConfig = (new Define()).new WifiConfig();
		if (FHSDK.getWifiConfig(PlayInfo.userID, wifiConfig))
		{
			int mode;
			//Log.i(TAG, wifiConfig.sSSID+"/"+ wifiConfig.wifiMode + "/" + wifiConfig.wifiType);
			edtSSID.setText(wifiConfig.sSSID);
			//edtDummy.setText(wifiConfig.sDummy);
			edtChan.setText(wifiConfig.sChan);
			edtPSK.setText(wifiConfig.sPSK);
			if (2 == (mode = wifiConfig.wifiMode))
				mode = 1;

			spWifiMode.setSelection(mode);
			spWifiType.setSelection(wifiConfig.wifiType);
			return;
		}
		else
			ActivtyUtil.openToast(WifiCfg.this, "获取配置信息失败，请重试！");
	}
	class WifiModeSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			modeSeldID = arg2;
			if (1 == arg2)
				modeSeldID = 2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class WifiTypeSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			typeSeldID = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
}
