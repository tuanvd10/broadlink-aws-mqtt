package com.viettel.vht.remoteapp.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils;
import com.viettel.vht.remoteapp.MainActivity;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.ControlMode;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;
import com.viettel.vht.remoteapp.monitoring.MonitoringSystem;
import com.viettel.vht.remoteapp.objects.AirPurifier;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    GridView gdView1, gdView2, gdView3;
    RelativeLayout aqStatus;
    TextView txtAQValue, txtAQLevel, txtAQTitle;
    ProgressBar loadingBar;
    SwipeRefreshLayout refreshStatusSwipe;
    ImageView dsIcon;
    TextView dsText;

    private Thread monitoringThread;
    private MonitoringSystem monitoringSystem;
    private MainActivity parentActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monitoringSystem = new MonitoringSystem(getActivity());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // define views
        gdView1 = (GridView) root.findViewById(R.id.gdHTP);
        gdView2 = (GridView) root.findViewById(R.id.gdCO2);
        gdView3 = (GridView) root.findViewById(R.id.gdPM);
        aqStatus = (RelativeLayout) root.findViewById(R.id.aq_status);
        txtAQValue = (TextView) root.findViewById(R.id.aq_status_value);
        txtAQLevel = (TextView) root.findViewById(R.id.aq_status_level);
        txtAQTitle = (TextView) root.findViewById(R.id.aq_status_title);
        loadingBar = (ProgressBar) root.findViewById(R.id.loading);
//        refreshStatusSwipe = (SwipeRefreshLayout) root.findViewById(R.id.swiperefreshStatus);
        dsIcon = (ImageView) root.findViewById(R.id.ds_icon);
        dsText = (TextView) root.findViewById(R.id.ds_text);


        // getting data in another thread
        monitoringThread = new Thread() {
            @Override
            public void run() {
                loadingStatusData();
            }
        };
        monitoringThread.start();

//        refreshStatusSwipe.setOnRefreshListener(
//                new SwipeRefreshLayout.OnRefreshListener() {
//                    @Override
//                    public void onRefresh() {
//                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
//                        if (monitoringSystem != null)
//                            monitoringSystem.readAndDisplayStatus(aqStatus, txtAQValue, txtAQTitle, txtAQLevel, gdView1, gdView2, gdView3, loadingBar, refreshStatusSwipe, dsIcon, dsText);
//
//                    }
//                }
//        );

        // Hungdv39 change below
        // Power
        mBtPower = root.findViewById(R.id.bt_power);
        mBtPower.setOnClickListener(btPowerClick);
        // Low speed
        mBtLowSpeed = root.findViewById(R.id.bt_low_speed);
        mBtLowSpeed.setOnClickListener(btSpeedClick);
        // Medium speed
        mBtMedSpeed = root.findViewById(R.id.bt_med_speed);
        mBtMedSpeed.setOnClickListener(btSpeedClick);
        // High speed
        mBtHighSpeed = root.findViewById(R.id.bt_high_speed);
        mBtHighSpeed.setOnClickListener(btSpeedClick);

        // Switch
        mSwitchMode = root.findViewById(R.id.sw_mode);
        mSwitchMode.setOnClickListener(switchModeListener);
        // parent activity
        parentActivity = (MainActivity) getActivity();

        // Dialog
        mCannotRemoteDevice = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.info)
                .setMessage(R.string.cannot_remote_device)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(LOG_TAG, "OK button click on CannotRemoteDevice dialog");
                    }
                }).create();

        // get expected state in device
        expectedStateInDevice = parentActivity.getExpectedState();
        stateInUI = new AirPurifier();
        // Wait to update ui
        disableAllButton();
        updateUI();

        return root;
    }

    private void loadingStatusData(){
        while (true) {
            try {
                if (getActivity() == null){
                    System.out.println("EXCEPCTION: Activity null...");
                    return;
                }
                System.out.println("Getting data...");
                monitoringSystem.readAndDisplayStatus(aqStatus, txtAQValue, txtAQTitle, txtAQLevel, gdView1, gdView2, gdView3, loadingBar, refreshStatusSwipe, dsIcon, dsText);
                Thread.sleep(Constants.UPDATE_DATA_TIME * 1000); // get data for each 5s

            } catch (InterruptedException e) { // stop getting data
                System.out.println("Stop getting data....");
                break;
            }
        }
    }

    @Override
    public void onResume() {
        if (!monitoringThread.isAlive()){
            System.out.println("RESTARTING data...");
            monitoringThread = new Thread() {
                @Override
                public void run() {
                    loadingStatusData();
                }
            };
            monitoringThread.start();
        }
        System.out.println("STARTING data...");
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        monitoringThread.interrupt(); // stop getting data
    }

    // Hungdv39 add variable
    private Button mBtPower, mBtLowSpeed, mBtMedSpeed, mBtHighSpeed;
    private AirPurifier expectedStateInDevice;
    private Switch mSwitchMode;
    private AirPurifier stateInUI;

    static final String LOG_TAG = HomeFragment.class.getCanonicalName();

    View.OnClickListener btPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "Click power button");
            power(v);
        }
    };

    View.OnClickListener btSpeedClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.bt_low_speed:
                    expectedStateInDevice.setSpeed(SpeedState.LOW);
                    stateInUI.setSpeed(SpeedState.LOW);
                    uiInLowSpeed();
                    Log.i(LOG_TAG, "low speed");
                    break;
                case R.id.bt_med_speed:
                    expectedStateInDevice.setSpeed(SpeedState.MED);
                    stateInUI.setSpeed(SpeedState.MED);
                    uiInMedSpeed();
                    Log.i(LOG_TAG, "med speed");
                    break;
                case R.id.bt_high_speed:
                    expectedStateInDevice.setSpeed(SpeedState.HIGH);
                    stateInUI.setSpeed(SpeedState.HIGH);
                    uiInHighSpeed();
                    Log.i(LOG_TAG, "high speed");
                    break;
                default:
                    Log.i(LOG_TAG, "wrong view : " + v.getId());
                    break;
            }
        }
    };


    View.OnClickListener switchModeListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mSwitchMode.isChecked()) {
                // Auto mode
                mSwitchMode.setText(R.string.mode_auto);
                stateInUI.setControlMode(ControlMode.AUTO);
                expectedStateInDevice.setControlMode(ControlMode.AUTO);
                loopFlag = false;
                uiInAutoMode();
            } else {
                // Manual mode
                mSwitchMode.setText(R.string.mode_manual);
                stateInUI.setControlMode(ControlMode.MANUAL);
                expectedStateInDevice.setControlMode(ControlMode.MANUAL);
                new updateUI().start();
            }
        }
    };

    /**
     * Enable speed button
     */
    private void enableSpeedButton() {
        mBtLowSpeed.setEnabled(true);
        mBtMedSpeed.setEnabled(true);
        mBtHighSpeed.setEnabled(true);
    }

    /**
     * Disable speed button
     */
    private void disableSpeedButton() {
        mBtLowSpeed.setEnabled(false);
        mBtMedSpeed.setEnabled(false);
        mBtHighSpeed.setEnabled(false);
    }

    /**
     * Disable all button
     */
    private void disableAllButton() {
        mBtPower.setEnabled(false);
        mSwitchMode.setEnabled(false);
        disableSpeedButton();
    }

    /**
     * Enable all button
     */
    private void enableAllButton() {
        mBtPower.setEnabled(true);
        mSwitchMode.setEnabled(true);
        enableSpeedButton();
    }
    /**
     * ui in power on
     */
    private void uiInPowerOn() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableSpeedButton();
                // Enable all button
                mBtPower.setEnabled(true);
                mBtPower.setBackground(getResources().getDrawable(R.drawable.bg_power_on_bt, null));
            }
        });

    }

    /**
     * ui in power off
     */
    private void uiInPowerOff() {
        // Disable speed button
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Disable speed button
                disableSpeedButton();
                // Enable power button
                mBtPower.setEnabled(true);
                mBtPower.setBackground(getResources().getDrawable(R.drawable.bg_power_off_bt, null));
            }
        });
    }


    private void uiInLowSpeed() {
        // Disable speed button
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Enable all button
                mBtLowSpeed.setBackground(getResources().getDrawable(R.drawable.bg_selected_speed_bt, null));
                mBtMedSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
                mBtHighSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
            }
        });
    }

    private void uiInMedSpeed() {
        // Disable speed button
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Enable all button
                mBtLowSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
                mBtMedSpeed.setBackground(getResources().getDrawable(R.drawable.bg_selected_speed_bt, null));
                mBtHighSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
            }
        });
    }

    private void uiInHighSpeed() {
        // Disable speed button
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Enable all button
                mBtLowSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
                mBtMedSpeed.setBackground(getResources().getDrawable(R.drawable.bg_speed_bt, null));
                mBtHighSpeed.setBackground(getResources().getDrawable(R.drawable.bg_selected_speed_bt, null));
            }
        });
    }

    /**
     * Disable all button except switch button
     */
    private void uiInAutoMode() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableAllButton();
                mSwitchMode.setEnabled(true);
                mSwitchMode.setChecked(true);
                mSwitchMode.setText(R.string.mode_auto);
            }
        });
    }

    private void uiInManualMode() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwitchMode.setEnabled(true);
                mSwitchMode.setChecked(false);
                mSwitchMode.setText(R.string.mode_manual);
            }
        });
    }

    private void power(View view) {
        // Check value of smart plug id
        if (stateInUI.getPower() == PowerState.OFF) {
            stateInUI.setPower(PowerState.ON);
            expectedStateInDevice.setPower(PowerState.ON);
        }
        // Value of speed in smart plug id (power + speed in device)
        Log.d(LOG_TAG, "Old_state = " + stateInUI.getSpeed().name());
        if (stateInUI.getSpeed() != SpeedState.OFF) {
            // Now: power on => Set power off
            // state in ui
            Log.i(LOG_TAG, "Turn off device");
            stateInUI.setSpeed(SpeedState.OFF);
            stateInUI.setPower(PowerState.OFF);
            // expected state
            expectedStateInDevice.setSpeed(SpeedState.OFF);
            expectedStateInDevice.setPower(PowerState.OFF);
            //Set ui
            uiInPowerOff();
        } else {
            // Now: power off => Set power on
            // State in ui
            Log.i(LOG_TAG, "Turn off device");
            stateInUI.setSpeed(SpeedState.LOW);
            stateInUI.setPower(PowerState.ON);
            // Expected state
            expectedStateInDevice.setSpeed(SpeedState.LOW);
            expectedStateInDevice.setPower(PowerState.ON);
            //Set ui
            uiInPowerOn();
            uiInLowSpeed();
        }
    }

    private void setVisibleAllButton() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set visible all button
                mBtPower.setVisibility(View.VISIBLE);
                mBtLowSpeed.setVisibility(View.VISIBLE);
                mBtMedSpeed.setVisibility(View.VISIBLE);
                mBtHighSpeed.setVisibility(View.VISIBLE);
                // Set visible switch
                mSwitchMode.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * check value between expected state and state in ui
     * @return
     */
    private boolean checkStateBetweenExpectedAndUI() {
        if (expectedStateInDevice.getControlMode() != stateInUI.getControlMode() ||
            expectedStateInDevice.getPower() != stateInUI.getPower() ||
            expectedStateInDevice.getSpeed() != stateInUI.getSpeed()) {
            return true;
        }

        return false;
    }

    /**
     * Start update ui
     */
    public void updateUI() {
        // start updateUI
        new updateUI().start();
    }

    private class updateUI extends Thread {

        @Override
        public void run() {
            try {
                while(loadingBar.getVisibility() == View.VISIBLE) {
                    Thread.sleep(Constants.WAIT_TO_UPDATE_UI);
                }

                // Visiable all button
                setVisibleAllButton();

                // Wait until complete load
                while(!expectedStateInDevice.isNotNull()) {
                    Thread.sleep(Constants.WAIT_TO_UPDATE_UI);
                }

                // set ui state
                stateInUI.setPower(expectedStateInDevice.getPower());
                stateInUI.setSpeed(expectedStateInDevice.getSpeed());
                stateInUI.setControlMode(expectedStateInDevice.getControlMode());

                // Set ui
                if (expectedStateInDevice.getControlMode() == ControlMode.AUTO) {
                    // auto mode
                    uiInAutoMode();
                } else {
                    // manual mode
                   uiInManualMode();
                    // Check speed and power
                    if (expectedStateInDevice.getSpeed() == SpeedState.OFF) {
                        // off device
                        uiInPowerOff();
                    } else {
                        // on device
                        uiInPowerOn();
                        switch (expectedStateInDevice.getSpeed()) {
                            case LOW:
                                uiInLowSpeed();
                                break;
                            case MED:
                                uiInMedSpeed();
                                break;
                            case HIGH:
                                uiInHighSpeed();
                                break;
                            default:
                                Log.e(LOG_TAG, "Error in speed");
                        }
                    }
                }

                // Start check update
                if (!loopFlag) {
                    checkUpdate();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }
    }


    // Dialog inform to user when it cannot remote the device
    private Dialog mCannotRemoteDevice;

    /**
     * function to call cannot remote dialog
     */
    private void showCannotRemoteDeviceDialog() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCannotRemoteDevice.show();
            }
        });
    }

    private void checkUpdate() {
        loopFlag = true;
        new CheckUpdate().start();
    }

    // Flag for check update
    private boolean loopFlag = false;

    private class CheckUpdate extends Thread {
        @Override
        public void run() {
            // infinite loop check value
            try {
                while(loopFlag) {
                    if (checkStateBetweenExpectedAndUI()) {
                        showCannotRemoteDeviceDialog();
                        updateUI();
                    }
                    Thread.sleep(Constants.WAIT_TO_STATE_CHANGE);
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }
    }

}