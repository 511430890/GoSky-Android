package eye.app.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.app.util.*;
import com.app.view.VideoPlayView;
import com.fh.lib.Define.CbDataInterface;
import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

 public class LocateRecList extends Activity {
	private static final String TAG = "RecListActivity";
	
	private static final int SEARCH_DONE = 0;
	private static final int UPDATE_THUMBNAIL = 1;
	
	
	private static final int NOTIFY_TYPE_SHOT_FileName = 0x00;
	private static final int NOTIFY_TYPE_SHOT_Fail     = 0x01;

	protected ListView fileList; 
	//private TextView tvResult;
	private SharedPreferences sp = null;
	private SimpleAdapter myAdapter;
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
	
	private String path;
	private List<String> mFileItems;
	private MoreCfg moreCfgObj = new MoreCfg();
	public Context mContext;
	
	public Bitmap mWatermark = null;
	public SearchThumbnailThread mSearchThumbnailThread= null;
	
	public File appDir = null;
	public static String thumbnailPath = null;
	public int thumbHandle = 0;
	public ArrayList<SoftReference> bitmapList = new ArrayList<SoftReference>();
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		super.onCreate(savedInstanceState);
		mContext = this;
		setTitle("本地录像");
		setContentView(R.layout.rec_search_list);
		MyApplication.getInstance().addActivity(this);
		findView();

    	Thread mSearchThread = new Thread(new SearchRecThread(), "SearchThread");
    	mSearchThread.start();
	}

	public void findView(){
		fileList = (ListView)findViewById(R.id.listViewRec);
		fileList.setOnItemClickListener(listItemListener);
		fileList.setOnItemLongClickListener(LongClickListener);
		
		//tvResult = (TextView)findViewById(R.id.tvResult);
		
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
	/*
	static String getDateTimeByMillisecond(long millisecond) {  
	    Date date = new Date(Long.valueOf(millisecond));  
	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
	    String time = format.format(date);  
	    return time;  
	}  
	*/
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			 switch (msg.what) {
				case SEARCH_DONE:
					fileList.setAdapter(myAdapter);
				case UPDATE_THUMBNAIL:
					myAdapter.notifyDataSetChanged();
					break;
			 }
		}
	};
	
	class SearchRecThread implements Runnable {
	    public void run() {
	    	if (recSearch())
	    	{
	    		mHandler.sendEmptyMessage(SEARCH_DONE);
	    	}
	    }
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int i;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mSearchThumbnailThread != null)
            	mSearchThumbnailThread.stop();

    	    if (appDir.exists()) {
    	    	//ActivtyUtil.RecursionDeleteFile(appDir);
    	    }


			HashMap<String, Object> map;
			for(i = 0; i < listItem.size(); i++)
			{
				Bitmap bitmap = null;
				map = listItem.get(i);
				try {
					bitmap = (Bitmap) map.get("recThumbnail");
					if(bitmap != null && !bitmap.isRecycled())
					{
						bitmap.recycle();
					}
				}catch(Exception e){
					e.printStackTrace();
				}

			}


        }
        
	    for(i = 0; i < bitmapList.size(); i++)
	    {
	    	SoftReference soft = bitmapList.get(i);
			if(soft != null && soft.get() !=null)
	    		((Bitmap) soft.get()).recycle();
	    	//bmp.recycle();
	    }
        
        //FHSDK.thumbnailStop(thumbHandle);
		
        return super.onKeyDown(keyCode, event);
    }
	
	
	
	public boolean recSearch()
	{
		if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
			File sd = Environment.getExternalStorageDirectory();
			path = sd.getPath() + VideoPlayView.getSettingPath();
			//log.e(path);
			File file = new File(path);
			if (!file.exists()) 
			{
				file.mkdir();
			}
			mFileItems =new ArrayList<String>();
			getFile(file);
						
        }else{
            Toast.makeText(this, getString(R.string.str_nofoundSDCard), Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
		return true;
	}

	public void saveImageToGallery(Context context, String fileName, Bitmap bmp) {
		
	    File file = new File(appDir, fileName);
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
	  
//	    try {
//	        MediaStore.Images.Media.insertImage(context.getContentResolver(),
//					file.getAbsolutePath(), fileName, null);
//	    } catch (FileNotFoundException e) {
//	        e.printStackTrace();
//	    }
	    
	    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" +  file.getAbsoluteFile())));
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

					String thumbnailFileName = ActivtyUtil.getFileNameNoEx(fileName) + ".bmp";
					
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(filePath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Bitmap bitmap  = BitmapFactory.decodeStream(fis);
					SoftReference btm_soft = new SoftReference(bitmap);
					if(btm_soft.get() != null)
					{
						bitmapList.add(btm_soft);
					}
//					HashMap<String, Object> map = listItem.get(0);
//					log.e("map.recName = " + map.get("recName"));
//					map.put("recThumbnail",bitmap);
//					myAdapter.notifyDataSetChanged();  
					
					
					//Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bitmap, 320, 240); 
					Bitmap resizeBmp = ActivtyUtil.resizeImage(bitmap, 90, 60);
					
					if (mWatermark != null)
						resizeBmp = ActivtyUtil.addWatermark(resizeBmp, mWatermark);
					
					
					saveImageToGallery(mContext, thumbnailFileName, resizeBmp);
					
					File f = new File(filePath);
					if(f != null && f.exists())
					{
						f.delete();
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
	public void getFile(File file){
		HashMap<String, Object> map; 
		File[] fileArray =file.listFiles();


		if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
			File sd = Environment.getExternalStorageDirectory();
			String path = sd.getPath() + VideoPlayView.getSettingPath();
			File mFile = new File(path);
			if (!mFile.exists()) 
			{
				mFile.mkdir();
			}
			FHSDK.setShotPath(mFile.getAbsolutePath());
		}

		if (null == fileArray || fileArray.length <= 0) {
			log.e("error, fileArray length = 0");
			return;
		}

		//thumbHandle = FHSDK.thumbnailStart();
		FHSDK.registerNotifyCallBack(dataFun);
		for (File f : fileArray) {
			if(f.isFile()){
				if((".h264".equalsIgnoreCase(getFileEx(f)))
				  ||(".h265".equalsIgnoreCase(getFileEx(f)))
				  ||(".avi".equalsIgnoreCase(getFileEx(f)))
				  ||(".mp4".equalsIgnoreCase(getFileEx(f)))
				  ||(".jpg".equalsIgnoreCase(getFileEx(f)))
				  ||(".bmp".equalsIgnoreCase(getFileEx(f)))){
					String sDataSize = null;
					long len = f.length();
					//if (len < 1024)
					//{
					//	//sDataSize = String.format("%I64u BYTE", len);
					//}
					if(len < 1024*1024)
						sDataSize = String.format("%.1fKB", (float)len/(float)1024);
					else
						sDataSize = String.format("%.1fMB", (float)len/((float)1024*1024));
					//String item = f.getName() + "  大小:" + sDataSize;
					map = new HashMap<String, Object>(); 
					
					map.put("recName", f.getName());
					map.put("recSize", sDataSize);
					
					if (".jpg".equalsIgnoreCase(getFileEx(f))
						|| ".bmp".equalsIgnoreCase(getFileEx(f)))
					{
						String s  = f.getAbsolutePath();
						Bitmap bmp = getImageThumbnail(f.getAbsolutePath(), 90, 60);
						if (bmp != null)
							map.put("recThumbnail", bmp);
					}
					else if (".avi".equalsIgnoreCase(getFileEx(f))
							|| ".mp4".equalsIgnoreCase(getFileEx(f)))
					{
						
						String s  = f.getAbsolutePath();
						Bitmap bmp = getVideoThumbnail(f.getAbsolutePath(), 90, 60, Thumbnails.MICRO_KIND);
						if (bmp != null)
							map.put("recThumbnail", bmp);
						else
							map.put("recThumbnail", R.drawable.video);
					}
					else
					{
						map.put("bGetThumbnail", 0);
						map.put("recPath", f.getAbsolutePath());
						map.put("recThumbnail", R.drawable.video);
						
						//FHSDK.thumbnailAddLocateFile(thumbHandle, f.getAbsolutePath(), 1);

					}
					listItem.add(map);
					
				}
			}else{
				getFile(f);
			}
		}
		myAdapter=new SimpleAdapter(this, listItem, R.layout.locate_rec_search_item, 
					new String[]{"recThumbnail", "recName","recSize"},  
					new int[]{R.id.recThumbnail, R.id.recName,R.id.recSize});
		myAdapter.setViewBinder(new ListViewBinder());  
		//fileList.setAdapter(myAdapter);
		
		if ( 0 == fileList.getCount())
		{
			//tvResult.setText("未找到条数据。路????" + path);
		}
		else
		{
			int color = Color.GREEN;//Color.argb(255,255,255,255);
			//tvResult.setTextColor(color);
			//tvResult.setText("查询????"+ fileList.getCount()+ " 条数据?? 路径:" + path);
		}
		
    	mSearchThumbnailThread = new SearchThumbnailThread();
    	mSearchThumbnailThread.start();
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

				String recPath = (String)map.get("recPath");
				String[] m = recPath.split("/");
				String fileName = m[m.length - 1]; 
				fileName = ActivtyUtil.getFileNameNoEx(fileName);
				String thumbnailFileName = thumbnailPath + "/"+ fileName + ".bmp";
				//log.e("thumbnailFileName =" + thumbnailFileName);
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
						//bitmap  = BitmapFactory.decodeStream(fis);
					}catch(Exception e)
					{
						e.printStackTrace();
						ActivtyUtil.sleep(10);
						continue;
					}
					if(null != bitmap){
						map.put("bGetThumbnail", 1);
						map.put("recThumbnail", bitmap);
						mHandler.sendEmptyMessage(UPDATE_THUMBNAIL);
						SoftReference btm_soft = new SoftReference(bitmap);
						if(btm_soft.get() != null)
						{
							bitmapList.add(btm_soft);
						}

					}
				}
				
	    	}
	    }
	    
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

	
	public String getFileEx(File file){
		String fileName=file.getName();
		int index=fileName.lastIndexOf('.');
		int length=fileName.length();
		String str=fileName.substring(index,length);
		return str;
	}

	private OnItemClickListener listItemListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			HashMap<String,String> map=(HashMap<String,String>)fileList.getItemAtPosition(arg2);
			String fileName = map.get("recName");
			File sd = Environment.getExternalStorageDirectory();
			String path = sd.getPath() + VideoPlayView.getSettingPath();
			//get file suffix
			int index=fileName.lastIndexOf('.');
			int length=fileName.length();
			String str=fileName.substring(index,length);
			if (".h264".equalsIgnoreCase(str))
			{
				//FHSDK.apiInit();
				PlayInfo.playType = FHSDK.PLAY_TYPE_LOCATE_PLAYBACK;//本地回放
				PlayInfo.pbRecFilePath = path + "/" + fileName;
				PlayInfo.decodeType  = moreCfgObj.getDecodecType(mContext);
				PlayInfo.displayMode = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_DISPLAY_MODE);
				Intent intent = new Intent(mContext, VideoPlayByOpengl.class);
				startActivity(intent);
			}
			else if (".h265".equalsIgnoreCase(str))
			{
				//FHSDK.apiInit();
				PlayInfo.playType = FHSDK.PLAY_TYPE_LOCATE_PLAYBACK;//本地回放
				PlayInfo.pbRecFilePath = path + "/" + fileName;
				PlayInfo.decodeType  = moreCfgObj.getDecodecType(mContext);
				PlayInfo.displayMode = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_DISPLAY_MODE);
				Intent intent = new Intent(mContext, VideoPlayByOpengl.class);
				startActivity(intent);
			}
			else if(".mp4".equalsIgnoreCase(str))
			{
				PlayInfo.playType = FHSDK.PLAY_TYPE_MP4FILE;//本地回放
				PlayInfo.pbRecFilePath = path + fileName;
				PlayInfo.decodeType  = moreCfgObj.getDecodecType(mContext);
				PlayInfo.displayMode = moreCfgObj.getLocateConfig(mContext, moreCfgObj.LOCATE_CONFIG_DISPLAY_MODE);

				Intent intent = new Intent(mContext, VideoPlayByOpengl.class);
				startActivity(intent);
				
			}
			else if(".avi".equalsIgnoreCase(str))
			{
				String filePath = path + "/" + fileName;
				Intent it = getVideoFileIntent(filePath);
				try{
					startActivity(it);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				
			}
			else if (".jpg".equalsIgnoreCase(str) || ".bmp".equalsIgnoreCase(str))
			{
				String filePath = path + "/" + fileName;
				Intent it = getImageFileIntent(filePath);
				startActivity(it);
			}
		}
	};
	private OnItemLongClickListener LongClickListener = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			HashMap<String,String> map=(HashMap<String,String>)fileList.getItemAtPosition(arg2);
			final int item = arg2;
			final String fileName = map.get("recName");
			new AlertDialog.Builder(LocateRecList.this).setTitle(getString(R.string.id_delete))
			.setMessage(getString(R.string.str_deleteSure)).setPositiveButton(
					getString(R.string.id_sure), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int which) {
							try {
								
								if (Environment.getExternalStorageState().equals(
						                Environment.MEDIA_MOUNTED)) {
									File sd = Environment.getExternalStorageDirectory();
									String filePath = sd.getPath() + VideoPlayView.getSettingPath() + "/" + fileName;
									File file = new File(filePath);
									if (file.exists()) { // 判断文件是否存在
						                file.delete(); // delete()方法 你应该知????是删除的意??;
						            } 
									listItem.remove(item);
									myAdapter.notifyDataSetChanged();  
									//tvResult.setText("查询????"+ fileList.getCount()+ " 条数据?? 路径:" + path);
								}
							} catch (Exception e) {
								//Log.e(TAG, e.getMessage(), e);
								ActivtyUtil.openToast(LocateRecList.this, e
										.getMessage());
							}
						}
					}).setNegativeButton(getText(R.string.id_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {

						}

					}).show();
			return true;//返回false会同时响应onItemClick，返回true只响应onItemLongClick
			//return false;
		}
   	};   


    public Intent getImageFileIntent( String param ) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    
    public Intent getVideoFileIntent(String param){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra ("oneshot",0);
        intent.putExtra ("configchange",0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType (uri, "video/*");
        return intent;
    }
    
    
    
    public static boolean deleteFoder(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文????
                file.delete(); // delete()方法 你应该知????是删除的意??;
            } else if (file.isDirectory()) { // 否则如果它是??????目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                if (files != null) {
                    for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                        deleteFoder(files[i]); // 把每个文????用这个方法进行迭????
                    }
                }
            }
            boolean isSuccess = file.delete();
            if (!isSuccess) {
                    return false;
            }
        }
        return true;
    }

	public boolean onCreateOptionsMenu(Menu menu) {  
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, getString(R.string.str_deleteList)).setIcon(R.drawable.btn_clear);
		return super.onCreateOptionsMenu(menu);
	} 
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		        // TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case Menu.FIRST + 1:
		{	
			new AlertDialog.Builder(LocateRecList.this).setTitle(getString(R.string.str_deleteList))
			.setMessage(getString(R.string.str_deleteAllRecordSure)).setPositiveButton(
					getString(R.string.id_sure), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int which) {
							try {
								if (Environment.getExternalStorageState().equals(
						                Environment.MEDIA_MOUNTED)) {
									File sd = Environment.getExternalStorageDirectory();
									String filePath = sd.getPath() + VideoPlayView.getSettingPath();
									File file = new File(filePath);
									deleteFoder(file);
									listItem.clear();
									myAdapter.notifyDataSetChanged();  
									//tvResult.setText("数据已清????);
								}
							} catch (Exception e) {
								//Log.e(TAG, e.getMessage(), e);
								ActivtyUtil.openToast(LocateRecList.this, e
										.getMessage());
							}
						}
					}).setNegativeButton(getString(R.string.id_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {

						}

					}).show();
			break;
		}
		default:
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

/**  
     * 根据指定的图像路径和大小来获取缩略图  
     * 此方法有两点好处???? 
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度???? 
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图??  
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使  
     *        用这个工具生成的图像不会被拉伸??  
     * @param imagePath 图像的路???? 
     * @param width 指定输出图像的宽???? 
     * @param height 指定输出图像的高???? 
     * @return 生成的缩略图  
     */    

    private Bitmap getImageThumbnail(String imagePath, int width, int height) {    
        Bitmap bitmap = null;    
        BitmapFactory.Options options = new BitmapFactory.Options();    
        options.inJustDecodeBounds = true;    
        // 获取这个图片的宽和高，注意此处的bitmap为null    
        bitmap = BitmapFactory.decodeFile(imagePath, options);    
        options.inJustDecodeBounds = false; // 设为 false    
        // 计算缩放????   
        int h = options.outHeight;    
        int w = options.outWidth;    
        int beWidth = w / width;    
        int beHeight = h / height;    
        int be = 1;    
        if (beWidth < beHeight) {    
            be = beWidth;    
        } else {    
            be = beHeight;    
        }    
        if (be <= 0) {    
            be = 1;    
        }    
        options.inSampleSize = be;    
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false    
        bitmap = BitmapFactory.decodeFile(imagePath, options);    
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象    
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,    
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);    
        return bitmap;    
    }    
    
    /**  
     * 获取视频的缩略图  
     * 先??过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图??  
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的??，这样会节省内存???? 
     * @param videoPath 视频的路???? 
     * @param width 指定输出视频缩略图的宽度  
     * @param height 指定输出视频缩略图的高度???? 
     * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND???? 
     *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96  
     * @return 指定大小的视频缩略图  
     */    
    private Bitmap getVideoThumbnail(String videoPath, int width, int height,    
            int kind) {    
        Bitmap bitmap = null;    
        // 获取视频的缩略图    
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);    
        //System.out.println("w"+bitmap.getWidth());    
        //System.out.println("h"+bitmap.getHeight());   
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,    
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);    
        return bitmap;    
    }    



	
}
