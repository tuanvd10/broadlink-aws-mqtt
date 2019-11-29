package com.viettel.vht.remoteapp.objects;

import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;

public class RemoteDevice {
    private String name;
    private String deviceId;

    public RemoteDevice() {

    }

    public RemoteDevice(String name, String deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
