package com.viettel.vht.remoteapp.remotecontrol;


import android.util.Log;

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

    public StateChecker (MqttClientToAWS mqttClient, AirPurifier expectedState, AirPurifier realState,
                         RemoteDevice remoteDevice) {
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
                // If have a change in
                if (expectedState.getControlMode() != realState.getControlMode() ||
                    realState.getPower() == PowerState.OFF ||
                    expectedState.getSpeed() != realState.getSpeed()) {
                    // Check remote control mode
                    if (expectedState.getControlMode() != realState.getControlMode()) {
                        boolean retCode = false;
                        if (expectedState.getControlMode() == ControlMode.AUTO) {
                            // auto
                            retCode = markRetry(DifferentType.CONTROL_MODE_AUTO);
                        } else {
                            // manual
                            retCode = markRetry(DifferentType.CONTROL_MODE_MANUAL);
                        }
                        // Check remote control
                        if (retCode) {
                            mqttClient.changeControlMode(expectedState.getControlMode());
                            // Wait to change state
                            Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                            // get state to check
                            mqttClient.requestGetCurrentControlMode();
                        }
                    } else if (realState.getPower() == PowerState.OFF) {
                        // Check expected state and real state
                        if (markRetry(DifferentType.POWER)) {
                            // Request turn on smart plug id
                            Log.i(LOG_TAG, "Turn on smart plug");
                            mqttClient.changeSmartPlugPower(remoteDevice.getSmartPlugId());
                            Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                            // Request change in remote state
                            mqttClient.requestPowerStateOfDevice(remoteDevice.getSmartPlugId());
                        }

                    } else if (expectedState.getSpeed() != realState.getSpeed()) {
                        // check speed
                        if (expectedState.getSpeed() == SpeedState.OFF) {
                            // expected state == 0
                            if (markRetry(DifferentType.SPEED_OFF)) {
                                // Turn off
                                Log.i(LOG_TAG, "Turn off air purifier");
                                mqttClient.changePowerOff(remoteDevice.getRemoteDeviceId());
                            }

                        } else {
                            // expected state > 0
                            if (realState.getSpeed() == SpeedState.OFF) {
                                if (markRetry(DifferentType.SPEED_ON)) {
                                    // Turn on
                                    Log.i(LOG_TAG, "Turn on air purifier");
                                    mqttClient.changePowerOn(remoteDevice.getRemoteDeviceId());
                                }

                            } else {
                                // change speed
                                switch(expectedState.getSpeed()) {
                                    case LOW:
                                        if (markRetry(DifferentType.SPEED_LOW)) {
                                            Log.i(LOG_TAG, "change speed to low");
                                            mqttClient.changeSpeedToLow(remoteDevice.getRemoteDeviceId());
                                        }
                                        break;
                                    case MED:
                                        if (markRetry(DifferentType.SPEED_MED)) {
                                            Log.i(LOG_TAG, "change speed to med");
                                            mqttClient.changeSpeedToMed(remoteDevice.getRemoteDeviceId());
                                        }

                                        break;
                                    case HIGH:
                                        if (markRetry(DifferentType.SPEED_HIGH)) {
                                            Log.i(LOG_TAG, "change speed to high");
                                            mqttClient.changeSpeedToHigh(remoteDevice.getRemoteDeviceId());
                                        }
                                        break;
                                    default:
                                        Log.e(LOG_TAG, "wrong expected speed : " + expectedState.getSpeed().getValue());
                                        break;
                                }
                            }
                        }
                        // Wait state to change
                        Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                        // Request state
                        mqttClient.requestAllStatesOfDevice(remoteDevice.getSmartPlugId());
                    }

                } else {
                    // Nothing to change
                    if (isChanging) {
                        removeRetry();
                    }
                    // Check on screen or not
                }

                // Wait to state change if it has change in start of loop
                Thread.sleep(Constants.WAIT_NEXT_LOOP);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Function is called every time expected state have differences with real state
     * @param diffType
     */
    private boolean markRetry(DifferentType diffType) {
        if (isChanging) {
            Log.d(LOG_TAG, "Retry in " + count + ". Type param = " + diffType.name() + ", Type object = " + this.diffType.name());

            // Check diff type
            if (this.diffType != diffType) {
                count = 0;
                this.diffType = diffType;
            } else {
                count++;
            }

            // Check diff type
            if (count == Constants.MAX_TRY_REQUEST) {
                // Inform to user and reset state of expected value
                Log.d(LOG_TAG, "Cannot change value, reset expected value");
                expectedState.setSpeed(realState.getSpeed());
                expectedState.setPower(realState.getPower());
                expectedState.setControlMode(realState.getControlMode());
                // Remove retry
                removeRetry();
                // Just 1 retry
                return false;
            }

        } else {
            Log.d(LOG_TAG, "Start retry");
            isChanging = true;
            count = 0;
            this.diffType = diffType;
        }

        return true;
    }

    /**
     * remove "retry" mark
     */
    private void removeRetry() {
        isChanging = false;
        count = 0;
        diffType = DifferentType.NOTHING;
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

    private boolean checkCompleteRequest(DifferentType diffType) {
        boolean retVal = false;
        switch (diffType) {
            case SPEED_LOW:
            case SPEED_MED:
            case SPEED_HIGH:
            case SPEED_ON:
            case SPEED_OFF:
                if (realState.getSpeed() == expectedState.getSpeed()) {
                    retVal = true;
                }
                Log.d(LOG_TAG, "Check complete speed");
                break;
            case POWER:
                if (expectedState.getPower() == realState.getPower()) {
                    retVal = true;
                }
                Log.d(LOG_TAG, "Check complete power");
                break;
            case CONTROL_MODE_AUTO:
            case CONTROL_MODE_MANUAL:
                if (expectedState.getControlMode() == realState.getControlMode()) {
                    retVal = true;
                }
                break;
            default:
                Log.d(LOG_TAG, "Error in different type in check complete request");
                break;
        }
        // return value
        return retVal;
    }
}
