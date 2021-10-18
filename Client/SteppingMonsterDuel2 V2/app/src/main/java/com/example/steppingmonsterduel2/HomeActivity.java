package com.example.steppingmonsterduel2;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.ButtonViews.ButtonEditCard;
import com.example.steppingmonsterduel2.ButtonViews.ButtonEditDeck;
import com.example.steppingmonsterduel2.ButtonViews.ButtonMatchmaking;
import com.example.steppingmonsterduel2.ButtonViews.FriendMenuButton;
import com.example.steppingmonsterduel2.ButtonViews.GPSButton;
import com.example.steppingmonsterduel2.ButtonViews.PoweroffButton;
import com.example.steppingmonsterduel2.ButtonViews.ShopBoosterButton;
import com.example.steppingmonsterduel2.Objects.Friends;
import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.Services.LocationService;
import com.example.steppingmonsterduel2.Services.UIUpdater;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private final int ACCESS_FINE_LOCATION_RESULT = 101;
    private int userID=-1;

    //for steps calculation and binding this activity with a service
    private TextView stepsText;
    private GPSButton gpsButton; //GPS BUTTON : RED -> no permissions / white -> no gps enabled / blue -> working as intended
    private LocationService mLocationService; //for communication with the steps tracking service
    private boolean boundWithService = false; //know if this activity has been successfully bound with the location service
    //permits to define reactions when the Activity either fails or successfully binds with the service
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "called on service connected ");
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mLocationService = binder.getService();
            if (!LocationService.startedLocationTracking)
                requestPermissions();
            boundWithService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "called on service disconnected ");
            mLocationService = null;
            boundWithService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        String firebaseID = Configuration.fbUser.getUid();
        //TODO what id fbUser is null?

        userID = getIDOrCreateNewUser(firebaseID);

        System.out.println("Homeactivity = "+firebaseID);
        //If id of user is not set; go to login activity
        if(userID==-1){
            Configuration.serverDownBehaviour(HomeActivity.this,4,true);
            return;
        }

        try {
            if(Configuration.currentUser==null) {
                setUserInformation( userID );
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(HomeActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(HomeActivity.this,1,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(HomeActivity.this,3,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(HomeActivity.this,0,true);
        }

        //Handle button presses here
        buttonPress();
        gpsButton.setEnabled(false);
    }

    /*
    Basic activity behaviours :
    -when this activity is started, bind it with the location Service (necessary for checking if the Service is started)
    -when this activity pauses, unbind
    -while this activity is in use, UIUpdater will update the steps text and gpsbutton state
     */

    @Override
    protected void onStart() {
        super.onStart();
        if (!boundWithService){
            bindService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.stepsText != null)
            UIUpdater.getInstance().addWatcher(this.stepsText);
        if (this.gpsButton != null) {
            UIUpdater.getInstance().setGpsButton(gpsButton);
            gpsButton.invalidate();
        }

    }

    @Override
    protected void onStop() {
        if (boundWithService)
            unbindService();
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        UIUpdater.getInstance().removeGpsButton();
        UIUpdater.getInstance().removeWatcher(this.stepsText);
    }

    private void setUserInformation(int id) throws ExecutionException, InterruptedException, JSONException, TimeoutException {
        //pull user info from server
        HttpGetter getUserInfo = new HttpGetter();
        String result = getUserInfo.execute(new String[]{"" + id, "getMyUserInformation"}).get(Configuration.timeoutTime, TimeUnit.SECONDS);
        JSONObject jsonObject = new JSONObject(result);

        Configuration.currentUser = new User(jsonObject.getInt("ID"), jsonObject.getInt("Steps"),
                jsonObject.getString("Name"), jsonObject.getInt("Wins"), jsonObject.getInt("Loses"), jsonObject.getString("Picture"));

        Log.d(TAG, "successfully pulled user info ");

        //pull user collection from server
        Configuration.CARDS = new Configuration.PlayerCardList();

        HttpGetter getCards = new HttpGetter();
        getCards.execute("getCards", "" + Configuration.currentUser.getId());

        String cardUser = getCards.get(Configuration.timeoutTime, TimeUnit.SECONDS);

        JSONObject json = new JSONObject(cardUser);
        JSONArray jsonCards = json.getJSONArray("Cards");

        for (int i = 0; i < jsonCards.length(); i++) {
            JSONObject card = jsonCards.getJSONObject(i);
            PlayerCard cardToAdd = new PlayerCard(card.getInt("Type"), card.getString("Picture"), card.getInt("Cid"));
            Configuration.CARDS.add(cardToAdd);
        }
        Log.d(TAG, "successfully pulled user collection ");

        //pull user unopened packs from server
        HttpGetter getPacksRequest = new HttpGetter();
        getPacksRequest.execute("get", "" + Configuration.currentUser.getId(), "Packs");

        JSONObject jsonPackResponse = new JSONObject(getPacksRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));

        JSONArray packTypes = jsonPackResponse.getJSONArray("PackTypes");

        OpenPacksActivity.packsTypesStack = new LinkedList<Integer>();
        OpenPacksActivity.numberOfPacks = packTypes.length();
        for (int i = 0; i < packTypes.length(); i++) {
            JSONObject pack = packTypes.getJSONObject(i);
            OpenPacksActivity.packsTypesStack.add(pack.getInt("Type"));
        }
        Log.d(TAG, "successfully pulled user packs ");
    }

    private int getIDOrCreateNewUser(String firebaseID){
        return getIDOrCreateNewUser(firebaseID, false);
    }
    //returns the user ID or -1 if something failed.
    private int getIDOrCreateNewUser(String firebaseID, boolean secondTry){
        String response = HttpGetter.safeGet(null, firebaseID, "id");
        if(response == null) return -1;
        else try {
            JSONObject jsonObject = new JSONObject(response);
            int receivedID = jsonObject.getInt("ID");
            if(receivedID == -1) { //That's the error code the server sends if no user with this ID exists...
                if(secondTry) return -1; //don't do infinite recursion.
                //...so we create a new user!
                if(HttpPoster.safePost(null, "insert", Configuration.fbUser.getUid(), Configuration.fbUser.getDisplayName(), Configuration.fbUser.getEmail(), "user")){
                    return getIDOrCreateNewUser(firebaseID, true);
                }
                else {
                    System.out.println("Failed to create a new user on the game server.");
                    return -1;
                }
            }
            else return receivedID; //all is well; the user existed on the game server
        } catch (JSONException e) {
            System.out.println("Error parsing JSONObject:\t"+e);
            return -1;
        }
    }

    private void buttonPress(){
        final ButtonEditCard changeCards = findViewById(R.id.ChangeCardPicture);
        final ButtonEditDeck editDeck = findViewById(R.id.editDeck);
        final PoweroffButton logout = findViewById(R.id.Logout);
        final ButtonMatchmaking matchmaking = findViewById(R.id.Matchmaking);
        final ShopBoosterButton shopBooster = findViewById(R.id.ShopBooster);
        final FriendMenuButton friendList = findViewById(R.id.FriendMenu);
        stepsText = findViewById(R.id.StepCount);
        gpsButton = findViewById(R.id.GPS);




            changeCards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeCards.setClicked(true);
                    changeCards.invalidate();
                    Intent editCards = new Intent(HomeActivity.this,EditCardsActivity.class);
                    startActivity(editCards);
                }
            });

            editDeck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editDeck.setClicked(true);
                    editDeck.invalidate();
                    Intent editDeck = new Intent(HomeActivity.this,EditDeckActivity.class);
                    startActivity(editDeck);
                }
            });


            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout.setClicked(true);
                    logout.invalidate();
                    Configuration.mAuth.signOut();
                    Configuration.fbUser = null;
                    Configuration.currentUser = null;
                    if (LocationService.startedLocationTracking)
                        stopService(new Intent(HomeActivity.this, LocationService.class)); // TEST
                    Intent toLogin = new Intent(HomeActivity.this,LoginActivity.class);
                    startActivity(toLogin);
                }
            });


            matchmaking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    matchmaking.setClicked(true);
                    matchmaking.invalidate();
                    Intent matchmaking = new Intent(HomeActivity.this,MatchmakingActivity.class);
                    startActivity(matchmaking);
                }
            });

            shopBooster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shopBooster.setClicked(true);
                    shopBooster.invalidate();
                    Intent shopBooster = new Intent(HomeActivity.this, OpenPacksActivity.class);
                    startActivity(shopBooster);
                }
            });

            friendList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendList.setClicked(true);
                    friendList.invalidate();
                    Intent friendIntent = new Intent(HomeActivity.this,FriendList.class);
                    startActivity(friendIntent);
                }
            });

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!boundWithService){
                    Toast toast = Toast.makeText(getApplicationContext(), "Couldn't bind to the Location service", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (mLocationService != null && !LocationService.startedLocationTracking){
                    requestPermissions();
                }
                else if (!isLocationEnabled()){
                    Toast toast = Toast.makeText(getApplicationContext(), "please enable location services", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    GPSButton.IS_GPS = 0;
                    gpsButton.invalidate();
                    Toast toast = Toast.makeText(getApplicationContext(), "Steps tracking is functionnal", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    //Request location permission, if gotten start the step tracking from the service
    private void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_RESULT);
        }
        else{
            mLocationService.startStepsTracking();
        }
    }

    //called when user accepts or refuses permissions, consequences
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_RESULT: {

                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission denied");
                    GPSButton.IS_GPS = 1;
                    gpsButton.invalidate();
                    gpsButton.setEnabled(true);
                } else {
                    Log.i(TAG, "permission given !");
                    mLocationService.startStepsTracking();
                }
                return;
            }
        }
    }

    //Binding with the service
    void bindService(){
        if (bindService(new Intent(this, LocationService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE)) {
            boundWithService = true;
            Log.d(TAG, "bound successfully");
        } else {
            Log.e(TAG, "couldn't bind the Location service");
            GPSButton.IS_GPS = 3;
            gpsButton.setEnabled(true);
        }
    }

    //unbind the service when the activity is paused
    void unbindService(){
        if (boundWithService){
            unbindService(mServiceConnection);
            boundWithService = false;
        }
    }
    //checks if any location service is enabled
    public boolean isLocationEnabled(){
        LocationManager locationManager = null;
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try{
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception e){
            Log.d(TAG, "error when trying to check : gps provider ");
        }

        try{
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception e){
            Log.d(TAG, "error when trying to check : network provider ");
        }

        Log.d(TAG, "gps enabled " + gpsEnabled);
        Log.d(TAG, "networkEnabled enabled " + networkEnabled);


        return gpsEnabled || networkEnabled;
    }
}
