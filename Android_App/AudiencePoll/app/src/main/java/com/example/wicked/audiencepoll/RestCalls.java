package com.example.wicked.audiencepoll;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.wicked.audiencepoll.Constants.BROADCAST_NAME;
import static com.example.wicked.audiencepoll.Constants.DEVICE_ID;
import static com.example.wicked.audiencepoll.Constants.ID_LIST;
import static com.example.wicked.audiencepoll.Constants.KEY_ID;
import static com.example.wicked.audiencepoll.Constants.KEY_NAME;
import static com.example.wicked.audiencepoll.Constants.NAME_LIST;
import static com.example.wicked.audiencepoll.Constants.VOTINGS_SUBMITTED;

/**
 * Created by wicked on 1/17/18.
 */

public class RestCalls {
    private Context context;
    private String url;
    private ArrayList names;
    private ArrayList ids;

    public RestCalls(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    public void getNameList() {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        names = new ArrayList();
        ids = new ArrayList();
        final Intent lists = new Intent();
        final Bundle d = new Bundle();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {

                    JSONArray nameArray = response.getJSONArray(NAME_LIST);
                    JSONArray idArray = response.getJSONArray(ID_LIST);
                    for (int i = 0; i < nameArray.length(); i++) {
                        names.add(nameArray.getString(i));
                        ids.add(idArray.getString(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error:" + e.toString(), Toast.LENGTH_LONG).show();
                }


                d.putStringArrayList(KEY_NAME, names);
                d.putStringArrayList(KEY_ID, ids);
                lists.setAction(BROADCAST_NAME);

                lists.putExtras(d);
                context.sendBroadcast(lists);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                d.putStringArrayList(KEY_NAME, names);
                d.putStringArrayList(KEY_ID, ids);
                lists.setAction(BROADCAST_NAME);
                lists.putExtras(d);

                context.sendBroadcast(lists);
            }
        });

        queue.add(jsonObjectRequest);

    }


    public void postPolls(Context c, float rate) {

        final Intent votings_submitted = new Intent();
        final Bundle bundle_data = new Bundle();
        RequestQueue postque = Volley.newRequestQueue(context);

        JSONObject data = new JSONObject();
        try {
            data.put("mobileid",DEVICE_ID);
            data.put("rate",rate);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    int result = response.getInt("status");

                    bundle_data.putString("message",message);
                    bundle_data.putInt("status",result);


                    votings_submitted.setAction(VOTINGS_SUBMITTED);
                    votings_submitted.putExtras(bundle_data);

                    context.sendBroadcast(votings_submitted);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        postque.add(jsonObjectRequest);

    }
}
