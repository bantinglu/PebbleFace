package com.example.ben.androidgesture;

/**
 * Created by ben on 1/31/2017.
 */

public class AccelData
{
    private final int xData;
    private final int yData;
    private final int zData;

    public AccelData(final int x, final int y, final int z)
    {
        this.xData = x;
        this.yData = y;
        this.zData = z;
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
}
