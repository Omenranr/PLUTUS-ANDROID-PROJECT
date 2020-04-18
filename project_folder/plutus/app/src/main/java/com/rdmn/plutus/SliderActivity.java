package com.rdmn.plutus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.rdmn.plutus.R;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class SliderActivity extends AppCompatActivity {
    ViewPager viewPager;
    Button btnStart;
    SliderAdapter sliderAdapter;
    WormDotsIndicator wormDotsIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intro manager
        if(restorePrefData()){
            Intent main_activity = new Intent(getApplicationContext(), SplashActivity2.class);
            startActivity(main_activity);
            finish();
        }


        setContentView(R.layout.activity_slider);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.wormDotsIndicator);
        btnStart = (Button) findViewById(R.id.btnStart);
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);
        wormDotsIndicator.setViewPager(viewPager);
        final Animation picFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if(position == 1){
                    btnStart.startAnimation(picFade);
                    btnStart.setVisibility(View.VISIBLE);
                }else{
                    btnStart.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SplashActivity2.class);
                startActivity(intent);
                savePrefsData();
                finish();
            }
        });
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        return pref.getBoolean("isAppOpened",false);
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isAppOpened", true);
        editor.apply();
    }

}
