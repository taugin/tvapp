package com.android.tvapp.util;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;

import com.android.tvapp.view.OnCompleteListener;

public class AudoPlayHelper implements OnBufferingUpdateListener,
        OnCompletionListener, MediaPlayer.OnPreparedListener, OnErrorListener {
    public MediaPlayer mediaPlayer;
    private SeekBar mSeekBar;
    private OnCompleteListener mOnCompleteListener;
    private Timer mTimer = new Timer();

    public AudoPlayHelper(SeekBar skbProgress) {
        mSeekBar = skbProgress;

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
             Log.e(Log.TAG, "error : " + e);
        }

        mTimer.schedule(mTimerTask, 0, 1000);
    }

    public void setOnCompleteListener(OnCompleteListener l) {
        mOnCompleteListener = l;
    }
    /*******************************************************
     * 通过定时器和Handler来更新进度条
     ******************************************************/
    TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (mediaPlayer == null)
                return;
            if (mediaPlayer.isPlaying() && mSeekBar.isPressed() == false) {
                handleProgress.sendEmptyMessage(0);
            }
        }
    };

    Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            if (mediaPlayer == null) {
                return;
            }
            int position = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();

            if (duration > 0) {
                long pos = mSeekBar.getMax() * position / duration;
                mSeekBar.setProgress((int) pos);
            }
        };
    };

    // *****************************************************

    public void play() {
        mediaPlayer.start();
    }

    public void playUrl(String videoUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepareAsync();// prepare之后自动播放
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "error : " + e);
            if (mOnCompleteListener != null) {
                mOnCompleteListener.onComplete();
            }
        } catch (IllegalStateException e) {
            Log.d(Log.TAG, "error : " + e);
            if (mOnCompleteListener != null) {
                mOnCompleteListener.onComplete();
            }
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
            if (mOnCompleteListener != null) {
                mOnCompleteListener.onComplete();
            }
        }
    }

    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }
    
    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    /**
     * 通过onPrepared播放
     */
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Log.e(Log.TAG, "onPrepared");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.e(Log.TAG, "onCompletion");
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferingProgress) {
        mSeekBar.setSecondaryProgress(bufferingProgress);
        int currentProgress = mSeekBar.getMax()
                * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
        Log.e(currentProgress + "% play", bufferingProgress + "% buffer");
    }

    @Override
    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
        return true;
    }
}
