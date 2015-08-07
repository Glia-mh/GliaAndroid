package com.layer.quick_start_android;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements LayerSyncListener{


    //other variables
    static Context context;
    String loginString;
    LoginController loginController;
    static public ParticipantProvider participantProvider;


    //account type 1 is counselor
    //account type 0 is student
    //default set to 0
    private int accountType;
    public static String myID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Parse.initialize(this, "pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl", "BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc");
        getSupportActionBar().hide();

        // Create a LayerClient object no UserId included
        loginController = new LoginController();
        loginController.setLayerClient(context, this);
        setContentView(R.layout.activity_main);

    }


    //Called before syncing with the Layer servers
    public void onBeforeSync(LayerClient layerClient) {
    }

    //Called after syncing with the Layer servers
    public void onAfterSync(LayerClient layerClient) {

        //Suggestions from Layer for 50 or more conversations--not implemented as of now
        //because it does not seem neccessary

        //place this in a different thread and set a timer for 10 seconds before hand
        //and check for calling onBeforeSync do not execute rest of thread if onBeforeSync is called abort thread
        //run a final thread that unregister sync listener


        Intent intent = new Intent(context, ConversationListActivity.class);
        intent.putExtra("mUserId", loginString);
        finish();
        startActivity(intent);
        loginController.getLayerClient().unregisterSyncListener(this);
    }


    //Captures any errors with syncing
    public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {
    }

    @Override
    protected void onResume(){

        super.onResume();
        Log.d("MainActivity", "onResume executed.");

        final SharedPreferences mPrefs = getSharedPreferences("label", 0);
        accountType = mPrefs.getInt("accounttype",0);


        final TextView textViewCounselorLogin=(TextView)findViewById(R.id.counselorlogin);

        if(accountType==0) {   // In student login
            // set counselor login indicator to be gone and cr logo visible
            findViewById(R.id.counselor_login_indicator).setVisibility(View.GONE);
            findViewById(R.id.login_cr_logo).setVisibility(View.VISIBLE);

            // set student login field to visible and counselor's to gone
            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.GONE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.GONE);
            findViewById(R.id.loginedittext).setVisibility(View.VISIBLE);

            //set login button to be below @id+/loginedittext
            RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams)findViewById(R.id.loginbutton).getLayoutParams();
            buttonParams.addRule(RelativeLayout.BELOW, R.id.loginedittext);
            findViewById(R.id.loginbutton).setLayoutParams(buttonParams);

            // option of selecting counselor login
            textViewCounselorLogin.setText("Counselor Login");
            textViewCounselorLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putInt("accounttype", 1).commit();
                    onResume();
                }
            });
        }
        else {   // In counselor login
            // set counselor login indicator to be visible and cr logo gone
            findViewById(R.id.counselor_login_indicator).setVisibility(View.VISIBLE);
            findViewById(R.id.login_cr_logo).setVisibility(View.GONE);

            // set counselor login fields to visible and student's to gone
            findViewById(R.id.counselor_login_edittext_username).setVisibility(View.VISIBLE);
            findViewById(R.id.counselor_login_edittext_password).setVisibility(View.VISIBLE);
            findViewById(R.id.loginedittext).setVisibility(View.GONE);

            //set login button to be below @id+/counselor_login_edittext_password
            RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams)findViewById(R.id.loginbutton).getLayoutParams();
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

        //Program Login Button
        Button loginButton = (Button) findViewById(R.id.loginbutton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginEditText = (EditText) findViewById(R.id.loginedittext);
                loginString = loginEditText.getText().toString().trim();
                loginEditText.setText("");

                //login validation type
                if(accountType==0) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("userID", loginString);
                    ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String s, ParseException e) {
                            if (s.equals("valid")) {
                                myID = s;
                                Log.d("MainActivity","My ID saved.");
                                setContentView(R.layout.loading_screen);
                                TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                                loggingoutintext.setText("Loading...");
                                loginController.login(loginString);
                            } else {
                                Toast.makeText(context, "Invalid ID.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    // As a counselor, login as a parse user
                    TextView usernameEditText = (TextView)findViewById(R.id.counselor_login_edittext_username);
                    final String username = usernameEditText.getText().toString();
                    TextView pwEditText = (TextView)findViewById(R.id.counselor_login_edittext_password);
                    String password = pwEditText.getText().toString();
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                // Hooray! The user is logged in.
                                myID = user.getString("userID");
                                Log.d("MainActivity", "myID saved as " + user.getString("userID"));
                                SharedPreferences.Editor mEditor = mPrefs.edit();
                                mEditor.putString("username", username).commit();
                                //mEditor.putString("userID",myID).commit();

                                setContentView(R.layout.loading_screen);
                                TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                                loggingoutintext.setText("Loading...");
                                loginController.login((String) user.get("userID"));
                            } else {
                                // Signup failed. Look at the ParseException to see what happened.
                                Toast.makeText(context, "Invalid Login.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });


        //Login if Authentication exists from last session

            if (loginController.getLayerClient().isAuthenticated()) {
                myID=loginController.getLayerClient().getAuthenticatedUserId();
                if(accountType==0) {
                    loginString = myID;
                    //myID = loginString;
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
                                //Toast.makeText(context, "Invalid ID.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    if(!mPrefs.getString("username","").equals("") && !mPrefs.getString("userID", "").equals("")) {
                        setContentView(R.layout.loading_screen);
                        TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                        loggingoutintext.setText("Loading...");
                        //myID=mPrefs.getString("userID","");
                        //Toast.makeText(context, "myID saved as "+myID, Toast.LENGTH_SHORT).show();
                        loginController.login(myID);
                    }

                }
            }

    }





    public void onUserAuthenticated(){
        //Populate Participant Provider
        participantProvider  = new ParticipantProvider();
        participantProvider.refresh();

        loginController.getLayerClient().registerSyncListener(MainActivity.this);




    }
}