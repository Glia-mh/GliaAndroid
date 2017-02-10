package com.team.r00ts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LayerSyncListener{



    String loginString;
    private String schoolObjectId;
    private LoginController loginController;
    static public ParticipantProvider participantProvider;


    //global application memory
    public static SharedPreferences mPrefs;

    public static final String PREFS_KNOWN = "prefs known";
    public static final String SCHOOL_NAME = "school";
    public static final String SCHOOL_OBJECT_ID = "school obj id";
    public static final String ACCOUNT_TYPE_NUMBER = "account type number";


    //same thing as is authenticated
    boolean isSynced=false;

    public NonSwipeableViewPager pager;
    public static List<School> schools;
    public static Handler networkHandler;






    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            if (getSupportActionBar() != null) getSupportActionBar().hide();

            //splash screen
            setContentView(R.layout.loading_screen);

            //Sets up services

            //General Setup
            mPrefs = getSharedPreferences("label", 0);



                    // Layer Setup
                    loginController = new LoginController();

                    if (isNetworkAvailable())




                    //Login if Authentication exists from last session
                    if (isNetworkAvailable()) {
                        loginController.setLayerClient(getApplicationContext(), this);
                        if (loginController.getLayerClient().isAuthenticated()) {
                            isSynced = true;
                            loginString = loginController.getLayerClient().getAuthenticatedUser().getUserId();
                            schoolObjectId = mPrefs.getString("loginSchoolObjectId", null);
                            participantProvider = new ParticipantProvider();
                            refresh(loginString, schoolObjectId, loginController);
                        } else {

                            createfirstAuthUI();
                        }


                    } else {
                        createfirstAuthUI();
                    }



        }

    }



    public  void refresh(final String loginString, String schoolObjectId, final LoginController loginController) {
        Log.d("ParticipantProvider", "refresh called.");

        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run
        //eventually mdHash it and add to participants provider--or may not be needed because
        // participantMap.put("",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));

        final List<Participant> participants = new ArrayList<Participant>();

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("schoolObjectId", schoolObjectId);
        ParseCloud.callFunctionInBackground("getCounselors", params, new FunctionCallback<ArrayList<String>>() {
            public void done(ArrayList<String> returned, ParseException e) {
                if (e == null) {
                    for (String obj : returned) {
                        Log.d("MainActivity", "Returned string from cloud function is: " + obj);
                        try {
                            JSONObject j = new JSONObject(obj);
                            participants.add(new Participant(j.getString("name"),
                                    j.getString("objectId"), j.getString("photoURL"),
                                    j.getString("bio"), j.getBoolean("isAvailable"), Integer.parseInt(j.getString("counselorType"))));
                            Log.d("MainActivity", "Successfully made JSON.");
                            Log.d("ObjectId", j.getString("objectId") + "objectId of Counselor");
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                            Log.d("MainActivity", "Couldn't convert string to JSON.");
                        }

                    }
                    participantProvider.refresh(participants);

                    loginController.login(loginString);

                }


            }
        });
    }




    public void createfirstAuthUI(){
        //view for non-existent pre-authentication
        setContentView(R.layout.activity_main);



        pager = (NonSwipeableViewPager) findViewById(R.id.login_container);
        pager.setScrollDurationFactor(2.0);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        populateSchoolList();
        networkHandler = new networkHandler(this);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }




    public void populateSchoolList() {
        if(schools==null) {
            Log.d("populate called", "populate called");
            final HashMap<String, Object> params = new HashMap<String, Object>();
            final ArrayList<School> schoolsLocal = new ArrayList<>();
            Log.d("I am here", "I am here");
            ParseCloud.callFunctionInBackground("getSchools", params, new FunctionCallback<ArrayList<String>>() {
                public void done(ArrayList<String> returned, ParseException e) {

                    if (e == null) {
                        Log.d("working", "working");
                        //synchronized (mSchoolListLock) {
                        for (String obj : returned) {
                            Log.d("MainActivity", "Returned string from cloud function is: " + obj);
                            try {
                                JSONObject j = new JSONObject(obj);
                                schoolsLocal.add(new School(j.getString("objectId"),
                                        j.getString("SchoolName"), j.getString("SchoolEmails")));

                            } catch (JSONException exception) {
                                exception.printStackTrace();
                                Log.d("MainActivity", "Couldn't convert string to JSON.");
                            }

                        }

                        setSchools(schoolsLocal);
                        MyPagerAdapter adapter = (MyPagerAdapter) pager.getAdapter();
                        SchoolSelectFragment schoolSelectFragment = (SchoolSelectFragment) adapter.getItem(1);
                        schoolSelectFragment.populateSchoolListUI();
                    } else {
                        Log.d("failed", "failed" + e);
                    }
                }
            });
        }
    }



    public void onSyncProgress(LayerClient layerClient, SyncType syncType, int progress){
        try {
            ProgressBar progressBar=(ProgressBar)pager.getChildAt(pager.getChildCount() - 1).findViewById(R.id.login_progress);
            progressBar.setProgress((int)((progress*0.4)+60));
        } catch (NullPointerException e) {
            Log.d("null","progress bar not updated, not on Main Activity view");
        }
    }



    //Called before syncing with the Layer servers
    public void onBeforeSync(LayerClient layerClient, SyncType syncType) {

    }

    //Called after syncing with the Layer servers
    public void onAfterSync(LayerClient layerClient, SyncType syncType) {
        schoolObjectId=getIntent().getStringExtra("schoolobjectid");
        //permanent application memory population
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("loginSchoolObjectId", schoolObjectId).apply();
        if(getIntent().getIntExtra("accountTypeNumber", 0)==1) //ensure only counselors have the email
            mEditor.putString("schoolemail", getIntent().getStringExtra("schoolemail")).apply();
        else
            mEditor.putString("schoolemail", null);
        Log.d("accounttype", "account type: " + getIntent().getIntExtra("accountTypeNumber", 0));
        mPrefs.edit().putInt("accounttype", getIntent().getIntExtra("accountTypeNumber", 0)).apply();
        //activity switch

        Intent intent;
        intent= new Intent(getApplicationContext(), ConversationListActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);

        loginController.getLayerClient().unregisterSyncListener(this);
    }
    static class networkHandler extends Handler {
        public final WeakReference<MainActivity> mainActivityVars;
        networkHandler(MainActivity ma) {
                mainActivityVars = new WeakReference<MainActivity>(ma);

        }

        @Override
        public void handleMessage (Message msg){
            //on network change if already authenticated login
            if(schools==null) {
                MainActivity mainActivity = mainActivityVars.get();
                mainActivity.populateSchoolList();
            }

        }

    }
    private class MyPagerAdapter extends FragmentPagerAdapter {
        GetStartedFragment getStartedFragment;
        SchoolSelectFragment schoolSelectFragment;
        AccountTypeSelectFragment accountTypeSelectFragment;
        LoginFragment loginFragment;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            getStartedFragment=new GetStartedFragment();
            schoolSelectFragment=new SchoolSelectFragment();
            accountTypeSelectFragment=new AccountTypeSelectFragment();
            loginFragment=new LoginFragment();
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return getStartedFragment;
                case 1: return schoolSelectFragment;
                case 2: return accountTypeSelectFragment;
                case 3: return loginFragment;
                default: return getStartedFragment;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    //Captures any errors with syncing
    public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {
    }


    public void onUserAuthenticated(){
        Log.d("onUserAuthenticated", "onUserAuthenticated");
        if (isSynced){
            Intent intent;
//            if(mPrefs.getInt("accounttype",0)==2){
//                intent=new Intent(this, ReportedIDListActivity.class);
//            } else {
            intent= new Intent(this, ConversationListActivity.class);
            //}
            finish();
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            loginController.getLayerClient().registerSyncListener(this);
        }
    }

    //Layer fails auth because it makes a mistake
    public void authFailLayer(){
        Toast.makeText(this, "Our servers messed up! :( Try again.", Toast.LENGTH_SHORT).show();
        MyPagerAdapter myPagerAdapter=(MyPagerAdapter)pager.getAdapter();
        LoginFragment loginFragment=(LoginFragment)myPagerAdapter.getItem(3);
        loginFragment.resetWithOutContentClear();
    }
    //Network Check
    public boolean isNetworkAvailable() {
        App appInstance =(App)(getApplication());
        return appInstance.isNetworkAvailable();
    }


    public AlertDialog getWelcomeAlertDialog(int stringAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringAddress)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }


    public List<School> getSchools(){
        return schools;
    }
    public void setSchools(ArrayList<School> schoolsLoaded) {
        schools=schoolsLoaded;
    }
    public LoginController getLoginController(){
        return loginController;
    }


    }


