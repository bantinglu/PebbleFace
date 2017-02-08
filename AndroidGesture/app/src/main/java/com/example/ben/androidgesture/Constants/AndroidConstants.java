package com.example.ben.androidgesture.Constants;

import android.os.Environment;

import java.util.UUID;

/**
 * Created by ben on 2/7/2017.
 */

public class AndroidConstants {

    private AndroidConstants(){}

    public static final String DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString();

    // The tuple key corresponding to a vector received from the watch
    public static final int PP_KEY_CMD = 128;
    public static final int PP_KEY_X   = 1;
    public static final int PP_KEY_Y   = 2;
    public static final int PP_KEY_Z   = 3;

    @SuppressWarnings("unused")
    public static final int PP_CMD_INVALID = 0;
    public static final int PP_CMD_VECTOR  = 1;
    public static final int PP_CMD_DONE    = 64;

    public static final int VECTOR_INDEX_X  = 0;
    public static final int VECTOR_INDEX_Y  = 1;
    public static final int VECTOR_INDEX_Z  = 2;
    public static final UUID PEBBLE_UUID = UUID.fromString("cf30b8b5-2cee-4f3a-bec8-234523a3ffe4");
}
