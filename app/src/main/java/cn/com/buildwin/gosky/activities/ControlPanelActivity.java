package cn.com.buildwin.gosky.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.usb.UsbDevice;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import buildwin.common.Utilities;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.buildwin.gosky.application.PlayerSelect;
import cn.com.buildwin.gosky.widget.audiorecognizer.VoiceRecognizer;
import cn.com.buildwin.gosky.widget.flycontroller.FlyController;
import cn.com.buildwin.gosky.widget.flycontroller.FlyControllerDelegate;
import cn.com.buildwin.gosky.application.Constants;
import cn.com.buildwin.gosky.R;
import cn.com.buildwin.gosky.widget.freespacemonitor.FreeSpaceMonitor;
import cn.com.buildwin.gosky.widget.rudderview.RudderView;
import cn.com.buildwin.gosky.application.Settings;
import cn.com.buildwin.gosky.widget.trackview.TrackView;
import tv.danmaku.ijk.media.widget.IjkVideoView;

import static tv.danmaku.ijk.media.widget.IRenderView.AR_MATCH_PARENT;
import static tv.danmaku.ijk.media.widget.IjkVideoView.RENDER_TEXTURE_VIEW;
import static tv.danmaku.ijk.media.widget.IjkVideoView.RTP_JPEG_PARSE_PACKET_METHOD_FILL;

/*
    Chronometer控件在某些系统上存在bug,
    处于同一层的控件在Invisible后,Chronometer也设为Invisible,
    则其他控件在Chronometer以下的部分就会显示不出来,
    所以代码中才使用Chronometer的GONE和INVISIBLE,还有setText。
 */

public class ControlPanelActivity extends AppCompatActivity implements FlyControllerDelegate , CameraDialog.CameraDialogParent, CameraViewInterface.Callback{

    private static final String TAG = "ControlPanelActivity";

    // 上排按键
    @BindView(R.id.control_panel_top_menubar)               ViewGroup mTopMenuBar;
    @BindView(R.id.control_panel_back_button)               ImageButton mBackButton;
    @BindView(R.id.control_panel_take_photo_button)         ImageButton mTakePhotoButton;
    @BindView(R.id.control_panel_record_video_button)       ImageButton mRecordVideoButton;
    @BindView(R.id.control_panel_review_button)             ImageButton mReviewButton;
    @BindView(R.id.control_panel_limit_speed_button)        ImageButton mLimitSpeedButton;
    @BindView(R.id.control_panel_limit_hight_button)        ImageButton mLimitHighButton;
    @BindView(R.id.control_panel_gravity_control_button)    ImageButton mGravityControlButton;
    @BindView(R.id.control_panel_switch_button)             ImageButton mSwitchButton;
    @BindView(R.id.control_panel_setting_button)            ImageButton mSettingsButton;
    // 右侧按键
    @BindView(R.id.control_panel_right_menubar)             ViewGroup mRightMenuBar;
    @BindView(R.id.control_panel_rotate_screen_button)      ImageButton mRotateScreenButton;
    @BindView(R.id.control_panel_split_screen_button)       ImageButton mSplitScreenButton;
    @BindView(R.id.control_panel_headless_button)           ImageButton mHeadlessButton;
    @BindView(R.id.control_panel_gyro_calibrate_button)     ImageButton mGyroCalibrateButton;
    @BindView(R.id.control_panel_light_button)              ImageButton mLightButton;
    // 其余按键
    @BindView(R.id.control_panel_fly_up_button)             ImageButton mFlyupButton;
    @BindView(R.id.control_panel_fly_down_button)           ImageButton mFlydownButton;
    @BindView(R.id.control_panel_one_key_stop_button)       ImageButton mOneKeyStopButton;
//    @BindView(R.id.control_panel_rotate_button)             ImageButton mRotateButton;
//    @BindView(R.id.control_panel_fixed_direction_rotate_button) ImageButton mFixedDirectionRotateButton;
//    @BindView(R.id.control_panel_return_button)             ImageButton mReturnButton;
    @BindView(R.id.control_panel_roll_button)               ImageButton mRollButton;
    @BindView(R.id.control_panel_track_button)              ImageButton mTrackButton;
    @BindView(R.id.control_panel_voice_button)              ImageButton mVoiceButton;

    // 预览
    private static final int VIDEO_VIEW_RENDER = RENDER_TEXTURE_VIEW;
    private static final int VIDEO_VIEW_ASPECT = AR_MATCH_PARENT;
    private static final int RTP_JPEG_PARSE_PACKET_METHOD = RTP_JPEG_PARSE_PACKET_METHOD_FILL;
    private static final int RECONNECT_INTERVAL = 500;

    private String mVideoPath;
    @BindView(R.id.video_view)          IjkVideoView mVideoView;
    @BindView(R.id.hud_view)            TableLayout mHudView;

    private PlayerSelect playerSelect;

    // 控制台界面
    @BindView(R.id.control_panel_rudderViewContainer)   ViewGroup mRudderViewContainer;
    @BindView(R.id.control_panel_left_rudderView)       RudderView mLeftRudderView;
    @BindView(R.id.control_panel_right_rudderView)      RudderView mRightRudderView;
    private RudderView mPowerRudder;
    private RudderView mRangerRudder;
    @BindView(R.id.control_panel_backgroundView)        ImageView mBackgroundView;
    @BindView(R.id.control_panel_progressBar)           ProgressBar mProgressBar;
    @BindView(R.id.control_panel_chronometer)           Chronometer mChronometer;
    // 轨迹飞行
    @BindView(R.id.control_panel_left_trackView)        TrackView mLeftTrackView;
    @BindView(R.id.control_panel_right_trackView)       TrackView mRightTrackView;
    private TrackView mTrackView;
    // 声控
    @BindView(R.id.control_panel_voice_guide_textView)  TextView mVoiceGuideTextView;
    @BindView(R.id.zoom)  TextView TVzoom;
    private Timer mVoiceControlTimer;


    // 设备占用
    @BindString(R.string.control_panel_device_in_use)
    String mDeviceInUseMessage;

    // Permission Message
    @BindString(R.string.permission_denied_title)
    String permissionDeniedTitle;
    @BindString(R.string.permission_denied_go_to_settings_record_audio)
    String permissionDeniedMsgRecordAudio;
    @BindString(R.string.permission_denied_go_to_settings_write_ext_storage)
    String permissionDeniedMsgAccessStorage;

    // USB camera
    @BindView(R.id.camera_view)
    public View mTextureView;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private boolean isRequest;
    private boolean isPreview;




    // 自动保存
    private boolean autosave;

    // 状态
    private boolean recording = false;
    private boolean mBackPressed;

    // 控制标志
    private boolean rightHandMode = false;
    private boolean enableControl = false;
    private boolean enableGravityControl = false;
    private int limitSpeed = 0;     // 0=30, 1=60, 2=100
    private boolean voiceMode = false;

    VoiceRecognizer mVoiceRecognizer;

    private CountDownTimer gyroCalibrateTimer;
    private CountDownTimer flyupTimer;
    private CountDownTimer flydownTimer;
    private CountDownTimer emergencyDownTimer;
    private CountDownTimer rollCountDownTimer;  // 用于360度翻转倒计时

    // 飞控(本来想用byte的,不过还是int用起来方便)
    private FlyController mFlyController;

    // 剩余空间监控
    private FreeSpaceMonitor mFreeSpaceMonitor;

    // 其他
    private CountDownTimer hideButtonsTimer;    // 隐藏按键的倒计时(3D View)
    private boolean isButtonsVisible = true;    // 3D View中Buttons是否可见
    private Handler updateUiHanlder;            // 用于非主线程更新UI
    private SoundPool mSoundPool;

    // 设备已经连接提示
    private Toast mInfoToast;

    // Debug
    // 打开右侧设置按钮，按住陀螺仪校准按钮，再长按打开右侧的设置按钮，即可打开帧数等信息。关闭方法重复操作一遍。
    private boolean touchDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_control_panel);
        ButterKnife.bind(this);

        autosave = Settings.getInstance(this).getParameterForAutosave();

        // 获取Right hand mode设置
        rightHandMode = Settings.getInstance(this).getParameterForRightHandMode();

        /**
         * Rudder View
         * 控制杆和微调
         */
        // 根据左右手模式设置不同功能对象
        if (rightHandMode) {
            mPowerRudder = mRightRudderView;
            mRangerRudder = mLeftRudderView;
        } else {
            mPowerRudder = mLeftRudderView;
            mRangerRudder = mRightRudderView;
        }
        // 设置Power Rudder
        mPowerRudder.setRudderStyle(RudderView.RudderStyle.RudderStylePower);
        mPowerRudder.setOnValueChangedListener(new RudderView.OnValueChangedListener() {
            @Override
            public void onBasePointMoved(PointF point) {
                // 使用Power Rudder的摇杆值
                float x = (point.x + 1.0f) / 2.0f;
                float y = (point.y + 1.0f) / 2.0f;
                int controlByteRUDD = floatToInt(x, 0xFF);
                int controlByteTHR = floatToInt(y, 0xFF);
                mFlyController.setControlByteRUDD(controlByteRUDD);
                mFlyController.setControlByteTHR(controlByteTHR);

//                Log.i("Power onBasePoint", controlByteRUDD + " " + controlByteTHR);

                // 方向键移动超过一半时,则一键旋转清零
                if (controlByteRUDD < 0x40 || controlByteRUDD > 0xC0) {
                    mFlyController.setRotateMode(false);
                    // 恢复旋转状态,发送消息到UI线程
                    Message message = new Message();
                    message.what = 0;
                    updateUiHanlder.sendMessage(message);
                }

                // 油门控制时，紧急停止置0
                mFlyController.setEmergencyDownMode(false);
                mOneKeyStopButton.setImageResource(R.mipmap.con_emergency_stop);
            }

            @Override
            public void onHTrimValueChanged(float value) {
                // 使用Power Rudder的横向微调值
//                float v = (value + 1.0f) / 2.0f;
                float v = value;
                mFlyController.setTrimByteRUDD(floatToInt(v, RudderView.H_SCALE_NUM));

//                Log.i("Power onHTrimValue", "" + trimByteRUDD);
            }

            @Override
            public void onVTrimValueChanged(float value) {
                // Power Rudder不使用纵向微调
            }
        });
        // 设置Ranger Rudder
        mRangerRudder.setRudderStyle(RudderView.RudderStyle.RudderStyleRanger);
        mRangerRudder.setRightHandMode(rightHandMode);
        mRangerRudder.setOnValueChangedListener(new RudderView.OnValueChangedListener() {
            @Override
            public void onBasePointMoved(PointF point) {
                // 使用Ranger Rudder的摇杆值
                float x = (point.x + 1.0f) / 2.0f;
                float y = (point.y + 1.0f) / 2.0f;
//                controlByteAIL = floatToInt(x);
//                controlByteELE = floatToInt(y);

                int intX = floatToInt(x, 0xFF);
                int intY = floatToInt(y, 0xFF);

                // 如果360度翻转开关打开
                if (mFlyController.isRollMode()) {
                    // 如果还没有触发360度翻转
                    if (!mFlyController.isTriggeredRoll()) {
                        if (intY > 0xC0) {
                            mFlyController.setTriggeredRoll(true);
                            mFlyController.setControlByteELE(0xFF);
                            mFlyController.setControlByteAIL(0x80); // 需要平衡位置？
                        } else if (intY < 0x40) {
                            mFlyController.setTriggeredRoll(true);
                            mFlyController.setControlByteELE(0x00);
                            mFlyController.setControlByteAIL(0x80); // 需要平衡位置？
                        } else {
                            mFlyController.setControlByteELE(intY);
                        }

                        if (!mFlyController.isTriggeredRoll()) {
                            if (intX > 0xC0) {
                                mFlyController.setTriggeredRoll(true);
                                mFlyController.setControlByteAIL(0xFF);
                                mFlyController.setControlByteELE(0x80); // 需要平衡位置？
                            } else if (intX < 0x40) {
                                mFlyController.setTriggeredRoll(true);
                                mFlyController.setControlByteAIL(0x00);
                                mFlyController.setControlByteELE(0x80); // 需要平衡位置？
                            } else {
                                mFlyController.setControlByteAIL(intX);
                            }
                        }

                        // 设置300ms定时器
                        if (mFlyController.isTriggeredRoll()) {
                            // 翻滚模式持续300ms
                            rollCountDownTimer = new CountDownTimer(300, 300) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    mFlyController.setTriggeredRoll(false);
                                    mFlyController.setRollMode(false);
                                    // 恢复翻转状态,发送消息到UI线程
                                    Message message = new Message();
                                    message.what = 1;
                                    updateUiHanlder.sendMessage(message);

                                    mFlyController.setControlByteELE(0x80);
                                    mFlyController.setControlByteAIL(0x80);

                                    rollCountDownTimer.cancel();
                                    rollCountDownTimer = null;
                                }
                            }.start();
                        }
                    }
                }
                // 如果360度翻转开关关闭
                else {
                    mFlyController.setControlByteELE(intY);
                    mFlyController.setControlByteAIL(intX);
                }

//                Log.i("Ranger onBasePoint", controlByteAIL + " " + controlByteELE);
            }

            @Override
            public void onHTrimValueChanged(float value) {
                // 使用Ranger Rudder的横向微调值
//                float v = (value + 1.0f) / 2.0f;
                float v = value;
                mFlyController.setTrimByteAIL(floatToInt(v, RudderView.H_SCALE_NUM));

//                Log.i("Ranger onHTrimValue", "" + trimByteAIL);
            }

            @Override
            public void onVTrimValueChanged(float value) {
                // 使用Ranger Rudder的纵向微调值
//                float v = (value + 1.0f) / 2.0f;
                float v = value;
                mFlyController.setTrimByteELE(floatToInt(v, RudderView.V_SCALE_NUM));

//                Log.i("Ranger onVTrimValue", "" + trimByteELE);
            }
        });
        mRangerRudder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceControlTimer != null) {
                    mVoiceControlTimer.purge();
                    mVoiceControlTimer.cancel();
                    mVoiceControlTimer = null;
                }
            }
        });

        /**
         * 轨迹飞行
         * !!! 因为涉及布局问题，所以需要放在RudderView后初始化 !!!
         */
        // 根据左右手模式，选择使用左右视图
        if (rightHandMode) {
            mRightTrackView.setVisibility(View.GONE);
            mTrackView = mLeftTrackView;
        } else {
            mLeftTrackView.setVisibility(View.GONE);
            mTrackView = mRightTrackView;
        }
        // 为了获取到布局后实际大小
        mTrackView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                if (Build.VERSION.SDK_INT < 16) {
                    mTrackView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTrackView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                // Here you can get the size :)
                setLimitSpeedValue(limitSpeed); // 设置限速值
            }
        });
        // 事件监听
        mTrackView.setOnTrackViewEventListener(new TrackView.OnTrackViewEventListener() {
            @Override
            public void beginOutput() {
                Log.i(TAG, "beginOutput");

                mFlyController.setControlByteELE(0x80);
                mFlyController.setControlByteAIL(0x80);
            }

            @Override
            public void outputPoint(PointF point) {
                float x = (point.x + 1.0f) / 2.0f;
                float y = (point.y + 1.0f) / 2.0f;
                int intX = floatToInt(x, 0xFF);
                int intY = floatToInt(y, 0xFF);

                mFlyController.setControlByteELE(intY);
                mFlyController.setControlByteAIL(intX);

//                Log.i(TAG, ">>> control point: x = " + controlByteAIL + ", y = " + controlByteELE);
            }

            @Override
            public void finishOutput() {
                Log.i(TAG, "finishOutput");

                mFlyController.setControlByteELE(0x80);
                mFlyController.setControlByteAIL(0x80);
            }
        });

        // step.1 initialize UVCCameraHelper
        mTextureView.bringToFront();
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
     //   mCameraHelper.setDefaultPreviewSize(1280,720);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        //以上2个参数一定要在 initUSB之前。 但是总是第二次进去报错
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);


        // handle arguments
        mVideoPath = Constants.RTSP_ADDRESS;

        playerSelect = new PlayerSelect(PlayerSelect.BUILDWIN);

        mVideoView.setRtpJpegParsePacketMethod(RTP_JPEG_PARSE_PACKET_METHOD);
        mVideoView.setRender(VIDEO_VIEW_RENDER);
        mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);

        mVideoView.setOnPreparedListener(new IjkVideoView.IVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(IjkVideoView videoView) {
                cancelInfoToast();
                onStartPlayback();
            }
        });
        mVideoView.setOnErrorListener(new IjkVideoView.IVideoView.OnErrorListener() {
            @Override
            public boolean onError(IjkVideoView videoView, int what, int extra) {
                stopAndRestartPlayback();
                return true;
            }
        });
        mVideoView.setOnReceivedRtcpSrDataListener(new IjkVideoView.IVideoView.OnReceivedRtcpSrDataListener() {
            @Override
            public void onReceivedRtcpSrData(IjkVideoView videoView, byte[] data) {
//                Log.d(TAG, new String(data) + Arrays.toString(data));

                if (checkIfIsValidHwActionCommand(data)) {
                    byte commandClass = data[4];    // index
                    byte command = data[6];         // data

                    doHwAction(commandClass, command);
                }
            }
        });
        mVideoView.setOnReceivedDataListener(new IjkVideoView.IVideoView.OnReceivedDataListener() {
            @Override
            public void onReceivedData(IjkVideoView videoView, byte[] data) {
                // work with firmware api -> wifi_data_send
            }
        });
        mVideoView.setOnTookPictureListener(new IjkVideoView.IVideoView.OnTookPictureListener() {
            @Override
            public void onTookPicture(IjkVideoView videoView, int resultCode, String fileName) {
                String toastText = getResources().getString(R.string.control_panel_alert_save_photo_fail);
                if (resultCode == 1) {
                    // 播放咔嚓声
                    mSoundPool.play(1, 1, 1, 0, 0, 1);
                }
                else if (resultCode == 0 && fileName != null) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        mediaScan(file);
                        // Show toast
                        toastText = getResources().getString(R.string.control_panel_alert_save_photo_success) + fileName;
                    }
                    Toast.makeText(ControlPanelActivity.this, toastText, Toast.LENGTH_SHORT).show();
                }
                else if (resultCode < 0) {
                    Toast.makeText(ControlPanelActivity.this, toastText, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mVideoView.setOnRecordVideoListener(new IjkVideoView.IVideoView.OnRecordVideoListener() {
            @Override
            public void onRecordVideo(IjkVideoView videoView, final int resultCode, final String fileName) {
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String noteText = null;
                        if (resultCode < 0) {
                            // 停止监控剩余空间
                            if (mFreeSpaceMonitor != null)
                                mFreeSpaceMonitor.stop();

                            recording = false;
                            noteText = getResources().getString(R.string.control_panel_alert_write_video_file_error);
                            Toast.makeText(
                                    ControlPanelActivity.this,
                                    noteText,
                                    Toast.LENGTH_SHORT
                            ).show();
                            mRecordVideoButton.setImageResource(R.mipmap.con_video);
                            // 隐藏录像计时器
                            showChronometer(false);
                        }
                        else if (resultCode == 0) {
                            recording = true;
                            // 开启录像计时
                            showChronometer(true);
                            mRecordVideoButton.setImageResource(R.mipmap.con_video_h);
                            // 开始监控剩余空间
                            mFreeSpaceMonitor.setListener(new FreeSpaceMonitor.FreeSpaceCheckerListener() {
                                @Override
                                public void onExceed() {
                                    // 如果剩余空间低于阈值，停止录像
                                    if (recording)
                                        mVideoView.stopRecordVideo();
                                }
                            });
                            mFreeSpaceMonitor.start();
                        }
                        else {
                            // 停止监控剩余空间
                            if (mFreeSpaceMonitor != null)
                                mFreeSpaceMonitor.stop();

                            // Scan file to media library
                            File file = new File(fileName);
                            mediaScan(file);

                            noteText = getResources().getString(R.string.control_panel_alert_record_video_success);
                            Toast.makeText(
                                    ControlPanelActivity.this,
                                    noteText + fileName,
                                    Toast.LENGTH_SHORT
                            ).show();
                            mRecordVideoButton.setImageResource(R.mipmap.con_video);
                            // 隐藏录像计时器
                            showChronometer(false);

                            // set flag
                            recording = false;
                        }
                    }
                });
            }

        });
        mVideoView.setOnDeviceConnectedListener(new IjkVideoView.IVideoView.OnDeviceConnectedListener() {
            @Override
            public void onDeviceConnected(IjkVideoView videoView) {
                if (!isFinishing()) {
                    showInfoToast(mDeviceInUseMessage);
                }
            }
        });
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mVideoView.isVrMode()) {
                        if (isButtonsVisible) {
                            setButtonsVisible(false);
                            if (hideButtonsTimer != null) {
                                hideButtonsTimer.cancel();
                                hideButtonsTimer = null;
                            }
                        } else {
                            setButtonsVisible(true);
                            setHideButtonsTimer();
                        }
                    }
                }
                return false;
            }
        });
        mVideoView.setOnCompletionListener(new IjkVideoView.IVideoView.OnCompletionListener() {
            @Override
            public void onCompletion(IjkVideoView videoView) {
                mVideoView.stopPlayback();
                mVideoView.release(true);
                mVideoView.stopBackgroundPlay();
            }
        });
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }

        // 飞控
        mFlyController = new FlyController();
        mFlyController.setDelegate(this);


        /**
         * Back Button
         * 返回按钮
         */
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackPressed = true;
                finish();
                // Activity slide from left
                overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
            }
        });

        /**
         * Take Photo Button
         * 截图按钮
         */
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(ControlPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    takePhoto(1);
                } else {
                    showOpenSettingsAlertDialog(permissionDeniedTitle, permissionDeniedMsgAccessStorage);
                }
            }
        });

        /**
         * Record Video Button
         * 录像按钮
         */
        mRecordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(ControlPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    recordVideo();
                } else {
                    showOpenSettingsAlertDialog(permissionDeniedTitle, permissionDeniedMsgAccessStorage);
                }
            }
        });

        /**
         * Replay Button
         * 查看按钮
         */
        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start replaying
                Intent i = new Intent(ControlPanelActivity.this, ReviewActivity.class);
                startActivity(i);
                // Activity slide from left
                overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
            }
        });

        /**
         * Limit Speed Button
         * 限速按钮
         */
        mLimitSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add limit speed support
                stepSpeedLimit();
            }
        });

        /**
         * Limit High Button
         * 限高按钮
         */
        boolean bAltitudeHold;
        if (autosave)
            bAltitudeHold = Settings.getInstance(this).getParameterForAltitudeHold();
        else
            bAltitudeHold = false;
        if (bAltitudeHold) {
            mFlyController.setEnableLimitHigh(true);
            mLimitHighButton.setImageResource(R.mipmap.con_altitude_hold_h);
        } else {
            mFlyController.setEnableLimitHigh(false);
            mLimitHighButton.setImageResource(R.mipmap.con_altitude_hold);
        }
        mLimitHighButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableLimitHigh(!mFlyController.isEnableLimitHigh());
            }
        });

        /**
         * Gravity Control Button
         * 重力控制按钮
         */
        mGravityControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (enableControl) {
                    if (mRangerRudder.isSupportGravityControl()) {
                        setEnableGravityControl(!enableGravityControl);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.control_panel_alert_not_support_gyro,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        /**
         * Switch Button
         * 开关按钮
         */
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Turn on or off control
                setEnableControl(!enableControl);
            }
        });

        /**
         * Settings Button
         * 右边设置按钮
         */
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示隐藏右侧按键
                int buttonsVisibility = mGyroCalibrateButton.getVisibility();
                if (buttonsVisibility == View.VISIBLE) {
                    mSettingsButton.setImageResource(R.mipmap.con_extra_settings);
                    mRotateScreenButton.setVisibility(View.INVISIBLE);
                    mSplitScreenButton.setVisibility(View.INVISIBLE);
                    mHeadlessButton.setVisibility(View.INVISIBLE);
                    mGyroCalibrateButton.setVisibility(View.INVISIBLE);
                    mLightButton.setVisibility(View.INVISIBLE);
                } else {
                    mSettingsButton.setImageResource(R.mipmap.con_extra_settings_h);
                    mRotateScreenButton.setVisibility(View.VISIBLE);
                    mSplitScreenButton.setVisibility(View.VISIBLE);
                    mHeadlessButton.setVisibility(View.VISIBLE);
                    mGyroCalibrateButton.setVisibility(View.VISIBLE);
                    mLightButton.setVisibility(View.VISIBLE);
                }
            }
        });

        /**
         * Rotate Screen Button
         * 旋转屏幕按钮
         */
        mRotateScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rotate the screen
                mVideoView.setRotation180(!mVideoView.isRotation180());

                mSettingsButton.setImageResource(R.mipmap.con_extra_settings);
                // 显示隐藏右侧按键
                mRotateScreenButton.setVisibility(View.INVISIBLE);
                mLightButton.setVisibility(View.INVISIBLE);
                mSplitScreenButton.setVisibility(View.INVISIBLE);
                mHeadlessButton.setVisibility(View.INVISIBLE);
                mGyroCalibrateButton.setVisibility(View.INVISIBLE);
            }
        });

        /**
         * Split Screen Button
         * 3D View按钮(Split Screen)
         */
        mSplitScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 进入或退出3D模式
                mVideoView.setVrMode(!mVideoView.isVrMode());

                // 根据不同模式,设置界面控件的显示
                if (mVideoView.isVrMode()) {
                    setEnableControl(false);
                    setHideButtonsTimer();
                } else {
                    setButtonsVisible(true);
                    if (hideButtonsTimer != null) {
                        hideButtonsTimer.cancel();
                        hideButtonsTimer = null;
                    }
                }

                // Set Image
                mSplitScreenButton.setImageResource(mVideoView.isVrMode() ? R.drawable.button_flat : R.drawable.button_3d);
            }
        });

        /**
         * Headless Button
         * 无头模式
         */
        mHeadlessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启用无头或者有头模式
                mFlyController.setHeadlessMode(!mFlyController.isHeadlessMode());

                // Set Image
                int resId;
                if (mFlyController.isHeadlessMode()) {
                    resId = R.mipmap.con_headless_h;
                } else {
                    resId = R.mipmap.con_headless;
                }
                mHeadlessButton.setImageResource(resId);
            }
        });

        /**
         * Gyro Calibrate Button
         * Gyro传感器校准
         */
        mGyroCalibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 校准Gyro传感器
                if (!mFlyController.isGyroCalibrateMode()) {
                    mFlyController.setGyroCalibrateMode(true);
                    if (gyroCalibrateTimer == null) {
                        gyroCalibrateTimer = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                mFlyController.setGyroCalibrateMode(false);
                                gyroCalibrateTimer.cancel();
                                gyroCalibrateTimer = null;
                            }
                        }.start();
                    }
                }
            }
        });

        /**
         * 灯光控制
         */
        mLightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlyController.setLightOn(!mFlyController.isLightOn());
                // Set Image
                int resId = mFlyController.isLightOn() ? R.mipmap.con_light_h : R.mipmap.con_light;
                mLightButton.setImageResource(resId);
            }
        });

        /**
         * 一键旋转
         */
//        mRotateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 设置一键旋转
//                rotateMode = !rotateMode;
//                // Set Image
//                int resId;
//                if (rotateMode) {
//                    resId = R.mipmap.con_rotate_h;
//                } else {
//                    resId = R.mipmap.con_rotate;
//                }
//                mRotateButton.setImageResource(resId);
//            }
//        });

//        /**
//         * 一键固定方向旋转
//         */
//        mFixedDirectionRotateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 设置一键固定方向旋转
//                fixedDirectionRollMode = !fixedDirectionRollMode;
//                // Set Image
//                int resId;
//                if (fixedDirectionRollMode) {
//                    resId = R.mipmap.con_rotate_direction_h;
//                } else {
//                    resId = R.mipmap.con_rotate_direction;
//                }
//                mFixedDirectionRotateButton.setImageResource(resId);
//            }
//        });

        /**
         * Flyup Button
         * 一键起飞
         */
        mFlyupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设置一键起飞
                if (!mFlyController.isFlyupMode()) {
                    mFlyController.setFlyupMode(true);
                    if (flyupTimer == null) {
                        flyupTimer = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                mFlyController.setFlyupMode(false);
                                flyupTimer.cancel();
                                flyupTimer = null;
                            }
                        }.start();
                    }
                }
            }
        });

        /**
         * Flydown Button
         * 一键降落
         */
        mFlydownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设置一键降落
                if (!mFlyController.isFlydownMode()) {
                    mFlyController.setFlydownMode(true);
                    if (flydownTimer == null) {
                        flydownTimer = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                mFlyController.setFlydownMode(false);
                                flydownTimer.cancel();
                                flydownTimer = null;
                            }
                        }.start();
                    }
                }
                // 按一键下降后，紧急停止置0
                mFlyController.setEmergencyDownMode(false);
                mOneKeyStopButton.setImageResource(R.mipmap.con_emergency_stop);
            }
        });

        /**
         * One Key Stop Button
         * 紧急降落
         */
        mOneKeyStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设置紧急降落
                if (!mFlyController.isEmergencyDownMode()) {
                    float fTHR = (float)mFlyController.getControlByteTHR() / 255.0f;
                    if (fTHR > 0.4) {
                        mFlyController.setEmergencyDownMode(true);
                        mOneKeyStopButton.setImageResource(R.mipmap.con_emergency_stop_h);
                        // 设置关闭功能定时器
                        if (emergencyDownTimer == null) {
                            emergencyDownTimer = new CountDownTimer(1000, 1000) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    mFlyController.setEmergencyDownMode(false);
                                    emergencyDownTimer.cancel();
                                    emergencyDownTimer = null;
                                    mOneKeyStopButton.setImageResource(R.mipmap.con_emergency_stop);
                                }
                            }.start();
                        }
                    }
                } else {
                    mFlyController.setEmergencyDownMode(false);
                    mOneKeyStopButton.setImageResource(R.mipmap.con_emergency_stop);
                    // 关闭定时器
                    if (emergencyDownTimer != null) {
                        emergencyDownTimer.cancel();
                        emergencyDownTimer = null;
                    }
                }
            }
        });

        /**
         * 一键返回
         */
//        mReturnButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!(rollMode || trackMode)) {
//                    // 设置一键返回
//                    returnMode = !returnMode;
//                    // Set Image
//                    int resId;
//                    if (returnMode) {
//                        resId = R.mipmap.con_go_home_h;
//                    } else {
//                        resId = R.mipmap.con_go_home;
//                    }
//                    mReturnButton.setImageResource(resId);
//                }
//            }
//        });

        /**
         * Roll Button
         * 360度翻转
         */
        mRollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(mFlyController.isReturnMode() || mFlyController.isTrackMode())) {
                    // 设置360度翻转
                    if (!mFlyController.isTriggeredRoll()) {
                        mFlyController.setRollMode(!mFlyController.isRollMode());

                        // Set Image
                        int resId;
                        if (mFlyController.isRollMode()) {
                            resId = R.mipmap.con_roll_h;
                        } else {
                            resId = R.mipmap.con_roll;
                        }
                        mRollButton.setImageResource(resId);
                    }
                }
            }
        });

        /**
         * Track Button
         * 轨迹飞行
         */
        mTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(mFlyController.isReturnMode() || mFlyController.isRollMode())) {
                    // 设置轨迹飞行
                    mFlyController.setTrackMode(!mFlyController.isTrackMode());
                    // set image
                    mTrackButton.setImageResource(mFlyController.isTrackMode() ? R.mipmap.con_track_h : R.mipmap.con_track);
                    // set rudders' visibility
                    if (mFlyController.isTrackMode()) {
                        mRangerRudder.setVisibility(View.INVISIBLE);
                        mTrackView.setVisibility(View.VISIBLE);
                        // 关掉重力感应控制
                        setEnableGravityControl(false);
                        // 退出语音控制
                        voiceMode = false;
                        mVoiceButton.setImageResource(R.mipmap.con_voice);
                        mVoiceRecognizer.stopListening();
                    } else {
                        mTrackView.setVisibility(View.INVISIBLE);
                        mRangerRudder.setVisibility(View.VISIBLE);
                        // 结束当前轨迹
                        mTrackView.reset();
                    }
                }
            }
        });

        /**
         * 语音控制
         */
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(ControlPanelActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    if (voiceMode) {
                        // 语音控制停止监听
                        mVoiceRecognizer.stopListening();
                        voiceMode = false;
                    } else {
                        voiceMode = true;
                        // 语音控制开始监听
                        mVoiceRecognizer.startListening();
                        // 关掉重力感应控制
                        setEnableGravityControl(false);
                        // 结束当前轨迹
                        if (mFlyController.isTrackMode()) {
                            mFlyController.setTrackMode(false);
                            mTrackButton.setImageResource(R.mipmap.con_track);

                            mTrackView.setVisibility(View.INVISIBLE);
                            mRangerRudder.setVisibility(View.VISIBLE);
                            // 结束当前轨迹
                            mTrackView.reset();
                        }
                    }
                    mVoiceButton.setImageResource(voiceMode ? R.mipmap.con_voice_h : R.mipmap.con_voice);
                } else {
                    showOpenSettingsAlertDialog(permissionDeniedTitle, permissionDeniedMsgRecordAudio);
                }
            }
        });

        // 语音指导文本
        resetDefaultVoiceGuide();
        mVoiceGuideTextView.setVisibility(View.INVISIBLE);

        mVoiceRecognizer = new VoiceRecognizer(this);
        mVoiceRecognizer.setVoiceRecognitionListener(new VoiceRecognizer.VoiceRecognitionListener() {
            @Override
            public void onListen() {
                Log.d(TAG, "onListen");
                mVoiceGuideTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause");
                mVoiceGuideTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResult(VoiceRecognizer.Action action, String text) {
                if (voiceMode) {
                    Log.d(TAG, "onResult: " + action);

                    mVoiceGuideTextView.setText(text);

                    TimerTask voiceControlTask = null;

                    switch (action) {
                        case FORWARD:
                            mRangerRudder.moveStickTo(0, 1.0f);
                            voiceControlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRangerRudder.moveStickTo(0, 0);
                                            resetDefaultVoiceGuide();
                                        }
                                    });
                                }
                            };
                            break;
                        case BACKWARD:
                            mRangerRudder.moveStickTo(0, -1.0f);
                            voiceControlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRangerRudder.moveStickTo(0, 0);
                                            resetDefaultVoiceGuide();
                                        }
                                    });
                                }
                            };
                            break;
                        case LEFT:
                            mRangerRudder.moveStickTo(-1.0f, 0);
                            voiceControlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRangerRudder.moveStickTo(0, 0);
                                            resetDefaultVoiceGuide();
                                        }
                                    });
                                }
                            };
                            break;
                        case RIGHT:
                            mRangerRudder.moveStickTo(1.0f, 0);
                            voiceControlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRangerRudder.moveStickTo(0, 0);
                                            resetDefaultVoiceGuide();
                                        }
                                    });
                                }
                            };
                            break;
                        case TAKEOFF:
                            mFlyController.setFlyupMode(true);
                            if (flyupTimer == null) {
                                flyupTimer = new CountDownTimer(1000, 1000) {
                                    @Override
                                    public void onTick(long l) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        mFlyController.setFlyupMode(false);
                                        flyupTimer.cancel();
                                        flyupTimer = null;
                                        resetDefaultVoiceGuide();
                                    }
                                }.start();
                            }
                            break;
                        case LANDING:
                            mFlyController.setFlydownMode(true);
                            if (flydownTimer == null) {
                                flydownTimer = new CountDownTimer(1000, 1000) {
                                    @Override
                                    public void onTick(long l) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        mFlyController.setFlydownMode(false);
                                        flydownTimer.cancel();
                                        flydownTimer = null;
                                        resetDefaultVoiceGuide();
                                    }
                                }.start();
                            }
                            break;
                    }
                    if (voiceControlTask != null) {
                        if (mVoiceControlTimer != null) {
                            mVoiceControlTimer.purge();
                            mVoiceControlTimer.cancel();
                            mVoiceControlTimer = null;
                        }
                        mVoiceControlTimer = new Timer("voice control");
                        mVoiceControlTimer.schedule(voiceControlTask, 3000, 3000);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "onError: " + error);
            }
        });

        /**
         * 处理UI刷新
         */
        updateUiHanlder = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int what = message.what;
                switch (what) {
                    // 更新mRotateButton图像
                    case 0:
                        mFlyController.setRotateMode(false);
//                        mRotateButton.setImageResource(R.mipmap.con_rotate);
                        break;
                    // 更新mRollButton图像
                    case 1:
                        mFlyController.setRollMode(false);
                        mRollButton.setImageResource(R.mipmap.con_roll);
                        break;
                }

                return true;
            }
        });

        // 载入声音资源
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1);

            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_SYSTEM);

            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        }
        mSoundPool.load(this, R.raw.shutter, 1);

        /**
         * 初始化控件显示
         */
        mRudderViewContainer.setVisibility(View.INVISIBLE);
        mChronometer.setVisibility(View.GONE);
        mRotateScreenButton.setVisibility(View.INVISIBLE);
        mSplitScreenButton.setVisibility(View.INVISIBLE);
        mHeadlessButton.setVisibility(View.INVISIBLE);
        mGyroCalibrateButton.setVisibility(View.INVISIBLE);
        mRollButton.setVisibility(View.INVISIBLE);
        mFlyupButton.setVisibility(View.INVISIBLE);
        mFlydownButton.setVisibility(View.INVISIBLE);
        mOneKeyStopButton.setVisibility(View.INVISIBLE);
        mTrackButton.setVisibility(View.INVISIBLE);
        mTrackView.setVisibility(View.INVISIBLE);
        mLightButton.setVisibility(View.INVISIBLE);
        mVoiceButton.setVisibility(View.INVISIBLE);

        /**
         * 初始化控制
         */
        if (autosave)
            mFlyController.setEnableLimitHigh(Settings.getInstance(this).getParameterForAltitudeHold());
        else
            mFlyController.setEnableLimitHigh(false);
        mPowerRudder.setAlititudeHoldMode(mFlyController.isEnableLimitHigh());
        setEnableControl(enableControl);
        setEnableGravityControl(enableGravityControl);

        if (autosave)
            limitSpeed = Settings.getInstance(this).getParameterForSpeedLimit();
        else
            limitSpeed = 0;
        setLimitSpeedValue(limitSpeed); // 设置限速值
        setLimitSpeedIcon(limitSpeed);  // 设置图标

        /**
         * 初始化命令
         */
        mFlyController.setControlByteAIL(0x80); // 副翼
        mFlyController.setControlByteELE(0x80); // 升降舵
        if (mFlyController.isEnableLimitHigh())
            mFlyController.setControlByteTHR(0x80); // 油门
        else
            mFlyController.setControlByteTHR(0);
        mFlyController.setControlByteRUDD(0x80);    // 方向舵
        // 微调
        mFlyController.setTrimByteAIL(floatToInt(mRangerRudder.getHTrimValue(), RudderView.H_SCALE_NUM));
        mFlyController.setTrimByteELE(floatToInt(mRangerRudder.getVTrimValue(), RudderView.V_SCALE_NUM));
        mFlyController.setTrimByteRUDD(floatToInt(mPowerRudder.getHTrimValue(), RudderView.H_SCALE_NUM));

        // for debug, show framerate, etc
        mGyroCalibrateButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    touchDebug = true;
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    touchDebug = false;
                return false;
            }
        });
        mSettingsButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (touchDebug) {
                    if (mHudView.getVisibility() != View.VISIBLE)
                        mHudView.setVisibility(View.VISIBLE);
                    else
                        mHudView.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }

    private Handler Zoomhandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
           // super.handleMessage(msg);
            int what = msg.what;
            TVzoom.bringToFront();
            if(what!=0) {
                TVzoom.setText(String.valueOf(what));
            }
            else  TVzoom.setText("无法获取");


        }
    };


    // USB camara
    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            showShortMsg(device.getDeviceName() + " onAttachDev");
            // request open permission
            if (!isRequest) {

                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                           // mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                           // mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));


                        }
                        Looper.loop();
                    }
                }).start();
             //   UVCCamera.updateCameraParams();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                        message.what = mCameraHelper.getZoomModelValue();
                        Log.i(TAG, "==========================run: %d-----"+mCameraHelper.getZoomModelValue());
                        Zoomhandler.sendMessage(message);}
                    }
                },2220,1000);
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
           // mVideoView.setVisibility(View.VISIBLE);
        }
    };
    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        mBackPressed = true;
        super.onBackPressed();
        finish();
        // Activity slide from left
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }

//        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
        if (!mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        mBackgroundView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVideoView.setRender(VIDEO_VIEW_RENDER);
        mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 关闭屏幕常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 停止录像
        if (recording)
            mVideoView.stopRecordVideo();
        // 停止语音控制
        mVoiceRecognizer.stopListening();
        voiceMode = false;
        mVoiceButton.setImageResource(R.mipmap.con_voice);
        // Turn off control
        setEnableControl(false);
        // Cancel info toast
        cancelInfoToast();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSoundPool.release();
        // Stop sending fly controller data
        mFlyController.sendFlyControllerData(false);
        mVoiceRecognizer.stopListening();
        mVoiceRecognizer.shutdown();

        Settings.release();
    }

    /**
     * 播放开始后执行
     */
    private void onStartPlayback() {
        // 隐藏BackgroundView,ProgressBar
        mBackgroundView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 关闭播放器并重新开始播放
     * 错误发生的时候调用
     */
    private void stopAndRestartPlayback() {
        // 显示BackgroundView,ProgressBar
        mBackgroundView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        mVideoView.post(new Runnable() {
            @Override
            public void run() {
                mVideoView.stopPlayback();
                mVideoView.release(true);
                mVideoView.stopBackgroundPlay();
            }
        });
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setRender(VIDEO_VIEW_RENDER);
                mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);
                mVideoView.setVideoPath(mVideoPath);
                mVideoView.start();
            }
        }, RECONNECT_INTERVAL);
    }

    /**
     * 扫描添加媒体文件到系统媒体库
     * @param file  媒体文件
     */
    private void mediaScan(File file) {
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[] { file.getAbsolutePath() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("MediaScanWork", "file " + path
                                + " was scanned seccessfully: " + uri);
                    }
                });
    }

    private void showInfoToast(String info) {
        if (mInfoToast != null)
            mInfoToast.cancel();
        mInfoToast = Toast.makeText(ControlPanelActivity.this, info, Toast.LENGTH_SHORT);
        mInfoToast.show();
    }

    private void cancelInfoToast() {
        if (mInfoToast != null) {
            mInfoToast.cancel();
            mInfoToast = null;
        }
    }

    private void stepSpeedLimit() {
        limitSpeed = ++limitSpeed % 3;
        setLimitSpeedValue(limitSpeed); // 设置限速值
        setLimitSpeedIcon(limitSpeed);  // 设置图标
    }

    /**
     * 设置限速值
     * @param limitSpeed    限速值
     */
    private void setLimitSpeedValue(int limitSpeed) {
        if (autosave)
            Settings.getInstance(this).saveParameterForSpeedLimit(limitSpeed);

        switch (limitSpeed) {
            case 0:
                mFlyController.setLimitSpeedValue(30);
                break;
            case 1:
                mFlyController.setLimitSpeedValue(60);
                break;
            case 2:
                mFlyController.setLimitSpeedValue(100);
                break;
            default:
                mFlyController.setLimitSpeedValue(30);
        }

        // 轨迹飞行设置速度级
        mTrackView.setSpeedLevel(limitSpeed);
    }

    private void setLimitSpeedIcon(int speedLimit) {
        int resId;
        switch (speedLimit) {
            case 0:
                resId = R.drawable.button_speed_30;
                break;
            case 1:
                resId = R.drawable.button_speed_60;
                break;
            case 2:
                resId = R.drawable.button_speed_100;
                break;
            default:
                resId = R.drawable.button_speed_30;
        }
        mLimitSpeedButton.setImageResource(resId);
    }

    /**
     * 开启/关闭限高开关
     * @param enableLimitHigh   限高开关
     */
    private void setEnableLimitHigh(boolean enableLimitHigh) {
        mFlyController.setEnableLimitHigh(enableLimitHigh);

        if (autosave)
            Settings.getInstance(this).saveParameterForAltitudeHold(enableLimitHigh);
        mPowerRudder.setAlititudeHoldMode(enableLimitHigh);

        // 启用控制时,才可开启定高模式
        if (enableControl) {
            mFlyupButton.setVisibility(View.VISIBLE);
            mFlydownButton.setVisibility(View.VISIBLE);
            mOneKeyStopButton.setVisibility(View.VISIBLE);
        }

        if (!enableLimitHigh) {
            mFlyupButton.setVisibility(View.INVISIBLE);
            mFlydownButton.setVisibility(View.INVISIBLE);
            mOneKeyStopButton.setVisibility(View.INVISIBLE);
        }

        // 设置图像
        int resId;
        if (enableLimitHigh) {
            resId = R.mipmap.con_altitude_hold_h;
        } else {
            resId = R.mipmap.con_altitude_hold;
        }
        mLimitHighButton.setImageResource(resId);
    }

    /**
     * 使能/禁能重力控制
     * @param enableGravityControl  重力开关
     */
    private void setEnableGravityControl(boolean enableGravityControl) {
        if (!enableGravityControl
                || (enableGravityControl && !mFlyController.isTrackMode() && !voiceMode)) {
            this.enableGravityControl = enableGravityControl;
            mRangerRudder.setEnableGravityControl(this.enableGravityControl);

            int resId;
            if (this.enableGravityControl) {
                resId = R.mipmap.con_gravity_control_h;
            } else {
                resId = R.mipmap.con_gravity_control;
            }
            mGravityControlButton.setImageResource(resId);
        }
    }

    /**
     * 开启/关闭控制开关
     * @param enableControl 控制开关
     */
    private void setEnableControl(boolean enableControl) {
        this.enableControl = enableControl;

        // 设置图像和Visibility
        int resId;
        if (this.enableControl) {
            boolean isShownChronometer = mChronometer.isShown();
            if (isShownChronometer)
                mChronometer.setVisibility(View.GONE);

            // UI
            mRudderViewContainer.setVisibility(View.VISIBLE);
//            mRotateButton.setVisibility(View.VISIBLE);
//            mFixedDirectionRotateButton.setVisibility(View.VISIBLE);
//            mReturnButton.setVisibility(View.VISIBLE);
            mRollButton.setVisibility(View.VISIBLE);
            mTrackButton.setVisibility(View.VISIBLE);
            mVoiceButton.setVisibility(View.VISIBLE);
//            if (trackMode)
//                mTrackView.setVisibility(View.VISIBLE);
//            else
//                mTrackView.setVisibility(View.INVISIBLE);
            if (mFlyController.isEnableLimitHigh()) {
                mFlyupButton.setVisibility(View.VISIBLE);
                mFlydownButton.setVisibility(View.VISIBLE);
                mOneKeyStopButton.setVisibility(View.VISIBLE);
            }
            resId = R.mipmap.con_on;

            // Start sending fly controller data
            mFlyController.sendFlyControllerData(true);

            if (isShownChronometer)
                mChronometer.setVisibility(View.VISIBLE);
        } else {
            // UI
            mRudderViewContainer.setVisibility(View.INVISIBLE);
            mFlyupButton.setVisibility(View.INVISIBLE);
            mFlydownButton.setVisibility(View.INVISIBLE);
            mOneKeyStopButton.setVisibility(View.INVISIBLE);
//            mRotateButton.setVisibility(View.INVISIBLE);
//            mFixedDirectionRotateButton.setVisibility(View.INVISIBLE);
//            mReturnButton.setVisibility(View.INVISIBLE);
            mRollButton.setVisibility(View.INVISIBLE);
            mTrackButton.setVisibility(View.INVISIBLE);
            mVoiceButton.setVisibility(View.INVISIBLE);
            mVoiceGuideTextView.setVisibility(View.INVISIBLE);
            if (mFlyController.isTrackMode())
                mTrackView.reset();
            resId = R.mipmap.con_off;

            if (voiceMode) {
                voiceMode = false;
                mVoiceRecognizer.stopListening();

                mVoiceButton.setImageResource(R.mipmap.con_voice);
            }

            // Stop sending fly controller data
            mFlyController.sendFlyControllerData(false);
        }
        mSwitchButton.setImageResource(resId);

        // 关闭控制时,关掉重力控制
        if (!this.enableControl) {
            // 关闭开关时关闭重力感应控制
            setEnableGravityControl(false);
        }
    }

    @Override
    public void sendFlyControllerData(int[] data) {
        byte[] bytes = new byte[data.length];
        for (int i=0; i<data.length; i++) {
            bytes[i] = (byte)data[i];
        }
        // Send
        try {
            mVideoView.sendRtcpRrData(bytes);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 转换摇杆值到整型([0, 1] -> [0, 0xFF])
//     * 为保证中间值为0x80,使用此转换方法
//     * @param v 浮点值
//     * @return  整型值
//     */
//    private int floatToInt(float v) {
//        int intV = (int)(v * 0x100);
//        if (intV < 0) intV = 0;
//        if (intV > 0xFF) intV = 0xFF;
//        return intV;
//    }

    private int floatToInt(float f, int maxValue) {
        return Math.round(f * maxValue);
    }

    /**
     * 显示或者隐藏Chronometer
     * @param bShow 显示开关
     */
    private void showChronometer(boolean bShow) {
        if (bShow) {
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
        } else {
            mChronometer.stop();
            mChronometer.setVisibility(View.INVISIBLE);
            mChronometer.setText("");
        }
    }

    /**
     * 设置按键的显示
     * @param visible   显示开关
     */
    private void setButtonsVisible(boolean visible) {
        if (visible) {
            isButtonsVisible = true;
            mBackButton.setVisibility(View.VISIBLE);
            mTopMenuBar.setVisibility(View.VISIBLE);
            mRightMenuBar.setVisibility(View.VISIBLE);
        } else {
            isButtonsVisible = false;
            mBackButton.setVisibility(View.INVISIBLE);
            mTopMenuBar.setVisibility(View.INVISIBLE);
            mRightMenuBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置隐藏按键的定时器
     */
    private void setHideButtonsTimer() {
        hideButtonsTimer = new CountDownTimer(3000, 3000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                mSettingsButton.setImageResource(R.mipmap.con_extra_settings);
                setButtonsVisible(false);
            }
        }.start();
    }

    /**
     * 设置Voice Guide为默认的Guide文本
     */
    private void resetDefaultVoiceGuide() {
        mVoiceGuideTextView.setText(R.string.control_panel_voice_guide);
    }

    /**
     * 拍照
     */
    private void takePhoto(int num) {
        // Take a photo
        String photoFilePath = Utilities.getPhotoDirPath();
        String photoFileName = Utilities.getMediaFileName();
        try {
            mVideoView.takePicture(photoFilePath, photoFileName, -1, -1, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录像
     */
    private void recordVideo() {
        if (recording) {
            mVideoView.stopRecordVideo();
        } else {
            mFreeSpaceMonitor = new FreeSpaceMonitor();
            if (mFreeSpaceMonitor.checkFreeSpace()) {
                String videoFilePath = Utilities.getVideoDirPath();
                String videoFileName = Utilities.getMediaFileName();
                // Start to record video
                try {
                    mVideoView.startRecordVideo(videoFilePath, videoFileName, -1, -1);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 提示剩余空间不足
                long threshold = mFreeSpaceMonitor.getThreshold();
                float megabytes = threshold / (1024 * 1024);
                String toastString = getResources().getString(R.string.control_panel_insufficient_storage_alert, megabytes);
                Toast.makeText(ControlPanelActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查是否有相应的权限
     * @return 是否已经拿到权限
     */
    private boolean checkPermission(Context context, String permission) {
        int permissionCheck;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = context.checkSelfPermission(permission);
        } else {
            permissionCheck = PermissionChecker.checkSelfPermission(context, permission);
        }

        return permissionCheck == PackageManager.PERMISSION_GRANTED;






    }

    /**
     * 显示打开应用设置的说明对话框
     * @param title     标题
     * @param message   消息
     */
    private void showOpenSettingsAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSettings();
                        // 系统问题：同意权限后，并没有第一时间
                        // 获取到权限，需要重新启动Activity
                        finish();
                    }
                })
                .show();
    }

    /**
     * 打开应用设置项
     */
    private void openSettings() {
        Context context = getApplicationContext();
        Intent myAppSettings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myAppSettings);
    }

    /* Hardware Action */

    private static final byte HW_ACTION_SIGNATURE_BYTE_1 = 0x0f;
    private static final byte HW_ACTION_SIGNATURE_BYTE_2 = 0x5a;
    private static final byte HW_ACTION_SIGNATURE_BYTE_3 = 0x1e;
    private static final byte HW_ACTION_SIGNATURE_BYTE_4 = 0x69;

    private static final byte HW_ACTION_CLASS_TAKE_PHOTO = 0x00;
    private static final byte HW_ACTION_COMMAND_TAKE_PHOTO = 0x01;

    private static final byte HW_ACTION_CLASS_RECORD_VIDEO = 0x01;
    private static final byte HW_ACTION_COMMAND_RECORD_VIDEO = 0x01;

    private void doHwAction(byte commandClass, byte command) {
        switch (commandClass) {
            case HW_ACTION_CLASS_TAKE_PHOTO:
                if (command == HW_ACTION_COMMAND_TAKE_PHOTO) {
                    takePhoto(1);
                }
                break;
            case HW_ACTION_CLASS_RECORD_VIDEO:
                if (command == HW_ACTION_COMMAND_RECORD_VIDEO) {
                    recordVideo();
                }
                break;
        }
    }

    /**
     * 检查是否是有效的硬件指令
     */
    private boolean checkIfIsValidHwActionCommand(byte[] data) {
        if (data.length >= 7) {
            if (data[0] == HW_ACTION_SIGNATURE_BYTE_1
                    && data[1] == HW_ACTION_SIGNATURE_BYTE_2
                    && data[2] == HW_ACTION_SIGNATURE_BYTE_3
                    && data[3] == HW_ACTION_SIGNATURE_BYTE_4) { // sign
//                byte commandClass = data[4];    // index
                byte commandLength = data[5];   // len
//                byte command = data[6];         // data

                if (data.length == commandLength) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {

            mCameraHelper.startPreview(mUVCCameraView);
          //  mCameraHelper.updateResolution(1280, 720);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }
}
