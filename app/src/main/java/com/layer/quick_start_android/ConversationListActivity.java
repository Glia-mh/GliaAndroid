package com.layer.quick_start_android;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ConversationListActivity extends ActionBarActivity {
    static LayerClient layerClient;
    LoginController loginController;
//    static Bitmap vanilliconBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        loginController = new LoginController();
        loginController.authenticationListener.assignConversationListActivity(this);
        layerClient = loginController.getLayerClient();
        Log.d("isAuthenticated", "Conversations retrieved:" + layerClient.getConversations());


            setContentView(R.layout.activity_list_conversation);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ViewConversationsFragment.newInstance(getIntent().getStringExtra("mUserId"))).commit();
       }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.addconversation:

                //show dialog
                final Dialog addDialog = new Dialog(this);
                addDialog.setContentView(R.layout.add_options);
                addDialog.setTitle("Add Conversation");
                addDialog.show();

                Button addButton = (Button) addDialog.findViewById(R.id.addbutton);

                //on Click Listener for Add Click
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //make error check for bad ids

                        //make list of participants
                        EditText addEditText = (EditText) addDialog.findViewById(R.id.addedittext);
                        String addString = addEditText.getText().toString();
                        addEditText.setText("");
                        addString = addString.trim();
                        final String[] ids = addString.split(",");
                        for (String id :ids) id = id.replaceAll(" ", "");
                        final List<String> participants = new ArrayList<String>();
                        for (final String id: ids) {
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("userID", id);
                            ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                                public void done(String response, ParseException e) {
                                    if (e == null) {
                                        if (response.equals("valid")) participants.add(id);
                                        else if (response.equals("invalid")) {
                                            Toast.makeText(MainActivity.context, "ID " + id + " is invalid.", Toast.LENGTH_SHORT).show();
                                        }
                                        if (java.util.Arrays.asList(ids).indexOf(id) == ids.length - 1 && participants.size() > 0)
                                            makeConversation();
                                    }
                                }

                                private void makeConversation() {
                                    participants.add(getIntent().getStringExtra("mUserId"));

                                    //Create new conversation
                                    Conversation newConversation = Conversation.newInstance(participants);
                                    Log.d("newConversationCheck", "newConversationCheck" + newConversation.getParticipants());
                                    MessagePart messagePart = MessagePart.newInstance("text/plain", "Created a new Conversation".getBytes());
                                    Message message = Message.newInstance(newConversation, Arrays.asList(messagePart));

                                    layerClient.sendMessage(message);
                                    while (!message.isSent()) {
                                        Log.d("Check", "Check if message is sent" + message.isSent());
                                    }

                                    addDialog.dismiss();

                                    //Enter new Conversation
                                    Log.d("We finally sent it", "We finally sent it " + message.isSent() + " Participants:" + message.getConversation().getParticipants());

                                    Log.d("trying to enter", "trying to enter");
                                    Intent intent = new Intent(getApplicationContext(), ViewMessagesActivity.class);
                                    intent.setData(newConversation.getId());
                                    intent.putExtra("mUserId", getIntent().getStringExtra("mUserId"));
                                    finish();
                                    startActivity(getIntent());
                                }
                            });

                        }

                    }
                });

                return true;
            case R.id.action_settings:
                return true;
            case R.id.logout:
                setContentView(R.layout.loading_screen);
                getSupportActionBar().hide();
                TextView loggingoutintext=(TextView)findViewById(R.id.loginlogoutinformation);
                loggingoutintext.setText("Logging Out...");
                loginController.logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onUserDeauthenticated() {
        Intent logoutIntent = new Intent(this, MainActivity.class);

        startActivity(logoutIntent);
        finish();
    }

    public static class ViewConversationsFragment extends ListFragment {
      //  private Bitmap bitmap;
        private String mUserId;
        private ConversationsAdapter adapter;
        private List<Conversation> conversations;

        public ViewConversationsFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        public static ViewConversationsFragment newInstance(String mUserId) {
            ViewConversationsFragment f = new ViewConversationsFragment();

            Bundle args = new Bundle();
            args.putString("mUserId", mUserId);
            f.setArguments(args);
            return f;
        }

        public static Drawable LoadImageFromWebOperations(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent(getActivity(), ViewMessagesActivity.class);
            intent.setData(conversations.get(position).getId());
            intent.putExtra("mUserId", mUserId);
            startActivity(intent);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            Bundle args = getArguments();
            mUserId = args.getString("mUserId");
            Log.d("Layer Client Conversations","layerClient Conversations in fragment"+layerClient.getConversations().toString());
            conversations = layerClient.getConversations();



            adapter = new ConversationsAdapter(conversations);
            setListAdapter(adapter);

            //on Long Click delete option
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    final Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.conversation_options);
                    dialog.setTitle("Patient Options");
                    dialog.show();
                    dialog.findViewById(R.id.conversationdeleter).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            layerClient.deleteConversation(adapter.getItem(position), LayerClient.DeletionMode.ALL_PARTICIPANTS);
                            adapter.remove(adapter.getItem(position));

                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    return false;
                }
            });


            super.onActivityCreated(savedInstanceState);
        }


        private class ConversationsAdapter extends ArrayAdapter<Conversation> {

            public ConversationsAdapter(List<Conversation> conversationsLocal) {
                super(getActivity(), 0, conversationsLocal);
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
                        bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("failed to decode bitmap","failed to decode bitmap");
                    }
                    return bitmap;
                }

                //set image view to bitmap
                protected void onPostExecute(Bitmap image ) {

                    if(image != null){
                        imageView.setImageBitmap(image);

                    }else{
                        Log.d("failed to set bitmap to image view", "failed to set bitmap to image view");
                    }
                }
            }
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.conv_list_item_fragment, null);
                }

                Conversation conversation = getItem(position);
                String participants_ids = conversation.getParticipants().toString();
                participants_ids = participants_ids.trim();
                participants_ids = participants_ids.replace("[", "");
                participants_ids = participants_ids.replace("]", "");
                participants_ids = participants_ids.replace(",", ", ");

                //NOT SOLVED FOR MORE THAN TWO PARTICIPANTS PER CONVERSATION!

                //remove my User ID
                ArrayList<String> participantsList= (ArrayList<String>) conversation.getParticipants();
                participantsList.remove(mUserId);


                //x is local therefore it will not interfere with other variables
                //load Vanillicon
                for (int x = 0; x<participantsList.size(); x++) {

                    byte[] bytesofTest = participantsList.get(x).getBytes();
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
                    ImageView vanilliconImageView=(ImageView) convertView.findViewById(R.id.imageViewVanillicon);

                    new LoadImage(vanilliconImageView).execute("http://vanillicon.com/"+sb.toString()+".png");
                }


                //Set text of list item
                TextView familyTitle = (TextView) convertView.findViewById(R.id.list_item_title);
                familyTitle.setText(participants_ids);
                TextView subtitle = (TextView) convertView.findViewById(R.id.list_item_subtitle);
                subtitle.setText(conversation.getLastMessage().getSentByUserId()+": "+getMessageText(conversation.getLastMessage()));

                return convertView;

            }

            //Takes a Layer Message and returns the contents
            public String getMessageText(Message msg) {
                //Stores the contents of the message
                String msgText = "";

                //Each message is composed of MessageParts with a mime type that can be defined by the sender (default is "text/plain")
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
            public int getCount() {
                return conversations.size();
            }

            @Override
            public Conversation getItem(int position) {
                return conversations.get(position);
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

    //for options dialog when adding a new conversation
    public static class MyDialogFragment extends DialogFragment {
        static MyDialogFragment newInstance() {
            return new MyDialogFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.add_options, container, false);
            return v;
        }
    }
}