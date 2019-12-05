package com.viettel.vht.remoteapp.common;

public enum KeyOfDevice {
    REMOTE ("RM"),
    SMART_PLUG ("SP");

    private String value;

    KeyOfDevice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
