package com.layer.quick_start_android;

import android.util.Log;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerConnectionListener;
public class MyConnectionListener implements LayerConnectionListener {
    private MainActivity main_activity;
    private ConversationListActivity conversationListActivity;
    public MyConnectionListener(MainActivity ma){
        main_activity=ma;
    }
    public MyConnectionListener(ConversationListActivity cla){
        conversationListActivity=cla;
    }

    @Override
    public void onConnectionConnected(LayerClient client) {
        Log.d("ConnectionCheckConnect", "ConnectionCheckConnect");
        if(client.isAuthenticated()){
            main_activity.onUserAuthenticated();
        }else {
            client.authenticate();
        }
    }


    @Override
    public void onConnectionDisconnected(LayerClient arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectionError(LayerClient arg0, LayerException e) {
        // TODO Auto-generated method stub
        Log.d("ConnectionCheckFail", "ConnectionCheckFail");

    }

}