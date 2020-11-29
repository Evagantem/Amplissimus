package de.amplus.amplissimus.data;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.material.snackbar.Snackbar;

public class Functions {
    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isOnline(Service service) {
        ConnectivityManager cm =
                (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void makeSnackbar(Activity activity, String message) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        ).setAction("Action", null).show();
    }
}
