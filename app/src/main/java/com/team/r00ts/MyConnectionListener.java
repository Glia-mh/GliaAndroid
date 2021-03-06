package com.team.r00ts;

import android.util.Log;
import android.widget.ProgressBar;


import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerConnectionListener;
public class MyConnectionListener implements LayerConnectionListener {
    private MainActivity main_activity;

    //if connection disconnected while in account, want to modify on connection connected callback
    private static boolean receive=false;


    private ConversationListActivity conversationListActivity;



    public MyConnectionListener(MainActivity ma){
        main_activity=ma;
    }


    public MyConnectionListener(ConversationListActivity cla){
        conversationListActivity=cla;
    }
    public static boolean getReceive(){
        return receive;
    }
    @Override
    public void onConnectionConnected(LayerClient client) {
        Log.d("ConnectionCheckConnect", "ConnectionCheckConnect");
        if(!(client.isAuthenticated())){
            try {
                ProgressBar progressBar=(ProgressBar)main_activity.pager.getChildAt(main_activity.pager.getChildCount()-1).findViewById(R.id.login_progress);
                progressBar.setProgress(40);
            } catch (NullPointerException e) {
                Log.d("null","progress bar not updated, not on Main Activity view");
            }
            client.authenticate();
        } else if (!receive){
            receive=true;
            main_activity.onUserAuthenticated();
            Log.d("ConnectionCheckConnect2","ConnectionCheckConnect2");
        }
    }


    public void setReceive(boolean receiveValue){
        receive=receiveValue;
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