package eye.app.activity;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.app.util.*;
import com.app.util.WLANCfg;
import com.app.util.log;
import com.fh.lib.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity {
	private static String TAG = "LoginActivity";
	protected String sIPAddr, sPort, sUserName, sPassword, sAesKey;
	protected EditText edtDNS, edtIPAddr, edtPort, edtUserName, edtPassword, edtAesKey;
	protected Button btnConnect, btnSetAesKey;
	private CheckBox checkBox;
//	static SharedPreferences sp = null;
	static SharedPreferences sharedPreferences = null;
	static String FILE = "LoginInfo";
	public boolean isItemSelected = false;
	private Context mContext;
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"onCreate()");
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		super.onCreate(savedInstanceState);
		mContext = this;
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  
		
		setTitle((String)getText(R.string.title_Login));
		setContentView(R.layout.login);
		MyApplication.getInstance().addActivity(this);
		findView();
		setListener();
	}
	private void setListener(){
		//btnSetAesKey.setOnClickListener(btnAesKeyListener);
		btnConnect.setOnClickListener(btnConneectListener);
		edtDNS.setOnFocusChangeListener(edtDNSListener);
	}
	private void findView(){
		edtAesKey = (EditText) findViewById(R.id.edtAesKey);
		//btnSetAesKey = (Button) findViewById(R.id.btnSetAesKey);
		edtDNS = (EditText) findViewById(R.id.edtDNS);
		edtIPAddr = (EditText) findViewById(R.id.edtIPAddr);
		edtPort = (EditText) findViewById(R.id.edtPort);
		edtUserName = (EditText) findViewById(R.id.edtUserName);
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		btnConnect   = (Button) findViewById(R.id.btnConnect);
		checkBox = (CheckBox) findViewById(R.id.checkBox1);
		checkBox.setVisibility(View.GONE);
		Bundle bundle = getIntent().getExtras();
		if(null != bundle)
		{
			isItemSelected = bundle.getBoolean("isItemSelected");
			sIPAddr = bundle.getString("ipAddr");
			sPort = bundle.getString("port");
		}

		//sp = getSharedPreferences(FILE, MODE_PRIVATE);
		// sharedPreferences= getSharedPreferences(FILE, MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("FHloginData", MODE_PRIVATE);
		if (!isItemSelected){
			//sIPAddr = sharedPreferences.getString("ip","");
			//sPort   = sharedPreferences.getString("port","");
		}
		else
		{
			isItemSelected = false;
		}
		edtIPAddr.setText(sIPAddr);
		edtPort.setText(String.valueOf(sPort));
		
		
		
		sUserName = sharedPreferences.getString("userName","");
		if (sUserName.length() == 0)
			edtUserName.setText("guanxukeji");
		else
			edtUserName.setText(sUserName);
		
		sPassword = sharedPreferences.getString("mPassword","");
		if (sPassword.length() == 0)
			edtPassword.setText("gxrdw60");
		else
			edtPassword.setText(sPassword);



		checkBox.setChecked(true);
		btnConnect.requestFocus();

	}	
	private View.OnFocusChangeListener edtDNSListener = new View.OnFocusChangeListener(){
		public void onFocusChange(View v, boolean hasFocus){
			if ( false == hasFocus){
				String mDNS = null, mIPAddr = null;
				mDNS = edtDNS.getText().toString();
				//Log.v("LOGIN1", mDNS);
				if (null == mDNS)
					return;
				//Log.v("LOGIN2", mDNS);
				try {
					java.net.InetAddress address=java.net.InetAddress.getByName(mDNS);
					mIPAddr = address.getHostAddress();
					//Log.v("LOGIN3", mIPAddr);
					if (mIPAddr.length() >= 8) //x.x.x.x
						edtIPAddr.setText(mIPAddr);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	};
	private View.OnClickListener btnAesKeyListener = new View.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			String sAesKey;
			sAesKey   = edtAesKey.getText().toString();
			//Log.e("xx","[" +  sAesKey +"]");
			if(sAesKey.trim().length()<=0)
			{
				ActivtyUtil.showAlert(Login.this, getText(R.string.error), getText(R.string.error_input), getText(R.string.id_sure));
				return;
			}
			if(FHSDK.setCryptKey(sAesKey))
			{
				ActivtyUtil.openToast(mContext, (String)getText(R.string.id_success)); 
			}
			else
			{
				ActivtyUtil.openToast(mContext, (String)getText(R.string.id_fail)); 
			}
		}
		
	};
	private View.OnClickListener btnConneectListener = new View.OnClickListener(){

		public void onClick(View v) {
			
			String sAesKey, mIPAddr, mUserName, mPassword;
			int mPort;
			//sAesKey   = edtAesKey.getText().toString();
			sAesKey="guanxukj@fh8620.";
			mIPAddr   = edtIPAddr.getText().toString();
			mUserName = edtUserName.getText().toString();
			mPassword = edtPassword.getText().toString();
			//Log.e("xx","[" +  edtPort.getText().toString()+"]");
			//if 
			if (edtPort.getText().toString().trim().length()<=0)
				mPort = 0;
			else
				mPort = Integer.parseInt(edtPort.getText().toString().trim());
			//trim() �����ַ����Ŀո�
			if (mUserName.trim().length()<=0 || mPassword.trim().length()<=0 || mPort<=0){
				ActivtyUtil.showAlert(Login.this, getText(R.string.error), getText(R.string.error_input), getText(R.string.id_sure));
				return;
			}

			FHSDK.apiInit();
			FHSDK.setCryptKey(sAesKey);
			PlayInfo.userID = FHSDK.login(mIPAddr, mPort, mUserName, mPassword);
			if (PlayInfo.userID !=0 ){
				
				WLANCfg wlan = new WLANCfg(Login.this);
				LocateInfo.wifiName = wlan.getSSID();
				LocateInfo.userName = mUserName;
				LocateInfo.userPwd = mPassword;
				LocateInfo.ip = mIPAddr;
				LocateInfo.port = mPort;

				long getCurrentTime = FHSDK.getCurrentTime();

				Log.e(TAG, "onClick: CurrentTime"+getCurrentTime);

				rememberTheData1();

				Intent intent = new Intent(Login.this, MainMenu.class);
				startActivity(intent);
			}
			else{
				//ActivtyUtil.openToast(Login.this,"��¼ʧ��...");
				//return;
				
				new AlertDialog.Builder(Login.this).setTitle((String)getText(R.string.id_login))
				.setMessage((String)getText(R.string.id_fail)).setPositiveButton(
						(String)getText(R.string.id_goOn), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								try {
									Intent intent = new Intent(Login.this, MainMenu.class);
									startActivity(intent);
								} catch (Exception e) {
									//Log.e(TAG, e.getMessage(), e);
									ActivtyUtil.openToast(Login.this, e
											.getMessage());
								}
							}
						}).setNegativeButton((String)getText(R.string.id_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}

						}).show();
						
			}
		}
	};

	private void rememberTheData1() {

		sharedPreferences = getSharedPreferences("FHloginData", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString("userName", edtUserName.getText().toString());
		editor.putString("mPassword", edtPassword.getText().toString());
		editor.commit();

	}







//	public void rememberTheData(){
//		if (sp == null) {
//			sp = getSharedPreferences(FILE, MODE_PRIVATE);
//	    }
//		Editor edit = sp.edit();
//		//edit.putString("aes_key", edtAesKey.getText().toString().trim());//don't save
//		edit.putString("ip", edtIPAddr.getText().toString().trim());
//		edit.putString("port", edtPort.getText().toString().trim());
//		edit.putString("username", edtUserName.getText().toString().trim());
//		edit.putString("password", edtPassword.getText().toString().trim());
//		edit.putString("userID", String.valueOf(PlayInfo.userID));
//		edit.commit();
//	}

}
