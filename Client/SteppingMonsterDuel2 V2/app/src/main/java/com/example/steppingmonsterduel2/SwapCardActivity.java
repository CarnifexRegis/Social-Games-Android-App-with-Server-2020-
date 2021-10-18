package com.example.steppingmonsterduel2;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SwapCardActivity extends AppCompatActivity {
    private static final String TAG = "SwapCards";
    ViewGroup cardBar; //The LinearLayout of the scrollable list on the right

    ViewGroup userTradeSlot;
    CardLayout userOfferedCard;
    Integer userOfferedCardID = null; //null means there isn't a card right now. I could also do int with value -1 but I like this.
    CardLayout opponentOfferedCard;
    Integer opponentOfferedCardID = null;
    boolean isRunning = true;

    Button mergeButton;

    SparseArray<ListCardLayout> cardTadeList;

    int idOfOtherUser = -1;
    boolean isOpponent;
    int mergeCount = -1;
    int tierOfTradedCard = -1;

    Runnable run;
    final Handler handler = new Handler();
    TextView mergeCounting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_swap_card_acitivity );


        mergeCounting = findViewById(R.id.MergeCount);
        userTradeSlot = findViewById( R.id.CardPlaceholderUser );
        ViewGroup opponentTradeSlot = findViewById(R.id.CardPlaceholderEnemy);
        cardBar = findViewById( R.id.tradeBar );
        ImageView traderImage = findViewById(R.id.traderImage);
        TextView traderName = findViewById(R.id.traderName);

        this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            //set informations like the image of the opponent
            //roles are given to the traders
            //one is the opponent one the user to differentiate between both when communicating with the server
            setInformationsOfTrader( bundle, traderImage, traderName );
            getMergeCount();
        }

        if(mergeCount>0) {

            mergeButton = findViewById(R.id.mergeButton);
            mergeButton.setVisibility(View.VISIBLE);
            setMergeButton();
            mergeCounting.setText("Merges* left: "+mergeCount);
        }



        cardTadeList = new SparseArray<>();
        userOfferedCard = new CardLayout(userTradeSlot);
        opponentOfferedCard = new CardLayout(opponentTradeSlot);

        onDragOfLayout();
        backButton();

        setAcceptButton();


        integrateCards();
    }

    //get the merge count from server that both the opponent and the user know how many merges are still consumable
    private void getMergeCount(){
        HttpGetter getMergeCount = new HttpGetter();
        getMergeCount.execute("get",""+Configuration.currentUser.getId(),""+idOfOtherUser,"MergeCount");
        try {
            String merge = getMergeCount.get(Configuration.timeoutTime, TimeUnit.SECONDS);
            JSONObject getMerge = new JSONObject(merge);
            mergeCount = getMerge.getInt("MergeCount");
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }

    }

    //Class that immediately stores the cards and saves its stats on the card
    private class CardLayout{

        final ViewGroup parent;
        //private boolean isSpell;

        //OOP? What's that?
        //Monster
        final View monsterCard;
        final TextView monsterName;
        final ImageView monsterElement;
        final TextView monsterMana;
        final TextView monsterHealth;
        final TextView monsterDamage;
        final ImageView monsterImage;

        //Spell
        final View spellCard;
        final TextView spellName;
        final ImageView spellElement;
        final TextView spellMana;
        final TextView spellDescription;
        final ImageView spellImage;

        CardLayout(ViewGroup parent){
            //create view and add it to parent
            this.parent = parent;
            this.spellCard = getLayoutInflater().inflate(R.layout.card_layout_spell, parent, false);
            parent.addView(spellCard);
            this.monsterCard = getLayoutInflater().inflate(R.layout.card_layout_monster, parent, false);
            parent.addView(monsterCard);
            parent.invalidate();
            setVisibility(true, false);
            setVisibility(false, false);

            //TODO more OOP. my eyes!
            //initialize display fields
            this.spellName = spellCard.findViewById(R.id.name);
            this.spellElement = spellCard.findViewById(R.id.element);
            this.spellMana = spellCard.findViewById(R.id.mana);
            this.spellDescription = spellCard.findViewById(R.id.description);
            this.spellImage = spellCard.findViewById(R.id.image);

            this.monsterName = monsterCard.findViewById(R.id.name);
            this.monsterElement = monsterCard.findViewById(R.id.element);
            this.monsterMana = monsterCard.findViewById(R.id.mana);
            this.monsterHealth = monsterCard.findViewById(R.id.health);
            this.monsterDamage = monsterCard.findViewById(R.id.damage);
            this.monsterImage = monsterCard.findViewById(R.id.image);

            //extra function because why not
            setTextsizes();
        }

        void setVisibility(boolean ofSpell, boolean visible){ //kinda ugly but sue me.
            (ofSpell ? spellCard : monsterCard).setVisibility(visible ? View.VISIBLE : View.GONE);
            parent.invalidate();
        }

        //makes the card display all relevant info of a CardType. If null, makes the card invisible
        void updateCardInfo(@Nullable GameContent.CardType cardType,String picture){
            //if cardType is null, just make the card invisible.
            if(cardType == null){
                setVisibility(true, false);
                setVisibility(false, false);
                return;
            }
            //else make the card display all relevant info
            //check whether cardType is a spell or a monster
            if(cardType instanceof GameContent.MonsterType){
                //isSpell = false;
                setVisibility(false, true);
                setVisibility(true, false);
                GameContent.MonsterType monType = (GameContent.MonsterType) cardType;
                monsterName.setText(cardType.name);
                monsterElement.setImageResource(Element.getMonsterResourceID(cardType.element));
                monsterMana.setText(intToString(cardType.mana));
                monsterHealth.setText(intToString(monType.health));
                monsterDamage.setText(intToString(monType.damage));
                if(picture.equals("null")){
                    monsterImage.setImageResource(R.drawable.standart); //TODO set actual image
                } else{
                    Configuration.getPictureOutOfStorageAndSetItToView(SwapCardActivity.this, monsterImage,picture);
                }
            }
            else {
                //isSpell = true;
                setVisibility(true, true);
                setVisibility(false, false);
                spellName.setText(cardType.name);
                spellElement.setImageResource(Element.getSpellResourceID(cardType.element));
                spellMana.setText(intToString(cardType.mana));
                spellDescription.setText(cardType.descriptionShort);
                if(picture.equals("null")){
                    spellImage.setImageResource(R.drawable.standart); //TODO set actual image
                } else{
                    Configuration.getPictureOutOfStorageAndSetItToView(SwapCardActivity.this, spellImage,picture);
                }
            }
        }
        //sets the textsizes of the cards
        void setTextsizes(){
            monsterName.setTextSize(13f);
            spellName.setTextSize(13f);
            monsterMana.setTextSize(20f);
            spellMana.setTextSize(20f);
            monsterDamage.setTextSize(15f);
            monsterHealth.setTextSize(15f);
            spellDescription.setTextSize(17f);
        }
    }

    //cards in list stored they are draggable btw.
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

        //updates the relevant views of the layout
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
                Configuration.getPictureOutOfStorageAndSetItToView(SwapCardActivity.this, cardImg,picture);
            }
            //update click listeners:
            //on click: display card description
            card.setOnClickListener((v)->
                    new AlertDialog.Builder(SwapCardActivity.this)
                            .setTitle("Description:")
                            .setMessage(cardType.descriptionLong)
                            .show()
            );
            //on long click: start drag and drop operation with the correct data.

            card.setOnLongClickListener( (view)->{
                ClipData data = ClipData.newPlainText( "CardID", cardType.element.name ); //TODO idk what the fuck a ClipData is
                View.DragShadowBuilder shadow = new View.DragShadowBuilder( card );
                card.startDragAndDrop( data, shadow, new DragInfo(playerCardID, this,picture,Configuration.CARDS.get(playerCardID).getCardType().tier), 0 );

                int color = 0xCC494444;
                Drawable drawable = new ColorDrawable( color );
                card.setForeground( drawable );
                return true;
            } );
        }
    }

    //info that must be stored in order to drag the cards
    private class DragInfo{
        final int playerCardID;
        final String picture;
        final ListCardLayout listCard; //this guy's foreground needs to be reset
        final int tier;
        DragInfo(int playerCardID, ListCardLayout listCard,String picture,int tier){
            this.playerCardID = playerCardID;
            this.listCard = listCard;
            this.picture = picture;
            this.tier = tier;
        }
        void resetForeground(){
            listCard.card.setForeground(null);
        }
    }


    //If someone leaves the activity the other user must be notified that
    // trading has ended so he can leave the activity aswell
    private void backButton(){

        ImageView view = findViewById(R.id.BackToMenuFromTrades);

        view.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpPoster poster = new HttpPoster();

                if(isOpponent) {
                    poster.execute( "destroy",""+idOfOtherUser ,""+Configuration.currentUser.getId(), "Trade" );
                } else{
                    poster.execute( "destroy", ""+Configuration.currentUser.getId(), ""+idOfOtherUser, "Trade" );
                }

                Intent intent = new Intent(SwapCardActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        } );
    }

    //if card is dragged set it to its new parent and delete it from the list
    //if parent already has a card attached this card needs getting put in list again
    private void onDragOfLayout(){
        userTradeSlot.setOnDragListener((view, event) -> {
            DragInfo info = (DragInfo) event.getLocalState();
            if (info == null)
                throw new RuntimeException("Something invalid was dragged into the userTradeSlot. Its event.getLocalState() payload wasn't of type DragInfo.");
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    //Step 1: remove view from scrollbar
                    info.listCard.setInvisible();
                    //Step 2: if there is already a card on tradePlaceholder then add the card to the scrollview again
                    if (userOfferedCardID != null) {
                        ListCardLayout toBeReadded = cardTadeList.get(userOfferedCardID);
                        if (toBeReadded == null)
                            throw new RuntimeException("UserTradeCard had invalid ID " + userOfferedCardID);
                        toBeReadded.setVisible();
                    }
                    //Step 3: update userOfferedCard
                    userOfferedCardID = info.listCard.playerCardID;
                    userOfferedCard.updateCardInfo(info.listCard.cardType,info.picture);
                    tierOfTradedCard = info.tier;
                    //Step 4: tell the server which card was placed
                    PlayerCard placedCard = Configuration.CARDS.get(info.listCard.playerCardID);
                    tellServerToUpateCard(placedCard, placedCard.getCardID());
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    info.resetForeground();
                    break;
            }
            return true;
        });
    }

    private String intToString(int integer){
        return String.format(Locale.US, "%d", integer);
    }


    //refreshes the trade activity every 5 seconds
    private void refresh(){
        run = new Runnable() {
            public void run() {
                //refresh button has the content attached that is refreshed
                refreshButton();
                if(isRunning) {
                    handler.postDelayed( this, 5000 );
                }
            }
        };
        if(isRunning) {
            handler.postDelayed( run, 5000 );
        }

    }

    //runs every 5 seconds to check if there was a change
    private void refreshButton(){
        //check if trading instance is still active
        //if not user is notified that he can leave the activity
        boolean tradeStillRunning = getIfTradeInstanceIsStillRunning();

        if(tradeStillRunning) {
            //checks if other user has dragged a card to the view
            //if so get it to ur view aswell
            getOpponentCardToView();
            //check if opponent accepted the trade
            getIfAccepted();
            //check if opponent accepted the merge request
            getIfAcceptedMerge();
        } else {
            isRunning = false;
            handler.removeCallbacksAndMessages(null);
            Toast.makeText(SwapCardActivity.this,
                    "Your tradefriend left the instance! You can leave this activity now!",
                    Toast.LENGTH_LONG).show();
        }
    }

    //accept trade and tell server that u accepted
    private void setAcceptButton(){
        Button button = findViewById(R.id.acceptTradeButton);
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptTrade();
            }
        });
    }

    //accept merge and tell server that u accepted the merge
    private void setMergeButton(){

        if(mergeButton!=null) {
            mergeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptMergeTrade();
                }
            });
        }
    }
    //--------------------
    //SERVER COMMUNICATION
    //--------------------

    //Here u get the information if the trading instance is still active
    private boolean getIfTradeInstanceIsStillRunning(){
        HttpGetter getIfTradeInstanceExists = new HttpGetter();

        if(isOpponent)
            getIfTradeInstanceExists.execute("instance",""+idOfOtherUser,
                    ""+Configuration.currentUser.getId(),"Available");
        else
            getIfTradeInstanceExists.execute("instance",""+Configuration.currentUser.getId(),
                    ""+idOfOtherUser,"Available");

        try {
            String result = getIfTradeInstanceExists.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            System.out.println("JSON Result got!\n"+result);
            return result.equals( "InstanceFound" );
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        }
        return false;
    }

    //takes the opponent card from server and sets its information to the opponent card placeholder
    private void getOpponentCardToView(){
        HttpGetter getOpponentCard = new HttpGetter();

        String cardIdentifier = (opponentOfferedCardID != null ? opponentOfferedCardID.toString() : "-1");

        if(isOpponent) {
            getOpponentCard.execute( "opponent",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent",""+cardIdentifier, "Card" );
        } else{
            getOpponentCard.execute( "opponent",""+Configuration.currentUser.getId() , ""+idOfOtherUser,"User",""+cardIdentifier , "Card" );
        }

        String result = "";
        try {
            //get information of the placed card of the opponent trader
            result = getOpponentCard.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            System.out.println("JSON Result got!\n"+result);
            if(!result.equals("Same")&&!result.equals("Not set")&&!result.equals("NoTrade")){
                System.out.println("OPPONENT CARD GETS RELOADED!");
                JSONObject object = new JSONObject(result);

                int cardID = object.getInt("CardID");
                int cardType = object.getInt("CardType");
                String cardPicture = object.getString("CardPicture"); //TODO implement pictures

                opponentOfferedCardID = cardID;

                //here the card informations are set to the opponent card placeholder
                opponentOfferedCard.updateCardInfo(GameContent.getCardTypeByID(cardType),cardPicture);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Invalid json:\n"+result);
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }

    //if the other user has accepted the trade you will get the opponent card to your list
    //if not do nothing
    private void getIfAccepted(){
        HttpGetter getIfTradeAccepted = new HttpGetter();
        if(isOpponent)
            getIfTradeAccepted.execute("get",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","IfAccept");
        else
            getIfTradeAccepted.execute("get",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","IfAccept");

        try {

            String result = getIfTradeAccepted.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);
            int resultIfTradingHappens = object.getInt("Worked");
            Log.d(TAG,""+resultIfTradingHappens);
            //if resultIfTradingHappens = 1 then both accepted the trade and cards will be swapped
            if(resultIfTradingHappens==1)
            {
                getOpponentCard();

                //only one is going to increase the tradecount for the quest because friends are a tupel
                if(isOpponent) {
                    HttpPoster incrementTradeCount = new HttpPoster();
                    incrementTradeCount.execute( "update", "" + Configuration.currentUser.getId(), "" + idOfOtherUser, "TradeCount" );
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }

    //if the other user has accepted the merge you will get the opponent card to your list
    //if not do nothing
    private void getIfAcceptedMerge(){
        HttpGetter getIfTradeAccepted = new HttpGetter();
        if(isOpponent)
            getIfTradeAccepted.execute("get",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","IfAcceptMerge");
        else
            getIfTradeAccepted.execute("get",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","IfAcceptMerge");

        try {
            String result = getIfTradeAccepted.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);
            int resultIfTradingHappens = object.getInt("Worked");
            Log.d(TAG,""+resultIfTradingHappens);
            //if resultIfTradingHappens = 1 then both accepted the merge and cards will be merged
            if(resultIfTradingHappens==1)
            {
                getOpponentCardForMerge();

                if(isOpponent) {
                    HttpPoster incrementTradeCount = new HttpPoster();
                    incrementTradeCount.execute( "update", "" + Configuration.currentUser.getId(), "" + idOfOtherUser, "TradeCount" );
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }

    //here the informations are set that you are ready to trade
    private void acceptTrade(){
        HttpGetter getIfTradeWorked = new HttpGetter();

        //u can only accept if you see the others card
        //if not you cant know what u trade
        if(userOfferedCardID == null || opponentOfferedCardID == null) return; //you can't accept if there are no cards in the first place

        //check if you are an opponent or not because of the roles given to each other as described on the top
        if(isOpponent)
            getIfTradeWorked.execute("accept",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","Trade");
        else
            getIfTradeWorked.execute("accept",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","Trade");

        try {
            String result = getIfTradeWorked.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);
            int resultIfTradingHappens = object.getInt("Worked");
            boolean isCardGot = object.getBoolean("CardGot");


            if(!isCardGot&&resultIfTradingHappens==1)
            {
                getOpponentCard();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }


    //here the informations are set that you are ready to merge
    private void acceptMergeTrade(){
        HttpGetter getIfTradeWorked = new HttpGetter();

        //u can only accept if you see the others card
        //if not you cant know what u merge
        if(userOfferedCardID == null || opponentOfferedCardID == null) return; //you can't accept if there are no cards in the first place

        //check if you are an opponent or not because of the roles given to each other as described on the top
        if(isOpponent)
            getIfTradeWorked.execute("accept",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","TradeMerge");
        else
            getIfTradeWorked.execute("accept",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","TradeMerge");

        try {
            String result = getIfTradeWorked.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);
            int resultIfTradingHappens = object.getInt("Worked");
            boolean isCardGot = object.getBoolean("CardGot");

            //System.out.println(resultIfTradingHappens+"-------------------------------------------------------------------------\n\n\n\n");
            if(!isCardGot&&resultIfTradingHappens==1)
            {
                getOpponentCardForMerge();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }


    //gets the card of the opponent that the merge can be fulfilled
    private void getOpponentCardForMerge(){
        HttpGetter getOpponentCard = new HttpGetter();

        if(isOpponent)
            getOpponentCard.execute("getTraded",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","Card");
        else
            getOpponentCard.execute("getTraded",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","Card");

        try {
            String result = getOpponentCard.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);

            //get the card information of the other merger
            int cardID = object.getInt("CardID");
            int cardType = object.getInt("CardType");
            String cardPicture =  object.getString("CardPicture");
            //set merged card to deck
            getMergeCardInDeck(cardType,cardPicture,cardID);

            //only one can decrease the mergecount mergecount is given because it is a reward from the quest.
            //If merged the questscore must be decreased
            if(isOpponent) {
                HttpGetter decreaseMergeCount = new HttpGetter();
                decreaseMergeCount.execute("decrease", "" + Configuration.currentUser.getId(), "" + idOfOtherUser, "MergeCount");
                String ifMergedDecreased = decreaseMergeCount.get(Configuration.timeoutTime, TimeUnit.SECONDS);
                JSONObject mergedDecreased = new JSONObject(ifMergedDecreased);
                boolean workedMerge = mergedDecreased.getBoolean("Worked?");
                if(workedMerge)
                    Log.d("SwapCardActivity","Merge was succesfully decreased");
            }

            //checks if the cards on the server are initialized
            HttpGetter setInfoThatCardGot = new HttpGetter();
            if(isOpponent){
                setInfoThatCardGot.execute("card",""+idOfOtherUser,""+Configuration.currentUser.getId()
                        ,"Opponent","Got");
            } else{
                setInfoThatCardGot.execute("card",""+Configuration.currentUser.getId(),""+idOfOtherUser
                        ,"User","Got");
            }
            String resultCardGot = setInfoThatCardGot.get( Configuration.timeoutTime,TimeUnit.SECONDS);

            if(resultCardGot.equals("OneSided")){
                Log.d(TAG, "OneSided friendrefresh");
            } else if(resultCardGot.equals("Initialized")){

                Log.d(TAG, "Initialized successful");
            } else{
                Log.d(TAG, "Failed to update");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }

    //finally gets the merged card into the deck
    private void getMergeCardInDeck(int cardType, String cardPicture,int cardID){


        //Step1 get the tier of the card the opponent offered
        GameContent.CardType cardOfEnemy = GameContent.getCardTypeByID(cardType);
        int opponentCardTier = cardOfEnemy.tier;

        int playerCardID;
        //Step2 add the tier of the  user card and the opponent card
        //if the tier of your traded card was -1 just do a normal trade
        if(tierOfTradedCard==-1) {
            playerCardID = Configuration.CARDS.add(new PlayerCard(cardType, cardPicture, cardID));
            HttpPoster postCardSwitch = new HttpPoster();
            postCardSwitch.execute("switch",cardID+"",Configuration.currentUser.getId()+"","Card");
            Toast.makeText(SwapCardActivity.this,"Merge did not work! \nYour cards got traded!",Toast.LENGTH_SHORT).show();
        } else{
            //else merge the cards
            int maxTier = opponentCardTier+ tierOfTradedCard;
            if(maxTier>3)
                maxTier = 3;

            //decrease mergecount locally so that it is shown correctly in trading
            mergeCount--;
            mergeCounting.setText("Merges* left: "+mergeCount);
            //if mergecount is smaller or equal 0 the button will disappear
            if(mergeCount<=0){
                mergeButton.setVisibility(View.GONE);
            }
            //get a new type with better tier
            int type = GameContent.getRandomCardType(maxTier);
            HttpPoster postCardSwitch = new HttpPoster();
            //set information to server which card was swapped and sets its information to the new type and user
            postCardSwitch.execute("switch",cardID+"",Configuration.currentUser.getId()+"",""+type,"CardAndUpdate");
            playerCardID = Configuration.CARDS.add(new PlayerCard(type, cardPicture, cardID));
            Toast.makeText(SwapCardActivity.this,"Your cards got merged!",Toast.LENGTH_SHORT).show();
        }


        //addviewtoscrollbar
        addViewToScrollbar(playerCardID,cardPicture);
        //delete ur offeredcard
        Configuration.CARDS.remove(userOfferedCardID); //remove PlayerCard from client registry
        cardTadeList.get(userOfferedCardID).removeView(); //remove the view from the list

        //initialize views
        userOfferedCard.updateCardInfo(null,cardPicture);
        opponentOfferedCard.updateCardInfo(null,cardPicture);
        //reset offered playerCardIDs
        opponentOfferedCardID = null;
        userOfferedCardID = null;
    }




    //Working exactly the same as the getOpponentCardasForMerge
    private void getOpponentCard(){
        HttpGetter getOpponentCard = new HttpGetter();

        if(isOpponent)
            getOpponentCard.execute("getTraded",""+idOfOtherUser,""+Configuration.currentUser.getId(),"Opponent","Card");
        else
            getOpponentCard.execute("getTraded",""+Configuration.currentUser.getId(),""+idOfOtherUser,"User","Card");

        try {
            String result = getOpponentCard.get(Configuration.timeoutTime,TimeUnit.SECONDS);
            JSONObject object = new JSONObject(result);

            int cardID = object.getInt("CardID");
            int cardType = object.getInt("CardType");
            String cardPicture =  object.getString("CardPicture"); //TODO implement pictures
            //set card to deck
            getOpponentCardInDeck(cardType,cardPicture,cardID);

            HttpGetter setInfoThatCardGot = new HttpGetter();
            if(isOpponent){
                //Todo
                setInfoThatCardGot.execute("card",""+idOfOtherUser,""+Configuration.currentUser.getId()
                        ,"Opponent","Got");
            } else{
                setInfoThatCardGot.execute("card",""+Configuration.currentUser.getId(),""+idOfOtherUser
                        ,"User","Got");
            }
            String resultCardGot = setInfoThatCardGot.get( Configuration.timeoutTime,TimeUnit.SECONDS);

            if(resultCardGot.equals("OneSided")){
                Log.d(TAG, "OneSided friendrefresh");
            } else if(resultCardGot.equals("Initialized")){

                Log.d(TAG, "Initialized successful");
            } else{
                Log.d(TAG, "Failed to update");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(SwapCardActivity.this,3,true);
        }
    }

    //setnewcardintodeck deletes urs u traded and initialize layouts
    private void getOpponentCardInDeck(int cardType, String cardPicture,int cardID){
        int playerCardID = Configuration.CARDS.add(new PlayerCard(cardType, cardPicture,cardID));

            HttpPoster postCardSwitch = new HttpPoster();
            postCardSwitch.execute("switch",cardID+"",Configuration.currentUser.getId()+"","Card");

        //addviewtoscrollbar
        addViewToScrollbar(playerCardID,cardPicture);
        //delete ur offeredcard
        Configuration.CARDS.remove(userOfferedCardID); //remove PlayerCard from client registry
        cardTadeList.get(userOfferedCardID).removeView(); //remove the view from the list


        Toast.makeText(SwapCardActivity.this,"Trade was successful!",Toast.LENGTH_SHORT).show();
        //initialize views
        userOfferedCard.updateCardInfo(null,cardPicture);
        opponentOfferedCard.updateCardInfo(null,cardPicture);
        //reset offered playerCardIDs
        opponentOfferedCardID = null;
        userOfferedCardID = null;
    }

    //Sets the information when entering the activity to distribute roles + getting name of friend and its picture
    private void setInformationsOfTrader(Bundle bundle,ImageView img,TextView name){
        if(bundle.getString("Role").equals("Opponent")){
            isOpponent = true;
        } else{
            isOpponent = false;
        }
        idOfOtherUser = bundle.getInt("Trader");
        String nameOfUser = bundle.getString("Name");
        String picture = bundle.getString("Picture");

        Configuration.getPictureOutOfStorageAndSetItToView(SwapCardActivity.this,img,picture);

        name.setText(nameOfUser);

    }

    //cards are updated serverside when dragging them to the placeholder
    private void tellServerToUpateCard(PlayerCard card, int idCard){
        if(idOfOtherUser!=-1){
            HttpGetter putCardAndGetFeedback = new HttpGetter();
            int type = card.getCardTypeID();

            if(isOpponent) {
                putCardAndGetFeedback.execute( "put", "" + idOfOtherUser, "" + Configuration.currentUser.getId(),
                        "Opponent", "" + idCard, "" + type, card.getPicture(), "Card" );
            } else{
                putCardAndGetFeedback.execute( "put", "" + Configuration.currentUser.getId(), "" + idOfOtherUser,
                        "User", "" + idCard, "" + type, card.getPicture(), "Card" );
            }

            try {
                String result = putCardAndGetFeedback.get(Configuration.timeoutTime, TimeUnit.SECONDS);
                if(!result.equals("Worked")){
                    Toast.makeText(SwapCardActivity.this,
                            "Something went wrong to store the card on the server",Toast.LENGTH_SHORT).show();
                }
                //else offeredCardID= idCard;
                //the ID of the offered card is saved in usererOfferedCard
            } catch (ExecutionException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(SwapCardActivity.this,2,true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(SwapCardActivity.this,1,true);
            } catch (TimeoutException e) {
                e.printStackTrace();
                Configuration.serverDownBehaviour(SwapCardActivity.this,0,true);
            }
        }
    }

    //adding the card to scrollview
    private void addViewToScrollbar(final Integer key,String picture) {
        ListCardLayout layout = new ListCardLayout(cardBar, key,picture);
        cardTadeList.put(key, layout);
    }

    //iterating over cards to add them to list
    private void integrateCards() {
        /*for (Integer key : Configuration.CARDS.keySet()) {
            addViewToScrollbar( key );
        }*/
        for(Pair<Integer, PlayerCard> pair : Configuration.CARDS){
            addViewToScrollbar(pair.first,pair.second.getPicture());
        }
    }

    //refresh if you enter the tradeactivity
    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    //if you leave the activity stop the handler
    @Override
    public void onStop() {
        super.onStop();

        handler.removeCallbacksAndMessages(null);
    }


    //jump to Homeactivity when pressing hte back button
    @Override
    public void onBackPressed() {
        HttpPoster poster = new HttpPoster();
        if(isOpponent) {
            poster.execute( "destroy",""+idOfOtherUser ,""+Configuration.currentUser.getId(), "Trade" );
        } else{
            poster.execute( "destroy", ""+Configuration.currentUser.getId(), ""+idOfOtherUser, "Trade" );
        }

        Intent intent = new Intent(SwapCardActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
