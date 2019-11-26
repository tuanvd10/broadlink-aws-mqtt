package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.app.Activity;
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
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;



public class RemoteControlFragment extends Fragment {
    private MainActivity parentActivity;

    private RemoteControlViewModel remoteControlViewModel;

    private final String LOG_TAG = RemoteControlFragment.class.getCanonicalName();
    String clientId;

    // Button
    private Button mPowerButton, mSwingButton, mTimerButton, mRhythmButton, mSpeedButton;
//    private Button mPowerButton, mSpeedUpButton, mSpeedDownButton;
    // Text View
    private TextView mStatus, mNameRemoteControl;

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

        View root = inflater.inflate(R.layout.fragment_remote_control, container, false);
        // Set name of remote control
        mNameRemoteControl = root.findViewById(R.id.tv_remote_control);
        mNameRemoteControl.setText(R.string.air_purifier);

        // Get status of remote control
        mStatus = root.findViewById(R.id.tv_status);

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
                        mStatus.append("\n" + getString(R.string.title_lost_connection));
                        mqttClient.setConnected(false);
                        mqttClient.setConnecting(true);
                        mqttClient.makeConnectionToServer();
                        // Start a new information checker
                        new InformationChecker().start();
                    }
                })
                .setNegativeButton(R.string.bt_pass, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mStatus.append("\n" + getString(R.string.title_lost_connection));
                    }
                })
                .create();

        // Get button from id and set on click onCheckedChangeListener
        // Power
        mPowerButton = root.findViewById(R.id.bt_power);
        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });

        // Speed
        mSpeedButton = root.findViewById(R.id.bt_speed);
        mSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });
        // Rhythm
        mRhythmButton = root.findViewById(R.id.bt_rhythm);
        mRhythmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });
        // Swing
        mSwingButton = root.findViewById(R.id.bt_swing);
        mSwingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });
        // Timer
        mTimerButton = root.findViewById(R.id.bt_timer);
        mTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemoteButton(v);
            }
        });

        // Get sound button
        soundButton = MediaPlayer.create(parentActivity, R.raw.sample);
        // Get vibrate
        vibrator = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Get progress bar
        mPbLoadConnection = root.findViewById(R.id.pb_load_connection);
        // New thread for check
        new InformationChecker().start();
        // Return view
        return root;
    }

    /**
     * Disable all button on remote control
     */
    private void disableRemoteButton() {
//        // Power up button
//        mPowerButton.setEnabled(false);
//        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
//        // Speed up button
//        mPowerButton.setEnabled(false);
//        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
//        // Speed down button
//        mPowerButton.setEnabled(false);
//        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));

        // Power button
        mPowerButton.setEnabled(false);
        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Rhythm button
        mRhythmButton.setEnabled(false);
        mRhythmButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Speed button
        mSpeedButton.setEnabled(false);
        mSpeedButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Swing button
        mSwingButton.setEnabled(false);
        mSwingButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
        // Timer button
        mTimerButton.setEnabled(false);
        mTimerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
    }

    /**
     * enable all button
     */
    private void enableRemoteButton() {
        // Power button
        mPowerButton.setEnabled(true);
        mPowerButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Rhythm button
        mRhythmButton.setEnabled(true);
        mRhythmButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Speed button
        mSpeedButton.setEnabled(true);
        mSpeedButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Swing button
        mSwingButton.setEnabled(true);
        mSwingButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
        // Timer button
        mTimerButton.setEnabled(true);
        mTimerButton.setBackground(getResources().getDrawable(R.drawable.list_selector_background, null));
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
        switch(view.getId()) {
            case R.id.bt_power :
                topic = AirPurifierTopics.POWER;
                break;
            case R.id.bt_speed:
                topic = AirPurifierTopics.SPEED;
                break;
            case R.id.bt_swing:
                topic = AirPurifierTopics.SWING;
                break;
            default:
                Log.e(LOG_TAG, "Cannot detect button id " + view.toString());
                return;
        }
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.getMqttManager().publishString(msg, topic, AWSIotMqttQos.QOS0);
                // TODO : send publish to get state after get condition
            } else {
                mConnectDialog.show();
            }
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }

    private class InformationChecker extends Thread {
        @Override
        public void run() {
            try {
                // Check connection
                int loopNumber = 6;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disableRemoteButton();
                        mPbLoadConnection.setVisibility(View.VISIBLE);
                    }
                });
                while (mqttClient.isConnecting()) {
                    Thread.sleep(500);
                }


                if (!mqttClient.isConnected()) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatus.setText(R.string.title_lost_connection);
                            mConnectDialog.show();
                        }
                    });
                    // Get out
                    return;
                } else {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatus.setText(R.string.connected_status);
                        }
                    });
                }

                String power = null, speed = null;

                // Get power
                for (int i = 0; i < loopNumber; i++) {
                    if ((power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue())) == null) {
                        Thread.sleep(500);
                        if (i == loopNumber - 1) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    disableRemoteButton();
                                }
                            });
                        }
                    } else {
                        break;
                    }
                }

                // Get speed
                for (int i = 0; i < loopNumber; i++) {
                    if ((speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue())) == null) {
                        Thread.sleep(500);
                        if (i == loopNumber - 1) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    disableRemoteButton();
                                }
                            });
                        }
                    } else {
                        break;
                    }
                }

                final String cPower = power, cSpeed = speed;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatus.append("\nPower = " + cPower + "\nSpeed = " + cSpeed);
                    }
                });
                // Get device id
                for (int i = 0; i < loopNumber; i++) {
                    if ((deviceId = parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()).getDeviceId()) != null) {
                        break;
                    } else {
                        Thread.sleep(500);
                        if (i == loopNumber - 1) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    disableRemoteButton();
                                }
                            });
                        }
                    }
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
