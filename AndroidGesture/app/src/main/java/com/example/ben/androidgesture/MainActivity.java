package com.example.ben.androidgesture;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity
{

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String[] menu = {"Raw Accelerometer Vectors", "TapReceiver"};
        setListAdapter(new ArrayAdapter<String>(this, R.layout.simple_layout, menu));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(this, PebbleReceiver.class);
                break;
            case 1:
                intent = new Intent(this, TapReceiver.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

}
