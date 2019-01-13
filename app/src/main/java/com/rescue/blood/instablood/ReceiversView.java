package com.rescue.blood.instablood;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Data;
import com.rescue.blood.instablood.common.DirectionAPIResponse;
import com.rescue.blood.instablood.common.FCMJsonSender;
import com.rescue.blood.instablood.common.FCMRequest;
import com.rescue.blood.instablood.common.FCMResponse;
import com.rescue.blood.instablood.common.FCMRetrofitClient;
import com.rescue.blood.instablood.common.MyFireBaseMessagingService;
import com.rescue.blood.instablood.common.RetrofitClient;
import com.rescue.blood.instablood.common.Token;
import com.rescue.blood.instablood.model.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rescue.blood.instablood.common.Common.mLastLocation;

/**
 * Created by sayan07 on 5/4/18.
 */

public class ReceiversView extends AppCompatActivity {

    ArrayList<Request> receivers=new ArrayList<>();
    static TextView address,duration;
    ProgressBar pb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Raleway-SemiBold.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.recycler_view);
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);
         if(getIntent()!=null)
             receivers= (ArrayList<Request>) getIntent().getExtras().getSerializable("key");

        Log.d("Sizee",receivers.size()+"");

        RecyclerView recyclerView=findViewById(R.id.rv2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new RecyclerViewAdapter(recyclerView, receivers));

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                    Intent i=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+requestCode));
                    startActivity(i);

            }

    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        RecyclerView rv;
        ArrayList<Request> receivers;

        public RecyclerViewAdapter(RecyclerView rv, ArrayList<Request> receivers) {
            this.rv = rv;
            this.receivers = receivers;
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.card_layout_receiver,parent,false));
        }
        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder holder, int position) {

            if(holder!=null)
            {
                holder.noOfPending.setText(position+1+"");
                holder.nPh.setText(receivers.get(position).getName()+", "+receivers.get(position).getPhone());
                //holder.address.setText(receivers.get(position).getAddress());
                //holder.duration.setText(receivers.get(position).getDuration());
                //new MyAsynctask().execute(receivers.get(position).getId());
                GeoFire geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference()
                        .child(Common.RECEIVERS).child(Common.user.getBloodGroup())
                );
                geoFire.getLocation(receivers.get(position).getId(), new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location1) {
                        LatLng latLng=new LatLng(location1.latitude,location1.longitude);
                        Log.d("latlng",latLng.latitude+"");
                        setAddressDuration(latLng,holder.address,holder.duration);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("err1",databaseError.getMessage());
                    }
                });
                if(position==getItemCount()-1) pb.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return receivers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView nPh,address,duration,noOfPending;
            Button call,cancel;
            public ViewHolder(final View itemView) {
                super(itemView);
                itemView.findViewById(R.id.seentext).setVisibility(View.INVISIBLE);
                itemView.findViewById(R.id.seen).setVisibility(View.INVISIBLE);
                nPh=itemView.findViewById(R.id.namePhone);
                noOfPending=itemView.findViewById(R.id.noOfPending);

                address=itemView.findViewById(R.id.address);
                ReceiversView.address=this.address;
                duration=itemView.findViewById(R.id.duration);
                ReceiversView.duration=this.duration;
                call=itemView.findViewById(R.id.view);
                cancel=itemView.findViewById(R.id.cancel);
                cancel.setVisibility(View.INVISIBLE);
                call.setText("CALL");
                call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int pos=rv.getChildAdapterPosition(itemView);
                        final String ph=receivers.get(pos).getPhone();

                        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiversView.this);
                        builder.setTitle(ph);
                        builder.setMessage("Press call to confirm.");
                        builder.setPositiveButton("CALL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ActivityCompat.checkSelfPermission(ReceiversView.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ReceiversView.this,new String[]{android.Manifest.permission.CALL_PHONE},Integer.parseInt(ph));
                                }else {


                                    FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(receivers.get(pos).getId())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    Token token=dataSnapshot.getValue(Token.class);
                                                    com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(Common.user.getId(),receivers.get(pos).getId(),false);
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
                                                    data.setToId(receivers.get(pos).getId());

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


                                    Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ph));
                                    startActivity(intent);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(Common.user.getId(),receivers.get(pos).getId(),true);
                                            startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("key",call)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                                        }
                                    },3000);

                                }

                                //...
                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        AlertDialog callDia = builder.create();
                        callDia.show();
                    }
                });



            }
        }

    }

    public static void setAddressDuration(LatLng destination, final TextView address, final TextView duration) {

        final String r[]=new String[3];
        String fullURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&destination=" + destination.latitude + "," + destination.longitude + "&mode=driving&key=" + Common.DIRECTION_API_KEY;
        DirectionAPIResponse directionAPIResponse = RetrofitClient.getRetrofitClient("", ScalarsConverterFactory.create()).create(DirectionAPIResponse.class);
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
                        address.setText(r[2]);
                        duration.setText(r[1]);
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

    }
}
