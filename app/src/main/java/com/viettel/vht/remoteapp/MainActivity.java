package com.viettel.vht.remoteapp;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.android.material.navigation.NavigationView;
import com.viettel.vht.remoteapp.common.AirPurifierTopics;
import com.viettel.vht.remoteapp.common.DevicesTopics;
import com.viettel.vht.remoteapp.common.KeyOfDevice;
import com.viettel.vht.remoteapp.common.KeyOfStates;
import com.viettel.vht.remoteapp.objects.Device;
import com.viettel.vht.remoteapp.utilities.MqttClientToAWS;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private MqttClientToAWS mqttClient;
    private HashMap<String, Device> deviceList = new HashMap<String, Device>();
    private HashMap<String, String> stateList = new HashMap<String, String>();
    private String remoteDeviceId = null;
    private String smartPlugId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Connect to server
        mqttClient = new MqttClientToAWS(this);
        // For navigator
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_remote_control, R.id.nav_air_remote_control, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Get info of device
        new InformationCollector().start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void subscribeDeviceInfo() {
        mqttClient.getMqttManager().subscribeToTopic(DevicesTopics.SUBSCRIBE_DEVICE_INFO, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                String strData = new String(data);
                Log.d(LOG_TAG, "data in device = " + strData);
                try {
                    JSONArray jsonDevices = new JSONArray(strData);
                    JSONObject jsonDevice;
                    Device device;
                    for (int i = 0; i < jsonDevices.length(); i++) {
                        jsonDevice = jsonDevices.getJSONObject(i);
                        device = new Device(jsonDevice.getString("name"), jsonDevice.getString("id"));
                        deviceList.put(device.getName(), device);
                    }
                    // get remoteId
                    remoteDeviceId = deviceList.get(KeyOfDevice.REMOTE.getValue()).getDeviceId();
                    smartPlugId = deviceList.get(KeyOfDevice.SMART_PLUG.getValue()).getDeviceId();

                } catch (JSONException je) {
                    Log.e(LOG_TAG, "Error when parsing json device info");
                    je.printStackTrace();
                }

            }
        });
    }

    private void subscribeDeviceStates() {
        // Subscribe state of topic
        mqttClient.getMqttManager().subscribeToTopic(AirPurifierTopics.SUBSCRIBE_STATE_POWER, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                Log.d(LOG_TAG, "data_power = " + new String(data));
                stateList.put(KeyOfStates.POWER.getValue(), new String(data));
            }
        });

        // Subscribe speed state of topic
        mqttClient.getMqttManager().subscribeToTopic(AirPurifierTopics.SUBSCRIBE_STATE_SPEED, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                Log.d(LOG_TAG, "data_speed = " + new String(data));
                stateList.put(KeyOfStates.SPEED.getValue(), new String(data));
            }
        });
    }

    /**
     * class: get device and state of device from aws iot
     */
    private class InformationCollector extends Thread {
        @Override
        public void run() {
            try {
                while(!mqttClient.isConnected()) {
                    Thread.sleep(200);
                }

                subscribeDeviceInfo();
                mqttClient.requestDeviceInfos();

                // Subscribe information
                while (remoteDeviceId == null) {
                    Thread.sleep(200);
                }
                // Subscribe state of devices
                subscribeDeviceStates();

                // Request state of device
                while (smartPlugId == null) {
                    Thread.sleep(200);
                }

                // Send request
                mqttClient.requestAWSIotServer("checkpower-" + smartPlugId, AirPurifierTopics.REQUEST_STATE_POWER);
                String power = null;
                while ((power = stateList.get(KeyOfStates.POWER.getValue())) == null) {
                    Thread.sleep(200);
                }

                // Check speed if power on
                Thread.sleep(200);
                if (power.equals(getString(R.string.state_power_on))) {
                    mqttClient.requestAWSIotServer("checkspeed-" + smartPlugId, AirPurifierTopics.REQUEST_STATE_SPEED);
                } else if (power.equals(getString(R.string.state_power_off))) {
                    stateList.put(KeyOfStates.SPEED.getValue(), "0");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Getter
    public MqttClientToAWS getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(MqttClientToAWS mqttClient) {
        this.mqttClient = mqttClient;
    }

    public HashMap<String, Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(HashMap<String, Device> deviceList) {
        this.deviceList = deviceList;
    }

    public HashMap<String, String> getStateList() {
        return stateList;
    }

    public void setStateList(HashMap<String, String> stateList) {
        this.stateList = stateList;
    }

    public String getRemoteDeviceId() {
        return remoteDeviceId;
    }

    public void setRemoteDeviceId(String remoteDeviceId) {
        this.remoteDeviceId = remoteDeviceId;
    }

    public String getSmartPlugId() {
        return smartPlugId;
    }

    public void setSmartPlugId(String smartPlugId) {
        this.smartPlugId = smartPlugId;
    }
}
