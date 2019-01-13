package com.rescue.blood.instablood.model;

/**
 * Created by sayan07 on 18/4/18.
 */

public class Seen {

    private String donorId, receiverId;
    private boolean isSeen;

    public Seen() {
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public Seen(String donorId, String receiverId, boolean isSeen) {
        this.donorId = donorId;
        this.receiverId = receiverId;
        this.isSeen = isSeen;
    }

    public String getDonorId() {
        return donorId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public boolean isSeen() {
        return isSeen;
    }
}
