package com.viettel.vht.remoteapp.objects;

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

    public AirPurifier(PowerState power, SpeedState speed) {
        this.power = power;
        this.speed = speed;
    }

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
}
