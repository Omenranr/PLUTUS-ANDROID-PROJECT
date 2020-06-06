package com.rdmn.plutus.helperapp.Activities;

import android.content.Intent;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.rdmn.plutus.helperapp.Common.Common;
import com.rdmn.plutus.helperapp.Model.RoutesAPI.GoogleMapsAPIRequest;
import com.rdmn.plutus.helperapp.Interfaces.IFCMService;
import com.rdmn.plutus.helperapp.Interfaces.googleAPIInterface;
import com.rdmn.plutus.helperapp.Messages.Message;
import com.rdmn.plutus.helperapp.Messages.Messages;
import com.rdmn.plutus.helperapp.Model.FCMResponse;
import com.rdmn.plutus.helperapp.Model.Notification;
import com.rdmn.plutus.helperapp.Model.Sender;
import com.rdmn.plutus.helperapp.Model.Token;
import com.rdmn.plutus.helperapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {
    TextView tvTime, tvAddress, tvDistance;
    Button btnAccept, btnDecline;
    MediaPlayer mediaPlayer;

    googleAPIInterface mService;
    IFCMService mFCMService;
    String blindID, token;

    double lat, lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);
        mService=Common.getGoogleAPI();
        mFCMService=Common.getFCMService();
        tvTime=findViewById(R.id.tvTime);
        tvDistance=findViewById(R.id.tvDistance);
        tvAddress=findViewById(R.id.tvAddress);
        btnDecline=findViewById(R.id.btnDecline);
        btnAccept=findViewById(R.id.btnAccept);

        mediaPlayer=MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent()!=null){
            lat=getIntent().getDoubleExtra("lat", -1.0);
            lng=getIntent().getDoubleExtra("lng", -1.0);
            blindID=getIntent().getStringExtra("blind");
            token=getIntent().getStringExtra("token");
            getDirection(lat, lng);
        }else finish();
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(blindID)) cancelRequest(blindID);

            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustommerCall.this, HelperTracking.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("blindID", blindID);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cancelRequest(String blindID) {
        Token token=new Token(blindID);

        Notification notification=new Notification("Cancel", "Helper has cancelled your request");
        Sender sender=new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success==1){
                    Message.message(getApplicationContext(), Messages.CANCELLED);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirection(double lat, double lng){

        final String requestApi;

        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?mode=driving&" +
                    "transit_routing_preference=less_driving&origin="+ Common.currentLat+","+Common.currentLng+"&" +
                    "destination="+lat+","+lng+"&key="+getResources().getString(R.string.google_direction_api);
            Log.d("URL_MAPS", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Gson gson = new Gson();
                    GoogleMapsAPIRequest requestObject = gson.fromJson(response.body().toString(), GoogleMapsAPIRequest.class);
                    Log.d("RESPONSE", response.body().toString());

                    tvDistance.setText(requestObject.routes.get(0).legs.get(0).distance.text);
                    tvTime.setText(requestObject.routes.get(0).legs.get(0).duration.text);
                    tvAddress.setText(requestObject.routes.get(0).legs.get(0).end_address);
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mediaPlayer.start();
        super.onResume();
    }
}
