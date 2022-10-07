package com.inflexionlabs.ringtoner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.connectivity.ConnectivityReceiver;
import com.inflexionlabs.ringtoner.util.Util;

public class MainActivity extends AppCompatActivity {

    private ConnectivityReceiver connectivityReceiver;

    private TextView netTextView;

    private ProgressBar progressBar;

    private AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        netTextView = findViewById(R.id.simView);
        progressBar = findViewById(R.id.loadProgress);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        //update check
        appUpdateManager = AppUpdateManagerFactory.create(this);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE /*AppUpdateType.FLEXIBLE*/)){

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE /*AppUpdateType.FLEXIBLE*/, MainActivity.this, Util.RC_APP_UPDATE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        connectivityReceiver = new ConnectivityReceiver(isConnected -> {
            if (isConnected){
                netTextView.setText(R.string.back_online);
                netTextView.setBackgroundColor(getColor(R.color.netColor));
                netTextView.postDelayed(()-> netTextView.setVisibility(View.GONE),2000);
            }else{
                netTextView.setVisibility(View.VISIBLE);
                netTextView.setText(R.string.no_connection);
                netTextView.setBackgroundColor(getColor(R.color.primaryColor));
                if (progressBar.getVisibility() == View.VISIBLE){
                    alertStart();
                }
            }
        });

        registerReceiver();

        //bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);

        //Navigation component setup
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentView);

        //navigation controller setup
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView,navController);

        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.RC_APP_UPDATE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(android.R.id.content),"App updated!", Snackbar.LENGTH_SHORT).show();
//                Log.e(TAG, "onActivityResult: app download failed");
            }
        }
    }

    protected void registerReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetReceiver(){
        try {
            unregisterReceiver(connectivityReceiver);
        }catch (Exception e){
            e.printStackTrace();
//            Log.d("GG", "unregisterNetReceiver: "+e);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE /*AppUpdateType.FLEXIBLE*/, MainActivity.this, Util.RC_APP_UPDATE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetReceiver();
    }
}