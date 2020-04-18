package com.rdmn.plutus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.rdmn.plutus.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SplashActivity2 extends AppCompatActivity {


    private TextInputLayout name, email, pass;
    private Button btn,btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setStatusBarColor(this.getColor(R.color.bg));
        //getWindow().setNavigationBarColor(this.getColor(R.color.bg));
        //Soft activity
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_splash2);
        //Slidr.attach(this);
        name =  (TextInputLayout)findViewById(R.id.name);
        email = (TextInputLayout) findViewById(R.id.email);
        pass = (TextInputLayout) findViewById(R.id.pass);
        btn = (Button) findViewById(R.id.btn);
        btn2 =(Button) findViewById(R.id.btn2);

        //Biom auth
        final Executor executor = Executors.newSingleThreadExecutor();
        final BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Fingerprint Authentification")
                .setSubtitle("Subtitle")
                .setDescription("Decription")
                .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).build();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmInput(v)){
                    Intent intent = new Intent(SplashActivity2.this, SplashActivity3.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_act_up,R.anim.slide_act_down);
                }
            }
        });

        final SplashActivity2 activity = this;
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SplashActivity2.this, "Authentificated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                //Intent intent = new Intent(SplashActivity2.this, SplashActivity3.class);
                //startActivity(intent);
                //overridePendingTransition(R.anim.slide_act_up,R.anim.no_act_change);
            }
        });

    }

    private boolean validateEmail(){
        String emailInput = email.getEditText().getText().toString().trim();
        if(emailInput.isEmpty()){
            email.setError("Field can't be empty");
            return false;
        }else{
            email.setError(null);
            //email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateName(){
        String nameInput = name.getEditText().getText().toString().trim();
        if(nameInput.isEmpty()){
            name.setError("Field can't be empty");
            return false;
        }else if(nameInput.length() > 15){
            name.setError("Name too long");
            return false;
        }else{
            name.setError(null);
            //email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePass(){
        String passInput = pass.getEditText().getText().toString().trim();
        if(passInput.isEmpty()){
            pass.setError("Field can't be empty");
            return false;
        }else{
            pass.setError(null);
            //email.setErrorEnabled(false);
            return true;
        }
    }

    public boolean confirmInput(View view){
        if( !validateEmail() | !validateName() | !validatePass()){
            return false;
        }
        String input = "Email: "+email.getEditText().getText().toString();
        input+="\n";
        input+="User: "+name.getEditText().getText().toString();
        input+="\n";
        input+="Pass: "+pass.getEditText().getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return true;
    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}
