package com.android.tvapp;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.android.tvapp.fragment.AudioFragment;
import com.android.tvapp.fragment.BaseFragment;
import com.android.tvapp.fragment.TextFragment;
import com.android.tvapp.fragment.VideoFragment;
import com.android.tvapp.info.TaskInfo;
import com.android.tvapp.manager.PollRequest;
import com.android.tvapp.manager.PollRequest.OnPollRequestCompletedListener;
import com.android.tvapp.manager.TaskRequest;
import com.android.tvapp.manager.TaskRequest.OnTaskRequestCompletedListener;
import com.android.tvapp.manager.UpgradeManager;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class TVAppActivity extends FragmentActivity implements OnTaskRequestCompletedListener, OnPollRequestCompletedListener {
    private static final long REQUEST_INTERVAL = 10 * 1000;
    private List<TaskInfo> mTaskList;
    private int mCurrentIndex = 0;
    private Handler mHandler;
    private long mRequestCount = 0;
    private PollRequest mPollRequest;
    private TaskRequest mTaskRequest;
    private String mCurrentTaskId = "noset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
        setContentView(R.layout.activity_tvapp);
        mHandler = new Handler();
        mPollRequest = new PollRequest(this);
        mPollRequest.setOnPollRequestCompletedListener(this);
        mTaskRequest = new TaskRequest(this);
        mTaskRequest.setOnTaskRequestCompletedListener(this);
        newVersionCheck();
    }

    private void newVersionCheck() {
        UpgradeManager manager = new UpgradeManager(this);
        manager.checkUpgrade();
    }

    private void requestTaskList() {
        // Log.d(Log.TAG, "Request TaskList " + mRequestCount + " times");
        mTaskRequest.requestTaskInfo();
    }

    @Override
    public void onTaskRequestCompleted(List<TaskInfo> list) {
        if (list != null) {
            Log.d(Log.TAG, "Request Success, showFragment");
            mTaskList = list;
            showFragment();
        }
    }

    private void requestPoll() {
        mRequestCount++;
        if (mRequestCount > Long.MAX_VALUE) {
            mRequestCount = 0;
        }
        Log.d(Log.TAG, "Poll Request Count : " + mRequestCount);
        mPollRequest.requestPollInfo();
    }

    @Override
    public void onPollRequestCompleted(String taskId) {
        // Log.d(Log.TAG, "taskId = " + taskId);
        if (taskId != null && !taskId.equals(mCurrentTaskId)) {
            Log.d(Log.TAG, "");
            requestTaskList();
        }
        mCurrentTaskId = taskId;
        mHandler.postDelayed(mRequestPollRunnable, REQUEST_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
        mHandler.removeCallbacks(mRequestPollRunnable);
    }

    private void register() {
        IntentFilter filter = new IntentFilter(Utils.TASK_COMPLETE);
        filter.addAction(Utils.FINISH_ACTIVITY);
        filter.addAction(Utils.NONEW_VERSION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregister() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private void showFragment() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTaskList == null || mTaskList.isEmpty()) {
                    Log.d(Log.TAG, "TaskList is Empty");
                    return ;
                }
                BaseFragment fragment = null;
                TaskInfo taskInfo = mTaskList.get(mCurrentIndex);
                if (taskInfo != null && !TextUtils.isEmpty(taskInfo.type)) {
                    fragment = createFragment(taskInfo.type);
                }
                try {
                    if (fragment != null) {
                        fragment.setTaskInfo(taskInfo);
                        FragmentTransaction transaction = getSupportFragmentManager()
                                .beginTransaction();
                        transaction.replace(R.id.fragment_layout, fragment);
                        transaction.commitAllowingStateLoss();
                    }
                } catch(Exception e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
        });
    }

    private Runnable mRequestPollRunnable = new Runnable() {
        @Override
        public void run() {
            requestPoll();
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return ;
            }
            Log.d(Log.TAG, "action : " + intent.getAction());
            if (Utils.TASK_COMPLETE.equals(intent.getAction())) {
                mCurrentIndex ++;
                if (mTaskList != null && mCurrentIndex >= mTaskList.size()) {
                    mCurrentIndex = 0;
                }
                showFragment();
            } else if (Utils.FINISH_ACTIVITY.equals(intent.getAction())) {
                finish();
            } else if (Utils.NONEW_VERSION.equals(intent.getAction())) {
                mHandler.post(mRequestPollRunnable);
            }
        }
    };

    private BaseFragment createFragment(String type) {
        if (!TextUtils.isEmpty(type)) {
            if (type.equals("text")) {
                return new TextFragment();
            } else if (type.equals("audio")) {
                return new AudioFragment();
            } else if (type.equals("video")) {
                return new VideoFragment();
            }
        }
        return null;
    }
}
