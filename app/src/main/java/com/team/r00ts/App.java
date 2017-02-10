package com.team.r00ts;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.FirebaseApp;
import com.layer.atlas.util.picasso.requesthandlers.MessagePartRequestHandler;
import com.layer.sdk.LayerClient;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.squareup.picasso.Picasso;

import static com.team.r00ts.LoginController.getLayerClient;

/**
 * Created by adityaaggarwal on 11/2/15.
 */
public class App extends Application {

    private static Picasso sPicasso;
    private static Application sInstance;
    @Override
    public void onCreate() {

        FirebaseApp.initializeApp(getApplicationContext());
        Parse.initialize(new Parse.Configuration.Builder(this)
                                .applicationId("pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl")
                                .clientKey("BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc")
                                .server("https://parseapi.back4app.com/").build()
                );

        ParseInstallation.getCurrentInstallation().saveInBackground();
        LayerClient.setLoggingEnabled(this, true);
        LayerClient.applicationCreated(this);
        sInstance = this;

        super.onCreate();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public static Picasso getPicasso() {
        if (sPicasso == null) {
            // Picasso with custom RequestHandler for loading from Layer MessageParts.
            sPicasso = new Picasso.Builder(sInstance)
                    .addRequestHandler(new MessagePartRequestHandler(getLayerClient()))
                    .build();
        }
        return sPicasso;
    }


}
