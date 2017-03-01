package com.example.ben.androidgesture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ben.androidgesture.Constants.AndroidConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity
{
    private LocationManager locationManager=null;
    private LocationListener locationListener=null;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10
// metters

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Buttons and Text
    //TextView locationText = (TextView) findViewById(R.id.locationText);

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    ACCESS_COARSE_LOCATION );
        }
        */
        // LOcation
        // Start location stuff
        /*
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        */
        String[] menu = {AndroidConstants.PATH };
        setContentView(R.layout.simple_layout);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //setListAdapter(new ArrayAdapter<String>(this, R.layout.simple_layout, menu));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    public void startIntent(View view) {
        Intent intent = null;
         intent = new Intent(this, PebbleReceiver.class);
         startActivity(intent);

    }

    public void checkGPS(View view){
        TextView locationText = (TextView) findViewById(R.id.locationText);
        locationText.setText( Boolean.toString(displayGpsStatus()) );
    }

    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);


        TextView locationText = (TextView) findViewById(R.id.GpsText);

        if (gpsStatus) {
            /*
            try {
                locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationText.setText( "Hurray" );
            } catch (SecurityException e) {
                locationText.setText( "Fail" ); // lets the user know there is a problem with the gps
            }
            */
            return true;
        } else {
            return false;
        }
    }
}

/*
public class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location loc) {
       // TextView displayText = (TextView) findViewById(R.id.GpsText);


        //displayText.setText("");
        // pb.setVisibility(View.INVISIBLE);
            Toast.makeText(getBaseContext(),"Location changed : Lat: " +
                            loc.getLatitude()+ " Lng: " + loc.getLongitude(),
                    Toast.LENGTH_SHORT).show();
        String longitude = "Longitude: " +loc.getLongitude();
        //Log.v(TAG, longitude);
        String latitude = "Latitude: " +loc.getLatitude();
        //Log.v(TAG, latitude);

        String cityName=null;
        /*Geocoder gcd = new Geocoder(getBaseContext(),
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc
                    .getLongitude(), 1);
            if (addresses.size() > 0)
                System.out.println(addresses.get(0).getLocality());
            cityName=addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String s = longitude+"\n"+latitude;
        //"\n\nMy Currrent City is: "+cityName;
        displayText.setText(s);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider,
                                int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}
*/