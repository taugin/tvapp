package com.android.tvapp.fragment;

import android.os.Bundle;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showtext, null);
        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        start();
    }

    protected void start() {
        String ipAddress = Utils.getIpAddress();
        String showText = "";
        showText += "IpAddress : " + ipAddress;
        Log.d(Log.TAG, "texturl : " + mTaskInfo.texturl);
        mWebView.loadUrl(mTaskInfo.texturl);
        super.start();
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
}

