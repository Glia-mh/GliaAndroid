package com.team.roots;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.atlas.AtlasConversationsList;
import com.layer.atlas.RoundImage;
import com.layer.sdk.messaging.Conversation;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class ConversationListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AtlasConversationsList.IMyEventListener {
    LoginController loginController;
    static public ParticipantProvider participantProvider;
    private AtlasConversationsList myConversationList;
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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversation);
        findViewById(R.id.listreported).setVisibility(View.GONE);
        if(!isNetworkAvailable()){
            findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);
        }


        context = this;
        mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype",0);


        Log.d("ConversationList", "Conversation List Activity recreated");
        //set layer Client and Authentication Listeners to ConversationListActivity

       // LoginController.authenticationListener.assignConversationListActivity(this);
        myID=LoginController.layerClient.getAuthenticatedUserId();

        availabilityHandler= new myHandler(this);


            getSupportActionBar().setTitle(R.string.conversations);  // provide compatibility to all the versions




        // COUNSELOR BAR*************************************************************
        LinearLayout counselorBar = (LinearLayout)findViewById(R.id.counselorbar);
        if(accountType==0) {


            //Memory for caches
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            mMemoryCache = new LruCache<String, Bitmap>(maxMemory) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
            new InitDiskCacheTask().execute();


            Participant[] participants = MainActivity.participantProvider.getCustomParticipants();
            ArrayList<View> greyedOutCounselors = new ArrayList<View>();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            for (final Participant p : participants) {

                View item = inflater.inflate(R.layout.counselor_bar_item, null, false);   // Inflate plain counselor_bar_item layout
                TextView text = (TextView) item.findViewById(R.id.counselorbartext);
                ImageView image = (ImageView) item.findViewById(R.id.counselorbarimage);
                text.setText(p.getFirstName());   // set up text



                image.setTag(p.getID());

                if (getBitmapFromCache(p.getID().toLowerCase()) == null) {
                    new LoadImage(image).execute(p.getAvatarString(), p.getID().toLowerCase());   // set up image
                } else {
                    RoundImage roundImage;
                    roundImage = new RoundImage(getBitmapFromCache(p.getID().toLowerCase()));
                    image.setImageDrawable(roundImage);
                }


                if(!p.getIsAvailable())
                    fadeImage(image, p.getIsAvailable());



                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (myConversationList.getCounselorsinConversationWith().contains(p.getID())) {
                            startMessagesActivity(myConversationList.getConversationWithCounselorId(p.getID()));
                        } else {
                            startNewMessagesActivity(p.getID());
                        }

                    }
                });

                if(!p.getIsAvailable()) greyedOutCounselors.add(item);
                else counselorBar.addView(item);
            }
            // Add greyed out counselors last
            for(View item: greyedOutCounselors) counselorBar.addView(item);
        } else {
            counselorBar.setVisibility(View.GONE);
        }

        //***********************************************************************





        // LEFT/RIGHT NAV DRAWERS********************************************* onCreate

        //Setting options for Drawers
        if(accountType==0) {
            mOptions = getResources().getStringArray(R.array.left_drawer_options);

        } else {
            mOptions= getResources().getStringArray(R.array.left_drawer_options_counselor);
            mOptionsRightDrawer=getResources().getStringArray(R.array.right_drawer_options_counselor);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);










        //right drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListRight = (ListView) findViewById(R.id.right_drawer);
        PackageManager pm = this.getPackageManager();
        if(accountType==1) {
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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerListLeft.setAdapter(new MyAdapter(this));
        if(accountType==0) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawerListRight);
        }

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

        participantProvider=MainActivity.participantProvider;

        //initialize Conversation List
        myConversationList = (AtlasConversationsList) findViewById(R.id.conversationlist);


        myConversationList.init(LoginController.layerClient, participantProvider, accountType, context);


        myConversationList.setClickListener(new AtlasConversationsList.ConversationClickListener() {
            public void onItemClick(Conversation conversation) {
                startMessagesActivity(conversation);
            }
        });




        leftDrawerListener.syncState();

        AtlasConversationsList atlasConversationsList =
                (AtlasConversationsList)findViewById(R.id.conversationlist);
        atlasConversationsList.setMyIEventListener(this);



    }

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
        LoginController.layerClient.registerEventListener(myConversationList);

        //set layer Client and Authentication Listeners to ConversationListActivity
        LoginController.authenticationListener.assignConversationListActivity(this);

    }


    public void onPause() {
        super.onPause();
        LoginController.layerClient.unregisterEventListener(myConversationList);
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
            if(accountType==0){
                images=new int[]{R.drawable.ic_logout,
                        R.drawable.ic_launcher};
            } else {
                images=new int[]{R.drawable.ic_logout,
                        R.drawable.ic_settings};
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
    class InitDiskCacheTask extends AsyncTask<Void, Void, Void> {
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

    public Bitmap getBitmapFromCache(String key) {
        if (mMemoryCache.get(key)!=null) {
            return mMemoryCache.get(key);
        } else {
            synchronized (mDiskCacheLock) {
                // Wait while disk cache is started from background thread
                while (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                        Log.w("disk cache start error","disk cache accessed before initialized");
                    }
                }

                if (mDiskLruCache != null) {
                    if(mDiskLruCache.getBitmap(key)!=null) {
                        mMemoryCache.put(key, mDiskLruCache.getBitmap(key));
                        return mDiskLruCache.getBitmap(key);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        ImageView imageView=null;
        String avatarString;
        String lowerCaseId;
        //for passing image View
        public LoadImage(ImageView imageViewLocal) {
            super();
            imageView=imageViewLocal;

        }
        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        //convert image of link to bitmap
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap=null;
            try {
                BitmapFactory.Options options= new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Rect padding=new Rect();
                padding.setEmpty();
                BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent(), padding, options);


                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 192, 192);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent(), padding, options);
                avatarString=args[0];
                lowerCaseId= args[1];
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
                Log.d("cacheString", avatarString + "cacheString");
                addBitmapToCache(lowerCaseId, image);

            }else{
                Log.d("ConversationListAct", "failed to set bitmap to image view");
            }
        }
    }
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
