package com.android.tvapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.tvapp.R;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class TextFragment extends Fragment {

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
        mWebView.loadUrl("file:///android_asset/index/index.html");
        // mWebView.loadData(showText, "text/html", "utf-8");
        mHandler.postDelayed(mRunnable, 10 * 1000);
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
            Log.d(Log.TAG, "send text task complete");
            Intent intent = new Intent(Utils.TASK_COMPLETE);
            getActivity().sendBroadcast(intent);
        }
    };
}

