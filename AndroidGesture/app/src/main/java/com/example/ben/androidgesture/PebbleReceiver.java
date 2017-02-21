package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.example.ben.androidgesture.Constants.AndroidConstants;

import java.util.Timer;
import java.util.TimerTask;

import java.io.*;
import java.sql.Timestamp;

import android.widget.TextView;



/**
 * Created by ben on 1/18/2017.
 */

public class PebbleReceiver extends Activity {


    private static int vector[] = new int[3];

    private PebbleKit.PebbleDataReceiver dataReceiver;
    private Timer timer;
    private boolean saveNext = false;

    private DataHolder pebbleAccelDataHolder;

    private void updateScreen()
    {
        final String x = "X:" + vector[AndroidConstants.VECTOR_INDEX_X];
        final String y = "Y:" + vector[AndroidConstants.VECTOR_INDEX_Y];
        final String z = "Z:" + vector[AndroidConstants.VECTOR_INDEX_Z];

        AccelData ad = new AccelData(vector[AndroidConstants.VECTOR_INDEX_X],
                                     vector[AndroidConstants.VECTOR_INDEX_Y],
                                     vector[AndroidConstants.VECTOR_INDEX_Z]);

        pebbleAccelDataHolder.pushToCurrentSet(ad);

        TextView xAccelAxis = (TextView) findViewById(R.id.accelerometerX);
        xAccelAxis.setText(x);

        TextView yAccelAxis = (TextView) findViewById(R.id.accelerometerY);
        yAccelAxis.setText(y);

        TextView zAccelAxis = (TextView) findViewById(R.id.accelerometerZ);
        zAccelAxis.setText(z);

    }

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        timer = new Timer();
        pebbleAccelDataHolder  = new DataHolder();
        setContentView(R.layout.activity_main);
        PebbleKit.startAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_UUID);

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

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            File dir = new File(AndroidConstants.PATH);
            dir.mkdirs();
            File file = new File(AndroidConstants.PATH + AndroidConstants.FILE_NAME);
            if(!file.exists()){
                file.createNewFile();
            }
            file.setReadable(true, false);
            FileOutputStream stream = new FileOutputStream(file);
            for(AccelData i : pebbleAccelDataHolder.popData()){
                stream.write((Integer.toString(i.getX()) + ", " + Integer.toString(i.getY())
                        + ", " + Integer.toString(i.getY()) + ", " + timestamp.toString() + "\n").getBytes());
            }
            MediaScannerConnection.scanFile(this, new String[] {dir.getAbsolutePath()}, null, null);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            stream.close();
        } catch(IOException e){
            e.printStackTrace();
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
                saveDataToFile();
            }
        }, 2000);
    }

}
