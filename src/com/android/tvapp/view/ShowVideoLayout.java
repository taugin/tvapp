package com.android.tvapp.view;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.android.tvapp.R;
import com.android.tvapp.util.Log;

public class ShowVideoLayout extends RelativeLayout implements OnCompletionListener, OnErrorListener, OnPreparedListener {

    private VideoView mVideoView;
    private OnCompleteListener mOnCompleteListener;
    private View mProgressBar;

    public ShowVideoLayout(Context context) {
        super(context);
    }
    public ShowVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ShowVideoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        mProgressBar = findViewById(R.id.progressbar);
        mVideoView = (VideoView) findViewById(R.id.videoview);
        mVideoView.setMediaController(new MediaController(getContext()));
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
    }

    public void setOnCompleteListener(OnCompleteListener l) {
        mOnCompleteListener = l;
    }

    public void start() {
        Uri uri1 = Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
        Uri uri2 = Uri.parse("file:///storage/emulated/0/≤‚ ‘ ”∆µ_3.mp4");
        Uri uri3 = Uri.parse("http://forum.ea3w.com/coll_ea3w/attach/2008_10/12237832415.3gp");
        Uri uri4 = Uri.parse("http://www.androidbook.com/akc/filestorage/android/documentfiles/3389/movie.mp4");
        mVideoView.setVideoURI(uri3);
        mVideoView.requestFocus();
        mVideoView.start();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void stop() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int arg1, int arg2) {
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(Log.TAG, "");
        post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
