package cn.com.buildwin.gosky.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.com.buildwin.gosky.widget.bwsocket.BWSocket;
import cn.com.buildwin.gosky.R;

public class RenameSSIDActivity extends AppCompatActivity implements BWSocket.BWSocketCallback {

    private static final String TAG = "RenameSSIDActivity";

    @BindView(R.id.rename_ssid_cancel_button)           Button cancelButton;
    @BindView(R.id.rename_ssid_save_button)             Button saveButton;
    @BindView(R.id.rename_ssid_current_ssid_editText)   EditText currentSSIDEditText;
    @BindView(R.id.rename_ssid_new_ssid_editText)       EditText newEditSSIDText;

    @BindString(R.string.rename_hud_collecting_information) String hudInformationText;
    @BindString(R.string.rename_hud_applying_change)        String hudApplyingChangeText;

    @BindString(R.string.rename_alert_change_another_ssid)  String alertChangeAnotherSsidText;
    @BindString(R.string.rename_error_get_info)             String errorGetInfoText;
    @BindString(R.string.rename_error_set_ssid)             String errorSetSsidText;
    @BindString(R.string.rename_error_reset)                String errorResetText;
    @BindString(R.string.rename_alert_get_info_fail)        String alertGetInfoFailText;

    @BindString(R.string.rename_message_box_notice_title)           String mbNoticeTitle;
    @BindString(R.string.rename_message_box_notice_message)         String mbNoticeMessage;
    @BindString(R.string.rename_message_box_notice_confirm_title)   String mbNoticeConfirmTitle;

    @BindString(R.string.rename_message_box_notice2_title)          String mbNotice2Title;
    @BindString(R.string.rename_message_box_notice2_message)        String mbNotice2Message;
    @BindString(R.string.rename_message_box_notice2_positive_title) String mbNotice2PosTitle;
    @BindString(R.string.rename_message_box_notice2_negative_title) String mbNotice2NegTitle;

    @BindString(R.string.rename_message_box_notice3_title)          String mbNotice3Title;
    @BindString(R.string.rename_message_box_notice3_message)        String mbNotice3Message;
    @BindString(R.string.rename_message_box_notice3_confirm_title)  String mbNotice3ConfirmTitle;

    @BindString(R.string.rename_alert_set_ssid_fail)        String alertSetSsidFailText;

    @BindString(R.string.rename_message_box_reset_title)    String mbResetTitle;
    @BindString(R.string.rename_message_box_reset_message)  String mbResetMessage;
    @BindString(R.string.rename_message_box_positive_title) String mbResetPosTitle;
    @BindString(R.string.rename_message_box_negative_title) String mbResetNegTitle;

    @BindString(R.string.rename_alert_reset_fail)           String alertResetFailText;

    private BWSocket asyncSocket;

    private enum WirelessAction {
        WIRELESS_ACTION_IDLE,
        WIRELESS_ACTION_PROCESSING,
        WIRELESS_ACTION_GET_INFO,
        WIRELESS_ACTION_SET_SSID,
        WIRELESS_ACTION_RESET_NET,
    }
    private WirelessAction action = WirelessAction.WIRELESS_ACTION_IDLE;

    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rename_ssid);
        ButterKnife.bind(this);

        // Save Button
        saveButton.setEnabled(false);

        hud = KProgressHUD.create(RenameSSIDActivity.this);
        hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(hudInformationText)
                .show();

        asyncSocket = BWSocket.getInstance();
        asyncSocket.setCallback(this);
        action = WirelessAction.WIRELESS_ACTION_GET_INFO;
        asyncSocket.getInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // release
        asyncSocket.setCallback(null);
        asyncSocket = null;
    }

    @OnClick({R.id.rename_ssid_cancel_button, R.id.rename_ssid_save_button, })
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rename_ssid_cancel_button: {
                finish();
                break;
            }
            case R.id.rename_ssid_save_button: {
                hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel(hudApplyingChangeText)
                        .show();

                action = WirelessAction.WIRELESS_ACTION_SET_SSID;
                String newSSIDString = newEditSSIDText.getText().toString();
                asyncSocket.setSSID(newSSIDString);
                break;
            }
        }
    }

    /* OnTextChanged */

    @OnTextChanged(value = R.id.rename_ssid_new_ssid_editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        String newSSIDString = editable.toString();
        if (newSSIDString.length() > 0) {
            // 相同名称则禁用保存
            if (newEditSSIDText.getText().toString().compareTo(currentSSIDEditText.getText().toString()) == 0 ) {
                saveButton.setEnabled(false);
                Toast.makeText(RenameSSIDActivity.this, alertChangeAnotherSsidText, Toast.LENGTH_SHORT).show();
            } else {
                saveButton.setEnabled(true);
            }
        }
        else
            saveButton.setEnabled(false);
    }

    /* BWSocketCallback */

    @Override
    public void didConnectToHost(String host, int port) {
        Log.d(TAG, "Callback didConnectToHost: " + host + "(" + port + ")");

    }

    @Override
    public void didDisconnectFromHost() {
        Log.d(TAG, "Callback didDisconnectFromHost");

        switch (action) {
            case WIRELESS_ACTION_GET_INFO:
                Toast.makeText(RenameSSIDActivity.this, errorGetInfoText, Toast.LENGTH_SHORT).show();
                break;
            case WIRELESS_ACTION_SET_SSID:
                Toast.makeText(RenameSSIDActivity.this, errorSetSsidText, Toast.LENGTH_SHORT).show();
                break;
            case WIRELESS_ACTION_RESET_NET:
                Toast.makeText(RenameSSIDActivity.this, errorResetText, Toast.LENGTH_SHORT).show();
                break;
        }
        action = WirelessAction.WIRELESS_ACTION_IDLE;
        hud.dismiss();
    }

    @Override
    public void didGetInformation(HashMap<String, String> map) {
        Log.d(TAG, "Callback didGetInformation: " + map);

        hud.dismiss();

        //
        String methodString = map.get(BWSocket.kKeyMethod);
        switch (action) {
            case WIRELESS_ACTION_GET_INFO:
                action = WirelessAction.WIRELESS_ACTION_PROCESSING;
                boolean got = false;
                if (methodString != null && methodString.compareTo(BWSocket.kCommandGetInfo) == 0) {
                    String ssidString = map.get(BWSocket.kKeySSID);
                    if (ssidString != null) {
                        currentSSIDEditText.setText(ssidString);
                        got = true;
                    }
                }
                if (!got) {
                    Toast.makeText(RenameSSIDActivity.this, alertGetInfoFailText, Toast.LENGTH_SHORT).show();
                }
                break;
            case WIRELESS_ACTION_SET_SSID:
                action = WirelessAction.WIRELESS_ACTION_PROCESSING;
                boolean set = false;
                if (methodString != null && methodString.compareTo(BWSocket.kCommandSetSSID) == 0) {
                    String statusCodeString = map.get(BWSocket.kKeyStatusCode);
                    String ssidString = map.get(BWSocket.kKeySSID);
                    if (statusCodeString != null && statusCodeString.compareTo(BWSocket.kStatusCodeOK) == 0) {
                        if (ssidString != null) {
                            currentSSIDEditText.setText(ssidString);

                            set = true;

                            // 相同名称则禁用保存
                            if (newEditSSIDText.getText().toString().compareTo(currentSSIDEditText.getText().toString()) == 0 ) {
                                saveButton.setEnabled(false);
                            }

                            /* ---- 因为现在硬件那边Reset方案有困难，所以先使用手动Reset ---- */
                            AlertDialog.Builder builder = new AlertDialog.Builder(RenameSSIDActivity.this);
                            builder.setMessage(mbNoticeMessage);
                            builder.setTitle(mbNoticeTitle);
                            builder.setPositiveButton(mbNoticeConfirmTitle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // 如果有符合的WiFi设置界面，则弹出提示
                                    String title = null;
                                    String message = null;
                                    String positiveTitle = null;
                                    String negativeTitle = null;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(RenameSSIDActivity.this);
                                    DialogInterface.OnClickListener onPositiveClickListener = null;

                                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        title = mbNotice2Title;
                                        message = mbNotice2Message;
                                        positiveTitle = mbNotice2PosTitle;
                                        negativeTitle = mbNotice2NegTitle;

                                        onPositiveClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Open WiFi setting page
                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                                if (intent.resolveActivity(getPackageManager()) != null) {
                                                    startActivity(intent);
                                                }
                                            }
                                        };
                                        builder.setNegativeButton(negativeTitle, null);
                                    } else {
                                        title = mbNotice3Title;
                                        message = mbNotice3Message;
                                        positiveTitle = mbNotice3ConfirmTitle;

                                        onPositiveClickListener = null;
                                    }
                                    builder.setMessage(message);
                                    builder.setTitle(title);
                                    builder.setPositiveButton(positiveTitle, onPositiveClickListener);
                                    builder.create().show();
                                }
                            });
                            builder.create().show();

//                            hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//                                    .setLabel(getResources().getString(R.string.rename_hud_resetting_board))
//                                    .show();
//
//                            // Reset board after 200ms
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    action = WirelessAction.WIRELESS_ACTION_RESET_NET;
//                                    asyncSocket.resetNet();
//                                }
//                            }, 200);
                        }
                    }
                }
                if (!set) {
                    Toast.makeText(RenameSSIDActivity.this, alertSetSsidFailText, Toast.LENGTH_SHORT).show();
                }
                break;
            case WIRELESS_ACTION_RESET_NET:
                action = WirelessAction.WIRELESS_ACTION_PROCESSING;
                if (methodString != null && methodString.compareTo(BWSocket.kCommandResetNet) == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RenameSSIDActivity.this);
                    builder.setMessage(mbResetMessage);
                    builder.setTitle(mbResetTitle);
                    builder.setPositiveButton(mbResetPosTitle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Open WiFi setting page
                            Intent intent = new Intent(Intent.ACTION_MAIN, null);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                            intent.setComponent(cn);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(mbResetNegTitle, null);
                    builder.create().show();
                } else {
                    Toast.makeText(RenameSSIDActivity.this, alertResetFailText, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
