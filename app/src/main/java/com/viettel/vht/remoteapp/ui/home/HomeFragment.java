package com.viettel.vht.remoteapp.ui.home;

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
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;
import com.viettel.vht.remoteapp.monitoring.MonitoringSystem;
import com.viettel.vht.remoteapp.objects.AirPurifier;
import com.viettel.vht.remoteapp.remotecontrol.StateChecker;

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
        refreshStatusSwipe = (SwipeRefreshLayout) root.findViewById(R.id.swiperefreshStatus);
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

        refreshStatusSwipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                        if (monitoringSystem != null)
                            monitoringSystem.readAndDisplayStatus(aqStatus, txtAQValue, txtAQTitle, txtAQLevel, gdView1, gdView2, gdView3, loadingBar, refreshStatusSwipe, dsIcon, dsText);

                    }
                }
        );

        // Hungdv39 change below
        // power
        mBtPower = root.findViewById(R.id.bt_power);
        mBtPower.setOnClickListener(btPowerClick);
        // low speed
        mBtLowSpeed = root.findViewById(R.id.bt_low_speed);
        mBtLowSpeed.setOnClickListener(btSpeedClick);
        // Medium speed
        mBtMedSpeed = root.findViewById(R.id.bt_med_speed);
        mBtMedSpeed.setOnClickListener(btSpeedClick);
        // High speed
        mBtHighSpeed = root.findViewById(R.id.bt_high_speed);
        mBtHighSpeed.setOnClickListener(btSpeedClick);

        // Parent Activity
        expectedStateInDevice = ((MainActivity) getActivity()).getExpectedState();
        // Wait to update ui
        disableAllButton();
        new updateUI().start();

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

    static final String LOG_TAG = HomeFragment.class.getCanonicalName();

    View.OnClickListener btPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            power(v);
        }
    };

    View.OnClickListener btSpeedClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.bt_low_speed:
                    expectedStateInDevice.setSpeed(SpeedState.LOW);
                    uiInLowSpeed();
                    Log.i(LOG_TAG, "low speed");
                    break;
                case R.id.bt_med_speed:
                    expectedStateInDevice.setSpeed(SpeedState.MED);
                    uiInMedSpeed();
                    Log.i(LOG_TAG, "med speed");
                    break;
                case R.id.bt_high_speed:
                    uiInHighSpeed();
                    expectedStateInDevice.setSpeed(SpeedState.HIGH);
                    Log.i(LOG_TAG, "high speed");
                    break;
                default:
                    Log.i(LOG_TAG, "wrong view : " + v.getId());
                    break;
            }
        }
    };

    private void enableSpeedButton() {
        mBtLowSpeed.setEnabled(true);
        mBtMedSpeed.setEnabled(true);
        mBtHighSpeed.setEnabled(true);
    }


    private void disableSpeedButton() {
        mBtLowSpeed.setEnabled(false);
        mBtMedSpeed.setEnabled(false);
        mBtHighSpeed.setEnabled(false);
    }

    private void disableAllButton() {
        mBtPower.setEnabled(false);
        disableSpeedButton();
    }

    /**
     * ui in power on
     */
    private void uiInPowerOn() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Enable all button
                mBtPower.setEnabled(true);
                mBtPower.setBackground(getResources().getDrawable(R.drawable.bg_power_on_bt, null));
                enableSpeedButton();
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
                // Enable all button
                mBtPower.setEnabled(true);
                mBtPower.setBackground(getResources().getDrawable(R.drawable.bg_power_off_bt, null));
                disableSpeedButton();
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


    private void power(View view) {
        if (expectedStateInDevice.getPower() == PowerState.ON) {
            // Power on
            expectedStateInDevice.setSpeed(SpeedState.OFF);
            expectedStateInDevice.setPower(PowerState.OFF);
            uiInPowerOff();
        } else if (expectedStateInDevice.getPower() == PowerState.OFF) {
            // Power off
            expectedStateInDevice.setSpeed(SpeedState.LOW);
            expectedStateInDevice.setPower(PowerState.ON);
            uiInPowerOn();
        } else {
            Log.e(LOG_TAG, "Error in expected power = null");
        }
    }

    private class updateUI extends Thread {
        @Override
        public void run() {
            try {
                while(expectedStateInDevice.getSpeed() == SpeedState.NULL) {
                    Thread.sleep(Constants.SLEEP_WAIT);
                }

                if (expectedStateInDevice.getSpeed() == SpeedState.OFF) {
                    uiInPowerOff();
                } else {
                    uiInPowerOn();
                }

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }
    }

}