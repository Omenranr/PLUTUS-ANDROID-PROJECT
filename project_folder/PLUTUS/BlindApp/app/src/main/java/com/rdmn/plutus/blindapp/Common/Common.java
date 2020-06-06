package com.rdmn.plutus.blindapp.Common;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.rdmn.plutus.blindapp.Interfaces.IFCMService;
import com.rdmn.plutus.blindapp.Messages.Errors;
import com.rdmn.plutus.blindapp.Messages.Message;
import com.rdmn.plutus.blindapp.Messages.Messages;
import com.rdmn.plutus.blindapp.Model.fcm.FCMResponse;
import com.rdmn.plutus.blindapp.Model.fcm.Notification;
import com.rdmn.plutus.blindapp.Model.firebase.Help;
import com.rdmn.plutus.blindapp.Model.firebase.User;
import com.rdmn.plutus.blindapp.Model.fcm.Sender;
import com.rdmn.plutus.blindapp.Model.firebase.Token;
import com.rdmn.plutus.blindapp.Retrofit.GoogleMapsAPI;
import com.rdmn.plutus.blindapp.Retrofit.IFCMClient;
import com.rdmn.plutus.blindapp.Retrofit.IGoogleAPI;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {
    public static final String helper_tbl="Helpers";
    public static final String user_helper_tbl="HelpersInformation";
    public static final String history_blind = "BlindHistory";
    public static final String user_blind_tbl="BlindsInformation";
    public static final String pickup_request_tbl="PickupRequest";
    public static final String CHANNEL_ID_ARRIVED="ARRIVED";
    public static String token_tbl="Tokens";
    public static String rate_detail_tbl="RateDetails";
    public static final int PICK_IMAGE_REQUEST = 9999;

    public static User currentUser=new User();
    public static String userID;

    public static boolean helperFound=false;
    public static String helperID="";
    public static LatLng currenLocation;

    public static final String fcmURL="https://fcm.googleapis.com/";
    public static final String googleAPIUrl="https://maps.googleapis.com";

    private static double baseFare=2.55;
    private static double timeRate=0.35;
    private static double distanceRate=1.75;

    public static double getPrice(double km, int min){
        return (baseFare+(timeRate*min)+(distanceRate*km));
    }

    public static IFCMService getFCMService(){
        return IFCMClient.getClient(fcmURL).create(IFCMService.class);
    }
    public static IGoogleAPI getGoogleService(){
        return GoogleMapsAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }
    public static void sendRequestToHelper(final String helperID, final IFCMService mService, final Context context, final LatLng lastLocation) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(helperID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Token token=postSnapShot.getValue(Token.class);
                    Help pickup=new Help();
                    pickup.setLastLocation(lastLocation);
                    pickup.setID(userID);
                    pickup.setToken(token);
                    String json_pickup=new Gson().toJson(pickup);

                    String blindToken=FirebaseInstanceId.getInstance().getToken();
                    Notification data=new Notification("Pickup", json_pickup);
                    Sender content=new Sender(token.getToken(), data);

                    mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success==1) Message.message(context, Messages.REQUEST_SUCCESS);
                            else Message.messageError(context, Errors.SENT_FAILED);
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
