package eye.app.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.app.util.*;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MoreCfg extends Activity {
	private static final String TAG = "MoreCfgActivity";

	public final static int LOCATE_CONFIG_TRANS_MODE   = 0x00;
	public final static int LOCATE_CONFIG_MD_STATUS    = 0x01;
	public final static int LOCATE_CONFIG_SAMPLERATE   = 0x02;
	public final static int LOCATE_CONFIG_AUDIO_FORMAT = 0x03;
	public final static int LOCATE_CONFIG_DECODE_TYPE  = 0x04;
	public final static int LOCATE_CONFIG_STREAM_TYPE  = 0x05;
	public final static int LOCATE_CONFIG_FRAME_CACHE  = 0x06;
	public final static int LOCATE_CONFIG_REC_TYPE     = 0x07;
	public final static int LOCATE_CONFIG_DISPLAY_MODE = 0x08;

	private static final String[] ModeArray = {"TCP","UDP"};
	private static final String[] MDArray = {"关闭", "开启"};
	private static final String[] SampleRateArray = {"8000", "16000"};
	private static final String[] FormatArray = {"PCM", "G711-ALAW", "G711-ULAW"};
	private static final String[] FrameCacheNum = {"0", "5", "10", "15", "20", "25"};
	private static final String[] StreamType = {"第一码流", "第二码流"};
	private static final String[] DecodeType = {"软解码", "硬解码"};
	private static final String[] LocateRecType = {"H264", "AVI", "MP4"};
	private static final String[] DisplayMode = {"普通", "鱼眼"};
	private static final String[] MirrorType = {"无", "up/down", "left/right", "all"};
	static String FILE = "transInfo";
	private SimpleAdapter simpleAdapter;
	private SharedPreferences sp = null;
	private ListView myList = null;
	private int mTransMode = 0;
	private int mMDStatus = 0;
	private int mSampleRate = 0;
	private int mAudioFormat = 0;
	private int  mFrameCacheNum = 0;
	private int  mStreamType = 0;
	private int  mDecodeType = 0;
	private int  mLocateRecType = 0;
	private int  mDisplayMode = 0;

	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.title_moreCfg));
		setContentView(R.layout.more_cfg_list);
		MyApplication.getInstance().addActivity(this);

		initView();
	}
	public void initView(){
		sp = getSharedPreferences(FILE, MODE_PRIVATE);
		if (null != sp)
		{
			//
			if (("" == sp.getString("mFrameCacheNum",""))
					|| ("" == sp.getString("mTransMode",""))
					|| ("" == sp.getString("mMDStatus",""))
					||	("" == sp.getString("mSampleRate",""))
					|| ("" == sp.getString("mAudioFormat",""))
					|| ("" == sp.getString("mDecodeType",""))
					|| ("" == sp.getString("mStreamType",""))
					|| ("" == sp.getString("mDisplayMode","")))
			{
				mFrameCacheNum = 0;
				mTransMode     = 0;
				mMDStatus      = 0;
				mSampleRate    = 1;
				mAudioFormat   = 0;
				mDecodeType    = 0;
				mStreamType    = 0;
				mLocateRecType = 0;
                mDisplayMode   = 0;
            }
			else
			{
				mFrameCacheNum = Integer.parseInt(sp.getString("mFrameCacheNum",""));
				mTransMode     = Integer.parseInt(sp.getString("mTransMode",""));
				mMDStatus      = Integer.parseInt(sp.getString("mMDStatus",""));
				mSampleRate    = Integer.parseInt(sp.getString("mSampleRate",""));
				mAudioFormat   = Integer.parseInt(sp.getString("mAudioFormat",""));
				mDecodeType    = Integer.parseInt(sp.getString("mDecodeType",""));
				mStreamType    = Integer.parseInt(sp.getString("mStreamType",""));
				mLocateRecType = Integer.parseInt(sp.getString("mLocateRecType",""));
				mDisplayMode   = Integer.parseInt(sp.getString("mDisplayMode",""));
			}
		}
		myList = (ListView)findViewById(R.id.listView1);
		myList.setOnItemClickListener(listItemListener);
		addListItem();
	}
	public void addListItem(){
		HashMap<String, Object> map;

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "预览传输模式");
		map.put("ItemChose", ModeArray[mTransMode]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "移动侦测提醒");
		map.put("ItemChose", MDArray[mMDStatus]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "语音对讲采样率");
		map.put("ItemChose", SampleRateArray[mSampleRate]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "语音对讲编码格式");
		map.put("ItemChose", FormatArray[mAudioFormat]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "视频缓存帧数");
		map.put("ItemChose", FrameCacheNum[mFrameCacheNum]);
		map.put("ItemImage", R.drawable.arrow_down);

		listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemTitle", "本地录像类型");
		map.put("ItemChose", LocateRecType[mLocateRecType]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "码流类型");
		map.put("ItemChose", StreamType[mStreamType]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "解码方式");
		map.put("ItemChose", DecodeType[mDecodeType]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "显示模式");
		map.put("ItemChose", DisplayMode[mDisplayMode]);
		map.put("ItemImage", R.drawable.arrow_down);
		listItem.add(map);

        map = new HashMap<String, Object>();
        map.put("ItemTitle", "镜像设置");
        map.put("ItemChose", "\"up/down\", \"left/right\", \"all\"");
        map.put("ItemImage", R.drawable.arrow_down);
        listItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemTitle", "软件版本");
		map.put("ItemChose", "ver2.2.4 [2018-7-31]");
		map.put("ItemImage", null);
		listItem.add(map);


		simpleAdapter=new SimpleAdapter(this, listItem, R.layout.more_cfg_item,
				new String[]{"ItemTitle","ItemChose", "ItemImage"},
				new int[]{R.id.tvItemTitle,R.id.textView2, R.id.imageView1});
		myList.setAdapter(simpleAdapter);
	}
	public void rememberTheData(){
		if (sp == null) {
			sp = getSharedPreferences(FILE, MODE_PRIVATE);
		}
		Editor edit = sp.edit();
		edit.putString("mTransMode", String.valueOf(mTransMode));
		edit.putString("mMDStatus", String.valueOf(mMDStatus));
		edit.putString("mSampleRate", String.valueOf(mSampleRate));
		edit.putString("mAudioFormat", String.valueOf(mAudioFormat));
		edit.putString("mFrameCacheNum", String.valueOf(mFrameCacheNum));
		edit.putString("mDecodeType", String.valueOf(mDecodeType));
		edit.putString("mStreamType", String.valueOf(mStreamType));
		edit.putString("mLocateRecType", String.valueOf(mLocateRecType));
		edit.putString("mDisplayMode", String.valueOf(mDisplayMode));
		edit.commit();
	}

	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final HashMap<String,String> map=(HashMap<String,String>)myList.getItemAtPosition(arg2);
			Intent intent = null;

			switch(arg2){
				case 0:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(ModeArray, mTransMode,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mTransMode = which;
											rememberTheData();
											map.put("ItemChose", ModeArray[mTransMode]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 1:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(MDArray, mMDStatus,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mMDStatus = which;
											rememberTheData();
											map.put("ItemChose", MDArray[mMDStatus]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 2:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(SampleRateArray, mSampleRate,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mSampleRate = which;
											rememberTheData();
											map.put("ItemChose", SampleRateArray[mSampleRate]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 3:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(FormatArray, mAudioFormat,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mAudioFormat = which;
											rememberTheData();
											map.put("ItemChose", FormatArray[mAudioFormat]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 4:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(FrameCacheNum, mFrameCacheNum,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mFrameCacheNum = which;
											rememberTheData();
											map.put("ItemChose", FrameCacheNum[mFrameCacheNum]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 5:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(LocateRecType, mLocateRecType,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mLocateRecType = which;
											rememberTheData();
											map.put("ItemChose", LocateRecType[mLocateRecType]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 6:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(StreamType, mStreamType,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mStreamType = which;
											rememberTheData();
											map.put("ItemChose", StreamType[mStreamType]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 7:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(DecodeType, mDecodeType,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mDecodeType = which;
											rememberTheData();
											map.put("ItemChose", DecodeType[mDecodeType]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
				case 8:
					new AlertDialog.Builder(MoreCfg.this)
							.setTitle("请选择")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(DisplayMode, mDisplayMode,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mDisplayMode = which;
											rememberTheData();
											map.put("ItemChose", DisplayMode[mDisplayMode]);
											simpleAdapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
							)
							.setNegativeButton("取消", null)
							.show();
					break;
                case 9:
                    new AlertDialog.Builder(MoreCfg.this)
                            .setTitle("请选择")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(MirrorType, 0,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //mMirrorType = which;
                                            //rememberTheData();
                                            //map.put("ItemChose", MirrorType[mMirrorType]);
                                            log.e("which = " + which);
                                            if(which > 0 && FHSDK.mirrorCtrl(PlayInfo.userID, which))
                                            	ActivtyUtil.openToast(MoreCfg.this, MirrorType[which]);
                                            //simpleAdapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                            )
                            .setNegativeButton("取消", null)
                            .show();
                    break;
			}
		}
	};

	public int getLocateConfig(Context ctx, int cfgIdx)
	{
		int ret = 0;
		SharedPreferences sp = null;
		sp = ctx.getSharedPreferences(FILE, MODE_PRIVATE);
		if (null != sp)
		{
			switch(cfgIdx)
			{
				case LOCATE_CONFIG_TRANS_MODE:
					if ("" == sp.getString("mTransMode",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mTransMode",""));
					break;
				case LOCATE_CONFIG_MD_STATUS:
					if ("" == sp.getString("mMDStatus",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mMDStatus",""));
					break;
				case LOCATE_CONFIG_SAMPLERATE:
					if ("" == sp.getString("mSampleRate",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mSampleRate",""));
					break;
				case LOCATE_CONFIG_AUDIO_FORMAT:
					if ("" == sp.getString("mAudioFormat",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mAudioFormat",""));
					break;
				case LOCATE_CONFIG_DECODE_TYPE:
					if ("" == sp.getString("mDecodeType",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mDecodeType",""));
					break;
				case LOCATE_CONFIG_STREAM_TYPE:
					if ("" == sp.getString("mStreamType",""))
						ret = 1;
					else
						ret = Integer.parseInt(sp.getString("mStreamType","")) + 1;  //
					break;
				case LOCATE_CONFIG_FRAME_CACHE:
					if ("" == sp.getString("mFrameCacheNum",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mFrameCacheNum",""))*5;


					break;
				case LOCATE_CONFIG_REC_TYPE:
					if ("" == sp.getString("mLocateRecType",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mLocateRecType",""));
					break;
				case LOCATE_CONFIG_DISPLAY_MODE:
					if ("" == sp.getString("mDisplayMode",""))
						ret = 0;
					else
						ret = Integer.parseInt(sp.getString("mDisplayMode",""));
					break;


				default:
					break;
			}
		}
		return ret;
	}


	public boolean isSupportMediaCodecHardDecoder(){
		boolean isHardcode = false;
		//读取系统配置文件/system/etc/media_codecc.xml
		File file = new File("/system/etc/media_codecs.xml");
		InputStream inFile = null;
		try {
			inFile = new FileInputStream(file);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if(inFile != null) {
			XmlPullParserFactory pullFactory;
			try {
				pullFactory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlPullParser = pullFactory.newPullParser();
				xmlPullParser.setInput(inFile, "UTF-8");
				int eventType = xmlPullParser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					String tagName = xmlPullParser.getName();
					switch (eventType) {
						case XmlPullParser.START_TAG:
							if ("MediaCodec".equals(tagName)) {
								String componentName = xmlPullParser.getAttributeValue(0);

								if(componentName.startsWith("OMX."))
								{
									if(!componentName.startsWith("OMX.google."))
									{
										isHardcode = true;
									}
								}
							}
					}
					eventType = xmlPullParser.next();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return isHardcode;
	}

	public int getDecodecType(Context ctx)
	{
		int ret, decType;

		ret = getLocateConfig(ctx, LOCATE_CONFIG_DECODE_TYPE);

		if (!isSupportMediaCodecHardDecoder())
		{
			//return FHSDK.DECODE_TYPE_FFMPEG2OPENGL;
		}


		if(0 == ret)
		{
			decType = FHSDK.DECODE_TYPE_FFMPEG2OPENGL;
		}
		else
		{
			decType = FHSDK.DECODE_TYPE_MEDIACODEC2OPENGL;
		}
		
		return decType;
	}



}
