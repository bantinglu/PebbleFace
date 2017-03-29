package com.example.ben.androidgesture;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ben.androidgesture.Models.StatusRow;
import com.example.ben.androidgesture.Utils.StatusAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ben on 3/28/2017.
 */

public class DeviceStatus extends Activity {

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
}
