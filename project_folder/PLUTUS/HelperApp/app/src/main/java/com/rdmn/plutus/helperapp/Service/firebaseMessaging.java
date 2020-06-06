package com.rdmn.plutus.helperapp.Service;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.rdmn.plutus.helperapp.Activities.CustommerCall;
import com.rdmn.plutus.helperapp.Common.Common;
import com.rdmn.plutus.helperapp.Model.Help;
import com.rdmn.plutus.helperapp.Model.Token;

public class firebaseMessaging extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification().getTitle().equals("Pickup")){
            Help pickup=new Gson().fromJson(remoteMessage.getNotification().getBody(), Help.class);
            Intent intent=new Intent(getBaseContext(), CustommerCall.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("lat", pickup.getLastLocation().latitude);
            intent.putExtra("lng", pickup.getLastLocation().longitude);
            intent.putExtra("blind", pickup.getID());
            intent.putExtra("token", pickup.getToken().getToken());
            startActivity(intent);
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.token_tbl);

        Token token=new Token(s);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)tokens.child(FirebaseAuth.getInstance().getUid())
                .setValue(token);
    }
}
