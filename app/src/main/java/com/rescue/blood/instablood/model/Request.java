package com.rescue.blood.instablood.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by sayan07 on 1/4/18.
 */

public class Request implements Comparable<Request>, Serializable {

    private String name,phone,address,duration,id;

    public Request() {
    }

    public Request(String name, String phone, String address, String duration, String id) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.duration = duration;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(@NonNull Request request) {
        if(this.id.equals(request.getId()))
            return 0;
        else if (id.compareTo(request.getId())<0)
            return -1;
        else
            return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(this.id.equals(((Request)obj).getId()))
            return true;
        return false;
    }
}
