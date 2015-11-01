package com.android.tvapp;

import java.io.File;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tvapp.fragment.AudioFragment2;
import com.android.tvapp.fragment.BaseFragment;
import com.android.tvapp.fragment.EmptyFragment;
import com.android.tvapp.fragment.TextFragment;
import com.android.tvapp.fragment.VideoFragment;
import com.android.tvapp.info.PollInfo;
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
    private BaseFragment mCurrentFragment;
    private int mCurrentIndex = 0;
    private Handler mHandler;
    private long mRequestCount = 0;
    private PollRequest mPollRequest;
    private TaskRequest mTaskRequest;
    private String mCurrentTaskId = "noset";
    private boolean mTaskPlayStatus = true;
    private ImageView mImageView;
    private TextView mShowStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
        setContentView(R.layout.activity_tvapp);
        mShowStatusView = (TextView) findViewById(R.id.show_state);
        mShowStatusView.setVisibility(View.GONE);
        mImageView = (ImageView) findViewById(R.id.playpause);
        mImageView.setVisibility(View.GONE);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_layout, new EmptyFragment());
        transaction.commitAllowingStateLoss();
        mHandler = new Handler();
        mPollRequest = new PollRequest(this);
        mPollRequest.setOnPollRequestCompletedListener(this);
        mTaskRequest = new TaskRequest(this);
        mTaskRequest.setOnTaskRequestCompletedListener(this);
        newVersionCheck();
        requestTaskList();
        showStatusInfo();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MENU) {
                showStatusInfo();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                Intent intent = new Intent(Utils.TASK_COMPLETE);
                sendBroadcast(intent);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showDebugString() {
        String ver = Utils.getAppVersion(this);
        String ip = Utils.getIpAddress();
        StringBuilder builder = new StringBuilder();
        String tmp = null;
        tmp = getResources().getString(R.string.version);
        builder.append(tmp);
        builder.append(" ");
        builder.append(ver);
        builder.append("\n");
        tmp = getResources().getString(R.string.ipaddress);
        builder.append(tmp);
        builder.append(" ");
        builder.append(ip);
        builder.append("\n");
        tmp = getResources().getString(R.string.remoteaddress);
        builder.append(tmp);
        builder.append(" ");
        builder.append(Utils.HOST_URL);

        TaskInfo taskInfo = getCurrentTaskInfo();
        if (taskInfo != null) {
            if (!TextUtils.isEmpty(taskInfo.name)) {
                builder.append("\n");
                tmp = getResources().getString(R.string.roomname);
                builder.append(tmp);
                builder.append(" ");
                builder.append(taskInfo.name);
            }

            if (!TextUtils.isEmpty(taskInfo.interval)) {
                builder.append("\n");
                tmp = getResources().getString(R.string.taskinterval);
                builder.append(tmp);
                builder.append(" ");
                builder.append(taskInfo.interval);
            }

            if (!TextUtils.isEmpty(taskInfo.type)) {
                builder.append("\n");
                tmp = getResources().getString(R.string.tasktype);
                builder.append(tmp);
                builder.append(" ");
                builder.append(taskInfo.type);
            }

            if (!TextUtils.isEmpty(taskInfo.ip)) {
                builder.append("\n");
                tmp = getResources().getString(R.string.acceptaddress);
                builder.append(tmp);
                builder.append(" ");
                builder.append(taskInfo.ip);
            }
        }
        if (mCurrentFragment != null) {
            long totalTime = mCurrentFragment.getTotalTime();
            builder.append("\n");
            tmp = getResources().getString(R.string.tasktime);
            builder.append(tmp);
            builder.append(" ");
            builder.append(totalTime);
            long leftTime = mCurrentFragment.getLeftTime();
            leftTime = (long) ((double)leftTime / 1000);
            builder.append("\n");
            tmp = getResources().getString(R.string.consumetime);
            builder.append(tmp);
            builder.append(" ");
            builder.append(leftTime);
        }
        mShowStatusView.setText(builder.toString());
        mShowStatusView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mDismissRunnable, 10 * 1000);
    }

    private void showStatusInfo() {
        if (mShowStatusView.getVisibility() != View.VISIBLE) {
            showDebugString();
        } else {
            mShowStatusView.setVisibility(View.INVISIBLE);
            mHandler.removeCallbacks(mDismissRunnable);
        }
    }

    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            mShowStatusView.setVisibility(View.GONE);
        }
    };

    private void newVersionCheck() {
        UpgradeManager manager = new UpgradeManager(this);
        manager.checkUpgrade();
    }

    private void requestTaskList() {
        Log.d(Log.TAG, "Request TaskList " + mRequestCount + " times");
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
        // Log.d(Log.TAG, "Poll Request Count : " + mRequestCount);
        if (mPollRequest != null) {
            mPollRequest.requestPollInfo();
        }
    }

    @Override
    public void onPollRequestCompleted(PollInfo pollInfo) {
        Log.d(Log.TAG, "" + pollInfo);
        String taskId = null;
        boolean status = true;
        if (pollInfo != null) {
            taskId = pollInfo.taskId;
            Log.LOGTOFILE = pollInfo.logtofile; 
            status = pollInfo.status;
        } else {
            Log.LOGTOFILE = false;
        }
        if (taskId != null && !taskId.equals(mCurrentTaskId)) {
            Log.d(Log.TAG, "");
            requestTaskList();
        }else if (mTaskPlayStatus != status) {
            mTaskPlayStatus = status;
            changeTaskStatus();
        }
        mCurrentTaskId = taskId;
        mHandler.postDelayed(mRequestPollRunnable, REQUEST_INTERVAL);
    }

    private void changeTaskStatus() {
        if (mCurrentFragment != null) {
            mImageView.setVisibility(mTaskPlayStatus ? View.GONE : View.VISIBLE);
            if (mTaskPlayStatus) {
                mCurrentFragment.resumeTask();
            } else {
                mCurrentFragment.pauseTask();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
        mPollRequest = null;
        mHandler.removeCallbacks(mRequestPollRunnable);
        mHandler.removeCallbacks(mDismissRunnable);
        deleteCache();
    }

    private void deleteCache() {
        File cacheDir = Utils.getPicCache(this);
        if (cacheDir != null) {
            File fileList[] = cacheDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    Log.d(Log.TAG, "file : " + file);
                    file.delete();
                }
            }
        }
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

    private TaskInfo getCurrentTaskInfo() {
        TaskInfo taskInfo = null;
        try {
            taskInfo = mTaskList.get(mCurrentIndex);
        } catch (IndexOutOfBoundsException e) {
            Log.d(Log.TAG, "error : " + e);
            mCurrentIndex = 0;
            taskInfo = mTaskList.get(mCurrentIndex);
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return taskInfo;
    }

    private void showFragment() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTaskList == null || mTaskList.isEmpty()) {
                    Log.d(Log.TAG, "TaskList is Empty");
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction();
                    transaction.replace(R.id.fragment_layout,
                            new EmptyFragment());
                    transaction.commitAllowingStateLoss();
                    return ;
                }
                BaseFragment fragment = null;
                TaskInfo taskInfo = getCurrentTaskInfo();
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
                        mCurrentFragment = fragment;
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
                return new AudioFragment2();
            } else if (type.equals("video")) {
                return new VideoFragment();
            }
        }
        return null;
    }
}