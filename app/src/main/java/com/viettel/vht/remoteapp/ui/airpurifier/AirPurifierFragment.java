package com.viettel.vht.remoteapp.ui.airpurifier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.viettel.vht.remoteapp.MainActivity;
import com.viettel.vht.remoteapp.R;

public class AirPurifierFragment extends Fragment {

    MainActivity parentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_air_purifier_home, container, false);
        // Get parent activity
        parentActivity = (MainActivity) getActivity();


        // Load start screen
        new StartScreenLoader().start();
        return root;
    }


    private class StartScreenLoader extends Thread {
        @Override
        public void run() {

        }
    }

}
