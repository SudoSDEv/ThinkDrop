package com.rescue.blood.instablood;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Data;
import com.rescue.blood.instablood.common.DirectionAPIResponse;
import com.rescue.blood.instablood.common.FCMJsonSender;
import com.rescue.blood.instablood.common.FCMRequest;
import com.rescue.blood.instablood.common.FCMResponse;
import com.rescue.blood.instablood.common.FCMRetrofitClient;
import com.rescue.blood.instablood.common.RetrofitClient;
import com.rescue.blood.instablood.common.Token;
import com.rescue.blood.instablood.model.Request;
import com.rescue.blood.instablood.model.Seen;
import com.rescue.blood.instablood.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rescue.blood.instablood.common.Common.mLastLocation;
import static com.rescue.blood.instablood.common.Common.pendingReceivers;

public class RequestForBlood extends AppCompatActivity {

    LatLng destination;
    TextView dt,dt1,address;

    static MediaPlayer mediaPlayer;
    User receiver;
    FirebaseFirestore firestore;
    FirebaseUser mUser;
    String toId,receiverToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Raleway-SemiBold.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_request_for_blood);

        mUser= FirebaseAuth.getInstance().getCurrentUser();
        firestore=FirebaseFirestore.getInstance();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        dt = findViewById(R.id.dt);
        dt1=findViewById(R.id.dt1);
        address = findViewById(R.id.address);
        Button call = findViewById(R.id.button2);
        Button decline=findViewById(R.id.decline);



        if (getIntent() != null) {
            Bundle b = getIntent().getExtras();
            //Log.d("s", b.getDouble("lat") + "");
            destination = new LatLng(b.getDouble("lat", 0), b.getDouble("lng", 0));
            if (destination.latitude == 0 && destination.longitude == 0) {
                LatLng latLng = new Gson().fromJson(b.getString("body"), LatLng.class);
                receiver = new Gson().fromJson(b.getString("user"), User.class);
                destination = new LatLng(latLng.latitude, latLng.longitude);
            } else {
                receiver = (User) b.getSerializable("user");
            }
            toId=b.getString("id");
            receiverToken=b.getString("token");
            Log.d("tits",receiverToken);



        }

            final String res[]=findDistanceDurationAddress(destination, getResources().getString(R.string.directionAPIKey));
            dt.setText("Fetching Info");
            address.setText("Fetching Info");

            Log.d("ph", receiver.getPhone());



        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(RequestForBlood.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RequestForBlood.this,new String[]{Manifest.permission.CALL_PHONE},86);
                }else {


                    FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(receiver.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Token token=dataSnapshot.getValue(Token.class);
                                    com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(Common.user.getId(),receiver.getId(),false);
                                    String callJSON=new Gson().toJson(call);
                                    com.rescue.blood.instablood.common.Notification myNotification = new com.rescue.blood.instablood.common.Notification();
                                    myNotification.setBody("Touch to progress to donate or receive.");
                                    myNotification.setClickAction("TrackMaps");
                                    myNotification.setTitle("ACKNOWLEDGEMENT PENDING");
                                    Log.d("kk", new Gson().toJson(myNotification));
                                    Data data=new Data();
                                    data.setCall(callJSON);
                                    data.setReceiver(true);
                                    data.setUser(new Gson().toJson(Common.user));
                                    data.setToId(receiver.getId());

                                    FCMJsonSender sender = new FCMJsonSender();

                                    sender.setPriority("high");
                                    sender.setTo(token.getToken());
                                    sender.setNotification(myNotification);
                                    sender.setData(data);

                                    Retrofit retrofit = FCMRetrofitClient.getRetrofitClient(Common.FCM_API);

                                    retrofit.create(FCMRequest.class)
                                            .sendRequest(sender).enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().getSuccess()==1)
                                                Log.d("notifieddd", response.body().toString());

                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.d("notifieddd", t.getMessage());

                                        }
                                    });



                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(FirebaseAuth.getInstance().getCurrentUser().getUid(),receiver.getId(),true);
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("key",call)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    Intent i=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+receiver.getPhone()));
                    startActivity(i);
                }
            }
        });



        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(res[0]!=null)
                {
                    final Request request=new Request(receiver.getName(),receiver.getPhone(),res[2],res[1],receiver.getId());
                    //boolean flag=false;
                    //firestore.collection("PendingReceivers").document(toId).collection("Requests")
                       //     .whereEqualTo("id",receiver.getId()).get().
                    //addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     //   @Override
                   //     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                      //      if(task.getResult().size()==0) {
                        firestore.collection("PendingReceivers").document(toId).collection("Requests").document(receiver.getId())
                                .set(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(getApplicationContext(), "The needy has been added to your pending lists. Try to help if possible, have a nice day @ThinkDrop", Toast.LENGTH_LONG).show();
                            }
                        });
                            //}

                    //    }




                    //});

                    Seen seen=new Seen(toId,receiver.getId(),true);
                    FirebaseFirestore.getInstance().collection("SeenInfos").document(receiver.getId()).collection("Seens")
                            .document(toId).set(seen).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                    Data data=new Data();
                    data.setReceiver(false);
                    data.setUser(new Gson().toJson(Common.user));
                    data.setToId(receiver.getId());
                    data.setBody(new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
                    data.setToken(Common.TOKEN);

                    FCMJsonSender sender=new FCMJsonSender();
                    sender.setTo(receiverToken);
                    sender.setPriority("high");
                    sender.setData(data);
                    sender.setNotification(null);

                    FCMRequest fcmRequest=FCMRetrofitClient.getRetrofitClient(Common.FCM_API).create(FCMRequest.class);
                    fcmRequest.sendRequest(sender).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body().getSuccess()==1){
                                Log.d("notifiedR", response.body().toString());

                            }

                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("notifiedR", t.getMessage());

                        }
                    });
                    finish();
                }

            }
        });


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case 86:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + receiver.getPhone()));
                    startActivity(i);
                }
                break;
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public String[] findDistanceDurationAddress(LatLng destination, String directionAPIKey) {
        final String r[]=new String[3];
        if(mLastLocation!=null) {
            String fullURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&destination=" + destination.latitude + "," + destination.longitude + "&mode=driving&key=" + directionAPIKey;
            DirectionAPIResponse directionAPIResponse = RetrofitClient.getRetrofitClient("", ScalarsConverterFactory.create()).create(DirectionAPIResponse.class);
            directionAPIResponse.getResponse(fullURL).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            jsonObject = jsonArray.getJSONObject(0);
                            jsonArray = jsonObject.getJSONArray("legs");
                            r[0] = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");
                            r[1] = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
                            r[2] = jsonArray.getJSONObject(0).getString("end_address");
                            dt.setText(receiver.getName() + " is at " + r[0] + " away.");
                            dt1.setText("Duration : " + r[1]);
                            address.setText(r[2]);
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

        }
            return r;

    }


    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }
}
