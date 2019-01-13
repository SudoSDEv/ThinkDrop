package com.rescue.blood.instablood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.rescue.blood.instablood.R;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.model.User;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rescue.blood.instablood.common.Common.user;

/**
 * Created by sayan07 on 13/5/18.
 */

public class UserView extends AppCompatActivity {

    TextInputEditText name,phone,email,bg;
    CircleImageView dp;
    ImageButton nameE,phoneE,emailE,bgE,dpE;
    ProgressBar pb;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.user_view);
        pb=findViewById(R.id.progress);
        databaseReference= FirebaseDatabase.getInstance().getReference(Common.USERS);
        dp=findViewById(R.id.dp);
        name=findViewById(R.id.uName);
        phone=findViewById(R.id.phone);
        email=findViewById(R.id.email);
        bg=findViewById(R.id.bg);


        nameE=findViewById(R.id.nameEdit);
        phoneE=findViewById(R.id.phoneEdit);
        emailE=findViewById(R.id.mailEdit);
        bgE=findViewById(R.id.bgEdit);
        dpE=findViewById(R.id.picEdit);

        name.setEnabled(false);
        phone.setEnabled(false);
        email.setEnabled(false);
        bg.setEnabled(false);


        pb.setVisibility(View.GONE);
        User user= Common.user;
        name.setText(user.getName());
        phone.setText(user.getPhone());
        email.setText(user.getEmail());
        bg.setText(user.getBloodGroup());


        FirebaseStorage.getInstance().getReference("Profile Pictures").child(Common.user.getId())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(uri!=null) {
                    Log.d("ooo",uri.toString());
                    Glide.with(UserView.this).load(uri).into(dp);
                }
            }
        });

        dpE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 86);

            }
        });

        nameE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.isEnabled()) {
                    name.setEnabled(true);
                    name.requestFocus();
                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
                    nameE.setImageResource(R.drawable.ic_action_name);
                }else{
                    name.setEnabled(false);
                    nameE.setImageResource(R.drawable.edit);
                    Common.user.setName(name.getText().toString());
                    databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(Common.user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserView.this, "Name saved successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        emailE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.isEnabled()) {
                    email.setEnabled(true);
                    email.requestFocus();
                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT);
                    emailE.setImageResource(R.drawable.ic_action_name);
                }else{
                    email.setEnabled(false);
                    emailE.setImageResource(R.drawable.edit);
                    Common.user.setEmail(email.getText().toString());
                    databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(Common.user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserView.this, "Email saved successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        phoneE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!phone.isEnabled()) {
                    phone.setEnabled(true);
                    phone.requestFocus();
                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(phone, InputMethodManager.SHOW_IMPLICIT);
                    phoneE.setImageResource(R.drawable.ic_action_name);
                }else{
                    phone.setEnabled(false);
                    phoneE.setImageResource(R.drawable.edit);
                    Common.user.setPhone(phone.getText().toString());
                    databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(Common.user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserView.this, "Phone number saved successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        bgE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bg.isEnabled()) {
                    bg.setEnabled(true);
                    bg.requestFocus();
                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(bg, InputMethodManager.SHOW_IMPLICIT);
                    bgE.setImageResource(R.drawable.ic_action_name);
                }else{
                    bg.setEnabled(false);
                    bgE.setImageResource(R.drawable.edit);
                    Common.user.setBloodGroup(bg.getText().toString());
                    databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(Common.user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserView.this, "Blood group saved successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });





    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 86 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            pb.setVisibility(View.VISIBLE);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                dp.setImageBitmap(bitmap);
                FirebaseStorage.getInstance().getReference("Profile Pictures").child(Common.user.getId()).putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pb.setVisibility(View.GONE);
                                String url=taskSnapshot.getDownloadUrl().toString();
                                user.setDpUrl(url);
                                Glide.with(UserView.this).load(url).into(dp);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserView.this, "Failed to upload image. Retry", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
