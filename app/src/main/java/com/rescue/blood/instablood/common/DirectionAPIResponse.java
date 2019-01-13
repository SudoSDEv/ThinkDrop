package com.rescue.blood.instablood.common;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by sayan07 on 21/2/18.
 */

public interface DirectionAPIResponse {

    @GET
    Call<String> getResponse(@Url String url);//using String instead of Interface
}
