package com.viettel.vht.remoteapp.objects;

import com.viettel.vht.remoteapp.common.ControlMode;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;

import java.util.UUID;

/**
 * Devices are remoted
 */
public class AirPurifier {
    private UUID id;
    private String name;
    private PowerState power;
    private SpeedState speed;
    private ControlMode controlMode;

//    public AirPurifier(PowerState power, SpeedState speed, ControlMode controlMode) {
//        this.power = power;
//        this.speed = speed;
//    }

    public AirPurifier() {
        this.power = PowerState.NULL;
        this.speed = SpeedState.NULL;
        this.controlMode = ControlMode.NULL;
    }

    // Is not null
    public boolean isNotNull() {
        if (power == PowerState.NULL || speed == SpeedState.NULL || controlMode == ControlMode.NULL) {
            return false;
        }

        return true;
    }

    // Getter and setter
    public PowerState getPower() {
        return power;
    }

    public void setPower(PowerState power) {
        this.power = power;
    }

    public SpeedState getSpeed() {
        return speed;
    }

    public void setSpeed(SpeedState speed) {
        this.speed = speed;
    }

    public ControlMode getControlMode() {
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;
    }
}
