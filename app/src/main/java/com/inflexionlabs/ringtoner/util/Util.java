package com.inflexionlabs.ringtoner.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.inflexionlabs.ringtoner.R;

import java.util.ArrayList;

public class Util {
    public static final String CATEGORY_NAME_TEXT = "text";
    public static final String CATEGORY_URL = "url";
    public static final String POSITION = "position";

    public static final String ADDED_TO_FAVOURITES = "Added to favourites";
    public static final String REMOVE_FROM_FAVOURITES = "Removed from favourites";

    public static final String BOOLEAN = "boolean";

    public static final int WRITE_SETTINGS_CODE = 1;
    public static final int WRITE_CONTACTS_CODE = 2;
    public static final int RC_APP_UPDATE = 11;

    public static ArrayList<String> FAVOURITE_STRING_ARRAYLIST = new ArrayList<>();

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void alert(Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.no_connection)
                .setMessage("Check your internet connection and retry again")
                .setPositiveButton("RETRY", (dialogInterface, i) -> {
                    if (Util.isConnected(activity.getApplicationContext())) {
                        dialogInterface.dismiss();
                    }else {
                        alert(activity);
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }


    public static void showToast(Context context, String toast){
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

}
