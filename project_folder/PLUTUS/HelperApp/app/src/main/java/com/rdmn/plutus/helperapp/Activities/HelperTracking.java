package com.rdmn.plutus.helperapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rdmn.plutus.helperapp.Common.Common;
import com.rdmn.plutus.helperapp.Helpers.DirectionJSONParser;
import com.rdmn.plutus.helperapp.Interfaces.IFCMService;
import com.rdmn.plutus.helperapp.Interfaces.googleAPIInterface;
import com.rdmn.plutus.helperapp.Interfaces.locationListener;
import com.rdmn.plutus.helperapp.Model.FCMResponse;
import com.rdmn.plutus.helperapp.Model.History;
import com.rdmn.plutus.helperapp.Model.Notification;
import com.rdmn.plutus.helperapp.Model.Sender;
import com.rdmn.plutus.helperapp.Model.Token;
import com.rdmn.plutus.helperapp.Model.User;
import com.rdmn.plutus.helperapp.R;
import com.rdmn.plutus.helperapp.Util.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelperTracking extends AppCompatActivity implements OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    Location location=null;

    private GoogleApiClient mGoogleApiClient;
    double blindLat, blindLng;
    private Circle blindMarker;
    private Marker helperMarker;

    GoogleSignInAccount account;

    private Polyline direction;
    private googleAPIInterface mService;
    IFCMService mFCMService;

    GeoFire geoFire;
    String blindID, blindToken;

    Button btnStartTrip;

    LatLng pickupLocation;

    DatabaseReference historyHelper, historyBlind, blindInformation, helpers, tokens;
    FirebaseDatabase database;

    User blindData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(getIntent()!=null){
            blindLat=getIntent().getDoubleExtra("lat",-1.0);
            blindLng=getIntent().getDoubleExtra("lng",-1.0);
            blindID = getIntent().getStringExtra("blindID");
            blindToken=getIntent().getStringExtra("token");
        }
        database = FirebaseDatabase.getInstance();
        historyHelper = database.getReference(Common.history_helper).child(Common.userID);
        historyBlind = database.getReference(Common.history_blind).child(blindID);
        blindInformation=database.getReference(Common.user_blind_tbl);
        tokens=database.getReference(Common.token_tbl);
        helpers= FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child(Common.currentUser.getHelpType());
        geoFire=new GeoFire(helpers);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        verifyGoogleAccount();


        mService = Common.getGoogleAPI();
        mFCMService=Common.getFCMService();
        location=new Location(this, new locationListener() {
            @Override
            public void locationResponse(LocationResult response) {
                // refresh current location
                Common.currentLat=response.getLastLocation().getLatitude();
                Common.currentLng=response.getLastLocation().getLongitude();
                displayLocation();

            }
        });
        btnStartTrip=(Button)findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStartTrip.getText().equals("START TRIP")){
                    pickupLocation=new LatLng(Common.currentLat, Common.currentLng);
                    btnStartTrip.setText("DROP OFF HERE");
                }else if(btnStartTrip.getText().equals("DROP OFF HERE")){
                    calculateCashFree(pickupLocation, new LatLng(Common.currentLat, Common.currentLng));
                }
            }
        });
        getBlindData();
    }

    private void getBlindData() {
        blindInformation.child(blindID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blindData=dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void calculateCashFree(final LatLng pickupLocation, LatLng latLng) {
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + pickupLocation.latitude + "," + pickupLocation.longitude + "&" +
                    "destination=" + latLng.latitude + "," + latLng.longitude + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response.body().toString());
                                JSONArray routes=jsonObject.getJSONArray("routes");

                                JSONObject object=routes.getJSONObject(0);
                                JSONArray legs=object.getJSONArray("legs");

                                JSONObject legsObject=legs.getJSONObject(0);

                                JSONObject distance=legsObject.getJSONObject("distance");
                                String distanceText=distance.getString("text");

                                Double distanceValue=Double.parseDouble(distanceText.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject timeObject=legsObject.getJSONObject("duration");
                                String timeText=timeObject.getString("text");

                                Double timeValue=Double.parseDouble(timeText.replaceAll("[^0-9\\\\.]+", ""));

                                sendDropOffNotification(blindToken);
                                Calendar calendar = Calendar.getInstance();
                                String date = String.format("%s, %d/%d", convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH));

                                History helperHistory = new History();
                                helperHistory.setName(blindData.getName());
                                helperHistory.setStartAddress(legsObject.getString("start_address"));
                                helperHistory.setEndAddress(legsObject.getString("end_address"));
                                helperHistory.setTime(String.valueOf(timeValue));
                                helperHistory.setDistance(String.valueOf(distanceValue));
                                helperHistory.setTotal(Common.formulaPrice(distanceValue, timeValue));
                                helperHistory.setLocationStart(String.format("%f,%f", pickupLocation.latitude, pickupLocation.longitude));
                                helperHistory.setLocationEnd(String.format("%f,%f", Common.currentLat, Common.currentLng));
                                helperHistory.setTripDate(date);
                                historyHelper.push().setValue(helperHistory);

                                History blindHistory = new History();
                                blindHistory.setName(Common.currentUser.getName());
                                blindHistory.setStartAddress(legsObject.getString("start_address"));
                                blindHistory.setEndAddress(legsObject.getString("end_address"));
                                blindHistory.setTime(String.valueOf(timeValue));
                                blindHistory.setDistance(String.valueOf(distanceValue));
                                blindHistory.setTotal(Common.formulaPrice(distanceValue, timeValue));
                                blindHistory.setLocationStart(String.format("%f,%f", pickupLocation.latitude, pickupLocation.longitude));
                                blindHistory.setLocationEnd(String.format("%f,%f", Common.currentLat, Common.currentLng));
                                blindHistory.setTripDate(date);
                                historyBlind.push().setValue(blindHistory);

                                Intent intent = new Intent(HelperTracking.this, HelpDetail.class);
                                intent.putExtra("start_address", legsObject.getString("start_address"));
                                intent.putExtra("end_address", legsObject.getString("end_address"));
                                intent.putExtra("time", String.valueOf(timeValue));
                                intent.putExtra("distance", String.valueOf(distanceValue));
                                intent.putExtra("total", Common.formulaPrice(distanceValue, timeValue));
                                intent.putExtra("location_start", String.format("%f,%f", pickupLocation.latitude, pickupLocation.longitude));
                                intent.putExtra("location_end", String.format("%f,%f", Common.currentLat, Common.currentLng));

                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(HelperTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //add helper location
        blindMarker=mMap.addCircle(new CircleOptions()
                .center(new LatLng(blindLat, blindLng))
                .radius(50)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5f));

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.plutus_style_map));
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child(Common.currentUser.getHelpType()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(blindLat, blindLng), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(blindToken);
                btnStartTrip.setEnabled(true);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification( "Arrived",String.format("The helper %s has arrived at your location", Common.currentUser.getName()));
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(HelperTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }

    private void sendDropOffNotification(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification( "DropOff", customerId);
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(HelperTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }
    private String convertToDayOfWeek(int day) {
        switch(day){
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
            default:
                return "UNK";
        }
    }
    private void displayLocation(){
        //add helper location
        if(helperMarker!=null)helperMarker.remove();
        helperMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(Common.currentLat, Common.currentLng))
        .title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.currentLat, Common.currentLng), 14f));
        geoFire.setLocation(Common.userID,
                new GeoLocation(Common.currentLat, Common.currentLng),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
        //remove route
        if(direction!=null)direction.remove();
          getDirection();

    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.currentLat, Common.currentLng);

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + blindLat + "," + blindLng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("ISAKAY", requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                new ParserTask().execute(response.body().toString());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(HelperTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void verifyGoogleAccount() {
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        OptionalPendingResult<GoogleSignInResult> opr=Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()){
            GoogleSignInResult result= opr.get();
            handleSignInResult(result);
        }else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            account = result.getSignInAccount();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        location.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        location.inicializeLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        location.stopUpdateLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog mDialog = new ProgressDialog(HelperTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}