package com.rescue.blood.instablood;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.DirectionAPIResponse;
import com.rescue.blood.instablood.common.RetrofitClient;
import com.rescue.blood.instablood.model.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.rescue.blood.instablood.common.Common.user;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {


    private GoogleMap mMap;
    Call call;
    boolean isCaller;
    boolean isDonor;
    DatabaseReference referenceForO, referenceForU;
    GeoFire trackO, trackU;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiClient;
    public static final int PERMISSION_REQUEST_CODE=1996;
    public static final int PLAY_SERVICES_REQUEST_CODE=86;
    private Marker mCurrent,oCurrent;
    GeoFire geofireSetLocation;
    private boolean firstZoom=false;
    ArrayList<Double> locOfO;
    private boolean drewPath=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        drewPath=false;

        if(getIntent()==null)
        {
            call=new Gson().fromJson(getIntent().getExtras().getString("call"),Call.class);
        }
        else
            call= (Call) getIntent().getExtras().getSerializable("key");

        isDonor=call.isIsdonor();

        if(call.getCaller().equals(Common.user.getId())) isCaller=true;
        else
            isCaller=false;

        if(isDonor)
        {

            geofireSetLocation=new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.DONORS).child(user.getBloodGroup()));


            if(!isCaller) {
                Log.d("isc", "Donor");

                AlertDialog.Builder confirm=new AlertDialog.Builder(MapsActivity.this);
                confirm.setTitle("ACKNOWLEDGEMENT");
                confirm.setMessage("Do you want to confirm donating blood?");
                confirm.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        geofireSetLocation = new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.RECEIVERS).child(user.getBloodGroup()));

                        FirebaseDatabase.getInstance().getReference("Transactions/" + call.getCaller()).setValue("1");
                        if (isCaller) {
                            referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                            // referenceForU=FirebaseDatabase.getInstance().getReference(Common.RECEIVERS+"/"+Common.user.getBloodGroup()).child(call.getCaller());

                        } else {
                            referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getCaller());
                            //referenceForU = FirebaseDatabase.getInstance().getReference(Common.RECEIVERS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                        }

                        referenceForO.child("l").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                locOfO = (ArrayList<Double>) dataSnapshot.getValue();
                                if (isDonor) {
                                    if (oCurrent != null) oCurrent.remove();
                                    oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Receiver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                                } else {
                                    oCurrent.remove();
                                    oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Donor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                }
                                Log.d("call", "changed" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseDatabase.getInstance().getReference("Transactions/" + call.getCaller()).setValue("0");
                        onBackPressed();

                    }
                }).create().show();

            }
            else{

                final ProgressDialog pd=new ProgressDialog(MapsActivity.this);
                pd.setTitle("Please Wait");
                pd.setMessage("Waiting for acknowledgement from receiver..");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.show();

                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Transactions/"+call.getCaller());
                Log.d("kii",databaseReference.toString());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null)
                        {
                            pd.dismiss();
                        if (dataSnapshot.getValue(String.class).equals("1")) {
                            if (isCaller) {
                                Log.d("isc", "Caller");
                                referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                                //referenceForU=FirebaseDatabase.getInstance().getReference(Common.DONORS+"/"+Common.user.getBloodGroup()).child(call.getCaller());

                            } else {
                                Log.d("isr", "Re");
                                referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getCaller());
                                // referenceForU = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                            }

                            referenceForO.child("l").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    locOfO = (ArrayList<Double>) dataSnapshot.getValue();
                                    if (isDonor) {
                                        if (oCurrent != null) oCurrent.remove();
                                        oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Receiver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    } else {
                                        if (oCurrent != null) oCurrent.remove();
                                        oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Donor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    }

                                    Log.d("call", "changed" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            trackO = new GeoFire(referenceForO);
                            trackU = new GeoFire(referenceForU);
                        } else
                            Toast.makeText(MapsActivity.this, "Receiver has declined you offer.", Toast.LENGTH_SHORT).show();
                    }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            //trackO=new GeoFire(referenceForO);
            //trackU=new GeoFire(referenceForU);


        }else {

            geofireSetLocation = new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.RECEIVERS).child(user.getBloodGroup()));


            if(isCaller) {
                final ProgressDialog pd = new ProgressDialog(MapsActivity.this);
                pd.setTitle("Please Wait");
                pd.setMessage("Waiting for acknowledgement from donor..");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.show();

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Transactions/" + call.getCaller());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null)
                        {
                            pd.dismiss();
                        if (dataSnapshot.getValue(String.class).equals("1")) {
                            if (isCaller) {
                                Log.d("isc", "Caller");
                                referenceForO = FirebaseDatabase.getInstance().getReference(Common.RECEIVERS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                                referenceForU = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getCaller());

                            } else {
                                Log.d("isr", "Re");

                                referenceForO = FirebaseDatabase.getInstance().getReference(Common.RECEIVERS + "/" + Common.user.getBloodGroup()).child(call.getCaller());
                                referenceForU = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                            }


                            trackO = new GeoFire(referenceForO);
                            trackU = new GeoFire(referenceForU);

                            referenceForO.child("l").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    locOfO = (ArrayList<Double>) dataSnapshot.getValue();
                                    if (isDonor) {
                                        if (oCurrent != null) oCurrent.remove();
                                        oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Receiver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    } else {
                                        if (oCurrent != null) oCurrent.remove();
                                        oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Donor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                    }
                                    Log.d("call", "changed" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else
                            Toast.makeText(MapsActivity.this, "Donor has refused your request.", Toast.LENGTH_SHORT).show();
                    }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else{

                AlertDialog.Builder confirm=new AlertDialog.Builder(MapsActivity.this);
                confirm.setTitle("ACKNOWLEDGEMENT");
                confirm.setMessage("Do you want to confirm receiving blood?");
                confirm.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseDatabase.getInstance().getReference("Transactions/" + call.getCaller()).setValue("1");



                if (isCaller) {
                    referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                    // referenceForU=FirebaseDatabase.getInstance().getReference(Common.RECEIVERS+"/"+Common.user.getBloodGroup()).child(call.getCaller());

                } else {
                    referenceForO = FirebaseDatabase.getInstance().getReference(Common.DONORS + "/" + Common.user.getBloodGroup()).child(call.getCaller());
                    //referenceForU = FirebaseDatabase.getInstance().getReference(Common.RECEIVERS + "/" + Common.user.getBloodGroup()).child(call.getReceiver());
                }
                referenceForO.child("l").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        locOfO = (ArrayList<Double>) dataSnapshot.getValue();
                        if (isDonor) {
                            if(oCurrent!=null) oCurrent.remove();
                            oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Receiver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                        else {
                            if(oCurrent!=null) oCurrent.remove();
                            oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(locOfO.get(0), locOfO.get(1))).title("Donor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }

                        Log.d("call", "changed" + dataSnapshot.getKey() + " " + dataSnapshot.getValue());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseDatabase.getInstance().getReference("Transactions/" + call.getCaller()).setValue("0");

                        onBackPressed();

                    }
                }).create().show();

            }

        }




        Log.d("call",call.getReceiver()+" "+call.getCaller());
        setUpLocation();

    }

    private void setUpLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            if(isPlayServicesAvailable()){
                Log.d("MyApp","PS avalaible");

                buildApiClient();
                Log.d("MyApp","Client built");

                setLocationRequest();
                Log.d("MyApp","location Set");

                displayLocation();
                Log.d("MyApp","Done");

            }

        }        else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);
        }

    }

    private void setLocationRequest() {

        mLocationRequest= LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setFastestInterval(1000).setInterval(3000).setSmallestDisplacement(0);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, 0);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });

    }

    private void buildApiClient() {
        GoogleApiClient.Builder builder=new GoogleApiClient.Builder(this);
        builder.addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this);
        mApiClient=builder.build();
        mApiClient.connect();
    }

    private boolean isPlayServicesAvailable() {

        GoogleApiAvailability apiAvailability=GoogleApiAvailability.getInstance();
        int code=apiAvailability.isGooglePlayServicesAvailable(this);
        if(code== ConnectionResult.SUCCESS){
            return true;
        }else{
            if(apiAvailability.isUserResolvableError(code))
            {
                apiAvailability.getErrorDialog(this,code,PLAY_SERVICES_REQUEST_CODE);
            }
            else{
                Log.d("MyApp","PS error");
                Snackbar.make(findViewById(R.id.map),"Your device is not supported. We pray for early blood recovery, From : I N S T A B L O O D",Snackbar.LENGTH_LONG).show();
            }
            return false;
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        displayLocation();
        //findDonors();
        Log.d("MyApp1","Connected");
    }

    private void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            Toast.makeText(getApplicationContext(),"u",Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,mLocationRequest,this);
            Log.d("MyApp","requested");
        }
        else
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Common.mLastLocation=location;
        Log.d("Changed","Location changed");
        displayLocation();
        Log.d("location",location.getLatitude()+" "+location.getLongitude());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    setUpLocation();
                break;
            case 86:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    //Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + receiver.getPhone()));
                    startActivity(new Intent(MapsActivity.this,PendingReceiver.class));
                }
                break;
            default: Toast.makeText(this,"Coding error!",Toast.LENGTH_LONG).show();
                break;

        }

    }

    @Override
    public void onBackPressed() {
            finish();
            firstZoom=false;
            removeLocationUpdates();
            super.onBackPressed();

    }

    private void removeLocationUpdates() {

        if(mCurrent!=null) mCurrent.remove();
        if(mMap!=null) mMap.clear();
        if(mApiClient!=null && mApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
      /*  LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        if(isDonor)
        mCurrent=mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney").icon(BitmapDescriptorFactory.defaultMarker()));
        else
        mCurrent=mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney").icon(BitmapDescriptorFactory.defaultMarker()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));*/
    }


    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            if(mCurrent!=null) mCurrent.remove();

            Common.mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            if(Common.mLastLocation!=null) {
                if(!drewPath) {
                    if(locOfO!=null) {
                        drawPath(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), new LatLng(locOfO.get(0), locOfO.get(1)));
                        drewPath = true;
                    }
                }
                Log.d("MyApp",Common.mLastLocation.getLatitude()+" "+Common.mLastLocation.getLongitude());
               geofireSetLocation.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(mCurrent!=null) mCurrent.remove();
                        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                        if(user==null) Log.d("Key","Null");
                        String name=user.getDisplayName();
                        if(TextUtils.isEmpty(name)) name="Guest";
                        if(isDonor)
                            mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())).title("You(Donor)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        else
                            mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())).title("You(Receiver)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mCurrent.showInfoWindow();
                        if(firstZoom==false) {
                            LatLng ll = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16.0f));
                            firstZoom=true;
                        }
                        Log.d("LL","Done");
                    }
                });
                Log.d("LL","OK");
                //  Toast.makeText(this, "Lat : " + mLastLocation.getLatitude() + " Lon : " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }

        }
        else
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);

    }


    public void drawPath(LatLng currentPosition,LatLng destination)
    {
        final String fullURL="https://maps.googleapis.com/maps/api/directions/json?origin="+currentPosition.latitude+","+currentPosition.longitude+"&destination="+destination.latitude+","+destination.longitude+"&mode=driving&key="+getResources().getString(R.string.directionAPIKey);
        DirectionAPIResponse directionAPIResponse= RetrofitClient.getRetrofitClient("https://maps.googleapis.com", ScalarsConverterFactory.create()).create(DirectionAPIResponse.class);
        directionAPIResponse.getResponse(fullURL).enqueue(new Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, Response<String> response) {
                if (response.isSuccessful()){

                    try {
                        JSONObject jsonObject=new JSONObject(response.body());
                        JSONArray jsonArray=jsonObject.getJSONArray("routes");
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            jsonObject=jsonArray.getJSONObject(i).getJSONObject("overview_polyline");
                            String points=jsonObject.getString("points");
                            ArrayList<LatLng> latLngArrayList=decodePoints(points);
                            Log.d("LL",latLngArrayList.get(0).latitude+" "+latLngArrayList.get(0).longitude+" "+latLngArrayList.size());

                            final LatLngBounds.Builder builder=new LatLngBounds.Builder();
                            for(LatLng latLng:latLngArrayList)
                                builder.include(latLng);
                            LatLngBounds latLngBounds=builder.build();

                            PolylineOptions blackPolylineOptions=new PolylineOptions();
                            blackPolylineOptions.addAll(latLngArrayList);
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.width(5.0f);
                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.endCap(new SquareCap());

                            mMap.addPolyline(blackPolylineOptions);

                            /*ValueAnimator valueAnimator=ValueAnimator.ofInt(0,100);
                            valueAnimator.setDuration(10000);
                            valueAnimator.setInterpolator(new LinearInterpolator());
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    int v=Integer.parseInt(valueAnimator.getAnimatedValue()+"");
                                    LatLng latLng=latLngArrayList.get(v*latLngArrayList.size()/100);
                                    referenceForO.child("l/0").setValue(latLng.latitude);
                                    referenceForO.child("l/1").setValue(latLng.longitude);
                                    Log.d("anim",valueAnimator.getAnimatedValue()+"");
                                }
                            });
                            valueAnimator.start();*/

                           for(LatLng x:latLngArrayList)//demo
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();

                                oCurrent.remove();
                                Log.d("xxx",x.latitude+"");
                                if(isDonor) {
                                    oCurrent.remove();
                                    oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(x.latitude, x.longitude)).title("Receiver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                }
                                else {
                                    oCurrent.remove();
                                    oCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(x.latitude, x.longitude)).title("Donor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                }
                                Location l=new Location("");
                                l.setLatitude(x.latitude);
                                l.setLatitude(x.longitude);
                                float r[]=new float[1];
                                Location.distanceBetween(x.latitude,x.longitude,Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude(),r);
                                Log.d("reach",r[0]+" "+" "+x.latitude+","+x.longitude+" "+Common.mLastLocation.distanceTo(l));
                                Log.d("reach",r[0]+" "+" "+Common.mLastLocation.getLatitude()+","+Common.mLastLocation
                                        .getLongitude()+" "+Common.mLastLocation.distanceTo(l));

                                if(r[0]<50){
                                    if(isDonor) {
                                        if(isCaller)
                                        FirebaseDatabase.getInstance().getReference("Transactionss/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(MapsActivity.this.call.getReceiver());
                                        else
                                            FirebaseDatabase.getInstance().getReference("Transactionss/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(MapsActivity.this.call.getCaller());
                                        Toast.makeText(MapsActivity.this, "Donor has reached you.", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder seshKorbo=new AlertDialog.Builder(MapsActivity.this);
                                        seshKorbo.setTitle("Final Step");
                                        seshKorbo.setMessage("Press OK to confirm donating blood.");
                                        seshKorbo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Toast.makeText(MapsActivity.this, "You can close the app. Hope you save a life @ThinkDrop", Toast.LENGTH_SHORT).show();
                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).create().show();
                                    }
                                    else {
                                        Toast.makeText(MapsActivity.this, "You reached target.", Toast.LENGTH_SHORT).show();
                                        if(isCaller)
                                            FirebaseDatabase.getInstance().getReference("Transactionss/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(MapsActivity.this.call.getReceiver());
                                        else
                                            FirebaseDatabase.getInstance().getReference("Transactionss/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(MapsActivity.this.call.getCaller());
                                        AlertDialog.Builder seshKorbo=new AlertDialog.Builder(MapsActivity.this);
                                        seshKorbo.setTitle("Final Step");
                                        seshKorbo.setMessage("Press OK to confirm receiving blood.");
                                        seshKorbo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Toast.makeText(MapsActivity.this, "You can close the app. Hope you save a life @ThinkDrop", Toast.LENGTH_SHORT).show();
                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).create().show();
                                    }
                                }

                            }
                          // oCurrent.remove();
                           // oCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(latLngArrayList.get(latLngArrayList.size()-1).latitude,latLngArrayList.get(latLngArrayList.size()-1).longitude)).title("Donors").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else Toast.makeText(getApplicationContext(),"Failure",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {

            }
        });
    }

    private ArrayList<LatLng> decodePoints(String encoded) {

        ArrayList poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);

        }

        return poly;

    }



    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
