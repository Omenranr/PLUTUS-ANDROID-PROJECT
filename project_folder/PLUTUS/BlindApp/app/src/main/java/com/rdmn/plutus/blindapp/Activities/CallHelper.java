package com.rdmn.plutus.blindapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rdmn.plutus.blindapp.Common.Common;
import com.rdmn.plutus.blindapp.Interfaces.IFCMService;
import com.rdmn.plutus.blindapp.Model.firebase.User;
import com.rdmn.plutus.blindapp.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallHelper extends AppCompatActivity {
    CircleImageView imgAvatar;
    TextView tvName, tvPhone, tvRate;
    Button btnCallHelper;

    String helperID;
    LatLng lastLocation;

    IFCMService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_helper);
        mService=Common.getFCMService();

        imgAvatar=(CircleImageView)findViewById(R.id.imgAvatar);
        tvName=findViewById(R.id.tvHelperName);
        tvPhone=findViewById(R.id.tvPhone);
        tvRate=findViewById(R.id.tvRate);
        btnCallHelper=findViewById(R.id.btnCallHelper);

        if(getIntent()!=null){
            helperID=getIntent().getStringExtra("helperID");
            double lat=getIntent().getDoubleExtra("lat", 0.0);
            double lng=getIntent().getDoubleExtra("lng", 0.0);
            lastLocation=new LatLng(lat, lng);
            loadHelperInfo(helperID);
        }else finish();
        btnCallHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(helperID!=null && !helperID.isEmpty())
                    Common.sendRequestToHelper(Common.helperID, mService, getApplicationContext(),
                            new LatLng(lastLocation.latitude, lastLocation.longitude));
            }
        });
    }

    private void loadHelperInfo(String helperID) {
        FirebaseDatabase.getInstance().getReference(Common.user_helper_tbl)
                .child(helperID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);

                //if(user.getAvatarUrl()!=null &&
                        //!TextUtils.isEmpty(user.getAvatarUrl()))
                    //Picasso.get().load(user.getAvatarUrl()).into(imgAvatar);
                tvName.setText(user.getName());
                tvPhone.setText(user.getPhone());
                tvRate.setText(user.getRates());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
