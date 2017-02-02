package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;
import android.os.Vibrator;
import android.widget.TextView;


/**
 * Created by ben on 1/18/2017.
 */

public class PebbleReceiver extends Activity {

    private static final String TAG = "PebblePointer";

    // The tuple key corresponding to a vector received from the watch
    private static final int PP_KEY_CMD = 128;
    private static final int PP_KEY_X   = 1;
    private static final int PP_KEY_Y   = 2;
    private static final int PP_KEY_Z   = 3;

    @SuppressWarnings("unused")
    private static final int PP_CMD_INVALID = 0;
    private static final int PP_CMD_VECTOR  = 1;
    private static final int PP_CMD_DONE    = 64;

    private static final int VECTOR_INDEX_X  = 0;
    private static final int VECTOR_INDEX_Y  = 1;
    private static final int VECTOR_INDEX_Z  = 2;

    private static int vector[] = new int[3];

    private PebbleKit.PebbleDataReceiver dataReceiver;
    private static final UUID PEBBLE_UUID = UUID.fromString("cf30b8b5-2cee-4f3a-bec8-234523a3ffe4");


    private DataHolder pebbleAccelDataHolder;

    private void updateScreen()
    {
        final String x = "X:" + vector[VECTOR_INDEX_X];
        final String y = "Y:" + vector[VECTOR_INDEX_Y];
        final String z = "Z:" + vector[VECTOR_INDEX_Z];

        AccelData ad = new AccelData(vector[VECTOR_INDEX_X],
                                     vector[VECTOR_INDEX_Y],
                                     vector[VECTOR_INDEX_Z]);

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
        pebbleAccelDataHolder  = new DataHolder();
        setContentView(R.layout.activity_main);
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_UUID);
        final Handler handler = new Handler();

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict) {
                PebbleKit.sendAckToPebble(context, transactionId);

                final Long cmdValue = dict.getInteger(PP_KEY_CMD);
                if (cmdValue == null) {
                    return;
                }

                if (cmdValue.intValue() == PP_CMD_VECTOR) {

                    // Capture the received vector.
                    final Long xValue = dict.getInteger(PP_KEY_X);
                    if (xValue != null) {
                        vector[VECTOR_INDEX_X] = xValue.intValue();
                    }

                    final Long yValue = dict.getInteger(PP_KEY_Y);
                    if (yValue != null) {
                        vector[VECTOR_INDEX_Y] = yValue.intValue();
                    }

                    final Long zValue = dict.getInteger(PP_KEY_Z);
                    if (zValue != null) {
                        vector[VECTOR_INDEX_Z] = zValue.intValue();
                    }

                }
                else if (cmdValue.intValue() == PP_CMD_DONE)
                {
                    pebbleAccelDataHolder.saveCurrentSet();
                }

                updateScreen();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

        PebbleKit.registerReceivedDataHandler(this, dataReceiver);

    }
    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause: ");

        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }
        PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_UUID);
    }

}
