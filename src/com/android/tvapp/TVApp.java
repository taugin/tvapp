package com.android.tvapp;

import com.android.tvapp.util.VolleyImageLoader;

import android.app.Application;

public class TVApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyImageLoader.initInstance(this);
    }

}
