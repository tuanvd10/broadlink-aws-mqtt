package com.viettel.vht.remoteapp.remotecontrol;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.viettel.vht.remoteapp.R;

public class MitsubishiControl extends AppCompatActivity {

    Button btPower;
    Button btSwing;
    Button btRhythm;
    Button btSpeed;
    Button btTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.misubishi_control_main);
        super.onCreate(savedInstanceState);

    }

}
