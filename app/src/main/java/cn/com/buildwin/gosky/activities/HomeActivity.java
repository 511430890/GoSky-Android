package cn.com.buildwin.gosky.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

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
                Intent i = new Intent(HomeActivity.this, ControlPanelActivity.class);
                startActivity(i);
                // Activity slide from left
                overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
                break;
            }
        }
    }

}
