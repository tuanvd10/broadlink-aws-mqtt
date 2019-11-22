package com.viettel.vht.remoteapp.common;

public class AirPurifierTopics {
    // Power and speed air purifier
    public final static String POWER = "broadlink/airpurifier/power";
    public final static String SPEED = "broadlink/airpurifier/speed";
    public final static String SWING = "broadlink/airpurifier/swing";

    // Subcribe topic to get state
    public final static String SUBSCRIBE_STATE_POWER = "broadlink-stat/airpurifier/power";  // ON - OFF
    public final static String SUBSCRIBE_STATE_SPEED = "broadlink-stat/airpurifier/speed";    // 0 - 1 - 2 - 3

    // Send message to server return state of device
    public final static String REQUEST_STATE_POWER = "broadlink/airpurifier/info";
    public final static String REQUEST_STATE_SPEED = "broadlink/airpurifier/info";
}
