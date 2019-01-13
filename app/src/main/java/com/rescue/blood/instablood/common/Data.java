package com.rescue.blood.instablood.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rescue.blood.instablood.model.User;

/**
 * Created by sayan07 on 18/3/18.
 */

public class Data {

    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("isreceiver")
    @Expose
    private boolean isReceiver;

    private String id;
    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("call")// used for only call FCM
    @Expose
    private String call;

    public void setCall(String call) {
        this.call = call;
    }

    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setToId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
