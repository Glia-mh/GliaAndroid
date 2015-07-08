package com.layer.quick_start_android;

import com.layer.atlas.Atlas;

/**
 * Created by adityaaggarwal on 7/1/15.
 */
public class Participant implements Atlas.Participant {

        private String name;
        private String ID;
        private String avatarString;
        public Participant(String localName, String localID, String localAvatarString ) {
            name = localName;
            ID=localID;
            avatarString=localAvatarString;

        }

        public String getFirstName() { return name; }
        public String getLastName() { return ""; }
        public String getID(){ return ID;}
        public String getAvatarString(){ return avatarString;}
    }

