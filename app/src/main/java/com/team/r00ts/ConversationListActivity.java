package com.team.r00ts;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.atlas.AtlasConversationsRecyclerView;
import com.layer.atlas.adapters.AtlasConversationsAdapter;
import com.layer.atlas.messagetypes.location.LocationCellFactory;
import com.layer.atlas.messagetypes.singlepartimage.SinglePartImageCellFactory;
import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.sdk.messaging.Conversation;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import static com.team.r00ts.App.getPicasso;
import static com.team.r00ts.LoginController.layerClient;


public class ConversationListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    LoginController loginController;
    static public ParticipantProvider participantProvider;
    private AtlasConversationsRecyclerView myConversationList;
    static Context context;
    private String myID;
    private String[] mOptions;
    private String[] mOptionsRightDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListLeft;
    private ListView mDrawerListRight;
    private ActionBarDrawerToggle leftDrawerListener;
    private SharedPreferences mPrefs;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruImageCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB


    public static myHandler availabilityHandler;

    //account type 1 is counselor
    //account type 0 is student
    //default set to 0
    private int accountType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversation);
        //findViewById(R.id.listreported).setVisibility(View.GONE);
        if (!isNetworkAvailable()) {
            findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);
        }


        context = this;
        mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype", 0);


        Log.d("ConversationList", "Conversation List Activity recreated");
        //set layer Client and Authentication Listeners to ConversationListActivity

        // LoginController.authenticationListener.assignConversationListActivity(this);
        myID = layerClient.getAuthenticatedUser().getUserId();

        availabilityHandler = new myHandler(this);


        getSupportActionBar().setTitle(R.string.conversations);  // provide compatibility to all the versions





        // LEFT/RIGHT NAV DRAWERS********************************************* onCreate

        //Setting options for Drawers
        if (accountType == 0 || accountType == 2) {
            mOptions = getResources().getStringArray(R.array.left_drawer_options);

        } else if (accountType == 1) {
            mOptions = getResources().getStringArray(R.array.left_drawer_options_counselor);
            mOptionsRightDrawer = getResources().getStringArray(R.array.right_drawer_options_counselor);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);



        //right drawer
        mDrawerListRight = (ListView) findViewById(R.id.right_drawer);
        PackageManager pm = this.getPackageManager();
        if (accountType == 1) {
            Log.d("MyID", "MyID initial push notification receiver set " + myID);
            if (!MainActivity.participantProvider.getParticipant(myID).getIsAvailable()) {
                Log.d("Disabled", "Disabled");
                ComponentName receiver = new ComponentName(this, LayerPushReceiver.class);


                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

            }


            mDrawerListRight.setAdapter(new CounselorRightDrawerAdapter(this));
        }


                //Left Drawer
            mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);

            // Set the adapter for the list view
            mDrawerListLeft.setAdapter(new MyAdapter(this));
            if (accountType == 0 || accountType == 2) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawerListRight);
            }

            leftDrawerListener = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawer) {
                    DrawerLayout.LayoutParams drawerParams = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
                    if (drawerParams.gravity == Gravity.START)
                        mDrawerLayout.closeDrawer(GravityCompat.END);
                }

            };

            mDrawerLayout.setDrawerListener(leftDrawerListener);
            mDrawerListLeft.setOnItemClickListener(this);





        // ************************************

        participantProvider=MainActivity.participantProvider;



        Picasso picasso = getPicasso();
        myConversationList = ((AtlasConversationsRecyclerView) findViewById(R.id.conversations_list))
                .init(LoginController.layerClient, picasso)
                .setOnConversationClickListener(new AtlasConversationsAdapter.OnConversationClickListener() {
                    public void onConversationClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                        startMessagesActivity(conversation);
                    }

                    public boolean onConversationLongClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                        return false;
                    }
                })
                .addCellFactories(new TextCellFactory(),
                        new ThreePartImageCellFactory(layerClient, App.getPicasso()),
                        new SinglePartImageCellFactory( layerClient, picasso),
                        new LocationCellFactory(picasso));




        leftDrawerListener.syncState();



    }


    static class myHandler extends Handler {
        public final WeakReference<ConversationListActivity> convListActivityVars;
        public WeakReference<ViewMessagesActivity> viewMessagesActivityWeakReference;
                myHandler(ConversationListActivity conv) {
                    convListActivityVars = new WeakReference<ConversationListActivity>(conv);
             }

        public void setViewMessagesActivityWeakReference(ViewMessagesActivity messagesActivity) {
            viewMessagesActivityWeakReference = new WeakReference<ViewMessagesActivity>(messagesActivity);
        }

        @Override
        public void handleMessage (Message msg){
            ConversationListActivity conversationListActivity = convListActivityVars.get();
            if(msg.what==2){
                if(conversationListActivity.findViewById(R.id.counselor_unavailible_warning)!=null)
                    conversationListActivity.findViewById(R.id.counselor_unavailible_warning).setVisibility(View.GONE);

                if (viewMessagesActivityWeakReference != null) {
                    ViewMessagesActivity viewMessagesActivity = viewMessagesActivityWeakReference.get();
                    TextView networkErrorWarning=(TextView)viewMessagesActivity.findViewById(R.id.counselor_unavailible_warning);
                    networkErrorWarning.setText("This counselor is offline. Don't expect an immediate response!");
                    networkErrorWarning.setVisibility(View.GONE);
                }
            }else if (msg.what==3){
                if(conversationListActivity!=null)
                    conversationListActivity.findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);

                if (viewMessagesActivityWeakReference != null) {
                    ViewMessagesActivity viewMessagesActivity = viewMessagesActivityWeakReference.get();
                    TextView networkErrorWarning=(TextView)viewMessagesActivity.findViewById(R.id.counselor_unavailible_warning);
                    networkErrorWarning.setText("Network Error, please connect to the Internet!");
                    networkErrorWarning.setVisibility(View.VISIBLE);
                }
            } else {
                if (conversationListActivity.accountType == 0) {
                    String userID = (String) msg.obj;
                    ImageView v = (ImageView) conversationListActivity.findViewById(R.id.horizontal_scroll_view_counselors).findViewWithTag(userID);
                    Log.d("gray value", "gray value function must be called");
                    if (msg.what == 0) {
                        conversationListActivity.fadeImage(v, false);
                        participantProvider.getParticipant(userID).setAvailable(false);
                        Log.d("called", "called false");
                        if (viewMessagesActivityWeakReference != null) {
                            ViewMessagesActivity viewMessagesActivity = viewMessagesActivityWeakReference.get();
                            viewMessagesActivity.findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);
                        }

                    } else {

                        conversationListActivity.fadeImage(v, true);
                        participantProvider.getParticipant(userID).setAvailable(true);

                        if (viewMessagesActivityWeakReference != null) {
                            ViewMessagesActivity viewMessagesActivity = viewMessagesActivityWeakReference.get();
                            viewMessagesActivity.findViewById(R.id.counselor_unavailible_warning).setVisibility(View.GONE);
                        }
                        Log.d("called", "called true");
                    }
                }
            }
        }

    }

    public void onResume() {
        super.onResume();
        //to receive feedback about events that you have not initiated (when another person texts the authenticated user)
        //layerClient.registerEventListener(myConversationList);

        //set layer Client and Authentication Listeners to ConversationListActivity
        LoginController.authenticationListener.assignConversationListActivity(this);

    }


    public void onPause() {
        super.onPause();
        //layerClient.unregisterEventListener(myConversationList);
    }


    //enters or starts a conversation
    private void startMessagesActivity(Conversation c){

        Intent intent = new Intent(ConversationListActivity.this, ViewMessagesActivity.class);

        if(c != null) {
            intent.putExtra("conversation-id", c.getId());

        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //enters or starts a conversation
    private void startNewMessagesActivity(String counselorID){

        Intent intent = new Intent(ConversationListActivity.this, ViewMessagesActivity.class);

        intent.putExtra("counselor-id", counselorID);
        intent.putExtra("school-id",mPrefs.getString("loginSchoolObjectId", null));
        Log.d("school-id", "school-id: " + mPrefs.getString("loginSchoolObjectId", null));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    //for logout
    public void onUserDeauthenticated() {
        Log.d("Login", "Logging Out");
        mPrefs.edit().clear().apply();
        layerClient.unregisterAuthenticationListener(LoginController.authenticationListener);
        layerClient.unregisterConnectionListener(LoginController.connectionListener);

        finish();

        Intent logoutIntent = new Intent(this, MainActivity.class);

        startActivity(logoutIntent);

    }

    private boolean isNetworkAvailable(){
        App appInstance =(App)(getApplication());
        return appInstance.isNetworkAvailable();
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
//        if(accountType==2){
            //if(item.getItemId()==android.R.id.home) {
//                NavUtils.navigateUpFromSameTask(this);
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//                return true;
            //}
//        }
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
            if(accountType==0){
                images=new int[]{R.drawable.ic_logout,
                        R.drawable.ic_launcher};
            } else if(accountType==1) {
                images=new int[]{R.drawable.ic_logout,
                        R.drawable.ic_settings};
            } else if(accountType==2){
                images=new int[]{R.drawable.ic_logout,
                        R.drawable.ic_launcher};
            }
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











    // RIGHT drawer (counselor)**************************************************************
    class CounselorRightDrawerAdapter extends BaseAdapter {
        String[] options;
        Context context;
        Switch toggle;

        public CounselorRightDrawerAdapter(Context context) {
            this.context = context;
            options = mOptionsRightDrawer;
            toggle = new Switch(context);
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
                TextView tv = (TextView)row.findViewById(R.id.textView);
                tv.setText(options[position]);
                //Replace image view with another view based on option
                if(position==0) {

                //First option for counselor: enable/disable isAvailable. Checkbox
                    ImageView iv = (ImageView)row.findViewById(R.id.imageView);
                    if (iv!=null) {
                        Log.d("imageView","the Image View is "+iv.toString());

                        ViewGroup ivparent = (ViewGroup) iv.getParent();
                        int index = ivparent.indexOfChild(iv);
                        ivparent.removeView(iv);
                        ivparent.addView(toggle, index);
                    }
                    toggle.setTextOff("No");
                    toggle.setTextOn("Yes");
                    try {

                        boolean isChecked = MainActivity.participantProvider.getParticipant(myID).getIsAvailable();
                        toggle.setChecked(isChecked);
                    } catch (NullPointerException exc) {
                        Log.d("ConversationListAct","Uh oh, NullPointerException when trying to see if checked.");
                    }

                    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            MainActivity.participantProvider.getParticipant(myID).setAvailable(isChecked);

                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("userID", myID);
                            if(isChecked) {

                                ParseCloud.callFunctionInBackground("setCounselorStateToAvailable",
                                        params, new FunctionCallback<String>() {
                                            @Override
                                            public void done(String s, ParseException e) {
                                                if (e == null) {
                                                    Toast.makeText(context,
                                                            "You have been flagged as available.",
                                                            Toast.LENGTH_SHORT).show();

                                                        ComponentName receiver = new ComponentName(getApplicationContext(), LayerPushReceiver.class);
                                                        PackageManager pm = getApplicationContext().getPackageManager();

                                                        pm.setComponentEnabledSetting(receiver,
                                                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                                PackageManager.DONT_KILL_APP);


                                                } else {
                                                    Toast.makeText(context,
                                                            "Unable to change availability. " +
                                                                    "Are you connected to the internet?",
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                            }
                            else {
                            //Gray Value function should be here
                                ParseCloud.callFunctionInBackground("setCounselorStateToUnavailable",
                                        params, new FunctionCallback<String>() {
                                            @Override
                                            public void done(String s, ParseException e) {
                                                if (e==null) {
                                                    Toast.makeText(context,
                                                            "You have been flagged as unavailable.",
                                                            Toast.LENGTH_SHORT).show();

                                                    ComponentName receiver = new ComponentName(getApplicationContext(), LayerPushReceiver.class);
                                                    PackageManager pm = getApplicationContext().getPackageManager();

                                                    pm.setComponentEnabledSetting(receiver,
                                                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                            PackageManager.DONT_KILL_APP);
                                                } else {
                                                    Toast.makeText(context,
                                                            "Unable to change availability. " +
                                                                    "Are you connected to the internet?",
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                            }
                        }
                    });
                }
            } else {
                row = convertView;
            }
            Log.d("position", "position Right Drawer:" + position);
            return row;
        }
    }
    //**************************************************************












    //bitmap caching and loading********************************************************************


    public void fadeImage(ImageView v, boolean isAvailable)
    {
        if(isAvailable) {
            v.clearColorFilter();
            v.setAlpha(1.0f);
        } else {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setAlpha(0.5f);  // 128 = 0.5
        }
    }

   /* class InitDiskCacheTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void ... params) {
            synchronized (mDiskCacheLock) {
                mDiskLruCache= new DiskLruImageCache(context, "thumbnails", DISK_CACHE_SIZE, Bitmap.CompressFormat.PNG, 50);
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }
*/

/*
    public void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache as before
            mMemoryCache.put(key, bitmap);


            // Also add to disk cache
            synchronized (mDiskCacheLock) {
                // Wait while disk cache is started from background thread
                while (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                        Log.w("disk cache start error","disk cache accessed before initialized");
                    }
                }
                if (mDiskLruCache != null && mDiskLruCache.getBitmap(key) == null) {
                    mDiskLruCache.put(key, bitmap);
                }
            }
    }
*/

    //********************************************************************

    private  AlertDialog getWelcomeAlertDialog(int stringAddress){
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
