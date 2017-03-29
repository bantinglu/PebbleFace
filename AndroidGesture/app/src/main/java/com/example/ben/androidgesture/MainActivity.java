package com.example.ben.androidgesture;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.*;
import java.util.ArrayList;

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
    public ArrayList<AccelData> importTestData(){
        ArrayList<AccelData> testData = new ArrayList<>();
        try {
            AssetManager am = this.getAssets();
            InputStream is = am.open("Right1.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitData = line.split(",");
                splitData[0] = splitData[0].replaceAll("\\s+", "");
                splitData[1] = splitData[1].replaceAll("\\s+", "");
                splitData[2] = splitData[2].replaceAll("\\s+", "");
                Integer x = Integer.parseInt(splitData[0]);
                Integer y = Integer.parseInt(splitData[1]);
                Integer z = Integer.parseInt(splitData[2]);
                AccelData newLine = new AccelData(x, y, z);
                testData.add(newLine);
            }
            bufferedReader.close();

        } catch(Exception e){
            e.printStackTrace();
        }
        return testData;

    }
}
