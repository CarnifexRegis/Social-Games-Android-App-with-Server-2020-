package InGame;

import InGame.CardType.CardType;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;
import InGame.Utility.BoardUpdatePair;
import InGame.Utility.PlayerBoardUpdate;
import InGame.Utility.PlayerSpecific;

import static InGame.CardType.CardTypes.*;
import InGame.CardType.CardTypes;
import container.Deck;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;

public class GameInstance {

    private static final int MAX_MANA = 10;
    private static final int STARTING_HEALTH = 100;
    private static final CardType[] DEFAULT_CARDS = new CardType[]{ABOMINATION, BAT, BAT, BOMBER_THE_PYROMANIAC, PHOENIX, QUICKSAND, ENT, QUEEN_BEE, DRACULA, MEDUSAS_HEAD, ANGEL, OVERGROWN, INFESTATION, MEDUSA, ARCHANGEL};

    private final PlayerSpecific<Deck> playerAccountDecks;
    private final PlayerSpecific<Element> playerElements;
    private final Map<Integer, String> cardImages = new HashMap<>();
    private ObjectNode cardImagesJSON = null;

    //helpers
    private static PlayerSpecific<BoardPosition[]> monsterPositions;
    static {
        monsterPositions = new PlayerSpecific<>(new BoardPosition[5], new BoardPosition[5]);
        for(EPlayer player : EPlayer.values()){
            for(int i=0; i<5; i++){
                monsterPositions.get(player)[i] = new BoardPosition(player, i);
            }
        }
    }

    private EPlayer whoseTurn;
    private int roundMana;
    private PlayerSpecific<List<HandCard>> decks = new PlayerSpecific<List<HandCard>>(new ArrayList<HandCard>(), new ArrayList<HandCard>());
    private PlayerSpecific<HashMap<Integer, HandCard>> handCards = new PlayerSpecific<>(new HashMap<Integer, HandCard>(), new HashMap<Integer, HandCard>());
    private PlayerSpecific<Monster[]> monsters = new PlayerSpecific<>(new Monster[5], new Monster[5]);
    private PlayerSpecific<Integer> health = new PlayerSpecific<>(STARTING_HEALTH, STARTING_HEALTH);
    private PlayerSpecific<Integer> mana = new PlayerSpecific<>(0,0);
    private BoardUpdatePair playerBoardUpdates = new BoardUpdatePair();

    private EPlayer winner = null;

    public GameInstance(@Nullable Deck deckPlayerOne, @Nullable Deck deckPlayerTwo, int elementPlayerOne, int elementPlayerTwo) {
        this.playerAccountDecks = new PlayerSpecific<>(deckPlayerOne, deckPlayerTwo);
        this.playerElements = new PlayerSpecific<>(Element.fromID(elementPlayerOne), Element.fromID(elementPlayerTwo));
        startGame();
    }

    public void startGame(){

        playerBoardUpdates.setElements(playerElements.get(EPlayer.ONE), playerElements.get(EPlayer.TWO));
        //initialize 0th turn
        roundMana = 0;
        whoseTurn = EPlayer.TWO;
        setPlayerMana(EPlayer.TWO, 0);
        endTurn(EPlayer.TWO); //start first turn

        int nextHandCardID = 0;
        for(EPlayer player : EPlayer.values()){
            setPlayerHealth(player, STARTING_HEALTH);

            if(playerAccountDecks.get(player) != null && playerAccountDecks.get(player).getDeck().size() != 0){
                for(container.Card playerCard : playerAccountDecks.get(player).getDeck()){
                    CardType cardType = CardTypes.fromID(playerCard.getType());
                    HandCard card = new HandCard(nextHandCardID, cardType, nextHandCardID);
                    addCardToDeck(player, card);
                    String cardImage = playerCard.getPicture();
                    if(cardImage != null && !cardImage.equals("null")) //in some iterations, no image meant the field was null. In others, the field was "null". I just want to be on the safe side.
                        cardImages.put(nextHandCardID, playerCard.getPicture());
                    nextHandCardID++;
                }
            } else {
                for(CardType cardType : DEFAULT_CARDS){
                    HandCard card = new HandCard(nextHandCardID, cardType, nextHandCardID);
                    addCardToDeck(player, card);
                    //dont add card image to cardimages here. I mean why? Let the client figure out what to do if a card doesn't have an image :)
                    nextHandCardID++;
                }
            }
            Collections.shuffle(decks.get(player));
            for(int i=0; i<5; i++){
                drawCard(player);
            }
        }
    }

    //------------
    //ACTIONS
    //------------

    //boolean return values mean whether the action was successful (legal).
    public boolean endTurn(EPlayer player) {
        if (player != whoseTurn) return false;
        //onRoundEnd
        for(int i=0; i<5; i++){
            Monster mon = monsters.get(whoseTurn)[i];
            if(mon != null) mon.finishRound(new BoardPosition(whoseTurn, i), this);
        }


        //actually give over turn
        whoseTurn = whoseTurn.other();
        playerBoardUpdates.setPlayerTurn(whoseTurn);
        if(whoseTurn == EPlayer.ONE) increaseRoundMana();
        setPlayerMana(whoseTurn, roundMana);
        drawCard(whoseTurn);

        //onRoundBegin
        for(int i=0; i<5; i++){
            Monster mon = monsters.get(whoseTurn)[i];
            if(mon != null) mon.enterNewRound(new BoardPosition(whoseTurn, i), this);
        }

        return true;
    }

    public Highlights getValidTargets(BoardPosition position) {
        boolean[] playerOneBoard = new boolean[5];
        boolean[] playerTwoBoard = new boolean[5];
        boolean playerOneFace = false, playerTwoFace = false;

        Monster attacker = getMonsterAtPosition(position);
        if (attacker != null)
        {
            for (int i = 0; i < 5; i++) {
                BoardPosition ones = new BoardPosition(EPlayer.ONE, i);
                BoardPosition twos = new BoardPosition(EPlayer.TWO, i);
                playerOneBoard[i] = attacker.canAttack(position, ones, this);
                playerTwoBoard[i] = attacker.canAttack(position, twos, this);
            }
            playerOneFace = attacker.canAttack(position, EPlayer.ONE, this);
            playerTwoFace = attacker.canAttack(position, EPlayer.TWO, this);
        }

        if(position.side == EPlayer.ONE) return new Highlights(playerOneBoard, playerTwoBoard, playerOneFace, playerTwoFace);
        else return new Highlights(playerTwoBoard, playerOneBoard, playerTwoFace, playerOneFace);
    }

    public Highlights getValidTargets(EPlayer player, int handCardIdentifier){
        boolean[] playerOneBoard = new boolean[5];
        boolean[] playerTwoBoard = new boolean[5];
        boolean playerOneFace = false, playerTwoFace = false;

        HandCard handCard = getHandCardFromID(player, handCardIdentifier);
        if(handCard != null){
            if(handCard.cardType.manaCost <= mana.get(player)){
                for (int i = 0; i < 5; i++) {
                   BoardPosition ones = new BoardPosition(EPlayer.ONE, i);
                   BoardPosition twos = new BoardPosition(EPlayer.TWO, i);
                   playerOneBoard[i] = handCard.cardType.canTarget(player, ones, this);
                   playerTwoBoard[i] = handCard.cardType.canTarget(player, twos, this);
                }
                playerOneFace = handCard.cardType.canTarget(player, EPlayer.ONE, this);
                playerTwoFace = handCard.cardType.canTarget(player, EPlayer.TWO, this);
            }
        }
        if(player == EPlayer.ONE) return new Highlights(playerOneBoard, playerTwoBoard, playerOneFace, playerTwoFace);
        else return new Highlights(playerTwoBoard, playerOneBoard, playerTwoFace, playerOneFace);
    }
    public boolean attack(BoardPosition attacker, BoardPosition target){
        Monster monAttacker = getMonsterAtPosition(attacker);
        if(monAttacker == null) return false;
        if(!monAttacker.canAttack(attacker, target, this)) return false;
        monAttacker.attack(attacker, target, this);
        return true;
    }
    public boolean attackFace(BoardPosition attacker, EPlayer player){
        Monster monAttacker = getMonsterAtPosition(attacker);
        if(monAttacker == null) return false;
        if(!monAttacker.canAttack(attacker, player, this)) return false;
        monAttacker.attack(attacker, player, this);
        return true;
    }
    public boolean placeCard(EPlayer player, int handCardIdentifier, BoardPosition position){
        return placeCard(player, handCardIdentifier, position, false);
    }

    public boolean placeCardOnFace(EPlayer player, int handCardIdentifier, EPlayer face){
        HandCard handCard = getHandCardFromID(player, handCardIdentifier);
        if(handCard == null) return false;
        if(handCard.cardType.manaCost > mana.get(player)) return false;
        if(!handCard.cardType.canTarget(player, face, this)) return false;
        handCard.cardType.place(player, face, handCard.imageID, this);
        addOrRemovePlayerMana(player, -handCard.cardType.manaCost);
        handCards.get(player).remove(handCardIdentifier);
        playerBoardUpdates.removeHandCard(player, handCardIdentifier);
        return true;
    }

    public PlayerBoardUpdate getUpdate( EPlayer player){
        return playerBoardUpdates.getAndReset(player);
    }
    public PlayerBoardUpdate getFullBoard( EPlayer player){
        return playerBoardUpdates.getFullBoardAndReset(player);
    }

    public boolean surrender( EPlayer player){
        endGame(player.other());
        return true;
    }

    //-------------------------------
    //CARDTYPE AND MONSTER INTERFACE
    //These guys should all modify PlayerBoardUpdates!
    //-------------------------------


    public void endGame( EPlayer winner){
        if(this.winner == null) this.winner = winner;
    }

    public void increaseRoundMana(){
        roundMana+=1;
        if(roundMana > MAX_MANA) roundMana = MAX_MANA;
        playerBoardUpdates.setRoundMana(roundMana);
    }

    public void addOrRemovePlayerMana(@Nonnull EPlayer player, int diff){
        int newMana = mana.get(player) + diff;
        setPlayerMana(player, newMana);
    }
    public void setPlayerMana(@Nonnull EPlayer player, int newMana){
        if(newMana < 0) newMana = 0;
        if(newMana > MAX_MANA) newMana = MAX_MANA;
        mana.set(newMana, player);
        playerBoardUpdates.setPlayerMana(player, newMana);
    }

    public void addOrRemovePlayerHealth(@Nonnull EPlayer player, int diff){
        int newHealth = health.get(player) + diff;
        setPlayerHealth(player, newHealth);
    }
    public void setPlayerHealth(@Nonnull EPlayer player, int newHealth){
        if(newHealth <= 0) endGame(player.other());
        health.set(newHealth, player);
        playerBoardUpdates.setPlayerHealth(player, newHealth);
    }
    public void spawnMonster(@Nonnull Monster monster, @Nonnull BoardPosition position){
        if(getMonsterAtPosition(position) != null) throw new IllegalArgumentException("There's already a monster at this position!");
        monsters.get(position.getSide())[position.getSlotID()] = monster;
        playerBoardUpdates.addMonster(monster, position);
        monster.postSpawn(position, this);
        if(monster.element == playerElements.get(position.getSide())){
            monster.applyHeal(monster.element.healthBuff, position, this);
            monster.modifyDamageBuffBy(monster.element.damageBuff, position, this);
        }
    }
    public void killMonster(@Nonnull BoardPosition position){
        Monster monster = getMonsterAtPosition(position);
        if(monster != null) monster.getKilled(position, this);
    }
    public void removeMonster(@Nonnull BoardPosition position){
        Monster[] boardSlots = monsters.get(position.getSide());
        boardSlots[position.getSlotID()] = null;
        playerBoardUpdates.removeMonster(position);
    }
    public void drawCard(@Nonnull EPlayer player){
        List<HandCard> deck = decks.get(player);
        HashMap<Integer, HandCard> hand = handCards.get(player);
        if(deck.isEmpty()) return;
        if(hand.size() >= 5) return;
        HandCard card = deck.remove(0);
        hand.put(card.identifier, card);
        playerBoardUpdates.addHandCard(player, card);
        playerBoardUpdates.setDeckSize(player, deck.size());
    }
    public void addCardToDeck(@Nonnull EPlayer player, @Nonnull HandCard card){
        List<HandCard> deck = decks.get(player);
        deck.add(card);
        playerBoardUpdates.setDeckSize(player, deck.size());
    }
    public boolean placeCard(@Nonnull EPlayer player, int handCardIdentifier, @Nonnull BoardPosition position, boolean ignoreMana){
        HandCard handCard = getHandCardFromID(player, handCardIdentifier);
        if(handCard == null) return false;
        if(!ignoreMana && handCard.cardType.manaCost > mana.get(player)) return false;
        if(!handCard.cardType.canTarget(player, position, this)) return false;
        handCard.cardType.place(player, position, handCard.imageID, this);
        if(!ignoreMana) addOrRemovePlayerMana(player, -handCard.cardType.manaCost);
        handCards.get(player).remove(handCardIdentifier);
        playerBoardUpdates.removeHandCard(player, handCardIdentifier);
        return true;
    }

    //const functions
    public @Nullable EPlayer getWinnerOrNull(){
        return winner;
    }

    public @Nullable Monster getMonsterAtPosition(@Nullable BoardPosition position){
        if(position == null) return null;
        return monsters.get(position.getSide())[position.getSlotID()];
    }
    public HandCard getHandCardFromID(@Nonnull EPlayer player, int handCardIdentifier){
        return handCards.get(player).get(handCardIdentifier);
    }
    public BoardUpdatePair getPlayerBoardUpdates(){
        return playerBoardUpdates;
    }

    public @Nullable BoardPosition getFreeBoardPositionNear(@Nonnull BoardPosition origin){
        if(getMonsterAtPosition(origin) == null) return origin;
        int radius = 1;
        while(radius < 5){
            BoardPosition testPos0 = origin.slotOffset(radius);
            if(testPos0 != null && getMonsterAtPosition(testPos0) == null) return testPos0;
            testPos0 = origin.slotOffset(-radius);
            if(testPos0 != null && getMonsterAtPosition(testPos0) == null) return testPos0;

            /*int slotID = origin.getSlotID() + radius;
            if(slotID < 5){
                BoardPosition testPos = new BoardPosition(origin.getSide(), slotID);
                if(getMonsterAtPosition(testPos) == null) return testPos;
            }
            slotID = origin.getSlotID() - radius;
            if(slotID >= 0){
                BoardPosition testPos = new BoardPosition(origin.getSide(), slotID);
                if(getMonsterAtPosition(testPos) == null) return testPos;
            }*/
            radius++;
        }
        return null;
    }

    //not const but can be treated as such
    public ObjectNode getCardImages(){
        if(cardImagesJSON != null) return cardImagesJSON; //don't need to create this from scratch every time this method is called.
        ObjectNode imgRoot = Json.newObject();
        ArrayNode array = imgRoot.arrayNode();
        for(Map.Entry<Integer, String> entry : cardImages.entrySet()){
            ObjectNode node = Json.newObject();
            node.put("ID", entry.getKey());
            node.put("URL", entry.getValue());
            array.add(node);
        }
        imgRoot.put("Images", array);
        return cardImagesJSON = imgRoot;
    }

    //BIG TODO make a const alternative
    public Map<Integer, HandCard> getPlayerHand(EPlayer player){
        return handCards.get(player);
    }

    //------------------
    //CLASSES
    //------------------
    public static class HandCard{
        public final int identifier;
        public final CardType cardType;
        public final int imageID;

        public HandCard(int identifier, CardType cardType, int imageID){
            this.identifier = identifier;
            this.cardType = cardType;
            this.imageID = imageID;
        }
    }

    public static class Highlights{
        public final boolean[] playerBoard, opponentBoard;
        public final boolean playerFace, opponentFace;
        public Highlights(boolean[] playerBoard, boolean[] opponentBoard, boolean playerFace, boolean opponentFace){
            this.playerBoard = playerBoard;
            this.opponentBoard = opponentBoard;
            this.playerFace = playerFace;
            this.opponentFace = opponentFace;
        }
        @Override public String toString(){
            String result = "OPP: ";
            result += opponentFace? "1 ":"0 ";
            for(int i=0; i<5; i++) result += opponentBoard[i]? "1":"0";
            result+="\nSELF: ";
            result+=playerFace? "1 ":" 0";
            for(int i=0; i<5; i++) result += playerBoard[i]? "1":"0";
            return result;
        }
        public ObjectNode toJSON(){
            ObjectNode hlRoot = Json.newObject();
            ArrayNode array = hlRoot.arrayNode();
            for(int i=0; i<5; i++){
                array.add(playerBoard[i] ? "1":"0");
            }
            for(int i=0; i<5; i++){
                array.add(opponentBoard[i] ? "1":"0");
            }
            array.add(playerFace ? "1":"0");
            array.add(opponentFace ? "1": "0");
            hlRoot.put("Highlights",array);
            return hlRoot;
        }
    }
}
