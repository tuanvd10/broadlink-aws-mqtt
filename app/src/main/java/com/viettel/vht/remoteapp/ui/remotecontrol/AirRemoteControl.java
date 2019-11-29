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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils;
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

    // Radio button
    private RadioButton mLowSpeed, mMedSpeed, mHighSpeed;

    // Text View
//    private TextView mPowerState, mNameRemoteControl, mSpeedState, tvPowerButton, tvUVButton;

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

    // On click listener for radio button
    RadioButton.OnClickListener rbOnClickListener = new RadioButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            try {
                switch (v.getId()) {
                    case R.id.rb_speed_low:
                        if (changeInSpeed(AirPurifierTopics.LOW_SPEED)) {
//                            mSpeedState.setText(R.string.speed_1);
                        }
                        break;
                    case R.id.rb_speed_med:
                        if (changeInSpeed(AirPurifierTopics.MED_SPEED)) {
//                            mSpeedState.setText(R.string.speed_2);
                        }
                        break;
                    case R.id.rb_speed_high:
                        if (changeInSpeed(AirPurifierTopics.HIGH_SPEED)) {
//                            mSpeedState.setText(R.string.speed_3);
                        }
                        break;
                    default:
                        Log.d(LOG_TAG, "Wrong id: " + v.getId());
                        break;
                }
            } catch (DisconnectionException de) {
                // TODO process disconnect exception
                de.printStackTrace();
                // show popup for user choose
                mCloudConnectionDialog.show();
            }
        }
    };
    // Power and speed property
    private String power = null;
    private String speed = null;
    private int expectedSpeed ;
    private String expectedPower = null;
    // Is progress bar visible
    private boolean isProgressBarVisible = false;

    public boolean isProgressBarVisible() {
        return isProgressBarVisible;
    }

    public void setProgressBarVisible(boolean progressBarVisible) {
        isProgressBarVisible = progressBarVisible;
    }

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
//        mNameRemoteControl = root.findViewById(R.id.tv_remote_control);
//        mNameRemoteControl.setText(R.string.air_purifier);

        // Get status of remote control
        // TODO change in power state
//        mPowerState = root.findViewById(R.id.tv_power_title);
//        mSpeedState = root.findViewById(R.id.tv_speed_state);

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
//                        showProgressBar();
//                        mqttClient.setConnected(false);
//                        mqttClient.setConnecting(true);
//                        mqttClient.makeConnectionToServer();
                        // Start a new information checker
//                        parentActivity.checkInformation();
//                        new AirRemoteControl.InformationChecker().start();
                    }
                })
                .setNegativeButton(R.string.bt_pass, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        // Dialog will show when connection to aws isn't established
        mServerConnectionDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.info)
                .setMessage(R.string.msg_no_response_from_device)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressBar();
                        mqttClient.setConnected(false);
                        mqttClient.setConnecting(true);
                        mqttClient.makeConnectionToServer();
                        // Start a new information checker
//                        new AirRemoteControl.InformationChecker().start();
                    }
                })
                .create();

        // Dialog will shown when app cannot found deviceid in response message
        mNotFoundDeviceIdDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.info)
                .setMessage(R.string.check_smart_plug_and_remote)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressBar();
                        requestDeviceInfo();
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
        // Get radio button
        // low speed
        mLowSpeed = root.findViewById(R.id.rb_speed_low);
        mLowSpeed.setOnClickListener(rbOnClickListener);
        // medium speed
        mMedSpeed = root.findViewById(R.id.rb_speed_med);
        mMedSpeed.setOnClickListener(rbOnClickListener);
        // high speed
        mHighSpeed = root.findViewById(R.id.rb_speed_high);
        mHighSpeed.setOnClickListener(rbOnClickListener);

        // Get UV button
        mUVButton = root.findViewById(R.id.bt_uv);
        mUVButton.setOnClickListener(onClickListener);
//        // Get TextView for power button
//        tvPowerButton = root.findViewById(R.id.tv_power);
//        // Get TextView for UV button
//        tvUVButton = root.findViewById(R.id.tv_uv);
        // Get sound button
        soundButton = MediaPlayer.create(parentActivity, R.raw.sample_2);
        // Get vibrate
        vibrator = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Get progress bar
        mPbLoadConnection = root.findViewById(R.id.pb_load_connection);
        // New thread for check
        showProgressBar();
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

    private boolean changeInSpeed(String topic) throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        // Publish message to aws server
        try {
            if (mqttClient.isConnected()) {
                mqttClient.publish(message, topic);
//                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // TODO : Get state after request change speed in server
//                removeSpeedStateInMainActivity();
//                mqttClient.requestSpeedStateOfDevice(parentActivity.getSmartPlugId());
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


    private boolean power() throws DisconnectionException {
        boolean retVal = true;
        String message = "play-" + deviceId;
        String topic = AirPurifierTopics.POWER;
        // Publish message to aws server
        try {
            // check connection to aws
            if (mqttClient.isConnected()) {
                if (power.equals(getString(R.string.state_power_off))) {
                    // TODO request set on to smart plug id

                }
                // request set on to device (air purifier)
                mqttClient.publish(message, topic);
//                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
                // request low energy for air purifier to know new value
                final boolean willPowerOn;
                if (expectedPower.equals(getString(R.string.state_power_on))) {
                    willPowerOn = true;
                } else {
                    willPowerOn = false;
                }

                // TODO check state from server
                // Change ui if speed has difference
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (willPowerOn) {
                        // TODO change mSpeedState (receive from server, speed now of air purifier)
//                        mPowerState.setText(R.string.state_power_on);
//                        mSpeedState.setText(speed);
//                        tvPowerButton.setText(getString(R.string.power_off));
                        enableUVButton();
                        enableSpeedRadioGroup();
                    } else {
//                        mPowerState.setText(R.string.state_power_off);
//                        mSpeedState.setText(R.string.speed_0);
//                        tvPowerButton.setText(getString(R.string.power_on));
                        disableUVButton();
                        disableSpeedRadioGroup();
                    }
                    }
                });

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
     * process uv button
     * @return
     * @throws DisconnectionException
     */
    private boolean uv() throws DisconnectionException {
        boolean retVal = true;
//        String message = "play-" + deviceId;
//        String topic = AirPurifierTopics.UV;
//        // Publish message to aws server
//        try {
//            if (mqttClient.isConnected()) {
//                mqttClient.getMqttManager().publishString(message, topic, AWSIotMqttQos.QOS0);
//                // TODO change text view in button
//                final boolean willPowerOn;
//                if (tvUVButton.getText().equals(getString(R.string.uv_off))) {
//                    willPowerOn = true;
//                } else {
//                    willPowerOn = false;
//                }
//                // TODO check state from server
//                // Change value in screen
//                ThreadUtils.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (willPowerOn) {
//                            tvUVButton.setText(getString(R.string.uv_on));
//                        } else {
//                            tvUVButton.setText(getString(R.string.uv_off));
//                        }
//
//                    }
//                });
//                // Wait new information checker
////                new InformationChecker().start();
//            } else {
//                throw new DisconnectionException();
//            }
//        } catch (AmazonClientException ace) {
//            retVal = false;
//            ace.printStackTrace();
//        }
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

    /**
     * Get speed state of device
     * @throws InterruptedException
     */
    private void requestSpeedStateOfDevice() throws InterruptedException {
        mqttClient.requestSpeedStateOfDevice(parentActivity.getSmartPlugId());
    }

    /**
     * Get power state of device
     * @throws InterruptedException
     */
    private void requestPowerStateOfDevice() {
        mqttClient.requestPowerStateOfDevice(parentActivity.getSmartPlugId());
    }

    private void requestDeviceInfo() {
        mqttClient.requestDeviceInfos();
    }

    private void disablePowerButton() {
        mPowerButton.setEnabled(false);
//        mPowerButton.setBackground(getResources().getDrawable(R.drawable.bt_grey_round, null));
    }


    /**
     * check mqtt connection
     * @return
     * @throws InterruptedException
     */
    private boolean checkMqttConnection() throws InterruptedException {
        boolean isConnect = false;
        for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
            if (mqttClient.isConnecting()) {
                // Wait to connect
                Thread.sleep(Constants.SLEEP_TIME);
            } else {
                // connected or lost connection
                if (mqttClient.isConnected()) {
                    isConnect = true;
                }
                break;
            }
        }
        return isConnect;
    }

    /**
     * check pơwer state
     * @return
     * @throws InterruptedException
     */
    private boolean checkPowerState() throws InterruptedException {
        boolean isHave = false;
        for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
            // Change power if it has in Main activity
            if ((power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue())) == null) {
                Thread.sleep(Constants.SLEEP_TIME);
            } else {
                isHave = true;
                break;
            }
        }
        return isHave;
    }

    /**
     * Check diffirent between old power and new power
     * @param oldPower
     * @return
     * @throws InterruptedException
     */
    private boolean checkDifferencePowerState(String oldPower) throws InterruptedException {
        boolean isDiff = false;
        for (int i = 0; i < Constants.CHECK_DIFFERENCE_LOOP_NUMBER; i++) {
            // Change power if it has in Main activity
            power = parentActivity.getStateList().get(KeyOfStates.POWER.getValue());
            if (oldPower.equals(power)) {
                Thread.sleep(Constants.CHECK_DIFFERENCE_SLEEP_TIME);
            } else {
                isDiff = true;
                break;
            }
        }
        return isDiff;
    }


    /**
     * Check speed state
     * @return
     * @throws InterruptedException
     */
    private boolean checkSpeedState() throws InterruptedException {
        boolean isSpeedHad = false;
        for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
            if ((speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue())) == null) {
                Thread.sleep(Constants.SLEEP_TIME);
            } else {
                isSpeedHad = true;
                break;
            }
        }
        return isSpeedHad;
    }

    /**
     * Check different state betweet new speed and old speed
     * @return
     * @throws InterruptedException
     */
    private boolean checkDifferenceSpeedState(String oldSpeed) throws InterruptedException {
        boolean isSpeedHad = false;
        for (int i = 0; i < Constants.CHECK_DIFFERENCE_LOOP_NUMBER; i++) {
            speed = parentActivity.getStateList().get(KeyOfStates.SPEED.getValue());
            if (oldSpeed.equals(speed)) {
                Thread.sleep(Constants.CHECK_DIFFERENCE_SLEEP_TIME);
            } else {
                isSpeedHad = true;
                break;
            }
        }
        return isSpeedHad;
    }
    /**
     * Check device id and smart plug id
     * @return
     * @throws InterruptedException
     */
    private boolean checkRemoteDevice() throws InterruptedException {
        // Get device id
        boolean isHaveDevice = false;
        for (int i = 0; i < Constants.LOOP_NUMBER; i++) {

            if (parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()) == null
                    || parentActivity.getDeviceList().get(KeyOfDevice.SMART_PLUG.getValue()) == null) {
                Thread.sleep(Constants.SLEEP_TIME);
            } else {
                deviceId = parentActivity.getDeviceList().get(KeyOfDevice.REMOTE.getValue()).getDeviceId();
                smartPlugId = parentActivity.getDeviceList().get(KeyOfDevice.SMART_PLUG.getValue()).getDeviceId();
                isHaveDevice = true;
                break;
            }
        }
        return isHaveDevice;
    }

    private boolean selectSpeedInRadioButton(int speed) {
        boolean retVal = false;
        switch(speed) {
            case 1:
                mLowSpeed.setChecked(true);
                break;
            case 2:
                mMedSpeed.setChecked(true);
                break;
            case 3:
                mHighSpeed.setChecked(true);
                break;
            default:
                Log.e(LOG_TAG, "Wrong check speed value: " + speed);
                retVal = false;
                break;
        }
        // return value
        return retVal;
    }

    /**
     * Class collects information from list state in main activity
     */
    private class InformationChecker extends Thread {
        @Override
        public void run() {
            boolean firstTime = true;
            boolean isSucess = false;
            // Start an infinite loop to check information
            while (true) {
                try {
                    // Refresh info
                    isSucess = refreshInfo();

                    if (isSucess) {
                        // Action in the first time
                        if (firstTime) {
                            // Change ui in the first time
                            updateUI();
                        }
                    } else {
                        disableRemoteButton();
                    }
                    hideProgressBar();

                    // Wait to next refresh
                    Thread.sleep(Constants.SLEEP_WAIT);
                } catch (InterruptedException ex) {
                    // disable remote button
                    disableRemoteButton();
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Update user interface
     */
    private void updateUI() {
        // Set speed, power state
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set speed and state
//                mSpeedState.setText(speed);
                // Disable button
                enableRemoteButton();
                int speedNumber = Integer.parseInt(speed);
                // TODO change color in power button
                if (power.equals(getString(R.string.state_power_off)) || speedNumber == 0) {
                    // Disable when off
//                    mPowerState.setText(R.string.state_power_off);
//                    tvPowerButton.setText(R.string.power_on);

                    disableUVButton();
                    disableSpeedRadioGroup();
                } else  {
//                    mPowerState.setText(R.string.state_power_on);
//                    tvPowerButton.setText(R.string.power_off);
                    selectSpeedInRadioButton(speedNumber);
                }
            }
        });
    }
    /**
     * Check speed state on device
     * @return
     */
    private boolean checkPower () {
        boolean isChanged = false;
        try {
            // Init variable for power
            String oldPower = power;
            // check difference between old power and new power
            if (checkDifferencePowerState(oldPower)) {
                isChanged = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isChanged;
    }

    /**
     * Check speed state on device
     * @return
     */
    private boolean checkSpeed() {
        boolean isChanged = false;
        try {
            // Init variable for speed
            String oldSpeed = speed;
            if (checkDifferenceSpeedState(oldSpeed)) {
                isChanged = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isChanged;
    }


    /**
     * Show progress bar
     */
    private void showProgressBar() {
        if (!isProgressBarVisible()) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // clear flag not touchable in window
                    parentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // set visibility on
                    mPbLoadConnection.setVisibility(View.VISIBLE);
                }
            });

            setProgressBarVisible(true);
        }
    }

    /**
     * Hide progress bar
     */
    private void hideProgressBar() {
        if (isProgressBarVisible()) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // clear flag not touchable
                    parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    // Invisible progress bar
                    mPbLoadConnection.setVisibility(View.INVISIBLE);
                }
            });

            setProgressBarVisible(false);
        }

    }

    /**
     * Refresh information
     * @throws InterruptedException
     */
    private boolean  refreshInfo() throws InterruptedException {
        // Check connection
        if (!checkMqttConnection()) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    disableRemoteButton();
                    mCloudConnectionDialog.show();
                }
            });
            // Get out
            return false;
        }

        // Get deviceId and smart plug id
        if (!checkRemoteDevice()) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    disableRemoteButton();
                    mNotFoundDeviceIdDialog.show();
                }
            });
            // Get out
            return false;
        }
        // Get power
        final boolean isPowerHad = checkPowerState();
        // Get speed
        final boolean isSpeedHad = checkSpeedState();

        // check value
        if (!(isPowerHad && isSpeedHad)) {
//            mPowerState.setText(R.string.title_lost_connection);
            disableRemoteButton();
            mServerConnectionDialog.show();
            // Get out this
            return false;
        }

        // test
        speed = "1";
        power = "on";
        smartPlugId = "34ea3479ee2b";
        deviceId = "770f78d91f64";

        return true;
    }


    /**
     * task: collects information from MainActivity
     */
//    private Runnable infomationChecker = new Runnable() {
//        private final int Constants.LOOP_NUMBER = 6;
//
//        private String checkPowerState() throws InterruptedException {
//            String power = null;
//            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
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
//            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
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
