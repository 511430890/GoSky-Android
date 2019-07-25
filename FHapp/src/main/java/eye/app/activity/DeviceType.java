package eye.app.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.app.util.ActivtyUtil;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class DeviceType extends Activity{
	private EditText edtIpAddr, edtPort;
	private RadioGroup devType;
	private Button btnPlay;
	private MoreCfg moreCfgObj = new MoreCfg();
	public Context mContext;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		setContentView(R.layout.udp_input);
		InitView();
		mContext = this;
	}

	public void InitView()
	{
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
		btnPlay = (Button)findViewById(R.id.btnPlay);
		devType = (RadioGroup)findViewById(R.id.radioGroup1);
		edtIpAddr = (EditText)findViewById(R.id.editIpAddr);
		edtPort = (EditText)findViewById(R.id.edtDNS);



		PlayInfo.udpDevType = 0x01;

		btnPlay.setOnClickListener(btnPlayListener);
		devType.setOnCheckedChangeListener(devTypeChangeListener);

		devType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
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
		});
	}


	private View.OnClickListener btnPlayListener = new View.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
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
			FHSDK.apiInit();

			PlayInfo.playType    = FHSDK.PLAY_TYPE_UDP;
			PlayInfo.udpIpAddr   = sIp;
			PlayInfo.udpPort     = Integer.parseInt(sPort);
			PlayInfo.decodeType  = moreCfgObj.getDecodecType(mContext);

			Intent intent = new Intent(mContext, VideoPlayByOpengl.class);
			startActivity(intent);

		}

	};
	private RadioGroup.OnCheckedChangeListener devTypeChangeListener = new  RadioGroup.OnCheckedChangeListener(){
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
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
}
