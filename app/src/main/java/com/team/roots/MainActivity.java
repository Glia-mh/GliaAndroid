package com.team.roots;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.quick_start_android.R;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerSyncListener;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LayerSyncListener{



    String loginString;
    private String schoolObjectId;
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

        setContentView(com.layer.quick_start_android.R.layout.activity_main);


        //get accounttype
        accountType = mPrefs.getInt("accounttype", 0);


        TextView textViewCounselorLogin = (TextView) findViewById(com.layer.quick_start_android.R.id.counselorlogin);

        if (accountType == 0) {


            findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_username).setVisibility(View.GONE);
            findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_password).setVisibility(View.GONE);
            findViewById(com.layer.quick_start_android.R.id.loginedittext).setVisibility(View.VISIBLE);



            textViewCounselorLogin.setText(com.layer.quick_start_android.R.string.c_login);

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


            // set counselor login fields to visible and student's to gone
            findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_username).setVisibility(View.VISIBLE);
            findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_password).setVisibility(View.VISIBLE);
            findViewById(com.layer.quick_start_android.R.id.loginedittext).setVisibility(View.GONE);



            // option of selecting student login.
            textViewCounselorLogin.setText(com.layer.quick_start_android.R.string.s_login);

            textViewCounselorLogin.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putInt("accounttype", 0).commit();
                    onResume();
                }
            });
        }

        //School Select Button
        Button schoolSelect=(Button)findViewById(R.id.school_select);
        schoolSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSchoolListDialog();
            }
        });

        //Login Button
        final Button loginButton = (Button) findViewById(com.layer.quick_start_android.R.id.loginbutton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginEditText = (EditText) findViewById(com.layer.quick_start_android.R.id.loginedittext);
                loginString = loginEditText.getText().toString().trim();
                //loginEditText.setText("");

                if(isNetworkAvailable()) {




                    //Show loading icon and make word login go away temporarily
                    findViewById(com.layer.quick_start_android.R.id.login_progress).setVisibility(View.VISIBLE);
                    loginButton.setText("");

                    //login validation student
                    if (accountType == 0) {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("userID", loginString);
                        params.put("schoolID",schoolObjectId);
                        ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                            @Override
                            public void done(String s, ParseException e) {
                                if (s.equals("valid")) {


                                    participantProvider  = new ParticipantProvider();
                                    participantProvider.refresh(loginString, schoolObjectId, loginController);
                                    Log.d("callback", "valid user id");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                    findViewById(com.layer.quick_start_android.R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                                    loginButton.setText(com.layer.quick_start_android.R.string.action_sign_in); //make button say login again
                                    onResume();
                                }

                            }
                        });

                        //login validation counselor
                    } else {
                        TextView usernameEditText = (TextView) findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_username);
                        String username = usernameEditText.getText().toString();
                        TextView pwEditText = (TextView) findViewById(com.layer.quick_start_android.R.id.counselor_login_edittext_password);
                        String password = pwEditText.getText().toString();

                        ParseUser.logInInBackground(username, password, new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {
                                    if(user.getParseObject("schoolID").getObjectId().equals(schoolObjectId)) {
                                        loginString = user.getObjectId();
                                        Log.d("user.getObjectId", "user.getObjectId=" + loginString);
                                        participantProvider = new ParticipantProvider();
                                        participantProvider.refresh(loginString, schoolObjectId, loginController);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                        findViewById(com.layer.quick_start_android.R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                                        loginButton.setText(com.layer.quick_start_android.R.string.action_sign_in); //make button say login again
                                        onResume();
                                    }
                                } else {

                                    Toast.makeText(getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                    findViewById(com.layer.quick_start_android.R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                                    loginButton.setText(com.layer.quick_start_android.R.string.action_sign_in); //make button say login again
                                    onResume();
                                }
                            }
                        });

                    }
                } else {
                    getWelcomeAlertDialog(com.layer.quick_start_android.R.string.no_internet_connection).show();
                }
            }
        });


        //Login if Authentication exists from last session
        if(isNetworkAvailable()) {
            if (loginController.getLayerClient().isAuthenticated()) {
                isSynced = true;
                setContentView(com.layer.quick_start_android.R.layout.loading_screen);
                loginString = loginController.getLayerClient().getAuthenticatedUserId();
                schoolObjectId=mPrefs.getString("loginSchool", null);
                participantProvider = new ParticipantProvider();
                participantProvider.refresh(loginString,schoolObjectId, loginController);

                //TextView loggingoutintext = (TextView) findViewById(R.id.loginlogoutinformation);
                //loggingoutintext.setText("Loading...");

            }
        }
    }



    public void onSyncProgress(LayerClient layerClient, SyncType syncType, int progress){
        ProgressBar progressBar=(ProgressBar)findViewById(com.layer.quick_start_android.R.id.login_progress);
        progressBar.setProgress((progress/2)+50);
    }



    //Called before syncing with the Layer servers
    public void onBeforeSync(LayerClient layerClient, SyncType syncType) {

    }

    //Called after syncing with the Layer servers
    public void onAfterSync(LayerClient layerClient, SyncType syncType) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("loginSchool", schoolObjectId).apply();
        Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
        intent.putExtra("mUserId", loginString);
        intent.putExtra("mSchoolId",schoolObjectId);
        finish();
        startActivity(intent);
        loginController.getLayerClient().unregisterSyncListener(this);
    }


    //Captures any errors with syncing
    public void onSyncError(LayerClient layerClient, List<LayerException> layerExceptions) {
    }


    public void onUserAuthenticated(){
        Log.d("onUserAuthenticated", "onUserAuthenticated");
        if (isSynced){
            Intent intent = new Intent(getApplicationContext(), ConversationListActivity.class);
            intent.putExtra("mUserId", loginString);
            intent.putExtra("mSchoolId", schoolObjectId);
            //needed to avoid future calls to onUserAuthenticated when phone disconnects
            finish();
            startActivity(intent);


        } else {
            loginController.getLayerClient().registerSyncListener(this);
        }
    }

    private void getSchoolListDialog(){
        final AlertDialog.Builder schoolListDialog = new AlertDialog.Builder(this);
        schoolListDialog.setIcon(R.drawable.ic_launcher);
        final List<School> schools=new ArrayList<School>();
        final HashMap<String, Object> params = new HashMap<String, Object>();
        if (isNetworkAvailable()) {
            ParseCloud.callFunctionInBackground("getSchools", params, new FunctionCallback<ArrayList<String>>() {
                public void done(ArrayList<String> returned, ParseException e) {
                    if (e == null) {
                        for (String obj : returned) {
                            Log.d("MainActivity", "Returned string from cloud function is: " + obj);
                            try {
                                JSONObject j = new JSONObject(obj);
                                schools.add(new School(j.getString("objectId"),
                                        j.getString("SchoolName")));
                                Log.d("MainActivity", "Successfully made JSON.");
                                Log.d("ObjectId", j.getString("objectId") + "objectId of School");
                            } catch (JSONException exception) {
                                exception.printStackTrace();
                                Log.d("MainActivity", "Couldn't convert string to JSON.");
                            }

                        }
                        final SchoolListAdapter schoolListAdapter = new SchoolListAdapter(getApplicationContext(), R.layout.schoollistrow, schools);
            schoolListDialog.setNegativeButton(
                    "cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                        schoolListDialog.setAdapter(
                                schoolListAdapter,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        School schoolSelected = schoolListAdapter.getItem(which);
                                        Button schoolSelect = (Button) findViewById(R.id.school_select);
                                        schoolSelect.setText(schoolSelected.getSchoolName());
                                        schoolObjectId = schoolSelected.getObjectId();

                                    }
                                });
                        schoolListDialog.show();

                    }


                }
            });

        } else {
            getWelcomeAlertDialog(com.layer.quick_start_android.R.string.no_internet_connection).show();
        }

    }
    public class SchoolListAdapter extends ArrayAdapter<School>{
        public SchoolListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public SchoolListAdapter(Context context, int resource, List<School> items) {
            super(context, resource, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.schoollistrow, null);
            }

            School school = getItem(position);

            if (school != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.school_name_text);

                if (tt1 != null) {
                    tt1.setText(school.getSchoolName());
                }


            }

            return v;
        }


    }

    public class School {
        private String objectId;
        private String schoolName;

        School(String objectId, String schoolName){
            this.objectId=objectId;
            this.schoolName=schoolName;
        }

        public String getSchoolName(){
            return schoolName;
        }

        public String getObjectId(){
            return objectId;
        }

        public void setSchoolName(String schoolName){
            this.schoolName=schoolName;
        }

        public void setObjectId(String objectId){
            this.objectId=objectId;
        }

    }
    private AlertDialog getWelcomeAlertDialog(int stringAddress){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringAddress)
                .setPositiveButton(com.layer.quick_start_android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }


}