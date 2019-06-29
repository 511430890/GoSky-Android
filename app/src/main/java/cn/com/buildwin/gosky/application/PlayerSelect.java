package cn.com.buildwin.gosky.application;

import android.view.View;
import android.widget.TableLayout;

import tv.danmaku.ijk.media.widget.IjkVideoView;

import static tv.danmaku.ijk.media.widget.IRenderView.AR_MATCH_PARENT;
import static tv.danmaku.ijk.media.widget.IjkVideoView.RENDER_TEXTURE_VIEW;
import static tv.danmaku.ijk.media.widget.IjkVideoView.RTP_JPEG_PARSE_PACKET_METHOD_FILL;

public class PlayerSelect {


    public static final int BUILDWIN = 0 ;
   private IjkVideoView mVideoView;
   // TableLayout mHudView;
    private static final int VIDEO_VIEW_RENDER = RENDER_TEXTURE_VIEW;
    private static final int VIDEO_VIEW_ASPECT = AR_MATCH_PARENT;
    private static final int RTP_JPEG_PARSE_PACKET_METHOD = RTP_JPEG_PARSE_PACKET_METHOD_FILL;
    private static final int RECONNECT_INTERVAL = 500;

   // private String mplayer;
   private int mplayer;
    public PlayerSelect(int player) {

        this.mplayer = player;

        if (mplayer == BUILDWIN)
        {
           // initBUILDWINplayer();
        }
    }

    private void initBUILDWINplayer() {



        // init UI
      //  mHudView.setVisibility(View.GONE);

        // init player
        mVideoView.setRtpJpegParsePacketMethod(RTP_JPEG_PARSE_PACKET_METHOD);
        mVideoView.setRender(VIDEO_VIEW_RENDER);
        mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);
       // mVideoView.setHudView(mHudView);



    }




}
