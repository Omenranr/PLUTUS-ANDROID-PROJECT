package com.rdmn.plutus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rdmn.plutus.R;
import com.thefuntasty.hauler.DragDirection;
import com.thefuntasty.hauler.HaulerView;
import com.thefuntasty.hauler.OnDragDismissedListener;

import org.jetbrains.annotations.NotNull;

public class SplashActivity3 extends AppCompatActivity {

    private HaulerView haulerView;
    Button btn3 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        //getWindow().setStatusBarColor(this.getColor(R.color.bg));
        //getWindow().setNavigationBarColor(this.getColor(R.color.bg));
        setContentView(R.layout.activity_splash3);
        /*haulerView = (HaulerView) findViewById(R.id.haulerView);

        haulerView.setOnDragDismissedListener(new OnDragDismissedListener() {
            @Override
            public void onDismissed(@NotNull DragDirection dragDirection) {
                finish();
            }
        });*/


        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


}
