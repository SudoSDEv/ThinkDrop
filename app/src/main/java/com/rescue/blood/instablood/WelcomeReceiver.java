package com.rescue.blood.instablood;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rescue.blood.instablood.common.Common.mLastLocation;

public class WelcomeReceiver extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private boolean isDonorFound=false,isDonorAgreed=false,firstZoom=false;
    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    GoogleApiClient mApiClient;
    LocationRequest mLocationRequest;
    double radius = 5;
    public static final double LIMIT=100000000; //max radius(in km) range
    Marker mCurrent;
    LatLng currentPosition;
    DirectionAPIResponse directionAPIResponse;
    ArrayList<LatLng> latLngArrayList;
    LatLngBounds latLngBounds;
    public static final int PERMISSION_REQUEST_CODE=1996;
    public static final int PLAY_SERVICES_REQUEST_CODE=86;
    MaterialAnimatedSwitch donorOnline;
    GeoFire geofireSetLocation,geofireTrackR;
    GeoQuery queryDonors;
    DatabaseReference databaseReference;
    private Retrofit retrofit;
    EditText destination;
    Polyline blackPolyLine;
    PolylineOptions blackPolylineOptions;
    String selectedBloodGroup;
    User mUser;//guest user
    static boolean checkLastLocationStatus=false,everyOneNotified=false;
    Button requestB;
    ArrayList<String> donorIDS=new ArrayList<>();
    ArrayList<Request> donors=new ArrayList<>();
    DatabaseReference monitorDonors;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Raleway-SemiBold.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_welcome_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("DONORS");
        setSupportActionBar(toolbar);
        requestB=findViewById(R.id.requestBlood);

        FirebaseDatabase.getInstance().goOnline();

        mUser= (User) getIntent().getSerializableExtra("USER");
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(WelcomeReceiver.this,MainActivity.class));
        }
        /*if(mUser==null) {
            mUser = new User();
            mUser.setName("thinkGuest");
            mUser.setBloodGroup("B+");
        }*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpLocation();

        requestB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(everyOneNotified==false)
                sendNotificationsTODonors();
                else
                    startActivity(new Intent(WelcomeReceiver.this,DonorsView.class).putExtra("key",donors));
            }
        });
        //loadAllReceivers();

        checkOnlineStatus=FirebaseDatabase.getInstance().getReference("test");
        checkOnlineStatus.setValue("HELLO WORLD!");

        if(retrofit==null)
            retrofit= RetrofitClient.getRetrofitClient("https://maps.googleapis.com", ScalarsConverterFactory.create());

        databaseReference= FirebaseDatabase.getInstance().getReference().child(Common.RECEIVERS).child(mUser.getBloodGroup());//adjust b group
        geofireSetLocation=new GeoFire(databaseReference);//same here
        geofireTrackR=new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.DONORS).child(mUser.getBloodGroup()));
        Log.d("user",FirebaseAuth.getInstance().getCurrentUser().getUid());
        //if(donorOnline.isChecked()){

        Snackbar.make(findViewById(R.id.map),"You are online",Snackbar.LENGTH_SHORT).show();
        //startLocationUpdates();
        //displayLocation();
        /*}
        else{
            removeLocationUpdates();
            Snackbar.make(findViewById(R.id.donorOnline),"You are offline",Snackbar.LENGTH_SHORT).show();

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void sendNotificationsTODonors() {

        GeoFire geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference().child(Common.BLOODREQUEST));
        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        if(isDonorFound) {

            requestB.setText("Requesting Blood...");

            if (mCurrent != null)
                mCurrent.remove();

            mCurrent = mMap.addMarker(new MarkerOptions().title("I'm here").snippet(mUser.getBloodGroup())
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            mCurrent.showInfoWindow();

            final Retrofit retrofit = FCMRetrofitClient.getRetrofitClient(Common.FCM_API);
            Log.d("Size", donorIDS.size() + "");
            for (final String id : donorIDS) {
                FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(id).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Token token = dataSnapshot.getValue(Token.class);
                                String latLngJson = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
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
                                data.setToId(id);
                                data.setToken(Common.TOKEN);
                                data.setReceiver(true);

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
                                        if(response.body().getSuccess()==1)
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

            everyOneNotified=true;
            Toast.makeText(getApplicationContext(), "Everyone notified", Toast.LENGTH_SHORT).show();
            requestB.setText("View/Call Donors");

        }
        else
            Toast.makeText(this, "No donor found.", Toast.LENGTH_SHORT).show();

    }



    private void loadAllDonors(){
        donorIDS.clear();
        mMap.clear();
        mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())).title("You ("+mUser.getName()+")").snippet(mUser.getBloodGroup()));
        if(queryDonors!=null) queryDonors.removeAllListeners();
        findDonors();
    }

    private void findDonors() {


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            if(mLastLocation!=null) {
                queryDonors = geofireTrackR.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);
                queryDonors.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, final GeoLocation location) {

                        isDonorFound = true;


                        Log.d("checkk", key + " " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                        if (!key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child(Common.USERS)
                                    .child(key)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);

                                            if (!donorIDS.contains(key) && !key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                donorIDS.add(key);
                                                Request request = new Request(user.getName(), user.getPhone(), "Fetching Info...", "Fetching Info...", user.getId());
                                                donors.add(request);
                                            }
                                            if (!user.isPassive())
                                                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                                        .title(user.getName()).snippet(user.getBloodGroup()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                            else
                                                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                                        .title(user.getName()).snippet(user.getBloodGroup()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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

                        if (!isDonorFound && radius <= LIMIT) {
                            radius++;
                            queryDonors.removeAllListeners();
                            findDonors();
                        }

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
            }
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
        if(queryDonors!=null) queryDonors.removeAllListeners();

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

            mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            if(mLastLocation!=null) {
                if(checkLastLocationStatus==false) {
                    loadAllDonors();
                    monitorDonors=FirebaseDatabase.getInstance().getReference(Common.DONORS).child(mUser.getBloodGroup());
                    monitorDonors.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    loadAllDonors();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    checkLastLocationStatus = true;
                }
                Log.d("MyApp1",mLastLocation.getLatitude()+" "+mLastLocation.getLongitude());
                geofireSetLocation.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(mCurrent!=null) mCurrent.remove();
                        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                        if(user==null) Log.d("Key","Null");
                        String name=user.getDisplayName();
                        if(TextUtils.isEmpty(name)) name="Guest";
                        if(mLastLocation!=null) {
                            mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("You (" + mUser.getName() + ")").snippet(mUser.getBloodGroup()));
                            mCurrent.showInfoWindow();
                            if (firstZoom == false) {
                                LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16.0f));
                                firstZoom = true;
                            }
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
                            status.startResolutionForResult(WelcomeReceiver.this, 0);
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

        if (id == R.id.bug_report) {

            AlertDialog.Builder builder=new AlertDialog.Builder(WelcomeReceiver.this);
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
                            Toast.makeText(WelcomeReceiver.this, "We are sorry for inconvenience, your complain is successfully submitted.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(WelcomeReceiver.this, "We are sorry for inconvenience, please retry.", Toast.LENGTH_SHORT).show();

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
                    startActivity(new Intent(WelcomeReceiver.this,About.class));
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Toast.makeText(WelcomeReceiver.this, "We are not that bad.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setCancelable(true);
            builder.create().show();

        } else if (id == R.id.exit) {
            WelcomeReceiver.this.finish();
        }
        else if(id==R.id.change)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(WelcomeReceiver.this);
            builder.setTitle("Choose group");
            final View v=LayoutInflater.from(this).inflate(R.layout.choose_bg,null);
            final RadioGroup bgs=v.findViewById(R.id.BGs);
            builder.setView(v);
            final RadioButton[] radioButton = new RadioButton[1];
            builder.setPositiveButton("SHOW", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int Id=bgs.getCheckedRadioButtonId();
                    radioButton[0] =v.findViewById(Id);
                    User user=new User(mUser);
                    user.setBloodGroup(radioButton[0].getText().toString());
                    finish();
                    firstZoom=false;
                    checkLastLocationStatus=false;
                    everyOneNotified=false;
                    isDonorFound=false;
                    donorIDS.clear();
                    removeLocationUpdates();
                    WelcomeReceiver.super.onBackPressed();
                    startActivity(new Intent(WelcomeReceiver.this,WelcomeReceiver.class).putExtra("USER",user));
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();

        }else if(id==R.id.pastTransactions){
            startActivity(new Intent(WelcomeReceiver.this,PastTransactions.class));
        }
        else if(id==R.id.logOut){

            firstZoom=false;
            checkLastLocationStatus=false;
            removeLocationUpdates();

            new AlertDialog.Builder(WelcomeReceiver.this).setTitle("SIGN OUT")
                    .setMessage("Do you want to sign out?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(WelcomeReceiver.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        }
        else if (id == R.id.acc_manage) {

            startActivity(new Intent(WelcomeReceiver.this,UserView.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
