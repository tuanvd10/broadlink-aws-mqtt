package com.viettel.vht.remoteapp.objects;

import android.util.Log;

import com.viettel.vht.remoteapp.common.ControlMode;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;

import java.util.UUID;

/**
 * Devices are remoted
 */
public class AirPurifier {
    static final String LOG_TAG = AirPurifier.class.getCanonicalName();

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

    public AirPurifier(PowerState powerState, SpeedState speedState, ControlMode controlMode) {
        this.power = powerState;
        this.speed = speedState;
        this.controlMode = controlMode;
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

    public void setPowerFromSpeed(SpeedState speed) {
        switch (speed) {
            case OFF:
                setPower(PowerState.OFF);
                break;
            case LOW:
            case MED:
            case HIGH:
                setPower(PowerState.ON);
                break;
            default:
                Log.e(LOG_TAG, "wrong speed from setPowerFromSpeed");
                break;
        }
    }
}
