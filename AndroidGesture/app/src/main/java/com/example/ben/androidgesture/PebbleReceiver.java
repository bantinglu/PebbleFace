package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ben.androidgesture.Utils.Gesture;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.example.ben.androidgesture.Constants.AndroidConstants;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.util.Timer;
import java.util.TimerTask;

import java.io.*;
import java.sql.Timestamp;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;



/**
 * Pebble Data Receiver page
 *
 */

public class PebbleReceiver extends Activity {
    private static int vector[] = new int[3];

    // Output Gesture File name button group
    private static RadioGroup radioGroup;
    private static RadioButton radioButton;

    // Pebble Data kit and Timer
    private PebbleKit.PebbleDataReceiver dataReceiver;
    private Timer timer;
    private boolean saveNext = false;

    /// Get Non moving points
    private boolean noStillValue = true;
    private boolean watchNotMoving = false;
    private AccelData stillValue;
    /// Store them in recent
    private AccelData recentStillData[] = new AccelData[2];
    private int recentDataIndex = 0;
    private boolean addedRecentData = false;
    /// Min Time displace required to satisfy gesture time condtion (1000 = 1 second)
    private static long minGestureTime = 1000;

    // Test Data Var
    private static int writeCount = 0;

    // For Static install
    private String deviceIp = "http://192.168.1.2:8081";

    // Threshold for gesture displacement
    private static int minGestureDisplaceThreshold = 100;
    private static int maxGestureDisplaceThreshold = 200;

    // Not really used, can be deleted
    private boolean lastRequestPass = false;
    private boolean waitingOnRequest = false;

    // Try and fix indexing
    private boolean firstGesture = true;

    private static int stillThreshold = 40;
    // Minimum number of sets of data within threshold to determine 'not moving'
    private static int minWaitData = 5;
    private static int waitDataCount = 0;

    private DataHolder pebbleAccelDataHolder;

    private void updateScreen()
    {
        final String x = "X:" + vector[AndroidConstants.VECTOR_INDEX_X];
        final String y = "Y:" + vector[AndroidConstants.VECTOR_INDEX_Y];
        final String z = "Z:" + vector[AndroidConstants.VECTOR_INDEX_Z];

        final long timestamp = (System.currentTimeMillis());

        AccelData ad = new AccelData(vector[AndroidConstants.VECTOR_INDEX_X],
                                     vector[AndroidConstants.VECTOR_INDEX_Y],
                                     vector[AndroidConstants.VECTOR_INDEX_Z],
                                         timestamp);

        pebbleAccelDataHolder.pushToCurrentSet(ad);

        TextView xAccelAxis = (TextView) findViewById(R.id.accelerometerX);
        xAccelAxis.setText(x);

        TextView yAccelAxis = (TextView) findViewById(R.id.accelerometerY);
        yAccelAxis.setText(y);

        TextView zAccelAxis = (TextView) findViewById(R.id.accelerometerZ);
        zAccelAxis.setText(z);

        // Sitting still start
        final String sittingStill = "Not moving";
        final String moving = "Moving!";
        TextView stillSit = (TextView) findViewById(R.id.stillCheck);

        //if(noStillValue){
          //  stillSit.setText("No Data!!!");
        //} else {

        if (watchNotMoving) {
            stillSit.setText(sittingStill);
        } else {
            stillSit.setText(moving);
        }

        //}
    }

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);



        timer = new Timer();
        pebbleAccelDataHolder  = new DataHolder();
        setContentView(R.layout.activity_main);
        PebbleKit.startAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_UUID);

        // Sitting Still
        dataReceiver = new PebbleKit.PebbleDataReceiver(AndroidConstants.PEBBLE_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict) {
                PebbleKit.sendAckToPebble(context, transactionId);
                saveNext = true;

                final Long cmdValue = dict.getInteger(AndroidConstants.PP_KEY_CMD);
                if (cmdValue == null) {
                    return;
                }

                if (cmdValue.intValue() == AndroidConstants.PP_CMD_VECTOR) {

                    // Capture the received vector.
                    final Long xValue = dict.getInteger(AndroidConstants.PP_KEY_X);
                    if (xValue != null) {
                        vector[AndroidConstants.VECTOR_INDEX_X] = xValue.intValue();
                    }

                    final Long yValue = dict.getInteger(AndroidConstants.PP_KEY_Y);
                    if (yValue != null) {
                        vector[AndroidConstants.VECTOR_INDEX_Y] = yValue.intValue();
                    }

                    final Long zValue = dict.getInteger(AndroidConstants.PP_KEY_Z);
                    if (zValue != null) {
                        vector[AndroidConstants.VECTOR_INDEX_Z] = zValue.intValue();
                    }

                    // Start
                    if(xValue != null && yValue != null && zValue != null) {
                        // Determine movement
                        if (noStillValue) {
                            // Try to create a still point to 'hover' around
                            final long timestamp = (System.currentTimeMillis());
                            stillValue = new AccelData(xValue.intValue(), yValue.intValue(), zValue.intValue(), timestamp );
                            noStillValue = false;
                            waitDataCount = 0;
                            addedRecentData = false;
                        } else {
                            //Check still value != null

                            // Check if were in acceptable range for 'Wait' each +-threshold
                            if (stillValue.getX() < (xValue.intValue() + stillThreshold) && stillValue.getX() > (xValue.intValue() - stillThreshold)
                                    && stillValue.getY() < (yValue.intValue() + stillThreshold) && stillValue.getY() > (yValue.intValue() - stillThreshold)
                                    && stillValue.getZ() < (zValue.intValue() + stillThreshold) && stillValue.getZ() > (zValue.intValue() - stillThreshold) ){

                                waitDataCount++;
                                if(waitDataCount == minWaitData)
                                {
                                    watchNotMoving = true;
                                }
                                else if (waitDataCount > minWaitData && !addedRecentData) {
                                    addedRecentData = true;
                                    if (firstGesture) {
                                        recentStillData[0] = stillValue;
                                        recentStillData[1] = null;
                                        firstGesture = false;
                                    }
                                    else
                                    {
                                        recentStillData[1] = stillValue;
                                    }
                                }

                                if(!firstGesture && recentStillData[1] != null){
                                    boolean sentHttp = false;
                                    // Determine if valid gesture
                                    int xDisplace = java.lang.Math.abs(recentStillData[0].getX() - recentStillData[1].getX());
                                    int yDisplace = java.lang.Math.abs(recentStillData[0].getY() - recentStillData[1].getY());
                                    long timeDisplace = recentStillData[1].getTime()- recentStillData[0].getTime();

                                    if(timeDisplace > minGestureTime)
                                        if( xDisplace > minGestureDisplaceThreshold && xDisplace < maxGestureDisplaceThreshold ){
                                            sendHTTP(deviceIp + "/?light1");
                                            sentHttp = true;
                                        } else if ( yDisplace > minGestureDisplaceThreshold && yDisplace < maxGestureDisplaceThreshold ){
                                            sendHTTP(deviceIp + "/?light2");
                                            sentHttp = true;
                                        }

                                    // TEST GESTURE DATA OUTPUT
                                    /*
                                    writeCount++;
                                    try{
                                        File dir = new File(AndroidConstants.PATH);
                                          dir.mkdirs();
                                        File file1 = new File(AndroidConstants.PATH + "/Gesture"+ sentHttp + writeCount + ".txt");
                                        if(!file1.exists()){
                                            file1.createNewFile();
                                        }
                                        file1.setReadable(true, false);
                                        FileOutputStream stream = new FileOutputStream(file1);
                                        stream.write(("1: X:" + recentStillData[0].getX() + " Y:" + recentStillData[0].getY() + " Z:" + recentStillData[0].getZ() + "\r\n").getBytes());
                                        stream.write(("2: X:" + recentStillData[1].getX() + " Y:" + recentStillData[1].getY() + " Z:" + recentStillData[1].getZ() + "\r\n").getBytes());
                                        stream.write(("3: Displace: " + timeDisplace + "\r\n").getBytes());
                                        stream.write(("4: Gesture?: " + sentHttp + "\r\n").getBytes());
                                        stream.write(( "\r\n").getBytes());
                                        MediaScannerConnection.scanFile(context, new String[] {dir.getAbsolutePath()}, null, null);
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));
                                        stream.close();
                                    } catch(IOException e){
                                        Log.e("temp", "Failed when saving data to text file");
                                    }
                                    */

                                    // Reset Array
                                    recentStillData[0] = recentStillData[1];
                                    recentStillData[1] = null;
                                    noStillValue = true;
                                }

                                // Gesture Check
                            } else {
                                // Else reset the still value
                                noStillValue = true;
                                watchNotMoving = false;
                            }
                        }
                    }
                }
                updateScreen();
                resetTimer();
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();
        PebbleKit.startAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_UUID);
        PebbleKit.registerReceivedDataHandler(this, dataReceiver);

    }
    @Override
    public void onPause() {
        super.onPause();

        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }
        PebbleKit.closeAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_UUID);
    }

    public void saveDataToFile(){

        radioGroup = (RadioGroup)findViewById(R.id.radio_group);
        int selectedId=radioGroup.getCheckedRadioButtonId();
        radioButton=(RadioButton)findViewById(selectedId);
        System.out.println(radioButton.getId());

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            File dir = new File(AndroidConstants.PATH);
            dir.mkdirs();
            File file = new File(AndroidConstants.PATH + "/" + radioButton.getText() +".txt");
            if(!file.exists()){
                file.createNewFile();
            }
            file.setReadable(true, false);
            FileOutputStream stream = new FileOutputStream(file);
            for(AccelData i : pebbleAccelDataHolder.popData()){
                stream.write((Integer.toString(i.getX()) + ", " + Integer.toString(i.getY())
                        + ", " + Integer.toString(i.getZ()) + "\r\n").getBytes());
            }
            stream.write(( "\r\n").getBytes());
            MediaScannerConnection.scanFile(this, new String[] {dir.getAbsolutePath()}, null, null);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            stream.close();

            //recentDataIndex = 0;
/*
            final String stopped = "Data stream stopped";
            TextView stillSits = (TextView) findViewById(R.id.stillCheck);
            stillSits.setText(stopped);*/

        } catch(IOException e){
            Log.e("temp", "Failed when saving data to text file");
        }

    }

    private void resetTimer()
    {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pebbleAccelDataHolder.saveCurrentSet();

                // Reset still - working?
                noStillValue = true;
                watchNotMoving = false;

                saveDataToFile();
                //updateScreen(); Not allowed?
            }
        }, 2000);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
    }

    public void sendRequest(View view) {
        // Kabloey
        // Spam URL button
        String url =deviceIp + "/?light1";
        sendHTTP(url);
        sendHTTP(deviceIp + "/?light2");
        sendHTTP(deviceIp + "/?light3");
    }

    public void sendHTTP(String sendUrl) {
        final TextView mTextView = (TextView) findViewById(R.id.sendRequestText);
        final TextView gestureView = (TextView) findViewById(R.id.gestureText);

        // From dev online code
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url ="http://192.168.1.4:8081/?light1";
        String url = sendUrl;
        gestureView.setText(url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        waitingOnRequest = false;
                        lastRequestPass = true;
                        mTextView.setText("Response succeeded");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        waitingOnRequest = false;
                        lastRequestPass = false;
                        mTextView.setText("That didn't work!");
                    }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // CURRENTLY NOT WORKING, TESTING SETUP FUNCTION TO FIND IP,
    // BEST SOLuTION IDEA, 1. have a couple ip's setup and take anything from in range
    public void findIP(View view){
        boolean foundIp = false;
        int ipCount = 1;
        TextView mTextView = (TextView) findViewById(R.id.sendRequestText);
        lastRequestPass = false;
        while(!foundIp && ipCount < 50){
            deviceIp = "http://192.168.1." + Integer.toString(ipCount++) + ":8081";
            mTextView.setText("Waiting!");
            sendHTTP(deviceIp);
            while( mTextView.toString().equals("Waiting!") ){
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            if( mTextView.toString().equals("Response succeeded") ) {
                foundIp = true;
                mTextView.setText(deviceIp);
            }
        }
        if(ipCount > 49){
            mTextView.setText("FAIL");
            deviceIp = "http://192.168.1.2:8081";
        }
    }

}
