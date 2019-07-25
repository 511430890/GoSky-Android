package eye.app.activity;


import com.app.util.*;
import com.app.util.log;
import com.fh.lib.Define;
import com.fh.lib.Define.Res_e;
import com.fh.lib.Define.VideoEncode;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class VideoEncodeCfg extends Activity {
	private String[] strStreamType61 = {"第一码流"};
	private String[] strStreamType62 = {"第一码流","第二码流"};
	private String[] strRes = null;
	private String[] strQuality = {"自定义","差","较差",  "一般", "好", "较好", "非常好"};
	private String[] strCtrlType= {"FixQP","定码率","变码率", "ABR"};
	private String[] strDenoise = {"关闭","弱","较弱",  "一般", "强", "较强", "非常强"};
	private String[] strDeinter =  {"关闭","5阶滤波（弱）","5阶滤波（中）",  "5阶滤波（强）", "5阶滤波（超强）", "Adapt AVG", "Medium", "Adapt Medium", "MA3"};


	protected EditText etMaxFRate, etMaxBitRate, etIFrameInterval;
	protected Spinner SpinnerStreamType, SpinnerRes, SpinnerQuality, SpinnerCtrlType, SpinnerDenoise, SpinnerDeinter;
	private ArrayAdapter<String> StreamTypeAdapter, ResAdapter,  QualityAdapter, CtrlTypeAdapter, DenoiseAdapter, DeinterAdapter;
	public Define myDefine;
	public VideoEncode encodeVideo;
	private Button btnSave;
	private int encId = 0;

	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_encode_cfg);
		MyApplication.getInstance().addActivity(this);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
		Init();
	}
	public void Init()
	{
		myDefine = new Define();
		encodeVideo =  myDefine.new VideoEncode();

		findView();

	}
	public void findView(){
		btnSave = (Button)findViewById(R.id.btnSave);
		etMaxFRate = (EditText)findViewById(R.id.etMaxFRate);
		etMaxBitRate = (EditText)findViewById(R.id.etMaxBitRate);
		etIFrameInterval = (EditText)findViewById(R.id.etIFrameInterval);

		SpinnerStreamType = (Spinner)findViewById(R.id.SpinnerStreamType);
		SpinnerRes = (Spinner)findViewById(R.id.SpinnerRes);
		SpinnerQuality = (Spinner)findViewById(R.id.SpinnerQuality);
		SpinnerCtrlType = (Spinner)findViewById(R.id.SpinnerCtrlType);
		SpinnerDenoise = (Spinner)findViewById(R.id.SpinnerDenoise);
		SpinnerDeinter = (Spinner)findViewById(R.id.SpinnerDeinter);

		int i;
		Res_e[] res = Res_e.values();
		strRes = new String[res.length];
		for(i = 0; i < res.length; i++)
		{
			strRes[i] = res[i].getName();
		}
		if(1==FHSDK.getDeviceFlag(PlayInfo.userID)) //8610
		{
			StreamTypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strStreamType61);
		}
		else
			StreamTypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strStreamType62);
		ResAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strRes);
		QualityAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strQuality);
		CtrlTypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strCtrlType);
		DenoiseAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strDenoise);
		DeinterAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strDeinter);

		StreamTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  //风格
		ResAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		QualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		CtrlTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DenoiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DeinterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		SpinnerStreamType.setAdapter(StreamTypeAdapter);
		SpinnerRes.setAdapter(ResAdapter);
		SpinnerQuality.setAdapter(QualityAdapter);
		SpinnerCtrlType.setAdapter(CtrlTypeAdapter);
		SpinnerDenoise.setAdapter(DenoiseAdapter);
		SpinnerDeinter.setAdapter(DeinterAdapter);

		SpinnerStreamType.setOnItemSelectedListener(new StreamTypeSelectedListener());
		SpinnerRes.setOnItemSelectedListener(new ResSelectedListener());
		SpinnerQuality.setOnItemSelectedListener(new QualitySelectedListener());
		SpinnerCtrlType.setOnItemSelectedListener(new CtrlTypeSelectedListener());
		SpinnerDenoise.setOnItemSelectedListener(new DenoiseSelectedListener());
		SpinnerDeinter.setOnItemSelectedListener(new DeinterSelectedListener());

		btnSave.setOnClickListener(btnSaveOnClickListener);

	}

	class StreamTypeSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			encId  = arg2 + 1;

			if(FHSDK.getEncodeVideoConfig(PlayInfo.userID, encId, encodeVideo))
			{
				int index = encodeVideo.res;
				if (index == 128)
					index = 55;


				SpinnerStreamType.setSelection(arg2);
				SpinnerRes.setSelection(index);
				SpinnerQuality.setSelection(encodeVideo.quality);
				SpinnerCtrlType.setSelection(encodeVideo.ctrlType);
				SpinnerDenoise.setSelection(encodeVideo.denoise);
				SpinnerDeinter.setSelection(encodeVideo.deinter);

				etMaxFRate.setText(String.valueOf(encodeVideo.maxFRate));
				etMaxBitRate.setText(String.valueOf(encodeVideo.maxBitRate));
				etIFrameInterval.setText(String.valueOf(encodeVideo.iFrameInterval));
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class ResSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			//typeSeldID = arg2;

			encodeVideo.res = arg2;
			if(encodeVideo.res == 55)
				encodeVideo.res = 128;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class QualitySelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			encodeVideo.quality = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class CtrlTypeSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			encodeVideo.ctrlType = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class DenoiseSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			encodeVideo.denoise = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class DeinterSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			encodeVideo.deinter = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	View.OnClickListener btnSaveOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (null != etMaxBitRate.getText().toString())
			{
				encodeVideo.maxBitRate = Integer.parseInt(etMaxBitRate.getText().toString());
			}
			if (null != etMaxFRate.getText().toString())
			{
				encodeVideo.maxFRate = Integer.parseInt(etMaxFRate.getText().toString());
			}
			if (null != etIFrameInterval.getText().toString())
			{
				encodeVideo.iFrameInterval = Integer.parseInt(etIFrameInterval.getText().toString());
			}

			if (FHSDK.setEncodeVideoConfig(PlayInfo.userID, encId, encodeVideo))
			{
				//
				ActivtyUtil.openToast(VideoEncodeCfg.this, "保存成功");
			}
			else
			{
				//
				ActivtyUtil.openToast(VideoEncodeCfg.this, "保存失败");
			}
		}
	};


}
