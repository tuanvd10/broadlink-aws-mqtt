package com.viettel.vht.remoteapp.monitoring;


public class MonitoringStatus {
    private String data_point_id;
    private String name;
    private String unit;
    private String value;
    private AirQualityLevel qualityLevel;
    private String iconName;

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


    public String getDataPointID() {
        return data_point_id;
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


}
