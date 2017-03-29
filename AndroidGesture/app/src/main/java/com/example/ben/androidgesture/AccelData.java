package com.example.ben.androidgesture;

/**
 * Created by ben on 1/31/2017.
 */

public class AccelData
{
    private final int xData;
    private final int yData;
    private final int zData;

    private long timestampData = 0;

    public AccelData(final int x, final int y, final int z)
    {
        this.xData = x;
        this.yData = y;
        this.zData = z;
    }

    public AccelData(final int x, final int y, final int z, final long timestamp)
    {
        this.xData = x;
        this.yData = y;
        this.zData = z;

        this.timestampData = timestamp;
    }

    public int getX()
    {
        return this.xData;
    }
    public int getY()
    {
        return this.yData;
    }
    public int getZ()
    {
        return this.zData;
    }

    public long getTime() { return this.timestampData; }
}