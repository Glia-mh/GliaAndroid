package com.layer.quick_start_android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.layer.atlas.AtlasConversationsList;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;


/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ConversationListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    static LayerClient layerClient;
    LoginController loginController;
    static public ParticipantProvider participantProvider;
    private AtlasConversationsList myConversationList;
    static Context context;

    private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle drawerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //set layer Client and Authentication Listeners to ConversationListActivity
        loginController = new LoginController();
        loginController.authenticationListener.assignConversationListActivity(this);
        layerClient = loginController.getLayerClient();

        setContentView(R.layout.activity_list_conversation);

        mOptions = getResources().getStringArray(R.array.left_drawer_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new MyAdapter(this));
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, mOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);

        drawerListener = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawer) {
                //Toast.makeText(context, "open", Toast.LENGTH_SHORT).show();
            }

            public void onDrawerClosed(View drawer) {
                //Toast.makeText(context, "closed", Toast.LENGTH_SHORT).show();
            }
        };

        mDrawerLayout.setDrawerListener(drawerListener);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // if (savedInstanceState==null){

            participantProvider=MainActivity.participantProvider;

            //initialize Conversation List
            myConversationList = (AtlasConversationsList) findViewById(R.id.conversationlist);
            myConversationList.init(layerClient, participantProvider);
            myConversationList.setClickListener(new AtlasConversationsList.ConversationClickListener() {
                public void onItemClick(Conversation conversation) {
                    SwipeDetector swipeDetector = new SwipeDetector();
                    if (swipeDetector.swipeDetected()){
                        if(swipeDetector.getAction().equals(SwipeDetector.Action.LR)){

                        }
                        // do the onSwipe action
                    } else {
                        // do the onItemClick action
                    }
                    startMessagesActivity(conversation);
                }
            });

  //eventual dialog for conversation options
  /*  myConversationList.setLongClickListener(new AtlasConversationsList.ConversationLongClickListener() {
            public void onItemLongClick(final Conversation conversation) {

                final Dialog dialog = new Dialog(ConversationListActivity.this);
                dialog.setContentView(R.layout.conversation_options);
                dialog.setTitle("Conversation Options");
                dialog.show();
                dialog.findViewById(R.id.conversationdeleter).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        layerClient.deleteConversation(conversation, LayerClient.DeletionMode.ALL_PARTICIPANTS);
                        myConversationList.getConversations().remove(conversation);
                        myConversationList.getAdapter().notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });


            }
        });*/


            //to recieve feedback about events that you have not initiated (when another person texts the authenticated user)
            layerClient.registerEventListener(myConversationList);

            //to start a new conversation with + button
            View newconversation = findViewById(R.id.newconversation);
            newconversation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startMessagesActivity(null);
                }
            });


       // }


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }


    // For when a nav drawer item is clicked
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if (mOptions[position].equals("Logout")) loginController.logout();
        else if (mOptions[position].equals("Settings")) {
            //go to settings (right nav drawer?...)
        } else if (mOptions[position].equals("About Roots")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://teamroots.org/"));
            startActivity(browserIntent);
        } else if (mOptions[position].equals("Get Involved")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://teamroots.org/"));
            startActivity(browserIntent);
        }
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

        if (drawerListener.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter {
        String[] options;
        int[] images = new int[]{R.drawable.ic_logout,
                R.drawable.ic_settings,
                R.drawable.ic_launcher,
                R.drawable.ic_get_involved};
        Context context;

        public MyAdapter(Context context) {
            this.context = context;
            options = context.getResources().getStringArray(R.array.left_drawer_options);
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return options[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);
            } else {
                row = convertView;
            }
            TextView titleTextView = (TextView) row.findViewById(R.id.textView);
            ImageView titleImageView = (ImageView) row
                    .findViewById(R.id.imageView);
            titleTextView.setText(options[position]);
            titleImageView.setImageResource(images[position]);
            return row; ///
        }
    }


}
