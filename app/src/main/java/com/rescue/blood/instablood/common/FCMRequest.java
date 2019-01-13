package com.rescue.blood.instablood.common;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by sayan07 on 14/3/18.
 */

public interface FCMRequest {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAH_r8lbE:APA91bHGp4G_uwhZND5Sw3HrPSF6ZoNnXdTxKedPji7GRpMV6IDwUT7IFWlCApsiQfdwadN_pcE8ZieYwKtKGl6eXFMByvUL4XyByr2EhfYL2PBuj9m95ONMRy7oEU91_e63w0-SEzQ6"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendRequest(@Body FCMJsonSender sender);
}
