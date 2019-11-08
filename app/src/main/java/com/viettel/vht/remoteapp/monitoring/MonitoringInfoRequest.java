package com.viettel.vht.remoteapp.monitoring;

import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.widget.TextView;

import com.viettel.vht.remoteapp.common.APILink;
import com.viettel.vht.remoteapp.common.Constants;




import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


import javax.net.ssl.HttpsURLConnection;


public class MonitoringInfoRequest extends AsyncTask<String, Void, String> {
    private HttpsURLConnection connection;
    private ArrayList<TextView> views = new ArrayList<TextView>();

    private String url;

    public MonitoringInfoRequest(TextView... views) {
        int length = views.length;
        for (int i = 0; i < length; i++) {
            this.views.add(views[0]);
        }

        // default url
        url = APILink.apiNodesURL;
    }


    @Override
    protected String doInBackground(String... strings) {
        String result = "Cannot get information";
        connection = connectToNetwork(url);
        try {

            if (connection != null) {
                // Write data to node
                connection.getOutputStream().write(new String("{\"node_id\":\"" + Constants.DEVICE_ID + "\", \"includes\":\"data_points\" }").getBytes());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = readStream(in);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }


    @Override
    protected void onPostExecute(String result) {
        views.get(0).setText(result);
    }


    /**
     *
     * @param host
     * @return
     */
    private HttpsURLConnection connectToNetwork(String host) {
        HttpsURLConnection urlConnection = null;
        try {
            // Send data to server
            URL url = new URL(host);
//    		 URL url = new URL("https://www.google.com");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");

            // Set POST connection
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer c4965f7b-7729-46a3-a389-5ede263b3440");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(2000);

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return urlConnection;
    }

    private String readStream(InputStream reader) throws Exception {
        StringBuilder sbResult = new StringBuilder("Result: ");
        byte[] buffer = new byte[Constants.BUFFER_SIZE];
        int readNum = 0;

        do {
            // Read input stream to array
            readNum = reader.read(buffer);
            // Append to result
            if (readNum == -1) {
                break;
            }
            sbResult.append(new String(buffer, 0, readNum));
        }while(readNum != 0);

        return sbResult.toString();
    }

    // Setter and getter
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
