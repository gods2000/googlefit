package com.wemadeconnect.etgnft.googlefit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    public static final String TAG = "StepCounter";

    public MyService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if("startForeground".equals(intent.getAction())) {
            Log.i(TAG, "startForeground equal");
            startForegroundService();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startForegroundService() {
        Log.i(TAG, "startForegroundService");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("스텝수 측정 중");
        builder.setContentText("백그라운드에서 사용자의 스텝수를 실시간으로 측정 중입니다.");

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        startForeground(1, builder.build());
    }
}