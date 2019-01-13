package com.rescue.blood.instablood;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Guest;
import com.rescue.blood.instablood.model.User;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String[] bloodGroups;
    String bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Raleway-SemiBold.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);
        bloodGroups=getResources().getStringArray(R.array.bloodGroups);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            final SpotsDialog sd=new SpotsDialog(this,R.style.Custom);
            sd.setCancelable(false);
            sd.setTitle("Logging you in..");
            sd.show();
            FirebaseDatabase.getInstance().getReference(Common.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Common.user=dataSnapshot.getValue(User.class);
                            if(Common.user!=null) {
                                sd.dismiss();
                                Common.user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                startActivity(new Intent(getApplicationContext(), Switch.class));
                                finish();
                            }
                            else {
                                sd.dismiss();
                                Toast.makeText(MainActivity.this, "Problem on Server, retry later.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Snackbar.make(findViewById(R.id.content_main), "Failed : " + databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                            sd.dismiss();
                        }
                    });
        }

        TextView txt = (TextView) findViewById(R.id.emergency);
        txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView tv = (TextView) findViewById(R.id.title);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Arkhip_font.ttf");

        tv.setTypeface(face);

        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        txt.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        //MultiDex.install(this);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        final View root = findViewById(R.id.content_main);
        switch (id) {
            case R.id.register:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("REGISTER");
                builder.setMessage("Please use email to register");
                final View view = LayoutInflater.from(this).inflate(R.layout.register_layout, null);

                final Spinner bg1 = view.findViewById(R.id.bg);
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(MainActivity.this,R.layout.spinner_list_item,bloodGroups);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bg1.setAdapter(arrayAdapter);
                bg1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        bg=bloodGroups[i];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                builder.setView(view);
                builder.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        MaterialEditText email = (MaterialEditText) view.findViewById(R.id.email);
                        MaterialEditText pass = (MaterialEditText) view.findViewById(R.id.password);
                        final MaterialEditText name = (MaterialEditText) view.findViewById(R.id.name);
                        MaterialEditText phone = (MaterialEditText) view.findViewById(R.id.ph);

                        if (TextUtils.isEmpty(email.getText().toString())) {
                            Snackbar.make(root, "Please enter email", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (TextUtils.isEmpty(pass.getText().toString())) {
                            Snackbar.make(root, "Please enter password", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (TextUtils.isEmpty(name.getText().toString())) {
                            Snackbar.make(root, "Please enter your name", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (TextUtils.isEmpty(phone.getText().toString())) {
                            Snackbar.make(root, "Please enter phone number", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (TextUtils.isEmpty(bg)) {
                            Snackbar.make(root, "Please enter blood group", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        final User mUser=new User(email.getText().toString(),pass.getText().toString(),name.getText().toString(),phone.getText().toString(),bg);
                        final SpotsDialog sd = new SpotsDialog(root.getContext(), R.style.Custom);
                        sd.setCancelable(false);
                        sd.show();
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest cr=new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()+" "+bg).build();
                                user.updateProfile(cr).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        user.sendEmailVerification();
                                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference(Common.USERS);
                                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.hasChild(user.getUid())){
                                                    mUser.setId(user.getUid());
                                                    databaseReference.child(Common.USERS).child(firebaseAuth.getCurrentUser().getUid()).setValue(mUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sd.dismiss();
                                                            Snackbar.make(root,"Account created. Please verify your email to login.",Snackbar.LENGTH_LONG).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Snackbar.make(root, "Failed : " + e.getMessage(), Snackbar.LENGTH_LONG).show();

                                                        }
                                                    });
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });
                                sd.dismiss();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sd.dismiss();
                                Snackbar.make(root, "Failed : " + e.getMessage(), Snackbar.LENGTH_LONG).show();

                            }
                        });

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            case R.id.login:

                builder = new AlertDialog.Builder(this);
                builder.setTitle("LOGIN");
                builder.setMessage("Please use email to login");
                final View view1 = LayoutInflater.from(this).inflate(R.layout.login_layout, null);
                builder.setView(view1);
                builder.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MaterialEditText email = (MaterialEditText) view1.findViewById(R.id.email);
                        MaterialEditText pass = (MaterialEditText) view1.findViewById(R.id.password);

                        if (TextUtils.isEmpty(email.getText().toString())) {
                            Snackbar.make(root, "Please enter email", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (TextUtils.isEmpty(pass.getText().toString())) {
                            Snackbar.make(root, "Please enter password", Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        final android.app.AlertDialog sd = new SpotsDialog(root.getContext(), R.style.Custom);
                        sd.setCancelable(false);
                        sd.show();

                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //start Welcome activity
                                final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                if(user!=null && user.isEmailVerified()) {
                                    databaseReference.child(Common.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    User user=dataSnapshot.getValue(User.class);
                                                    user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                    //MainActivity.this.finish();
                                                    Common.user=user;
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Common.setOnlineOfflineMode();
                                                        }
                                                    }).start();
                                                    //MainActivity.this.finish();
                                                    sd.dismiss();
                                                    startActivity(new Intent(getApplicationContext(), Switch.class).putExtra("USER",user));
                                                    finish();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }else{
                                    Toast.makeText(getApplicationContext(),"Please verify your email/ Check your Internet connectivity",Toast.LENGTH_LONG).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sd.dismiss();
                                Snackbar.make(root, "Failed : Their is no record corresponding to this user.", Snackbar.LENGTH_LONG).show();
                            }
                        });

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

                break;

            case R.id.emergency:
                final android.app.AlertDialog sd = new SpotsDialog(root.getContext(), R.style.Custom);
                sd.setCancelable(false);
                sd.show();

                firebaseAuth.signInWithEmailAndPassword(Guest.email, Guest.pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //start Welcome activity
                        sd.dismiss();
                        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                        if(user!=null && user.isEmailVerified()) {
                            final User user1=new User(Guest.email,Guest.pass,"","","");
                                            user1.setId(user.getUid());
                                            //MainActivity.this.finish()
                                            Common.user=user1;

                                            AlertDialog.Builder choose=new AlertDialog.Builder(MainActivity.this);
                                            choose.setTitle("Please provide us two details.");
                                            final View view=LayoutInflater.from(MainActivity.this).inflate(R.layout.guest_choose,null);
                                            choose.setView(view);
                                            final MaterialEditText uName=view.findViewById(R.id.name);
                                            final Spinner bg1 = view.findViewById(R.id.bg);
                                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(MainActivity.this,R.layout.spinner_list_item,bloodGroups);
                                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            bg1.setAdapter(arrayAdapter);
                                            bg1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    bg=bloodGroups[i];
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });
                                            final MaterialEditText ph=view.findViewById(R.id.ph);
                                            choose.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    user1.setName(uName.getText().toString());
                                                    user1.setPhone(ph.getText().toString());
                                                    user1.setBloodGroup(bg);
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Common.setOnlineOfflineMode();
                                                        }
                                                    }).start();
                                                    //MainActivity.this.finish();
                                                    sd.dismiss();
                                                    startActivity(new Intent(MainActivity.this, WelcomeReceiver.class).putExtra("USER", user1).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                                                    //startActivity(new Intent(getApplicationContext(), Switch.class).putExtra("USER",user1));
                                                    finish();
                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                       dialogInterface.dismiss();
                                                }
                                            });
                                            choose.create().show();


                        }else{
                            Toast.makeText(getApplicationContext(),"Please verify your email/ Check your Internet connectivity",Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sd.dismiss();
                        Snackbar.make(root, "Failed : Their is no record corresponding to this user.", Snackbar.LENGTH_LONG).show();
                    }
                });

                break;
        }

    }



}
