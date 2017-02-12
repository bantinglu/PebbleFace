package com.example.ben.androidgesture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 1/31/2017.
 */

public class DataHolder {

    List<List<AccelData>> dataHolder;
    List<AccelData> currentDataSet;

    public DataHolder()
    {
        this.dataHolder = new ArrayList<List<AccelData>>();
        this.currentDataSet = new ArrayList<AccelData>();
    }

    public void pushToCurrentSet(final AccelData data)
    {
        currentDataSet.add(data);
    }

    public void saveCurrentSet()
    {
        if(currentDataSet.size() != 0)
        {
            dataHolder.add(currentDataSet);
            currentDataSet = new ArrayList<AccelData>();
        }

    }

    public List<AccelData> normalizeData(final List<AccelData> nonNormalized)
    {
        //temporary return
        return null;
    }
}
