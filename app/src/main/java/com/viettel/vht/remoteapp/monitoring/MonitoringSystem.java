package com.viettel.vht.remoteapp.monitoring;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Response;
import com.github.ybq.android.spinkit.SpinKitView;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.APILink;
import com.viettel.vht.remoteapp.common.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MonitoringSystem {
    private Activity activity;
    List<MonitoringStatus> listStatus;
    private MonitoringRequest httpRequest;

    public MonitoringSystem(Activity activity) {
        this.activity = activity;
        listStatus = DatabaseHelper.getAirStatusConfig(activity.getApplicationContext());
        httpRequest = new MonitoringRequest(activity.getApplicationContext());
    }

    public void readAndDisplayStatus(final RelativeLayout vAQStatus, final TextView txtAQValue, final TextView txtAQTitle, final TextView txtAQLevel,
                                     final GridView gdView1, final GridView gdView2, final GridView gdView3, final SpinKitView loadingBar) {

        JSONObject jsonObject = new JSONObject(); // requesting json
        try {
            jsonObject.put("last", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.jsonPostRequest(APILink.apiMeasurementsURL, jsonObject, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<MonitoringStatus> gdList1 = new ArrayList<>();
                        List<MonitoringStatus> gdList2 = new ArrayList<>();
                        List<MonitoringStatus> gdList3 = new ArrayList<>();
                        MonitoringStatus aqStatus = null;

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            //System.out.println("**LOG**: " + jsonObject.toString());
                            for (int i = 0; i < listStatus.size(); i++) {
                                if (jsonObject.getString(listStatus.get(i).getDataPointID()) != null) {
                                    listStatus.get(i).setValue(extractMeasurement(jsonObject.getString(listStatus.get(i).getDataPointID())));
                                    calculateQualityLevel(listStatus.get(i));

                                    if (listStatus.get(i).getIconName() == "ic_aq")
                                        aqStatus = listStatus.get(i);

                                    if (listStatus.get(i).getIconName() == "ic_co2"
                                    || listStatus.get(i).getIconName() == "ic_formaldehyde"
                                    || listStatus.get(i).getIconName() == "ic_voc"){
                                        gdList1.add(listStatus.get(i));
                                    }

                                    if (listStatus.get(i).getIconName() == "ic_temperature"
                                            || listStatus.get(i).getIconName() == "ic_humidity"
                                            || listStatus.get(i).getIconName() == "ic_pressure"){
                                        gdList2.add(listStatus.get(i));
                                    }

                                    if (listStatus.get(i).getIconName() == "ic_pm") {
                                        gdList3.add(listStatus.get(i));
                                    }
                                }
                            }

                            loadingBar.setVisibility(View.GONE); // disable the loading bar

                            // set data for grid views
                            gdView1.setAdapter(new MonitoringGridAdapter(activity, gdList3));
                            gdView2.setAdapter(new MonitoringGridAdapter(activity, gdList1));
                            gdView3.setAdapter(new MonitoringGridAdapter(activity, gdList2));
                            gdView1.setBackground(activity.getApplicationContext().getDrawable(R.drawable.border_rectangle));
                            gdView2.setBackground(activity.getApplicationContext().getDrawable(R.drawable.border_rectangle));
                            gdView3.setBackground(activity.getApplicationContext().getDrawable(R.drawable.border_rectangle));
                            GradientDrawable shapeDrawable = (GradientDrawable) ResourcesCompat.getDrawable(activity.getApplicationContext().getResources(),
                                    getDrawableResIdByName(aqStatus.getQualityLevel().toCycle()), null);

                            // set data for AQ Status
                            vAQStatus.setBackground(shapeDrawable);
                            txtAQLevel.setText(aqStatus.getQualityLevel().toString());
                            txtAQTitle.setText(aqStatus.getName());
                            txtAQValue.setText(aqStatus.getValue());

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

    public int getDrawableResIdByName(String resName) {
        int resID = activity.getApplicationContext().getResources().getIdentifier(resName, "drawable", activity.getApplicationContext().getPackageName());
        return resID;
    }

    private void calculateQualityLevel(MonitoringStatus status) {
        AirQualityLevel result;
        double value = Double.parseDouble(status.getValue());
        switch (status.getIconName()) {
            case "ic_aq":
                if (value > 89)
                    result = AirQualityLevel.GOOD;
                else if (value >= 80 && value <= 89)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_co2":
                if (value < 850)
                    result = AirQualityLevel.GOOD;
                else if (value >= 850 && value <= 1100)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_formaldehyde":
                if (value < 0.05)
                    result = AirQualityLevel.GOOD;
                else if (value >= 0.05 && value <= 0.12)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_humidity":
                if (value >= 45 && value <= 60)
                    result = AirQualityLevel.GOOD;
                else if ((value >= 30 && value <= 45) || (value >= 60 && value <= 65))
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_pm":
                if (value < 25.4)
                    result = AirQualityLevel.GOOD;
                else if (value >= 25.4 && value <= 55.4)
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_pressure":
                result = AirQualityLevel.GOOD;
                break;
            case "ic_temperature":
                if (value >= 68 && value <= 74)
                    result = AirQualityLevel.GOOD;
                else if ((value >= 55 && value <= 68) || (value >= 74 && value <= 83))
                    result = AirQualityLevel.MODERATE;
                else
                    result = AirQualityLevel.POOR;
                break;
            case "ic_voc":
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

     //   System.out.println(status.getName() + " = " + value + " - Level: " + result.toString());

        status.setQualityLevel(result);
    }

}
