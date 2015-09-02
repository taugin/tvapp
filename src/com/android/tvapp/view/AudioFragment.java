package com.android.tvapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import com.android.tvapp.R;
import com.android.tvapp.util.AudoPlayHelper;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.util.VolleyImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class AudioFragment extends Fragment implements OnCompleteListener {

    private ViewFlipper mViewFlipper;
    private AudoPlayHelper mAudoPlayHelper;
    
    private String [] mBgImage = new String[] {
            "http://f.hiphotos.baidu.com/image/pic/item/ac4bd11373f082022707d43e49fbfbedab641b1d.jpg",
            "http://c.hiphotos.baidu.com/image/pic/item/a044ad345982b2b73ae68fc533adcbef76099bb2.jpg",
            "http://g.hiphotos.baidu.com/image/pic/item/6609c93d70cf3bc7910a48edd300baa1cd112aac.jpg",
            "http://a.hiphotos.baidu.com/image/pic/item/7c1ed21b0ef41bd5353de6f353da81cb39db3d4e.jpg"
    };

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showaudio, null);
        mViewFlipper = (ViewFlipper) view.findViewById(R.id.viewflipper);
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
    }


    private void addBgImage() {
        mViewFlipper.removeAllViews();
        ImageView imageView = null;
        for (String url : mBgImage) {
            imageView = new ImageView(getActivity());
            VolleyImageLoader loader = VolleyImageLoader.getVolleyImageLoader(getActivity());
            ImageListener imageListener = VolleyImageLoader.getImageListener(imageView, 0);
            imageView.setTag(VolleyImageLoader.encodeUrl(url));
            loader.get(url, imageListener);
            mViewFlipper.addView(imageView);
        }
    }

    private void start() {
        String musicUrl = "http://yinyueshiting.baidu.com/data2/music/31266477/87757815120064.mp3?xcode=ac9504977ba7c59b350a5c038dd045b5";
        SeekBar seekBar = new SeekBar(getActivity());
        mAudoPlayHelper = new AudoPlayHelper(seekBar);
        mAudoPlayHelper.setOnCompleteListener(this);
        mAudoPlayHelper.playUrl(musicUrl);

        if (mViewFlipper != null) {
            addBgImage();
            mViewFlipper.startFlipping();
            Log.d(Log.TAG, "View Count : " + mViewFlipper.getChildCount());
        }
    }

    @Override
    public void onComplete() {
        Log.d(Log.TAG, "send audio task complete");
        Intent intent = new Intent(Utils.TASK_COMPLETE);
        getActivity().sendBroadcast(intent);
    }
}
