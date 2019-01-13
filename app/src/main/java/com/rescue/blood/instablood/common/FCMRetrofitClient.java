package com.rescue.blood.instablood.common;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sayan07 on 15/3/18.
 */

public class FCMRetrofitClient {

    private static Retrofit retrofit=null;

    public static Retrofit getRetrofitClient(String baseUrl)
    {
        if(retrofit==null)
        {
            retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

}
