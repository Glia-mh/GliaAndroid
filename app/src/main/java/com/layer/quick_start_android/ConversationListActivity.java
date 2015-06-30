package com.layer.quick_start_android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
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


//******************************************************************************************************
//******************************************************************************************************

/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ConversationListActivity extends ActionBarActivity implements LayerSyncListener {
    static LayerClient layerClient;
    LoginController loginController;
    static Context context;
 //   SwipeRefreshLayout swipeLayout;
//    static Bitmap vanilliconBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loginController = new LoginController();
        loginController.authenticationListener.assignConversationListActivity(this);
        layerClient = loginController.getLayerClient();
        layerClient.registerSyncListener(this);
        //Log.d("isAuthenticated", "Conversations retrieved:" + layerClient.getConversations());


            setContentView(R.layout.activity_list_conversation);
            //swipeLayout = (SwipeRefreshLayout) findViewById(R.id.container);
            //swipeLayout.setOnRefreshListener(this);
            //swipeLayout.setColorScheme(android.R.color.holo_green_light);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ViewConversationsFragment.newInstance(getIntent().getStringExtra("mUserId")), "ConversationListViewFragment").commit();
       }
    }

    public void onBeforeSync(LayerClient client) {
        // LayerClient is starting synchronization
    }

    public void onAfterSync(LayerClient client) {
        // LayerClient has finshed synchronization
        Log.d("Message syncing complete", "Message Syncing Complete");
        ViewConversationsFragment frag = (ViewConversationsFragment) getSupportFragmentManager().findFragmentByTag("ConversationListViewFragment");
        if (frag!=null) {
            frag.refreshList();
        }
        // frag.scrollToBottom();
    }

    public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {

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
                                           // Toast.makeText(context, "ID " + id + " is invalid.", Toast.LENGTH_SHORT).show();
                                            Log.d("Not working","not working");
                                        }
                                        if (java.util.Arrays.asList(ids).indexOf(id) == ids.length - 1 && participants.size() > 0)
                                            makeConversation();
                                    }
                                }

                                private void makeConversation() {
                                    participants.add(getIntent().getStringExtra("mUserId"));

                                    //Create new conversation
                                    Conversation newConversation = layerClient.newConversation(participants);
                                    Log.d("newConversationCheck", "newConversationCheck" + newConversation.getParticipants());
                                    MessagePart messagePart = layerClient.newMessagePart("text/plain", "Created a new Conversation".getBytes());
                                    Message message = layerClient.newMessage(Arrays.asList(messagePart));

                                    newConversation.send(message);
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
        private SwipeRefreshLayout mSwipeRefreshLayout;
        public ViewConversationsFragment() {
        }



        /**
         * @return the fragment's {@link android.support.v4.widget.SwipeRefreshLayout} widget.
         */
        public SwipeRefreshLayout getSwipeRefreshLayout() {
            return mSwipeRefreshLayout;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Create the list fragment's content view by calling the super method
            final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

            // Now create a SwipeRefreshLayout to wrap the fragment's content view
            mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

            // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
            // the SwipeRefreshLayout
            mSwipeRefreshLayout.addView(listFragmentView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            // Make sure that the SwipeRefreshLayout will fill the fragment
            mSwipeRefreshLayout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            mSwipeRefreshLayout.setColorScheme(android.R.color.holo_green_light);
            // Now return the SwipeRefreshLayout as this fragment's content view
            return mSwipeRefreshLayout;
        }

        public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
            mSwipeRefreshLayout.setOnRefreshListener(listener);
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
            getActivity().finish();
            layerClient.unregisterSyncListener((LayerSyncListener)getActivity());

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
        }

        public void setRefreshing(boolean refreshing) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


            setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshList();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setRefreshing(false);
                            Log.d("On Refresh Called", "onRefresh called from SwipeRefreshLayout");
                        }
                    }, 1500);

                }
            });

        }

        @Override
        public void onStart() {
            Bundle args = getArguments();
            mUserId = args.getString("mUserId");
            Query query = Query.builder(Conversation.class).sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                    .build();

            Log.d("Layer Client Conversations","layerClient Conversations in fragment"+layerClient.getConversations().toString());
            conversations = layerClient.executeQuery(query, Query.ResultType.OBJECTS);



            adapter = new ConversationsAdapter(conversations);
            setListAdapter(adapter);

            //on Long Click delete option
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    final Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.conversation_options);
                    dialog.setTitle("Conversation Options");
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


            super.onStart();
        }


        public void refreshList(){
            Query query = Query.builder(Conversation.class).sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                    .build();

            if(!(layerClient.executeQuery(query, Query.ResultType.OBJECTS).equals(conversations))) {
                onStart();
                adapter.notifyDataSetChanged();
            }
        }


        /**
         * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
         * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
         * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
         * expects to be the one which triggers refreshes. In our case the layout's child is the content
         * view returned from
         * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
         * which is a {@link android.view.ViewGroup}.
         *
         * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
         * override the default behavior and properly signal when a gesture is possible. This is done by
         * overriding {@link #canChildScrollUp()}.
         */
        private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

            public ListFragmentSwipeRefreshLayout(Context context) {
                super(context);
            }

            /**
             * As mentioned above, we need to override this method to properly signal when a
             * 'swipe-to-refresh' is possible.
             *
             * @return true if the {@link android.widget.ListView} is visible and can scroll up.
             */
            @Override
            public boolean canChildScrollUp() {
                final ListView listView = getListView();
                if (listView.getVisibility() == View.VISIBLE) {
                    return canListViewScrollUp(listView);
                } else {
                    return false;
                }
            }

        }

        // BEGIN_INCLUDE (check_list_can_scroll)
        /**
         * Utility method to check whether a {@link ListView} can scroll up from it's current position.
         * Handles platform version differences, providing backwards compatible functionality where
         * needed.
         */
        private static boolean canListViewScrollUp(ListView listView) {
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                // For ICS and above we can call canScrollVertically() to determine this
                return ViewCompat.canScrollVertically(listView, -1);
            } else {
                // Pre-ICS we need to manually check the first visible item and the child view's top
                // value
                return listView.getChildCount() > 0 &&
                        (listView.getFirstVisiblePosition() > 0
                                || listView.getChildAt(0).getTop() < listView.getPaddingTop());
            }
        }

        // END_INCLUDE (check_list_can_scroll)
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
                subtitle.setText(conversation.getLastMessage().getSender().getUserId()+": "+getMessageText(conversation.getLastMessage()));

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
