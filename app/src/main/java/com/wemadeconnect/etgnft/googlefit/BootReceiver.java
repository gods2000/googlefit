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
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent boot_intent = new Intent(context,MyService.class);
            boot_intent.setAction("startForeground");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(boot_intent);
            } else {
                context.startService(boot_intent);
            }

            mainActivity.rebootSensorManager(context);

        } else {
            Log.i(TAG, "Not Equal");
        }
    }
}
