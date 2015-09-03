package com.android.tvapp;

import android.app.Application;

import com.android.tvapp.util.VolleyImageLoader;

public class TVApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyImageLoader.initInstance(this);
    }

}
