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
import com.viettel.vht.remoteapp.common.KeyOfDevice;
import com.viettel.vht.remoteapp.common.KeyOfStates;
import com.viettel.vht.remoteapp.exceptions.DisconnectionException;
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;

import java.security.Key;

public class AirRemoteControl extends Fragment {
    private MainActivity parentActivity;

    private RemoteControlViewModel remoteControlViewModel;

    private final String LOG_TAG = RemoteControlFragment.class.getCanonicalName();
    String clientId;

    // Button
    private Button mPowerButton, mSpeedUpButton, mSpeedDownButton;

    // Text View
    private TextView mPowerState, mNameRemoteControl, mSpeedState;

    // Mqtt client
    private MqttClientToAWS mqttClient;

    // Dialog alert
    private Dialog mConnectDialog;

    // Sound button
    private MediaPlayer soundButton;
    // Vibrator when click button
    private Vibrator vibrator;
    // mProgressBar
    ProgressBar mPbLoadConnection;

    private String deviceId = null;


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
        mConnectDialog = new AlertDialog.Builder(parentActivity)
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

        // Get button from id and set on click listener
        // Power
        mPowerButton = root.findViewById(R.id.bt_power);
        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });
        // Speed up
        mSpeedUpButton = root.findViewById(R.id.bt_speed_up);
        mSpeedUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });

        // Speed down
        mSpeedDownButton = root.findViewById(R.id.bt_speed_down);
        mSpeedDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });


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
        // Power up button
        mPowerButton.setEnabled(false);
        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Speed up button
        mSpeedUpButton.setEnabled(false);
        mSpeedUpButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Speed down button
        mSpeedDownButton.setEnabled(false);
        mSpeedDownButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));

    }

    /**
     * Disable all button
     */
    private void enableRemoteButton() {
        // Power up button
        mPowerButton.setEnabled(true);
        mPowerButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Speed up button
        mSpeedUpButton.setEnabled(true);
        mSpeedUpButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Speed down button
        mSpeedDownButton.setEnabled(true);
        mSpeedDownButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
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
                case R.id.bt_speed_up:
                    speedUp();
                    break;
                case R.id.bt_speed_down:
                    speedDown();
                    break;
                default:
                    Log.e(LOG_TAG, "Cannot detect button id " + view.toString());
                    return;
            }
        } catch (DisconnectionException de) {
            mConnectDialog.show();
        }


    }

    private boolean speedUp() throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        String topic = AirPurifierTopics.SPEED;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO : send publish to get state after get condition
                removeStatesInMainActivity();
                requestStatesOfAirPurifier();
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

    private boolean speedDown() throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        String topic = AirPurifierTopics.SPEED;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO : send publish to get state after get condition
                removeStatesInMainActivity();
                requestStatesOfAirPurifier();
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
                // TODO : send publish to get state after get condition
                removeStatesInMainActivity();
                requestStatesOfAirPurifier();
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

    private void removeStatesInMainActivity() {
        parentActivity.getStateList().remove(KeyOfStates.POWER.getValue());
        parentActivity.getStateList().remove(KeyOfStates.SPEED.getValue());
    }

    private void requestStatesOfAirPurifier() throws InterruptedException {
        mqttClient.requestAllStatesOfDevice(parentActivity.getSmartPlugId());
    }

    private class InformationChecker extends Thread {
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
                            mConnectDialog.show();
                        }
                    });
                    // Get out
                    return;
                }

                String power = null, speed = null;
                // Get power
                while ((power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue())) == null) {
                    Thread.sleep(500);
                }

                // Get speed
                while ((speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue())) == null) {
                    Thread.sleep(500);
                }

                final String cPower = power, cSpeed = speed;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPowerState.setText(cPower);
                        // Disable button
                        if (cPower.equals(getString(R.string.state_power_off))) {
                            // Disable when off
                            mSpeedDownButton.setEnabled(false);
                            mSpeedDownButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
                            mSpeedUpButton.setEnabled(false);
                            mSpeedUpButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
                        } else if (cPower.equals(getString(R.string.state_power_on))) {
                            // Enable when on
                            mSpeedDownButton.setEnabled(true);
                            mSpeedDownButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
                            mSpeedUpButton.setEnabled(true);
                            mSpeedUpButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
                        }
                        mSpeedState.setText(cSpeed);
                    }
                });

                // Get device id
                while ((deviceId = parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()).getDeviceId()) == null) {
                    Thread.sleep(500);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Set progress bar
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableRemoteButton();
                    mPbLoadConnection.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
