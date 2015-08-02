package com.layer.quick_start_android;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import com.layer.sdk.messaging.Metadata;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

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
            counselorId=(String)conversation.getMetadata().get("counselor.ID");
        }

        FrameLayout bioInformationDrawer=(FrameLayout)findViewById(R.id.counselor_bio_drawer);
        bioInformationDrawer.setVisibility(View.VISIBLE);
        //Bio View
        if (accountType==0) {
            ImageView imageViewCounselor = (ImageView) findViewById(R.id.counselorbioimage);
            boolean fadeImage = false;
            Log.d("ViewMessagesAct","ConversationListActivity.participantprovder.getPartticipant(counselorId)=="+ConversationListActivity.participantProvider.getParticipant(counselorId));
            if(ConversationListActivity.participantProvider.getParticipant(counselorId).getIsAvailable()==false) {
                fadeImage=true;
                findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);  //Show warning if unavailable
            }
            new LoadImage(imageViewCounselor, fadeImage).execute(ConversationListActivity.participantProvider.getParticipant(counselorId).getAvatarString());

            TextView counselorTitle = (TextView) findViewById(R.id.bioinformationtitle);
            counselorTitle.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getFirstName());

            TextView counselorInfo = (TextView) findViewById(R.id.bioinformation);
            counselorInfo.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getBio());
        } else {

            bioInformationDrawer.setVisibility(View.GONE);
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
                    String[] participants = {counselorId, "1"};

                    if(participants.length > 0){







                        Metadata counselor=Metadata.newInstance();
                        counselor.put("name",ConversationListActivity.participantProvider.getParticipant(participants[0]).getFirstName());
                        counselor.put("ID",ConversationListActivity.participantProvider.getParticipant(participants[0]).getID());
                        counselor.put("avatarString",ConversationListActivity.participantProvider.getParticipant(participants[0]).getAvatarString());

                        Metadata student=Metadata.newInstance();
                        student.put("name","");
                        student.put("ID", ConversationListActivity.layerClient.getAuthenticatedUserId());
                        student.put("avatarString",getVanilliconLink());
                        //set MetaData to Conversations
/*                        HashMap<String,HashMap<String, String>> metadataMap=new HashMap<String, HashMap<String, String>>();
                        HashMap<String, String> counselor=new HashMap<String, String>();
                        HashMap<String, String> student=new HashMap<String, String>();*/

                       Metadata metadataConv=Metadata.newInstance();


                        metadataConv.put("counselor",counselor);
                        metadataConv.put("student", student);





                        conversation = ConversationListActivity.layerClient.newConversation(participants);

                        //set metatdata
                        conversation.putMetadata(metadataConv, false);
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

        // Open counselor info nav drawer automatically
        DrawerLayout dl = (DrawerLayout)findViewById(R.id.view_messages_drawer_layout);
        dl.openDrawer(Gravity.RIGHT);


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
        public LoadImage(ImageView imageViewLocal, boolean grayOut) {
            super();
            imageView=imageViewLocal;
            if(grayOut) fadeImage(imageView);

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

        public void fadeImage(ImageView v)
        {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setAlpha(128);   // 128 = 0.5
        }
    }
}

