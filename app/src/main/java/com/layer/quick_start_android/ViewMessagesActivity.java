package com.layer.quick_start_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesList;
import com.layer.atlas.AtlasParticipantPicker;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.atlas.DiskLruImageCache;
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


    private Context context;
    private AtlasMessagesList messagesList;
    private AtlasParticipantPicker participantPicker;
    private AtlasTypingIndicator typingIndicator;
    private AtlasMessageComposer atlasComposer;
    private Conversation conversation;
    private String counselorId=null;
    private String DRAWER_OPEN = "DrawerOpen";
    private boolean drawerOpen;

    //Image Caching
    private LruCache<String, Bitmap> mMemoryCache;
    private com.layer.atlas.DiskLruImageCache mDiskLruCache;
    private boolean mDiskCacheStarting = true;
    private final Object mDiskCacheLock = new Object();
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB



    //account type 1 is counselor
    //account type 0 is student
    //default set to 0
    private int accountType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;

        if(savedInstanceState!=null){
            drawerOpen=savedInstanceState.getBoolean(DRAWER_OPEN);
        } else {
            drawerOpen=true;
        }


        setContentView(R.layout.activity_messages_view);

        if(!isNetworkAvailable()){
            TextView networkErrorWarning=(TextView)findViewById(R.id.counselor_unavailible_warning);
            networkErrorWarning.setText("Network Error, please connect to the Internet!");
            networkErrorWarning.setVisibility(View.VISIBLE);
        }


        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype", 0);

        //if conversation does not exist set counselor Id for conversation initialization
        counselorId=getIntent().getStringExtra("counselor-id");



        //get current conversation
        Uri id = getIntent().getParcelableExtra("conversation-id");
        if(id != null)
            conversation = LoginController.layerClient.getConversation(id);


        if(counselorId==null){
            counselorId=(String)conversation.getMetadata().get("counselor.ID");
        }


        FrameLayout bioInformationDrawer=(FrameLayout)findViewById(R.id.counselor_bio_drawer);
        bioInformationDrawer.setVisibility(View.VISIBLE);

        ConversationListActivity.availabilityHandler.setViewMessagesActivityWeakReference(this);
        //Bio View
        if (accountType==0) {





            ImageView imageViewCounselor = (ImageView) findViewById(R.id.counselorbioimage);
            boolean fadeImage = false;
            Log.d("ViewMessagesAct", "ConversationListActivity.participantprovder.getPartticipant(counselorId)==" + ConversationListActivity.participantProvider.getParticipant(counselorId));


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



            if(!ConversationListActivity.participantProvider.getParticipant(counselorId).getIsAvailable()) {
                fadeImage=true;
                findViewById(R.id.counselor_unavailible_warning).setVisibility(View.VISIBLE);  //Show warning if unavailable
            }
            if(getBitmapFromCache(counselorId.toLowerCase())==null) {
                new LoadImage(imageViewCounselor, fadeImage).execute(ConversationListActivity.participantProvider.getParticipant(counselorId).getAvatarString());
            } else {
                RoundImage roundImage=new RoundImage(getBitmapFromCache(counselorId.toLowerCase()));
                imageViewCounselor.setImageDrawable(roundImage);
            }



            TextView counselorTitle = (TextView) findViewById(R.id.bioinformationtitle);
            counselorTitle.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getName());

            TextView counselorInfo = (TextView) findViewById(R.id.bioinformation);
            counselorInfo.setText(ConversationListActivity.participantProvider.getParticipant(counselorId).getBio());
        } else {
            View bioNavDrawer = findViewById(R.id.counselor_bio_drawer);
            ((ViewManager)bioNavDrawer.getParent()).removeView(bioNavDrawer);
        }


        //set message list
        messagesList = (AtlasMessagesList) findViewById(R.id.messageslist);
        messagesList.init(LoginController.layerClient, ConversationListActivity.participantProvider, accountType, this);
        messagesList.setConversation(conversation);





        //automatically set to hidden
        //a view with dynamic filtering of a list that allows you to add participants
        participantPicker = (AtlasParticipantPicker) findViewById(R.id.participantpicker);
        String[] currentUser = {LoginController.layerClient.getAuthenticatedUserId()};
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
        atlasComposer.init(LoginController.layerClient, conversation);
        atlasComposer.setListener(new AtlasMessageComposer.Listener() {
            //if returns false means the message will not send and participants not entered
            //in new conversation
            public boolean beforeSend(Message message) {
                if (conversation == null) {
                    //does not include sender only reciever
                    String[] participants = {counselorId, "1"};

                    if (participants.length > 0) {


                        Metadata counselor = Metadata.newInstance();
                        counselor.put("name", ConversationListActivity.participantProvider.getParticipant(participants[0]).getFirstName());
                        counselor.put("ID", ConversationListActivity.participantProvider.getParticipant(participants[0]).getID());
                        counselor.put("avatarString", ConversationListActivity.participantProvider.getParticipant(participants[0]).getAvatarString());

                        Metadata student = Metadata.newInstance();
                        student.put("name", "");
                        student.put("ID", LoginController.layerClient.getAuthenticatedUserId());
                        student.put("avatarString", getVanilliconLink());
                        //set MetaData to Conversations
/*                        HashMap<String,HashMap<String, String>> metadataMap=new HashMap<String, HashMap<String, String>>();
                        HashMap<String, String> counselor=new HashMap<String, String>();
                        HashMap<String, String> student=new HashMap<String, String>();*/

                        Metadata metadataConv = Metadata.newInstance();


                        metadataConv.put("counselor", counselor);
                        metadataConv.put("student", student);


                        conversation = LoginController.layerClient.newConversation(participants);

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


        DrawerLayout dl = (DrawerLayout)findViewById(R.id.view_messages_drawer_layout);
        // set bio drawer listener
        dl.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) { }
            @Override
            public void onDrawerOpened(View view) {
                drawerOpen=true;
            }
            @Override
            public void onDrawerClosed(View view) {
                drawerOpen=false;
            }
            @Override
            public void onDrawerStateChanged(int i) {}
        });




        if (accountType==0 && mPrefs.getBoolean("firstTimeStudentOnViewMessagesAct", true)) {
            AlertDialog welcomeAlertDialog = getWelcomeAlertDialog(R.string.dialog_welcome_student_view_messages_act);
            welcomeAlertDialog.show();
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putBoolean("firstTimeStudentOnViewMessagesAct", false).apply();
            dl.openDrawer(GravityCompat.END);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(DRAWER_OPEN, drawerOpen);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onResume() {
        super.onResume();
        LoginController.layerClient.registerEventListener(messagesList);
    }

    protected void onPause(){
        super.onPause();
        LoginController.layerClient.unregisterEventListener(messagesList);
    }

    private boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }

    public String getVanilliconLink() {
        //load Vanillicon
        byte[] bytesofTest = LoginController.layerClient.getAuthenticatedUserId().getBytes();
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
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ConversationListAct", "failed to decode bitmap");
            }
            return bitmap;
        }

        //set image view to bitmap
        protected void onPostExecute(Bitmap image ) {

            if(image != null){
                    //Log.d("caching","caching");
                    String upperCaseData;
                    if(accountType==0) {
                        upperCaseData = (String) conversation.getMetadata().get("counselor.ID");
                    }else {
                        upperCaseData = (String) conversation.getMetadata().get("student.ID");
                    }
                    addBitmapToCache(upperCaseData.toLowerCase(),image);
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


    public void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache
        mMemoryCache.put(key, bitmap);


        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && mDiskLruCache.getBitmap(key) == null) {
                mDiskLruCache.put(key, bitmap);
            }
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

