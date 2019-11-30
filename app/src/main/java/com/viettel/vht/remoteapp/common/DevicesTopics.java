package com.viettel.vht.remoteapp.common;

public class DevicesTopics {
    public final static String SUBSCRIBE_DEVICE_INFO = "broadlink-stat/airpurifier/info";
    public final static String REQUEST_DEVICE_INFO = "broadlink/airpurifier/getinfo";  // payload: getInfo

    public final static String REQUEST_GET_CURRENT_MODE_TOPIC = "broadlink/airthinx/getcurrentmode"; // payload = "getairthinxmode"
    public final static String REQUEST_SET_CURRENT_MODE_TOPIC = "broadlink/airthinx/setmode"; // payload = "setairthinxmode-<auto/manuall>"

    public final static String SUBSCRIBE_CURRENT_MODE_TOPIC="broadlink-stat/airthinx/currentmode"; // response = auto/manual

}
