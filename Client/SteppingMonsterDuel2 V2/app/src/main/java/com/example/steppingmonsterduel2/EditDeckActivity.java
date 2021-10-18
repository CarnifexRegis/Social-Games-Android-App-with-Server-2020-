package com.example.steppingmonsterduel2;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.steppingmonsterduel2.Objects.*;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
Displays the user's deck and allows him to edit any one of them
 */
public class EditDeckActivity extends AppCompatActivity {

    private ArrayList<Deck> decks =new ArrayList<Deck>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        ImageView backButton = findViewById(R.id.BackToMenuFromEditDeck);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menue = new Intent(EditDeckActivity.this,HomeActivity.class);
                menue.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(menue);
            }
        });

        //initialize decks
        //Todo change this to get array from server
        //   decks = new ArrayList<>(Arrays.asList(new Deck(3,"Big balls",13, Element.STORM),
        //            new Deck(4,"Auto cheese",13,Element.INFERNAL),
        //           new Deck(4,"Caitlyn OP",8,Element.TSUNAMI)
        //          ,new Deck(4,"Twisted Treeline",4,Element.TWILIGHT),
        //           new Deck(4,"Master Yi is garbage",2,Element.INFERNAL)));


        //update whole activity


    }

    @Override
    protected void onStart(){
        super.onStart();
        try{
            getDecks();
        }catch(Exception e){
            onBackPressed();
        }
        //initialize views ,layouts and buttons
        ArrayList<Button> btnArray = setButtons();
        ArrayList<TextView> numberOfCardsInDeck = setNumberOfCards();
        ArrayList<TextView> elementTypeOfDeck = setTypeOfDeck();
        ArrayList<LinearLayout> layout = setLayouts();

        initializeEverything(btnArray,elementTypeOfDeck,layout,numberOfCardsInDeck);
    }

    private void getDecks() throws InterruptedException, ExecutionException, TimeoutException, JSONException {

        HttpGetter httpGetter = new HttpGetter();
        httpGetter.execute("get",""+Configuration.currentUser.getId(),"DeckSkeleton");


        JSONObject json = new JSONObject(httpGetter.get(Configuration.timeoutTime, TimeUnit.SECONDS));
        JSONArray jsonDecks = json.getJSONArray("Decks");
        decks.clear();
        for(int i=0;i<jsonDecks.length();i++)
        {
            JSONObject deck = jsonDecks.getJSONObject(i);
            decks.add(new Deck(deck.getInt("ID"),deck.getString("Name"),deck.getInt("AmountOfCards"),Element.fromID(deck.getInt("Element"))));
            //  if(!matchRequest.containsKey(user.getInt("ID"))) {
            //     matchRequest.put( user.getInt( "ID" ), new User( user.getInt( "ID" ), user.getInt( "Steps" )
            //              , user.getString( "Name" ), winrate, user.getString( "Picture" ) ) );
            //      updateRequests = true;
            //   }

        }
    }

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
        deck.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDeckActivity.this,CustomizeDeckActivity.class);
                Bundle deckInfo = new Bundle();
                deckInfo.putInt("id",d.getId());
                deckInfo.putString("name",d.getName());
                intent.putExtras(deckInfo);

                startActivity(intent);
                //finish();
            }
        } );
    }
}
