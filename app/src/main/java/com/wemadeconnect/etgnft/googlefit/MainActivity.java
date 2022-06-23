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
//    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
//    private FitnessOptions fitnessOptions;

    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    TextView tvStepCount;

    private float nowStep = 0;
    private float backStep = 0;  //백그라운드 일때 스텝
    private boolean isBack = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GoogleFitnessInit();
        tvStepCount = (TextView) findViewById(R.id.tvStepCount);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCountSensor == null) {
            Toast.makeText(this,"No Step Detect Sensor",Toast.LENGTH_SHORT).show();
        }
    }

    public void onStartForegroundService(View view) {
        Intent intent = new Intent(this,MyService.class);
        intent.setAction("startForeground");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
//
//    private void GoogleFitnessInit() {
//        fitnessOptions =
//                FitnessOptions.builder()
//                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                        .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
//                        .build();
//    }
//
//    private void GoogleFitPermissionCheck() {
//        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
//            GoogleSignIn.requestPermissions(
//                    this,
//                    REQUEST_OAUTH_REQUEST_CODE,
//                    GoogleSignIn.getLastSignedInAccount(this),
//                    fitnessOptions);
//        } else {
//            subscribe();
//        }
//    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void subscribe() {
//// Read the data that's been collected throughout the past week.
//        List<String> theDates = new ArrayList<String>();
//        List<Integer> totalAvgSteps = new ArrayList<Integer>();
//
//        long startLongTime = loadTime();
//        //if(startLongTime == 0) {
//            startLongTime = System.currentTimeMillis() - 1000000000;
//            saveNowTime(true);
//        //}
//        long endLongTime = System.currentTimeMillis();
//        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        String strStart = timeFormat.format(new Date(startLongTime));
//        String endStart = timeFormat.format(new Date(endLongTime));
//        Log.i(TAG, "Str Start: " + strStart);
//        Log.i(TAG, "Str End: " + endStart);
//        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
//                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
//                .setType(DataSource.TYPE_DERIVED)
//                .setStreamName("estimated_steps")
//                .setAppPackageName(this.getPackageName())
//                .build();
//
//        DataReadRequest readRequest = new DataReadRequest.Builder()
//                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .bucketByTime(1, TimeUnit.DAYS)
//                .setTimeRange(startLongTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .enableServerQueries()
//                .build();
//
//
//        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
//                .readData(readRequest)
//                .addOnSuccessListener(response -> {
//
//
//                    // The aggregate query puts datasets into buckets, so convert to a
//                    // single list of datasets
//                    for (Bucket bucket : response.getBuckets()) {
//
//                        //convert days in bucket to milliseconds
//                        long days = bucket.getStartTime(TimeUnit.MILLISECONDS);
//                        //convert milliseconds to date
//                        Date stepsDate = new Date(days);
//                        //convert date to day of the week eg: monday, tuesday etc
//                        @SuppressLint("SimpleDateFormat")
//                        SimpleDateFormat df = new SimpleDateFormat("EEE");
//                        String weekday = df.format(stepsDate);
//
//                        Log.i(TAG,"StepsDate = " +  stepsDate.toString());
//                        theDates.add(weekday);
//
//                        for (DataSet dataSet : bucket.getDataSets()) {
//                            totalAvgSteps.add(dumpDataSet(dataSet));
//                        }
//                    }
//                    Log.i(TAG, theDates.toString());
//                    Log.i(TAG, totalAvgSteps.toString());
//                })
//                .addOnFailureListener(e ->
//                        Log.w(TAG, "There was an error reading data from Google Fit", e));
//
//    }
//    private int dumpDataSet(DataSet dataSet) {
//        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}");
//        int totalSteps = 0;
//        for (DataPoint dp : dataSet.getDataPoints()) {
//            Log.i(TAG, "Data point:");
//            Log.i(TAG, "\tType: ${dp.dataType.name}");
//            Log.i(TAG, "\tStart: ${dp.getStartTimeString()}");
//            Log.i(TAG, "\tEnd: ${dp.getEndTimeString()}");
//
//
//            for (Field field : dp.getDataType().getFields()) {
//                String fieldName = field.getName();
//                totalSteps += dp.getValue(field).asInt();
//
//                Log.i(TAG, "\tfield: " + fieldName + "value: " + dp.getValue(field));
//            }
//        }
//        return totalSteps;
//    }
//    private void readData() {
//        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
//                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
//                .addOnSuccessListener(
//                        new OnSuccessListener<DataSet>() {
//                            @Override
//                            public void onSuccess(DataSet dataSet) {
//                                int userInputSteps = 0;
//
//                                for (DataPoint dp : dataSet.getDataPoints()) {
//                                    for(Field field : dp.getDataType().getFields()) {
//                                        Log.d("Stream Name : ", dp.getOriginalDataSource().getStreamName());
//                                        if(!"user_input".equals(dp.getOriginalDataSource().getStreamName())){
//                                            int steps = dp.getValue(field).asInt();
//                                            userInputSteps += steps;
//                                        }
//                                    }
//                                }
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "There was a problem getting the step count.", e);
//                            }
//                        });
//    }
//
//    private void saveNowTime(Boolean bReset) {
//        long nowTime = System.currentTimeMillis();
//        if(bReset)
//            nowTime = 0;
//        try {
//            FileOutputStream os = openFileOutput("Time.txt",MODE_PRIVATE);
//            os.write((String.valueOf(nowTime)).getBytes());
//            Log.i(TAG, "Save Time = " + (String.valueOf(nowTime)));
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private long loadTime() {
//        try {
//            FileInputStream fis = openFileInput("Time.txt");
//            byte[] buf = new byte[1024];
//            int nRLen = fis.read(buf);
//            String strBuff = new String(buf,0,nRLen);
//            Log.i(TAG, "Load Time = " + strBuff);
//            fis.close();
//
//            return Long.parseLong(strBuff);
//
//        } catch (Exception e) {
//            Log.i(TAG, "Not exist file");
//        }
//        return 0;
//    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
//                subscribe();
//            }
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        isBack = false;
        //saveNowTime(false);
        //sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
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
        //GoogleFitPermissionCheck();
        isBack = true;
        if(stepCountSensor != null)
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            tvStepCount.setText("Step Count : " + String.valueOf(event.values[0]));
            Log.i(TAG, "Step Count : " + String.valueOf(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        //editor.putInt("step",)
    }
}