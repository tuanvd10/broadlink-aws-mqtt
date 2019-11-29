package com.viettel.vht.remoteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.android.material.navigation.NavigationView;
import com.viettel.vht.remoteapp.common.AirPurifierTopics;
import com.viettel.vht.remoteapp.common.Constants;
import com.viettel.vht.remoteapp.common.DevicesTopics;
import com.viettel.vht.remoteapp.common.KeyOfDevice;
import com.viettel.vht.remoteapp.common.KeyOfStates;
import com.viettel.vht.remoteapp.common.PowerState;
import com.viettel.vht.remoteapp.objects.RemoteDevice;
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
    private HashMap<String, RemoteDevice> deviceList = new HashMap<String, RemoteDevice>();
    private HashMap<String, String> stateList = new HashMap<String, String>();


    // Dialog alert
    private Dialog mConnectionProblemDialog, mSmartPlugProblemDialog, mInfoDeviceProblemDialog;

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
                R.id.nav_home, R.id.nav_air_purifier, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Get dialog
        mConnectionProblemDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.title_lost_connection)
                                    .setMessage(R.string.msg_lost_connection)
                                    .setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setNegativeButton(R.string.bt_pass, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .create();

        // Dialog will show when connection to aws isn't established
        mSmartPlugProblemDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.info)
                                    .setMessage(R.string.msg_no_response_from_device)
                                    .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).setPositiveButton(R.string.bt_try_again, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .create();

        // Dialog will shown when app cannot found deviceid in response message
        mInfoDeviceProblemDialog = new AlertDialog.Builder(this)
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

                                        }
                                    }).create();
        // Get info of device
        checkInformation();
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
                    RemoteDevice remoteDevice;
                    for (int i = 0; i < jsonDevices.length(); i++) {
                        jsonDevice = jsonDevices.getJSONObject(i);
                        remoteDevice = new RemoteDevice(jsonDevice.getString("name"), jsonDevice.getString("id"));
                        deviceList.put(remoteDevice.getName(), remoteDevice);
                    }

                } catch (JSONException je) {
                    Log.e(LOG_TAG, "Error when parsing json device info");
                    je.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void subscribeDeviceStates() {
        // Subscribe state of topic
        AWSIotMqttNewMessageCallback callbackPower = new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                Log.d(LOG_TAG, "data_power = " + new String(data));
                stateList.put(KeyOfStates.POWER.getValue(), new String(data));
            }
        };
        mqttClient.subscribe(AirPurifierTopics.SUBSCRIBE_STATE_POWER, callbackPower);

        // Subscribe speed state of topic
        AWSIotMqttNewMessageCallback callbackSpeed = new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                Log.d(LOG_TAG, "data_speed = " + new String(data));
                stateList.put(KeyOfStates.SPEED.getValue(), new String(data));
            }
        };
        mqttClient.subscribe(AirPurifierTopics.SUBSCRIBE_STATE_SPEED, callbackSpeed);

    }


    public void checkInformation() {
        new InformationCollector().start();
    }

    /**
     * class: get device and state of device from aws iot
     */
    private class InformationCollector extends Thread {
        private long sleepTime = 500L;
        /**
         * check mqtt connection
         * @return
         * @throws InterruptedException
         */
        private boolean checkMqttConnection() throws InterruptedException {
            boolean isConnected = false;

            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
                if(!mqttClient.isConnected()) {
                    // Check error
                    Thread.sleep(sleepTime);
                    if (i == Constants.LOOP_NUMBER - 1) {
                        Log.e(LOG_TAG, "Connection is not established");
                    }
                } else {
                    isConnected = true;
                    break;
                }
            }

            return isConnected;
        }

        /**
         * Check RemoteDevice Info
         * @return
         * @throws InterruptedException
         */
        private boolean checkRemoteDeviceInfo() throws InterruptedException {
            boolean isHaveInfo = false;
            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
                if(getSmartPlugId() == null || getRemoteDeviceId() == null) {
                    Thread.sleep(sleepTime);
                    // Check error
                    if (i == Constants.LOOP_NUMBER - 1) {
                        Log.e(LOG_TAG, "Not found smart plug id or remote device id");
                    }
                } else {
                    isHaveInfo = true;
                    break;
                }
            }

            return isHaveInfo;
        }

        /**
         * Check power on device
         * @return
         * @throws InterruptedException
         */
        private boolean checkPowerDevice() throws InterruptedException {
            boolean isPowerExist = false;
            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
                if(stateList.get(KeyOfStates.POWER.getValue()) == null) {
                    Thread.sleep(sleepTime);
                    // Check error
                    if (i == Constants.LOOP_NUMBER - 1) {
                        Log.e(LOG_TAG, "Not found power state");
                    }
                } else {
                    isPowerExist = true;
                    break;
                }
            }

            return isPowerExist;
        }

        /**
         * check speed on device
         * @return
         * @throws InterruptedException
         */
        private boolean checkSpeedDevice() throws InterruptedException {
            boolean isSpeedExist = false;
            for (int i = 0; i < Constants.LOOP_NUMBER; i++) {
                if(stateList.get(KeyOfStates.SPEED.getValue()) == null) {
                    Thread.sleep(sleepTime);
                    // Check error
                    if (i == Constants.LOOP_NUMBER - 1) {
                        Log.e(LOG_TAG, "Not found speed state");
                    }
                } else {
                    isSpeedExist = true;
                    break;
                }
            }

            return isSpeedExist;
        }


        @Override
        public void run() {
            try {
                if (!checkMqttConnection()) {
                    mConnectionProblemDialog.show();
                    return;
                }
                // Subscribe information
                subscribeDeviceInfo();
                // Subscribe state of devices
                subscribeDeviceStates();


                mqttClient.requestDeviceInfos();
                // Check information
                if (!checkRemoteDeviceInfo()) {
                    mInfoDeviceProblemDialog.show();
                    return;
                }

                // Request state of device
                mqttClient.requestAWSIotServer("checkpower-" + getSmartPlugId(), AirPurifierTopics.REQUEST_STATE_POWER);
                mqttClient.requestAWSIotServer("checkspeed-" + getSmartPlugId(), AirPurifierTopics.REQUEST_STATE_SPEED);

               if (!(checkPowerDevice() && checkSpeedDevice())) {
                   mSmartPlugProblemDialog.show();
                   return;
               }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Getter and setter
    public MqttClientToAWS getMqttClient() {
        return mqttClient;
    }


    public HashMap<String, RemoteDevice> getDeviceList() {
        return deviceList;
    }


    public HashMap<String, String> getStateList() {
        return stateList;
    }


    public String getRemoteDeviceId() {
        return deviceList.get(KeyOfDevice.REMOTE.getValue()).getDeviceId();
    }


    public String getSmartPlugId() {
        return deviceList.get(KeyOfDevice.SMART_PLUG.getValue()).getDeviceId();
    }


    public int getSpeed() {
        String speedStr = stateList.get(KeyOfStates.SPEED.getValue());
        int speed = -1;
        if (speedStr != null) {
            speed = Integer.parseInt(speedStr);
        }
        return speed;
    }


    public PowerState getPower() throws NullPointerException {
        PowerState retVal = PowerState.NULL;
        String powerStr = stateList.get(KeyOfStates.POWER.getValue());
        if (powerStr != null) {
            if (powerStr.equals(PowerState.ON.getValue())) {
                retVal = PowerState.ON;
            } else {
                retVal = PowerState.OFF;
            }
        }

        return retVal;
    }






}
