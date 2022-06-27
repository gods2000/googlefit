package com.wemadeconnect.etgnft.googlefit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "StepCounter";
    MainActivity mainActivity = new MainActivity();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BootReceiver onReceive");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            mainActivity.startForegroundService();
        }
    }
}
