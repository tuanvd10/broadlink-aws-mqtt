package com.viettel.vht.remoteapp.common;

public enum PowerState {
    ON ("ON"),
    OFF ("OFF"),
    NULL (null);

    private String value;
    private PowerState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
