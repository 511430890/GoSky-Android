package eye.app.activity;


import com.app.util.*;
import com.fh.lib.Define.BCSS;
import com.fh.lib.Define.DevSearch;
import com.fh.lib.Define.SerialDataCallBackInterface;
import com.fh.lib.Define;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class SerialTest extends Activity {

	protected Button btnSerialStart,btnSerialSend, btnSerialStop, btnSerialSendtoPort, btnTestBCSS;

	protected TextView tvSerial;
	private BCSS bcssObj, bcssDefObj;
	private long serialHandle;
	//private int brightnessInitVal, contrastInitVal, saturationInitVal, sharpnessInitVal;
	//private int brightness, contrast, saturation, sharpness;
	private SeekBar seekBarBrightness,seekBarContrast, seekBarSaturation, seekBarSharpness;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_test);
		MyApplication.getInstance().addActivity(this);

		InitView();
	}
	public void InitView()
	{
		btnSerialStart = (Button)findViewById(R.id.btnSerialStart);
		btnSerialSend = (Button)findViewById(R.id.btnSerialSend);
		btnSerialStop = (Button)findViewById(R.id.btnSerialStop);
		btnSerialSendtoPort = (Button)findViewById(R.id.btnSerialSendtoPort);
		btnTestBCSS = (Button)findViewById(R.id.btnTestBCSS);

		btnSerialStart.setOnClickListener(btnSerialStartListener);
		btnSerialSend.setOnClickListener(btnSerialSendListener);
		btnSerialStop.setOnClickListener(btnSerialStopListener);
		btnSerialSendtoPort.setOnClickListener(btnSerialSendtoPortListener);
		btnTestBCSS.setOnClickListener(btnTestBCSSListener);

		bcssObj = (new Define()).new BCSS();
		bcssDefObj = (new Define()).new BCSS();
	}
	private SerialDataCallBackInterface fun = new SerialDataCallBackInterface(){
		public int SerialDataCallBack(int serialHandle, byte[] buffer, int bufferLen){
			Log.e("xxx","serialHandle = " + serialHandle + "| len = " + bufferLen);
			return 0;
		}
	};


	private View.OnClickListener btnSerialStartListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int serialPort = 1;
			int serialIndex = 1;
			serialHandle = FHSDK.startSerial(PlayInfo.userID, serialPort, serialIndex, fun);
		}

	};
	private View.OnClickListener btnSerialSendListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

			byte[] sendBuf = {'a','b','c'};
			int bufLen = sendBuf.length;
			FHSDK.sendSerial(serialHandle, sendBuf, bufLen);
		}

	};

	private View.OnClickListener btnSerialStopListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			FHSDK.stopSerial(serialHandle);
		}

	};
	private View.OnClickListener btnSerialSendtoPortListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int serialPort = 1;
			int serialIndex = 1;
			byte[] sendBuf = {'a','b','c'};
			int bufLen = sendBuf.length;
			FHSDK.sendToSerialPort(PlayInfo.userID, serialPort, serialIndex, sendBuf, bufLen);
		}

	};

	private View.OnClickListener btnTestBCSSListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//BCSS obj = (new Define()).new BCSS();
			//FHSDK.getVideoBCSS(PlayInfo.userID, obj);
			//FHSDK.setVideoBCSS(PlayInfo.userID, obj);
			LayoutInflater layoutInflater = LayoutInflater.from(SerialTest.this);
			View myInputView = layoutInflater.inflate(R.layout.bcss, null);
			//edtPort = (EditText)myInputView.findViewById(R.id.edtDNS);

			seekBarBrightness = (SeekBar)myInputView.findViewById(R.id.SeekBarBrightNess);
			seekBarContrast = (SeekBar)myInputView.findViewById(R.id.SeekBarContrast);
			seekBarSaturation = (SeekBar)myInputView.findViewById(R.id.SeekBarSaturation);
			seekBarSharpness = (SeekBar)myInputView.findViewById(R.id.SeekBarSharpness);

			if (FHSDK.getVideoBCSS(PlayInfo.userID, bcssObj))
			{
				Log.e("xx", "get bscc = " + bcssObj.brightness + "/" + bcssObj.contrast + "/" + bcssObj.saturation + "/" + bcssObj.sharpness);

				//

				bcssDefObj.brightness = bcssObj.brightness;
				bcssDefObj.contrast   = bcssObj.contrast;
				bcssDefObj.saturation = bcssObj.saturation;
				bcssDefObj.sharpness  = bcssObj.sharpness;

				seekBarBrightness.setMax(255);
				seekBarBrightness.setProgress(bcssObj.brightness);
				seekBarBrightness.setOnSeekBarChangeListener(seekBarBrightnessChangeListener);

				seekBarContrast.setMax(63);
				seekBarContrast.setProgress(bcssObj.contrast);
				seekBarContrast.setOnSeekBarChangeListener(seekBarContrastChangeListener);

				seekBarSaturation.setMax(63);
				seekBarSaturation.setProgress(bcssObj.saturation);
				seekBarSaturation.setOnSeekBarChangeListener(seekBarSaturationChangeListener);

				seekBarSharpness.setMax(15);
				seekBarSharpness.setProgress(bcssObj.sharpness);
				seekBarSharpness.setOnSeekBarChangeListener(seekBarSharpnessChangeListener);


			}

			//seekBarSharpness.SET
			final AlertDialog.Builder builder = new AlertDialog.Builder(SerialTest.this);
			builder.setTitle("UDP点播");
			builder.setView(myInputView).setPositiveButton("保存", OnSaveClickLister);
			builder.setView(myInputView).setNegativeButton("取消", OnCancelClickLister);
			AlertDialog alert = builder.create();
			alert.show();



		}

	};
	private DialogInterface.OnClickListener OnSaveClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {

			Log.e("xx", "set bscc = " + bcssObj.brightness + "/" + bcssObj.contrast + "/" + bcssObj.saturation + "/" +bcssObj.sharpness);
			if(!FHSDK.setVideoBCSS(PlayInfo.userID, bcssObj))
			{
				ActivtyUtil.openToast(SerialTest.this, "保存失败");

				FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
			}
		}
	};

	private DialogInterface.OnClickListener OnCancelClickLister = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//TODO...
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarBrightnessChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.brightness= arg1;
			//tvBrightness.setText(brightness);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}

	};
	private SeekBar.OnSeekBarChangeListener  seekBarContrastChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.contrast= arg1;
			//tvContrast.setText(contrast);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarSaturationChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.saturation= arg1;
			//tvSaturation.setText(saturation);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
	private SeekBar.OnSeekBarChangeListener  seekBarSharpnessChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			bcssObj.sharpness= arg1;
			//tvSharpness.setText(sharpness);
			FHSDK.setVideoBCSS(PlayInfo.userID, bcssDefObj);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};
}
