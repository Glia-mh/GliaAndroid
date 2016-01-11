package com.team.roots;

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

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.parse.FunctionCallback;
import com.parse.Parse;
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
        if(savedInstanceState==null) {
            mPrefs = getSharedPreferences("label", 0);
        }
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl", "BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc");

        // Layer Setup
        loginController = new LoginController();

        if(isNetworkAvailable())
            loginController.setLayerClient(getApplicationContext(), this);

        //Content View
        setContentView(R.layout.activity_main);


        if (getSupportActionBar()!=null) getSupportActionBar().hide();


        pager = (NonSwipeableViewPager) findViewById(R.id.login_container);
        pager.setScrollDurationFactor(2.0); //Modify transition speed!
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        networkHandler=new networkHandler(this);


        //Login if Authentication exists from last session
        if(isNetworkAvailable()) {

            if (loginController.getLayerClient().isAuthenticated()) {
                isSynced = true;
                setContentView(R.layout.loading_screen);
                loginString = loginController.getLayerClient().getAuthenticatedUserId();
                schoolObjectId=mPrefs.getString("loginSchoolObjectId", null);
                participantProvider = new ParticipantProvider();
                participantProvider.refresh(loginString,schoolObjectId, loginController);
            } else {
                populateSchoolList();
            }
        }


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
                                        j.getString("SchoolName")));
                                Log.d("MainActivity", "Successfully made JSON.");
                                Log.d("ObjectId", j.getString("objectId") + "objectId of School");
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
            progressBar.setProgress((progress/2)+50);
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
        Log.d("accounttype", "account type: " + getIntent().getIntExtra("accountTypeNumber", 0));
        mPrefs.edit().putInt("accounttype", getIntent().getIntExtra("accountTypeNumber", 0)).apply();

        //activity switch
        Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);

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
            Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
            finish();
            startActivity(intent);
        } else {
            loginController.getLayerClient().registerSyncListener(this);
        }
    }


    //Network Check
    public boolean isNetworkAvailable() {
        RootsApp rootsAppInstance=(RootsApp)(getApplication());
        return rootsAppInstance.isNetworkAvailable();
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


