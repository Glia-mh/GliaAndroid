package com.layer.quick_start_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class CounselorLoginActivity extends MainActivity{


    //other variables
    static Context context;
    String loginString;
    LoginController loginController;
    static public ParticipantProvider participantProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Parse.initialize(this, "pya3k6c4LXzZMy6PwMH80kJx4HD2xF6duLSSdYUl", "BOOijRRSKlKh5ogT2IaacnnK2eHJZqt8L30VPIcc");
        getSupportActionBar().hide();

        // Create a LayerClient object no UserId included
        loginController = new LoginController();
        loginController.setLayerClient(context, this);
        setContentView(R.layout.activity_login_counselor);
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


    protected void onResume(){

        super.onResume();
        //Program Login Button
        Button loginButton = (Button) findViewById(R.id.loginbutton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginEditText = (EditText) findViewById(R.id.loginedittext);
                loginString = loginEditText.getText().toString().trim();
                loginEditText.setText("");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("userID", loginString);
                ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String s, ParseException e) {
                        if (s.equals("valid")) {
                            setContentView(R.layout.loading_screen);
                            TextView loggingoutintext=(TextView)findViewById(R.id.loginlogoutinformation);
                            loggingoutintext.setText("Loading...");
                            loginController.login(loginString);
                        } else {
                            Toast.makeText(context, "Invalid ID.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });



        //Login if Authentication exists from last session

        if (loginController.getLayerClient().isAuthenticated()) {
            setContentView(R.layout.loading_screen);
            TextView loggingoutintext=(TextView)findViewById(R.id.loginlogoutinformation);
            loggingoutintext.setText("Loading...");
            loginString=loginController.getLayerClient().getAuthenticatedUserId();
            loginController.login(loginString);
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onUserAuthenticated(){
        //Populate Participant Provider
        participantProvider  = new ParticipantProvider();



        /*ParseQuery<ParseObject> query = ParseQuery.getQuery("Counselors");
        query.whereEqualTo("counselorType", "1");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> counselorList, ParseException e) {
                try {
                    if (e == null) {
                        List<Participant> counselorLocalList = new ArrayList<Participant>();

                        Log.d("counselors", "Retrieved " + counselorList.size() + " counselors");
                        for (ParseObject parseCounselor : counselorList) {
                            // participantMap.put(parseCounselor.getString("userID"), new Participant(parseCounselor.getString("Name"), parseCounselor.getString("userID"), parseCounselor.getString("Photo_URL")));
                            counselorLocalList.add(new Participant(parseCounselor.getString("Name"), parseCounselor.getString("userID"), parseCounselor.getString("Photo_URL")));
                            // Log.d("Username",participantMap.get(parseCounselor.getString("userID")).getID()+" Username");
                        }
                        participantProvider.refresh(counselorLocalList);
                    } else {
                        Log.d("counselors", "Error: counselors" + e.getMessage());
                    }

                } catch (Exception a) {
                    Log.d("Error", "Error" + a.toString());
                }
                loginController.getLayerClient().registerSyncListener(MainActivity.this);
            }
        });*/


        final HashMap<String, Object> params = new HashMap<String, Object>();
        ParseCloud.callFunctionInBackground("getCounselors", params, new FunctionCallback<String>() {
            public void done(String returned, ParseException e) {
                if (e == null) {
                    //Log.d("MainActivity", "Returned string from cloud function is: "+returned);
                    List<Participant> counselorLocalList = new ArrayList<Participant>();

                    String[] counselors = returned.split(Pattern.quote("$"));
                    Log.d("MainActivity", "counselors.length="+counselors.length);
                    for (String c : counselors) {
                        //Log.d("MainActivity","String in counselers array is "+c);
                        String[] props = c.split(","); // [Name, userID, Photo_URL]
                        counselorLocalList.add(new Participant(props[0], props[1], props[2]));
                        Log.d("MainActivity","New counselor added name="+props[0]+", userID="+props[1]+", photo_URL="+props[2]);
                    }
                    participantProvider.refresh(counselorLocalList);
                }
                loginController.getLayerClient().registerSyncListener(CounselorLoginActivity.this);


            }
        });




    }
}