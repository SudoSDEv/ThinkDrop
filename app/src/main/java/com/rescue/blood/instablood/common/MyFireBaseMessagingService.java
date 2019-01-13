package com.rescue.blood.instablood.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.rescue.blood.instablood.MapsActivity;
import com.rescue.blood.instablood.R;
import com.rescue.blood.instablood.RequestForBlood;
import com.rescue.blood.instablood.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.rescue.blood.instablood.common.Common.DIRECTIONS_API;
import static com.rescue.blood.instablood.common.Common.mLastLocation;

/**
 * Created by sayan07 on 14/3/18.
 */

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //hndle after call push ntfvtn
        String isCallFCM = remoteMessage.getData().get("call");
        if (isCallFCM==null) {

            String isReceiver = remoteMessage.getData().get("isreceiver");
            if (isReceiver.equals("true")) {
                LatLng latLng = new Gson().fromJson(remoteMessage.getData().get("body"), LatLng.class);
                User user = new Gson().fromJson(remoteMessage.getData().get("user"), User.class);
            /*PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, com.rescue.blood.instablood.Switch.class), PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentIntent(pendingIntent)
                    .setContentText("You have a blood request.")
                    .setContentTitle("DRIPDROP");

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());*/

                Log.d("NTF", latLng.latitude + " " + latLng.longitude + " " + remoteMessage.getFrom());

                startActivity(new Intent(MyFireBaseMessagingService.this, RequestForBlood.class)
                        .putExtra("lat", latLng.latitude).putExtra("lng", latLng.longitude)
                        .putExtra("user", user)
                        .putExtra("id", remoteMessage.getData().get("id"))
                        .putExtra("token", remoteMessage.getData().get("token"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


                //Toast.makeText(getBaseContext(),remoteMessage.getFrom()+" "+remoteMessage.getNotification().getBody(),Toast.LENGTH_LONG).show();
            }
        }
        else{
            com.rescue.blood.instablood.model.Call call=new Gson().fromJson(remoteMessage.getData().get("call"), com.rescue.blood.instablood.model.Call.class);
            startActivity(new Intent(MyFireBaseMessagingService.this, MapsActivity.class).putExtra("key",call)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }





    public static String getAddressDuration(LatLng destination) {

        final String r[]=new String[3];
        String fullURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&destination=" + destination.latitude + "," + destination.longitude + "&mode=driving&key=" + "AIzaSyANfMgR6YI2KpFmNcTLgiu-4GQrFHKWmM0";
        DirectionAPIResponse directionAPIResponse = RetrofitClient.getRetrofitClient(Common.DIRECTIONS_API, ScalarsConverterFactory.create()).create(DirectionAPIResponse.class);
        directionAPIResponse.getResponse(fullURL).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        jsonObject=jsonArray.getJSONObject(0);
                        jsonArray=jsonObject.getJSONArray("legs");
                        r[0]=jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");
                        r[1]=jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
                        r[2]=jsonArray.getJSONObject(0).getString("end_address");
                        Log.d("address",r[1]+" "+r[2]);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else Log.d("Result", "Failure");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                Log.d("Result", t.getMessage());
            }
        });

        return r[2]+" "+r[1];
    }

}


