package com.android.tvapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.tvapp.R;
import com.android.tvapp.util.AudioPlayHelper;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.util.VolleyImageLoader;
import com.android.tvapp.view.CustomViewFlipper;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class AudioFragment extends BaseFragment implements OnCompleteListener, OnClickListener {

    private CustomViewFlipper mViewFlipper;
    private AudioPlayHelper mAudoPlayHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showaudio, null);
        mViewFlipper = (CustomViewFlipper) view.findViewById(R.id.viewflipper);
        mViewFlipper.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        start();
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        super.onDestroy();
        if (mViewFlipper != null) {
            mViewFlipper.stopFlipping();
        }
        if (mAudoPlayHelper != null) {
            mAudoPlayHelper.stop();
        }
    }

    private void addBgImage() {
        mViewFlipper.removeAllViews();
        ImageView imageView = null;
        for (String url : mTaskInfo.imgurl) {
            Log.d(Log.TAG, "imgurl : " + url);
            imageView = new ImageView(getActivity());
            imageView.setScaleType(ScaleType.FIT_CENTER);
            VolleyImageLoader loader = VolleyImageLoader.getVolleyImageLoader(getActivity());
            ImageListener imageListener = VolleyImageLoader.getImageListener(imageView, 0);
            imageView.setTag(VolleyImageLoader.encodeUrl(url));
            loader.get(url, imageListener);
            mViewFlipper.addView(imageView);
        }
    }

    private void start() {
        Log.d(Log.TAG, "");
        if (mViewFlipper != null) {
            int interval = 0;
            try {
                interval = Integer.parseInt(mTaskInfo.interval);
                interval *= 1000;
            } catch(NumberFormatException e) {
                interval = 10 * 1000;
            }
            Log.d(Log.TAG, "interval : " + interval);
            mViewFlipper.setFlipInterval(interval);
            addBgImage();
            mViewFlipper.startFlipping();
            Log.d(Log.TAG, "View Count : " + mViewFlipper.getChildCount());
        }

        String musicUrl = mTaskInfo.audiourl;
        Log.d(Log.TAG, "audiourl : " + musicUrl);
        mAudoPlayHelper = new AudioPlayHelper();
        mAudoPlayHelper.setOnCompleteListener(this);
        mAudoPlayHelper.playUrl(musicUrl);
        Log.d(Log.TAG, "after playurl");
    }

    @Override
    public void onComplete() {
        Log.d(Log.TAG, "send audio task complete");
        Intent intent = new Intent(Utils.TASK_COMPLETE);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onClick(View view) {
        Log.d(Log.TAG, "send audio task complete");
        Intent intent = new Intent(Utils.TASK_COMPLETE);
        getActivity().sendBroadcast(intent);
    }
}
