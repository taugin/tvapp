package com.android.tvapp.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.android.tvapp.R;
import com.android.tvapp.util.Utils;

public class ShowTextLayout extends RelativeLayout {

    private WebView mWebView;
    private OnCompleteListener mOnCompleteListener;
    private Handler mHandler;
    public ShowTextLayout(Context context) {
        super(context);
    }
    public ShowTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ShowTextLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onFinishInflate() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mHandler = new Handler();
    }

    public void start() {
        String ipAddress = Utils.getIpAddress();
        String showText = "";
        showText += "IpAddress : " + ipAddress;
        // mWebView.loadUrl("http://www.baidu.com/");
        mWebView.loadData(showText, "text/html", "utf-8");
        mHandler.postDelayed(mRunnable, 30 * 1000);
    }

    public void stop() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnCompleteListener != null) {
                mOnCompleteListener.onComplete();
            }
        }
    };

    public void setOnCompleteListener(OnCompleteListener l) {
        mOnCompleteListener = l;
    }
}

