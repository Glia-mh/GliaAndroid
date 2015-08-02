package com.layer.quick_start_android;

import android.util.Log;

import com.layer.atlas.Atlas;

public class Participant implements Atlas.Participant {

    private String name;
    private String ID;
    private String avatarString;
    private String bio;
    private boolean isAvailable;
    public Participant(String localName, String localID, String localAvatarString, String localBio, boolean localIsAvailable) {
        name = localName;
        ID=localID;
        Log.d("Participant","Participant created with an id of "+ID);
        avatarString=localAvatarString;
        bio=localBio;
        isAvailable = localIsAvailable;
    }

    public String getFirstName() { return name.split(" ")[0]; }
    public String getLastName() { return name.split(" ")[1]; }
    public String getID(){ return ID;}
    public String getBio() {return bio; }
    public String getAvatarString(){ return avatarString;}

    public boolean getIsAvailable() {
        return isAvailable;
    }
}

