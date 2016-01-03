package com.team.roots;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.URL;


/**
 * Created by adityaaggarwal on 9/20/15.
 */


public class LayerPushReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("push", "received push notification");


        //Don't show a notification on boot
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            return;

        // Get notification content
        Bundle extras = intent.getExtras();
        String message = "";
        Uri conversationId;
        if (extras.containsKey("layer-push-message")) {
            message = extras.getString("layer-push-message");
        }
        if (extras.containsKey("layer-conversation-id")) {
            conversationId = extras.getParcelable("layer-conversation-id");
        } else {
            conversationId = null;
        }
        String[] tokens;
        if(message!=null)
            tokens= message.split(",");
        else
            tokens=null;

        // Build the notification


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(tokens[0])
                .setContentText(tokens[2])
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setLights(context.getResources().getColor(R.color.tappable_blue), 100, 1900)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

        new LoadImage(mBuilder, context, conversationId).execute(tokens[1]);









    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        NotificationCompat.Builder builder;
        Uri conversationId;
        Context context;

        //for passing image View
        public LoadImage(NotificationCompat.Builder builder, Context context, Uri conversationId) {

            super();
            this.builder=builder;
            this.conversationId=conversationId;
            this.context=context;

        }


        //convert image of link to bitmap
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap=null;
            try {
                bitmap=BitmapFactory.decodeStream(new URL(args[0]).openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ConversationListAct", "failed to decode bitmap");
            }
            return bitmap;
        }

        //set image view to bitmap
        protected void onPostExecute(Bitmap image ) {

            builder.setLargeIcon(image);
            Intent resultIntent = new Intent(context, ViewMessagesActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            resultIntent.putExtra("conversation-id", conversationId);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            // Show the notification
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, builder.build());
        }

    }

}