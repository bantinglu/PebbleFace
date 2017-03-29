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
    private final String deviceIP = "http://192.168.1.2:8081";
    private final int NUMBER_OF_ITEMS = 3;


    private StatusRow optionOne = new StatusRow("IP", "OFF");
    private StatusRow optionTwo = new StatusRow("IP", "OFF");
    ArrayList<StatusRow> arrayOfStatus = new ArrayList<StatusRow>();
    private StatusAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_status_layout);
        // Create the adapter to convert the array to views
        adapter = new StatusAdapter(this, arrayOfStatus);
        adapter.add(optionOne);
        adapter.add(optionTwo);

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
        sendHTTP(deviceIP+"/?hStatus");
        optionOne.name = "Sheldon";
        adapter.notifyDataSetChanged();
    }

    public void sendHTTP(final String sendUrl) {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, sendUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                       // waitingOnRequest = false;
                        //lastRequestPass = true;
                        Log.d("myTag","Response succeeded");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //waitingOnRequest = false;
                //lastRequestPass = false;
                Log.d("myTag","That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }




}
