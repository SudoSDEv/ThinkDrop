package com.rescue.blood.instablood;

import android.*;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Data;
import com.rescue.blood.instablood.common.FCMJsonSender;
import com.rescue.blood.instablood.common.FCMRequest;
import com.rescue.blood.instablood.common.FCMResponse;
import com.rescue.blood.instablood.common.FCMRetrofitClient;
import com.rescue.blood.instablood.common.Token;
import com.rescue.blood.instablood.model.Request;
import com.rescue.blood.instablood.model.User;

import java.util.ArrayList;
import java.util.logging.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by sayan07 on 1/4/18.
 */

class RecieverAdapter extends RecyclerView.Adapter<RecieverAdapter.ViewHolder> {

    Context context; RecyclerView recyclerView; ArrayList<Request> receivers=new ArrayList<>();

    public RecieverAdapter(Context activity, RecyclerView recyclerView, ArrayList<Request> receivers) {
        context=activity;
        this.recyclerView=recyclerView;
        this.receivers.addAll(receivers);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_layout_receiver,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if(holder!=null){

            holder.no.setText(position+1+"");
            holder.namePhone.setText(receivers.get(position).getName()+", "+receivers.get(position).getPhone());
            holder.address.setText(receivers.get(position).getAddress());
            holder.duration.setText(receivers.get(position).getDuration());

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("Delete request");
                    builder.setMessage("Are you sure to delete the request?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            FirebaseFirestore.getInstance()
                                    .collection("PendingReceivers").document(Common.user.getId())
                                    .collection("Requests").document(receivers.get(position).getId())
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Request deleted.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Request failed to delete. Please check your internt conenction, else try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                            receivers.remove(position);
                            notifyDataSetChanged();

                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog ad=builder.create();
                    ad.show();
                }
            });

            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Please go back and provide permission and then relaunch the view.", Toast.LENGTH_SHORT).show();
                    }else {
                        FirebaseDatabase.getInstance().getReference("Calls").child(receivers.get(position).getId())
                                .push().setValue(Common.user.getId()).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        FirebaseDatabase.getInstance().getReference("Calls").child(Common.user.getId())
                                .push().setValue(receivers.get(position).getId()).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });



                        FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(receivers.get(position).getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        Token token=dataSnapshot.getValue(Token.class);
                                        com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(Common.user.getId(),receivers.get(position).getId(),false);
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
                                        data.setToId(receivers.get(position).getId());

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

                        Log.d("text",receivers.get(position).getPhone()+"");
                        Intent i=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+receivers.get(position).getPhone()));
                        context.startActivity(i);
                        Log.d("text2",receivers.get(position).getPhone()+"");

                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                com.rescue.blood.instablood.model.Call call=new com.rescue.blood.instablood.model.Call(Common.user.getId(),receivers.get(position).getId(),false);
                                context.startActivity(new Intent(context, MapsActivity.class).putExtra("key",call).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        },30000);

                    }
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return receivers.size();
    }

    public void add(Request x) {
        this.receivers.add(x);
        notifyDataSetChanged();
    }

    public void update(ArrayList<Request> pendingReceivers) {
        receivers.clear();
        receivers.addAll(pendingReceivers);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView no,namePhone,address,duration;
        Button call,cancel;
        public ViewHolder(View itemView) {
            super(itemView);
            call=itemView.findViewById(R.id.view);
            cancel=itemView.findViewById(R.id.cancel);
            no=itemView.findViewById(R.id.noOfPending);
            namePhone=itemView.findViewById(R.id.namePhone);
            address=itemView.findViewById(R.id.address);
            duration=itemView.findViewById(R.id.duration);
            itemView.findViewById(R.id.seentext).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.seen).setVisibility(View.INVISIBLE);

        }
    }
}
