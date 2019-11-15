package com.viettel.vht.remoteapp.monitoring;

import android.app.Activity;
import android.widget.GridView;

import com.android.volley.Response;
import com.viettel.vht.remoteapp.common.APILink;
import com.viettel.vht.remoteapp.common.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MonitoringSystem {
    private Activity activity;
    List<MonitoringStatus> listStatus;
    private MonitoringRequest httpRequest;

    public MonitoringSystem(Activity activity) {
        this.activity = activity;
        listStatus = DatabaseHelper.getAirStatusConfig();
        httpRequest = new MonitoringRequest(activity.getApplicationContext());
    }

    public void readAndDisplayStatus(final GridView gdView) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("last", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.jsonPostRequest(APILink.apiMeasurementsURL, jsonObject, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            //System.out.println("**LOG**: " + jsonObject.toString());
                            for (int i = 0; i < listStatus.size(); i++) {
                                if (jsonObject.getString(listStatus.get(i).getDataPointID()) != null) {
                                    listStatus.get(i).setValue(extractMeasurement(jsonObject.getString(listStatus.get(i).getDataPointID())));
                                    calculateQualityLevel(listStatus.get(i));

                                }
                            }
                            gdView.setAdapter(new MonitoringGridAdapter(activity, listStatus));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private String extractMeasurement(String meas) {
        meas = meas.replace('[', '\0');
        meas = meas.replace(']', '\0');
        return meas.split(",")[1];
    }

    //            result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8b","AQ","", "ic_aq",80,90));
//        result.add(new MonitoringStatus("5dc0d224f7e52600016d7d8a","CO\u2082","ppm","ic_co2",1100,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce95","Formaldehyde","mg/m\u2083","ic_formaldehyde",0,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce98","Humidity","%","ic_humidity",0,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce94","PM","\u00B5g/m\u2083","ic_pm",0,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9a","Pressure","hPa","ic_pressure",0,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce97","Temperature","\u2109","ic_temperature",0,0));
//        result.add(new MonitoringStatus("5dc0d1e8de8e1200015dce9e","VOC(EtOH)","ppm","ic_voc",0,0));
    private void calculateQualityLevel(MonitoringStatus status) {
        AirQualityLevel result;
        double value = Double.parseDouble(status.getValue());
        switch (status.getName()) {
            case "AQ":
                if (value > 89)
                    result = AirQualityLevel.GOOD;
                else if (value >= 80 && value <= 89)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "CO\u2082":
                if (value < 850)
                    result = AirQualityLevel.GOOD;
                else if (value >= 850 && value <= 1100)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "Formaldehyde":
                if (value < 0.05)
                    result = AirQualityLevel.GOOD;
                else if (value >= 0.05 && value <= 0.12)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "Humidity":
                if (value >= 45 && value <= 60)
                    result = AirQualityLevel.GOOD;
                else if ((value >= 30 && value <= 45) || (value >= 60 && value <= 65))
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "PM":
                if (value < 25.4)
                    result = AirQualityLevel.GOOD;
                else if (value >= 25.4 && value <= 55.4)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "Pressure":
                result = AirQualityLevel.GOOD;
                break;
            case "Temperature":
                if (value >= 68 && value <= 74)
                    result = AirQualityLevel.GOOD;
                else if ((value >= 55 && value <= 68) || (value >= 74 && value <= 83))
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "VOC(EtOH)":
                if (value < 1)
                    result = AirQualityLevel.GOOD;
                else if (value >= 1 && value <= 2)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            default:
                result = AirQualityLevel.GOOD;
        }

        status.setQualityLevel(result);
    }

}

enum AirQualityLevel {
    GOOD, MODERATE, POOR, OFFLINE;

    @Override
    public String toString() {
        switch (this) {
            case GOOD:
                return "Good";
            case MODERATE:
                return "Moderate";
            case POOR:
                return "Poor";
            case OFFLINE:
                return "Offline";
            default:
                return "Offline";
        }
    }

    public String toColor() {
        switch (this) {
            case GOOD:
                return "Blue";
            case MODERATE:
                return "Yellow";
            case POOR:
                return "Red";
            case OFFLINE:
                return "Grey";
            default:
                return "Grey";
        }
    }
}