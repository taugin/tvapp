package com.android.tvapp.fragment;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.android.tvapp.R;
import com.android.tvapp.util.Log;

public class VideoFragment extends BaseFragment implements OnCompletionListener, OnErrorListener, OnPreparedListener {

    private VideoView mVideoView;
    private View mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showvideo, null);
        mProgressBar = view.findViewById(R.id.progressbar);
        mVideoView = (VideoView) view.findViewById(R.id.videoview);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        start();
    }

    protected void start() {
        Uri uri1 = Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
        Uri uri2 = Uri.parse("file:///storage/emulated/0/²âÊÔÊÓÆµ_3.mp4");
        Uri uri3 = Uri.parse("http://forum.ea3w.com/coll_ea3w/attach/2008_10/12237832415.3gp");
        Uri uri4 = Uri.parse("http://www.androidbook.com/akc/filestorage/android/documentfiles/3389/movie.mp4");
        if (mTaskInfo == null) {
            sendCompleteBroadcast();
            return ;
        }
        Log.d(Log.TAG, "videourl : " + mTaskInfo.videourl);
        Uri videoUri = Uri.parse(mTaskInfo.videourl);
        mVideoView.stopPlayback();
        mVideoView.setVideoURI(videoUri);
        mVideoView.requestFocus();
        mVideoView.start();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public long getLeftTime() {
        if (mVideoView != null) {
            long curpos = mVideoView.getCurrentPosition();
            long leftTime = curpos;
            return leftTime > 0 ? leftTime : -1;
        }
        return -1;
    }

    @Override
    public long getTotalTime() {
        long time = -1;
        if (mVideoView != null) {
            time = mVideoView.getDuration() / 1000;
        }
        return time;
    }

    public void pauseTask() {
        Log.d(Log.TAG, "");
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    public void resumeTask() {
        Log.d(Log.TAG, "");
        if (mVideoView != null) {
            mVideoView.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        sendCompleteBroadcast();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int arg1, int arg2) {
        Log.d(Log.TAG, "send video task complete");
        sendCompleteBroadcast();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(Log.TAG, "");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
