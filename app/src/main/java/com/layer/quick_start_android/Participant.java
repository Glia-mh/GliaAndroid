package com.layer.quick_start_android;

import com.layer.atlas.Atlas;

/**
 * Created by adityaaggarwal on 7/1/15.
 */
public class Participant implements Atlas.Participant {

    private String name;
    private String ID;
    private String avatarString;
    private String bio;
    public Participant(String localName, String localID, String localAvatarString, String localBio ) {
        name = localName;
        ID=localID;
        avatarString=localAvatarString;
        bio=localBio;
    }

    public String getFirstName() { return name; }
    public String getLastName() { return ""; }
    public String getID(){ return ID;}
    public String getBio() {return bio; }
    public String getAvatarString(){ return avatarString;}
}

