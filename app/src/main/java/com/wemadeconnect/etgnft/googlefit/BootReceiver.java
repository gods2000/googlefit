package com.wemadeconnect.etgnft.googlefit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "StepCounter";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BootReceiver onReceive");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent boot_intent = new Intent(context,MyService.class);
            intent.setAction("startForeground");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(boot_intent);
            } else {
                context.startService(boot_intent);
            }
        }
    }
}
