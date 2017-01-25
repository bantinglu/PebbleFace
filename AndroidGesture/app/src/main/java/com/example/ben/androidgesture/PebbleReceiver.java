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

    private PebbleKit.PebbleDataReceiver dataReceiver;
    private static final UUID PEBBLE_UUID = UUID.fromString("cf30b8b5-2cee-4f3a-bec8-234523a3ffe4");

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);

        /*
        PebbleDictionary dict = new PebbleDictionary();
        final int AppKeyContactName = 0;
        final int AppKeyAge = 1;

        final String contactName = "sheldon";
        final int age = 12;

        dict.addString(AppKeyContactName, contactName);
        dict.addInt32(AppKeyAge, age);
        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_UUID, dict);*/

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict)
            {
                PebbleKit.sendAckToPebble(context, transactionId);
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
