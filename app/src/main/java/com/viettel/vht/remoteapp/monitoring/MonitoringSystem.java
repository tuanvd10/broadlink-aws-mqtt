package com.viettel.vht.remoteapp.monitoring;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Response;
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
                                     final GridView gdView1, final GridView gdView2, final GridView gdView3, final ProgressBar loadingBar, final ImageView dsIcon, final TextView dsText) {

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
                        CharSequence mesuareTime = null;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            //System.out.println("**LOG**: " + jsonObject.toString());
                            for (int i = 0; i < listStatus.size(); i++) {
                                if (mesuareTime == null)
                                    mesuareTime = DateUtils.getRelativeTimeSpanString(extractTime(jsonObject.getString(listStatus.get(i).getDataPointID())));
                                if (jsonObject.getString(listStatus.get(i).getDataPointID()) != null) {
                                    listStatus.get(i).setValue(extractMeasurement(jsonObject.getString(listStatus.get(i).getDataPointID())));
                                    calculateQualityLevel(listStatus.get(i));

                                    if (listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.PM1)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.PM25)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.PM10))
                                        gdList3.add(listStatus.get(i));

                                    if (listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.CO2)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.H2CO)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.VOC)) {
                                        gdList1.add(listStatus.get(i));
                                    }

                                    if (listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.Temperature)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.HM)
                                            || listStatus.get(i).getName() == activity.getApplicationContext().getString(R.string.Pressure)) {
                                        gdList2.add(listStatus.get(i));
                                    }

                                    if ( listStatus.get(i).getName() ==  activity.getApplicationContext().getString(R.string.AQ)) {
                                        aqStatus = listStatus.get(i);
                                    }
                                }
                            }
                            loadingBar.setVisibility(View.GONE); // disable the loading bar

                            // set data for grid views
                            gdView1.setAdapter(new MonitoringGridAdapter(activity, gdList3));
                            gdView2.setAdapter(new MonitoringGridAdapter(activity, gdList2));
                            gdView3.setAdapter(new MonitoringGridAdapter(activity, gdList1));
                            gdView1.setBackground(activity.getApplicationContext().getDrawable(R.drawable.rectangle_home_border));
                            gdView2.setBackground(activity.getApplicationContext().getDrawable(R.drawable.rectangle_home_border));
                            gdView3.setBackground(activity.getApplicationContext().getDrawable(R.drawable.rectangle_home_border));
                            GradientDrawable shapeDrawable = (GradientDrawable) ResourcesCompat.getDrawable(activity.getApplicationContext().getResources(),
                                    getDrawableResIdByName(aqStatus.getQualityLevel().toCycle()), null);

                            // set data for AQ Status
//                            View parent = (View)gdView2.getParent();
//                            int aqStatusSize = (int) (parent.getTop()/1.5);
//                            //System.out.println("Size: " + parent.getTop()/1.5);
//                            vAQStatus.setLayoutParams(new ConstraintLayout.LayoutParams(aqStatusSize, aqStatusSize));
                            txtAQLevel.setText(aqStatus.getQualityLevel().toString());
                            txtAQTitle.setText(aqStatus.getName());
                            txtAQValue.setText(aqStatus.getValue());
                            vAQStatus.setBackground(shapeDrawable);

                           // vAQStatus.setLayoutParams(new RelativeLayout.LayoutParams(50,50));



                            // set status for device
                            if (mesuareTime.charAt(0) == '0'){
                                dsText.setText(activity.getApplicationContext().getString(R.string.online));
                                dsText.setTextColor(activity.getColor(R.color.Black));
                                GradientDrawable gradientDrawable = (GradientDrawable) ResourcesCompat.getDrawable(activity.getApplicationContext().getResources(),
                                        R.drawable.online_ic, null);
                                dsIcon.setBackground(gradientDrawable);
                            }
                            else {
                                String msTime = mesuareTime.toString();
                                if (msTime.lastIndexOf("day") > -1){
                                    msTime = msTime.substring(0, msTime.lastIndexOf("day")-1) + " " + activity.getApplicationContext().getString(R.string.dayago);
                                }
                                else if (msTime.lastIndexOf("hour") > -1){
                                    msTime = msTime.substring(0, msTime.lastIndexOf("hour")-1) + " " + activity.getApplicationContext().getString(R.string.hourago);
                                }
                                else {
                                    msTime = msTime.substring(0, msTime.lastIndexOf("minute")-1) + " " + activity.getApplicationContext().getString(R.string.minago);
                                }
                                dsText.setText(activity.getApplicationContext().getString(R.string.active) + " " + msTime);
                                dsText.setTextColor(activity.getColor(R.color.Grey));
                                GradientDrawable gradientDrawable = (GradientDrawable) ResourcesCompat.getDrawable(activity.getApplicationContext().getResources(),
                                        R.drawable.offline_ic, null);
                                dsIcon.setBackground(gradientDrawable);
                            }

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

    private Long extractTime(String meas){
        meas = meas.replace('[', '\0');
        meas = meas.replace(']', '\0');
        return Long.parseLong(meas.split(",")[0].trim());
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
