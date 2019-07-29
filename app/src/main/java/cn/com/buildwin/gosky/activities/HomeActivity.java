package cn.com.buildwin.gosky.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fh.lib.FHSDK;
import com.fh.lib.PlayInfo;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.buildwin.gosky.R;
import cn.com.buildwin.gosky.application.Settings;
import eye.app.activity.VideoPlayByOpengl;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.home_help_button)    ImageButton mHelpButton;
    @BindView(R.id.home_setting_button) ImageButton mSettingButton;
    @BindView(R.id.home_play_button)    ImageButton mPlayButton;

    @BindString(R.string.permission_denied_prefix)
    String msgPrefix;
    @BindString(R.string.permission_denied_comma)
    String msgComma;
    @BindString(R.string.permission_denied_external_storage)
    String msgDeniedExtStorage;
    @BindString(R.string.permission_denied_record_audio)
    String msgDeniedRecordAudio;
    private  Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // check permissions
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                )
                .withListener(allPermissionsListener)
                .check();
    }

    private MultiplePermissionsListener allPermissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            if (!report.areAllPermissionsGranted()) {
                StringBuilder msg = new StringBuilder();
                List<PermissionDeniedResponse> deniedList = report.getDeniedPermissionResponses();

                for (PermissionDeniedResponse response : deniedList) {
                    if (msg.length() != 0)
                        msg.append(msgComma);

                    switch (response.getPermissionName()) {
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        case Manifest.permission.READ_EXTERNAL_STORAGE:
                            msg.append(msgDeniedExtStorage);
                            break;
                        case Manifest.permission.RECORD_AUDIO:
                            msg.append(msgDeniedRecordAudio);
                            break;
                    }
                }
                msg.insert(0, msgPrefix);
                Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

        }
    };

    @OnClick({R.id.home_help_button, R.id.home_setting_button, R.id.home_play_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_help_button: {
                // 显示帮助界面
                Intent i = new Intent(HomeActivity.this, HelpActivity.class);
                startActivity(i);
                break;
            }
            case R.id.home_setting_button: {
                // 显示设置界面
                Intent i = new Intent(HomeActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            }
            case R.id.home_play_button: {
                // 显示控制面板

                if (GetGatawayIP().equals("172.19.10.1")||GetGatawayIP().equals("172.17.10.1")||GetGatawayIP().equals("172.16.10.1") )
                {
                    FHSDK.apiInit();
                    if(!GetGatawayIP().equals("172.16.10.1"))
                    { FHSDK.setCryptKey("guanxukj@fh8620."); }

                    PlayInfo.userID = FHSDK.login(GetGatawayIP(), 8888, "guanxukeji", "gxrdw60");

                    int deviceFlag = FHSDK.getDeviceFlag(PlayInfo.userID);
                    if (Settings.getInstance(this).getParameterForIs720p())
                    {
                       if(2== deviceFlag) //8620
                        {PlayInfo.streamType = 1;}
                        else if (4== deviceFlag) //8852
                       {PlayInfo.streamType = 2;}
                       else if (5== deviceFlag) //8632
                       {PlayInfo.streamType = 2;}


                    }
                    else
                    {
                        if(2== deviceFlag) //8620
                        {PlayInfo.streamType = 2;} //VGA
                        else if (4== deviceFlag) //8852
                        {PlayInfo.streamType = 1;}// 1080P
                        else if (5== deviceFlag) //8632
                        {PlayInfo.streamType = 1;}
                    }



                    Intent i = new Intent(HomeActivity.this, VideoPlayByOpengl.class);
                    startActivity(i);
                    Toast.makeText(this, GetGatawayIP(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent i = new Intent(HomeActivity.this, ControlPanelActivity.class);
                    startActivity(i);
                }
                // Activity slide from left
                overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
                break;
            }
        }
    }

    public String GetGatawayIP()
    {
       WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info= null;
        if (wifiManager != null) {
            info = wifiManager.getDhcpInfo();

        }
        int gateway= 0;
        if (info != null) {
            gateway = info.gateway;
        }
        String ip=intToIp(gateway);
        return ip;
    }
        /**
         * int值转换为ip
         * @param addr
         * @return
         */
        public static String intToIp(int addr) {
            return ((addr & 0xFF) + "." +
                    ((addr >>>= 8) & 0xFF) + "." +
                    ((addr >>>= 8) & 0xFF) + "." +
                    ((addr >>>= 8) & 0xFF));
        }
}
