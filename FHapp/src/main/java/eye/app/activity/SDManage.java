package eye.app.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.app.util.*;
import com.fh.lib.Define;
import com.fh.lib.Define.SDCardFormat;
import com.fh.lib.Define.SDCardInfo;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class SDManage extends Activity {
	private static final String TAG = "SDManage";
	private static final String[] SDStatus = {"找到SD卡, 未加载","SD卡已加载","SD卡正常","正在格式化"};
	private static final String[] MountStatus = {"挂载", "卸载"};
	private static final String[] FormatType = {"快速", "慢速", "低速"};
	private static final String[] FormatTip = {"点击选择格式化类型", "已卸载, 无法格式化"};
	private static final byte FORMAT_COMPLETE = 0;
	private static final byte FHNPEN_SDCardState_FOUND      = (1 << 0);
	private static final byte FHNPEN_SDCardState_LOADED     = (1 << 1);
	private static final byte FHNPEN_SDCardState_NORMAL     = (1 << 2);
	private static final byte FHNPEN_SDCardState_FORMATING  = (1 << 3);

	private SimpleAdapter simpleAdapter;
	private ListView myList = null;

	private int IndexMount = 0;
	private ProgressDialog ProDialog;
	private HashMap<String,String> map;
	ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setTitle("存储管理");
		setContentView(R.layout.sdcard_manage);
		MyApplication.getInstance().addActivity(this);
		initView();
	}
	public void initView(){
		myList = (ListView)findViewById(R.id.listSDManage);
		myList.setOnItemClickListener(listItemListener);
		addListItem();
	}

	public void updateSDCardInfo(){
		String totalSize = null, userdSize = null, leftSize = null;
		SDCardInfo sdcardInfo = (new Define()).new SDCardInfo();
		HashMap<String, String> map;

		if(FHSDK.getSDCardInfo(PlayInfo.userID, sdcardInfo))
		{
			totalSize = String.format("%.1fMB", (float)sdcardInfo.totalSize/((float)1024*1024));
			userdSize = String.format("%.1fMB", (float)sdcardInfo.usedSize/((float)1024*1024));
			leftSize = String.format("%.1fMB", (float)(sdcardInfo.totalSize-sdcardInfo.usedSize)/((float)1024*1024));

			map = (HashMap<String,String>)myList.getItemAtPosition(4);
			if((sdcardInfo.state & FHNPEN_SDCardState_LOADED) > 0)
				IndexMount = 0;
			else
				IndexMount = 1;
			map.put("ItemDetail", MountStatus[IndexMount]);
		}
		else
		{
			IndexMount = 1;
			map = (HashMap<String,String>)myList.getItemAtPosition(4);
			map.put("ItemDetail", MountStatus[IndexMount]);
		}
		map = (HashMap<String,String>)myList.getItemAtPosition(0);
		if((sdcardInfo.state & FHNPEN_SDCardState_FORMATING) > 0)
			map.put("ItemDetail", SDStatus[3]);
		else if ((sdcardInfo.state & FHNPEN_SDCardState_NORMAL) > 0)
			map.put("ItemDetail", SDStatus[2]);
		else if ((sdcardInfo.state & FHNPEN_SDCardState_LOADED) > 0)
			map.put("ItemDetail", SDStatus[1]);
		else if ((sdcardInfo.state & FHNPEN_SDCardState_FOUND) > 0)
			map.put("ItemDetail", SDStatus[0]);

		map = (HashMap<String,String>)myList.getItemAtPosition(1);
		map.put("ItemDetail", totalSize);

		map = (HashMap<String,String>)myList.getItemAtPosition(2);
		map.put("ItemDetail", userdSize);

		map = (HashMap<String,String>)myList.getItemAtPosition(3);
		map.put("ItemDetail", leftSize);

		map = (HashMap<String,String>)myList.getItemAtPosition(5);
		map.put("ItemDetail", FormatTip[IndexMount]);

		simpleAdapter.notifyDataSetChanged();
	}
	public void addListItem(){
		/*
		sdcard_info_t sdcardInfo = new sdcard_info_t();
		SDLActivity.getSDCardInfo(SDLActivity.userID, sdcardInfo);
		//Log.e( TAG, sdcardInfo.state +","+String.valueOf(sdcardInfo.totalSize)  +","+ String.valueOf(sdcardInfo.usedSize));

		String totalSize = String.format("%.1fMB", (float)sdcardInfo.totalSize/((float)1024*1024));
		String userdSize = String.format("%.1fMB", (float)sdcardInfo.usedSize/((float)1024*1024));
		String leftSize = String.format("%.1fMB", (float)(sdcardInfo.totalSize-sdcardInfo.usedSize)/((float)1024*1024));
		*/
		HashMap<String, String> map;

		map = new HashMap<String, String>();
		map.put("ItemTitle", "SD卡状态");
		map.put("ItemDetail", null);
		listItem.add(map);

		map = new HashMap<String, String>();
		map.put("ItemTitle", "总容量");
		map.put("ItemDetail", null);
		listItem.add(map);

		map = new HashMap<String, String>();
		map.put("ItemTitle", "已用空间");
		map.put("ItemDetail", null);
		listItem.add(map);

		map = new HashMap<String, String>();
		map.put("ItemTitle", "可用空间");
		map.put("ItemDetail", null);
		listItem.add(map);

		map = new HashMap<String, String>();
		map.put("ItemTitle", "挂载/卸载");
		map.put("ItemDetail", null);
		listItem.add(map);

		map = new HashMap<String, String>();
		map.put("ItemTitle", "格式化");
		map.put("ItemDetail", null);
		listItem.add(map);

		simpleAdapter=new SimpleAdapter(this, listItem, R.layout.sdcard_manage_item,
				new String[]{"ItemTitle","ItemDetail"},
				new int[]{R.id.tvItemTitle,R.id.tvItemDetail});
		myList.setAdapter(simpleAdapter);
		updateSDCardInfo();
	}

	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final HashMap<String,String> map=(HashMap<String,String>)myList.getItemAtPosition(arg2);
			Intent intent = null;
			switch(arg2){
				case 4:
				{
					final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
					new AlertDialog.Builder(SDManage.this)
							.setTitle("挂载/卸载")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(MountStatus, IndexMount, choiceListener)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int which) {
									if(0 == choiceListener.getWhich())
										FHSDK.loadSDCard(PlayInfo.userID);
									else
										FHSDK.unLoadSDCard(PlayInfo.userID);
									updateSDCardInfo();
								}
							})
							.setNegativeButton("取消", null)
							.show();
					break;
				}
				case 5:
				{
					if (1 == IndexMount)
						break;

					final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
					new AlertDialog.Builder(SDManage.this)
							.setTitle("格式化类型")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(FormatType, 0, choiceListener)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int which) {
									FHSDK.startSDCardFormat(PlayInfo.userID, choiceListener.getWhich());
									ProDialog = ProgressDialog.show(SDManage.this, "",
											"正在格式化，请稍候...", true);

									WaitFormathread mFormatThread = new WaitFormathread(handler);
									mFormatThread.start();
								}
							})
							.setNegativeButton("取消", null)
							.show();
					break;
				}
			}
		}
	};

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what){
				case FORMAT_COMPLETE:
				{
					ProDialog.dismiss();

					int formatState = msg.getData().getInt("formatState");
					if (0x01 == formatState)
						ActivtyUtil.openToast(SDManage.this, "格式化完成");
					else if (0x03 == formatState)
						ActivtyUtil.openToast(SDManage.this, "格式化失败");
					else if (0x04 == formatState)
						ActivtyUtil.openToast(SDManage.this, "未知错误");

					updateSDCardInfo();

					break;
				}

				default:
					break;
			}

		}
	};

	private class WaitFormathread extends Thread{
		final static int STATE_STOP = 0;
		final static int STATE_RUNNING = 1;
		Handler mHandler;
		int mState = STATE_RUNNING;
		SDCardFormat format = (new Define()).new SDCardFormat();
		public WaitFormathread(Handler h){
			mHandler = h;
		}
		public void run(){
			while(STATE_RUNNING == mState)
			{
				if(FHSDK.getSDCardFormatState(format))
				{
					if ( 0x02 == format.formatState)
					{
						continue;
					}

				}
				FHSDK.stopSDCardFormat();
				Message msg = mHandler.obtainMessage();
				msg.what = FORMAT_COMPLETE;
				Bundle b = new Bundle();
				b.putInt("formatState", format.formatState);
				msg.setData(b);
				handler.sendMessage(msg);

				mState = STATE_STOP;

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.e("ERROR", "Thread Interrupted");
				}
			}
		}
		public void setState(int state){
			mState = state;
		}
	}
	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {

		private int which = 0;
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			this.which = which;
		}

		public int getWhich() {
			return which;
		}
	}
}
