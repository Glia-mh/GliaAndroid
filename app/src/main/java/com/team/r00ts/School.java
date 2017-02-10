package com.team.r00ts;

/**
 * Created by adityaaggarwal on 12/17/15.
 */
public class School {
    private String objectId;
    private String schoolName;
    private String email;
    private int positioninList;


    School(String objectId, String schoolName, String email) {
        this.objectId = objectId;
        this.schoolName = schoolName;
        this.email=email;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setPositioninList(int positioninList){
        this.positioninList=positioninList;
    }

    public int getPositioninList(){
        return positioninList;
    }

    public String getEmail(){return email;}
    @Override
    public boolean equals(Object schoolObject){
        if(schoolObject instanceof School) {
            School schoolObjectA = (School) schoolObject;
            return schoolName.equals(schoolObjectA.getSchoolName()) && (objectId.equals(schoolObjectA.getObjectId()));
        } else {
            return false;
        }
    }

}

