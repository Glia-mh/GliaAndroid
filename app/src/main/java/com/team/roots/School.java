package com.team.roots;

/**
 * Created by adityaaggarwal on 12/17/15.
 */
public class School {
    private String objectId;
    private String schoolName;
    private int positioninList;

    School(String objectId, String schoolName) {
        this.objectId = objectId;
        this.schoolName = schoolName;
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

