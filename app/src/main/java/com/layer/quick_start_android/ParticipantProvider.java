package com.layer.quick_start_android;

/**
 * Created by adityaaggarwal on 7/1/15.
 */

import android.util.Log;

import com.layer.atlas.Atlas;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantProvider implements Atlas.ParticipantProvider{

    private final Map<String, Participant> participantMap =
            new HashMap<String, Participant>();

    public void refresh() {
        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run

        // counselors.put(authUser.getId(), authUser);

        participantMap.put("107070",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));
        //Populate counselors with counselors from parse
        Log.d("Refresh Check", "Refresh Check");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Counselors");
            query.whereEqualTo("counselorType", "1");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> counselorList, ParseException e) {
                    try {
                        if (e == null) {
                            Log.d("counselors", "Retrieved " + counselorList.size() + " counselors");
                            for (ParseObject parseCounselor : counselorList) {
                                participantMap.put(parseCounselor.getString("userID"), new Participant(parseCounselor.getString("Name"), parseCounselor.getString("userID"), parseCounselor.getString("Photo_URL")));
                               // Log.d("Username",participantMap.get(parseCounselor.getString("userID")).getID()+" Username");
                            }
                        } else {
                            Log.d("counselors", "Error: counselors" + e.getMessage());
                        }
                    } catch (Exception a) {
                        Log.d("Error", "Error" + a.toString());
                    }
                }
            });


    }

    public Map<String, Atlas.Participant> getParticipants(String filter, Map<String,
            Atlas.Participant> result) {
        if (result == null) {
            result = new HashMap<String, Atlas.Participant>();
        }

        if (filter == null) {
            for (Participant p : participantMap.values()) {
                result.put(p.getID(), p);
            }
            return result;
        }

        for (Participant p : participantMap.values()) {
            if (p.getFirstName() != null && !p.getFirstName().toLowerCase().contains(filter)) {
                result.remove(p.getID());
                continue;
            }

            result.put(p.getID(), p);
        }

        return result;
    }

    @Override
    public Atlas.Participant getParticipant(String userId) {
        return participantMap.get(userId);
    }
}
