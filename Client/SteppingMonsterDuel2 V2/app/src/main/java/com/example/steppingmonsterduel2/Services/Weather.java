package com.example.steppingmonsterduel2.Services;



import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class Weather {
    private static Weather weatherSingleton = null;
    final String TAG = "Weather";
    private final long SUNSET_INTERVAL_SECONDS = 1800;
    protected Element buffedAndDroppableElement = null;

    /*
    Contain the parsed data from the api, what we will need is:
    - Temperature
    - Weather id (each type of weather has different variations)
    -sunrise/sunset times for twilight monsters
    -weather description (in case we need it for the UI, it's a unique description)
     */
    private String weatherDescription = "";
    private int weatherId;
    private double temperature;
    private long sunriseLong;
    private long sunsetLong;

    public static Weather getInstance(){
        if (weatherSingleton == null){
            weatherSingleton = new Weather();
            return weatherSingleton;
        }else{
            return weatherSingleton;
        }
    }

    //parses the Json object to get weather data
    public void parseWeather(JSONObject jsonObject){

        try {
            temperature = (jsonObject.getJSONObject("main").getDouble("temp")) - 273.15d; // from kelvin to celsius
            sunsetLong = jsonObject.getJSONObject("sys").getLong("sunset");
            sunriseLong = jsonObject.getJSONObject("sys").getLong("sunrise");
            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            weatherDescription = weatherArray.getJSONObject(0).getString("description");
            weatherId = weatherArray.getJSONObject(0).getInt("id");

            Log.d(TAG, "parsed temperature : " + temperature);
            Log.d(TAG, "parsed weather : " + weatherDescription);
            Log.d(TAG, "parsed sunrise : " + sunriseLong);
            Log.d(TAG, "parsed sunset : " + sunsetLong);

            SetActualMonsterBuffs();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //set the board buffs depending on various weathers
    private void SetActualMonsterBuffs(){
        long actualUnixTime = System.currentTimeMillis()/1000L;

        //First step is to see if we're 30 minutes before or after the sunset or sunrise
        //twilight is the only type that depends on the time period

        long sunriseDifference = Math.abs(actualUnixTime-sunriseLong);
        long sunsetDifference = Math.abs(actualUnixTime-sunsetLong);
        if ((sunriseDifference <= SUNSET_INTERVAL_SECONDS || sunsetDifference <= SUNSET_INTERVAL_SECONDS) || weatherId == 741 || weatherId == 721 || weatherId == 701){
            //only happens at sunset or sunrise (or when there is fog/haze/mist but rarely happens)
            buffedAndDroppableElement = Element.TWILIGHT;
            return;
        }

        //next type of weather decides, using weatherid
        /* https://openweathermap.org/weather-conditions
        ID 200-232 : thunderstorm
        ID 300-321 : drizzle
        ID 500-531 : rain
        ID 600-622 : snow
        ID 800 : clear
        ID 801-804 : clouds
        ID 701-784 : misc
         */
        if ((weatherId >= 200 && weatherId <= 232) || weatherId == 771 || weatherId == 781){
            //happens by thunderstorm (or squall or tornado but rare)
            buffedAndDroppableElement = Element.STORM;
        }
        else if (weatherId == 800 || (weatherId >= 801 && weatherId <= 803 && temperature > 20d) || weatherId == 711 || weatherId == 762){
            //inferno monster are either when skies are clear or there are clouds and relativly high temperature ( or misc : ash or smoke)
            buffedAndDroppableElement = Element.INFERNAL;
        } else if ((weatherId >= 600 && weatherId <= 622) || (weatherId >= 502 && weatherId <= 531) || (weatherId >= 302 && weatherId <= 321)){
            buffedAndDroppableElement = Element.TSUNAMI;
            //in snow or heavy rain and heavy drizzle
        } else if ((weatherId >= 801 && weatherId <= 804) || weatherId == 500 || weatherId == 501 || weatherId == 300 || weatherId == 301 || weatherId == 761 || weatherId == 731 || weatherId == 751){
            buffedAndDroppableElement = Element.EARTHQUAKE;
            //in clouds or weak rain or drizzle (or rarely : dust/sand)(otherwise storm would be too dominant)
        }
        else buffedAndDroppableElement = null;

    }

    public Element getBuffedAndDroppableElement(){
        return this.buffedAndDroppableElement;
    }
}

/*enum WEATHER_TYPES{
    CLEAR,
    CLOUDS,
    RAIN,
    SNOW,
    TUNDERSTORM,
    MIST,
    SMOKE,
    HAZE,
    DUST,
    FOG,
    SAND,
    ASH,
    SQUALL,
    TORNADO
}*/
