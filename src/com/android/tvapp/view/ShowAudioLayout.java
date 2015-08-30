package com.android.tvapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import com.android.tvapp.R;
import com.android.tvapp.util.AudoPlayHelper;
import com.android.tvapp.util.VolleyImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class ShowAudioLayout extends RelativeLayout implements OnCompleteListener {

    private ViewFlipper mViewFlipper;
    private AudoPlayHelper mAudoPlayHelper;
    private OnCompleteListener mOnCompleteListener;
    
    private String [] mBgImage = new String[] {
            "http://f.hiphotos.baidu.com/image/pic/item/ac4bd11373f082022707d43e49fbfbedab641b1d.jpg",
            "http://c.hiphotos.baidu.com/image/pic/item/a044ad345982b2b73ae68fc533adcbef76099bb2.jpg",
            "http://g.hiphotos.baidu.com/image/pic/item/6609c93d70cf3bc7910a48edd300baa1cd112aac.jpg",
            "http://a.hiphotos.baidu.com/image/pic/item/7c1ed21b0ef41bd5353de6f353da81cb39db3d4e.jpg"
    };
    public ShowAudioLayout(Context context) {
        super(context);
    }
    public ShowAudioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ShowAudioLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
    }

    public void setOnCompleteListener(OnCompleteListener l) {
        mOnCompleteListener = l;
    }

    private void addBgImage() {
        mViewFlipper.removeAllViews();
        ImageView imageView = null;
        for (String url : mBgImage) {
            imageView = new ImageView(getContext());
            VolleyImageLoader loader = VolleyImageLoader.getVolleyImageLoader(getContext());
            ImageListener imageListener = VolleyImageLoader.getImageListener(imageView, 0);
            imageView.setTag(VolleyImageLoader.encodeUrl(url));
            loader.get(url, imageListener);
            mViewFlipper.addView(imageView);
        }
    }

    public void start() {
        String musicUrl = "http://yinyueshiting.baidu.com/data2/music/31266477/87757815120064.mp3?xcode=ac9504977ba7c59b350a5c038dd045b5";
        SeekBar seekBar = new SeekBar(getContext());
        mAudoPlayHelper = new AudoPlayHelper(seekBar);
        mAudoPlayHelper.setOnCompleteListener(this);
        mAudoPlayHelper.playUrl(musicUrl);

        if (mViewFlipper != null) {
            addBgImage();
            mViewFlipper.startFlipping();
        }
    }

    public void stop() {
        if (mAudoPlayHelper != null) {
            mAudoPlayHelper.stop();
        }
        if (mViewFlipper != null) {
            mViewFlipper.stopFlipping();
        }
    }

    @Override
    public void onComplete() {
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onComplete();
        }
    }
}
