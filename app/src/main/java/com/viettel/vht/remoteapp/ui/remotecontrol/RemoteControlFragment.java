package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.viettel.vht.remoteapp.R;
import com.viettel.vht.remoteapp.common.MitsubishiFanTopics;

import java.security.KeyStore;
import java.util.UUID;

public class RemoteControlFragment extends Fragment {
    private RemoteControlViewModel remoteControlViewModel;

    private final String LOG_TAG = RemoteControlFragment.class.getCanonicalName();
    String clientId;

    // Button
    Button mPowerButon, mSwingButton, mTimerButton, mRhythmButton, mSpeedButton;
    // Text View
    TextView mStatus, mNameRemoteControl;

    // --- Constants to modify per your configuration ---

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a3oosh7oql9nlc-ats.iot.us-east-1.amazonaws.com";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "broadlink-Policy";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";

    // Mqtt client
    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;

    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(this.getClass().getName(), "Create view");
        remoteControlViewModel = ViewModelProviders.of(this).get(RemoteControlViewModel.class);

        View root = inflater.inflate(R.layout.fragment_remote_control, container, false);
        // Set name of remote control
        mNameRemoteControl = root.findViewById(R.id.tv_remote_control);
        mNameRemoteControl.setText(R.string.fan_mitsubishi);

        // Get status of remote control
        mStatus = root.findViewById(R.id.tv_status);
        mStatus.setText(R.string.connecting_status);
        // Set remote control view model
        remoteControlViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(LOG_TAG, "Have a change in remote control view model");
            }
        });

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();
        // Connect to aws iot server
        AWSMobileClient.getInstance().initialize(getActivity(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                initIotClient();
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "onError: ", e);
            }
        });

        // Get button from id and set on click listener
        // Power
        mPowerButon = root.findViewById(R.id.bt_power);
        mPowerButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFanRemoteButton(v);
            }
        });
        // Speed
        mSpeedButton = root.findViewById(R.id.bt_speed);
        mSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFanRemoteButton(v);
            }
        });
        // Rhythm
        mRhythmButton = root.findViewById(R.id.bt_rhythm);
        mRhythmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFanRemoteButton(v);
            }
        });
        // Swing
        mSwingButton = root.findViewById(R.id.bt_swing);
        mSwingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFanRemoteButton(v);
            }
        });
        // Timer
        mTimerButton = root.findViewById(R.id.bt_timer);
        mTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFanRemoteButton(v);
            }
        });

        // Return view
        return root;
    }


    private void initIotClient() {
        // Get region
        Region region = Region.getRegion(MY_REGION);

        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);
        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament(MitsubishiFanTopics.POWER.getvalue(), "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
        mIotAndroidClient.setRegion(region);

//        keystorePath = getFilesDir().getPath();
        keystorePath = getActivity().getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        // TODO: command this try block because we use unauthen
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    /* initIoTClient is invoked from the callback passed during AWSMobileClient initialization.
                    The callback is executed on a background thread so UI update must be moved to run on UI Thread. */
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            btnConnect.setEnabled(true);
//                        }
//                    });

                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }


        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                btnConnect.setEnabled(true);
//                            }
//                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }

        // Connect to aws server
        Log.d(LOG_TAG, "clientId = " + clientId);

        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    // TODO if status is "lost connect" or "unconnect", it must show error to user and suggest user choose "reconnect" or "contact to admin" or "close"
                    // Change connected status in remote control
                    if (String.valueOf(status).equals("Connected")) {
                        mStatus.setText(R.string.connected_status);
                    }

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (throwable != null) {
//                                Log.e(LOG_TAG, "Connection error. ", throwable);
//                            }
//                        }
//                    });
                }
            });

        } catch (Exception e) {
            Log.e(LOG_TAG, "Connection error. ", e);
        }
    }

    private void disconnect() {
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }

    public void clickFanRemoteButton(View view) {
        String msg = "";
        MitsubishiFanTopics topic = null;
        // Check button
        switch(view.getId()) {
            case R.id.bt_power :
                msg = "play";
                topic = MitsubishiFanTopics.POWER;
                break;
            case R.id.bt_speed:
                msg = "speed up";
                topic = MitsubishiFanTopics.SPEED;
                break;
            case R.id.bt_swing:
                msg = "swing";
                topic = MitsubishiFanTopics.SWING;
                break;
            case R.id.bt_rhythm:
                msg = "rhythm in the rain";
                topic = MitsubishiFanTopics.RHYTHM;
                break;
            case R.id.bt_timer:
                msg = "set a turn off time";
                topic = MitsubishiFanTopics.TIMER;
                break;
            default:
                Log.e(LOG_TAG, "Cannot detect button id " + view.toString());
                return;
        }
        // Publish message to aws server
        try {
            mqttManager.publishString(msg, MitsubishiFanTopics.POWER.getvalue(), AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }
}
