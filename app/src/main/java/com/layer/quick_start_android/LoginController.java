package com.layer.quick_start_android;

import android.content.Context;
import android.util.Log;

import com.layer.sdk.LayerClient;

import java.util.UUID;

/**
 * Created by adityaaggarwal on 2/18/15.
 */
public class LoginController {
    static LayerClient layerClient;
    static MyAuthenticationListener authenticationListener;
    static MyConnectionListener connectionListener;

    public void LoginController() {

    }

    public void setLayerClient(Context context, MainActivity ma) {
        UUID appID = UUID.fromString("e25bc8da-9f52-11e4-97ea-142b010033d0");
        layerClient = LayerClient.newInstance(context, appID, "155377978502");
        connectionListener = new MyConnectionListener(ma);
        authenticationListener = new MyAuthenticationListener(ma);
    }

    public void login(String mUserId){
        layerClient.registerConnectionListener(connectionListener);
        layerClient.registerAuthenticationListener(authenticationListener);
        authenticationListener.setmUserId(mUserId);
        //add log statement
        if(layerClient.isConnected() && !(layerClient.isAuthenticated())) {
            layerClient.authenticate();
        }else {
           layerClient.connect();
        }
    }
    public LayerClient getLayerClient(){
        return layerClient;
    }
    public void logout(){

        //if (layerClient.isConnected())
          //  layerClient.disconnect();

            layerClient.deauthenticate();
            Log.d("changing","in login controller");
    }

}
