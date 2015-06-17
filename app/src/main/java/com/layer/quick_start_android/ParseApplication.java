package com.layer.quick_start_android;

import android.widget.Toast;

import com.parse.Parse;

/**
 * Created by ryananderson on 6/16/15.
 */
public class ParseApplication extends  android.app.Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this,"Parse Application onCreate executed.", Toast.LENGTH_SHORT);
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, "YOUR_APP_ID", "YOUR_CLIENT_KEY");
    }
}
