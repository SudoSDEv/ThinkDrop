package com.rescue.blood.instablood.model;

import java.io.Serializable;

/**
 * Created by sayan07 on 18/4/18.
 */

public class Call implements Serializable {

    private String caller,receiver;
    boolean isdonor;



    public Call() {
    }

    public boolean isIsdonor() {
        return isdonor;
    }

    public void setIsdonor(boolean isdonor) {
        this.isdonor = isdonor;
    }

    public Call(String caller, String receiver, boolean isdonor) {

        this.caller = caller;
        this.receiver = receiver;
        this.isdonor = isdonor;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
