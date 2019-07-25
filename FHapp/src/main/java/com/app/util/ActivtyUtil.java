package com.app.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.widget.Toast;
import android.content.SharedPreferences;
public class ActivtyUtil {
		 
	  public static void showAlert(Context context,CharSequence title,CharSequence message,CharSequence btnTitle){
	    	new AlertDialog.Builder(context).setTitle(title)
	    	.setMessage(message).setPositiveButton(btnTitle, new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					
				}
	    		
	    	}).show();
	    }
	    public static void openToast(Context context,String str){
	    	Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	    }
	    
	    
		public static String getCurSysDate()
		{
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); 
			String date = sDateFormat.format(new java.util.Date());
			return date;
		}
	    
		public static String formatTime(long time)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
			String hms = formatter.format(time);
			return hms;
		}
		
	    public static String byte2HexStr(byte[] b) {
		    String stmp = "";
		    StringBuilder sb = new StringBuilder("");
		    for (int n = 0; n < b.length; n++) {
		      stmp = Integer.toHexString(b[n] & 0xFF);
		      sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
		      sb.append(" ");
		    }
		    return sb.toString().toUpperCase().trim();
	    }
	    
	    
	    public static Bitmap getImageFromAssetsFile(Context context, String fileName)
	    {
	    	Bitmap image = null;
	    	AssetManager am = context.getResources().getAssets();
	    	try
	    	{
	    		InputStream is = am.open(fileName);
	    		image = BitmapFactory.decodeStream(is);
	    	}
	    	catch(IOException e)
	    	{
	    		e.printStackTrace();
	    	}
	    	return image;
	    }

	    
		public static Bitmap addWatermark(Bitmap src, Bitmap watermark)
		{
			// 另外创建一张图片
			Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.RGB_565);// 创建一个新的和SRC长度宽度一样的位图 
			Canvas canvas = new Canvas(newb);
			canvas.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入原图片src
			canvas.drawBitmap(watermark, (src.getWidth() - watermark.getWidth()) /2, (src.getHeight()-watermark.getHeight())/2, null);// 涂鸦图片画到原图片中间位置
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			
			//watermark.recycle();
			//watermark = null;
			return newb;
		}
	    
	    public static Bitmap resizeImage(Bitmap bitmap, int w, int h)   
	    {   
	        Bitmap BitmapOrg = bitmap;    
	        int width = BitmapOrg.getWidth();    
	        int height = BitmapOrg.getHeight();    
	        int newWidth = w;    
	        int newHeight = h;      
	        float scaleWidth = ((float) newWidth) / width;    
	        float scaleHeight = ((float) newHeight) / height; 
	        Matrix matrix = new Matrix();    
	        matrix.postScale(scaleWidth, scaleHeight);    
	        return Bitmap.createBitmap(BitmapOrg, 0, 0, width,    
	                        height, matrix, true);    
	    } 
		
	    
	    public static void RecursionDeleteFile(File file){
	        if(file.isFile()){
	            file.delete();
	            return;
	        }
	        if(file.isDirectory()){
	            File[] childFile = file.listFiles();
	            if(childFile == null || childFile.length == 0){
	                file.delete();
	                return;
	            }
	            for(File f : childFile){
	                RecursionDeleteFile(f);
	            }
	            file.delete();
	        }
	    }
	    
	    public static void sleep(long millis)
	    {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
		public static String getFileNameNoEx(String filename) {
			if ((filename != null) && (filename.length() > 0)) {
				int dot = filename.lastIndexOf('.');
				if ((dot >-1) && (dot < (filename.length()))) {
					return filename.substring(0, dot);
				}
			}
			return filename;
		}
}

  