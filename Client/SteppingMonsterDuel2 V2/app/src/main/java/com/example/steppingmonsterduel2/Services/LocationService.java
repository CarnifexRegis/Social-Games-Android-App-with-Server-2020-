package com.example.steppingmonsterduel2.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.steppingmonsterduel2.OpenPacksActivity;
import com.example.steppingmonsterduel2.R;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/*
 Background service that deals with step calculations
 Code inspired by the android studio tutorials
 */
public class LocationService extends Service {

    private static final String TAG = "LocationService";

    //for the notification
    private final int NOTIFICATION = 101;
    private final String CHANNEL_ID = "channel_01";
    private final int STEPS_MILESTONE = 3000; //milestone for drops
    private NotificationManager mNotificationManager;

    //Attributes necessary for initialising the steps tracking
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Notification.Builder notificationBuilder;
    public static String[] notificationText; // notification text that needs to be displayed ( when we get a pack this gets added
    protected SettingsClient mSettingsClient;
    protected boolean notLeaving = false;
    protected Notification notification;
    protected int stepsToMilestone = -1;

    //Weather getter class
    protected WeatherGetter mWeatherGetter = null;
    protected final int WEATHER_UPDATE_INTERVAL = 50;
    protected int weatherUpdateIntervalCounter = 0;

    public static boolean startedLocationTracking = false;

    //thread Parameter
    private Handler mHandler;
    private boolean mBound = false;
    //Save old location to calculate distance run and translate it to steps
    private Location lastLocation;

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    //binder (for binding with homeActivity)
    private final IBinder mBinder = new LocalBinder();


    @Override
    public void onCreate() {
        //initialising parameters
        notificationText = new String[3];
        notificationText[0] = ""; // pack drop
        notificationText[1] = ""; // number of steps
        notificationText[2] = ""; // weather
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationCallback();
        initLocationRequest();

        //Make this service use a different thread than the main thread
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());


        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // for newer version of androids a notification channel is also necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{0L});
            mNotificationManager.createNotificationChannel(mChannel);
        }
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();

        //a server request to get the actual steps
        serverRequest(0.0f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    //disable the server on destroy
    @Override
    public void onDestroy() {
        startedLocationTracking = false;
        mNotificationManager.cancel(NOTIFICATION);
        StopStepsTracking();
        mHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Stopping and destroying the service");

    }

    //formats the actual buffed Monsters to string
    private String getElementString(){
        if (Weather.getInstance().getBuffedAndDroppableElement() == null){
            return "None";
        }
        return Weather.getInstance().getBuffedAndDroppableElement().name;
    }

    //UPdate the notification when user gets extra steps / pack drop
    private void updateNotification(){
        if (notification == null || notificationBuilder == null){
            Log.e(TAG, "Null pointer error for the notification");
            return;
        }

        notificationText[1] = "Steps : " + UIUpdater.getInstance().getSteps();
        notificationText[2] = "Empowered Element : " + getElementString();
        //if we have a pack drop adapt
        if (!notificationText[0].equals("")) {
            Intent notificationIntent = new Intent(getApplicationContext(), OpenPacksActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(notificationIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(pendingIntent);
            notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(notificationText[0] + "\n" + notificationText[1] + "\n" + notificationText[2]));
            notificationBuilder.setContentText(notificationText[0]);
        }else{
            notificationBuilder.setContentText(notificationText[1] + " | " + notificationText[2]);
            notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(notificationText[1] + "\n" + notificationText[2]));
        }

        notification = notificationBuilder.build();
        if (!mBound)
            mNotificationManager.notify(NOTIFICATION, notification);
        Log.d(TAG, "Notification updated");
    }
    //shows a notification on the status bar that the Steps Tracking is enabled
    private void showNotification() {

        // Set the info for the views that show in the notification panel.
        notificationBuilder = new Notification.Builder(this)
                .setTicker("Steps tracking")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{0L})
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText("Steps : " + UIUpdater.getInstance().getSteps()) ; // the contents of the entry


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID); // Channel ID
        }

        notification = notificationBuilder.build();
        Log.d(TAG, " Got the notification generated ! " + notification);
        // Send the notification.
        //mNotificationManager.notify(NOTIFICATION, notification);

    }

    //Builds the location request depending on the settings
    private void initSettingsRequestBuilder(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //options for location updates speed/precision
    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(12000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*
    Most important location service part :
    -Defines behaviour when service gets a new location => sends the new location latitude longtitude to server to calculate walked steps meanwhile
    and returns them to the user
    -detects changes in the location availability => handle behaviour when user turns off his location services
     */
    protected void initLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location currentLocation = locationResult.getLastLocation();
                if (lastLocation != null){
                    float distance = lastLocation.distanceTo(currentLocation);
                    //PackDrop();  // enable this for opening pack debugging
                    serverRequest(distance);

                    updateNotification();
                    //notificationManager.notify(id, builder.build());
                }
                lastLocation = currentLocation;
                if (mWeatherGetter == null && lastLocation != null){
                    mWeatherGetter = new WeatherGetter(lastLocation, getApplicationContext());
                    weatherUpdateIntervalCounter = 0;
                    mWeatherGetter.weatherRequest();

                } else if (lastLocation != null && weatherUpdateIntervalCounter>=WEATHER_UPDATE_INTERVAL){
                    weatherUpdateIntervalCounter = 0;
                    mWeatherGetter.weatherRequest();
                } else {
                    weatherUpdateIntervalCounter++;
                }
            }
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (!locationAvailability.isLocationAvailable()){
                    UIUpdater.getInstance().updateButtonColor(2);
                } else{
                    UIUpdater.getInstance().updateButtonColor(0);
                }
            }
        };
    }

    //server request sends the distance that was moved in a time period and server translates it to steps and gives back the actual steps
    private int serverRequest(float distance) {
        HttpGetter getRequest = new HttpGetter();
        if(Configuration.currentUser == null){
            System.out.println("LocationService#serverRequest: Current User is null.");
            stopSelf();
            return 0;
        }
        getRequest.execute("update",""+Configuration.currentUser.getId(),""+distance,"Steps");
        try {
            JSONObject jsonObject = new JSONObject(getRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));
            int steps = jsonObject.getInt("Steps");

            if (stepsToMilestone != -1 && steps != -1){
                int stepDifference = steps - stepsToMilestone;
                if ((float)stepDifference / STEPS_MILESTONE >= 1.0){
                    Log.d(TAG, "we have a pack drop for milestone " + STEPS_MILESTONE + " we have " + (stepDifference/STEPS_MILESTONE));
                    //we have achieved a milestone we can move on
                    PackDrop();
                    stepsToMilestone = steps;
                }
            }
            else if (steps != -1){
                //steps milestone was not initialised
                stepsToMilestone = steps; // this is our starting point, after 3000 steps we will rechange this and drop a pack
            }


            if (steps == -1){
                Log.e(TAG, "Error while retrieving new steps");
            } else {
                UIUpdater.getInstance().setSteps(steps);
                Log.d(TAG, "Retrieved steps successfully total steps " + steps + " for distance " + distance);
            }

            return steps;

        } catch (JSONException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,3,false);
        } catch (ExecutionException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,2,false);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,1,false);
        } catch (TimeoutException e){
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,0,false);
        }

        return -1;
    }


    //Detect a pack drop ! user gets notified and server gets a request
    private void PackDrop(){
        Element buffedElement = Weather.getInstance().getBuffedAndDroppableElement();


        int packID;
        if(buffedElement == null){
            Log.e(TAG, "No weather was specified ! ");
            packID = -1;
            Log.e(TAG, "Unexpected pack id");
        }
        else packID = buffedElement.databaseID;

        try {
        //now server request to add the pack
        HttpGetter getRequest = new HttpGetter();
        getRequest.execute("add",""+Configuration.currentUser.getId(),""+packID,"Pack");


            JSONObject jsonObject = new JSONObject(getRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));
            boolean didItWork = jsonObject.getBoolean("Worked?");
            Log.d(TAG, "sent an add pack request to server and got " + didItWork);

            //add a pack of that type to the OpenPacksActivityStatic member
            OpenPacksActivity.numberOfPacks++;
            OpenPacksActivity.packsTypesStack.add(packID);

            //if it worked , change the notification and user can got to pack opening using it
            if (notificationBuilder != null){
                notificationText[0] = " You have a pack Drop !";
                updateNotification();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,3,false);
        } catch (ExecutionException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,2,false);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,1,false);
        } catch (TimeoutException e){
            e.printStackTrace();
            //Configuration.serverDownBehaviour(LocationService.this,0,false);
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.e(TAG, "user is null !");
        }

    }

    //start the service
    @SuppressLint("MissingPermission")
    public void startStepsTracking( ) {
        startService(new Intent(getApplicationContext(), LocationService.class));

        try{
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            startedLocationTracking = true;
        } catch (SecurityException e) {
            Log.e(TAG, "no permission ! ");
        }
        UIUpdater.getInstance().setGpsButtonEnabled(true);

    }

    public void doStopForground(boolean value){
        stopForeground(value);
    }

    //stop the service
    public void StopStepsTracking(){
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            startedLocationTracking = false;
            //stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "no permission ! ");
        } finally {
            //stopSelf();
        }
    }

    /*
    Making sure this service doesn't die : make sure it always stays in the forground
    (code inspired from the google samples on github)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind()");
        notificationText[0] = "";
        mBound = true;
        updateNotification();
        stopForeground(true);
        notLeaving = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
        notificationText[0] = "";
        mBound = true;
        updateNotification();
        stopForeground(true);
        notLeaving = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        if (!notLeaving) {
            Log.i(TAG, "Starting foreground service " + notification);

            startForeground(NOTIFICATION, notification);
        }
        mBound = false;
        updateNotification();
        return true;
    }

    //if the app is swiped and closed, removes the notification
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        stopSelf();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION);
    }


}
