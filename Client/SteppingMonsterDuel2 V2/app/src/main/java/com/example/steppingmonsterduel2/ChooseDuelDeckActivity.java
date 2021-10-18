package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.steppingmonsterduel2.Objects.Deck;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.Weather;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChooseDuelDeckActivity extends AppCompatActivity {
    private ArrayList<Deck> decks =new ArrayList<Deck>();
    TextView activityName;
   public static final String name = "Choose your Deck";

   private Handler handler;
   private boolean deckSet;

   private boolean gameAlreadyStarted;

   private Element weatherElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);
        activityName = findViewById(R.id.EditdeckText);
        activityName.setText(name);
        try{
            getDecks();
        }catch(Exception e){
            onBackPressed();
        }
        ArrayList<Button> btnArray = setButtons();
        ArrayList<TextView> numberOfCardsInDeck = setNumberOfCards();
        ArrayList<TextView> elementTypeOfDeck = setTypeOfDeck();
        ArrayList<LinearLayout> layout = setLayouts();

        initializeEverything(btnArray, elementTypeOfDeck, layout, numberOfCardsInDeck);

        ImageView backButton = findViewById(R.id.BackToMenuFromEditDeck);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menue = new Intent(ChooseDuelDeckActivity.this,HomeActivity.class);
                menue.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(menue);
            }
        });

        //Server communication
        gameAlreadyStarted = false;
        handler = new Handler();
        deckSet = false;

        weatherElement = Weather.getInstance().getBuffedAndDroppableElement();
        if(weatherElement == null) weatherElement = Element.TWILIGHT;
        new AlertDialog.Builder(ChooseDuelDeckActivity.this)
                .setTitle("Active Element:")
                .setMessage(weatherElement.name+"!")
                .setPositiveButton( "OK", null).show();
    }

    // copy paste from Editdeck xD
    private void getDecks() throws InterruptedException, ExecutionException, TimeoutException, JSONException {

        HttpGetter httpGetter = new HttpGetter();
        httpGetter.execute("get",""+ Configuration.currentUser.getId(),"DeckSkeleton");


        JSONObject json = new JSONObject(httpGetter.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonDecks = json.getJSONArray("Decks");
        decks.clear();
        for(int i=0;i<jsonDecks.length();i++)
        {
            JSONObject deck = jsonDecks.getJSONObject(i);
            decks.add(new Deck(deck.getInt("ID"),deck.getString("Name"),deck.getInt("AmountOfCards"), Element.fromID(deck.getInt("Element"))));
            //  if(!matchRequest.containsKey(user.getInt("ID"))) {
            //     matchRequest.put( user.getInt( "ID" ), new User( user.getInt( "ID" ), user.getInt( "Steps" )
            //              , user.getString( "Name" ), winrate, user.getString( "Picture" ) ) );
            //      updateRequests = true;
            //   }

        }
    }
    // yanns edit deck stuff
    private ArrayList<LinearLayout> setLayouts(){
        LinearLayout deck1 = findViewById(R.id.Deck1);
        LinearLayout deck2 = findViewById(R.id.Deck2);
        LinearLayout deck3 = findViewById(R.id.Deck3);
        LinearLayout deck4 = findViewById(R.id.Deck4);
        LinearLayout deck5 = findViewById(R.id.Deck5);

        return new ArrayList<>( Arrays.asList(deck1,deck2,deck3,deck4,deck5));
    }

    private ArrayList<Button> setButtons(){
        Button buttonDeck1 = findViewById(R.id.buttonDeck1);
        Button buttonDeck2 = findViewById(R.id.buttonDeck2);
        Button buttonDeck3 = findViewById(R.id.buttonDeck3);
        Button buttonDeck4 = findViewById(R.id.buttonDeck4);
        Button buttonDeck5 = findViewById(R.id.buttonDeck5);

        return new ArrayList<>( Arrays.asList(buttonDeck1,buttonDeck2,buttonDeck3,buttonDeck4,buttonDeck5));
    }

    private ArrayList<TextView> setNumberOfCards(){
        TextView viewDeck1 = findViewById(R.id.AmountCardsDeck1);
        TextView viewDeck2 = findViewById(R.id.AmountCardsDeck2);
        TextView viewDeck3 = findViewById(R.id.AmountCardsDeck3);
        TextView viewDeck4 = findViewById(R.id.AmountCardsDeck4);
        TextView viewDeck5 = findViewById(R.id.AmountCardsDeck5);

        return new ArrayList<>( Arrays.asList(viewDeck1,viewDeck2,viewDeck3,viewDeck4,viewDeck5));
    }

    private ArrayList<TextView> setTypeOfDeck(){
        TextView viewDeck1 = findViewById(R.id.elementDeck1);
        TextView viewDeck2 = findViewById(R.id.elementDeck2);
        TextView viewDeck3 = findViewById(R.id.elementDeck3);
        TextView viewDeck4 = findViewById(R.id.elementDeck4);
        TextView viewDeck5 = findViewById(R.id.elementDeck5);

        return new ArrayList<>( Arrays.asList(viewDeck1,viewDeck2,viewDeck3,viewDeck4,viewDeck5));
    }


    private void initializeEverything(ArrayList<Button> btn,ArrayList<TextView> elementDeck,ArrayList<LinearLayout> layout,ArrayList<TextView> nbrCards){
        for(int i=0;i<5;i++){
            Deck d = decks.get(i);
            editElementDeck(elementDeck.get(i),d);
            //Configuration.editLayoutImg(layout.get(i),d.getElement(),true);
            layout.get(i).setBackgroundResource(Element.getDeckResourceID(d.getElement()));
            editNmbrCards(nbrCards.get(i),d);
            editButton(btn.get(i),d);
        }
    }
    private void editNmbrCards(TextView view,Deck d){
        view.setText("Card amount:\n"+d.getAmountOfCards()+"/15");
    }

    private void editElementDeck(TextView view,Deck d){
        view.setText("Element:\n"+d.getElement());
    }

    private void editButton(Button deck,final Deck d){
        deck.setText(d.getName());
        deck.setOnClickListener( (v)-> {
            if(deckSet) return; //No need to do anything if the deck has already been selected once.
            //tell server what's up
            Element activeWeather = Weather.getInstance().getBuffedAndDroppableElement();
            if(HttpPoster.safePost(null, "GameManagement", "SelectDeck", Configuration.currentUser.getId(), d.getId(), activeWeather.databaseID)){
                //periodically check whether the other user has also selected their deck and the game may start
                Runnable runnable = new Runnable() {
                    @Override public void run() {
                        tryStartGame();
                        handler.postDelayed(this, Configuration.REFRESH_TIME);
                    }
                };
                handler.postDelayed(runnable, Configuration.REFRESH_TIME);
                deckSet = true;
            }
        } );
    }

    private void tryStartGame(){

        if(gameAlreadyStarted) return; //So this may never be called twice.
        String result = HttpGetter.safeGet(null, "GameManagement", "HasGameStarted", Configuration.currentUser.getId());
        if(result.equals("yes")){
            gameAlreadyStarted = true;
            //add opponent info to the intent and start DuelActivity.
            Intent intent = new Intent(ChooseDuelDeckActivity.this,DuelActivity.class);
            Parcelable opponent = getIntent().getParcelableExtra("Opponent");
            intent.putExtra("Opponent", opponent);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(handler != null) handler.removeCallbacksAndMessages(null);
    }
}
