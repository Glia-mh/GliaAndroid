package com.layer.quick_start_android;

import android.util.Log;

import com.layer.atlas.Atlas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantProvider implements Atlas.ParticipantProvider{

    private final Map<String, Participant> participantMap =
            new HashMap<String, Participant>();

    public void refresh(List<Participant> participants) {
        Log.d("ParticipantProvider", "refresh called.");

        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run
        //eventually mdHash it and add to participants provider--or may not be needed because
        // participantMap.put("",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));


        //Populate counselors with counselors from parse
        for (Participant participant:participants){
            participantMap.put(participant.getID(), participant);
            Log.d("ParticipantProvider","Participant with id of "+participant.getID()+" added to map.");
        }




    }

    public Map<String, Participant> getCustomParticipants(String filter, Map<String, Participant> result) {
        if (result == null) {
            result = new HashMap<String, Participant>();
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
    public Participant getParticipant(String userId) {
        Log.d("ParticipantProvider","getParticipant called");
        Log.d("ParticipantProvider","requested userID=="+userId);
        Log.d("ParticipantProvider","map is "+participantMap.toString());
        Log.d("ParticipantProvider","participantMap.get(userId)=="+participantMap.get(userId));
        return participantMap.get(userId);
    }

    // Returns an array of type Participant
    public Participant[] getCustomParticipants() {
        return participantMap.values().toArray(new Participant[]{});
    }
}
