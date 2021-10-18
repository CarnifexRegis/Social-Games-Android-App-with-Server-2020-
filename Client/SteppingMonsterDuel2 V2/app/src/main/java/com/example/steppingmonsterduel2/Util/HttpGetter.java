package com.example.steppingmonsterduel2.Util;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This convenience class is used to handle asynchronous 
 * HTTP-GET messages to the webservice.
 * 
 * @author Niklas Klügel
 *
 */

public class HttpGetter extends AsyncTask<String, Void, String> {
	private String serverUrl = "";

	public HttpGetter(String url) {
		this.serverUrl = url;
	}
	
	// Uses the default url specified in the Configuration class 
	public HttpGetter(){
		this(Configuration.ServerURL);
	}
	
	private String httpGetResult = "";

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
            URL getter = new URL(url);
            final HttpURLConnection aHttpURLConnection = (HttpURLConnection) getter.openConnection();
            result = convertInputStreamToString(aHttpURLConnection.getInputStream());
            Log.d("INFORMATION","Getter returned " + result);
        } catch(Exception e) {
            /*
             * some exception handling should take place here
             */

            Log.e("INFORMATION","Getter got " + e);

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
    	httpGetResult = result;
    }   
    
    /**
     * Returns the result of the operation if needed lateron.
     * @return
     */
    public String getResult(){
    	return httpGetResult;
    }

    /**
     * Sends a GET request in the form of params to the server and returns the response.
     * If server communication fails, passes the Exception to onFailure, then returns null.
     * If onFailure is null, the exception gets swallowed.
     */
    public static String safeGet(@Nullable OnFailureListener onFailure, String... params){
        HttpGetter getter = new HttpGetter();
        getter.execute(params);
        try {
            return getter.get(Configuration.timeoutTime, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if(onFailure != null) onFailure.onFailure(e);
            return null;
        }
    }
    //Converts the passed objects into Strings, then calls the method with the same name
    public static String safeGet(@Nullable OnFailureListener onFailure, Object... params){
        String[] strings = new String[params.length];
        for(int i=0; i<params.length; i++){
            strings[i] = params[i].toString();
        }
        return safeGet(onFailure, strings);
    }
    //Call this if you like a quick fuck. It throws a RuntimeException if server communication fails, so it's definitely unsafe.
    public static String quickGet(String... params) throws RuntimeException {
        return safeGet((e)->{throw new RuntimeException("HttpGetter#QuickGet failed:\n"+e);}, params);
    }
    public static String quickGet(Object... params) throws RuntimeException {
        return safeGet((e)->{throw new RuntimeException("HttpGetter#QuickGet failed:\n"+e);}, params);
    }
}