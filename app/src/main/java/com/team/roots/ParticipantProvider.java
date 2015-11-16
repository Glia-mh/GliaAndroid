package com.team.roots;

import android.util.Log;

import com.layer.atlas.Atlas;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantProvider implements Atlas.ParticipantProvider {

    private final Map<String, Participant> participantMap =
            new HashMap<String, Participant>();

    public void refresh() {
        Log.d("ParticipantProvider", "refresh called.");

        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run
        //eventually mdHash it and add to participants provider--or may not be needed because
        // participantMap.put("",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));

        final List<Participant> participants = new ArrayList<Participant>();

        final HashMap<String, Object> params = new HashMap<String, Object>();
        ParseCloud.callFunctionInBackground("getCounselors", params, new FunctionCallback<ArrayList<String>>() {
            public void done(ArrayList<String> returned, ParseException e) {
                if (e == null) {
                    for (String obj : returned) {
                        Log.d("MainActivity", "Returned string from cloud function is: " + obj);
                        try {
                            JSONObject j = new JSONObject(obj);
                            participants.add(new Participant(j.getString("name"),
                                    j.getString("objectId"), j.getString("photoURL"),
                                    j.getString("bio"), j.getBoolean("isAvailable")));
                            Log.d("MainActivity", "Successfully made JSON.");
                            Log.d("ObjectId",j.getString("objectId")+"objectId of Counselor");
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                            Log.d("MainActivity", "Couldn't convert string to JSON.");
                        }

                    }

                    //Populate counselors with counselors from parse
                    for (Participant participant:participants) {
                        participantMap.put(participant.getID(), participant);
                        Log.d("ParticipantProvider", "Participant with id of " + participant.getID() + " added to map.");
                    }

                }


            }
        });






    }

    public void refresh(final String loginString, final LoginController loginController) {
        Log.d("ParticipantProvider", "refresh called.");

        //Connect to your user management service and sync the user's
        // contact list, making sure you include the authenticated user.
        // Then, store those users in the participant map

        //Add the authenticated user
        //--removed check if there is an effect on run
        //eventually mdHash it and add to participants provider--or may not be needed because
        // participantMap.put("",new Participant("You","107070","http://icons.iconarchive.com/icons/mazenl77/I-like-buttons-3a/512/Cute-Ball-Go-icon.png"));

        final List<Participant> participants = new ArrayList<Participant>();

        final HashMap<String, Object> params = new HashMap<String, Object>();
        ParseCloud.callFunctionInBackground("getCounselors", params, new FunctionCallback<ArrayList<String>>() {
            public void done(ArrayList<String> returned, ParseException e) {
                if (e == null) {
                    for (String obj : returned) {
                        Log.d("MainActivity", "Returned string from cloud function is: " + obj);
                        try {
                            JSONObject j = new JSONObject(obj);
                            participants.add(new Participant(j.getString("name"),
                                    j.getString("objectId"), j.getString("photoURL"),
                                    j.getString("bio"), j.getBoolean("isAvailable")));
                            Log.d("MainActivity", "Successfully made JSON.");
                            Log.d("ObjectId",j.getString("objectId")+"objectId of Counselor");
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                            Log.d("MainActivity", "Couldn't convert string to JSON.");
                        }

                    }

                    //Populate counselors with counselors from parse
                    for (Participant participant:participants) {
                        participantMap.put(participant.getID(), participant);
                        Log.d("ParticipantProvider", "Participant with id of " + participant.getID() + " added to map.");
                    }
                    loginController.login(loginString);
                }


            }
        });






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
