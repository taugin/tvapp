package com.android.tvapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.android.tvapp.util.Log;
import com.android.tvapp.view.OnCompleteListener;
import com.android.tvapp.view.ShowAudioLayout;
import com.android.tvapp.view.ShowTextLayout;
import com.android.tvapp.view.ShowVideoLayout;

public class TVAppActivity extends Activity implements OnCompleteListener {
    private static int sCount = 0;
    private ProgressBar mProgressBar;
    private ShowTextLayout mShowTextLayout;
    private ShowAudioLayout mShowAudioLayout;
    private ShowVideoLayout mShowVideoLayout;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvapp);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mShowTextLayout = (ShowTextLayout) findViewById(R.id.showtextlayout);
        mShowAudioLayout = (ShowAudioLayout) findViewById(R.id.showaudiolayout);
        mShowVideoLayout = (ShowVideoLayout) findViewById(R.id.showvideolayout);
        
        mShowTextLayout.setOnCompleteListener(this);
        mShowAudioLayout.setOnCompleteListener(this);
        mShowVideoLayout.setOnCompleteListener(this);

        mHandler = new Handler();
        showVideoView();
    }

    private void showHideView() {
        mShowAudioLayout.stop();
        mShowVideoLayout.stop();
        mShowTextLayout.setVisibility(View.GONE);
        mShowAudioLayout.setVisibility(View.GONE);
        mShowVideoLayout.setVisibility(View.GONE);
    }

    private void showTextView() {
        showHideView();
        mShowTextLayout.setVisibility(View.VISIBLE);
        mShowTextLayout.start();
    }

    private void showAudioView() {
        showHideView();
        mShowAudioLayout.setVisibility(View.VISIBLE);
        mShowAudioLayout.start();
    }

    private void showVideoView() {
        showHideView();
        mShowVideoLayout.setVisibility(View.VISIBLE);
        mShowVideoLayout.start();
    }

    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mShowTextLayout.stop();
        mShowAudioLayout.stop();
        mShowVideoLayout.stop();
    }

    @Override
    public void onComplete() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(Log.TAG, "sCount : " + sCount);
                if (sCount % 3 == 0) {
                    showTextView();
                } else if (sCount % 3 == 1){
                    showAudioView();
                } else if (sCount % 3 == 2) {
                    showVideoView();
                }
                sCount++;
            }
        });
    }
}
