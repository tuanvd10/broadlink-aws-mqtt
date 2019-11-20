package com.viettel.vht.remoteapp.common;

public enum KeyOfStates {
    POWER("POWER"),
    SPEED("SPEED");

    private String value;

    KeyOfStates(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
