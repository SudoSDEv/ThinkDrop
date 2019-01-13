package com.rescue.blood.instablood.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by sayan07 on 14/3/18.
 */

public class FCMResponse {

    @SerializedName("multicast_id")
    @Expose
    private float multicastId;
    @SerializedName("success")
    @Expose
    private float success;
    @SerializedName("failure")
    @Expose
    private float failure;
    @SerializedName("canonical_ids")
    @Expose
    private float canonicalIds;
    @SerializedName("results")
    @Expose
    private List<Result> results = null;

    public float getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Integer multicastId) {
        this.multicastId = multicastId;
    }

    public float getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public float getFailure() {
        return failure;
    }

    public void setFailure(Integer failure) {
        this.failure = failure;
    }

    public float getCanonicalIds() {
        return canonicalIds;
    }

    public void setCanonicalIds(Integer canonicalIds) {
        this.canonicalIds = canonicalIds;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

}

