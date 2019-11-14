package com.viettel.vht.remoteapp.common;

public enum MitsubishiFanTopics {
    POWER("broadlink/fan/mitsubishi/power"),
    SPEED("broadlink/fan/mitsubishi/speed"),
    SWING("broadlink/fan/mitsubishi/swing"),
    TIMER("broadlink/fan/mitsubishi/timer"),
    RHYTHM("broadlink/fan/mitsubishi/rhythm");

    private String value;

    private MitsubishiFanTopics(String value) {
        this.value = value;
    }

    public String getvalue() {
        return value;
    }
}
