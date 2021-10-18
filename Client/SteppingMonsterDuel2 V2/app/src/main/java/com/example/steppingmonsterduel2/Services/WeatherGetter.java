package com.example.steppingmonsterduel2.Services;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class WeatherGetter {

    Location mLocation;
    RequestQueue mRequestQueue;
    Weather mWeather;
    final String TAG = "WeatherGetter";
    final String URL = "https://api.openweathermap.org/data/2.5/weather?";
    final String APIKEY = "Weather API Key";

    public WeatherGetter(Location location, Context context){
        this.mLocation = location;
        this.mWeather = Weather.getInstance();
        mRequestQueue = Volley.newRequestQueue(context);
    }

    //weather server request, a Weather object will then take care of parsing the json response
    public void weatherRequest(){
        if (mLocation == null){
            Log.e(TAG, "Location is null");
            return;
        }


        String WeatherRequest = URL +"lat=" + mLocation.getLatitude() +"&lon=" + mLocation.getLongitude() + "&appid=" + APIKEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, WeatherRequest, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response gotten ! " + response.toString());
                mWeather.parseWeather(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d(TAG, "error on Response: "  + error.getMessage());
            }
        }
        );

        mRequestQueue.add(jsonObjectRequest);

    }

}
