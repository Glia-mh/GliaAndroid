package com.layer.quick_start_android;

import android.app.Application;

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

}
