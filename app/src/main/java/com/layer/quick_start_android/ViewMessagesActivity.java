package com.layer.quick_start_android;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesList;
import com.layer.atlas.AtlasParticipantPicker;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.atlas.RoundImage;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ViewMessagesActivity extends ActionBarActivity  {


    private AtlasMessagesList messagesList;
    private AtlasParticipantPicker participantPicker;
    private AtlasTypingIndicator typingIndicator;
    private AtlasMessageComposer atlasComposer;
    private Conversation conversation;
    private String counselorId=null;

    //account type 1 is counselor
    //account type 0 is student
    //default set to 0
    private int accountType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_view);


        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype",0);

        //if conversation does not exist set counselor Id for conversation initialization
        counselorId=getIntent().getStringExtra("counselor-id");




        //get current conversation
        Uri id = getIntent().getParcelableExtra("conversation-id");
        if(id != null)
            conversation = ConversationListActivity.layerClient.getConversation(id);


        if(counselorId==null){
            Map<String, String> counselor=(Map)conversation.getMetadata().get("counselor");
            counselorId=counselor.get("ID");
        }

        LinearLayout bioInformationBar=(LinearLayout)findViewById(R.id.counselorbiobar);
        bioInformationBar.setVisibility(View.VISIBLE);
        //Bio View
        if (accountType==0) {
            ImageView imageViewCounselor = (ImageView) findViewById(R.id.counselorbioimage);

            new LoadImage(imageViewCounselor).execute(ConversationListActivity.participantProvider.getParticipant(counselorId).getAvatarString());

            TextView counselorTitle = (TextView) findViewById(R.id.bioinformationtitle);
            counselorTitle.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getFirstName());

            TextView counselorInfo = (TextView) findViewById(R.id.bioinformation);
            counselorInfo.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getBio());
        } else {

            bioInformationBar.setVisibility(View.GONE);
        }


        //set message list
        messagesList = (AtlasMessagesList) findViewById(R.id.messageslist);
        messagesList.init(ConversationListActivity.layerClient, ConversationListActivity.participantProvider, accountType);
        messagesList.setConversation(conversation);





        //automatically set to hidden
        //a view with dynamic filtering of a list that allows you to add participants
        participantPicker = (AtlasParticipantPicker) findViewById(R.id.participantpicker);
        String[] currentUser = {ConversationListActivity.layerClient.getAuthenticatedUserId()};
        participantPicker.init(currentUser, ConversationListActivity.participantProvider);
        //if(conversation != null)
        participantPicker.setVisibility(View.GONE);










        //to inform user if someone on the receiving end is typing
        typingIndicator = (AtlasTypingIndicator) findViewById(R.id.typingindicator);
        typingIndicator.init(conversation, new AtlasTypingIndicator.Callback(){
            public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
            }
        });



        //used to create and send messages
        atlasComposer = (AtlasMessageComposer) findViewById(R.id.textinput);
        atlasComposer.init(ConversationListActivity.layerClient, conversation);
        atlasComposer.setListener(new AtlasMessageComposer.Listener(){
            //if returns false means the message will not send and participants not entered
            //in new conversation
            public boolean beforeSend(Message message) {
                if(conversation == null){
                    //does not include sender only reciever
                    String[] participants = {counselorId};

                    if(participants.length > 0){







                        //set MetaData to Conversations
                        HashMap<String,HashMap<String, String>> metadataMap=new HashMap<String, HashMap<String, String>>();
                        HashMap<String, String> counselor=new HashMap<String, String>();
                        HashMap<String, String> student=new HashMap<String, String>();
                        counselor.put("name",ConversationListActivity.participantProvider.getParticipant(participants[0]).getFirstName());
                        counselor.put("ID",ConversationListActivity.participantProvider.getParticipant(participants[0]).getID());
                        counselor.put("avatarString",ConversationListActivity.participantProvider.getParticipant(participants[0]).getAvatarString());
                        student.put("name","Anonymous User 123");
                        student.put("ID",ConversationListActivity.layerClient.getAuthenticatedUserId());
                        student.put("avatarString",getVanilliconLink());
                        metadataMap.put("counselor",counselor);
                        metadataMap.put("student", student);





                        conversation = ConversationListActivity.layerClient.newConversation(participants);

                        //set metatdata
                        conversation.putMetadata((Map)metadataMap, false);
                        Log.d("getting Metadata", "MetaData:" + conversation.getMetadata().toString());


                        messagesList.setConversation(conversation);
                        atlasComposer.setConversation(conversation);
                    } else {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    protected void onResume() {
        super.onResume();
        ConversationListActivity.layerClient.registerEventListener(messagesList);
    }

    protected void onPause(){
        super.onPause();
        ConversationListActivity.layerClient.unregisterEventListener(messagesList);
    }

    public String getVanilliconLink() {
        //load Vanillicon
        byte[] bytesofTest = ConversationListActivity.layerClient.getAuthenticatedUserId().getBytes();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] thedigest = messageDigest.digest(bytesofTest);

        StringBuffer sb = new StringBuffer();
        for (byte b : thedigest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        String vanilliconLink = "http://vanillicon.com/" + sb.toString() + ".png";
        return vanilliconLink;
    }
    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        ImageView imageView=null;

        //for passing image View
        public LoadImage(ImageView imageViewLocal) {
            super();
            imageView=imageViewLocal;

        }

        //convert image of link to bitmap
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap=null;
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ConversationListAct", "failed to decode bitmap");
            }
            return bitmap;
        }

        //set image view to bitmap
        protected void onPostExecute(Bitmap image ) {

            if(image != null){
                RoundImage roundImage=new RoundImage(image);
                imageView.setImageDrawable(roundImage);

            }else{
                Log.d("ConversationListAct", "failed to set bitmap to image view");
            }
        }
    }
}

