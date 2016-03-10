package com.team.roots;

/**
 * Created by adityaaggarwal on 3/7/16.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
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

import com.layer.atlas.ReportedIDList;



public class ReportedIDListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    LoginController loginController;
    private ReportedIDList reportedIDList;
    static Context context;
    private String myID;
    private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListLeft;
    private ActionBarDrawerToggle leftDrawerListener;
    private SharedPreferences mPrefs;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversation);
        findViewById(R.id.conversationlist).setVisibility(View.GONE);
        if(!isNetworkAvailable()){
            findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);
        }
        mPrefs = getSharedPreferences("label", 0);

        context = this;



        Log.d("ConversationList", "Conversation List Activity recreated");
        //set layer Client and Authentication Listeners to ConversationListActivity

        // LoginController.authenticationListener.assignConversationListActivity(this);




        getSupportActionBar().setTitle(R.string.reportedids);  // provide compatibility to all the versions










        // LEFT/RIGHT NAV DRAWERS********************************************* onCreate

        //Setting options for Drawers

        mOptions = getResources().getStringArray(R.array.left_drawer_options);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);













        //Left Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);



        leftDrawerListener = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawer) {
                DrawerLayout.LayoutParams drawerParams = (DrawerLayout.LayoutParams)drawer.getLayoutParams();
                if(drawerParams.gravity== Gravity.START)mDrawerLayout.closeDrawer(GravityCompat.END);
            }

        };

        mDrawerLayout.setDrawerListener(leftDrawerListener);
        mDrawerListLeft.setOnItemClickListener(this);





        // ************************************



        //initialize Conversation List
        reportedIDList = (ReportedIDList) findViewById(R.id.listreported);


        reportedIDList.init(LoginController.layerClient,  context);


        reportedIDList.setClickListener(new ReportedIDList.UserClickListener() {
            public void onItemClick(String user) {

            }
        });




        leftDrawerListener.syncState();




    }


    public void onResume() {
        super.onResume();
        //set layer Client and Authentication Listeners to ConversationListActivity
        LoginController.authenticationListener.assignReportedIDListActivity(this);

    }




    //enters or starts a conversation
//    private void startMessagesActivity(Conversation c){
//
//        Intent intent = new Intent(ConversationListActivity.this, ViewMessagesActivity.class);
//
//        if(c != null) {
//            intent.putExtra("conversation-id", c.getId());
//
//        }
//
//        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//    }



    //for logout
    public void onUserDeauthenticated() {
        Log.d("Login", "Logging Out");
        mPrefs.edit().clear().apply();
        LoginController.layerClient.unregisterAuthenticationListener(LoginController.authenticationListener);
        LoginController.layerClient.unregisterConnectionListener(LoginController.connectionListener);

        finish();

        Intent logoutIntent = new Intent(this, MainActivity.class);

        startActivity(logoutIntent);

    }

    private boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }


    //Options Menu Functions **********************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return leftDrawerListener.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
    //**********************************************






    // Left drawer**********************************************************************
    class MyAdapter extends BaseAdapter {
        String[] options;
        int[] images;
        Context context;

        public MyAdapter(Context context) {
            this.context = context;
            options = mOptions;

            images=new int[]{R.drawable.ic_logout,
                    R.drawable.ic_launcher};

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
            View row;
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


    // For when a left nav drawer item is clicked
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if (mOptions[position].equals("Logout")) {
            if(isNetworkAvailable()) {
                setContentView(R.layout.loading_screen);
                getSupportActionBar().hide();
                //TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                //loggingoutintext.setText("Logging Out...");
                if (loginController==null)
                    loginController=new LoginController();
                loginController.logout();
            } else {
                getWelcomeAlertDialog(R.string.no_internet_connection).show();
            }
        } else if (mOptions[position].equals("Settings")) {
            //go to settings (right nav drawer)
            DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
            dl.closeDrawer(GravityCompat.START);
            dl.openDrawer(GravityCompat.END);
        } else if (mOptions[position].equals("About Roots")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://teamroots.org/"));
            startActivity(browserIntent);
        }
    }
    //**********************************************************************


    private AlertDialog getWelcomeAlertDialog(int stringAddress){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(stringAddress)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    public void onConversationDeleted(){
        Log.d("ConvListAct", "onConversationDeleted");
        onResume();
    }







}

