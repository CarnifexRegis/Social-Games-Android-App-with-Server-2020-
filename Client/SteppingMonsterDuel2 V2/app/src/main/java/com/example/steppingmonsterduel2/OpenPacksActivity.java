package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Services.LocationService;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class OpenPacksActivity extends AppCompatActivity {


    private ImageView packsView;
    private TextView packsNumberView;
    private final String TAG = "OpenPacksActivity";

    //parsed data for packs, this is first called in the home activity, if an errors occurs it will be called from here
    public static int numberOfPacks = 0;
    public static LinkedList<Integer> packsTypesStack; //contains a list of packTypes gotten from server, the first element in the list is displayed at the top of pack list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_packs);
        ImageView BackToHome = findViewById(R.id.HomeMenuButton);

        BackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(OpenPacksActivity.this, HomeActivity.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
            }
        });

        Button buyPacks = findViewById(R.id.BuyPackButton);

        buyPacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buyPackIntent = new Intent(OpenPacksActivity.this, BuyPacksActivity.class);
                startActivity(buyPackIntent);
            }
        });

        packsView = (ImageView)findViewById(R.id.Packs);
        packsNumberView = (TextView)findViewById(R.id.PackNumber);
        packsView.setEnabled(false);
        packsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packsView.setEnabled(false);
                OpenPack();
                packsView.setEnabled(true);
            }
        });
    }

    /**
     * steps that happen when user opens a pack :
     * - if user has no packs => go to the buy packs activity
     * - if user has a pack : pack is removed from the user list and a request to the server will take care of adding
     * 5 random cards to the user collection and returning them to the client
     * The 5 cards will be sent to the PackCardsActivity that will be opened to display the cards the user got
     */
    private void OpenPack() {
        //server methods needs to open pack and add the cards to user collection and return the opened cards
        if (packsTypesStack == null || packsTypesStack.size() == 0){
            Intent buyPackIntent = new Intent(OpenPacksActivity.this, BuyPacksActivity.class);
            startActivity(buyPackIntent);
            packsView.setEnabled(true);
            return;
        }

        int packId = packsTypesStack.pop();
        HttpGetter getRequest = new HttpGetter();
        getRequest.execute("open",""+Configuration.currentUser.getId(),""+packId,"Pack");

        try {
            JSONObject jsonObject = new JSONObject(getRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));
            boolean didItwork = jsonObject.getBoolean("worked");
            if (!didItwork){
                //an error occured ( probably mismatch between packs in UI and the real number of packs on the server, we're going to repull)
                Log.e(TAG, "Error occured while opening packs");
                Toast.makeText(getApplicationContext(), "An error occured, repulling packs from server", Toast.LENGTH_SHORT);
                serverPacksRequest();
                return;
            }
            numberOfPacks--;
            JSONArray cardIds = jsonObject.getJSONArray("CardIds");

            if (cardIds.length() != 5){
                Log.e(TAG, "unexpected number of cards " + cardIds.length() + " ! 5 expected");
                return;
            }

            PackCardsActivity.cardsInPack = new int[5];
            for (int i=0; i<cardIds.length(); i++){
                PackCardsActivity.cardsInPack[i] = cardIds.getJSONObject(i).getInt("ID");
            }

            refreshCards();
            Log.d(TAG, "successfully added cards to packCardsActivity now going through");
            updateUI();
            //go to the next activity
            Intent showObtainedCards = new Intent(OpenPacksActivity.this, PackCardsActivity.class);
            startActivity(showObtainedCards);

        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,3,true);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,1,true);
        } catch (TimeoutException e){
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,0,true);
        }
    }

    /**
     * repulls the user collection from the server
     */
    public void refreshCards(){
        Configuration.CARDS = new Configuration.PlayerCardList();
        try {
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

        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,1,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,3,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,0,true);
        }


    }
    @Override
    protected void onResume(){
        super.onResume();
        //disable the ability to open a pack until the packs are gotten from the server
        packsView.setEnabled(false);
        LocationService.notificationText[0] = "";
        updateUI();
        packsView.setEnabled(true);
    }

    /**
    get the number of packs and their Types from server
     */
    protected void serverPacksRequest(){
        HttpGetter getRequest = new HttpGetter();
        getRequest.execute("get",""+Configuration.currentUser.getId(),"Packs");

        try {
            JSONObject jsonObject = new JSONObject(getRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));

            JSONArray packTypes = jsonObject.getJSONArray("PackTypes");

            packsTypesStack = new LinkedList<Integer>();
            numberOfPacks = packTypes.length();
            for (int i=0; i<packTypes.length(); i++){
                JSONObject pack = packTypes.getJSONObject(i);
                packsTypesStack.add(pack.getInt("Type"));
            }

            Log.d(TAG, "Got a total of packs : " + packTypes.length());
            updateUI();

        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,3,true);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,1,true);
        } catch (TimeoutException e){
            e.printStackTrace();
            Configuration.serverDownBehaviour(OpenPacksActivity.this,0,true);
        }
    }

    /**
     * Updates the UI :
     * - The number of packs on the top
     * - The first pack on the stack is displayed and can be opened (Pack type is what matters)
     */
    protected void updateUI(){
        if (packsView == null || packsNumberView == null){
            Log.e(TAG, "unexpected null pointers on UI elements");
            return;
        }

        packsNumberView.setText("You have " + numberOfPacks + " packs !");

        if (numberOfPacks > 0){
            int firstPackType = packsTypesStack.peekFirst();

            switch (firstPackType){
                case 0: //inferno
                    packsView.setImageResource(R.drawable.pack_inferno);
                    break;
                case 1: //tsunami
                    packsView.setImageResource(R.drawable.pack_tsunami);
                    break;
                case 2: //storm
                    packsView.setImageResource(R.drawable.pack_storm);
                    break;
                case 3://Earthquake
                    packsView.setImageResource(R.drawable.pack_land);
                    break;
                case 4://twilight
                    packsView.setImageResource(R.drawable.pack_twilight);
                    break;
                case 5://normal
                    packsView.setImageResource(R.drawable.pack_neutral);
                    break;
                default:
                    Log.e(TAG, "unexpected pack id !");
                    break;

            }
        }else{
            packsView.setImageResource(R.drawable.pack_placeholder);
        }
    }


}
