package com.layer.quick_start_android;

/**
 * Created by adityaaggarwal on 7/1/15.
 */

import android.util.Log;

import com.layer.atlas.Atlas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantProvider implements Atlas.ParticipantProvider{

    private final Map<String, Participant> participantMap =
            new HashMap<String, Participant>();

    public void refresh(List<Participant> participants) {
        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run
        //eventually mdHash it and add to participants provider--or may not be needed because
       // participantMap.put("",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));



        for (Participant participant:participants){
            participantMap.put(participant.getID(), participant);
        }

        //Populate counselors with counselors from parse
        Log.d("Refresh Check", "Refresh Check");



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
