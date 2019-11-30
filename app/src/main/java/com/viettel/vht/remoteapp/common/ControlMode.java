package com.viettel.vht.remoteapp.common;

public enum ControlMode {
    AUTO ("auto"),
    MANUAL ("manual"),
    NULL (null);

    private String value;
    private ControlMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
