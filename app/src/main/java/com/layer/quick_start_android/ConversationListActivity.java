package com.layer.quick_start_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.atlas.AtlasConversationsList;
import com.layer.atlas.RoundImage;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class ConversationListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    static LayerClient layerClient;
    LoginController loginController;
    static public ParticipantProvider participantProvider;
    private AtlasConversationsList myConversationList;
    static Context context;

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
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    //account type 1 is counselor
    //account type 0 is student
    //default set to 0
    private int accountType;
    @Override
    protected void onStart() {
        super.onStart();
       // super.onCreate(savedInstanceState);
        context = this;
        mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype",0);



        //set layer Client and Authentication Listeners to ConversationListActivity
        loginController = new LoginController();
        loginController.authenticationListener.assignConversationListActivity(this);
        layerClient = loginController.getLayerClient();

        setContentView(R.layout.activity_list_conversation);




        // COUNSELOR BAR*************************************************************
        LinearLayout counselorBar = (LinearLayout)findViewById(R.id.counselorbar);
        if(accountType==0) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory;
            //if(savedInstanceState==null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };

                new InitDiskCacheTask().execute();
            //}

            Participant[] participants = MainActivity.participantProvider.getCustomParticipants();
            ArrayList<View> greyedOutCounselors = new ArrayList<View>();

            for (final Participant p : participants) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View item = inflater.inflate(R.layout.counselor_bar_item, null, false);   // Inflate plain counselor_bar_item layout
                TextView text = (TextView) item.findViewById(R.id.counselorbartext);
                ImageView image = (ImageView) item.findViewById(R.id.counselorbarimage);
                text.setText(p.getFirstName());   // set up text
                boolean greyImage = false;
                if(p.getIsAvailable()==false) greyImage=true;
                if (getBitmapFromCache(p.getID().toLowerCase()) == null) {
                    new LoadImage(image, greyImage).execute(p.getAvatarString(), p.getID().toLowerCase());   // set up image
                } else {
                    RoundImage roundImage;
                    roundImage = new RoundImage(getBitmapFromCache(p.getID().toLowerCase()));
                    image.setImageDrawable(roundImage);
                    Log.d("cached", "cachedcounselorbarpic");
                }


                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (myConversationList.getCounselorsinConversationWith().contains(p.getID())) {
                            Log.d("ConversationListAct", "counselorsinconversationwith" + myConversationList.getCounselorsinConversationWith().contains(p.getID()));
                            startMessagesActivity(myConversationList.getConversationWithCounselorId(p.getID()));
                        } else {
                            startNewMessagesActivity(p.getID());
                        }

                    }
                });
                if(p.getIsAvailable()==false) greyedOutCounselors.add(item);
                else counselorBar.addView(item);
            }
            // Add greyed out counselors last
            for(View item: greyedOutCounselors) counselorBar.addView(item);
        } else {
            counselorBar.setVisibility(View.GONE);
        }

        //***********************************************************************

        //Setting options for Drawers
        if(accountType==0) {
            mOptions = getResources().getStringArray(R.array.left_drawer_options);
            mOptionsRightDrawer = getResources().getStringArray(R.array.right_drawer_options);
        } else {
            mOptions= getResources().getStringArray(R.array.left_drawer_options_counselor);
            mOptionsRightDrawer=getResources().getStringArray(R.array.right_drawer_options_counselor);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // LEFT/RIGHT NAV DRAWERS*********************************************

        //Left Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerListLeft.setAdapter(new MyAdapter(this));
        // Set the list's click listener
        //mDrawerListLeft.setOnItemClickListener(this);

        leftDrawerListener = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawer) {
                //Toast.makeText(context, "open", Toast.LENGTH_SHORT).show();
                DrawerLayout.LayoutParams drawerParams = (DrawerLayout.LayoutParams)drawer.getLayoutParams();
                if(drawerParams.gravity==Gravity.LEFT)mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }

            public void onDrawerClosed(View drawer) {
                //Toast.makeText(context, "closed", Toast.LENGTH_SHORT).show();
            }
        };

        mDrawerLayout.setDrawerListener(leftDrawerListener);
        mDrawerListLeft.setOnItemClickListener(this);

        //right drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListRight = (ListView) findViewById(R.id.right_drawer);

        // Set the adapter for the list view
        if(accountType==1) {
            Log.d("attempt1","attempt1");
            mDrawerListRight.setAdapter(new CounselorRightDrawerAdapter(this));
        }




        //try to uncomment and see result later ************************************
        // if (savedInstanceState==null){

        participantProvider=MainActivity.participantProvider;

        //initialize Conversation List
        myConversationList = (AtlasConversationsList) findViewById(R.id.conversationlist);
        Log.d("I have reach here","I have reached here");
        synchronized (mDiskCacheLock) {
            myConversationList.init(layerClient, participantProvider, accountType, context);
        }
        myConversationList.setClickListener(new AtlasConversationsList.ConversationClickListener() {
            public void onItemClick(Conversation conversation) {
                startMessagesActivity(conversation);
            }
        });

        //to recieve feedback about events that you have not initiated (when another person texts the authenticated user)
        layerClient.registerEventListener(myConversationList);

        // Show the welcome dialog if first time on
        if (accountType==0 && mPrefs.getString("firstTimeStudent", "YARP").equals("YARP")) {  //Yaarp.....
            AlertDialog welcomeAlertDialog = getWelcomeAlertDialog(R.string.dialog_welcome_student);
            welcomeAlertDialog.show();
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("firstTimeStudent", "NARP").commit(); //Naarp.......
        } else if (accountType==1 && mPrefs.getString("firstTimeCounselor", "YARP").equals("YARP")) {  //Yaarp.....
            AlertDialog welcomeAlertDialog = getWelcomeAlertDialog(R.string.dialog_welcome_counselor);
            welcomeAlertDialog.show();
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("firstTimeCounselor", "NARP").commit();
        }

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




        //to start a new conversation with + button may be used on counselor side eventually
            /*View newconversation = findViewById(R.id.newconversation);
            newconversation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startMessagesActivity(null);
                }
            });*/


        // }





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



    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        leftDrawerListener.syncState();
    }


    // For when a left nav drawer item is clicked
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if (mOptions[position].equals("Logout")) {
            setContentView(R.layout.loading_screen);
            getSupportActionBar().hide();
            TextView loggingoutintext=(TextView)findViewById(R.id.loginlogoutinformation);
            loggingoutintext.setText("Logging Out...");
            loginController.logout();
        } else if (mOptions[position].equals("Settings")) {
            //go to settings (right nav drawer)
            DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
            dl.closeDrawer(Gravity.LEFT);
            dl.openDrawer(Gravity.RIGHT);
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
        if(c != null) {
            intent.putExtra("conversation-id", c.getId());
            //intent.putExtra("counselor.ID", c.getMetadata().get("counselor.ID").toString());
            //Log.d("ConversatonListActivity",c.getMetadata().get("counselor.ID").toString());
        }
        startActivity(intent);
    }

    //enters or starts a conversation
    private void startNewMessagesActivity(String counselorID){
        Intent intent = new Intent(ConversationListActivity.this, ViewMessagesActivity.class);
        intent.putExtra("counselor-id",counselorID);
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



        if (leftDrawerListener.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Left drawer
    class MyAdapter extends BaseAdapter {
        String[] options;
        int[] images = new int[]{R.drawable.ic_logout,
                R.drawable.ic_settings,
                R.drawable.ic_launcher,
                R.drawable.ic_get_involved};
        Context context;

        public MyAdapter(Context context) {
            this.context = context;
            options = mOptions;
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

    // RIGHT drawer (counselor)
    class CounselorRightDrawerAdapter extends BaseAdapter {
        String[] options;
        Context context;
        CheckBox availableCheckBox;

        public CounselorRightDrawerAdapter(Context context) {
            this.context = context;
            options = mOptionsRightDrawer;
            availableCheckBox = new CheckBox(context);
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
                if(position==0) { //First option for counselor: enable/disable isAvailable. Checkbox
                    ImageView iv = (ImageView)row.findViewById(R.id.imageView);
                    if (iv!=null) {
                        Log.d("imageView","the Image View is "+iv.toString());

                        ViewGroup ivparent = (ViewGroup) iv.getParent();
                        int index = ivparent.indexOfChild(iv);
                        ivparent.removeView(iv);
                        ivparent.addView(availableCheckBox, index);
                    }
                    try {
                        Log.d("ConversationListAct","MainActivity.myID=="+MainActivity.myID);
                        Log.d("ConversationListAct","MainActivity.participantProvider=="+MainActivity.participantProvider);
                        Log.d("ConversationListAct","MainActivity.participantProvider.getParticipant(MainActivity.myID)=="+MainActivity.participantProvider.getParticipant(MainActivity.myID));
                        boolean isChecked = MainActivity.participantProvider.getParticipant(MainActivity.myID).getIsAvailable();
                        availableCheckBox.setChecked(isChecked);
                    } catch (NullPointerException exc) {
                        Log.d("ConversationListAct","Uh oh, NullPointerException when trying to see if checked.");
                    }

                    availableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            MainActivity.participantProvider.getParticipant(MainActivity.myID).setAvailable(isChecked);

                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("userID", MainActivity.myID);
                            if(isChecked) {
                                ParseCloud.callFunctionInBackground("setCounselorStateToAvailable",
                                        params, new FunctionCallback<String>() {
                                            @Override
                                            public void done(String s, ParseException e) {
                                                if (e == null) {
                                                    Toast.makeText(context,
                                                            "You have been flagged as available.",
                                                            Toast.LENGTH_SHORT).show();
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
                                ParseCloud.callFunctionInBackground("setCounselorStateToUnavailable",
                                        params, new FunctionCallback<String>() {
                                            @Override
                                            public void done(String s, ParseException e) {
                                                if (e==null) {
                                                    Toast.makeText(context,
                                                            "You have been flagged as unavailable.",
                                                            Toast.LENGTH_SHORT).show();
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
        //if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, bitmap);


            // Also add to disk cache
            synchronized (mDiskCacheLock) {
                // Wait while disk cache is started from background thread
                while (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (mDiskLruCache != null && mDiskLruCache.getBitmap(key) == null) {
                    mDiskLruCache.put(key, bitmap);
                }
            }
        //}
    }



    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        ImageView imageView=null;
        String avatarString;
        String lowerCaseId;
        //for passing image View
        public LoadImage(ImageView imageViewLocal, boolean grayOut) {
            super();
            imageView=imageViewLocal;
            if(grayOut) fadeImage(imageView);

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
                //int imageHeight = options.outHeight;
                //int imageWidth = options.outWidth;
                //String imageType = options.outMimeType;

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

        public void fadeImage(ImageView v)
        {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setAlpha(128);   // 128 = 0.5
        }
    }


    private  AlertDialog getWelcomeAlertDialog(int stringAddress){
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(stringAddress)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothin' cuz we don't gotta
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
