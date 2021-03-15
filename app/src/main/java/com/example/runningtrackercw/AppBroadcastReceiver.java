package com.example.runningtrackercw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class AppBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the information on the network connection
        intent.getAction();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // If no connection display the warning to the user
        if( !(networkInfo != null && networkInfo.isConnected()) ) {
            Toast.makeText(context, "The application cannot reach the internet at the moment, for accurate location readings please re-connect to the network.",Toast.LENGTH_LONG).show();
        }
    }
}
