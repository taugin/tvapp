package com.android.tvapp.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class GlobalRequest {

    private static GlobalRequest sGlobalRequest;
    private Context mContext;

    private RequestQueue mRequestQueue = null;

    private GlobalRequest(Context context) {
        mContext = context;
    }

    public static GlobalRequest get(Context context) {
        if (sGlobalRequest == null) {
            sGlobalRequest = new GlobalRequest(context);
        }
        return sGlobalRequest;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            start();
        }
        return mRequestQueue;
    }

    public void start() {
        mRequestQueue = Volley
                .newRequestQueue(mContext.getApplicationContext());
    }

    public void stop() {
        if (mRequestQueue == null) {
            mRequestQueue.stop();
            mRequestQueue = null;
        }
    }
}
