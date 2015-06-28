package com.layer.quick_start_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ViewMessagesActivity extends ActionBarActivity implements LayerSyncListener {

    static LayerClient layerClient;
    LoginController loginController;
    static boolean isSender=false;
    //public static int x=0;
    //Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loginController = new LoginController();
        layerClient = loginController.getLayerClient();

        //layer client is null
        //Get Conversations with participants only includes conversations with that one participant; no more

        Log.d("layerClient", "layerclient"+layerClient.toString());
        setContentView(R.layout.activity_messages_view);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ViewMessagesFragment.newInstance(getIntent().getStringExtra("mUserId"), layerClient),"ViewMessagesFragment").commit();
        }


    }
    @Override
    public void onBackPressed (){
        layerClient.unregisterSyncListener(this);
        Intent intent=new Intent(this, ConversationListActivity.class);
        intent.putExtra("mUserId",layerClient.getAuthenticatedUserId().toString());
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                layerClient.unregisterSyncListener(this);
                Intent intent=new Intent(this, ConversationListActivity.class);
                intent.putExtra("mUserId",layerClient.getAuthenticatedUserId().toString());
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


        public void onBeforeSync(LayerClient client) {
            // LayerClient is starting synchronization
        }

        public void onAfterSync(LayerClient client) {
            // LayerClient has finshed synchronization
            Log.d("Message syncing complete", "Message Syncing Complete");
            ViewMessagesFragment frag = (ViewMessagesFragment) getSupportFragmentManager().findFragmentByTag("ViewMessagesFragment");
            if(frag!=null) {
                frag.refreshList();
            }
            // frag.scrollToBottom();
        }

        public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {

        }


    //Why 4 event changes?
    /*public void onEventMainThread(LayerChangeEvent event) {

        List<LayerChange> changes = event.getChanges();

        if (changes.size() > 0) {
            Log.d("on Event Async has run", "on Event Async has run");


            //ViewMessagesFragment frag = (ViewMessagesFragment) getSupportFragmentManager().findFragmentByTag("ViewMessagesFragment");


            for (LayerChange change : changes) {
                switch (change.getObjectType()) {
                    case CONVERSATION:
                        // Object is a conversation
                        Log.d("Conversation Changed", "Conversation Changed");
                        break;

                    case MESSAGE:
                        // Object is a message


                    switch (change.getChangeType()) {
                        case UPDATE:
                            if(change.getAttributeName()=="isSent") {
                                if(!isSender) {
                                    Log.d("Message Updated", "Message Updated" + change.toString());
                                   *//* ViewMessagesFragment frag = (ViewMessagesFragment) getSupportFragmentManager().findFragmentByTag("ViewMessagesFragment");
                                    frag.refreshList();
                                    frag.scrollToBottom();
                                    Toast.makeText(this, "Event changed.", Toast.LENGTH_SHORT).show();*//*
                                } else
                                {
                                    isSender=false;
                                }
                            }
                            break;
                    }
                }
            }
        }*/








    public static class ViewMessagesFragment extends ListFragment {
        private String mUserId;
        private MessagesAdapter adapter;
        private List<Message> messages;
        private Uri localConversationId;

        public ViewMessagesFragment() {
        }

        public void refreshList(){
            if(!(adapter.getItem(adapter.getCount()-1).equals(layerClient.getConversation(localConversationId).getLastMessage()))) {
                messages=layerClient.getMessages(layerClient.getConversation(localConversationId));
                for(int newMessageCounter=adapter.getCount()-1; newMessageCounter<messages.size(); newMessageCounter++){
                    adapter.add(messages.get(newMessageCounter));
                }
                adapter.notifyDataSetChanged();
                ListView listView = getListView();
                listView.smoothScrollToPosition(getListAdapter().getCount() - 1);

            }
        }
        public static ViewMessagesFragment newInstance(String mUserId, LayerClient layerClient) {
            ViewMessagesFragment f = new ViewMessagesFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();

            args.putString("mUserId", mUserId);

            f.setArguments(args);

            return f;
        }


        //Takes a Layer Message and returns the contents
        public String getMessageText(Message msg) {
            //Stores the contents of the message
            String msgText = "";

            //Each message is composed of MessgeParts with a mime type that can be defined by the sender (default is "text/plain")
            Iterator itr = msg.getMessageParts().iterator();
            while (itr.hasNext()) {
                MessagePart part = (MessagePart) itr.next();

                //In this case, we print text messages, but you can check for and handle whatever content you want
                if (part.getMimeType().equalsIgnoreCase("text/plain")) {
                    try {
                        //Put together the message, with a newline between each part
                        msgText += new String(part.getData(), "UTF-8") + "\n";
                    } catch (UnsupportedEncodingException e) {
                        //Handle encoding failure
                    }
                }
            }

            return msgText;
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState){
            //set to scroll to bottom of list view at the beginning


            getListView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    getListView().setSelection(getListAdapter().getCount() - 1);
                }
            }, 600L);
        }
        public void scrollToBottom(){
            //scroll to bottom
            ListView listView=getListView();
            listView.setSelection(getListAdapter().getCount()-2);
            listView.smoothScrollToPosition(getListAdapter().getCount()-1);
        }
        @Override
        public void onStart(){

            Bundle args = getArguments();
            mUserId = args.getString("mUserId");


            //define local ConversationId
            localConversationId = getActivity().getIntent().getData();

            //set list adapter
            messages = (List<Message>) layerClient.getMessages(layerClient.getConversation(localConversationId));
            adapter = new MessagesAdapter(messages);
            setListAdapter(adapter);

            //Log.d("Participants","Participants"+localLayerClient.getConversation(getActivity().getIntent().getData()).getParticipants().toString());

            //Set ActionBar Title
            String participants_ids=layerClient.getConversation(getActivity().getIntent().getData()).getParticipants().toString();
            participants_ids = participants_ids.replace("[", "");
            participants_ids = participants_ids.replace("]", "");
            participants_ids = participants_ids.replace(",", ", ");
            getActivity().setTitle(participants_ids);



            //on send button click
            Button sendTextButton = (Button) getActivity().findViewById(R.id.sendtextbutton);
            sendTextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                  //  x=2;
                    //getListView().setStackFromBottom(false);
                    //Retrieve Message from Edit Text
                    EditText messageEditText = (EditText) getActivity().findViewById(R.id.messageedittext);
                    String messageString = messageEditText.getText().toString();

                    //Clear EditText
                    messageEditText.setText("");

                    //Send Message
                    MessagePart messagePart =layerClient.newMessagePart("text/plain", messageString.getBytes());
                    Message message = layerClient.newMessage(Arrays.asList(messagePart));
                    layerClient.getConversation(localConversationId).send(message);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //automatic refresh
                    adapter.add(message);
                    adapter.notifyDataSetChanged();
                    isSender=true;

                    //scroll to bottom
                    ListView listView=getListView();
                    listView.smoothScrollToPosition(getListAdapter().getCount()-1);


                }
            });

            EditText editText=(EditText)getActivity().findViewById(R.id.messageedittext);

            editText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    getListView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Select the last row so it will scroll into view...
                            getListView().smoothScrollToPosition(getListAdapter().getCount()-1);
                        }
                    }, 300L);

                }
            });

            // ListView l=getListView();
            // l.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            //l.setStackFromBottom(true);
            super.onStart();
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            layerClient.registerSyncListener((LayerSyncListener)getActivity());
            super.onActivityCreated(savedInstanceState);
        }

        private class MessagesAdapter extends ArrayAdapter<Message> {
            public MessagesAdapter(List<Message> messagesLocal) {
                super(getActivity(), 0, messagesLocal);
            }


            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {


                Message message = getItem(position);
                String messageText = getMessageText(message);
                Log.d("User ID Sent", "UserID of Sent Message: " + message.getSentByUserId());
                Log.d("User ID from Intent", "User ID from Intent: " + mUserId);
                Log.d("Message Sent", "Message Text: "+ getMessageText(message));
                Log.d("Instructions", "UserID of Sent Message should equal UserID from Intent");

                if (message.getSentByUserId().equals(mUserId)) {
                    //if (convertView == null) {
                        Log.d("Converting View", "Creating Green Row");
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.my_sms_row, null);
                    //}
                } else {
                   // if (convertView == null) {
                        Log.d("Converting View","Creating Yellow Row");
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.other_sms_row, null);
                    //}
                }
                TextView smsRowText = (TextView) convertView.findViewById(R.id.smsrowtext);
                smsRowText.setText(messageText);
                return convertView;

            }


            @Override
            public int getCount() {
                return messages.size();
            }

            @Override
            public Message getItem(int position) {
                return messages.get(position);
            }


            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }
        }

    }
}

