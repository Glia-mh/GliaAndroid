package com.layer.quick_start_android;

import android.app.Application;

import com.layer.sdk.LayerClient;

/**
 * Created by adityaaggarwal on 11/2/15.
 */
public class RootsApp extends Application {
    @Override
    public void onCreate() {
        LayerClient.enableLogging(this);
        LayerClient.applicationCreated(this);
        super.onCreate();
    }

}
