package com.example.ben.androidgesture;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends ListActivity implements BeaconConsumer, RangeNotifier
{
    Intent intent = null;


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 50;
    // Beacon Start
    private BeaconManager mBeaconManager;
    private double closestDistance = 100;
    private String closestId = "bb";
    private Boolean onceIsEnough = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String[] menu = {"Raw Accelerometer Vectors", "TapReceiver", "Device Status"};
        setListAdapter(new ArrayAdapter<String>(this, R.layout.simple_layout, menu));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {

        switch (position) {
            case 0:
                intent = new Intent(this, PebbleReceiver.class);
                break;
            case 1:
                intent = new Intent(this, TapReceiver.class);
                break;
            case 2:
                intent = new Intent(this, DeviceStatus.class);
                break;
            }

        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PLEASE", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }




    @Override
    public void onResume() {
        super.onResume();

        Log.d("RangingActivity", "I see !");

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        Log.d("RangingActivity", "I see 1!");
        Log.d("RangingActivity", mBeaconManager.toString());
        // Detect the main Eddystone-UID frame:
        if(!onceIsEnough) {
            Log.d("RangingActivity", "ONLY ONCE");
            mBeaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
            Log.d("RangingActivity", "I see 2");


            mBeaconManager.bind(this);
            onceIsEnough = true;
        }
        Log.d("RangingActivity", "I see e");

    }

    @Override
    public void onBeaconServiceConnect() {

        Log.d("RangingActivity", "I see a beacon transmitting namespace id: ");

        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(beacons.size() == 0){
            closestId = "new";
            closestDistance = 100;
        }

        for (Beacon beacon: beacons) {
            //if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d("RangingActivity", "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");
                final String beaconText = " ID:" + namespaceId + "  DIST: " + beacon.getDistance();


                if( (beacon.getDistance() < closestDistance) && !(closestId.equals(namespaceId.toString())) ){
                    closestId = namespaceId.toString();
                    closestDistance = beacon.getDistance();
                    Log.d("RangingActivity", "BLOCK INTENT STARTED");
                    Intent intent = null;
                    switch(closestId) {
                        case "0x00000000000000000000":
                            intent = new Intent(this, PebbleReceiver.class);
                            break;
                        case "0x2f234454f4911ba9ffa6":
                            intent = new Intent(this, TapReceiver.class);
                            break;
                        /*case "02":
                            intent = new Intent(this, PebbleReceiver.class);*/
                    }

                    startActivity(intent);
                    Log.d("RangingActivity", "INTENT STARTED" +
                            "" +
                            "");
                }
                /*Intent intent = null;
                intent = new Intent(this, PebbleReceiver.class);
                startActivity(intent);

                // Send Closest device with intent to class

                */
                /*runOnUiThread(new Runnable() {
                    public void run() {
                        ((TextView)RangingActivity.this.findViewById(R.id.message)).setText("Eddystone:" + beaconText);
                    }
                });*/
           // }
        }
    }
}
