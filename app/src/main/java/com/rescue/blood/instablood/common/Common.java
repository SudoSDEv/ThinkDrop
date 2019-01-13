package com.rescue.blood.instablood.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rescue.blood.instablood.MainActivity;
import com.rescue.blood.instablood.MapsActivity;
import com.rescue.blood.instablood.R;
import com.rescue.blood.instablood.model.Request;
import com.rescue.blood.instablood.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by sayan07 on 9/3/18.
 */

public class Common {

    public static Context context;
    public static ArrayList<Request> pendingReceivers=new ArrayList<>();
    public static User user=null;
    public static User offUser=null;
    public static String TOKEN=null;
    public static Location mLastLocation=null;
    public static final String DONORS="Donors";
    public static final String RECEIVERS="Receivers";
    public static final String BLOODREQUEST="BloodRequest";
    public static final String USERS="Users";
    public static final String TOKENS="Tokens";
    public static final String SUCCESSFUL_TRANSACTIONS="SuccessfulTransactions";
    public static final String PAST_TRANSACTIONS="PastTransactions";

    public static final String DIRECTIONS_API="";
    public static final String FCM_API="https://fcm.googleapis.com/";
    public static final String DIRECTION_API_KEY="AIzaSyANfMgR6YI2KpFmNcTLgiu-4GQrFHKWmM0";


    public static void updateToServer(String token) {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child(Common.TOKENS);
        Token token1=new Token(Common.TOKEN);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token1);

    }

    public static void setOnlineOfflineMode()
    {

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        final DatabaseReference userRef=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isConnected=dataSnapshot.getValue(boolean.class);

                if(isConnected && user!=null)
                {
                    Log.d("dis","connected");
                    user.setPassive(false);
                    userRef.setValue(user);

                    if(offUser==null)
                    offUser=new User(user);

                    offUser.setPassive(true);
                    userRef.onDisconnect().setValue(offUser);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*public static void trackCalls() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (Common.user != null) {
            reference.child("Calls/" + Common.user.getId()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setTitle("THINK DROP");
                    builder.setMessage("Did u agreed on the call? Confirm to start live tracking");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String opponent=dataSnapshot.getValue(String.class);
                            context.startActivity(new Intent(context, MapsActivity.class).putExtra("opponent",opponent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference("Calls/"+Common.user.getId())
                                    .child(dataSnapshot.getKey()).setValue(null);

                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    alertDialog.show();

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }*/
}