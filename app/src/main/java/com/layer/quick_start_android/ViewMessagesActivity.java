package com.layer.quick_start_android;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesList;
import com.layer.atlas.AtlasParticipantPicker;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;

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

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_messages_view);



            Uri id = getIntent().getParcelableExtra("conversation-id");
            if(id != null)
                conversation = ConversationListActivity.layerClient.getConversation(id);

            messagesList = (AtlasMessagesList) findViewById(R.id.messageslist);

            messagesList.init(ConversationListActivity.layerClient, ConversationListActivity.participantProvider);
            messagesList.setConversation(conversation);

            participantPicker = (AtlasParticipantPicker) findViewById(R.id.participantpicker);
            String[] currentUser = {ConversationListActivity.layerClient.getAuthenticatedUserId()};
            participantPicker.init(currentUser, ConversationListActivity.participantProvider);
            if(conversation != null)
                participantPicker.setVisibility(View.GONE);

            typingIndicator = (AtlasTypingIndicator) findViewById(R.id.typingindicator);
            typingIndicator.init(conversation, new AtlasTypingIndicator.Callback(){
                public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
                }
            });

            atlasComposer = (AtlasMessageComposer) findViewById(R.id.textinput);
            atlasComposer.init(ConversationListActivity.layerClient, conversation);
            atlasComposer.setListener(new AtlasMessageComposer.Listener(){
                public boolean beforeSend(Message message) {
                    if(conversation == null){
                        String[] participants = participantPicker.getSelectedUserIds();
                        if(participants.length > 0){
                            participantPicker.setVisibility(View.GONE);
                            conversation = ConversationListActivity.layerClient.newConversation(participants);
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

}

