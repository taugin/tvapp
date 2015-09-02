package com.android.tvapp;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.android.tvapp.manager.TaskManager;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.view.AudioFragment;
import com.android.tvapp.view.TextFragment;
import com.android.tvapp.view.VideoFragment;

public class TVAppActivity extends FragmentActivity {
    private ArrayList<Fragment> mFragmentList;
    private int mCurrentIndex = 0;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
        setContentView(R.layout.activity_tvapp);
        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(new TextFragment());
        mFragmentList.add(new AudioFragment());
        mFragmentList.add(new VideoFragment());
        mHandler = new Handler();
        showFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
    }

    private void register() {
        IntentFilter filter = new IntentFilter(Utils.TASK_COMPLETE);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregister() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private void showFragment() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = mFragmentList.get(mCurrentIndex);
                Log.d(Log.TAG, "mCurrentIndex : " + mCurrentIndex + " , fragment : " + fragment);
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.fragment_layout, fragment);
                transaction.commitAllowingStateLoss();
            }
        });
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return ;
            }
            if (Utils.TASK_COMPLETE.equals(intent.getAction())) {
                mCurrentIndex = ++mCurrentIndex % mFragmentList.size();
                showFragment();
            }
        }
    };
}
