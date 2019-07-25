package eye.app.activity;


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

import com.app.util.ActivtyUtil;
import com.fh.lib.Define;
import com.fh.lib.Define.Res_e;
import com.fh.lib.Define.SerialPortCfg;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

public class SerialCfg extends Activity {
	private Integer[] iBaudRate = {110, 300,1200,2400,4800,9600,19200,38400,57600,115200,230400,460800,921600};
	private Integer[] iDataBit = {5,6,7, 8};
	private String[] strParity = {"无","奇校验","偶校验"};
	private Integer[] iStopBit =  {1,2};
	private String[] strFlowCtrl = {"无","Xon/Xoff","硬件"};

	protected Spinner SpinnerBaudRate, SpinnerDataBit, SpinnerParity, SpinnerStopBit, SpinnerFlowCtrl;
	private ArrayAdapter<Integer> BaudRateAdapter, DataBitAdapter, StopBitAdapter;
	private ArrayAdapter<String> ParityAdapter, FlowCtrlAdapter;

	public Define myDefine;
	private Button btnSave;
	SerialPortCfg SerialCfg;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_cfg);
		MyApplication.getInstance().addActivity(this);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //隐藏键盘
		Init();
	}
	public void Init()
	{
		myDefine = new Define();
		SerialCfg =  myDefine.new SerialPortCfg();

		findView();

	}
	public void findView(){
		btnSave = (Button)findViewById(R.id.btnSave);

		SpinnerBaudRate = (Spinner)findViewById(R.id.SpinnerBaudRate);
		SpinnerDataBit = (Spinner)findViewById(R.id.SpinnerDataBit);
		SpinnerParity = (Spinner)findViewById(R.id.SpinnerParity);
		SpinnerStopBit = (Spinner)findViewById(R.id.SpinnerStopBit);
		SpinnerFlowCtrl = (Spinner)findViewById(R.id.SpinnerFlowCtrl);


		BaudRateAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, iBaudRate);
		DataBitAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, iDataBit);
		StopBitAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, iStopBit);
		ParityAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strParity);
		FlowCtrlAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strFlowCtrl);

		BaudRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  //风格
		DataBitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		StopBitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ParityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		FlowCtrlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		SpinnerBaudRate.setAdapter(BaudRateAdapter);
		SpinnerDataBit.setAdapter(DataBitAdapter);
		SpinnerStopBit.setAdapter(StopBitAdapter);
		SpinnerParity.setAdapter(ParityAdapter);
		SpinnerFlowCtrl.setAdapter(FlowCtrlAdapter);

		SpinnerBaudRate.setOnItemSelectedListener(new BaudRateSelectedListener());
		SpinnerDataBit.setOnItemSelectedListener(new DataBitSelectedListener());
		SpinnerStopBit.setOnItemSelectedListener(new StopBitSelectedListener());
		SpinnerParity.setOnItemSelectedListener(new ParitySelectedListener());
		SpinnerFlowCtrl.setOnItemSelectedListener(new FlowCtrlSelectedListener());

		btnSave.setOnClickListener(btnSaveOnClickListener);

	}

	class BaudRateSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {

			int idx = 0;
			if(FHSDK.getSerialPortConfig(PlayInfo.userID, SerialCfg))
			{
				for (idx = 0; idx < iBaudRate.length; idx++){
					if(iBaudRate[idx] == SerialCfg.baudRate)
						break;
				}
				SpinnerBaudRate.setSelection(idx);

				for (idx = 0; idx < iDataBit.length; idx++){
					if(iDataBit[idx] == SerialCfg.dataBit)
						break;
				}
				SpinnerDataBit.setSelection(idx);


				for (idx = 0; idx < iStopBit.length; idx++){
					if(iStopBit[idx] == SerialCfg.stopBit)
						break;
				}
				SpinnerStopBit.setSelection(idx);
				SpinnerParity.setSelection(SerialCfg.parity);
				SpinnerFlowCtrl.setSelection(SerialCfg.flowCtrl);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class DataBitSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			//typeSeldID = arg2;

			SerialCfg.dataBit  = iDataBit[arg2];
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class StopBitSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			SerialCfg.stopBit  = iStopBit[arg2];
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class ParitySelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			SerialCfg.parity = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class FlowCtrlSelectedListener implements OnItemSelectedListener{

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			SerialCfg.flowCtrl = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}


	View.OnClickListener btnSaveOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (FHSDK.setSerialPortConfig(PlayInfo.userID, SerialCfg))
			{
				//
				ActivtyUtil.openToast(SerialCfg.this, "保存成功");
			}
			else
			{
				//
				ActivtyUtil.openToast(SerialCfg.this, "保存失败");
			}
		}
	};


}
