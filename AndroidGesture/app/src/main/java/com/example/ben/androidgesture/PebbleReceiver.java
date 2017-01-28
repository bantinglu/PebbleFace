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

    private static final int VECTOR_INDEX_X  = 0;
    private static final int VECTOR_INDEX_Y  = 1;
    private static final int VECTOR_INDEX_Z  = 2;

    private static int vector[] = new int[3];

    private PebbleKit.PebbleDataReceiver dataReceiver;
    private static final UUID PEBBLE_UUID = UUID.fromString("cf30b8b5-2cee-4f3a-bec8-234523a3ffe4");

    private void updateScreen()
    {

    }

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);

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
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_UUID);

        PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);

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
