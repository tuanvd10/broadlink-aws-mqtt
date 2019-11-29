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
import com.viettel.vht.remoteapp.monitoring.MonitoringSystem;

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


        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        monitoringThread.interrupt(); // stop getting data
    }

    // Hungdv39 add variable
    private Button mBtPower, mBtLowSpeed, mBtMedSpeed, mBtHighSpeed;
    private PowerState expectedPower = PowerState.NULL;
    private int expectedSpeed = -1;

    static final String LOG_TAG = HomeFragment.class.getCanonicalName();

    private void power() {
        if (expectedPower == PowerState.ON) {
            expectedPower = PowerState.OFF;
        } else if (expectedPower == PowerState.OFF) {
            expectedPower = PowerState.ON;
        } else {
            Log.e(LOG_TAG, "Error in expected power = null");
        }
    }


    private void changeSpeed(View view) {
        switch(view.getId()) {
            case R.id.bt_low_speed:
                Log.i(LOG_TAG, "low speed");
                break;
            case R.id.bt_med_speed:
                Log.i(LOG_TAG, "med speed");
                break;
            case R.id.bt_high_speed:
                Log.i(LOG_TAG, "high speed");
                break;
            default:
                Log.i(LOG_TAG, "wrong view : " + view.getId());
                break;
        }
    }

}