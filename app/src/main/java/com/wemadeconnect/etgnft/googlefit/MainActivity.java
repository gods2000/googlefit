package com.wemadeconnect.etgnft.googlefit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private FitnessOptions fitnessOptions;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleFitnessInit();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }
    }

    private void GoogleFitnessInit() {
        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                        .build();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void subscribe() {
// Read the data that's been collected throughout the past week.
        List<String> theDates = new ArrayList<String>();
        List<Integer> totalAvgSteps = new ArrayList<Integer>();
        ZonedDateTime endTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        }
        ZonedDateTime startTime = endTime.minusWeeks(1);

        Log.i(TAG, "Range Start: $startTime");
        Log.i(TAG, "Range End: $endTime");

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName(this.getPackageName())
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .enableServerQueries()
                .build();


        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener(response -> {


                    // The aggregate query puts datasets into buckets, so convert to a
                    // single list of datasets
                    for (Bucket bucket : response.getBuckets()) {

                        //convert days in bucket to milliseconds
                        long days = bucket.getStartTime(TimeUnit.MILLISECONDS);
                        //convert milliseconds to date
                        Date stepsDate = new Date(days);
                        //convert date to day of the week eg: monday, tuesday etc
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat df = new SimpleDateFormat("EEE");
                        String weekday = df.format(stepsDate);

                        Log.i(TAG, stepsDate.toString());
                        theDates.add(weekday);

                        for (DataSet dataSet : bucket.getDataSets()) {
                            totalAvgSteps.add(dumpDataSet(dataSet));
                        }
                    }
                    Log.i(TAG, theDates.toString());
                    Log.i(TAG, totalAvgSteps.toString());
                })
                .addOnFailureListener(e ->
                        Log.w(TAG, "There was an error reading data from Google Fit", e));

    }
    private int dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}");
        int totalSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: ${dp.dataType.name}");
            Log.i(TAG, "\tStart: ${dp.getStartTimeString()}");
            Log.i(TAG, "\tEnd: ${dp.getEndTimeString()}");


            for (Field field : dp.getDataType().getFields()) {
                String fieldName = field.getName();
                totalSteps += dp.getValue(field).asInt();

                Log.i(TAG, "\tfield: " + fieldName + "value: " + dp.getValue(field));

            }
        }
        return totalSteps;
    }
    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                int userInputSteps = 0;

                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    for(Field field : dp.getDataType().getFields()) {
                                        Log.d("Stream Name : ", dp.getOriginalDataSource().getStreamName());
                                        if(!"user_input".equals(dp.getOriginalDataSource().getStreamName())){
                                            int steps = dp.getValue(field).asInt();
                                            userInputSteps += steps;
                                        }
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }
}