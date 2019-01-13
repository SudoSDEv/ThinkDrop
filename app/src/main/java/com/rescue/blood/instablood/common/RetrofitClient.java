package com.rescue.blood.instablood.common;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by sayan07 on 21/2/18.
 */

public class RetrofitClient {

    public static Retrofit retrofit=null;

    public static Retrofit getRetrofitClient(String baseUrl, Converter.Factory x)
    {
        if(retrofit==null)
        {
            retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(x).build();
        }
        return retrofit;
    }
}
