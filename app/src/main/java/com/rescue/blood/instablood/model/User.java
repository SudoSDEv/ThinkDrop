package com.rescue.blood.instablood.model;

import java.io.Serializable;

/**
 * Created by sayan07 on 28/2/18.
 */

public class User implements Serializable
{
    private String Id;
    private String email;
    private String pass;
    private String name;
    private String phone;
    private String bloodGroup;
    private boolean isPassive=true;
    private String dpUrl=null;

    public User(String email, String pass, String name, String phone, String bloodGroup) {
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.isPassive = isPassive;
    }

    public User(String id, String email, String pass, String name, String phone, String bloodGroup, boolean isPassive, String dpUrl) {
        Id = id;
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.isPassive = isPassive;
        this.dpUrl = dpUrl;
    }

    public String getDpUrl() {
        return dpUrl;
    }

    public void setDpUrl(String dpUrl) {
        this.dpUrl = dpUrl;
    }

    public boolean isPassive() {
        return isPassive;
    }

    public User(User user)
    {
        this(user.getEmail(),user.getPass(),user.getId(),user.getName(),user.getPhone(),user.getBloodGroup());
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public User(String email, String pass, String id, String name, String phone, String bloodGroup) {
        this.email = email;
        this.pass = pass;
        this.Id=id;
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.isPassive = isPassive;
    }

    public User(){}


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
}
