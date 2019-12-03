package com.viettel.vht.remoteapp.remotecontrol;


import android.util.Log;

import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.ControlMode;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;
import com.viettel.vht.remoteapp.objects.AirPurifier;
import com.viettel.vht.remoteapp.objects.RemoteDevice;
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;

public class StateChecker extends Thread {
    private final String LOG_TAG = StateChecker.class.getCanonicalName();
    private AirPurifier expectedState;
    private AirPurifier realState;
    private MqttClientToAWS mqttClient;
    private RemoteDevice remoteDevice;
    private final int MAX_RETRY = 3;

    public StateChecker (MqttClientToAWS mqttClient, AirPurifier expectedState, AirPurifier realState, RemoteDevice remoteDevice) {
        this.mqttClient = mqttClient;
        this.expectedState = expectedState;
        this.realState = realState;
        this.remoteDevice = remoteDevice;
    }

    private boolean flag = true;
    private boolean isChanging = false;
    private DifferentType diffType = DifferentType.NOTHING;
    private int count = 0;

    @Override
    public void run() {

        while (flag) {
            try {
//                if (isChanging) {
//                    retry();
//                }
                // Check expected state and real state
                if (realState.getPower() == PowerState.OFF) {
                    // Request turn on smart plug id
                    // Turn off speed
                    Log.i(LOG_TAG, "Turn on smart plug");
                    mqttClient.changeSmartPlugPower(remoteDevice.getSmartPlugId());
                    Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                    // Request change in remote state
                    mqttClient.requestPowerStateOfDevice(remoteDevice.getSmartPlugId());
                    markRetry(DifferentType.POWER);

                } else {
                    if (expectedState.getSpeed() != realState.getSpeed()) {
                        // Check remote control mode
                        if (expectedState.getControlMode() != realState.getControlMode()) {
                            // Check remote control
                            mqttClient.changeControlMode(expectedState.getControlMode());
                            if (expectedState.getControlMode() == ControlMode.AUTO) {
                                // auto
                                markRetry(DifferentType.CONTROL_MODE_AUTO);
                            } else {
                                // manual
                                markRetry(DifferentType.CONTROL_MODE_MANUAL);
                            }
                        }

                        if (expectedState.getSpeed() == SpeedState.OFF) {
                            // expected state == 0
                            // Turn off
                            Log.i(LOG_TAG, "Turn off air purifier");
                            mqttClient.changePower(remoteDevice.getRemoteDeviceId());
                            markRetry(DifferentType.SPEED_OFF);
                        } else {
                            // expected state > 0
                            if (realState.getSpeed() == SpeedState.OFF) {
                                // Turn on
                                Log.i(LOG_TAG, "Turn on air purifier");
                                mqttClient.changePower(remoteDevice.getRemoteDeviceId());
                                markRetry(DifferentType.SPEED_ON);
                            } else {
                                // change speed
                                switch(expectedState.getSpeed()) {
                                    case LOW:
                                        Log.i(LOG_TAG, "change speed to low");
                                        mqttClient.changeSpeedToLow(remoteDevice.getRemoteDeviceId());
                                        markRetry(DifferentType.SPEED_LOW);
                                        break;
                                    case MED:
                                        Log.i(LOG_TAG, "change speed to med");
                                        mqttClient.changeSpeedToMed(remoteDevice.getRemoteDeviceId());
                                        markRetry(DifferentType.SPEED_MED);
                                        break;
                                    case HIGH:
                                        Log.i(LOG_TAG, "change speed to high");
                                        mqttClient.changeSpeedToHigh(remoteDevice.getRemoteDeviceId());
                                        markRetry(DifferentType.SPEED_HIGH);
                                        break;
                                    default:
                                        Log.e(LOG_TAG, "wrong expected speed : " + expectedState.getSpeed().getValue());
                                        break;
                                }
                            }
                        }
                        // Wait to change
                        sleepToWaitChange();
                    } else {
                        // Nothing to change
                        if (isChanging) {
                            removeRetry();
                        }

                        // Check on screen or not

                    }
                }

                // Wait until next state check
                Thread.sleep(Constants.WAIT_NEXT_LOOP);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void retry() {
        // Check change value
        Log.d(LOG_TAG, "one of values is changing. retry");
        switch (diffType) {
            case SPEED_LOW:
                if (realState.getSpeed() != SpeedState.LOW) {
                    mqttClient.changeSpeedToLow(remoteDevice.getRemoteDeviceId());
                } else {
                    removeRetry();
                }
                break;
            case SPEED_MED:
                if (realState.getSpeed() != SpeedState.MED) {
                    mqttClient.changeSpeedToMed(remoteDevice.getRemoteDeviceId());
                } else {
                    removeRetry();
                }
                break;
            case SPEED_HIGH:
                if (realState.getSpeed() != SpeedState.HIGH) {
                    mqttClient.changeSpeedToHigh(remoteDevice.getRemoteDeviceId());
                } else {
                    removeRetry();
                }
                break;
            case SPEED_OFF:
                if (realState.getSpeed() != SpeedState.OFF) {
                    mqttClient.changePower(remoteDevice.getRemoteDeviceId());
                } else {
                    removeRetry();
                }
                break;
            case SPEED_ON:
                if (realState.getSpeed() == SpeedState.OFF) {
                    mqttClient.changePower(remoteDevice.getRemoteDeviceId());
                } else {
                    removeRetry();
                }
                break;
            case POWER:
                if (realState.getPower() == PowerState.OFF) {
                    mqttClient.changeSmartPlugPower(remoteDevice.getSmartPlugId());
                } else {
                    removeRetry();
                }
                break;
            case CONTROL_MODE_AUTO:
                if (realState.getControlMode() != ControlMode.AUTO) {
                    mqttClient.changeControlMode(ControlMode.AUTO);
                } else {
                    removeRetry();
                }
                break;
            case CONTROL_MODE_MANUAL:
                if (realState.getControlMode() != ControlMode.MANUAL) {
                    mqttClient.changeControlMode(ControlMode.MANUAL);
                } else {
                    removeRetry();
                }
                break;
            default:
                Log.d(LOG_TAG, "Error in different type");
                break;
        }
        // Increase value retry
        count++;
        // Check maximum number retry
        if (count == MAX_RETRY) {
            removeRetry();
        }
    }

    /**
     * Function is called every time expected state have differences with real state
     * @param diffType
     */
    private void markRetry(DifferentType diffType) {
        if (isChanging) {
            Log.d(LOG_TAG, "Retry in " + count + " with type " + diffType.name());
            if (this.diffType == diffType) {
                // increase number retry
                count++;
                if (count == MAX_RETRY) {
                    // Inform to user and reset state of expected value
                    switch (diffType) {
                        case SPEED_LOW:
                        case SPEED_MED:
                        case SPEED_HIGH:
                        case SPEED_OFF:
                        case SPEED_ON:
                            expectedState.setSpeed(realState.getSpeed());
                            // Change in ui
                            break;
                        case POWER:
                            expectedState.setPower(realState.getPower());
                            // Change in ui
                            break;
                        case CONTROL_MODE_AUTO:
                        case CONTROL_MODE_MANUAL:
                            expectedState.setControlMode(realState.getControlMode());
                            // Change in ui
                            break;
                        default:
                            Log.d(LOG_TAG, "Error in different type");
                            break;
                    }
                }

            } else {
                // another type request

            }
        } else {
            Log.d(LOG_TAG, "Start retry");
            isChanging = true;
            count = 0;
            this.diffType = diffType;
        }
    }

    /**
     * remove "retry" mark
     */
    private void removeRetry() {
        isChanging = false;
        count = 0;
        diffType = DifferentType.NOTHING;
    }

    private void sleepToWaitChange() throws InterruptedException {
        Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
        mqttClient.requestAllStatesOfDevice(remoteDevice.getSmartPlugId());
    }


    /**
     * Different type: save vale of different
     */
    private enum DifferentType {
        SPEED_LOW,                  // Real speed must be low
        SPEED_MED,                  // Real speed must be med
        SPEED_HIGH,                 // Real speed must be high
        SPEED_OFF,                  // Real speed must be off
        SPEED_ON,                   // Real speed must not be off
        POWER,                      // Real power must be on
        CONTROL_MODE_AUTO,          // Real control mode must be auto
        CONTROL_MODE_MANUAL,        // Real control mode must be manual
        NOTHING;                    // Nothing to change
    }
}
