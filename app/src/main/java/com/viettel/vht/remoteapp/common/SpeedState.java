package com.viettel.vht.remoteapp.common;

public enum SpeedState {
    LOW(1),
    MED(2),
    HIGH(3),
    OFF(0),
    NULL(-1);

    private int value;

    private SpeedState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
