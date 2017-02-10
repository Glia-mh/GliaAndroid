package com.team.r00ts;

/**
 * Created by adityaaggarwal on 12/17/15.
 */
public class AccountType {
    private int objectId;
    private String accountType;
    private int positioninList;

    AccountType(int objectId, String accountType) {
        this.objectId = objectId;
        this.accountType = accountType;
    }

    public String getaccountType() {
        return accountType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setaccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public void setPositioninList(int positioninList){
        this.positioninList=positioninList;
    }

    public int getPositioninList(){
        return positioninList;
    }
    @Override
    public boolean equals(Object accountTypeObject){
        if(accountTypeObject instanceof AccountType) {
            AccountType accountTypeObjectA = (AccountType) accountTypeObject;
            return accountType.equals(accountTypeObjectA.getaccountType()) && (objectId == accountTypeObjectA.getObjectId());
        } else {
            return false;
        }
    }
}