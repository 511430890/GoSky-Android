package eye.app.activity;


import com.app.util.*;
import com.fh.lib.Define;
import com.fh.lib.Define.IpConfig;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class IpCfg extends Activity {
	private static String TAG = "IpSetActivity";
	protected EditText edtPort, edtIP, edtMark, edtGateway;// edtDnsF, edtDnsS;
	protected String sPort, sIP, sMark, sGateway;// sDnsF, sDnsS;
	protected int isAutoIP;// isAutoDNS;
	protected RadioButton radioBtnAutoIP, radioBtnManualIP;// radioBtnAutoDNS, radioBtnManualDNS;
	//protected boolean isAutoIPChecked, isAutoDNSChecked;
	protected Button btnSet, btnCancel;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_ip);
		MyApplication.getInstance().addActivity(this);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
		findView();
		Init();
	}

	public void findView(){
		edtPort = (EditText)findViewById(R.id.edtDNS);
		edtIP = (EditText)findViewById(R.id.editText2);
		edtMark = (EditText)findViewById(R.id.editText3);
		edtGateway = (EditText)findViewById(R.id.etGateway);

		radioBtnAutoIP = (RadioButton)findViewById(R.id.radioAutoIP);
		radioBtnManualIP = (RadioButton)findViewById(R.id.radioManualIP);

		//isAutoIPChecked = radioBtnAutoIPradioBtnManualIP.isChecked();
		radioBtnAutoIP.setOnClickListener(radioBtnAutoIPListener);
		radioBtnManualIP.setOnClickListener(radioBtnManualIPListener);

		btnSet = (Button)findViewById(R.id.btnSet);
		//btnCancel = (Button)findViewById(R.id.btnCancel);
		btnSet.setOnClickListener(btnSetListener);
		//btnCancel.setOnClickListener(btnCancelListener);
	}

	private View.OnClickListener btnSetListener = new View.OnClickListener(){
		public void onClick(View v) {
			IpConfig ipConfig = (new Define()).new IpConfig();
			if (FHSDK.getIPConfig(PlayInfo.userID, ipConfig))
			{
				if (radioBtnAutoIP.isChecked())
				{
					ipConfig.isAutoIP = 1;
				}
				else{
					ipConfig.isAutoIP = 0;
					ipConfig.sPort = edtPort.getText().toString();
					ipConfig.sIP = edtIP.getText().toString();
					ipConfig.sMark = edtMark.getText().toString();
					ipConfig.sGateway = edtGateway.getText().toString();
					//Log.i(TAG, "ip = " + ipConfig.sIP+" mark = "+ ipConfig.sMark + " gw = " + "ipConfig.sGateway");
				}
				if (FHSDK.setIPConfig(PlayInfo.userID, ipConfig))
				{
					ActivtyUtil.openToast(IpCfg.this, "设置成功，重启设备生效");
					Intent intent = new Intent(IpCfg.this, MainMenu.class);
					startActivity(intent);
				}
				else
					ActivtyUtil.openToast(IpCfg.this, "设置失败！");
			}
			else
				ActivtyUtil.openToast(IpCfg.this, "获取配置信息失败，请重试！");
		}
	};
	private View.OnClickListener btnCancelListener = new View.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(IpCfg.this, MainMenu.class);
			startActivity(intent);
		}
	};


	public void Init(){
		IpConfig ipConfig = (new Define()).new IpConfig();
		if (FHSDK.getIPConfig(PlayInfo.userID, ipConfig))
		{
			//Log.v(TAG, ipConfig.sPort + "/" + ipConfig.sIP + "/" + ipConfig.isAutoIP);
			edtPort.setText(ipConfig.sPort);
			edtIP.setText(ipConfig.sIP);
			edtMark.setText(ipConfig.sMark);
			edtGateway.setText(ipConfig.sGateway);

			if (ipConfig.isAutoIP > 0)
			{
				radioBtnAutoIP.setChecked(true);
				radioBtnManualIP.setChecked(false);
				edtIP.setEnabled(false);
				edtMark.setEnabled(false);
				edtGateway.setEnabled(false);
			}
			else
			{
				radioBtnAutoIP.setChecked(false);
				radioBtnManualIP.setChecked(true);
				edtIP.setEnabled(true);
				edtMark.setEnabled(true);
				edtGateway.setEnabled(true);
			}
		}
	}

	private View.OnClickListener radioBtnAutoIPListener = new View.OnClickListener(){
		public void onClick(View v) {
			//radioBtnAutoIP.setChecked(true);
			radioBtnManualIP.setChecked(false);
			edtIP.setEnabled(false);
			edtMark.setEnabled(false);
			edtGateway.setEnabled(false);
		}
	};

	private View.OnClickListener radioBtnManualIPListener = new View.OnClickListener(){
		public void onClick(View v) {
			radioBtnAutoIP.setChecked(false);
			//radioBtnManualIP.setChecked(true);
			edtIP.setEnabled(true);
			edtMark.setEnabled(true);
			edtGateway.setEnabled(true);
		}
	};
}
