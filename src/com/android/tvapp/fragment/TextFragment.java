package com.android.tvapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.tvapp.R;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class TextFragment extends BaseFragment {

    private WebView mWebView;
    private Handler mHandler;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showtext, null);
        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mHandler = new Handler();
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
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void start() {
        String ipAddress = Utils.getIpAddress();
        String showText = "";
        showText += "IpAddress : " + ipAddress;
        Log.d(Log.TAG, "texturl : " + mTaskInfo.texturl);
        mWebView.loadUrl(mTaskInfo.texturl);
        // mWebView.loadData(showText, "text/html", "utf-8");
        long time = 0;
        try {
            time = Long.parseLong(mTaskInfo.time);
            time = time * 1000;
        } catch(NumberFormatException e) {
            time = 10 * 1000;
        }
        Log.d(Log.TAG, "time : " + time);
        mHandler.postDelayed(mRunnable, time);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            Log.d(Log.TAG, "errorCode : " + errorCode + " , description : " + description);
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(Log.TAG, "send text task complete");
            Intent intent = new Intent(Utils.TASK_COMPLETE);
            if (getActivity() != null) {
                getActivity().sendBroadcast(intent);
            }
        }
    };
}

