package com.viettel.vht.remoteapp.remotecontrol;


import android.util.Log;

import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;
import com.viettel.vht.remoteapp.objects.AirPurifier;
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;

public class StateChecker extends Thread {
    private final String LOG_TAG = StateChecker.class.getCanonicalName();
    private AirPurifier expectedState;
    private AirPurifier realState;
    private MqttClientToAWS mqttClient;
    private String remoteDeviceId;

    public StateChecker (MqttClientToAWS mqttClient, AirPurifier expectedState, AirPurifier realState, String remoteDeviceId) {
        this.mqttClient = mqttClient;
        this.expectedState = expectedState;
        this.realState = realState;
        this.remoteDeviceId = remoteDeviceId;
    }

    @Override
    public void run() {
        boolean flag = true;

        while (flag) {
            try {
                // Check expected state and real state
                if (realState.getPower() == PowerState.OFF) {
                    // request turn on smart plug id
                    // turn off speed


                } else {
                    if (expectedState.getSpeed() != realState.getSpeed()) {
                        if (expectedState.getSpeed() == SpeedState.OFF) {
                            // expected state == 0
                            // Turn off
                            mqttClient.changePower(remoteDeviceId);
                        } else {
                            // expected state > 0
                            if (realState.getSpeed() == SpeedState.OFF) {
                                // Turn on
                                mqttClient.changePower(remoteDeviceId);
                            } else {
                                // change speed
                                switch(expectedState.getSpeed()) {
                                    case LOW:
                                        mqttClient.changeSpeedToLow(remoteDeviceId);
                                        break;
                                    case MED:
                                        mqttClient.changeSpeedToMed(remoteDeviceId);
                                        break;
                                    case HIGH:
                                        mqttClient.changeSpeedToHigh(remoteDeviceId);
                                        break;
                                    default:
//
                                        break;
                                }
                            }
                        }
                        Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                    }
                }

                // Wait until next state check
                Thread.sleep(Constants.SLEEP_WAIT);
            } catch (InterruptedException ie) {
                ie.printStackTrace();;
            }


        }
    }
}
