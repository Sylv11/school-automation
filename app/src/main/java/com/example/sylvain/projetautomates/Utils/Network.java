package com.example.sylvain.projetautomates.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/* This class lets you know if the device is connected to a network. Permissions are present in the Manifest */

public class Network {

    private Context context;
    private NetworkInfo network;
    private ConnectivityManager connexStatus;

    public Network(Context context) {
        this.context = context;
    }

    public boolean checkNetwork() {
        this.connexStatus = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            assert this.connexStatus != null;
            this.network = this.connexStatus.getActiveNetworkInfo();
            // return true if there is a network connectivity
            return this.network != null && this.network.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
