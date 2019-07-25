package eye.app.activity;

import java.util.LinkedList;
import java.util.List;

import com.app.util.CrashHandler;
import com.app.util.log;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {

	public List<Activity> activityList = new LinkedList<Activity>(); 
	private static MyApplication instance;
	public MyApplication()
    {
    }

     public static MyApplication getInstance()
     {
         if(null == instance)
         {
             instance = new MyApplication();
         }
         return instance;             
     }

     public void addActivity(Activity activity)
     {
         activityList.add(activity);
     }

     public void exit()
     {
         for(Activity activity:activityList)
         {
        	 //Log.i(TAG, "activity is ->" + activity);
             activity.finish();
         }
         System.exit(0);
    }
    
     public void onCreate() {  
         super.onCreate();
         log.e("application create");
         CrashHandler crashHandler = CrashHandler.getInstance();  
         crashHandler.init(getApplicationContext());
     }  
}