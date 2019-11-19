package com.viettel.vht.remoteapp.common;

import android.content.Context;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.monitoring.MonitoringStatus;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    public static List<MonitoringStatus> getAirStatusConfig(Context ctx){
        List<MonitoringStatus> result = new ArrayList<>();
//        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8b", "AQ","", "ic_aq"));
//        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8a","CO\u2082","ppm","ic_co2"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce95","Formaldehyde","mg/m\u2083","ic_formaldehyde"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce98","Độ ẩm","%","ic_humidity"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce94","PM","\u00B5g/m\u2083","ic_pm"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9a","Áp suất","hPa","ic_pressure"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce97","Nhiệt độ","\u2109","ic_temperature"));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9e","VOC(EtOH)","ppm","ic_voc"));

//
        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8b", ctx.getString(R.string.AQ),"", "ic_aq"));
        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8a",ctx.getString(R.string.CO2),"ppm","ic_co2"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce95",ctx.getString(R.string.H2CO),"mg/m\u2083","ic_formaldehyde"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce94",ctx.getString(R.string.PM),"\u00B5g/m\u2083","ic_pm"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce97",ctx.getString(R.string.Temperature),"\u2109","ic_temperature"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce98",ctx.getString(R.string.HM),"%","ic_humidity"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9a",ctx.getString(R.string.Pressure),"hPa","ic_pressure"));
        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9e",ctx.getString(R.string.VOC),"ppm","ic_voc"));
        return result;
    }
}
