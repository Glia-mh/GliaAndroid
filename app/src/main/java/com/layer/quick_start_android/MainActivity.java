package com.layer.quick_start_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LayerSyncListener{



    String loginString;
    LoginController loginController;
    static public ParticipantProvider participantProvider;


    //global application memory
    SharedPreferences mPrefs;
    //account type 0 is student
    //account type 1 is counselor
    //default set to 0
    private int accountType;

    boolean isSynced=false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        mPrefs= getSharedPreferences("label", 0);



    }

    private boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }
    @Override
    protected void onResume() {
        // Layer Setup
        loginController = new LoginController();

        if(isNetworkAvailable())
            loginController.setLayerClient(getApplicationContext(), this);


        super.onResume();

        setContentView(R.layout.activity_main);


        //get accounttype
        accountType = mPrefs.getInt("accounttype", 0);


        TextView textViewCounselorLogin = (TextView) findViewById(R.id.counselorlogin);

        if (accountType == 0) {
            //Log.d("is counselor","is counslor login indicator null :"+findViewById(R.id.counselor_login_indicator) + " "+ findViewById(R.id.root_main_activity_layout));
            //UI setup student login
            //findViewById(R.id.counselor_login_indicator).setVisibility(View.GONE);
            //findViewById(R.id.login_cr_logo).setVisibility(View.VISIBLE);

            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.GONE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.GONE);
            findViewById(R.id.loginedittext).setVisibility(View.VISIBLE);

            //RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) findViewById(R.id.loginbutton).getLayoutParams();
            //buttonParams.addRule(RelativeLayout.BELOW, R.id.loginedittext);
            //findViewById(R.id.loginbutton).setLayoutParams(buttonParams);

            textViewCounselorLogin.setText(R.string.c_login);

            //student to counselor switch
            textViewCounselorLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putInt("accounttype", 1).commit();
                    onResume();
                }
            });
        } else {
            //Log.d("is counselor","is counslor login indicator null :"+findViewById(R.id.counselor_login_indicator) + " "+ findViewById(R.id.root_main_activity_layout));
            //UI setup counselor login
            // set counselor login indicator to be visible and cr logo gone
            //findViewById(R.id.counselor_login_indicator).setVisibility(View.VISIBLE);
            //findViewById(R.id.login_cr_logo).setVisibility(View.GONE);

            // set counselor login fields to visible and student's to gone
            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.VISIBLE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.VISIBLE);
            findViewById(R.id.loginedittext).setVisibility(View.GONE);

            //set login button to be below @id+/counselor_login_edittext_password
            //RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) findViewById(R.id.loginbutton).getLayoutParams();
            //buttonParams.addRule(RelativeLayout.BELOW, R.id.counselor_login_edittext_password);
            //findViewById(R.id.loginbutton).setLayoutParams(buttonParams);

            // option of selecting student login.
            textViewCounselorLogin.setText(R.string.s_login);

            textViewCounselorLogin.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putInt("accounttype", 0).commit();
                    onResume();
                }
            });
        }


        //Login Button
        Button loginButton = (Button) findViewById(R.id.loginbutton);
        final MainActivity mainActivity=this;
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginEditText = (EditText) findViewById(R.id.loginedittext);
                loginString = loginEditText.getText().toString().trim();
                loginEditText.setText("");

                if(isNetworkAvailable()) {
                    if(loginController.getLayerClient()==null) {
                        loginController.setLayerClient(getApplicationContext(), mainActivity);
                    }

                    participantProvider  = new ParticipantProvider();
                    participantProvider.refresh();

                    //login validation student
                    if (accountType == 0) {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("userID", loginString);
                        setContentView(R.layout.loading_screen);
                        ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                            @Override
                            public void done(String s, ParseException e) {
                                if (s.equals("valid")) {



                                    Log.d("callback","valid user id");
                                    TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                                    loggingoutintext.setText("Loading...");
                                    if(mPrefs.getString("firstTimeStudent", "1").equals("1")){
                                        loggingoutintext.setText("First Login May Take Time. Loading...");
                                    }
                                    loginController.login(loginString);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Invalid ID.", Toast.LENGTH_SHORT).show();
                                    onResume();
                                }

                            }
                        });

                        //login validation counselor
                    } else {
                        TextView usernameEditText = (TextView) findViewById(R.id.counselor_login_edittext_username);
                        String username = usernameEditText.getText().toString();
                        TextView pwEditText = (TextView) findViewById(R.id.counselor_login_edittext_password);
                        String password = pwEditText.getText().toString();
                        setContentView(R.layout.loading_screen);
                        ParseUser.logInInBackground(username, password, new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {

                                    loginString = user.getObjectId();
                                    Log.d("user.getObjectId", "user.getObjectId=" + loginString);



                                    TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);

                                    loggingoutintext.setText("Loading...");
                                    if(mPrefs.getString("firstTimeCounselor", "1").equals("1")){
                                        loggingoutintext.setText("First Login May Take Time. Loading...");
                                    }
                                    loginController.login(user.getObjectId());
                                } else {

                                    Toast.makeText(getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                    onResume();
                                }
                            }
                        });

                    }
                } else {
                    getWelcomeAlertDialog(R.string.no_internet_connection).show();
                }
            }
        });


        //Login if Authentication exists from last session
        if(isNetworkAvailable()) {
            if (loginController.getLayerClient().isAuthenticated()) {
                isSynced = true;
                setContentView(R.layout.loading_screen);

                participantProvider = new ParticipantProvider();
                participantProvider.refresh();

                TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                loggingoutintext.setText("Loading...");
                loginString = loginController.getLayerClient().getAuthenticatedUserId();
                loginController.login(loginString);
            }
        }
    }



    public void onSyncProgress(LayerClient layerClient, SyncType syncType, int progress){
    }



    //Called before syncing with the Layer servers
    public void onBeforeSync(LayerClient layerClient, SyncType syncType) {

    }

    //Called after syncing with the Layer servers
    public void onAfterSync(LayerClient layerClient, SyncType syncType) {
        Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
        intent.putExtra("mUserId", loginString);
        finish();
        startActivity(intent);
        loginController.getLayerClient().unregisterSyncListener(this);
    }


    //Captures any errors with syncing
    public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {
    }


    public void onUserAuthenticated(){
        Log.d("onUserAuthenticated","onUserAuthenticated");
        if (isSynced){
            Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
            intent.putExtra("mUserId", loginString);

            //needed to avoid future calls to onUserAuthenticated when phone disconnects
            LoginController.connectionListener.setReceive(true);
            finish();
            startActivity(intent);


        } else {
            loginController.getLayerClient().registerSyncListener(this);
        }
    }


    private AlertDialog getWelcomeAlertDialog(int stringAddress){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringAddress)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
}