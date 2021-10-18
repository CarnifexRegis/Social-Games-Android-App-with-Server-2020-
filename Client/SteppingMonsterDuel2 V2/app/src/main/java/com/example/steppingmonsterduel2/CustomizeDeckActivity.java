package com.example.steppingmonsterduel2;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.GameContent;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
Activity that displays the collection and the user's deck cards, is an updated version
of Customize deck
 */
public class CustomizeDeckActivity extends AppCompatActivity {

    final String TAG = "CustomizeDeck";
    final int MAX_DECK_SIZE = 15;

    ArrayList<PlayerCard> deckElements = new ArrayList<PlayerCard>(); // Cards contained in deck
    SparseArray<ListCardLayout> deckCardsView; // cards adapted to the UI
    SparseArray<View> collectionCardsView; // cards in the collection
    ViewGroup deckCardBar;
    ViewGroup collectionContainer; // Contains multiple lines of collection cards

    LinearLayout actualCollectionLayout = null; //horizontal layout and contains up to 5 cards from the collection
    List<LinearLayout> collectionRows;

    int numberOfCardsInLayout = 0;
    final int MAX_CARDS_IN_ROW = 4;
    public int idOfDeck = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_deck2);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.d(TAG, "Came here");
        Bundle getDeck = getIntent().getExtras();

        //Test if deck is valid. If not set it to -1!

        if(getDeck!=null){
            idOfDeck = getDeck.getInt("id");
        }
        EditText editText = findViewById(R.id.DeckName);
        editText.setText(getDeck.getString("name"));

        Button updateDeckNameButton = findViewById(R.id.SaveDeckName);
        updateDeckNameButton.setOnClickListener(v ->{
            updateDeckName(editText.getText().toString(), idOfDeck);
        });

        ImageView goBackToHome = findViewById(R.id.BackToMenuFromCustomizeDeck);

        goBackToHome.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomizeDeckActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } );

        //getting the views
        deckCardBar = findViewById(R.id.DeckContainer);
        collectionContainer = findViewById(R.id.CollectionContainer);
        collectionRows = new ArrayList<>();
        collectionCardsView = new SparseArray<>();
        deckCardsView = new SparseArray<>();

        //initialize the UI
        checkIFDeckIDISValid(idOfDeck);
        getDeckElements(idOfDeck);
        SetDeckContainer();
        setCollectionUI();
    }


    //Checks if the deck id is valid
    private void checkIFDeckIDISValid(int idOfDeck){
        if(idOfDeck==-1){
            Toast.makeText(CustomizeDeckActivity.this,"Deck not found",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CustomizeDeckActivity.this,EditDeckActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //sends a request to server to change the deck name
    public void updateDeckName(String s, int did){
        try{
            HttpPoster poster = new HttpPoster();
            poster.execute( "changeDeck",""+did ,s, "Name" );
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //for each card in the collection, adds the cards to the collection with is a scroll view with lines of 5 cards
    private void setCollectionUI(){
        numberOfCardsInLayout = 0;
        actualCollectionLayout = null;

        for(Pair<Integer, PlayerCard> pair : Configuration.CARDS) {
            Log.e(TAG, "pairs : " + pair.first + " and " + pair.second.getClass());
            if (actualCollectionLayout == null || numberOfCardsInLayout >= MAX_CARDS_IN_ROW){
                generateCollectionRow();
            }

            if (actualCollectionLayout == null){
                Log.e(TAG, "unexpcted still null");
                return;
            }
            try{
                addCardToCollection(pair.second.getCardID(), pair.second.getPicture(), actualCollectionLayout, pair.second.getCardType() instanceof GameContent.MonsterType, pair.second.getCardTypeID());
            }catch(Exception e){
                Log.e(TAG, "something went wrong? " + e.getMessage());
            }

            numberOfCardsInLayout++;
        }
    }

    private void generateCollectionRow(){
        actualCollectionLayout = new LinearLayout(getApplicationContext());
        collectionRows.add(actualCollectionLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500);
        layoutParams.setMargins(0,0,0,16);
        actualCollectionLayout.setLayoutParams(layoutParams);
        actualCollectionLayout.setOrientation(LinearLayout.HORIZONTAL);

        numberOfCardsInLayout = 0;
        collectionContainer.addView(actualCollectionLayout);
    }

    //the cards on the right that are contained in the deck
    private void SetDeckContainer() {
        for(PlayerCard pc : deckElements){
            //this happens when a card is traded ! it needs to be removed from the deck
            if (Configuration.CARDS.get(pc.getCardID()) == null){
                removeCardFromDeck(idOfDeck, pc.getCardID());
                continue;
            }
            addViewToScrollbar(pc.getCardID(),pc.getPicture());
        }
    }

    //adds a card to the deck UI
    private void addViewToScrollbar(final Integer key,String picture) {
        ListCardLayout layout = new ListCardLayout(deckCardBar, key,picture);
        deckCardsView.put(key, layout);
    }


    //manages adding cards to the collection
    private void addCardToCollection(final Integer key, String picture, LinearLayout linearLayout, boolean monster, int typeID){
        //CollectionCard layout = new CollectionCard(linearLayout, playerCard.getCardID(), playerCard.getPicture(), playerCard.getCardType());
        View card;
        GameContent.CardType cardStats = GameContent.getCardTypeByID(typeID);

        if (monster){
            card = getLayoutInflater().inflate(R.layout.card_layout_monster, linearLayout, false);
            TextView atk = card.findViewById(R.id.damage);
            TextView health = card.findViewById(R.id.health);
            GameContent.MonsterType monsterStats = GameContent.getMonsterByID(typeID);
            atk.setText(""+monsterStats.damage);
            atk.setTextSize(8f);
            health.setText(""+monsterStats.health);
            health.setTextSize(8f);
            ImageView cardElement = card.findViewById(R.id.element);
            cardElement.setImageResource(Element.getMonsterResourceID(cardStats.element));

        }else{
            card = getLayoutInflater().inflate(R.layout.card_layout_spell, linearLayout, false);
            TextView cardDescription = card.findViewById(R.id.description);
            cardDescription.setText(cardStats.descriptionShort);
            cardDescription.setTextSize(8f);
            ImageView cardElement = card.findViewById(R.id.element);
            cardElement.setImageResource(Element.getSpellResourceID(cardStats.element));
        }

        TextView cardName = card.findViewById(R.id.name);
        ImageView cardImage = card.findViewById(R.id.image);

        TextView Cardmana = card.findViewById(R.id.mana);

        if(picture.equals("null")){
            cardImage.setImageResource(R.drawable.standart); //TODO set actual image
        } else{
            Configuration.getPictureOutOfStorageAndSetItToView(CustomizeDeckActivity.this, cardImage,picture);
        }

        cardName.setText(""+cardStats.name);
        cardName.setTextSize(8f);
        Cardmana.setText(""+cardStats.mana);
        Cardmana.setTextSize(8f);



       LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 490);
        layoutParams.setMargins(8,0,8,0);
        card.setLayoutParams(layoutParams);
        card.setOnClickListener(v -> {
            //ui update
            card.setEnabled(false);
            boolean sizeBool = deckCardsView.size() < MAX_DECK_SIZE;
            boolean serverRequest = addCardToDeck(this.idOfDeck, key);
            Log.d(TAG, "serverRequest : " + serverRequest + " and size bool :" + sizeBool + " and both " + ((sizeBool) && (serverRequest)));
            if ((sizeBool) && (serverRequest)){
                Drawable foreground = getDrawable(R.drawable.used_card_cover);
                card.setForeground(foreground);

                //add to deck viewer
                addViewToScrollbar(key, picture);
            }else{
                Toast.makeText(CustomizeDeckActivity.this, "Your deck is already full", Toast.LENGTH_SHORT).show();
                card.setEnabled(true);
            }
            Log.d(TAG, "Deck size :" + deckCardsView.size());

        });

        String cardDescription = cardStats.descriptionLong;
        card.setOnLongClickListener(v -> {
            new AlertDialog.Builder(CustomizeDeckActivity.this)
                    .setTitle("Description:")
                    .setMessage(cardDescription)
                    .show();
            return false;
        });

        //if the deck contains this card, disable it
        if (deckCardsView.get(key) != null){
            card.setEnabled(false);
            Drawable foreground = getDrawable(R.drawable.used_card_cover);
            card.setForeground(foreground);

        }

        linearLayout.addView(card);
        Log.e(TAG, " is card null " + card);
        collectionCardsView.put(key, card);
    }


    //sets a collection card to clickable (card that was removed from the deck)
    private void enableCollectionCard(int cardID){
        View cardView = collectionCardsView.get(cardID);
        cardView.setEnabled(true);
        cardView.setForeground(null);
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
            Toast.makeText(CustomizeDeckActivity.this, "Deck not found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CustomizeDeckActivity.this, EditDeckActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public boolean removeCardFromDeck(int did, int cid){
        try{
            HttpGetter poster = new HttpGetter();
            poster.execute( "removeCard",""+cid ,""+did, "FromDeck" );
            String result = poster.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            if(result.equals("failed")){
                return false;
            }else{
                return  true;
            }
        }catch(Exception e){
            return false;
        }
    }
    public boolean addCardToDeck(int did, int cid){
        Log.d(TAG, "addCard Called");
        try{
            HttpGetter poster = new HttpGetter();
            poster.execute( "addCard",""+  Configuration.currentUser.getId(),""+cid ,""+did, "ToDeck" );
            String result = poster.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            if(result.equals("failed")){
                return false;
            }else{
                return  true;
            }
        }catch(Exception e){
            return  false;
        }
    }

    private class ListCardLayout {

        final View card;
        final ViewGroup parent;
        int playerCardID;
        final TextView cardName;
        final ImageView cardElem;
        final ImageView cardImg;

        private GameContent.CardType cardType;

        ListCardLayout(ViewGroup parent, int playerCardID,String picture){
            //create view and add it to parent
            this.parent = parent;
            this.card = getLayoutInflater().inflate(R.layout.card_layout_tradelist, parent, false);
            parent.addView(card);
            parent.invalidate();
            //initialize members
            this.cardName = card.findViewById(R.id.name);
            this.cardElem = card.findViewById(R.id.element);
            this.cardImg = card.findViewById(R.id.picture);
            //configure everything that has to do with the card's type
            updateCardInfo(playerCardID,picture);
        }

        void setInvisible(){
            card.setVisibility(View.GONE);
        }
        void setVisible(){
            card.setVisibility(View.VISIBLE);
        }
        void removeView(){
            parent.removeView(card);
        }

        private void updateCardInfo(int playerCardID,String picture){
            //get crucial info
            this.playerCardID = playerCardID;
            PlayerCard playerCard = Configuration.CARDS.get(playerCardID);
            this.cardType = playerCard.getCardType();
            //update display
            cardName.setText( cardType.name );
            cardElem.setImageResource(Element.getDeckResourceID(cardType.element));
            //cardImg.setImageResource( R.drawable.affe ); //TODO set actual image
            if(picture.equals("null")){
                cardImg.setImageResource(R.drawable.standart); //TODO set actual image
            } else{
                Configuration.getPictureOutOfStorageAndSetItToView(CustomizeDeckActivity.this, cardImg,picture);
            }
            //update click listeners:
            //on click: display card description
            //on long click: start drag and drop operation with the correct data.
            this.card.setOnClickListener(v -> {
                if (removeCardFromDeck(idOfDeck, this.playerCardID)){
                    enableCollectionCard(this.playerCardID);
                    deckCardsView.remove(this.playerCardID);
                    this.parent.removeView(this.card);
                }else{
                    Toast.makeText(CustomizeDeckActivity.this, "Error while removing card", Toast.LENGTH_SHORT);
                }
            });

            this.card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(CustomizeDeckActivity.this)
                        .setTitle("Description:")
                        .setMessage(cardType.descriptionLong)
                        .show();
                return false;
            });

        }
    }
}
