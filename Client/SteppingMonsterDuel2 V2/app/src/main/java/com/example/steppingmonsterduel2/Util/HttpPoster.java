package com.example.steppingmonsterduel2.Util;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This convenience class is used to handle asynchronous 
 * HTTP-POST messages to the webservice.
 * 
 * @author Niklas Klügel
 *
 */

public class HttpPoster extends AsyncTask<String, Void, String> {
	private String serverUrl = "";
	private String httpPostResult;
	
	public HttpPoster(String url) {
		this.serverUrl = url;
	}
	
	// Uses the default url specified in the Configuration class 
	public HttpPoster(){
		this(Configuration.ServerURL);
	}

    @Override
    protected String doInBackground(String... params)
    {
        BufferedReader inBuffer = null;
        String url = this.serverUrl;
        String result = "fail";
        
        for(String parameter: params) {
        	url = url + "/" + parameter;
        }                
        
        try {
            // https://stackoverflow.com/questions/31433687/android-gradle-apache-httpclient-does-not-exist
            URL poster = new URL(url);
            Log.d("INFORMATION","URL: " + url);
            HttpURLConnection connection = (HttpURLConnection) poster.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            result = connection.getResponseMessage();
            Log.d("INFORMATION","Poster returned " + result);

        } catch(Exception e) {

            Log.e("INFORMATION","Post failed with " + e);
            Log.e("INFORMATION", Arrays.toString(e.getStackTrace()));
            /*
             * some useful exception handling should be here 
             */
        } finally {
            if (inBuffer != null) {
                try {
                    inBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  result;
    }
    
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }


    protected void onPostExecute(String result) {
    	// this is used to access the result of the HTTP GET lateron
    	httpPostResult = result;
    }   
    
    /**
     * Returns the result of the operation if needed lateron.
     * @return
     */
    
    public String getResult(){
    	return httpPostResult;
    }

    /**
     * Sends a POST request in the form of params to the server.
     * If the server responds with "OK", returns true.
     * If the server responds with "Bad Request", returns false.
     * If the server responds with something else, passes a WeirdServerResponseException to onFailure. Then returns false.
     * If server communication fails, passes the Exception to onFailure. Then returns false.
     * If onFailure is null, the exception just gets swallowed.
     */
    public static boolean safePost(@Nullable OnFailureListener onFailure, String... params){
        HttpPoster poster = new HttpPoster();
        poster.execute(params);
        try {
            String result = poster.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            if(result.equals("OK")) return true;
            if(result.equals("Bad Request")) return false;
            else throw new WeirdServerResponseException("Weird response got: "+result, result);
        } catch (InterruptedException |ExecutionException | TimeoutException |WeirdServerResponseException e) {
            if(onFailure != null) onFailure.onFailure(e);
            return false;
        }
    }
    //Converts params to Strings, then calls the method with the same name
    public static boolean safePost(@Nullable OnFailureListener onFailure, Object... params){
        String[] strings = new String[params.length];
        for(int i=0; i<params.length; i++){
            strings[i] = params[i].toString();
        }
        return safePost(onFailure, strings);
    }

    //Call this if you like a quick fuck. It throws RuntimeException if server communication fails, so it's definitely unsafe.
    public static boolean quickPost(String... params) throws RuntimeException {
        return safePost((e)->{throw new RuntimeException("HttpPoster#QuickPost failed:\n"+e);}, params);
    }
    public static boolean quickPost(Object... params) throws RuntimeException {
        return safePost((e)->{throw new RuntimeException("HttpPoster#QuickPost failed:\n"+e);}, params);
    }

    public static class WeirdServerResponseException extends Exception{
        private final String serverResponse;
        private WeirdServerResponseException(String message, String serverResponse){
            this.serverResponse = serverResponse;
        }
        public String getServerResponse(){
            return serverResponse;
        }
    }
}  