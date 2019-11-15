package com.viettel.vht.remoteapp.common;

import com.viettel.vht.remoteapp.monitoring.MonitoringStatus;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    public static List<MonitoringStatus> getAirStatusConfig(){
        List<MonitoringStatus> result = new ArrayList<>();
        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8b","AQ","", "ic_aq"));
        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8a","CO\u2082","ppm","ic_co2"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce95","Formaldehyde","mg/m\u2083","ic_formaldehyde"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce98","Humidity","%","ic_humidity"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce94","PM","\u00B5g/m\u2083","ic_pm"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9a","Pressure","hPa","ic_pressure"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce97","Temperature","\u2109","ic_temperature"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9e","VOC(EtOH)","ppm","ic_voc"));

        return result;
    }
}
