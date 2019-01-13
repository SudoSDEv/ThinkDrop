package com.rescue.blood.instablood;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Token;
import com.rescue.blood.instablood.model.Call;
import com.rescue.blood.instablood.model.PastTransaction;
import com.rescue.blood.instablood.model.SuccessfulTrans;
import com.rescue.blood.instablood.model.User;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import ng.max.slideview.SlideView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Switch extends AppCompatActivity {

    boolean backPressedOnce=false;
    @Override
    protected void onResume() {


        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
        Common.updateToServer(Common.TOKEN);
        Log.d("key1","updatedToken");
        super.onResume();
    }

    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_switch);
        Common.context=getApplicationContext();
        //Common.trackCalls();

        final DatabaseReference transactionAck= FirebaseDatabase.getInstance().getReference("Transactionss/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                {

                    final String[] opp = new String [1];
                    opp[0]=dataSnapshot.getValue(String.class);
                    Log.d("opp",opp[0]);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(Switch.this);
                    builder.setTitle("Last Transaction");
                    FirebaseDatabase.getInstance().getReference(Common.USERS + "/" + opp[0])
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final User user = dataSnapshot.getValue(User.class);
                                    builder.setMessage("Did you completed the last transaction with "+ user.getName() +"?");
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference( "Transactionss/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                            FirebaseDatabase.getInstance().getReference("Transactions/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot!=null);
                                                            FirebaseDatabase.getInstance().getReference("Transactions/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                            SuccessfulTrans o=new SuccessfulTrans(FirebaseAuth.getInstance().getCurrentUser().getUid(),opp[0]);
                                            Log.d("ppp",o.id1+" "+o.id2);
                                            FirebaseDatabase.getInstance().getReference(Common.SUCCESSFUL_TRANSACTIONS).push().setValue(o).
                                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            PastTransaction x=new PastTransaction();
                                                            x.setEmail(user.getEmail());
                                                            x.setPhone(user.getPhone());
                                                            x.setName(user.getName());
                                                            DatabaseReference r=FirebaseDatabase.getInstance().getReference(Common.PAST_TRANSACTIONS+"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .push();
                                                            Log.d("ooo",r.getKey());
                                                            x.setFirebaseID(r.getKey());

                                                            r.setValue(x).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(Switch.this, "Thanx for the acknowledgement.", Toast.LENGTH_SHORT).show();

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(Switch.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Switch.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }
                                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            FirebaseDatabase.getInstance().getReference("Transactionss/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                            FirebaseDatabase.getInstance().getReference("Transactions/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot!=null);
                                                            FirebaseDatabase.getInstance().getReference("Transactions/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });                                            Toast.makeText(Switch.this, "Thanx for the acknowledgement.", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                    builder.show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(Switch.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        transactionAck.addValueEventListener(valueEventListener);

        // Log.d("token1", FirebaseInstanceId.getInstance().getToken());

        final User user= Common.user;

//        Log.d("key",user.getBloodGroup());
        SlideView donor=findViewById(R.id.donor);
        donor.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {

                SpotsDialog sd=new SpotsDialog(Switch.this,R.style.Custom);
                sd.show();
                sd.setCancelable(false);
                if(user!=null) {
                    startActivity(new Intent(Switch.this, WelcomeDonor2.class).putExtra("USER", user));
                    sd.dismiss();
                }
            }
        });

        SlideView receiver=findViewById(R.id.receiver);
        receiver.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                SpotsDialog sd=new SpotsDialog(Switch.this,R.style.Custom);
                sd.show();
                sd.setCancelable(false);
                if(user!=null) {
                    startActivity(new Intent(Switch.this, WelcomeReceiver.class).putExtra("USER", user));
                    sd.dismiss();
                }
            }
        });

        findViewById(R.id.signOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(Switch.this).setTitle("SIGN OUT")
                        .setMessage("Do you want to sign out?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(transactionAck!=null && valueEventListener!=null)
                                    transactionAck.removeEventListener(valueEventListener);
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(Switch.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();

                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
        });



    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {

        if(backPressedOnce) {
            FirebaseAuth.getInstance().signOut();
            super.onBackPressed();
            return;
        }
        else{
            backPressedOnce=true;
            Toast.makeText(getApplicationContext(),"Press twice to exit",Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedOnce=false;
                }
            },2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        MenuItem menuItem=menu.findItem(R.id.signOut);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onBackPressed();
                return true;
            }
        });

        return true;
    }


}
