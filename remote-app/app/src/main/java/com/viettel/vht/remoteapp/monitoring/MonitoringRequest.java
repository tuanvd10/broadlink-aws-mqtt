package com.viettel.vht.remoteapp.monitoring;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.viettel.vht.remoteapp.common.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MonitoringRequest {
    private RequestQueue requestQueue;
    private Context ctx;

    public MonitoringRequest(Context ctx) {
        this.ctx = ctx;
        requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public void jsonPostRequest(String url, final JSONObject jsonObject, Response.Listener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //txtDebug.setText("Error: " + error.toString());
            }



        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + Constants.API_TOKEN);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        requestQueue.add(stringRequest);
    }


    public void testRequest(final TextView txtDebug) {
        String url = "https://api.environet.io/search/data_points";
        Map<String, String> postParam;
        final JSONObject json = new JSONObject();
        try {
            json.put("last", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //JSONObject jsObj = new JSONObject(response.getString(10));
                            // String result = jsObj.getString("_id");
                            //
                            //  json = new JSONArray(response);
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(10));
                            String measStr = new JSONArray(jsonObject.getString("measurements")).toString();
                            measStr = measStr.replace('[', '\0');
                            measStr = measStr.replace(']', '\0');
                            txtDebug.setText("Response: " + measStr.split(",")[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtDebug.setText("Error: " + error.toString());
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer fb5fb49e-2cde-44a6-90c0-8110df215fb6");
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        requestQueue.add(stringRequest);
    }


}
