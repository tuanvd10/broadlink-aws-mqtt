package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.viettel.vht.remoteapp.MainActivity;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.AirPurifierTopics;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.KeyOfDevice;
import com.viettel.vht.remoteapp.common.KeyOfStates;
import com.viettel.vht.remoteapp.exceptions.DisconnectionException;
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;

public class AirRemoteControl extends Fragment {
    private MainActivity parentActivity;

    private RemoteControlViewModel remoteControlViewModel;

    private final String LOG_TAG = RemoteControlFragment.class.getCanonicalName();
    String clientId;

    // Button
    private Button mPowerButton, mUVButton;
    // Radio group
    private RadioGroup mSpeedRadioGroup;

    // Text View
    private TextView mPowerState, mNameRemoteControl, mSpeedState, tvPowerButton, tvUVButton;

    // Mqtt client
    private MqttClientToAWS mqttClient;

    // Dialog alert
    private Dialog mCloudConnectionDialog, mServerConnectionDialog, mNotFoundDeviceIdDialog;

    // Sound button
    private MediaPlayer soundButton;
    // Vibrator when click button
    private Vibrator vibrator;
    // mProgressBar
    ProgressBar mPbLoadConnection;

    private String deviceId = null;
    private String smartPlugId = null;
    // On click listener for button
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clickRemoteButton(v);
        }
    };

    // On checked change onCheckedChangeListener
    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            try {
                switch (checkedId) {
                    case R.id.rb_speed_low:
                        changeInSpeed(AirPurifierTopics.LOW_SPEED);
                        break;
                    case R.id.rb_speed_med:
                        changeInSpeed(AirPurifierTopics.MED_SPEED);
                        break;
                    case R.id.rb_speed_high:
                        changeInSpeed(AirPurifierTopics.HIGH_SPEED);
                        break;
                }
            } catch (DisconnectionException de) {
                de.printStackTrace();
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(this.getClass().getName(), "Create view");
        remoteControlViewModel = ViewModelProviders.of(this).get(RemoteControlViewModel.class);

        // Get parent activity
        parentActivity = (MainActivity) getActivity();
        // set mqtt client
        Log.d(LOG_TAG, "Make mqtt client");
        mqttClient = parentActivity.getMqttClient();

        View root = inflater.inflate(R.layout.fragment_air_remote_control, container, false);
        // Set name of remote control
        mNameRemoteControl = root.findViewById(R.id.tv_remote_control);
        mNameRemoteControl.setText(R.string.air_purifier);

        // Get status of remote control
        mPowerState = root.findViewById(R.id.tv_power_state);
        mSpeedState = root.findViewById(R.id.tv_speed_state);

        // Set remote control view model
        remoteControlViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(LOG_TAG, "Have a change in remote control view model");
            }
        });

        // Get dialog
        mCloudConnectionDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.title_lost_connection)
                .setMessage(R.string.msg_lost_connection)
                .setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disableRemoteButton();
                        mPbLoadConnection.setVisibility(View.VISIBLE);
                        mqttClient.setConnected(false);
                        mqttClient.setConnecting(true);
                        mqttClient.makeConnectionToServer();
                        // Start a new information checker
                        new AirRemoteControl.InformationChecker().start();
                    }
                })
                .setNegativeButton(R.string.bt_pass, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disableRemoteButton();
                    }
                })
                .create();

        // Dialog will show when connection to aws isn't established
        mServerConnectionDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.info)
                .setMessage(R.string.msg_no_response_connection)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPowerState.setText(R.string.title_lost_connection);
                        disableRemoteButton();
                    }
                }).setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disableRemoteButton();
                        mPowerState.setText(R.string.title_lost_connection);
                        mPbLoadConnection.setVisibility(View.VISIBLE);
                        mqttClient.setConnected(false);
                        mqttClient.setConnecting(true);
                        mqttClient.makeConnectionToServer();
                        // Start a new information checker
                        new AirRemoteControl.InformationChecker().start();
                    }
                })
                .create();

        // Dialog will shown when app cannot found deviceid in response message
        mNotFoundDeviceIdDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.info)
                .setMessage(R.string.error_not_found_device_id)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPowerState.setText(R.string.title_lost_connection);
                        disableRemoteButton();
                    }
                }).create();
        // Get button from id and set on click onCheckedChangeListener
        // Power
        mPowerButton = root.findViewById(R.id.bt_power);
        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });
        // Get radio group button
        mSpeedRadioGroup = root.findViewById(R.id.rg_speed);
        mSpeedRadioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        // Get UV button
        mUVButton = root.findViewById(R.id.bt_uv);
        mUVButton.setOnClickListener(onClickListener);
        // Get TextView for power button
        tvPowerButton = root.findViewById(R.id.tv_power);
        // Get TextView for UV button
        tvUVButton = root.findViewById(R.id.tv_uv);
        // Get sound button
        soundButton = MediaPlayer.create(parentActivity, R.raw.sample_2);
        // Get vibrate
        vibrator = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Get progress bar
        mPbLoadConnection = root.findViewById(R.id.pb_load_connection);
        // New thread for check
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableRemoteButton();
                mPbLoadConnection.setVisibility(View.VISIBLE);
            }
        });
        new AirRemoteControl.InformationChecker().start();
        // Return view
        return root;
    }

    /**
     * Disable all button on remote control
     */
    private void disableRemoteButton() {
        disablePowerButton();
        disableSpeedRadioGroup();
        disableUVButton();
    }

    /**
     * Enable all button
     */
    private void enableRemoteButton() {
        enablePowerButton();
        enableSpeedRadioGroup();
        enableUVButton();
    }

    /**
     * Enable power button
     */
    private void enablePowerButton() {
        // Power up button
        mPowerButton.setEnabled(true);
    }

    /**
     * enable speed radio button
     */
    private void enableSpeedRadioGroup() {
        // radio group
        mSpeedRadioGroup.setEnabled(true);
        // Loop to enable all radio button
        int length = mSpeedRadioGroup.getChildCount();
        for (int i = 0; i < length; i++) {
            ((RadioButton) mSpeedRadioGroup.getChildAt(i)).setEnabled(true);
        }
    }

    /**
     * disable speed radio button
     */
    private void disableSpeedRadioGroup() {
        mSpeedRadioGroup.setEnabled(false);
        // Loop to disable all radio button
        int length = mSpeedRadioGroup.getChildCount();
        for (int i = 0; i < length; i++) {
            ((RadioButton) mSpeedRadioGroup.getChildAt(i)).setEnabled(false);
        }
    }

    /**
     * Enable uv button
     */
    private void enableUVButton() {
        mUVButton.setEnabled(true);
    }

    /**
     * disable uv button
     */
    private void disableUVButton() {
        mUVButton.setEnabled(false);

    }

    public void clickRemoteButton(View view) {
        String msg = "";
        msg = "play-" + deviceId;
        String topic = null;
        // Sound and vibrator
        soundButton.start();
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(200);
        }

        // Check button
        try {
            switch(view.getId()) {
                case R.id.bt_power :
                    power();
                    break;
                case R.id.bt_uv:
                    uv();
                    break;
                default:
                    Log.e(LOG_TAG, "Cannot detect button id " + view.toString());
                    return;
            }
        } catch (DisconnectionException de) {
            mCloudConnectionDialog.show();
        }


    }

//    private boolean speedUp() throws DisconnectionException {
//        boolean retVal = true;
//        String message = "play-" + deviceId;
//        String topic = AirPurifierTopics.SPEED;
//        // Publish message to aws server
//        try {
//            if (mqttClient.isConnected()) {
//                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
//                removeSpeedStateInMainActivity();
//                mqttClient.requestSpeedStateOfDevice(parentActivity.getSmartPlugId());
//                // Wait new information checker
//                new InformationChecker().start();
//            } else {
//                throw new DisconnectionException();
//            }
//        } catch (AmazonClientException ace) {
//            retVal = false;
//            ace.printStackTrace();
//        } catch (InterruptedException ie) {
//            retVal = false;
//            Log.e(LOG_TAG, "An InterruptedException!");
//            ie.printStackTrace();
//        }
//
//        return retVal;
//    }

    private boolean changeInSpeed(String topic) throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO : send publish to get state after get condition
                removeSpeedStateInMainActivity();
                mqttClient.requestSpeedStateOfDevice(parentActivity.getSmartPlugId());
                // Wait new information checker
                new InformationChecker().start();
            } else {
                throw new DisconnectionException();
            }
        } catch (AmazonClientException ace) {
            retVal = false;
            ace.printStackTrace();
        } catch (InterruptedException ie) {
            retVal = false;
            Log.e(LOG_TAG, "An InterruptedException!");
            ie.printStackTrace();
        }

        return retVal;
    }


    private boolean power() throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        String topic = AirPurifierTopics.POWER;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO change text view in button
                final boolean willPowerOn;
                if (tvPowerButton.getText().equals(getString(R.string.state_power_on))) {
                    willPowerOn = false;
                } else {
                    willPowerOn = true;
                }
                // TODO check state from server
                removePowerStateInMainActivity();
                mqttClient.requestAllStatesOfDevice(parentActivity.getSmartPlugId());
                // Change value in screen
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (willPowerOn) {
                            tvPowerButton.setText(getString(R.string.power_on));
                            enableUVButton();
                            enableSpeedRadioGroup();
                        } else {
                            tvPowerButton.setText(getString(R.string.power_off));
                            disableUVButton();
                            disableSpeedRadioGroup();
                        }

                    }
                });
                // Wait new information checker
                new InformationChecker().start();
            } else {
                throw new DisconnectionException();
            }
        } catch (AmazonClientException ace) {
            retVal = false;
            ace.printStackTrace();
        } catch (InterruptedException ie) {
            retVal = false;
            Log.e(LOG_TAG, "An InterruptedException!");
            ie.printStackTrace();
        }

        return retVal;
    }

    private boolean uv() throws DisconnectionException {
        // TODO process for uv button
        boolean retVal = true;
        String message = "play-" + deviceId;
        String topic = AirPurifierTopics.UV;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO change text view in button
                final boolean willPowerOn;
                if (tvUVButton.getText().equals(getString(R.string.uv_off))) {
                    willPowerOn = true;
                } else {
                    willPowerOn = false;
                }
                // TODO check state from server
                // Change value in screen
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (willPowerOn) {
                            tvUVButton.setText(getString(R.string.uv_on));
                        } else {
                            tvUVButton.setText(getString(R.string.uv_off));
                        }

                    }
                });
                // Wait new information checker
//                new InformationChecker().start();
            } else {
                throw new DisconnectionException();
            }
        } catch (AmazonClientException ace) {
            retVal = false;
            ace.printStackTrace();
        }
//        catch (InterruptedException ie) {
//            retVal = false;
//            Log.e(LOG_TAG, "An InterruptedException!");
//            ie.printStackTrace();
//        }
        return retVal;
    }

    /**
     * Remove all state is stored in main activity
     */
    private void removeStatesInMainActivity() {
        removePowerStateInMainActivity();
        removeSpeedStateInMainActivity();
    }

    /**
     * remove speed state in main activity
     */
    private void removeSpeedStateInMainActivity() {
        parentActivity.getStateList().remove(KeyOfStates.SPEED.getValue());
    }

    /**
     * remove power state in main activity
     */
    private void removePowerStateInMainActivity() {
        parentActivity.getStateList().remove(KeyOfStates.POWER.getValue());
    }

    /**
     * Request to get all states in air purifier
     * @throws InterruptedException
     */
    private void requestStatesOfAirPurifier() throws InterruptedException {
        mqttClient.requestAllStatesOfDevice(parentActivity.getSmartPlugId());
    }



    private void disablePowerButton() {
        mPowerButton.setEnabled(false);
//        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
    }

    /**
     * Class collects information from list state in main activity
     */
    private class InformationChecker extends Thread {

        private final int loopNumber = 3;
        private final int sleepTime = 200;

        private String checkPowerState() throws InterruptedException {
            String power = null;
            for (int i = 0; i < loopNumber; i++) {
                if ((power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue())) == null) {
                    Thread.sleep(sleepTime);
                } else {
                    break;
                }
            }

            return power;
        }

        private String checkSpeedState() throws InterruptedException {
            String speed = null;
            for (int i = 0; i < loopNumber; i++) {
                if ((speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue())) == null) {
                    Thread.sleep(sleepTime);
                } else {
                    break;
                }
            }

            return speed;
        }

        /**
         * Check device id and smart plug id
         * @return
         * @throws InterruptedException
         */
        private boolean checkRemoteDevice() throws InterruptedException {
            // Get device id
            boolean isHaveDevice = false;
            for (int i = 0; i < loopNumber; i++) {

                if (parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()) == null
                        || parentActivity.getDeviceList().get(KeyOfDevice.SMART_PLUG) == null) {
                    Thread.sleep(sleepTime);
                } else {
                    deviceId = parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()).getDeviceId();
                    smartPlugId = parentActivity.getDeviceList().get(KeyOfDevice.SMART_PLUG.getValue()).getDeviceId();
                    isHaveDevice = true;
                    break;
                }
            }
            return isHaveDevice;
        }


        @Override
        public void run() {

            try {
                while (mqttClient.isConnecting()) {
                    Thread.sleep(500);
                }


                if (!mqttClient.isConnected()) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCloudConnectionDialog.show();
                        }
                    });
                    // Get out
                    return;
                }

                // Get deviceId and smart plug id
                if (!checkRemoteDevice()) {
                    // TODO process if don't have device Id
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mNotFoundDeviceIdDialog.show();
                        }
                    });
                    // Get out
                    return;
                }

                // Get power
                final String power = checkPowerState();
                // Get speed
                final String speed = checkSpeedState();

                // Set progress bar
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // check value
                        if (power == null || speed == null) {
                            mPowerState.setText(R.string.error);
                            mServerConnectionDialog.show();
                            // Get out this
                            return;
                        }
                        // Set speed and state
                        mPowerState.setText(power);
                        mSpeedState.setText(speed);
                        // Disable button
                        enableRemoteButton();
                        if (power.equals(getString(R.string.state_power_off))) {
                            // Disable when off
                            disableUVButton();
                            disableSpeedRadioGroup();
                        }
                    }
                });

            } catch (Exception ex) {
                // disable remote button
                disableRemoteButton();
                ex.printStackTrace();
            } finally {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPbLoadConnection.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    /**
     * task: collects information from MainActivity
     */
//    private Runnable infomationChecker = new Runnable() {
//        private final int loopNumber = 6;
//
//        private String checkPowerState() throws InterruptedException {
//            String power = null;
//            for (int i = 0; i < loopNumber; i++) {
//                if ((power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue())) == null) {
//                    Thread.sleep(500);
//                } else {
//                    break;
//                }
//            }
//
//            return power;
//        }
//
//        private String checkSpeedState() throws InterruptedException {
//            String speed = null;
//            for (int i = 0; i < loopNumber; i++) {
//                if ((speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue())) == null) {
//                    Thread.sleep(500);
//                } else {
//                    break;
//                }
//            }
//
//            return speed;
//        }
//
//        @Override
//        public void run() {
//            // check connection, device id, client id, speed and power state
//            try {
//                while (mqttClient.isConnecting()) {
//                    Thread.sleep(500);
//                }
//
//
//                if (!mqttClient.isConnected()) {
//                    ThreadUtils.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mCloudConnectionDialog.show();
//                        }
//                    });
//                    // Get out
//                    return;
//                }
//
//                // Get device id
//                while ((deviceId = parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()).getDeviceId()) == null) {
//                    Thread.sleep(500);
//                }
//
//                // Get power
//                final String power = checkPowerState();
//                // Get speed
//                final String speed = checkSpeedState();
//
//                // Set progress bar
//                ThreadUtils.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        // check value
//                        if (power == null || speed == null) {
//                            mServerConnectionDialog.show();
//                            // Get out this
//                            return;
//                        }
//                        // Set speed and state
//                        mPowerState.setText(power);
//                        mSpeedState.setText(speed);
//                        // Disable button
//                        enableRemoteButton();
//                        if (power.equals(getString(R.string.state_power_off))) {
//                            // Disable when off
//
//                        } else {
//                            int nSpeed = Integer.parseInt(speed);
//                            // check speed
//                            if (nSpeed == Constants.MAX_AIR_PURIFIER_SPEED) {
//
//                            } else if (nSpeed == Constants.MIN_AIR_PURIFIER_SPEED) {
//
//                            }
//                        }
//                    }
//                });
//
//            } catch (Exception ex) {
//                // disable remote button
//                disableRemoteButton();
//                ex.printStackTrace();
//            } finally {
//                ThreadUtils.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPbLoadConnection.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//        }
//    };
}
