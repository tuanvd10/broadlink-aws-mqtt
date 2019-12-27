package com.viettel.vht.remoteapp.common;


import androidx.annotation.NonNull;

public class Constants {
    public static final int BUFFER_SIZE = 2048;
    public static final String API_TOKEN = "fb5fb49e-2cde-44a6-90c0-8110df215fb6";
    public static final String DEVICE_ID = "5dc0391a8ba45e000102d77f";
    public static final int UPDATE_DATA_TIME = 5; // second
    // Name of argment to pass data from activity to fragment
    public static final String ARG_MQTT_CLIENT = "mqtt-client";
    // Constants for air purifier
    public static final int MAX_AIR_PURIFIER_SPEED = 3;
    public static final int MIN_AIR_PURIFIER_SPEED = 0;

    public static final int LOOP_NUMBER = 20;
    public static final long SLEEP_TIME = 500L;
    public static final long WAIT_NEXT_LOOP = 2000L;
    public static final long WAIT_TO_STATE_CHANGE = 3000L;
    public static final long WAIT_TO_UPDATE_UI = 1000L;

    public static final int MAX_TRY_REQUEST = 1;

    public static final int CHECK_DIFFERENCE_LOOP_NUMBER = 1;
    public static final long CHECK_DIFFERENCE_SLEEP_TIME = 1000L;

}

