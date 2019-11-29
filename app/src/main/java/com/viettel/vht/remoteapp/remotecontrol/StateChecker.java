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
    private String smartPlugId;

    public StateChecker (MqttClientToAWS mqttClient, AirPurifier expectedState, AirPurifier realState, String remoteDeviceId, String smartPlugId) {
        this.mqttClient = mqttClient;
        this.expectedState = expectedState;
        this.realState = realState;
        this.remoteDeviceId = remoteDeviceId;
        this.smartPlugId = smartPlugId;
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
                    Log.i(LOG_TAG, "Turn on smart plug");
                    mqttClient.changeSmartPlugPower(smartPlugId);
                    Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);

                } else {
                    if (expectedState.getSpeed() != realState.getSpeed()) {
                        if (expectedState.getSpeed() == SpeedState.OFF) {
                            // expected state == 0
                            // Turn off
                            Log.i(LOG_TAG, "Turn off air purifier");
                            mqttClient.changePower(remoteDeviceId);
                        } else {
                            // expected state > 0
                            if (realState.getSpeed() == SpeedState.OFF) {
                                // Turn on
                                Log.i(LOG_TAG, "Turn on air purifier");
                                mqttClient.changePower(remoteDeviceId);
                            } else {
                                // change speed
                                switch(expectedState.getSpeed()) {
                                    case LOW:
                                        Log.i(LOG_TAG, "change speed to low");
                                        mqttClient.changeSpeedToLow(remoteDeviceId);
                                        break;
                                    case MED:
                                        Log.i(LOG_TAG, "change speed to med");
                                        mqttClient.changeSpeedToMed(remoteDeviceId);
                                        break;
                                    case HIGH:
                                        Log.i(LOG_TAG, "change speed to high");
                                        mqttClient.changeSpeedToHigh(remoteDeviceId);
                                        break;
                                    default:
                                        Log.e(LOG_TAG, "wrong expected speed : " + expectedState.getSpeed().getValue());
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
