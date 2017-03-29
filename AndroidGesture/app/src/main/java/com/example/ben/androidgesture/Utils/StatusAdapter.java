package com.example.ben.androidgesture.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ben.androidgesture.Models.StatusRow;
import com.example.ben.androidgesture.R;

import java.util.ArrayList;

/**
 * Created by ben on 3/28/2017.
 */

public class StatusAdapter extends ArrayAdapter<StatusRow>
{
    public StatusAdapter(Context context, ArrayList<StatusRow> status)
    {
        super(context, 0, status);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        StatusRow sr = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.status_list_layout ,parent, false);
        }

        TextView deviceName = (TextView) convertView.findViewById(R.id.name);
        TextView deviceStatus = (TextView) convertView.findViewById(R.id.status);
        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.icon);

        // Populate the data into the template view using the data object
        deviceName.setText("Name: " + sr.name);
        deviceStatus.setText("Status: " + sr.status);

        if (sr.status == "ON")
        {
            deviceImage.setImageResource(R.drawable.check_mark);
        }
        else if (sr.status == "OFF")
        {
            deviceImage.setImageResource(R.drawable.red_x);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}