package com.example.ben.androidgesture;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ben.androidgesture.Models.StatusRow;
import com.example.ben.androidgesture.Utils.StatusAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ben on 3/28/2017.
 */

public class DeviceStatus extends Activity {
    final private String deviceIp = "http://192.168.1.7:8081";
    private final int NUMBER_OF_ITEMS = 3;
    private final List<String> ITEMS =  Arrays.asList("Door", "Lights", "Ghost");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_status_layout);

        ArrayList<StatusRow> arrayOfStatus = new ArrayList<StatusRow>();
        // Create the adapter to convert the array to views
        StatusAdapter adapter = new StatusAdapter(this, arrayOfStatus);

        StatusRow newstatus = new StatusRow("Door", "San Diego", "Off");
        adapter.add( newstatus);
        newstatus = new StatusRow("Door", "San Diego", "Off");
        adapter.add( newstatus);
        newstatus = new StatusRow("Door", "San Diego", "Off");
        adapter.add( newstatus);
        newstatus = new StatusRow("Door", "San Diego", "Off");
        adapter.add( newstatus);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listItems);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void selfDestruct(View view)
    {
        Log.d("myTag", deviceIp+"/?hStatus");
        sendHTTP(deviceIp+"/?hStatus");
    }

    public void sendHTTP(final String url)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        // Display the first 500 characters of the response string.
                        Log.d("myTag", "This is my message");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("myTag", "Go fuck yourself");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
