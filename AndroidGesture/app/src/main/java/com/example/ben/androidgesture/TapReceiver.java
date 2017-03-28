package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.example.ben.androidgesture.Utils.Gesture;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.example.ben.androidgesture.Constants.AndroidConstants;

import java.util.Timer;

/**
 * Created by ben on 3/27/2017.
 */

public class TapReceiver extends Activity {

    private PebbleKit.PebbleDataReceiver dataReceiver;
    private long direction;
    private long magnitude;

    public void update()
    {
        String dir = " ";

        switch((int)direction)
        {
            case 1:
                dir = "X";
                break;
            case 2:
                dir = "Y";
                break;
            case 3:
                dir = "Z";
                break;
        }

        final String direct = "Direction: " + dir;
        final String mag = "Magnitude:" + magnitude;

        TextView xAccelAxis = (TextView) findViewById(R.id.directionTap);
        xAccelAxis.setText(direct);

        TextView yAccelAxis = (TextView) findViewById(R.id.magnitudeTap);
        yAccelAxis.setText(mag);
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.tap_receiver);
        PebbleKit.startAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_TAP_UUID);

        dataReceiver = new PebbleKit.PebbleDataReceiver(AndroidConstants.PEBBLE_TAP_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict)
            {
                PebbleKit.sendAckToPebble(context, transactionId);

                final Long cmdValue = dict.getInteger(AndroidConstants.PP_KEY_CMD);

                if (cmdValue.intValue() == AndroidConstants.PP_CMD_VECTOR)
                {
                    direction = dict.getInteger(AndroidConstants.PP_KEY_X);
                    magnitude = dict.getInteger(AndroidConstants.PP_KEY_Y);
                }
                update();
            }

        };
    }


    @Override
    public void onResume() {
        super.onResume();
        PebbleKit.startAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_TAP_UUID);
        PebbleKit.registerReceivedDataHandler(this, dataReceiver);

    }
    @Override
    public void onPause() {
        super.onPause();

        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }
        PebbleKit.closeAppOnPebble(getApplicationContext(), AndroidConstants.PEBBLE_TAP_UUID);
    }
}
