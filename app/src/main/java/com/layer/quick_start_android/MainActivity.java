package com.layer.quick_start_android;

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
import com.parse.Parse;
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
        getSupportActionBar().hide();
        mPrefs= getSharedPreferences("label", 0);

        //Parse setup
        Parse.initialize(this, "pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl", "BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc");
        participantProvider  = new ParticipantProvider();
        participantProvider.refresh();



        setContentView(R.layout.activity_main);

    }


    @Override
    protected void onResume() {

        super.onResume();

        // Layer Setup
        loginController = new LoginController();
        loginController.setLayerClient(getApplicationContext(), this);


        //get accounttype
        accountType = mPrefs.getInt("accounttype", 0);


        TextView textViewCounselorLogin = (TextView) findViewById(R.id.counselorlogin);

        if (accountType == 0) {

            //UI setup student login
            findViewById(R.id.counselor_login_indicator).setVisibility(View.GONE);
            findViewById(R.id.login_cr_logo).setVisibility(View.VISIBLE);

            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.GONE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.GONE);
            findViewById(R.id.loginedittext).setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) findViewById(R.id.loginbutton).getLayoutParams();
            buttonParams.addRule(RelativeLayout.BELOW, R.id.loginedittext);
            findViewById(R.id.loginbutton).setLayoutParams(buttonParams);

            textViewCounselorLogin.setText("Counselor Login");

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

            //UI setup counselor login
            // set counselor login indicator to be visible and cr logo gone
            findViewById(R.id.counselor_login_indicator).setVisibility(View.VISIBLE);
            findViewById(R.id.login_cr_logo).setVisibility(View.GONE);

            // set counselor login fields to visible and student's to gone
            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.VISIBLE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.VISIBLE);
            findViewById(R.id.loginedittext).setVisibility(View.GONE);

            //set login button to be below @id+/counselor_login_edittext_password
            RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) findViewById(R.id.loginbutton).getLayoutParams();
            buttonParams.addRule(RelativeLayout.BELOW, R.id.counselor_login_edittext_password);
            findViewById(R.id.loginbutton).setLayoutParams(buttonParams);

            // option of selecting student login.
            textViewCounselorLogin.setText("Student Login");

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginEditText = (EditText) findViewById(R.id.loginedittext);
                loginString = loginEditText.getText().toString().trim();
                loginEditText.setText("");

                //login validation student
                if (accountType == 0) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("userID", loginString);
                    ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String s, ParseException e) {
                            if (s.equals("valid")) {
                                setContentView(R.layout.loading_screen);
                                TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                                loggingoutintext.setText("Loading...");
                                loginController.login(loginString);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid ID.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                //login validation counselor
                } else {
                    TextView usernameEditText = (TextView) findViewById(R.id.counselor_login_edittext_username);
                    String username = usernameEditText.getText().toString();
                    TextView pwEditText = (TextView) findViewById(R.id.counselor_login_edittext_password);
                    String password = pwEditText.getText().toString();

                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {

                                loginString=user.getObjectId();
                                Log.d("user.getObjectId","user.getObjectId="+loginString);

                                setContentView(R.layout.loading_screen);

                                TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                                loggingoutintext.setText("Loading...");
                                loginController.login(user.getObjectId());
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });


        //Login if Authentication exists from last session

        if (loginController.getLayerClient().isAuthenticated()) {
            isSynced=true;
            setContentView(R.layout.loading_screen);
            TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
            loggingoutintext.setText("Loading...");
            loginString=loginController.getLayerClient().getAuthenticatedUserId();
            loginController.login(loginString);
        }
    }



    public void onSyncProgress(LayerClient layerClient, int progress){
    }



    //Called before syncing with the Layer servers
    public void onBeforeSync(LayerClient layerClient) {

    }

    //Called after syncing with the Layer servers
    public void onAfterSync(LayerClient layerClient) {
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
        if (isSynced){
            Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
            intent.putExtra("mUserId", loginString);
            loginController.connectionListener.setReceive(true);
            finish();
            startActivity(intent);

        } else {
            loginController.getLayerClient().registerSyncListener(this);
        }
    }
}