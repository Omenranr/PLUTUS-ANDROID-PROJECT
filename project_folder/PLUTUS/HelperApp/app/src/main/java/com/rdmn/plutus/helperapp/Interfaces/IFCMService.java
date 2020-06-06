package com.rdmn.plutus.helperapp.Interfaces;

import com.rdmn.plutus.helperapp.Model.FCMResponse;
import com.rdmn.plutus.helperapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
        "Content-Type:application/json",
        "Authorization:key=AAAAhTuTejE:APA91bHt6ZuVYfw_hG5pRSQNbyz0HTU32RQ3Rjdi21oG3Vs7IL9DLcYTkDzM7fdkYi8SJmeEM_MT_yK6vFOGrtbbZ2ymBY4IA0d4v9b59_IqaVZ6ZlzKty47luzM2iaeVjq1hmeWCt43"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
