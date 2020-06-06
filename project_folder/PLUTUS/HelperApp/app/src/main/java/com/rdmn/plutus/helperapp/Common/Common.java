package com.rdmn.plutus.helperapp.Common;

import com.rdmn.plutus.helperapp.Interfaces.IFCMService;
import com.rdmn.plutus.helperapp.Interfaces.googleAPIInterface;
import com.rdmn.plutus.helperapp.Model.User;
import com.rdmn.plutus.helperapp.Retrofit.FCMClient;
import com.rdmn.plutus.helperapp.Retrofit.RetrofitClient;

public class Common {
    public static final String helper_tbl="Helpers";
    public static final String user_helper_tbl="HelpersInformation";
    public static final String history_helper = "HelperHistory";
    public static final String history_blind = "BlindHistory";
    public static final String user_blind_tbl="BlindsInformation";
    public static final String pickup_request_tbl="PickupRequest";
    public static final String token_tbl="Tokens";
    public static User currentUser;
    public static String userID;
    public static final int PICK_IMAGE_REQUEST = 9999;

    public static Double currentLat;
    public static Double currentLng;

    public static final String baseURL="https://maps.googleapis.com";
    public static final String fcmURL="https://fcm.googleapis.com/";

    public static double baseFare=2.55;
    private static double timeRate=0.35;
    private static double distanceRate=1.75;

    public static double formulaPrice(double km, double min){
        return baseFare+(distanceRate*km)+(timeRate*min);
    }
    public static googleAPIInterface getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(googleAPIInterface.class);
    }
    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
