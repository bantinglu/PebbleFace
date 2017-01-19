package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


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
    private static final UUID PEBBLE_UUID = UUID.fromString("273761eb-97dc-4f08-b353-3384a2170902");


    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume: ");

        final Handler handler = new Handler();

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict)
            {
                PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);
                Long number = dict.getInteger(PP_KEY_CMD);
            }
        };
    }
}
