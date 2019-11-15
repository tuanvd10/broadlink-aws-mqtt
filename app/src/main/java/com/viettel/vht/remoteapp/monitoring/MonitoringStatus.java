package com.viettel.vht.remoteapp.monitoring;


public class MonitoringStatus {
    private String data_point_id;
    private String name;
    private String unit;
    private String value;
    private AirQualityLevel qualityLevel;
    private double poorThreshold[];
    private double goodThreshold[];
    private String iconName;
    boolean isDisplay;

    public MonitoringStatus(String data_point_id, String name, String unit, String iconName) {
        this.data_point_id = data_point_id;
        this.name = name;
        this.unit = unit;
        this.iconName = iconName;
        qualityLevel = AirQualityLevel.OFFLINE;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplay(boolean display) {
        isDisplay = display;
    }

    public boolean isDisplay() {
        return isDisplay;
    }

    public String getDataPointID() {
        return data_point_id;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public AirQualityLevel getQualityLevel() {
        return qualityLevel;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }

    public String getIconName() {
        return iconName;
    }

    public void setQualityLevel(AirQualityLevel qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    //    private void calculateQualityLevel() {
//        double doubleValue = Double.parseDouble(getValue());
//        if (doubleValue <= qualityThreshold[0])
//            qualityLevel = AirQualityLevel.POOR;
//        else if (doubleValue > qualityThreshold[0] && doubleValue <= qualityThreshold[1])
//            qualityLevel = AirQualityLevel.MODERATE;
//        else if (doubleValue > qualityThreshold[1])
//            qualityLevel = AirQualityLevel.GOOD;
//        else
//            qualityLevel = AirQualityLevel.OFFLINE;
//
//        if (qualityThreshold[0] == 0)
//            qualityLevel = AirQualityLevel.OFFLINE;
//    }
}
