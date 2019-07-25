package com.fh.lib;

import android.util.Log;


public class Define {
	public interface SerialDataCallBackInterface{
		public abstract int SerialDataCallBack(int serialHandle, byte[] buffer, int bufferLen);
	}

	public interface CbDataInterface{
		public abstract void cb_data(int type, byte[] data, int len);
	}

	public interface StreamDataCallBackInterface{
		public abstract void StreamDataCallBack(int playHandle, int streamType,  FrameHead frameHead, byte[] buf, int dataLen);
	}
	public interface YUVDataCallBackInterface{
		public abstract void update(byte[] yuv);
		public abstract void update(byte[] y, byte[] u, byte[] v);
		public abstract void update(int w, int h);
	}


	public class VideoEncode {
		public int res;
		public int quality;
		public int ctrlType;
		public int maxFRate;
		public int maxBitRate;
		public int iFrameInterval;
		public int denoise;
		public int deinter;
	}
	public class WifiConfig {
		public String sSSID;
		public String sPSK;
		public String sChan;
		public int wifiMode;
		public int wifiType;
		public int status;

	}
	public class IpConfig {
		public int isAutoIP;
		public String sPort;
		public String sIP;
		public String sMark;
		public String sGateway;
	}
	public class DevSearch {
		public int    isAlive;
		public String devName;
		public String devIP;
		public String port;
	}
	public class RecSearch {
		public int startYear;
		public int startMonth;
		public int startDay;
		public int stopYear;
		public int stopMonth;
		public int stopDay;
		public int chanSeldID;
		public int typeSeldID;
		public int lockFSeldID;
	}
	public class Record{

		public long dataSize;
		public long startTime;
		public long stopTime;
		public int chanID;
		public int recType;
		public int lockFlag;

	}

	public class PicSearch {
		public int startYear;
		public int startMonth;
		public int startDay;
		public int stopYear;
		public int stopMonth;
		public int stopDay;
		public int chanSeldID;
		public int typeSeldID;
		public int lockFSeldID;
	}

	public class Picture{
		public long frameCount;  // pic count
		public long dataSize;
		public long startTime;
		public long stopTime;
		public int chanID;
		public int picType;
		public int lockFlag;

	}


	public class SDCardInfo {
		public byte state;
		public long totalSize;
		public long usedSize;
	}
	public class SDCardFormat {
		public int formatState;
		public int formatProgress;
	}

	public class PBRecTime {
		public long pbStartTime;
		public long pbStopTime;
	}
	public class BCSS{
		public int brightness;
		public int contrast;
		public int saturation;
		public int sharpness;
	}
	public class Preview {
		public int chan;
		public int encId;
		public int transMode;
		public int blocked;
	}

	public class FrameHead {
		public int frameType;
		public int videoFormat;
		public int width;
		public int height;
		public long timeStamp;
	}

	public class DeviceTime{
		public int year;   // 年(1970-2038)
		public int month;  // 月(1-12)
		public int day;    // 日(1-31)
		public int wday;   // 星期(0-6)(0-星期日, 1-星期一...)
		public int hour;   // 时(0-23)
		public int minute; // 分(0-59)
		public int second; // 秒(0-59)
		public int msecond;// 毫秒(0-999)
	}

	public class Circle{
		public int x;
		public int y;
		public int r;
	}

	public class SerialPortCfg{
		public int baudRate;
		public int dataBit;
		public int stopBit;
		public int parity;
		public int flowCtrl;
	}

	public enum Res_e{
		FHNPEN_ER_QCIF("QCIF" ,0),
		FHNPEN_ER_CIF ("CIF"  ,1),
		FHNPEN_ER_HALFD1("HALFD1" ,2),
		FHNPEN_ER_4CIF("4CIF" ,3),
		FHNPEN_ER_D1  ("D1"   ,4),
		FHNPEN_ER_960H ("960H" ,5),
		FHNPEN_ER_720P ("720P" ,6),
		FHNPEN_ER_1080P("1080P",7),
		FHNPEN_ER_960P ("960P" ,8),
		FHNPEN_ER_640x480 ("VGA(640x480)"  ,9),
		FHNPEN_ER_QVGA ("QVGA" ,10),
	    FHNPEN_ER_640x360("VGA(640x360)" ,11),          // VGA 640x360
	    FHNPEN_ER_960x960("960x960" ,12),          // 960x960
	    FHNPEN_ER_2048x1536("2048x1536" ,13),
	    FHNPEN_ER_2048x1520("2048x1520" ,14),
	    FHNPEN_ER_2048x2048("2048x2048" ,15),
	    FHNPEN_ER_1536x1536("1536x1536" ,16),
	    FHNPEN_ER_1520x1520("1520x1520" ,17),
	    FHNPEN_ER_1024x1024("1024x1024" ,18),
	    FHNPEN_ER_512x512("512x512" ,19),
	    FHNPEN_ER_CIF_N("CIF_N(352x240)" ,20),            // 352x240
	    FHNPEN_ER_4CIF_N("4CIF_N(704x480)" ,21),           // 704x480
	    FHNPEN_ER_D1_N("D1_N(720x480)" ,22),             // 720x480
	    FHNPEN_ER_768x768("768x768" ,23),
	    FHNPEN_ER_384x384("384x384" ,24),

		// 2018.09.28 add 
		FHEN_ER_1600x904("1600x904" ,25),
		FHEN_ER_1600x1200("1600x1200" ,26),
		FHEN_ER_2560x1440("2560x1440" ,27),
		FHEN_ER_2560x1944("2560x1944" ,28),
		FHEN_ER_2560x1920("2560x1920" ,29),          //4:3
		FHEN_ER_2560x1600("2560x1600" ,30),          //16:10
		FHEN_ER_4096x2160("4096x2160" ,31),
		FHEN_ER_1920x1200("1920x1200" ,32),
		FHEN_ER_1440x904("1440x904" ,33),
		FHEN_ER_1360x768("1360x768" ,34),
		FHEN_ER_1792x1344("1792x1344" ,35),
		FHEN_ER_2048x1152("2048x1152" ,36),
		FHEN_ER_2304x1728("2304x1728" ,37),
		FHEN_ER_2592x1944("2592x1944" ,38),
		FHEN_ER_3072x1728("3072x1728" ,39),
		FHEN_ER_2816x2112("2816x2112" ,40),
		FHEN_ER_3072x2304("3072x2304" ,41),
		FHEN_ER_3264x2448("3264x2448" ,42),
		FHEN_ER_3840x2160("3840x2160" ,43),
		FHEN_ER_3456x2592("3456x2592" ,44),
		FHEN_ER_3600x2704("3600x2704" ,45),
		FHEN_ER_4096x2304("4096x2304" ,46),
		FHEN_ER_3840x2800("3840x2800" ,47),
		FHEN_ER_4000x3000("4000x3000" ,48),
		FHEN_ER_4608x2592("4608x2592" ,49),
		FHEN_ER_4096x3072("4096x3072" ,50),
		FHEN_ER_4800x3200("4800x3200" ,51),
		FHEN_ER_5120x2880("5120x2880" ,52),
		FHEN_ER_5120x3840("5120x3840" ,53),
		FHEN_ER_6400x4800("6400x4800" ,54),

	    FHNPEN_ER_1072x1072("1072x1072" ,128); // 1072x1072

		
		private int index;
		private String name;
		private Res_e(String name, int index)
		{
			this.name = name;
			this.index = index;
		}
		public int getIndex()
		{
			return this.index;
		}
		public String getName()
		{
			return this.name;
		}
		public static String getNameByIndex(int index)
		{
			for(Res_e res : Res_e.values())
			{
				if(index == res.index)
					return res.name;
			}
			return null;
		}
	}
}
