package com.wemadeconnect.etgnft.googlefit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final String TAG = "StepCounter";

    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    TextView tvStepCount;

    private String NOW_STEP_KEY = "now_step";
    private String BACK_STEP_KEY = "back_step";
    private float nowStep = 0;
    private float backStep = 0;  //백그라운드 일때 스텝
    private boolean isBack = false;
    private SharedPreferences sharedPreferences;
    public boolean bReBoot = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        tvStepCount = (TextView) findViewById(R.id.tvStepCount);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCountSensor == null) {
            Toast.makeText(this,"No Step Detect Sensor",Toast.LENGTH_SHORT).show();
        } else {
            onForegroundService();
            Toast.makeText(this,"Start ForeGround Service",Toast.LENGTH_SHORT).show();
        }
    }

    public void onForegroundService() {
        Log.i(TAG, "onForegroundService");
        Intent intent = new Intent(this,MyService.class);
        intent.setAction("startForeground");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        isBack = true;
        saveData(nowStep,NOW_STEP_KEY);
        //sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        if(stepCountSensor != null)
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_NORMAL);

        if(isBack) {
            isBack = false;
            Toast.makeText(this,"BackGround Total Step = " + backStep,Toast.LENGTH_LONG).show();
            //saveData(loadData() + backStep);
            backStep = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            if(isBack) {
                backStep = event.values[0] - nowStep;
                if(backStep % 10 == 0) {
                    saveData(backStep,BACK_STEP_KEY);
                }
            } else {
                nowStep = event.values[0];
            }

            if(tvStepCount != null)
                tvStepCount.setText("Step Count : " + String.valueOf(nowStep));
            Log.i(TAG, "Step Count : " + String.valueOf(nowStep));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void saveData(float stepValue,String keyValue) {
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putFloat(keyValue,stepValue);
        editor.commit();
    }

    private float loadData(String keyValue) {
        return sharedPreferences.getFloat(keyValue,0);
    }

    public void rebootSensorManager(Context context) {

        bReBoot = true;

        sensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCountSensor == null) {
            Log.i(TAG, "stepCountSensor null");
        } else {
            Log.i(TAG, "stepCountSensor register");
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}