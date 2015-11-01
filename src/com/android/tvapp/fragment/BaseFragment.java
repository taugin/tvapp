package com.android.tvapp.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;

import com.android.tvapp.info.TaskInfo;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class BaseFragment extends Fragment {
    protected TaskInfo mTaskInfo;
    protected Handler mHandler;
    protected long mStartTime;
    protected long mLeftTime;

    public BaseFragment() {
        mHandler = new Handler();
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        mTaskInfo = taskInfo;
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(mCompleteRunnable);
        }
    }

    protected void sendCompleteBroadcast() {
        mHandler.post(mCompleteRunnable);
    }

    private long getPresetLeftTime() {
        long time = 0;
        try {
            time = Long.parseLong(mTaskInfo.time);
            time = time * 1000;
        } catch(NumberFormatException e) {
            time = 10 * 1000;
        }
        // Log.d(Log.TAG, "time : " + time);
        return time;
    }

    protected void start() {
        Log.d(Log.TAG, "");
        mLeftTime = getPresetLeftTime();
        startCountDown();
    }

    private void startCountDown() {
        mStartTime = SystemClock.elapsedRealtime();
        mHandler.post(mCountDownRunnable);
    }

    public long getLeftTime() {
        long now = SystemClock.elapsedRealtime();
        return now - mStartTime;
    }

    public long getTotalTime() {
        long time = -1;
        if (mTaskInfo != null) {
            try {
                time = Long.parseLong(mTaskInfo.time);
            } catch(NumberFormatException e) {
            }
        }
        return time;
    }

    public void pauseTask() {
        Log.d(Log.TAG, "");
        mHandler.removeCallbacks(mCountDownRunnable);
        long now = SystemClock.elapsedRealtime();
        long elapsed = now - mStartTime;
        mLeftTime = mLeftTime - elapsed;
        Log.d(Log.TAG, "mLeftTime : " + mLeftTime);
    }

    public void resumeTask() {
        Log.d(Log.TAG, "");
        startCountDown();
    }

    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.elapsedRealtime();
            // Log.d(Log.TAG, "now : " + now + " , s : " + mStartTime + " , l : " + mLeftTime);
            if (now - mStartTime > mLeftTime) {
                sendCompleteBroadcast();
                return;
            }
            mHandler.postDelayed(mCountDownRunnable, 1000);
        }
    };

    private Runnable mCompleteRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(Log.TAG, "send task complete broadcast");
            Intent intent = new Intent(Utils.TASK_COMPLETE);
            if (getActivity() != null) {
                getActivity().sendBroadcast(intent);
            }
        }
    };
}
