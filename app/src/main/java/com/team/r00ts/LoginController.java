package com.team.r00ts;

import android.content.Context;
import android.util.Log;

import com.layer.sdk.LayerClient;

public class LoginController {
    static LayerClient layerClient;
    static MyAuthenticationListener authenticationListener;
    static MyConnectionListener connectionListener;

    public  LoginController() {

    }

    public void setLayerClient(Context context, MainActivity ma) {
        if(layerClient==null) {

            Log.d("check", "check");
            //options.historicSyncPolicy(LayerClient.Options.HistoricSyncPolicy.ALL_MESSAGES);
            layerClient = LayerClient.newInstance(context, "layer:///apps/staging/e25bc8da-9f52-11e4-97ea-142b010033d0",
                    new LayerClient.Options().useFirebaseCloudMessaging(true));


        }
            connectionListener = new MyConnectionListener(ma);
            authenticationListener = new MyAuthenticationListener(ma);

    }

    public void login(String mUserId){
        layerClient.registerConnectionListener(connectionListener);
        layerClient.registerAuthenticationListener(authenticationListener);

        authenticationListener.setmUserId(mUserId);

        //add log statement
        if (!(layerClient.isConnected())) {
            layerClient.connect();
            Log.d("connect", "connect");
        } else if(!(layerClient.isAuthenticated())) {
            layerClient.authenticate();
            Log.d("authenticate","authenticate");
        } else {

            authenticationListener.main_activity.onUserAuthenticated();
        }

    }

    public static LayerClient getLayerClient(){

        return layerClient;
    }
    public void logout(){

        //if (layerClient.isConnected())
          //  layerClient.disconnect();

            connectionListener.setReceive(false);
            layerClient.deauthenticate();

            Log.d("changing", "in login controller");
    }

}
