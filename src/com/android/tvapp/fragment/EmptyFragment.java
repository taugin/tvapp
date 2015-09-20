package com.android.tvapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.tvapp.R;

public class EmptyFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty_fragment, null);
    }

    @Override
    public void pauseTask() {
    }

    @Override
    public void resumeTask() {
    }
}
