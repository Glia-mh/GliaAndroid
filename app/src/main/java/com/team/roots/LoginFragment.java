package com.team.roots;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class LoginFragment extends Fragment {
    //global application memory
    SharedPreferences mPrefs;
    String loginString;
    int accountType;
    private boolean isUsernamePopulated;
    private boolean isPasswordPopulated;
    private View theView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        if(savedInstanceState==null)
            theView = inflater.inflate(R.layout.login_layout, container, false);
        else
            theView =  super.onCreateView(inflater, container, savedInstanceState);

        return theView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible()) {
            if (isVisibleToUser) {
                Log.d("account type number","account type number: " +getActivity().getIntent().getIntExtra("accountTypeNumber", 0));
                //get accounttype
                accountType = getActivity().getIntent().getIntExtra("accountTypeNumber", 0);


                //sets login view based on account type
                int idOfViewToFocusOn;
                if (accountType == 0) {


                    getView().findViewById(R.id.counselor_login_edittext_username).setVisibility(View.GONE);
                    getView().findViewById(R.id.counselor_login_edittext_password).setVisibility(View.GONE);
                    getView().findViewById(R.id.loginedittext).setVisibility(View.VISIBLE);

                    getView().findViewById(R.id.loginedittext).requestFocus();
                    idOfViewToFocusOn = R.id.loginedittext;

                } else {

                    // set counselor login fields to visible and student's to gone
                    getView().findViewById(R.id.counselor_login_edittext_username).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.counselor_login_edittext_password).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.loginedittext).setVisibility(View.GONE);

                    getView().findViewById(R.id.counselor_login_edittext_username).requestFocus();
                    idOfViewToFocusOn = R.id.counselor_login_edittext_username;


                } InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(((EditText) theView.findViewById(idOfViewToFocusOn)),InputMethodManager.SHOW_FORCED);
                Log.d("LoginFrag","Keyboard enabled");

                //UI population unspecific to accountType
                final School school = new School(getActivity().getIntent().getStringExtra("schoolobjectid"),
                        getActivity().getIntent().getStringExtra("schoolname"));




                //Login Button
                if(accountType==0) {
                    TextView loginDescription = (TextView) getView().findViewById(R.id.logindescription);
                    loginDescription.setText("To verify you are at " + school.getSchoolName() + ", enter your ID number.");
                    EditText loginEditText = (EditText) getView().findViewById(R.id.loginedittext);
                    loginEditText.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void afterTextChanged(Editable s) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                            if (s.length() > 0) {
                                final Button loginButton = (Button) getView().findViewById(R.id.loginbutton);
                                ColorDrawable buttonColor = (ColorDrawable) loginButton.getBackground();
                                int colorId = buttonColor.getColor();
                                Log.d("reached here","reached here"+colorId);
                                if (colorId == -7082078) {
                                    Log.d("reached here 2","reached here 2");
                                    loginButton.setBackgroundColor(getResources().getColor(R.color.roots_green_darker));
                                    loginButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {


                                            if (isNetworkAvailable()) {

                                                //global application memory
                                                mPrefs = getActivity().getSharedPreferences("label", 0);


                                                //Show loading icon and make word login go away temporarily
                                                getView().findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
                                                loginButton.setText("");

                                                //login validation student

                                                final EditText loginEditText = (EditText) getView().findViewById(R.id.loginedittext);
                                                loginString = loginEditText.getText().toString().trim();

                                                final HashMap<String, String> params = new HashMap<String, String>();
                                                params.put("userID", loginString);
                                                params.put("schoolID", school.getObjectId());
                                                ParseCloud.callFunctionInBackground("validateStudentID", params, new FunctionCallback<String>() {
                                                    @Override
                                                    public void done(String s, ParseException e) {
                                                        if (s.equals("valid")) {

                                                            MainActivity ma = (MainActivity) getActivity();
                                                            MainActivity.participantProvider = new ParticipantProvider();
                                                            MainActivity.participantProvider.refresh(loginString, params.get("schoolID"), ma.getLoginController());
                                                            Log.d("callback", "valid user id");
                                                        } else {
                                                            Toast.makeText(getActivity().getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                                            getView().findViewById(R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                                                            loginButton.setText(R.string.action_sign_in); //make button say login again
                                                            loginEditText.setText("");
                                                        }

                                                    }
                                                });

                                                //login validation counselor

                                            } else {
                                                MainActivity ma = (MainActivity) getActivity();
                                                ma.getWelcomeAlertDialog(R.string.no_internet_connection).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                nullifyListener();
                            }

                        }
                    });
                } else {
                    TextView loginDescription = (TextView) getView().findViewById(R.id.logindescription);
                    loginDescription.setText("To verify you are at " + school.getSchoolName() + ", enter your login.");
                    isUsernamePopulated=false;
                    isPasswordPopulated=false;
                    Log.d("populated","password: "+isPasswordPopulated+" and username: "+isUsernamePopulated);
                    TextView usernameEditText = (TextView) getView().findViewById(R.id.counselor_login_edittext_username);
                    String username = usernameEditText.getText().toString();
                    TextView pwEditText = (TextView) getView().findViewById(R.id.counselor_login_edittext_password);
                    String password = pwEditText.getText().toString();
                    if(username.length()>0) {
                        isUsernamePopulated=true;
                    }
                    if(password.length()>0) {
                        isPasswordPopulated=true;
                    }


                    pwEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.length() > 0) {

                                isPasswordPopulated = true;
                                Log.d("populated","password: "+isPasswordPopulated+" and username: "+isUsernamePopulated);
                                if (isUsernamePopulated) {
                                    Button loginButton = (Button) getView().findViewById(R.id.loginbutton);
                                    ColorDrawable buttonColor = (ColorDrawable) loginButton.getBackground();
                                    int colorId = buttonColor.getColor();
                                    Log.d("populated","color Id: " + colorId);
                                    if(colorId== -7082078) {
                                        setOnClickListenerCounselor();
                                    }
                                }
                            } else {
                                nullifyListener();
                                if(isPasswordPopulated){
                                    isPasswordPopulated=false;
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    usernameEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.length() > 0) {
                                isUsernamePopulated = true;
                                Log.d("populated","password: "+isPasswordPopulated+" and username: "+isUsernamePopulated);
                                if (isPasswordPopulated) {
                                    Button loginButton = (Button) getView().findViewById(R.id.loginbutton);
                                    ColorDrawable buttonColor = (ColorDrawable) loginButton.getBackground();
                                    int colorId = buttonColor.getColor();
                                    if(colorId== -7082078) {
                                        setOnClickListenerCounselor();
                                    }
                                }

                            } else {
                                nullifyListener();
                                if(isUsernamePopulated){
                                    isUsernamePopulated=false;
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });


                }


                TextView back = (TextView) getView().findViewById(R.id.back_school_login);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity ma = (MainActivity) getActivity();
                        ma.pager.setCurrentItem(ma.pager.getCurrentItem() - 1, true);
                    }
                });
            }
        }
    }


    //Network Check
    public boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getActivity().getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }
    private void nullifyListener() {
        Button loginButton = (Button) getView().findViewById(R.id.loginbutton);
        loginButton.setBackgroundColor(getResources().getColor(R.color.roots_green_lighter));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void setOnClickListenerCounselor() {

        final Button loginButton = (Button) getView().findViewById(R.id.loginbutton);
        loginButton.setBackgroundColor(getResources().getColor(R.color.roots_green_darker));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UI population unspecific to accountType
                final School school = new School(getActivity().getIntent().getStringExtra("schoolobjectid"),
                        getActivity().getIntent().getStringExtra("schoolname"));
                EditText usernameEditText=(EditText)getView().findViewById(R.id.counselor_login_edittext_username);
                final EditText passwordEditText=(EditText)getView().findViewById(R.id.counselor_login_edittext_password);
                getView().findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
                ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        TextView usernameEditText = (TextView) getView().findViewById(R.id.counselor_login_edittext_username);
                        TextView pwEditText = (TextView) getView().findViewById(R.id.counselor_login_edittext_password);
                        if (user != null) {
                            if (user.getParseObject("schoolID").getObjectId().equals(school.getObjectId())) {

                                MainActivity ma = (MainActivity) getActivity();
                                loginString = user.getObjectId();

                                Log.d("user.getObjectId", "user.getObjectId=" + loginString);
                                MainActivity.participantProvider = new ParticipantProvider();
                                MainActivity.participantProvider.refresh(loginString, school.getObjectId(), ma.getLoginController());
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                                getView().findViewById(R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                                loginButton.setText(R.string.action_sign_in); //make button say login again
                                Log.d("invalid login log","invalid because of inccorrect school ID");
                                usernameEditText.setText("");
                                pwEditText.setText("");
                            }
                        } else {

                            Toast.makeText(getActivity().getApplicationContext(), "Invalid Login.", Toast.LENGTH_SHORT).show();
                            getView().findViewById(R.id.login_progress).setVisibility(View.INVISIBLE); //make loading circle invisible again
                            loginButton.setText(R.string.action_sign_in); //make button say login again
                            Log.d("invalid login log", "invalid because does not match any user"+ usernameEditText.getText().toString()+passwordEditText.getText().toString());
                            usernameEditText.setText("");
                            pwEditText.setText("");
                        }
                    }
                });
            }
        });
    }
    }



