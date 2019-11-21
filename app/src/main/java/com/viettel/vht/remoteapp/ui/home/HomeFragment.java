package com.viettel.vht.remoteapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.github.ybq.android.spinkit.SpinKitView;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.monitoring.MonitoringSystem;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    GridView gdView1, gdView2, gdView3;
    RelativeLayout aqStatus;
    TextView txtAQValue, txtAQLevel, txtAQTitle;
    SpinKitView loadingBar;

    private Thread monitoringThread;
    private MonitoringSystem monitoringSystem;

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
        loadingBar = (SpinKitView) root.findViewById(R.id.loading);

        // getting data in another thread
        monitoringThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (getActivity() == null)
                            return;
                        System.out.println("Getting data...");
                        monitoringSystem.readAndDisplayStatus(aqStatus, txtAQValue, txtAQTitle, txtAQLevel, gdView1, gdView2, gdView3, loadingBar);
                        Thread.sleep(Constants.UPDATE_DATA_TIME * 1000); // get data for each 5s
                    } catch (InterruptedException e) { // stop getting data
                        System.out.println("Stop getting data...");
                        break;
                    }
                }
            }
        };
        monitoringThread.start();

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        monitoringThread.interrupt(); // stop getting data
    }

}