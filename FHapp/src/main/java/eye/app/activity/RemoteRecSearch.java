package eye.app.activity;

import java.net.UnknownHostException;
import java.util.Calendar;


import com.app.util.*;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class RemoteRecSearch extends Activity {
	private static String TAG = "RecSearchActivity";
	protected Button btnRecSearch;
	protected Spinner spRecChan, spRecType, spRecLockF;
	protected EditText edtStartTime;
	protected EditText edtStopTime;
	private ArrayAdapter<String> ChanAdapter, TypeAdapter, LockFAdpter;
	private String[] strRecChan = {"所有通道"};//,"通道01","通道02", "通道03","通道04"};
	private String[] strRecType = {"所有录像","手动录像","定时录像"};
	private String[] strRecLockF = {"全部", "锁定", "未锁定"};
	protected int chanSeldID, typeSeldID, lockFSeldID;
	protected int mStartYear, mStartMonth, mStartDay; // u must initialize
	protected int mStopYear, mStopMonth, mStopDay;
	static String FILE = "RecSearchInfo";
	static SharedPreferences sp = null;
	private static final int SHOW_START_DATAPICK = 0;
	private static final int SHOW_STOP_DATAPICK = 1;
	private static final int DATE_DIALOG_START_ID = 2;
	private static final int DATE_DIALOG_STOP_ID = 3;
	private RemoteRecSearch objRecSearch;
	protected void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setTitle("录像查询");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rec_search_info);
		MyApplication.getInstance().addActivity(this);
		initView();
	}


	public void initView(){
		btnRecSearch = (Button)findViewById(R.id.btnRecSearch);
		btnRecSearch.setOnClickListener(btnRecSearchListener);

		edtStartTime = (EditText)findViewById(R.id.edtStartTime);
		edtStartTime.setOnClickListener(edtStartTimeListener);
		setStartTime();

		edtStopTime = (EditText)findViewById(R.id.edtStopTime);
		edtStopTime.setOnClickListener(edtStopTimeListener);
		setStopTime();

		spRecChan = (Spinner)findViewById(R.id.spRecChan);
		ChanAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strRecChan);
		ChanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spRecChan.setAdapter(ChanAdapter);
		spRecChan.setOnItemSelectedListener(new RecChanSelectedListener());


		spRecType = (Spinner)findViewById(R.id.spRecType);
		TypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strRecType);
		TypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spRecType.setAdapter(TypeAdapter);
		spRecType.setOnItemSelectedListener(new RecTypeSelectedListener());

		spRecLockF = (Spinner)findViewById(R.id.spRecLockF);
		LockFAdpter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, strRecLockF);
		LockFAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spRecLockF.setAdapter(LockFAdpter);
		spRecLockF.setOnItemSelectedListener(new RecLockFSelectedListener());
	}
	class RecChanSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			chanSeldID = arg2;
			//Log.e("chanSeldID", "chanSeldID = " + chanSeldID);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class RecTypeSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			typeSeldID = arg2;
			//Log.e("typeSeldID", "typeSeldID = " + typeSeldID);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	class RecLockFSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
			lockFSeldID = arg2;
			//Log.e("lockFSeldID", "lockFSeldID = " + lockFSeldID);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	private View.OnClickListener edtStartTimeListener = new View.OnClickListener(){
		public void onClick(View v){
			Message msg = new Message();
			msg.what = RemoteRecSearch.SHOW_START_DATAPICK;
			RemoteRecSearch.this.DateHandler.sendMessage(msg);
		}
	};
	private View.OnClickListener edtStopTimeListener = new View.OnClickListener(){
		public void onClick(View v){
			Message msg = new Message();
			msg.what = RemoteRecSearch.SHOW_STOP_DATAPICK;
			RemoteRecSearch.this.DateHandler.sendMessage(msg);
		}
	};
	private void setStartTime() {
		/*
        final Calendar c = Calendar.getInstance();
        mStartYear = c.get(Calendar.YEAR);
        mStartMonth = c.get(Calendar.MONTH);
        mStartDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay(edtStartTime, mStartYear, mStartMonth, mStartDay);
        */

		mStartYear = 2000;
		mStartMonth = 0;
		mStartDay= 1;
		updateDisplay(edtStartTime, mStartYear, mStartMonth, mStartDay);

	}
	private void setStopTime() {
		final Calendar c = Calendar.getInstance();
		mStopYear = c.get(Calendar.YEAR);
		mStopMonth = c.get(Calendar.MONTH);
		mStopDay = c.get(Calendar.DAY_OF_MONTH);
		//Log.e("Stop time", mStopYear + "/" + (mStopMonth+1) + "/" + mStopDay);
		updateDisplay(edtStopTime, mStopYear, mStopMonth, mStopDay);
	}
	private void updateDisplay(EditText mEdit, int mYear, int mMonth, int mDay) {
		mEdit.setText(new StringBuilder().append(mYear).append("-").append(
				(mMonth + 1) < 10? "0" + (mMonth + 1):(mMonth + 1)).append("-").append(
				(mDay < 10) ? "0" + mDay : mDay));
	}

	private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
							  int dayOfMonth) {
			mStartYear = year;
			mStartMonth = monthOfYear;
			mStartDay = dayOfMonth;
			updateDisplay(edtStartTime, mStartYear, mStartMonth, mStartDay);
		}
	};
	private DatePickerDialog.OnDateSetListener mStopDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
							  int dayOfMonth) {
			mStopYear = year;
			mStopMonth = monthOfYear;
			mStopDay = dayOfMonth;
			updateDisplay(edtStopTime, mStopYear, mStopMonth, mStopDay);
		}
	};
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_DIALOG_START_ID:
				return new DatePickerDialog(this, mStartDateSetListener, mStartYear, mStartMonth, mStartDay);  //年月日需要初始化
			case DATE_DIALOG_STOP_ID:
				return new DatePickerDialog(this, mStopDateSetListener, mStopYear, mStopMonth, mStopDay);

		}
		return null;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DATE_DIALOG_START_ID:
				((DatePickerDialog) dialog).updateDate(mStartYear, mStartMonth, mStartDay);
				break;
			case DATE_DIALOG_STOP_ID:
				((DatePickerDialog) dialog).updateDate(mStopYear, mStopMonth, mStopDay);
				break;
		}
	}
	Handler DateHandler = new Handler(){
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg){
			switch(msg.what){
				case RemoteRecSearch.SHOW_START_DATAPICK:
					showDialog(DATE_DIALOG_START_ID);
					break;
				case RemoteRecSearch.SHOW_STOP_DATAPICK:
					showDialog(DATE_DIALOG_STOP_ID);
					break;
			}
		}
	};


	private View.OnClickListener btnRecSearchListener = new View.OnClickListener(){
		public void onClick(View v) {
			String str = edtStartTime.getText().toString();
			String[] strStartTimeArray = str.split("-");

			str = edtStopTime.getText().toString();
			String[] strStopTimeArray = str.split("-");

			/*
			objRecSearch = new RecSearchActivity();
			objRecSearch.mStartYear = Integer.parseInt(strStartTimeArray[0]);
			objRecSearch.mStartMonth =Integer.parseInt(strStartTimeArray[1]);
			objRecSearch.mStartDay = Integer.parseInt(strStartTimeArray[2]);
			objRecSearch.mStopYear = Integer.parseInt(strStopTimeArray[0]);
			objRecSearch.mStopMonth = Integer.parseInt(strStopTimeArray[1]);
			objRecSearch.mStopDay = Integer.parseInt(strStopTimeArray[2]);
			
			objRecSearch.chanSeldID = chanSeldID;
			objRecSearch.typeSeldID = typeSeldID;
			objRecSearch.lockFSeldID = lockFSeldID;
			*/
			if (sp == null){
				sp = getSharedPreferences(FILE, MODE_PRIVATE);
			}
			Editor edit = sp.edit();
			edit.putString("startYear", strStartTimeArray[0]);
			edit.putString("startMonth", strStartTimeArray[1]);
			edit.putString("startDay", strStartTimeArray[2]);
			edit.putString("stopYear", strStopTimeArray[0]);
			edit.putString("stopMonth", strStopTimeArray[1]);
			edit.putString("stopDay", strStopTimeArray[2]);
			edit.putString("chanSeldID", String.valueOf(chanSeldID));
			edit.putString("typeSeldID", String.valueOf(typeSeldID));
			edit.putString("lockFSeldID", String.valueOf(lockFSeldID));
			edit.commit();

			Intent intent = new Intent(RemoteRecSearch.this, RemoteRecList.class);
			startActivity(intent);
		}
	};

}


