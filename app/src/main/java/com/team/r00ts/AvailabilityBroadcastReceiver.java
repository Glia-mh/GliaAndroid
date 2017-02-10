package com.team.r00ts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adityaaggarwal on 11/5/15.
 */
public class AvailabilityBroadcastReceiver extends ParsePushBroadcastReceiver {

        private static final String TAG = "PushNotificationReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            Log.d("network","Connectivity change detected");
            onPushReceive(context, intent);
        }

        @Override
        protected void onPushReceive(Context mContext, Intent intent) {
            Log.d("push", "Push Notification Gray Value Function ");
            String action = intent.getAction();
            SharedPreferences mPrefs = mContext.getSharedPreferences("label", 0);
            int accountType = mPrefs.getInt("accounttype",0);
            if (action.equals(ParsePushBroadcastReceiver.ACTION_PUSH_RECEIVE) && accountType==0) {
                Log.d("push", "Push Notification Gray Value Function ");
                JSONObject extras;
                Message msg;
                try {
                    extras = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));

                    if(extras.getString("alert").equals("true")) {

                         msg=Message.obtain(ConversationListActivity.availabilityHandler, 1, extras.getString("userID"));

                    } else {
                         msg=Message.obtain(ConversationListActivity.availabilityHandler, 0, extras.getString("userID"));
                    }
                        msg.sendToTarget();

                    Log.d("push", "Push Notification Gray Value Function " + extras.getString("alert")+ "UserIDs "+ extras.getString("userID"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                Message msg;
                Message msg2=null;
                Log.d("Network","in app network change detected");
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                //should check null because in airplane mode it will be null
                if(netInfo != null && netInfo.isConnected()){
                    msg=Message.obtain(ConversationListActivity.availabilityHandler,2);
                    msg2=Message.obtain(MainActivity.networkHandler);
                } else {
                    msg=Message.obtain(ConversationListActivity.availabilityHandler,3);

                }
                if(ConversationListActivity.availabilityHandler!=null) {
                    msg.sendToTarget();
                    if(msg2!=null && MainActivity.networkHandler!=null) {
                        msg2.sendToTarget();
                    }
                }
            }
        }
}
