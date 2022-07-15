package com.inflexionlabs.ringtoner.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.util.Util;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.isConnected(this)){
            handler.postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            },2000);
        }else{
            alertStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    public void alertStart(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_connection)
                .setMessage("Check your internet connection and restart the app")
                .setPositiveButton("RESTART", (dialogInterface, i) -> {
                    if (Util.isConnected(this)) {
                        dialogInterface.dismiss();
                        this.finish();
                        this.startActivity(this.getIntent());
                    }else {
                        alertStart();
                    }
                }).setCancelable(false)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> this.finish()).show();
    }

}