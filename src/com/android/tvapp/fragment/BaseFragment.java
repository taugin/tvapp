package com.android.tvapp.fragment;

import android.support.v4.app.Fragment;

import com.android.tvapp.info.TaskInfo;

public class BaseFragment extends Fragment {
    protected TaskInfo mTaskInfo;
    public void setTaskInfo(TaskInfo taskInfo) {
        mTaskInfo = taskInfo;
    }
}
