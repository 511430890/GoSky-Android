/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.com.buildwin.gosky.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.buildwin.gosky.R;
import cn.com.buildwin.gosky.content.RecentMediaStorage;
import tv.danmaku.ijk.media.widget.AndroidMediaController;
import tv.danmaku.ijk.media.widget.IjkVideoView;

import static tv.danmaku.ijk.media.widget.IRenderView.AR_ASPECT_FIT_PARENT;
import static tv.danmaku.ijk.media.widget.IjkVideoView.RENDER_TEXTURE_VIEW;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;
    private Uri    mVideoUri;

    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mRightDrawer;

    private boolean mBackPressed;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");

        if (!TextUtils.isEmpty(mVideoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(mVideoPath);
        }

        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        String videoTitle = getIntent().getStringExtra("videoTitle");
        if (actionBar != null)
            actionBar.setTitle(videoTitle);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);
        mHudView = (TableLayout) findViewById(R.id.hud_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mRightDrawer = (ViewGroup) findViewById(R.id.right_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // init player
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setRender(RENDER_TEXTURE_VIEW);
        mVideoView.setAspectRatio(AR_ASPECT_FIT_PARENT);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView);

        mVideoView.setOnErrorListener(new IjkVideoView.IVideoView.OnErrorListener() {
            @Override
            public boolean onError(IjkVideoView videoView, int what, int extra) {
                Toast.makeText(VideoActivity.this, R.string.VideoView_error_text_unknown, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mHudView.setVisibility(View.GONE);

        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
    }
}
