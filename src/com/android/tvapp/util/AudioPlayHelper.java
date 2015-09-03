package com.android.tvapp.util;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import com.android.tvapp.fragment.OnCompleteListener;

public class AudioPlayHelper implements OnCompletionListener, MediaPlayer.OnPreparedListener, OnErrorListener {
    public MediaPlayer mediaPlayer;
    private OnCompleteListener mOnCompleteListener;

    public AudioPlayHelper() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
             Log.e(Log.TAG, "error : " + e);
        }
    }

    public void setOnCompleteListener(OnCompleteListener l) {
        mOnCompleteListener = l;
    }

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
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.d(Log.TAG, "extra : " + extra);
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
        return true;
    }
}
