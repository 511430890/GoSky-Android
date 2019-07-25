package eye.app.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.app.util.ActivtyUtil;
import com.app.util.log;
import com.app.view.VideoPlayView;
import com.fh.lib.Define;
import com.fh.lib.Define.CbDataInterface;
import com.fh.lib.Define.FrameHead;
import com.fh.lib.Define.PicSearch;
import com.fh.lib.Define.Picture;
import com.fh.lib.Define.RecSearch;
import com.fh.lib.Define.Record;
import com.fh.lib.Define.StreamDataCallBackInterface;
import com.fh.lib.FHSDK;
import com.fh.lib.Define.WifiConfig;
import com.fh.lib.PlayInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class RemoteRecList extends Activity {
	private static final String TAG = "RecListActivity";
	private static String[] strRecTypeArray = {"所有录像","手动","定时"};
	private static String[] strRecLockFArray = {"未锁", "锁定"};

	private static final int SEARCH_DONE = 0;
	private static final int UPDATE_THUMBNAIL = 1;


	private static final int NOTIFY_TYPE_SHOT_FileName = 0x00;
	private static final int NOTIFY_TYPE_SHOT_Fail     = 0x01;

	private int mChanID, mRecType, mLockFlag;
	//private String strChanID, strRecType, strLockFlag;
	//private int mStartYear, mStartMonth, mStartDay, mStartHour, mStartMin, mStartSec;//, ullStartTime, ullStopTime, ullDataSize;
	//private int mStopYear, mStopMonth, mStopDay, mStopHour, mStopMin, mStopSec;
	private  long mStartTime, mStopTime, mDataSize;
	protected ListView fileList;
	private TextView tvResult;
	private SharedPreferences sp = null;
	private MoreCfg moreCfgObj = new MoreCfg();
	private SimpleAdapter myAdapter;
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	private Context mContext;
	private long picDownloadHandle = 0;
	private static final int NOTIFY_UPDATE_PIC = 0;

	public Bitmap mWatermark = null;

	public SearchThumbnailThread mSearchThumbnailThread= null;
	public PicThumbnailThread mPicThumbnailThread = null;
	public File appDir = null;
	public static String thumbnailPath = null;
	public long curStartTime = 0, curStopTime = 0;
	public boolean isRecvPicBuf = false;
	public ArrayList<SoftReference> bitmapList = new ArrayList<SoftReference>();
	public int thumbHandle = 0;
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);

		mContext = this;
		setTitle("查询结果");
		setContentView(R.layout.rec_search_list);
		MyApplication.getInstance().addActivity(this);
		findView();

		Thread mSearchThread = new Thread(new SearchRecThread(), "SearchThread");
		mSearchThread.start();
	}

	public void findView(){
		fileList = (ListView)findViewById(R.id.listViewRec);
		fileList.setOnItemClickListener(listItemListener);
		tvResult = (TextView)findViewById(R.id.tvResult);

		mWatermark = ActivtyUtil.getImageFromAssetsFile(mContext, "record.png");
		if (null == mWatermark)
		{
			log.e("mWatermark is null");
		}
		//Environment.getExternalStorageDirectory()
		appDir = new File(mContext.getCacheDir(), "/Thumbnail");
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		//fileName = "new_" + fileName;
		thumbnailPath = appDir.getAbsolutePath();
	}

	class SearchRecThread implements Runnable {
		public void run() {
			if (recSearch())
			{
				mHandler.sendEmptyMessage(SEARCH_DONE);

				mPicThumbnailThread = new PicThumbnailThread();
				mPicThumbnailThread.start();

			}
		}
	}


	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SEARCH_DONE:
					if (null != fileList)
						fileList.setAdapter(myAdapter);
					break;
				case UPDATE_THUMBNAIL:
					if (null != myAdapter)
						myAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int i = 0;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mSearchThumbnailThread != null)
				mSearchThumbnailThread.stop();

			if(mPicThumbnailThread != null)
				mPicThumbnailThread.stop();

			//File appDir = new File(Environment.getExternalStorageDirectory(), "/Thumbnail");
			if (appDir.exists()) {
				//ActivtyUtil.RecursionDeleteFile(appDir);
			}

			for(i = 0; i < bitmapList.size(); i++)
			{
				//SoftReference soft = bitmapList.get(i);
				//((Bitmap) soft.get()).recycle();
				//bmp.recycle();

			}


			HashMap<String, Object> map;
			for(i = 0; i < listItem.size(); i++)
			{
				Bitmap bitmap = null;
				map = listItem.get(i);
				try {
					bitmap = (Bitmap) map.get("recThumbnail");
				}catch(Exception e){
					e.printStackTrace();
				}
				if(bitmap != null && !bitmap.isRecycled())
				{
					bitmap.recycle();
				}
			}


			//FHSDK.thumbnailStop(thumbHandle);
		}

		return super.onKeyDown(keyCode, event);
	}
	/*
	static String getDateTimeByMillisecond(long millisecond) {
	    Date date = new Date(Long.valueOf(millisecond));
	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
	    String time = format.format(date);
	    return time;
	}
	*/
	@SuppressLint("DefaultLocale")
	public boolean recSearch(){

		int picDownloadHandle = 0;
		Bitmap bitmap = null;
		FileInputStream fis = null;

		FHSDK.setPlayInfo(new PlayInfo());
		//thumbHandle = FHSDK.thumbnailStart();
		FHSDK.registerNotifyCallBack(dataFun);
		mSearchThumbnailThread = new SearchThumbnailThread();
		mSearchThumbnailThread.start();

		FHSDK.registerStreamDataCallBack(fun);

		long handle;
		RecSearch objRecSearch = (new Define()).new RecSearch();
		sp = getSharedPreferences(RemoteRecSearch.FILE, MODE_PRIVATE);
		if (sp != null)
		{

			objRecSearch.startYear = Integer.parseInt(sp.getString("startYear",""));
			objRecSearch.startMonth =Integer.parseInt(sp.getString("startMonth",""));
			objRecSearch.startDay = Integer.parseInt(sp.getString("startDay",""));
			objRecSearch.stopYear = Integer.parseInt(sp.getString("stopYear",""));
			objRecSearch.stopMonth = Integer.parseInt(sp.getString("stopMonth",""));
			objRecSearch.stopDay = Integer.parseInt(sp.getString("stopDay",""));

			objRecSearch.chanSeldID = Integer.parseInt(sp.getString("chanSeldID",""));
			objRecSearch.typeSeldID = Integer.parseInt(sp.getString("typeSeldID",""));
			objRecSearch.lockFSeldID = Integer.parseInt(sp.getString("lockFSeldID",""));

		}

		handle = FHSDK.searchRecord(PlayInfo.userID, objRecSearch);
		if (handle == 0)
		{
			//ActivtyUtil.openToast(RemoteRecList.this, "查询出错,请重试");
			return false;
		}
		HashMap<String, Object> map;
		Record result = (new Define()).new Record();
		while(true)
		{
			if (!FHSDK.searchNextRecord(handle, result))
			{
				break;
			}
			else
			{
				String sDataSize = null;

				//if (result.mDataSize < 1024)
				//{
				//	sDataSize = String.format("%I64u BYTE", result.mDataSize);
				//}
				if(result.dataSize < 1024*1024)
				{
					sDataSize = String.format("%.1fKB", (float)result.dataSize/(float)1024);
				}
				else
				{
					sDataSize = String.format("%.1fMB", (float)result.dataSize/((float)1024*1024));
				}
				map = new HashMap<String, Object>();
				String strTime = FHSDK.timeConvert(PlayInfo.userID, result.startTime);
				map.put("startTime", FHSDK.timeConvert(PlayInfo.userID, result.startTime));
				map.put("stopTime",  FHSDK.timeConvert(PlayInfo.userID, result.stopTime));
				map.put("tvChanName", result.chanID);
				map.put("tvRecTypeName",  strRecTypeArray[result.recType]);
				map.put("tvLoackFlagName", strRecLockFArray[result.lockFlag]);
				map.put("tvDataSize", sDataSize);
				map.put("startTimeByMS", result.startTime);
				map.put("stopTimeByMS", result.stopTime);
				map.put("flag", 0);

				map.put("bGetThumbnail", 0);
				map.put("recThumbnail", R.drawable.video);

				String thumbnailFileName = thumbnailPath + "/"+ String.valueOf(map.get("startTimeByMS")) + "-" + String.valueOf(map.get("stopTimeByMS")) + ".bmp";
				File f = new File(thumbnailFileName);
				if(!f.exists())
				{
					//FHSDK.thumbnailAddRemoteFile(thumbHandle, (Long)map.get("startTimeByMS"), (Long)map.get("stopTimeByMS"), result.recType, 0);
				}
				else
				{
					try {
						fis = new FileInputStream(thumbnailFileName);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(fis != null)
					{
						try{
							bitmap  = BitmapFactory.decodeStream(fis);
						}catch(Exception e)
						{
							e.printStackTrace();
							ActivtyUtil.sleep(10);
							continue;
						}
						if(null != bitmap){
							map.put("bGetThumbnail", 1);
							map.put("recThumbnail", bitmap);

							//mHandler.sendEmptyMessage(UPDATE_THUMBNAIL);
						}
					}



				}

				listItem.add(map);
			}
		}
		FHSDK.closeSearchRecord(handle);


		PicSearch objPicSearch = (new Define()).new PicSearch();
		sp = getSharedPreferences(RemoteRecSearch.FILE, MODE_PRIVATE);
		if (sp != null)
		{
			objPicSearch.startYear = Integer.parseInt(sp.getString("startYear",""));
			objPicSearch.startMonth =Integer.parseInt(sp.getString("startMonth",""));
			objPicSearch.startDay = Integer.parseInt(sp.getString("startDay",""));
			objPicSearch.stopYear = Integer.parseInt(sp.getString("stopYear",""));
			objPicSearch.stopMonth = Integer.parseInt(sp.getString("stopMonth",""));
			objPicSearch.stopDay = Integer.parseInt(sp.getString("stopDay",""));

			objPicSearch.chanSeldID = Integer.parseInt(sp.getString("chanSeldID",""));
			objPicSearch.typeSeldID = Integer.parseInt(sp.getString("typeSeldID",""));
			objPicSearch.lockFSeldID = Integer.parseInt(sp.getString("lockFSeldID",""));

		}

		long picHandle = FHSDK.searchPicture(PlayInfo.userID, objPicSearch);
		if (picHandle == 0)
		{
			//ActivtyUtil.openToast(RemoteRecList.this, "图片查询出错,请重试");
			return false;
		}
		Picture picResult = (new Define()).new Picture();
		while(true)
		{
			if (!FHSDK.searchNextPicture(picHandle, picResult))
			{
				break;
			}
			else
			{
				String sDataSize = null;

				//if (result.mDataSize < 1024)
				//{
				//	sDataSize = String.format("%I64u BYTE", result.mDataSize);
				//}
				if(picResult.dataSize < 1024*1024)
				{
					sDataSize = String.format("%.1fKB", (float)picResult.dataSize/(float)1024);
				}
				else
				{
					sDataSize = String.format("%.1fMB", (float)picResult.dataSize/((float)1024*1024));
				}
				map = new HashMap<String, Object>();
				String strTime = FHSDK.timeConvert(PlayInfo.userID, picResult.startTime);
				map.put("startTime", FHSDK.timeConvert(PlayInfo.userID, picResult.startTime/1000));
				map.put("stopTime",  FHSDK.timeConvert(PlayInfo.userID, picResult.stopTime/1000));
				map.put("tvChanName",  picResult.chanID);
				map.put("tvRecTypeName",  strRecTypeArray[picResult.picType]);
				map.put("tvLoackFlagName", strRecLockFArray[picResult.lockFlag]);
				map.put("tvDataSize", sDataSize);
				map.put("startTimeByMS", picResult.startTime);
				map.put("stopTimeByMS", picResult.stopTime);
				map.put("flag", 1);
				map.put("DataSize", picResult.dataSize);
				map.put("recTypeName",  picResult.picType);
				map.put("loackFlagName", picResult.lockFlag);

				map.put("bGetThumbnail", 0);
				map.put("recThumbnail", R.drawable.video);
				listItem.add(map);

			}
		}
		FHSDK.closeSearchPicture(picHandle);



		myAdapter=new SimpleAdapter(this, listItem, R.layout.rec_search_item,
				new String[]{"recThumbnail", "startTime","stopTime", "tvChanName", "tvRecTypeName", "tvLoackFlagName", "tvDataSize"}, //tvChanName
				new int[]{R.id.recThumbnail, R.id.startTime,R.id.stopTime,R.id.tvChanName, R.id.tvRecTypeName, R.id.tvLockFlagName, R.id.tvDataSize});
		myAdapter.setViewBinder(new ListViewBinder());
		//fileList.setAdapter(myAdapter);

		if ( 0 == fileList.getCount())
		{
			//tvResult.setText("无录像,请修改查询条件重新查询!");
		}
		else
		{
			int color = Color.GREEN;//Color.argb(255,255,255,255);
			//tvResult.setTextColor(color);
			//tvResult.setText("查询到 "+ fileList.getCount()+ " 个录像, 点击录像项进入回放。");
		}



		return true;

	}


	private class ListViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(View view, Object data,
									String textRepresentation) {
			// TODO Auto-generated method stub
			if((view instanceof ImageView) && (data instanceof Bitmap)) {
				ImageView imageView = (ImageView) view;
				Bitmap bmp = (Bitmap) data;
				imageView.setImageBitmap(bmp);
				return true;
			}
			return false;
		}
	}
	class PicThumbnailThread implements Runnable {

		int index = 0, count = 0;
		Bitmap bitmap = null;
		FileInputStream fis = null;
		HashMap<String, Object> map = null;
		private boolean isStarting = false;
		public void stop()
		{
			isStarting = false;
		}
		public void start()
		{
			isStarting = true;
			new Thread(this).start();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isStarting)
			{

				if(listItem == null)
				{
					ActivtyUtil.sleep(10);
					continue;
				}

				if (index >= listItem.size())
				{
					index = 0;
					ActivtyUtil.sleep(100);
					//break;
					continue;
				}

				map = listItem.get(index);
				if(map == null)
				{
					index = 0;
					ActivtyUtil.sleep(10);
					continue;
				}
				index++;

				if (0 == (Integer)map.get("flag"))
				{
					ActivtyUtil.sleep(10);
					continue;
				}

				String thumbnailFileName = thumbnailPath + "/"+ String.valueOf(map.get("startTimeByMS")) + "-" + String.valueOf(map.get("stopTimeByMS")) + ".bmp";
				File f = new File(thumbnailFileName);
				if(f.exists())
				{
					ActivtyUtil.sleep(10);
					continue;
				}



				Picture pic = (new Define()).new Picture();

				pic.frameCount = 1;
				pic.lockFlag =  (Integer)map.get("loackFlagName");
				pic.picType =  (Integer)map.get("recTypeName");
				pic.stopTime = (Long)map.get("stopTimeByMS");
				pic.startTime = (Long)map.get("startTimeByMS");
				pic.dataSize = (Long)map.get("DataSize");
				pic.chanID = (Integer)map.get("tvChanName");



				long picDownloadHandle = FHSDK.createPicDownload(PlayInfo.userID,  pic);

				count = 0;
				curStartTime= (Long)map.get("startTimeByMS");
				curStopTime = (Long)map.get("stopTimeByMS");
				isRecvPicBuf = false;
				while((0 != picDownloadHandle) && (count < 50) && !isRecvPicBuf)
				{
					ActivtyUtil.sleep(10);
					count++;
				}
				isRecvPicBuf = false;
				curStartTime= 0;
				curStopTime = 0;
				FHSDK.destoryPicDownload(picDownloadHandle);

			}

		}

	}
	class SearchThumbnailThread implements Runnable {

		int index = 0;
		Bitmap bitmap = null;
		FileInputStream fis = null;
		HashMap<String, Object> map = null;
		private boolean isStarting = false;
		public void stop()
		{
			isStarting = false;
		}
		public void start()
		{
			isStarting = true;
			new Thread(this).start();
		}

		public void run() {
			//
			while(isStarting)
			{

				if(listItem == null || myAdapter == null || thumbnailPath == null)
				{
					ActivtyUtil.sleep(10);
					continue;
				}
				if (index >= listItem.size())
				{
					index = 0;
					ActivtyUtil.sleep(100);
					continue;
				}

				map = listItem.get(index);
				if(map == null)
				{
					index = 0;
					ActivtyUtil.sleep(10);
					continue;
				}

				index++;

				if (map.get("bGetThumbnail") == null || 1 == (Integer)map.get("bGetThumbnail"))
				{
					ActivtyUtil.sleep(10);
					continue;
				}

				if(R.drawable.video != (Integer)map.get("recThumbnail"))
				{
					ActivtyUtil.sleep(10);
					continue;
				}

				String thumbnailFileName = thumbnailPath + "/"+ String.valueOf(map.get("startTimeByMS")) + "-" + String.valueOf(map.get("stopTimeByMS")) + ".bmp";
				File f = new File(thumbnailFileName);
				if(!f.exists())
				{
					ActivtyUtil.sleep(10);
					continue;
				}

				try {
					fis = new FileInputStream(thumbnailFileName);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(fis != null)
				{
					try{
						bitmap  = BitmapFactory.decodeStream(fis);
					}catch(Exception e)
					{
						e.printStackTrace();
						ActivtyUtil.sleep(10);
						continue;
					}
					if(null != bitmap){
						//log.e("notify!!");
						map.put("bGetThumbnail", 1);
						map.put("recThumbnail", bitmap);
						mHandler.sendEmptyMessage(UPDATE_THUMBNAIL);
//						SoftReference btm_soft = new SoftReference(bitmap);
//						if(btm_soft.get() != null)
//						{
//							bitmapList.add(btm_soft);
//						}
					}
				}

			}
		}

	}


	private CbDataInterface dataFun = new CbDataInterface(){
		public void cb_data(int type, byte[] data, int len){

			switch(type)
			{
				case NOTIFY_TYPE_SHOT_FileName:
				{
					String filePath = new String(data,0, len);

					String[] m = filePath.split("/");
					String fileName = m[m.length-1];
					//fileScan(path);
					//Log.e("XXX", "fileName="+ fileName);
					//Bundle bundle=new Bundle();
					//bundle.putString("fileName", fileName);
					//Message msg = handler.obtainMessage();
					//msg.what = SHOT_FileName;
					//msg.setData(bundle);
					//handler.sendMessage(msg);

					String picName = ActivtyUtil.getFileNameNoEx(fileName) + ".bmp";

					FileInputStream fis = null;
					try {
						fis = new FileInputStream(filePath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Bitmap bitmap  = BitmapFactory.decodeStream(fis);
//					SoftReference btm_soft = new SoftReference(bitmap);
//					if(btm_soft.get() != null)
//					{
//						bitmapList.add(btm_soft);
//					}
//					HashMap<String, Object> map = listItem.get(0);
//					log.e("map.recName = " + map.get("recName"));
//					map.put("recThumbnail",bitmap);
//					myAdapter.notifyDataSetChanged();


					//Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bitmap, 320, 240);
					Bitmap resizeBmp = ActivtyUtil.resizeImage(bitmap, 90, 60);


					if (mWatermark != null)
						resizeBmp = ActivtyUtil.addWatermark(resizeBmp, mWatermark);


					saveImageToGallery(mContext, picName, resizeBmp);

					File f = new File(filePath);
					if(f != null && f.exists())
					{
						f.delete();
					}

					if(!resizeBmp.isRecycled())
					{
						//resizeBmp.recycle();
					}



					break;
				}
				case NOTIFY_TYPE_SHOT_Fail:
				{

					//Log.v("xxx"," has no sd card");
					//Context  context = mContext;
					//String s = mContext.getString(R.string.ERR_NO_SDCARD);
					//Log.v("xxx", s);
					//ActivtyUtil.openToast(context, s);
//					Message msg = handler.obtainMessage();
//					msg.what = SHOT_Fail;
//					handler.sendMessage(msg);

					break;
				}

				default:
					break;
			}



		}
	};

	public void saveImageToGallery(Context context, String fileName, Bitmap bmp) {

		// File appDir = new File(Environment.getExternalStorageDirectory(), "/Thumbnail");
		// if (!appDir.exists()) {
		//     appDir.mkdir();
		// }
		//fileName = "new_" + fileName;
		// thumbnailPath = appDir.getAbsolutePath();
		//log.e("thumbnailPath = " +thumbnailPath);
		File file = new File(appDir, fileName);
		//log.e("file.getAbsolutePath()" + file.getAbsolutePath());
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		try {
//			MediaStore.Images.Media.insertImage(context.getContentResolver(),
//					file.getAbsolutePath(), fileName, null);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

//		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" +  file.getAbsoluteFile())));
	}



	public static String createRandom(boolean numberFlag, int length){
		String retStr = "";
		String strTable = numberFlag ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
		int len = strTable.length();
		boolean bDone = true;
		do {
			retStr = "";
			int count = 0;
			for (int i = 0; i < length; i++) {
				double dblR = Math.random() * len;
				int intR = (int) Math.floor(dblR);
				char c = strTable.charAt(intR);
				if (('0' <= c) && (c <= '9')) {
					count++;
				}
				retStr += strTable.charAt(intR);
			}
			if (count >= 2) {
				bDone = false;
			}
		} while (bDone);

		return retStr;
	}

	public StreamDataCallBackInterface fun = new StreamDataCallBackInterface(){
		public void StreamDataCallBack(int playHandle, int streamType, FrameHead frameHead, byte[] buf, int dataLen)
		{
			log.e("dataLen = " + dataLen);
			if (0 == dataLen)
			{
				return;
			}

			String thumbnailFileName = thumbnailPath + "/"+ String.valueOf(curStartTime) + "-" + String.valueOf(curStartTime) + ".bmp";
			File file = new File(thumbnailFileName);
			if(file.exists()){
				return;//file.delete();
			}
			try {
				//file.createNewFile();
				FileOutputStream out = new FileOutputStream(new File(thumbnailFileName));// 指定要写入的图片
				out.write(buf, 0, dataLen);// 将读取的内容，写入到输出流当中
				out.close();// 关闭输入输出流
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isRecvPicBuf = true;


//			FileInputStream fis = null;
//			try {
//				fis = new FileInputStream(thumbnailFileName);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

//			Bitmap bitmap  = BitmapFactory.decodeStream(fis);
//			SoftReference btm_soft = new SoftReference(bitmap);
//			if(btm_soft.get() != null)
//			{
//				bitmapList.add(btm_soft);
//			}
//			HashMap<String, Object> map = listItem.get(0);
//			log.e("map.recName = " + map.get("recName"));
//			map.put("recThumbnail",bitmap);
//			myAdapter.notifyDataSetChanged();


			//Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bitmap, 320, 240);
			//Bitmap resizeBmp = ActivtyUtil.resizeImage(bitmap, 90, 60);
			//saveImageToGallery(mContext, thumbnailFileName, resizeBmp);
//			bitmap.recycle();
//			bitmap = null;
//
//			resizeBmp.recycle();
//			resizeBmp = null;

//			//随机生成文件名， 保证不重复， 使用同一个文件会出现不更新的情况。
//			File sd = Environment.getExternalStorageDirectory();
//			String tmpfile = sd.getPath() + "/" + createRandom(false, 20)+".jpeg";
//			File file = new File(tmpfile);
//			if(file.exists()){
//				file.delete();
//			}
//
//			try {
//				 //file.createNewFile();
//				 FileOutputStream out = new FileOutputStream(new File(tmpfile));// 指定要写入的图片
//                 out.write(buf, 0, dataLen);// 将读取的内容，写入到输出流当中
//			     out.close();// 关闭输入输出流
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Intent it = getImageFileIntent(tmpfile);
//			startActivity(it);
//
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(file.exists()){
//				file.delete();
//			}
		}
	};

	public StreamDataCallBackInterface fun2 = new StreamDataCallBackInterface(){
		public void StreamDataCallBack(int playHandle, int streamType, FrameHead frameHead, byte[] buf, int dataLen)
		{
			log.e("dataLen = " + dataLen);
			if (0 == dataLen)
			{
				return;
			}
			//随机生成文件名， 保证不重复， 使用同一个文件会出现不更新的情况。
			File sd = Environment.getExternalStorageDirectory();
			String tmpfile = sd.getPath() + "/" + createRandom(false, 20)+".jpeg";
			File file = new File(tmpfile);
			if(file.exists()){
				file.delete();
			}

			try {
				//file.createNewFile();
				FileOutputStream out = new FileOutputStream(new File(tmpfile));// 指定要写入的图片
				out.write(buf, 0, dataLen);// 将读取的内容，写入到输出流当中
				out.close();// 关闭输入输出流
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent it = getImageFileIntent(tmpfile);
			startActivity(it);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(file.exists()){
				file.delete();
			}
		}
	};


	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			HashMap<String,Object> map=(HashMap<String,Object>)fileList.getItemAtPosition(arg2);
			SharedPreferences sp = null;
			//FHSDK.PBChan = Integer.parseInt(map.get("tvChanName"));
			int flag =(Integer)map.get("flag");
			if (flag == 1)
			{
				FHSDK.registerStreamDataCallBack(fun2);
				Picture pic = (new Define()).new Picture();

				pic.frameCount = 1;
				pic.lockFlag =  (Integer)map.get("loackFlagName");
				pic.picType =  (Integer)map.get("recTypeName");
				pic.stopTime = (Long)map.get("stopTimeByMS");
				pic.startTime = (Long)map.get("startTimeByMS");
				pic.dataSize = (Long)map.get("DataSize");
				pic.chanID = (Integer)map.get("tvChanName");

				if (picDownloadHandle != 0)
				{
					FHSDK.destoryPicDownload(picDownloadHandle);
					picDownloadHandle = 0;
				}
				picDownloadHandle = FHSDK.createPicDownload(PlayInfo.userID,  pic);
			}
			else
			{

				String recType = (String)map.get("tvRecTypeName");
				if (recType.equals(strRecTypeArray[0]))
				{
					PlayInfo.pbRecType = 0;
				}
				else if (recType.equals(strRecTypeArray[1]))
				{
					PlayInfo.pbRecType = 1;
				}
				else if (recType.equals(strRecTypeArray[2]))
				{
					PlayInfo.pbRecType = 2;
				}
				//else if (recType.equals(strRecTypeArray[3]))
				//{
				//	FHSDK.PBRecType = 3;
				//}
				else
				{
					Log.e(TAG, "recType err!!");
					return;
				}

				VideoPlayView.PBStartTime = (Long)map.get("startTimeByMS");
				VideoPlayView.PBStopTime = (Long)map.get("stopTimeByMS");

				PlayInfo.playType = FHSDK.PLAY_TYPE_REMOTE_PLAYBACK;//远程回放
				PlayInfo.pbStartTime = (Long)map.get("startTimeByMS");
				PlayInfo.pbStopTime = (Long)map.get("stopTimeByMS");
				PlayInfo.transMode = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_TRANS_MODE);
				PlayInfo.decodeType  = moreCfgObj.getDecodecType(mContext);
				PlayInfo.displayMode = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_DISPLAY_MODE);
				Intent intent = new Intent(mContext, VideoPlayByOpengl.class);
				startActivity(intent);
			}
		}

	};
	public Intent getImageFileIntent( String param ) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param ));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}
	protected void onDestroy()
	{
		if (picDownloadHandle != 0)
		{
			FHSDK.destoryPicDownload(picDownloadHandle);
			picDownloadHandle = 0;
		}
		super.onDestroy();
	}
}
