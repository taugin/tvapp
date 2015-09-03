package com.android.tvapp;

import java.util.HashMap;
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
import android.view.View;
import android.widget.TextView;

import com.android.tvapp.info.TaskInfo;
import com.android.tvapp.manager.TaskRequest;
import com.android.tvapp.manager.TaskRequest.OnTaskRequestCompletedListener;
import com.android.tvapp.manager.UpgradeManager;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.view.AudioFragment;
import com.android.tvapp.view.BaseFragment;
import com.android.tvapp.view.TextFragment;
import com.android.tvapp.view.VideoFragment;

public class TVAppActivity extends FragmentActivity implements OnTaskRequestCompletedListener {
    private HashMap<String, BaseFragment> mHashMap;
    private List<TaskInfo> mTaskList;
    private TextView mEmptyView;
    private int mCurrentIndex = 0;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
        setContentView(R.layout.activity_tvapp);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mHashMap = new HashMap<String, BaseFragment>();
        mHashMap.put("text", new TextFragment());
        mHashMap.put("audio", new AudioFragment());
        mHashMap.put("video", new VideoFragment());
        mHandler = new Handler();
        newVersionCheck();
    }

    private void newVersionCheck() {
        UpgradeManager manager = new UpgradeManager(this);
        manager.checkUpgrade();
    }

    private void requestTaskList() {
        TaskRequest taskRequest = new TaskRequest(this);
        taskRequest.setOnTaskRequestCompletedListener(this);
        taskRequest.requestTaskInfo();
    }

    @Override
    public void onTaskRequestCompleted(List<TaskInfo> list) {
        if (list != null) {
            mTaskList = list;
        } else {
            Log.d(Log.TAG, "Request TaskList Failure");
        }
        if (mTaskList != null) {
            showFragment();
        } else {
            Log.d(Log.TAG, "TaskList is Empty");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
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
                if (mTaskList == null) {
                    Log.d(Log.TAG, "TaskList is Empty");
                    return ;
                }
                BaseFragment fragment = null;
                TaskInfo taskInfo = mTaskList.get(mCurrentIndex);
                if (taskInfo != null && !TextUtils.isEmpty(taskInfo.type)) {
                    fragment = mHashMap.get(taskInfo.type);
                }
                Log.d(Log.TAG, "mCurrentIndex : " + mCurrentIndex + " , fragment : " + fragment);
                try {
                    if (fragment != null) {
                        fragment.setTaskInfo(taskInfo);
                        FragmentTransaction transaction = getSupportFragmentManager()
                                .beginTransaction();
                        transaction.replace(R.id.fragment_layout, fragment);
                        transaction.commitAllowingStateLoss();
                        mEmptyView.setVisibility(View.INVISIBLE);
                    }
                } catch(Exception e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
        });
    }

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
                    requestTaskList();
                }
                showFragment();
            } else if (Utils.FINISH_ACTIVITY.equals(intent.getAction())) {
                finish();
            } else if (Utils.NONEW_VERSION.equals(intent.getAction())) {
                requestTaskList();
            }
        }
    };
}
