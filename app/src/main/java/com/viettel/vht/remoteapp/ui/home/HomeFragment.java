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

import com.viettel.vht.remoteapp.MainActivity;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.common.SpeedState;
import com.viettel.vht.remoteapp.monitoring.MonitoringSystem;
import com.viettel.vht.remoteapp.objects.AirPurifier;

import java.util.HashMap;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    GridView gdView1, gdView2, gdView3;
    RelativeLayout aqStatus;
    TextView txtAQValue, txtAQLevel, txtAQTitle;
    ProgressBar loadingBar;
    ImageView dsIcon;
    TextView dsText;

    private Thread monitoringThread;
    private MonitoringSystem monitoringSystem;
    private MainActivity activity;

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
        dsIcon = (ImageView) root.findViewById(R.id.ds_icon);
        dsText = (TextView) root.findViewById(R.id.ds_text);

        // getting data in another thread
        monitoringThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (getActivity() == null)
                            return;
                        System.out.println("Getting data...");
                        monitoringSystem.readAndDisplayStatus(aqStatus, txtAQValue, txtAQTitle, txtAQLevel, gdView1, gdView2, gdView3, loadingBar, dsIcon, dsText);
                        Thread.sleep(Constants.UPDATE_DATA_TIME * 1000); // get data for each 5s
                    } catch (InterruptedException e) { // stop getting data
                        System.out.println("Stop getting data...");
                        break;
                    }
                }
            }
        };
        monitoringThread.start();

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

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        monitoringThread.interrupt(); // stop getting data
    }

    // Hungdv39 add variable
    private Button mBtPower, mBtLowSpeed, mBtMedSpeed, mBtHighSpeed;
    private AirPurifier expectedStateInDevice = new AirPurifier(PowerState.NULL, SpeedState.NULL);

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
                    Log.i(LOG_TAG, "low speed");
                    break;
                case R.id.bt_med_speed:
                    expectedStateInDevice.setSpeed(SpeedState.MED);
                    Log.i(LOG_TAG, "med speed");
                    break;
                case R.id.bt_high_speed:
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

    /**
     * ui in power on
     */
    private void uiInPowerOn() {
        // Enable all button
        mBtPower.setEnabled(true);
        enableSpeedButton();
    }

    /**
     * ui in power off
     */
    private void uiInPowerOff() {
        // Disable speed button
        disableSpeedButton();
    }

    private void power(View view) {
        if (expectedStateInDevice.getPower() == PowerState.ON) {
            // Power on
            expectedStateInDevice.setPower(PowerState.OFF);
        } else if (expectedStateInDevice.getPower() == PowerState.ON) {
            // Power off
            expectedStateInDevice.setPower(PowerState.ON);
        } else {
            Log.e(LOG_TAG, "Error in expected power = null");
        }
    }

}