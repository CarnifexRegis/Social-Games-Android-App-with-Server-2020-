package com.example.steppingmonsterduel2.Util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.example.steppingmonsterduel2.LoginActivity;
import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.R;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.LocationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Created by Simon on 01.03.2018.
 */

public class Configuration {

    //Server URL to connect to the Play! Framework
    //public static String ServerURL= "http://192.168.2.115:9000";
    //public static String ServerURL= "http://192.168.178.26:9000";
    //houssein address : http://192.168.0.2:9000 / Yann :http://192.168.188.46:9000
    private static final String HOUSSEIN_ADDRESS = "http://192.168.0.2:9000";
    private static final String YANN_ADDRESS = "http://192.168.178.33:9000";
    private static final String MAXI_ADDRESS = "http://192.168.178.31:9000";
    private static final String SIMON_ADRESS = "http://192.168.56.1:9000";
    public static String ServerURL=HOUSSEIN_ADDRESS;
    public static final String locationService = ".Services.LocationService";
    public static FirebaseAuth mAuth;
    public static FirebaseUser fbUser;
    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static  List<String> list = Arrays.asList("ServerTimeout: Please login again!","Interrupt occured: Please login again!",
            "Execution occured: Please login again!","Data could not be read: Please login again!","ID not set");
    public static int timeoutTime = 5;
    public static int REFRESH_TIME = 1000;
    public static final int REFRESH_TIME_SLOW = 10000;
    public static User currentUser = null;
    //User cards
    public static PlayerCardList CARDS;

    //0 timeout
    //1 interruptexception
    //2 executionexception
    //3 json
    //4 id not set
    public static void serverDownBehaviour(Context context,int number,boolean isActivity){
        String reason;
        switch(number){
            case 0:
                reason = "Timeout";
                break;
            case 1:
                reason = "InterruptedException";
                break;
            case 2:
                reason = "ExecutionException";
                break;
            case 3:
                reason = "JSON";
                break;
            case 4:
                reason = "ID not set";
                break;
            default :
                reason = "No Idea what happened :/";
        }

        System.out.println("Server communication failed: "+reason);
        mAuth.signOut();
        fbUser=null;
        if (LocationService.startedLocationTracking)
            context.stopService(new Intent(context, LocationService.class)); // TEST
        Intent intent = new Intent( context, LoginActivity.class );
        if(isActivity)
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity( intent );
        Toast.makeText( context, list.get( number ), Toast.LENGTH_SHORT ).show();
    }


    //sets an image to the view out of firebase storage
    public static void getPictureOutOfStorageAndSetItToView(final Context context, final ImageView view, String picture){
        Consumer<Bitmap> whatToDoWithBitmap = view::setImageBitmap;
        OnFailureListener onFail = (e)->{
            //Toast.makeText(context, "Pictures cannot be loaded!", Toast.LENGTH_LONG).show();
            view.setImageResource(R.drawable.standart);
        };
        getPictureOutOfStorageAnd(picture, whatToDoWithBitmap, onFail);
    }

    //bissl mächtiger als die obere Methode
    public static void getPictureOutOfStorageAnd(String picture, Consumer<Bitmap> whatToDoWithBitmap, OnFailureListener onFailure){
        //convert string back to normal string
        picture = picture.replace(',','/');
        //set new reference
        StorageReference storageRef = storage.getReference();
        //search the image in firebase storage and convert it into bitmap
        OnSuccessListener<byte[]> onSuccess = (bytes)->{
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            whatToDoWithBitmap.accept(bitmap);
        };
        storageRef.child(picture).getBytes(Long.MAX_VALUE).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    //defines the playercards
    public static class PlayerCardList implements Iterable<Pair<Integer, PlayerCard>>{
        private final SparseArray<PlayerCard> playerCards;
        private int nextID;

        public PlayerCardList(){
            this.playerCards = new SparseArray<>();
            this.nextID = 0;
        }
        //add a playercard
        public int add(PlayerCard card){ //returns the playerCardID of the added card.
            int result = card.getCardID();
            playerCards.put(card.getCardID(), card);
            return result;
        }

        //gets a playeercard
        public PlayerCard get(int playerCardID){
            PlayerCard result = playerCards.get(playerCardID);
            if(result == null) {
                System.out.println("card not found");
            }
            return result;
        }

        //removes a playercard
        public void remove(int playerCardID){
            if(playerCards.get(playerCardID) == null) throw new IllegalArgumentException("There was no Player Card with ID "+playerCardID);
            playerCards.remove(playerCardID);
        }

        @NonNull
        @Override
        public Iterator<Pair<Integer, PlayerCard>> iterator() {
            return new SparseArrayIterator<PlayerCard>(playerCards);
        }
    }
    public static class SparseArrayIterator<T> implements Iterator<Pair<Integer, T>> {

        final SparseArray<T> list;
        private int currentIdx;
        public SparseArrayIterator(SparseArray<T> list){
            this.list = list;
            currentIdx = 0;
        }

        @Override
        public boolean hasNext() {
            return currentIdx < list.size();
        }

        @Override
        public Pair<Integer, T> next() {
            Pair<Integer, T> result = new Pair<>(list.keyAt(currentIdx), list.valueAt(currentIdx));
            currentIdx++;
            return result;
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException("Maxi hasn't implemented this."); //TODO maybe. Tell me if you ever end up needing this
        }
    }
}
