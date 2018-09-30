package com.demyati.seddik.followerscounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.facebook.AccessToken;
public class UpdateClass extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent UpdateItem=new Intent("UpdateNumber");
        LocalBroadcastManager.getInstance(context).sendBroadcast(UpdateItem);
    }
}
