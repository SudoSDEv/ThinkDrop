package com.rescue.blood.instablood;

import android.*;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Data;
import com.rescue.blood.instablood.common.DirectionAPIResponse;
import com.rescue.blood.instablood.common.FCMJsonSender;
import com.rescue.blood.instablood.common.FCMRequest;
import com.rescue.blood.instablood.common.FCMResponse;
import com.rescue.blood.instablood.common.FCMRetrofitClient;
import com.rescue.blood.instablood.common.MyCustomInfoWindow;
import com.rescue.blood.instablood.common.MyFireBaseMessagingService;
import com.rescue.blood.instablood.common.RetrofitClient;
import com.rescue.blood.instablood.common.Token;
import com.rescue.blood.instablood.model.Request;
import com.rescue.blood.instablood.model.User;

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


public class WelcomeDonor2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private int currentLayout;
    private boolean isReceiverFound=false,isDonorAgreed=false,firstZoom=false;
    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    GoogleApiClient mApiClient;
    LocationRequest mLocationRequest;
    double radius = 5;
    public static final double LIMIT=100; //max radius(in km) range
    Marker mCurrent;
    Button findRoute;
    LatLng currentPosition;
    DirectionAPIResponse directionAPIResponse;
    ArrayList<LatLng> latLngArrayList;
    LatLngBounds latLngBounds;
    public static final int PERMISSION_REQUEST_CODE=1996;
    public static final int PLAY_SERVICES_REQUEST_CODE=86;
    MaterialAnimatedSwitch donorOnline;
    GeoFire geofireSetLocation,geofireTrackR;
    GeoQuery queryReceivers;
    DatabaseReference databaseReference;
    private Retrofit retrofit;
    EditText destination;
    Polyline blackPolyLine;
    PolylineOptions blackPolylineOptions;
    String selectedBloodGroup;
    User mUser;//guest user
    static boolean checkLastLocationStatus=false;
    Button donateB;
    ArrayList<String> receiverIDs=new ArrayList<>();
    ArrayList<Request> receivers=new ArrayList<>();
    DatabaseReference monitorReceivers;
    DatabaseReference checkOnlineStatus;

    @Override
    protected void onResume() {
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
        Common.updateToServer(Common.TOKEN);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Raleway-SemiBold.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_welcome_donor2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("RECEIVERS");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),ReceiversView.class).putExtra("key",receivers));

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseDatabase.getInstance().goOnline();

        mUser= Common.user;

        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(WelcomeDonor2.this,MainActivity.class));
        }
        if(mUser==null) {
            mUser = new User();
            mUser.setName("guest07");
            mUser.setBloodGroup("B+");
        }

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            setUpLocation();

        //loadAllReceivers();

        checkOnlineStatus=FirebaseDatabase.getInstance().getReference("test");
        checkOnlineStatus.setValue("HELLO WORLD!");

        if(retrofit==null)
            retrofit= RetrofitClient.getRetrofitClient("https://maps.googleapis.com", ScalarsConverterFactory.create());

        databaseReference= FirebaseDatabase.getInstance().getReference().child(Common.DONORS).child(mUser.getBloodGroup());//adjust b group
        geofireSetLocation=new GeoFire(databaseReference);//same here
        geofireTrackR=new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.RECEIVERS).child(mUser.getBloodGroup()));
        Log.d("user",FirebaseAuth.getInstance().getCurrentUser().getUid());
        //donorOnline=findViewById(R.id.donorOnline);
        //if(donorOnline.isChecked()){

        Snackbar.make(findViewById(R.id.map),"You are online",Snackbar.LENGTH_SHORT).show();
        //startLocationUpdates();
        //displayLocation();
        /*}
        else{
            removeLocationUpdates();
            Snackbar.make(findViewById(R.id.donorOnline),"You are offline",Snackbar.LENGTH_SHORT).show();

        }*/
    }

    private void sendNotificationsTOReceivers() {

        GeoFire geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.BLOODREQUEST));
        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

        if(isReceiverFound) {

            donateB.setText("Connecting to Receiver...");

            if (mCurrent != null)
                mCurrent.remove();

            mCurrent = mMap.addMarker(new MarkerOptions().title("I'm here").snippet(mUser.getBloodGroup())
                    .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude())));
            mCurrent.showInfoWindow();

            final Retrofit retrofit = FCMRetrofitClient.getRetrofitClient(Common.FCM_API);
            Log.d("Size", receiverIDs.size() + "");
            for (final String id : receiverIDs) {
                FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(id).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Token token = dataSnapshot.getValue(Token.class);
                                String latLngJson = new Gson().toJson(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                                String userInfo=new Gson().toJson(mUser);
                                Log.d("kk", latLngJson);
                                com.rescue.blood.instablood.common.Notification myNotification = new com.rescue.blood.instablood.common.Notification();
                                myNotification.setBody("Touch to view receiver.");
                                myNotification.setClickAction("RequestForBlood");
                                myNotification.setTitle(mUser.getName() + ": Request for " + mUser.getBloodGroup() + " blood");
                                Log.d("kk", new Gson().toJson(myNotification));
                                Data data=new Data();
                                data.setBody(latLngJson);
                                data.setUser(userInfo);

                                FCMJsonSender sender = new FCMJsonSender();

                                sender.setPriority("high");
                                sender.setTo(token.getToken());
                                sender.setNotification(myNotification);
                                sender.setData(data);

                                Log.d("json", new Gson().toJson(sender));

                                retrofit.create(FCMRequest.class)
                                        .sendRequest(sender).enqueue(new Callback<FCMResponse>() {
                                    @Override
                                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                        Log.d("notifiedd", response.body().toString());

                                    }

                                    @Override
                                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                                        Log.d("notifiedd", t.getMessage());

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                Log.d("notified", "cancelled");

                            }
                        }
                );
            }

            Toast.makeText(getApplicationContext(), "Everyone notified", Toast.LENGTH_SHORT).show();
            donateB.setText("Informed");

        }

    }



    private void loadAllReceivers(){
        receiverIDs.clear();
        receivers.clear();
        mMap.clear();
        mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())).title("You ("+mUser.getName()+")").snippet(mUser.getBloodGroup()));
        if(queryReceivers!=null) queryReceivers.removeAllListeners();
        findReceivers();
    }

    private void findReceivers() {


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            queryReceivers=geofireTrackR.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),radius);
            queryReceivers.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(final String key, final GeoLocation location) {

                    isReceiverFound=true;

                    Log.d("checkk",key+" "+FirebaseAuth.getInstance().getCurrentUser().getUid());

                    if(!key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        FirebaseDatabase.getInstance().getReference().child(Common.USERS)
                                .child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final User user = dataSnapshot.getValue(User.class);

                                        if(!receiverIDs.contains(key) && !key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                            receiverIDs.add(key);
                                            Request request=new Request(user.getName(),user.getPhone(),"Fetching Info...","Fetching Info...",user.getId());
                                            receivers.add(request);
                                        }

                                        //if(!user.isPassive())
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                                .title(user.getName()).snippet(user.getBloodGroup()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                        //else
                                        //  mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude,location.longitude))
                                        //        .title(user.getName()).snippet(user.getBloodGroup()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                    if(!isReceiverFound && radius<=LIMIT){
                        radius++;
                        queryReceivers.removeAllListeners();
                        findReceivers();
                    }

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
        else
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);

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

    private void removeLocationUpdates() {

        if(mCurrent!=null) mCurrent.remove();
        if(mMap!=null) mMap.clear();
        if(mApiClient!=null && mApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
        if(queryReceivers!=null) queryReceivers.removeAllListeners();

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

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            if(mCurrent!=null) mCurrent.remove();

            Common.mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            if(Common.mLastLocation!=null) {
                if(checkLastLocationStatus==false) {
                    loadAllReceivers();
                    monitorReceivers=FirebaseDatabase.getInstance().getReference(Common.DONORS).child(mUser.getBloodGroup());
                    monitorReceivers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadAllReceivers();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    checkLastLocationStatus = true;
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
                        mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())).title("You ("+mUser.getName()+")").snippet(mUser.getBloodGroup()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
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

    private void setLocationRequest() {

        mLocationRequest=LocationRequest.create();
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
                            status.startResolutionForResult(WelcomeDonor2.this, 0);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        // Add a marker in Kolkata and move the camera
        LatLng sydney = new LatLng(22.57,88.36);
        mMap.setInfoWindowAdapter(new MyCustomInfoWindow(this));
        mCurrent=mMap.addMarker(new MarkerOptions().position(sydney).title("Kolkata").snippet("B+").icon(BitmapDescriptorFactory.defaultMarker()));
        mCurrent.showInfoWindow();
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
                    startActivity(new Intent(WelcomeDonor2.this,PendingReceiver.class));
                }
                break;
            default: Toast.makeText(this,"Coding error!",Toast.LENGTH_LONG).show();
                break;

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            firstZoom=false;
            checkLastLocationStatus=false;
            removeLocationUpdates();
            super.onBackPressed();
        }
    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.requests) {

            if (ActivityCompat.checkSelfPermission(WelcomeDonor2.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(WelcomeDonor2.this,new String[]{android.Manifest.permission.CALL_PHONE},86);
            }else {
                startActivity(new Intent(WelcomeDonor2.this,PendingReceiver.class));
            }

        } else if (id == R.id.acc_manage) {

            startActivity(new Intent(WelcomeDonor2.this,UserView.class));

        } else if (id == R.id.bug_report) {

            AlertDialog.Builder builder=new AlertDialog.Builder(WelcomeDonor2.this);
            builder.setTitle("FeedBack");
            builder.setMessage("Fill out the form..");
            View view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.complain,null);
            final MaterialEditText submit=view.findViewById(R.id.complain);
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(submit, 0);
            builder.setView(view);
            builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    FirebaseDatabase.getInstance().getReference("Complains").child(Common.user.getId())
                            .setValue(submit.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(WelcomeDonor2.this, "We are sorry for inconvenience, your complain is successfully submitted.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(WelcomeDonor2.this, "We are sorry for inconvenience, please retry.", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.dismiss();
                }
            });

            builder.create().show();


        } else if (id == R.id.about) {

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("ThinkDrop demands your silence.");
            builder.setMessage("Are you sure you want to view this?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(WelcomeDonor2.this,About.class));
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Toast.makeText(WelcomeDonor2.this, "We are not that bad.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setCancelable(true);
            builder.create().show();

        } else if (id == R.id.exit) {
            WelcomeDonor2.this.finish();
        }
        else if (id==R.id.cr)
        {
            startActivity(new Intent(WelcomeDonor2.this,PastTransactions.class));
        }
        else if(id==R.id.logOut){


            firstZoom=false;
            checkLastLocationStatus=false;
            removeLocationUpdates();

                    new AlertDialog.Builder(WelcomeDonor2.this).setTitle("SIGN OUT")
                            .setMessage("Do you want to sign out?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    FirebaseAuth.getInstance().signOut();

                                    startActivity(new Intent(WelcomeDonor2.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                }
                            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
