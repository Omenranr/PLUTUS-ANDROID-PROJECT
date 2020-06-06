package com.rdmn.plutus.blindapp.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rdmn.plutus.blindapp.Common.Common;
import com.rdmn.plutus.blindapp.Fragments.BottomSheetBlindFragment;
import com.rdmn.plutus.blindapp.Helper.CustomInfoWindow;
import com.rdmn.plutus.blindapp.Interfaces.HttpResponse;
import com.rdmn.plutus.blindapp.Interfaces.IFCMService;
import com.rdmn.plutus.blindapp.Interfaces.locationListener;
import com.rdmn.plutus.blindapp.Messages.Errors;
import com.rdmn.plutus.blindapp.Messages.Message;
import com.rdmn.plutus.blindapp.Model.firebase.User;
import com.rdmn.plutus.blindapp.Model.firebase.Token;
import com.rdmn.plutus.blindapp.Model.placesapi.PlacesResponse;
import com.rdmn.plutus.blindapp.Model.placesapi.Results;
import com.rdmn.plutus.blindapp.R;
import com.rdmn.plutus.blindapp.Util.Location;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rdmn.plutus.blindapp.Util.NetworkUtil;
import com.rdmn.plutus.blindapp.adapter.RecyclerViewPlaces.ClickListener;
import com.rdmn.plutus.blindapp.adapter.RecyclerViewPlaces.PlacesAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener {
    private ImageView help1, help2;
    private Button btnRequestHelp;
    private Toolbar toolbar;
    private GoogleMap mMap;
    private LinearLayout llPickupInput, llDestinationInput, llPickupPlace, llDestinationPlace;
    private EditText etFinalPickup, etFinalDestination, etPickup, etDestination;
    private RecyclerView rvPickupPlaces, rvDestinationPlaces;
    private GoogleSignInAccount account;
    private SupportMapFragment mapFragment;

    private Marker blindMarket, destinationMarker;
    private ArrayList<Marker> helperMarkers=new ArrayList<>();

    //Gooogle
    private GoogleApiClient mGoogleApiClient;
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    boolean isLoggedInFacebook = accessToken != null && !accessToken.isExpired();

    private DatabaseReference helpersAvailable;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private IFCMService ifcmService;

    private Location location;
    private NetworkUtil networkUtil;

    private String mPlaceLocation, mPlaceDestination;
    private Double currentLat, currentLng;
    private boolean isHelp1=true, pickupPlacesSelected=false;
    private int radius=1, distance=1; // km
    private static final int LIMIT=3;
    private String URL_BASE_API_PLACES="https://maps.googleapis.com/maps/api/place/textsearch/json?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        verifyGoogleAccount();
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        ifcmService=Common.getFCMService();
        networkUtil=new NetworkUtil(this);
        location=new Location(this, new locationListener() {
            @Override
            public void locationResponse(LocationResult response) {
                // Add a icon_marker in Sydney and move the camera
                currentLat=response.getLastLocation().getLatitude();
                currentLng=response.getLastLocation().getLongitude();
                Common.currenLocation=new LatLng(response.getLastLocation().getLatitude(), response.getLastLocation().getLongitude());
                displayLocation();
                if(mPlaceLocation==null) {
                    helpersAvailable = FirebaseDatabase.getInstance().getReference(Common.helper_tbl);
                    helpersAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            loadAllAvailableHelper(new LatLng(currentLat, currentLng));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        help1=findViewById(R.id.selectedHelp1);
        help2=findViewById(R.id.selectedHelp2);

        help1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isToggle= isHelp1;
                isHelp1=true;
                if(isToggle) {
                    help1.setImageResource(R.drawable.help_cui);
                    help2.setImageResource(R.drawable.help_cui);
                }
                loadAllAvailableHelper(new LatLng(currentLat, currentLng));
            }
        });

        help2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isToggle= isHelp1;
                isHelp1=true;
                if(isToggle) {
                    help1.setImageResource(R.drawable.help_cui);
                    help2.setImageResource(R.drawable.help_cui);
                }
                loadAllAvailableHelper(new LatLng(currentLat, currentLng));
            }
        });

        btnRequestHelp=findViewById(R.id.btnPickupRequest);
        btnRequestHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLat!=null && currentLng!=null) {
                    if (!Common.helperFound)
                        RequestHelp(Common.userID);
                    else
                        Common.sendRequestToHelper(Common.helperID, ifcmService, getApplicationContext(), Common.currenLocation);
                }
            }
        });
        etFinalPickup.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    llPickupInput.setVisibility(View.VISIBLE);
                    llPickupPlace.setVisibility(View.GONE);
                    llDestinationInput.setVisibility(View.GONE);
                    llDestinationPlace.setVisibility(View.GONE);
                    etPickup.requestFocus();
                }
            }
        });
        etFinalDestination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    llPickupInput.setVisibility(View.GONE);
                    llPickupPlace.setVisibility(View.GONE);
                    llDestinationInput.setVisibility(View.VISIBLE);
                    llDestinationPlace.setVisibility(View.GONE);
                    etDestination.requestFocus();
                }
            }
        });
        etPickup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getPlacesByString(charSequence.toString(), true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getPlacesByString(charSequence.toString(), false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        updateFirebaseToken();
    }

    private void initViews() {
        llPickupInput = findViewById(R.id.ll_pickup_input);
        llPickupPlace = findViewById(R.id.ll_pickup_place);
        llDestinationInput = findViewById(R.id.ll_destination_input);
        llDestinationPlace = findViewById(R.id.ll_destination_place);
        etFinalPickup = findViewById(R.id.et_final_pickup_location);
        etFinalDestination = findViewById(R.id.et_final_destination);
        etDestination = findViewById(R.id.et_destination);
        etPickup = findViewById(R.id.et_pickup);
        rvPickupPlaces = findViewById(R.id.rv_pickup_places);
        rvPickupPlaces.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvDestinationPlaces = findViewById(R.id.rv_destination_places);
        rvDestinationPlaces.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void initDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView=navigationView.getHeaderView(0);
        TextView tvName=(TextView)navigationHeaderView.findViewById(R.id.tvBlindName);
        TextView tvStars=(TextView)findViewById(R.id.tvStars);
        CircleImageView imageAvatar=(CircleImageView) navigationHeaderView.findViewById(R.id.imgAvatar);

        tvName.setText(Common.currentUser.getName());
        if(Common.currentUser.getRates()!=null &&
                !TextUtils.isEmpty(Common.currentUser.getRates()))
            tvStars.setText(Common.currentUser.getRates());

        if(isLoggedInFacebook)
            Picasso.get().load("https://graph.facebook.com/" + Common.userID + "/picture?width=500&height=500").into(imageAvatar);
        else if(account!=null)
            Picasso.get().load(account.getPhotoUrl()).into(imageAvatar);
        if(Common.currentUser.getAvatarUrl()!=null &&
                !TextUtils.isEmpty(Common.currentUser.getAvatarUrl()))
            Picasso.get().load(Common.currentUser.getAvatarUrl()).into(imageAvatar);
    }

    private void loadUser(){
        FirebaseDatabase.getInstance().getReference(Common.user_blind_tbl)
                .child(Common.userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Common.currentUser=dataSnapshot.getValue(User.class);
                        initDrawer();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

    private void updateFirebaseToken() {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        final DatabaseReference tokens=db.getReference(Common.token_tbl);

        final Token token=new Token(FirebaseInstanceId.getInstance().getToken());
        if(FirebaseAuth.getInstance().getUid()!=null) tokens.child(FirebaseAuth.getInstance().getUid()).setValue(token);
        else if(account!=null) tokens.child(account.getId()).setValue(token);
        else{
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            String id = object.optString("id");
                            tokens.child(id).setValue(token);
                        }
                    });
            request.executeAsync();
        }
    }

    private void RequestHelp(String uid) {
        DatabaseReference dbRequest=FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeofire=new GeoFire(dbRequest);
        mGeofire.setLocation(uid, new GeoLocation(Common.currenLocation.latitude, Common.currenLocation.longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
        if (blindMarket.isVisible())blindMarket.remove();
        blindMarket=mMap.addMarker(new MarkerOptions().title(getResources().getString(R.string.help_here)).snippet("").position(new LatLng(Common.currenLocation.latitude, Common.currenLocation.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        blindMarket.showInfoWindow();
        btnRequestHelp.setText(getResources().getString(R.string.getting_helper));
        findHelper();
    }

    private void findHelper() {
        DatabaseReference helperLocation;
        if(isHelp1)
            helperLocation=FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child("Help1");
        else
            helperLocation=FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child("Help2");
        GeoFire geoFire=new GeoFire(helperLocation);
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(Common.currenLocation.latitude, Common.currenLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!Common.helperFound){
                    Common.helperFound=true;
                    Common.helperID=key;
                    btnRequestHelp.setText(getApplicationContext().getResources().getString(R.string.call_helper));
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
                if (!Common.helperFound && radius<LIMIT){
                    radius++;
                    findHelper();
                }else{
                    if(!Common.helperFound) {
                        Toast.makeText(Home.this, "No available any helper near you", Toast.LENGTH_SHORT).show();
                        btnRequestHelp.setText("REQUEST HELP");
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_trip_history:
                showHelpHistory();
                break;
            case R.id.nav_usage:
                showUsage();
                break;
            case R.id.nav_updateInformation:
                showDialogUpdateInfo();
                break;
            case R.id.nav_signOut:
                signOut();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUsage() {
        Intent intent=new Intent(Home.this, AppInfos.class);
        startActivity(intent);
    }

    private void showHelpHistory() {
        Intent intent=new Intent(Home.this, HelpHistory.class);
        startActivity(intent);
    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("UPDATE INFORMATION");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_information, null);
        final MaterialEditText etName = (MaterialEditText) layout_pwd.findViewById(R.id.etName);
        final MaterialEditText etPhone = (MaterialEditText) layout_pwd.findViewById(R.id.etPhone);
        final ImageView image_upload = (ImageView) layout_pwd.findViewById(R.id.imageUpload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        alertDialog.setView(layout_pwd);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).build();
                waitingDialog.show();
                String name = etName.getText().toString();
                String phone = etPhone.getText().toString();

                Map<String, Object> updateInfo = new HashMap<>();
                if(!TextUtils.isEmpty(name))
                    updateInfo.put("name", name);
                if(!TextUtils.isEmpty(phone))
                    updateInfo.put("phone",phone);
                DatabaseReference helperInformation = FirebaseDatabase.getInstance().getReference(Common.user_blind_tbl);
                helperInformation.child(Common.userID)
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if(task.isSuccessful())
                                    Toast.makeText(Home.this,"Information Updated!",Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Home.this,"Information Update Failed!",Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void chooseImage() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){
                            Intent intent=new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, Common.PICK_IMAGE_REQUEST);
                        }else{
                            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Common.PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri saveUri=data.getData();
            if(saveUri!=null){
                final ProgressDialog progressDialog=new ProgressDialog(this);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                String imageName=UUID.randomUUID().toString();
                final StorageReference imageFolder=storageReference.child("images/"+imageName);

                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(Home.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Map<String, Object> avatarUpdate=new HashMap<>();
                                        avatarUpdate.put("avatarUrl", uri.toString());


                                        DatabaseReference helperInformations=FirebaseDatabase.getInstance().getReference(Common.user_blind_tbl);
                                        helperInformations.child(Common.userID).updateChildren(avatarUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                            Toast.makeText(Home.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(Home.this, "Uploaded error!", Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded "+progress+"%");
                    }
                });
            }
        }
    }

    private void signOut() {
        if(account!=null) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Intent intent = new Intent(Home.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Home.this, "Could not log out", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(isLoggedInFacebook){
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            finish();
        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(Home.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            account = result.getSignInAccount();
            Common.userID=account.getId();
            loadUser();
        }else if(isLoggedInFacebook){
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            String id=object.optString("id");
                            Common.userID=id;
                            loadUser();
                        }
                    });
            request.executeAsync();
        }else{
            Common.userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadUser();
        }
    }

    private void displayLocation(){
        if (currentLat!=null && currentLng!=null){
            //presence system
            helpersAvailable = FirebaseDatabase.getInstance().getReference(Common.helper_tbl);
            helpersAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //if have change from helpers table, we will reload all helpers available
                    loadAllAvailableHelper(new LatLng(currentLat, currentLng));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            loadAllAvailableHelper(new LatLng(currentLat, currentLng));

        }else{
            Message.messageError(this, Errors.WITHOUT_LOCATION);
        }

    }

    private void loadAllAvailableHelper(final LatLng location) {
        for (Marker helperMarker:helperMarkers) {
            helperMarker.remove();
        }
        helperMarkers.clear();
        if(!pickupPlacesSelected) {
            if (blindMarket != null)
                blindMarket.remove();

            blindMarket = mMap.addMarker(new MarkerOptions().position(location)
                    .title(getResources().getString(R.string.you))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
        }


        DatabaseReference helperLocation;
        if(isHelp1)
            helperLocation=FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child("Help1");
        else
            helperLocation=FirebaseDatabase.getInstance().getReference(Common.helper_tbl).child("Help2");
        GeoFire geoFire=new GeoFire(helperLocation);

        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.user_helper_tbl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User helper=dataSnapshot.getValue(User.class);
                        String name;
                        String phone;

                        if(helper.getName()!=null) name=helper.getName();
                        else name="not available";

                        if (helper.getPhone()!=null)phone="Phone: "+helper.getPhone();
                        else phone="Phone: none";


                        helperMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).flat(true)
                                .title(name).snippet("Helper ID: "+dataSnapshot.getKey()).icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance<=LIMIT){
                    distance++;
                    loadAllAvailableHelper(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.plutus_style_map));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(destinationMarker!=null)
                    destinationMarker.remove();
                destinationMarker=mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                BottomSheetBlindFragment mBottomSheet= BottomSheetBlindFragment.newInstance(String.format("%f,%f", currentLat, currentLng),
                        String.format("%f,%f",latLng.latitude, latLng.longitude), true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        location.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();
        location.stopUpdateLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayLocation();
        location.inicializeLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(!marker.getTitle().equals("You")){
            Intent intent=new Intent(Home.this, CallHelper.class);
            String ID= marker.getSnippet().replace("Helper ID: ", "");
            intent.putExtra("helperID", ID);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        }
    }

    private void getPlacesByString(String s, final boolean isPickup){
        String queryEncode= s.toString();
        try {
            queryEncode = URLEncoder.encode(s.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String query="&query="+queryEncode;
        String location="&location="+ Common.currenLocation.latitude +","+Common.currenLocation.longitude;
        String radius="radius=1500";
        String key="&key="+ getResources().getString(R.string.google_direction_api);
        String url=(URL_BASE_API_PLACES+radius+location+query+key).replaceAll(" ", "%20");

        Log.d("URL_PLACES", url);
        networkUtil.httpRequest(url, new HttpResponse() {
            @Override
            public void httpResponseSuccess(String response) {
                pickupPlacesSelected=true;
                Gson gson=new Gson();
                PlacesResponse placesResponse=gson.fromJson(response, PlacesResponse.class);
                for(Results result: placesResponse.results){
                    if(result.geometry.location==null){
                        placesResponse.results.remove(result);
                    }else if(result.geometry.location.lat==null || result.geometry.location.lat.equals("") || result.geometry.location.lat.equals("0.0")){
                        placesResponse.results.remove(result);
                    }else if(result.geometry.location.lng==null || result.geometry.location.lng.equals("") || result.geometry.location.lng.equals("0.0")){
                        placesResponse.results.remove(result);
                    }
                }
                if(isPickup)
                    implementPickupRecyclerView(placesResponse.results);
                else
                    implementDestinationRecyclerView(placesResponse.results);
                
            }
        });
    }

    private void implementPickupRecyclerView(final ArrayList<Results> results) {
        PlacesAdapter placesAdapter=new PlacesAdapter(this, results, new ClickListener() {
            @Override
            public void onClick(View view, int index) {
                mPlaceLocation=results.get(index).formatted_address;
                etFinalPickup.setText(mPlaceLocation);

                llPickupInput.setVisibility(View.GONE);
                llPickupPlace.setVisibility(View.VISIBLE);
                llDestinationInput.setVisibility(View.GONE);
                llDestinationPlace.setVisibility(View.VISIBLE);

                Double lat=Double.valueOf(results.get(index).geometry.location.lat);
                Double lng=Double.valueOf(results.get(index).geometry.location.lng);
                LatLng latLng=new LatLng(lat, lng);
                if(blindMarket!=null)
                    blindMarket.remove();
                blindMarket=mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
                        .title("Pickup Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            }
        });
        rvPickupPlaces.setAdapter(placesAdapter);
    }

    private void implementDestinationRecyclerView(final ArrayList<Results> results) {
        PlacesAdapter placesAdapter=new PlacesAdapter(this, results, new ClickListener() {
            @Override
            public void onClick(View view, int index) {
                mPlaceDestination=results.get(index).formatted_address;
                etFinalDestination.setText(mPlaceDestination);

                llPickupInput.setVisibility(View.GONE);
                llPickupPlace.setVisibility(View.VISIBLE);
                llDestinationInput.setVisibility(View.GONE);
                llDestinationPlace.setVisibility(View.VISIBLE);

                Double lat=Double.valueOf(results.get(index).geometry.location.lat);
                Double lng=Double.valueOf(results.get(index).geometry.location.lng);
                LatLng latLng=new LatLng(lat, lng);
                if(destinationMarker!=null)
                    destinationMarker.remove();
                destinationMarker=mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_destination_marker))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                BottomSheetBlindFragment mBottomSheet= BottomSheetBlindFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });
        rvDestinationPlaces.setAdapter(placesAdapter);
    }

}