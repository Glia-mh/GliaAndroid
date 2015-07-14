package com.layer.quick_start_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.layer.atlas.AtlasConversationsList;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;


/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ConversationListActivity extends ActionBarActivity  {
    static LayerClient layerClient;
    LoginController loginController;
    static public ParticipantProvider participantProvider;
    private AtlasConversationsList myConversationList;
    static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //set layer Client and Authentication Listeners to ConversationListActivity
        loginController = new LoginController();
        loginController.authenticationListener.assignConversationListActivity(this);
        layerClient = loginController.getLayerClient();

        setContentView(R.layout.activity_list_conversation);


       // if (savedInstanceState==null){

            participantProvider=MainActivity.participantProvider;

            //initialize Conversation List
            myConversationList = (AtlasConversationsList) findViewById(R.id.conversationlist);
            myConversationList.init(layerClient, participantProvider);
            myConversationList.setClickListener(new AtlasConversationsList.ConversationClickListener() {
                public void onItemClick(Conversation conversation) {
                    startMessagesActivity(conversation);
                }
            });

            //to recieve feedback about events that you have not initiated (when another person texts the authenticated user)
            layerClient.registerEventListener(myConversationList);

            //to start a new conversation
            View newconversation = findViewById(R.id.newconversation);
            newconversation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startMessagesActivity(null);
                }
            });


       // }


    }

    //enters or starts a conversation
    private void startMessagesActivity(Conversation c){
        Intent intent = new Intent(ConversationListActivity.this, ViewMessagesActivity.class);
        if(c != null)
            intent.putExtra("conversation-id",c.getId());
        startActivity(intent);
    }

    //for logout
    public void onUserDeauthenticated() {
        Intent logoutIntent = new Intent(this, MainActivity.class);

        startActivity(logoutIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




}
