package com.layer.quick_start_android;

import android.content.Context;
import android.content.Intent;
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
        protected void onPushReceive(Context mContext, Intent intent) {
            Log.d("push", "Push Notification Gray Value Function ");
            String action = intent.getAction();
            if (action.equals(ParsePushBroadcastReceiver.ACTION_PUSH_RECEIVE)) {
                Log.d("push", "Push Notification Gray Value Function ");
                JSONObject extras;
                try {
                    extras = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));
                    Message msg;
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
            }
        }
}
