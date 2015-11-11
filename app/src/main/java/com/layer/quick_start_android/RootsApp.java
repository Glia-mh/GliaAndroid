package com.layer.quick_start_android;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.layer.sdk.LayerClient;
import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by adityaaggarwal on 11/2/15.
 */
public class RootsApp extends Application {
    @Override
    public void onCreate() {
        //Parse setup
        Parse.initialize(this, "pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl", "BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        LayerClient.enableLogging(this);
        LayerClient.applicationCreated(this);

        super.onCreate();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
