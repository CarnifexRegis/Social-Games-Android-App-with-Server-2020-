package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.GameContent;
import com.example.steppingmonsterduel2.Util.Duel.PlayerBoardUpdate;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.Duel.SlowLoadingMap;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;
import com.google.android.gms.tasks.OnFailureListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DuelActivity extends AppCompatActivity {

    //Server communication
    private ServerInterface serverInterface;
    String playerID;//used for server communication.
    Handler handler;
    User opponent;
    private SlowLoadingMap<Integer, Bitmap> cardImages;

    //game info
    boolean gameOver;
    Element playerElement;
    Element opponentElement;
    private boolean postGameStarted;

    //Layouts and displays!
    BoardSlot[] oppBoard;
    BoardSlot[] playerBoard;
    SparseArray<PlayerDrawnCard> playerDrawnCards;
    List<OpponentDrawnCard> oppDrawnCards;
    ViewGroup hand;
    ViewGroup oppHand;
    CorePlayerStat health, mana, oppHealth, oppMana;
    TextView deckSize, oppDeckSize;
    View deck, oppDeck;
    Button endTurnButton;
    Button surrenderButton;
    ImageView playerAvatar, oppAvatar;
    View playerAvatarHighlight, oppAvatarHighlight;
    TextView playerName, opponentName;

    //Data for displays
    int roundMana = 1;
    final int maxPlayerHealth = GameContent.MAX_PLAYER_HEALTH;

    //Interaction
    boolean myTurn;
    Click firstClick = null;

    @Override
    protected void onStop(){
        super.onStop();
        //serverInterface.quitGame();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        gameOver = false;
        serverInterface = new ServerInterface();
        playerID = Integer.toString(Configuration.currentUser.getId());
        opponent = getIntent().getParcelableExtra("Opponent");
        cardImages = new SlowLoadingMap<>();
        serverInterface.getCardImages();
        handler = new Handler();
        postGameStarted = false;

        //Member initialization
        playerDrawnCards = new SparseArray<>();
        oppDrawnCards = new ArrayList<>();
        health = new CorePlayerStat(findViewById(R.id.PlayerHealthBar), findViewById(R.id.PlayerHealth));
        mana = new CorePlayerStat(findViewById(R.id.PlayerManaBar), findViewById(R.id.PlayerMana));
        oppHealth = new CorePlayerStat(findViewById(R.id.OppHealthBar), findViewById(R.id.OppHealth));
        oppMana = new CorePlayerStat(findViewById(R.id.OppManaBar), findViewById(R.id.OppMana));
        deckSize = findViewById(R.id.PlayerCardLeft);
        oppDeckSize = findViewById(R.id.OppCardsLeft);
        deck = findViewById(R.id.PlayerDeck);
        oppDeck = findViewById(R.id.OppDeck);
        endTurnButton = findViewById(R.id.EndTurnButton);
        endTurnButton.setOnClickListener((view)->endTurn());
        surrenderButton = findViewById(R.id.SurrenderButton);
        surrenderButton.setOnClickListener((v)->surrender());
        playerAvatar = findViewById(R.id.PlayerAvatar);
        oppAvatar = findViewById(R.id.OppAvatar);
        playerAvatar.setOnClickListener((view)->click(ClickType.AVATAR_PLAYER, 0));
        oppAvatar.setOnClickListener((view)->click(ClickType.AVATAR_OPPONENT, 0));
        playerAvatarHighlight = findViewById(R.id.PlayerAvatarHighlight);
        oppAvatarHighlight = findViewById(R.id.OppAvatarHighlight);
        playerName = findViewById(R.id.PlayerName);
        opponentName = findViewById(R.id.OppName);

        oppBoard = new BoardSlot[]{
                new BoardSlot(findViewById(R.id.opp_played0)),
                new BoardSlot(findViewById(R.id.opp_played1)),
                new BoardSlot(findViewById(R.id.opp_played2)),
                new BoardSlot(findViewById(R.id.opp_played3)),
                new BoardSlot(findViewById(R.id.opp_played4))
        };
        playerBoard = new BoardSlot[]{
                new BoardSlot(findViewById(R.id.player_played0)),
                new BoardSlot(findViewById(R.id.player_played1)),
                new BoardSlot(findViewById(R.id.player_played2)),
                new BoardSlot(findViewById(R.id.player_played3)),
                new BoardSlot(findViewById(R.id.player_played4))
        };
        hand = (LinearLayout) findViewById(R.id.PlayerHand);
        oppHand = (LinearLayout) findViewById(R.id.OppHand);
        for(int i=0; i<5; i++){
            final int id = i;
            oppBoard[i].setEmpty();
            oppBoard[i].setOnClickListener((view)->{click(ClickType.BOARD_OPPONENT, id);});
            playerBoard[i].setEmpty();
            playerBoard[i].setOnClickListener((view)->{click(ClickType.BOARD_PLAYER, id);});
        }

        playerName.setText(Configuration.currentUser.getName());
        opponentName.setText(opponent.getName());

        Configuration.getPictureOutOfStorageAndSetItToView(DuelActivity.this, playerAvatar, Configuration.currentUser.getPicture());
        Configuration.getPictureOutOfStorageAndSetItToView(DuelActivity.this, oppAvatar, opponent.getPicture());

        //GET INITIAL BOARD INFO
        serverInterface.getFullBoardUpdate();

        //WHEN IT ISN'T THE PLAYER'S TURN, PERIODICALLY CHECK FOR BOARDUPDATES
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /*if(!myTurn)*/ serverInterface.getBoardUpdate();
                handler.postDelayed(this, Configuration.REFRESH_TIME);
            }
        };
        handler.postDelayed(runnable, Configuration.REFRESH_TIME);
    }

    //Board Updates
    void setMyTurn(boolean myTurn){
        this.myTurn = myTurn;
    }

    void applyBoardUpdate(PlayerBoardUpdate update){
        if(update == null){
            if(!gameOver) throw new IllegalArgumentException("player update was null!");
            else return;
        }
        if(update.isPlayerTurn != null) setMyTurn(update.isPlayerTurn);
        if(update.roundMana != null) roundMana = update.roundMana;
        if(update.playerHealth != null) health.setValue(update.playerHealth, maxPlayerHealth);
        if(update.opponentHealth != null) oppHealth.setValue(update.opponentHealth, maxPlayerHealth);
        if(update.playerMana != null) mana.setValue(update.playerMana, roundMana);
        if(update.opponentMana != null) oppMana.setValue(update.opponentMana, roundMana);
        if(update.playerDeckSize != null) setPlayerDeckSize(update.playerDeckSize);
        if(update.opponentDeckSize != null) setOppDeckSize(update.opponentDeckSize);
        if(update.playerElement != null) playerElement = Element.fromID(update.playerElement);
        if(update.opponentElement != null) opponentElement = Element.fromID(update.opponentElement);
        if(update.opponentHandDiff != null) {
            if(update.opponentHandDiff > 0){
                for(int i=0; i<update.opponentHandDiff; i++){
                    View newCard = addOppDrawnCardView();
                    OpponentDrawnCard drawnCard = new OpponentDrawnCard(newCard);
                    oppDrawnCards.add(drawnCard);
                }
            }
            else {
                for(int i=0; i>update.opponentHandDiff; i--){
                    OpponentDrawnCard removedCard = oppDrawnCards.remove(0);
                    removedCard.removeView();
                }
            }
        }
        for(int id : update.getHandCardsRemoved()) {
            playerDrawnCards.get(id).removeView();
            playerDrawnCards.remove(id);
        }
        for(Pair<Integer, PlayerBoardUpdate.HandCard> heldCard : update.getHandCardsAdded()){
            View newCard = addDrawnCardView();
            PlayerDrawnCard drawnCard = new PlayerDrawnCard(heldCard.first, heldCard.second.typeID, newCard, heldCard.second.imageID);
            drawnCard.setOnClickListener((view)->click(ClickType.DRAWN_CARD, drawnCard.id));
            playerDrawnCards.put(heldCard.first, drawnCard);
        }
        for(PlayerBoardUpdate.PlayerBoardPosition removedMon : update.getRemovedMonsters()){
            BoardSlot boardSlot = (removedMon.onEnemySide? oppBoard : playerBoard)[removedMon.slotID];
            if(boardSlot.isEmpty()) System.out.println("Maxi: CLIENTSIDE WARNING: tried to remove monster at "+removedMon+" even though there was none.");
            boardSlot.setEmpty();
        }
        for(Pair<PlayerBoardUpdate.PlayerBoardPosition, PlayerBoardUpdate.AddedMonster> pair : update.getAddedMonsters()){
            BoardSlot boardSlot = (pair.first.onEnemySide? oppBoard : playerBoard)[pair.first.slotID];
            PlayerBoardUpdate.AddedMonster mon = pair.second;
            boardSlot.create(mon.typeID, mon.health, mon.dammage, mon.imageID);
            boardSlot.setStunned(mon.isStunned);
        }
        for(Pair<PlayerBoardUpdate.PlayerBoardPosition, PlayerBoardUpdate.MonsterUpdate> pair : update.getMonsterUpdates()){
            BoardSlot boardSlot = (pair.first.onEnemySide? oppBoard : playerBoard)[pair.first.slotID];
            PlayerBoardUpdate.MonsterUpdate mon = pair.second;
            if(boardSlot.isEmpty()){
                throw new RuntimeException("Maxi: Client received a monster update even thouogh there was no monster to update. That means Maxi fucked something up big time, tell him that!");
            }
            else {
                if(mon.typeID != null) boardSlot.setTypeInfo(mon.typeID);
                if(mon.health != null) boardSlot.setHealth(mon.health);
                if(mon.damage != null) boardSlot.setDamage(mon.damage);
                if(mon.isStunned != null) boardSlot.setStunned(mon.isStunned);
            }
        }
    }

    //returns whether at least one possible move exists.
    boolean applyHighlights(@Nullable PlayerHighlights highlights){
        if(highlights == null){ //unhighlight everything
            for(int i=0; i<5; i++){
                playerBoard[i].setHighlight(false);
                oppBoard[i].setHighlight(false);
            }
            highlightPlayerAvatar(false);
            highlightOppAvatar(false);
            return false;
        }
        else{
            boolean somethingHighlighted = false;
            if(!myTurn) System.out.println("Maxi: !!!This should never happen! Highlights were applied even though it wasn't the user's turn!");
            for(int i=0; i<5; i++){
                boolean highlighted = highlights.playerBoard[i];
                if(highlighted) somethingHighlighted = true;
                playerBoard[i].setHighlight(highlighted);
                highlighted = highlights.opponentBoard[i];
                if(highlighted) somethingHighlighted = true;
                oppBoard[i].setHighlight(highlighted);
            }
            if(highlights.playerFace) somethingHighlighted = true;
            highlightPlayerAvatar(highlights.playerFace);
            if(highlights.opponentFace) somethingHighlighted = true;
            highlightOppAvatar(highlights.opponentFace);
            return somethingHighlighted;
        }
    }

    //Helpers for BoardUpdates

    void setPlayerDeckSize(int count){
        deckSize.setText(String.format(Locale.US, "%d", count));
        deck.setVisibility(count == 0? View.INVISIBLE : View.VISIBLE);
        deckSize.invalidate();
        deck.invalidate();
    }
    void setOppDeckSize(int count){
        oppDeckSize.setText(String.format(Locale.US, "%d", count));
        oppDeck.setVisibility(count == 0? View.INVISIBLE : View.VISIBLE);
        oppDeckSize.invalidate();
        oppDeck.invalidate();
    }

    View addDrawnCardView(){
        View view = getLayoutInflater().inflate(R.layout.arena_card_held_player, hand, false);
        hand.addView(view);
        return view;
    }
    View addOppDrawnCardView(){
        View view = getLayoutInflater().inflate(R.layout.arena_card_held_opponent, oppHand, false);
        oppHand.addView(view);
        return view;
    }

    //Helpers for Highlights
    private void highlightPlayerAvatar(boolean highlighted){
        playerAvatarHighlight.setVisibility(highlighted? View.VISIBLE : View.INVISIBLE);
    }
    private void highlightOppAvatar(boolean highlighted){
        oppAvatarHighlight.setVisibility(highlighted? View.VISIBLE : View.INVISIBLE);
    }


    //Clicks
    void click(ClickType type, int slotID){
        if(!myTurn) return;
        Click thisClick = new Click(type, slotID);
        boolean isFirstClick = firstClick == null;
        if(!isFirstClick){
            if(type == ClickType.DRAWN_CARD) isFirstClick = true; //weird flow control.
            else if(firstClick.execute(thisClick)){
                System.out.println("Maxi: VALID CLICK");
            }
            else System.out.println("Maxi: INVALID CLICK");
            firstClick = null;
            applyHighlights(null);
        }
        if(isFirstClick) {
            firstClick = thisClick;
            if(!firstClick.applyHighlights()) firstClick = null;
        }
    }
    void endTurn(){
        firstClick = null;
        applyHighlights(null);
        if(!myTurn) {
            System.out.println("Maxi: It's not your turn, mate ;)");
            return;
        }
        if(!serverInterface.endTurn()) {
            System.out.println("Maxi: Server said it wasn't your turn. Maxi probably fucked something up, please tell him!");
        }
    }

    void surrender(){
        handler.removeCallbacksAndMessages(null); //no need to still check for updates.
        if(!serverInterface.quitGame()){ //surrenders the game. The server automatically responds with "Defeat" and the game closes
            //if something went wrong with server communication, just jumps to the home menu.
            new AlertDialog.Builder(DuelActivity.this)
                .setTitle("Server communication failed!")
                .setMessage("You surrendered but the server was on vacation. This game was probably dropped.")
                .setPositiveButton( "OK", (dialog, which)->{
                    Intent intent = new Intent(DuelActivity.this, HomeActivity.class);
                    startActivity(intent);
                }).show();
        }
    }

    //---------------
    //Classes
    //---------------
    class BoardSlot {
        private final ImageView button;
        private final ImageView image;
        private final View card;
        private final View highlight;
        private final TextView health;
        private final TextView damage;
        private final TextView name;
        private final View stun;
        private final ImageView border;
        private boolean visible;

        public BoardSlot(View slotView){
            this.card = (ViewGroup) slotView.findViewById(R.id.card);
            this.image = slotView.findViewById(R.id.image);
            this.highlight = slotView.findViewById(R.id.highlight);
            this.button = (ImageView) slotView.findViewById(R.id.button);
            this.health = (TextView) slotView.findViewById(R.id.health);
            this.damage = (TextView) slotView.findViewById(R.id.damage);
            this.name = (TextView) slotView.findViewById(R.id.name);
            this.stun = slotView.findViewById(R.id.stun);
            this.border = slotView.findViewById(R.id.border);
            this.setEmpty();
            this.setStunned(false);
            this.setHighlight(false);
        }

        //useful stuff
        public void setEmpty(){
            setVisibility(false);
        }
        public void create(int monsterTypeID, int healthPoints, int damagePoints, int imageID){
            setVisibility(true);
            setStunned(false);
            setTypeInfo(monsterTypeID);
            setHealth(healthPoints);
            setDamage(damagePoints);
            setImage(imageID);
        }
        public void setOnClickListener(View.OnClickListener listener){
            button.setOnClickListener(listener);
        }
        public void setOnLongClickListener(View.OnLongClickListener listener){
            button.setOnLongClickListener(listener);
        }

        public void setHealth(int healthPoints){
            setTextInt(health, healthPoints);
        }
        public void setDamage(int damagePoints){
            setTextInt(damage, damagePoints);
        }
        public void setImage(int imageID){
            cardImages.slowGet(imageID, image::setImageBitmap);
        }
        public void setStunned(boolean stunned){
            stun.setVisibility(stunned? View.VISIBLE : View.INVISIBLE);
        }
        public boolean isEmpty(){
            return !visible;
        }
        public void setHighlight(boolean highlighted){
            highlight.setVisibility(highlighted? View.VISIBLE : View.INVISIBLE);
        }
        public void setTypeInfo(int monsterTypeID){
            GameContent.MonsterType monsterType = GameContent.getMonsterByID(monsterTypeID);
            setName(monsterType.name);
            setElement(monsterType.element);
            setOnLongClickListener((view)->{
                new AlertDialog.Builder(DuelActivity.this)
                        .setTitle("Card Info")
                        .setMessage(monsterType.descriptionLong)
                        .setPositiveButton( "OK", null).show();
                return true;
            });
        }

        private void setName(String name){
            this.name.setText(name);
        }
        private void setElement(Element element){
            border.setImageResource(element.monsterResourceID);
        }
        private void setVisibility(boolean visible){
            card.setVisibility(visible? View.VISIBLE : View.INVISIBLE);
            this.visible = visible;
        }
    }

    class PlayerDrawnCard {
        private final int id;
        private final View card;
        private final TextView name;
        private final TextView mana;
        private final TextView health;
        private final TextView damage;
        private final ImageButton button;
        private final ImageView border;
        private final ImageView image;
        private final TextView description;
        public PlayerDrawnCard(int cardID, int typeID, View card, int imageID){
            this.id = cardID;
            this.card = card;
            this.name = card.findViewById(R.id.name);
            this.mana = card.findViewById(R.id.mana);
            this.health = card.findViewById(R.id.health);
            this.damage = card.findViewById(R.id.damage);
            this.button = card.findViewById(R.id.button);
            this.border = card.findViewById(R.id.border);
            this.image = card.findViewById(R.id.image);
            this.description = card.findViewById(R.id.description);
            setTypeInfo(typeID);
            setImage(imageID);
        }

        public void removeView(){
            ((ViewGroup)card.getParent()).removeView(card);
        }

        public void setOnClickListener(View.OnClickListener listener){
            button.setOnClickListener(listener);
        }
        public void setOnLongClickListener(View.OnLongClickListener listener){
            button.setOnLongClickListener(listener);
        }

        private void setTypeInfo(int typeID){
            GameContent.CardType cardType = GameContent.getCardTypeByID(typeID);
            setName(cardType.name);
            setMana(cardType.mana);
            if(cardType instanceof GameContent.MonsterType){
                GameContent.MonsterType monster = (GameContent.MonsterType) cardType;
                setMonsterStatVisibility(true);
                setHealth(monster.health + (playerElement == monster.element ? playerElement.healthBuff : 0));
                setDamage(monster.damage + (playerElement == monster.element ? playerElement.damageBuff : 0));
                setElement(monster.element, true);
            }
            else {
                setMonsterStatVisibility(false);
                setElement(cardType.element, false);
                setDescription(cardType.descriptionShort);
            }
            setOnLongClickListener((view)->{
                new AlertDialog.Builder(DuelActivity.this)
                        .setTitle("Card Info")
                        .setMessage(cardType.descriptionLong)
                        .setPositiveButton( "OK", null).show();
                return true;
            });
        }
        private void setImage(int imageID){
            cardImages.slowGet(imageID, image::setImageBitmap);
        }
        private void setName(String name){
            this.name.setText(name);
        }
        private void setMana(int value){
            setTextInt(mana, value);
        }
        private void setHealth(int value){
            setTextInt(health, value);
        }
        private void setDamage(int value){
            setTextInt(damage, value);
        }
        private void setDescription(String desc) {this.description.setText(desc);}
        private void setMonsterStatVisibility(boolean visible){
            health.setVisibility(visible? View.VISIBLE : View.INVISIBLE);
            damage.setVisibility(visible? View.VISIBLE : View.INVISIBLE);
            description.setVisibility(visible? View.INVISIBLE : View.VISIBLE); //yes, monsters don't have descriptions.
        }
        private void setElement(Element element, boolean isMonster){
            border.setImageResource(isMonster ? element.monsterResourceID : element.spellResourceID);
        }
    }

    private static class OpponentDrawnCard{
        final View card;
        public OpponentDrawnCard(View card){
            this.card = card;
        }
        public void removeView(){
            ((ViewGroup)card.getParent()).removeView(card);
        }
    }

    private static class CorePlayerStat {
        private final ProgressBar bar;
        private final TextView text;
        public CorePlayerStat(ProgressBar bar, TextView text){
            this.bar = bar;
            this.text = text;
        }
        public void setValue(int value, int max){
            bar.setProgress((value * 100) / max);
            setTextInt(text, value);
            bar.invalidate();
            text.invalidate();
        }
    }

    private static void setTextInt(TextView view, int value) {
        view.setText(String.format(Locale.US, "%d", value));
    }

    private enum ClickType{
        DRAWN_CARD, BOARD_PLAYER, BOARD_OPPONENT, AVATAR_PLAYER, AVATAR_OPPONENT;
    }

    private class Click {
        final ClickType type;
        final int meta;
        Click(ClickType type, int meta){
            this.type = type;
            this.meta = meta;
        }
        boolean execute(Click secondClick){
            switch(type) {
                case DRAWN_CARD: //maybe place a new card?
                    switch (secondClick.type) {
                        case DRAWN_CARD:
                            return false;
                        case BOARD_PLAYER:
                            return serverInterface.placeCard(this.meta, false, secondClick.meta);
                        case BOARD_OPPONENT:
                            return serverInterface.placeCard(this.meta, true, secondClick.meta);
                        case AVATAR_PLAYER:
                            return serverInterface.placeCardOnFace(this.meta, false);
                        case AVATAR_OPPONENT:
                            return serverInterface.placeCardOnFace(this.meta, true);
                    }
                case BOARD_PLAYER:
                    switch (secondClick.type) {
                        case DRAWN_CARD:
                            return false;
                        case BOARD_PLAYER:
                            return serverInterface.attack(this.meta, false, secondClick.meta);
                        case BOARD_OPPONENT:
                            return serverInterface.attack(this.meta, true, secondClick.meta);
                        case AVATAR_PLAYER:
                            return serverInterface.attackOnFace(this.meta, false);
                        case AVATAR_OPPONENT:
                            return serverInterface.attackOnFace(this.meta, true);
                    }
                case BOARD_OPPONENT:
                    return false;
                case AVATAR_PLAYER:
                    return false;
                case AVATAR_OPPONENT:
                    return false;
                default:
                    return false; //just needed for compiler. obviously all types are covered here.
            }
        }
        boolean applyHighlights(){
            switch(type){
                case DRAWN_CARD:
                    return serverInterface.getValidTargetsForCard(meta);
                case BOARD_PLAYER:
                    return serverInterface.getValidTargetsForMonster(meta);
                case BOARD_OPPONENT:
                    DuelActivity.this.applyHighlights(null);
                    return false;
                case AVATAR_PLAYER:
                    DuelActivity.this.applyHighlights(null);
                    return false;
                case AVATAR_OPPONENT:
                    DuelActivity.this.applyHighlights(null);
                    return false;
                default:
                    DuelActivity.this.applyHighlights(null);
                    return false; //just needed for compiler. obviously all types are covered here.
            }
        }
    }

    private void startPostGameActivity(boolean playerVictory){
        gameOver = true;
        if(postGameStarted) return; //so this function can never be called twice!
        postGameStarted = true;

        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(DuelActivity.this, GameDecisionActivity.class);
        intent.putExtra("Opponent", opponent);
        intent.putExtra("Victory", playerVictory);
        startActivity(intent);

        finish();
    }

    //SERVER INTERFACE FUNCTIONS
    private class ServerInterface{
        boolean quitGame(){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "Surrender", playerID);
            return applyServerResponse(result);
        }

        void getCardImages(){
            String result = HttpGetter.safeGet(this::displayProblem,"InGame", "GetCardImages", playerID);
            try{
                JSONObject json = new JSONObject(result);
                JSONArray array = json.getJSONArray("Images");
                for(int i=0; i<array.length(); i++){
                    JSONObject entry = array.getJSONObject(i);
                    final int imageID = entry.getInt("ID");
                    final String imageURL = entry.getString("URL");
                    Consumer<Bitmap> onSuccess = (bm)-> cardImages.put(imageID, bm);
                    Configuration.getPictureOutOfStorageAnd(imageURL, onSuccess, this::displayProblem);
                }
            } catch (JSONException e) {
                displayProblem(e);
            }
        }

        boolean getBoardUpdate(){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "GetUpdate", playerID);
            return applyServerResponse(result);
        }
        boolean getFullBoardUpdate(){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "GetFullBoard", playerID);
            return applyServerResponse(result);
        }
        private boolean applyServerResponse(String response){
            System.out.println("Maxi: PBU FROM SERVER:\n"+response);
            if(response == null) return false;
            if(response.equals("Victory")) {
                startPostGameActivity(true);
                return true;
            }
            if(response.equals("Defeat")){
                startPostGameActivity(false);
                return true;
            }
            if(response.equals("InvalidMove")){
                displayProblem("Invalid Move!"); //If this ever happens, I (Maxi) fucked something up
                return false;
            }
            if(response.equals("fail")){
                displayProblem("ServerResponse: fail");
                return false;
            }
            try{
                JSONObject json = new JSONObject(response);
                applyBoardUpdate(new PlayerBoardUpdate(json));
                return true;
            } catch (JSONException e) {
                displayProblem(e);
                return false;
            }
        }
        boolean endTurn(){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "EndTurn", playerID);
            return applyServerResponse(result);
        }
        boolean placeCard(int handCardID, boolean onEnemySide, int slotID){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "PlaceCard", playerID, handCardID, onEnemySide, slotID);
            return applyServerResponse(result);
        }
        boolean placeCardOnFace(int handCardID, boolean onEnemyFace){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "PlaceCardOnFace", playerID, handCardID, onEnemyFace);
            return applyServerResponse(result);
        }
        boolean attack(int attackerSlotID, boolean onEnemySide, int targetSlotID){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "Attack", playerID, attackerSlotID, onEnemySide, targetSlotID);
            return applyServerResponse(result);
        }
        boolean attackOnFace(int attackerSlotID, boolean onEnemyFace){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "AttackFace", playerID, attackerSlotID, onEnemyFace);
            return applyServerResponse(result);
        }
        boolean getValidTargetsForCard(int handCardID){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "CardTargets", playerID, handCardID);
            return applyServerHighlights(result);
        }
        boolean getValidTargetsForMonster(int slotID){
            String result = HttpGetter.safeGet(this::displayProblem, "InGame", "MonsterTargets", playerID, slotID);
            return applyServerHighlights(result);
        }
        private boolean applyServerHighlights(String response){
            System.out.println("Maxi: Highlights from server:\t"+response);
            if(response == null){
                applyHighlights(null);
                return false;
            }
            try{
                JSONObject json = new JSONObject(response);
                return applyHighlights(new PlayerHighlights(json));
            } catch (JSONException e) {
                displayProblem(e);
                return false;
            }
        }
        private void displayProblem(Object o){
            System.out.println("Maxi: Server Communication problem:\n"+o);
            Toast.makeText(DuelActivity.this, o.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private static class PlayerHighlights{
        final boolean[] playerBoard, opponentBoard;
        final boolean playerFace, opponentFace;
        PlayerHighlights(boolean[] playerBoard, boolean[] opponentBoard, boolean playerFace, boolean opponentFace){
            this.playerBoard = playerBoard;
            this.opponentBoard = opponentBoard;
            this.playerFace = playerFace;
            this.opponentFace = opponentFace;
        }

        PlayerHighlights(JSONObject json){
            try{
                JSONArray array = json.getJSONArray("Highlights");
                boolean[] playerBoard = new boolean[5];
                boolean[] opponentBoard = new boolean[5];
                boolean playerFace, opponentFace;
                for(int i=0; i<5; i++){
                    String value = array.getString(i);
                    playerBoard[i] = value.equals("1");
                }
                for(int i=5; i<10; i++){
                    String value = array.getString(i);
                    opponentBoard[i-5] = value.equals("1");
                }
                String value = array.getString(10);
                playerFace = value.equals("1");
                value = array.getString(11);
                opponentFace = value.equals("1");
                this.playerBoard = playerBoard;
                this.opponentBoard = opponentBoard;
                this.playerFace = playerFace;
                this.opponentFace = opponentFace;
            } catch (JSONException e) {
                throw new RuntimeException("Maxi: Couldn't parse highlights json:\n"+json);
            }
        }
    }
}