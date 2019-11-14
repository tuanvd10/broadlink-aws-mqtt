package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.viettel.vht.remoteapp.R;

public class RemoteControlFragment extends Fragment {
    private RemoteControlViewModel remoteControlViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(this.getClass().getName(), "Create view");
        remoteControlViewModel = ViewModelProviders.of(this).get(RemoteControlViewModel.class);

        View root = inflater.inflate(R.layout.fragment_remote_control, container, false);
        final TextView textView = root.findViewById(R.id.text_remote_control);
        remoteControlViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });


        return root;
    }

}
