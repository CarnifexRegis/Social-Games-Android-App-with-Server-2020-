package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.RecyclerHelpers.RecyclerDeckAdapter;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.transform.Result;

//www.youtube.com/watch?v=94rCjYxvzEE
// https://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type

/*
Obsolete Activity, was remade in CustomizeDeckActivity
this was kept to recycle the code in an improved UI display of the deck editing
 */
public class CustomizeDeck extends AppCompatActivity {
    ArrayList<PlayerCard> deckElements = new ArrayList<PlayerCard>();
    public int idOfDeck = -1;
    private   RecyclerDeckAdapter deckAdapter;
    private   RecyclerDeckAdapter ownedAdapter;

    public RecyclerDeckAdapter getDeckAdapter() {
        return deckAdapter;
    }

    public RecyclerDeckAdapter getOwnedAdapter() {
        return ownedAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_customize_deck );

        Bundle getDeck = getIntent().getExtras();

        //Test if deck is valid. If not set it to -1!

        if(getDeck!=null){
            idOfDeck = getDeck.getInt("id");
        }
        EditText editText = (EditText) findViewById(R.id.editDeckName);
        editText.setText(getDeck.getString("name"));
        Button b = (Button) findViewById(R.id.updateName);

            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                   updateDeckName(String.valueOf(editText.getText()),getIntent().getExtras().getInt("id"));
                }

            });


        //If not valid go to editDeckActivity
        checkIFDeckIDISValid(idOfDeck);
        getDeckElements(idOfDeck);
        refreshCards();

        initDeckView();
        initOwnedView();
    }

    public void updateDeckName(String s, int did){
        try{
        HttpPoster poster = new HttpPoster();
            poster.execute( "changeDeck",""+did ,s, "Name" );
        }catch(Exception e){

        }
    }
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

            } catch (Exception e) {
                Toast.makeText(CustomizeDeck.this, "Cards not found", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CustomizeDeck.this, EditDeckActivity.class);
                startActivity(intent);
                finish();
            }

    }
    public void getDeckElements(int did){

            try {

                HttpGetter getCards = new HttpGetter();
                getCards.execute("getDeck", "" + did,"Elements");

                String cardUser = getCards.get(Configuration.timeoutTime, TimeUnit.SECONDS);

                JSONObject json = new JSONObject(cardUser);
                JSONArray jsonCards = json.getJSONArray("Cards");

                for (int i = 0; i < jsonCards.length(); i++) {
                    JSONObject card = jsonCards.getJSONObject(i);
                    PlayerCard cardToAdd = new PlayerCard(card.getInt("Type"), card.getString("Picture"), card.getInt("Cid"));
                    deckElements.add(cardToAdd);
                }

            } catch (Exception e) {
                Toast.makeText(CustomizeDeck.this, "Deck not found", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CustomizeDeck.this, EditDeckActivity.class);
                startActivity(intent);
                finish();
            }

    }
    public void initDeckView(){
        LinearLayoutManager deckManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView recyclerView = findViewById(R.id.deckRec);
        recyclerView.setLayoutManager(deckManager);
        deckAdapter = new RecyclerDeckAdapter (this, deckElements,this,true);
        recyclerView.setAdapter(deckAdapter);
    }
    public void initOwnedView(){
        LinearLayoutManager  ownedManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView recyclerView = findViewById(R.id.ownedRec);
        recyclerView.setLayoutManager(ownedManager);
        ArrayList<PlayerCard> pc  = new ArrayList<PlayerCard>();
        for(Pair<Integer, PlayerCard> pair : Configuration.CARDS){
            pc.add(pair.second);
        }
      //  for(int i = 0; i < deckElements.size(); i++){
        //   pc.remove(deckElements.get(i));
        //}
        ownedAdapter = new RecyclerDeckAdapter (this,pc,this,false);
        recyclerView.setAdapter(ownedAdapter);
    }
    public boolean removeCardFromDeck(int did, int cid){
        try{
            HttpGetter poster = new HttpGetter();
            poster.execute( "removeCard",""+cid ,""+did, "FromDeck" );
            String result = poster.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            if(result.equals("failed")){
                           Toast.makeText(this,
                                    "Something went wrong removing Card from Deck",Toast.LENGTH_SHORT).show();
                return false;
            }else{
                return  true;
            }
        }catch(Exception e){
            return false;
        }
    }
    public boolean addCardToDeck(int did, int cid){
        try{
            HttpGetter poster = new HttpGetter();
            poster.execute( "addCard",""+  Configuration.currentUser.getId(),""+cid ,""+did, "ToDeck" );
            String result = poster.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            if(result.equals("failed")){
                Toast.makeText(this,
                        "Something went wrong adding Card to Deck"+result,Toast.LENGTH_SHORT).show();
                return false;
            }else{
                return  true;
            }
        }catch(Exception e){
                return  false;
        }
    }

    private void checkIFDeckIDISValid(int idOfDeck){
        if(idOfDeck==-1){
            Toast.makeText(CustomizeDeck.this,"Deck not found",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CustomizeDeck.this,EditDeckActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
