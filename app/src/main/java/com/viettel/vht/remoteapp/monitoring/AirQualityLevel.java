package com.viettel.vht.remoteapp.monitoring;

public enum AirQualityLevel {
    GOOD, MODERATE, POOR, OFFLINE;

    @Override
    public String toString() {
        switch (this) {
            case GOOD:
                return "Tốt";
            case MODERATE:
                return "Khá";
            case POOR:
                return "Kém";
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

    public String toCycle(){
        switch (this) {
            case GOOD:
                return "good_aq_cycle";
            case MODERATE:
                return "mod_aq_cycle";
            case POOR:
                return "poor_aq_cycle";
            case OFFLINE:
                return "good_aq_cycle";
            default:
                return "good_aq_cycle";
        }
    }
}