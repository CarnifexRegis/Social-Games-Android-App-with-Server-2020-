package InGame.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;

public class PlayerBoardUpdate {
    public Boolean isPlayerTurn = null;
    public Integer roundMana = null;
    public Integer playerHealth = null;
    public Integer opponentHealth = null;
    public Integer playerMana = null;
    public Integer opponentMana = null;
    public Integer playerDeckSize = null;
    public Integer opponentDeckSize = null;
    public Integer opponentHandDiff = 0;
    public Integer playerElement = null;
    public Integer opponentElement = null;
    private Set<Integer> handCardsRemoved = new HashSet<>();
    private Map<Integer, HandCard> handCardsAdded = new HashMap<>();
    private Map<PlayerBoardPosition, AddedMonster> addedMonsters = new HashMap<>();
    private Set<PlayerBoardPosition> removedMonsters = new HashSet<>();
    private Map<PlayerBoardPosition, MonsterUpdate> monsterUpdates = new HashMap<>();

    public PlayerBoardUpdate(){
    }

    //Member getters. These are const; they can't be used to manipulate the values of the containers
    /*public List<Integer> getHandCardsRemoved(){
        return new ArrayList<>(handCardsRemoved);
    }
    public List<Pair<Integer, HandCard>> getHandCardsAdded(){
        List<Pair<Integer, HandCard>> result = new ArrayList<>();
        for(int i=0; i<handCardsAdded.size(); i++){
            int key = handCardsAdded.keyAt(i);
            result.add(new Pair<>(key, handCardsAdded.get(key)));
        }
        return result;
    }
    public List<Pair<PlayerBoardPosition, AddedMonster>> getAddedMonsters(){
        List<Pair<PlayerBoardPosition, AddedMonster>> result = new ArrayList<>();
        for(Map.Entry<PlayerBoardPosition, AddedMonster> entry : addedMonsters.entrySet()){
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    public List<PlayerBoardPosition> getRemovedMonsters(){
        return new ArrayList<>(removedMonsters);
    }
    public List<Pair<PlayerBoardPosition, MonsterUpdate>> getMonsterUpdates(){
        List<Pair<PlayerBoardPosition, MonsterUpdate>> result = new ArrayList<>();
        for(Map.Entry<PlayerBoardPosition, MonsterUpdate> entry : monsterUpdates.entrySet()){
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }*/

    public ObjectNode toJSON(){
        /*ObjectNode root = Json.newObject();
        ArrayNode array = root.arrayNode();
        for(int i=0; i<8; i++){
            ObjectNode node = Json.newObject();
            node.put(Integer.toString(i), Integer.toString(i*10));
            array.add(node);
        }
        root.put("Decs", array);*/

        ObjectNode pbuRoot = Json.newObject();
        if(isPlayerTurn != null) pbuRoot.put("IsPlayerTurn", isPlayerTurn);
        if(roundMana != null) pbuRoot.put("RoundMana", roundMana);
        if(playerHealth != null) pbuRoot.put("PlayerHealth", playerHealth);
        if(opponentHealth != null) pbuRoot.put("OpponentHealth", opponentHealth);
        if(playerMana != null) pbuRoot.put("PlayerMana", playerMana);
        if(opponentMana != null) pbuRoot.put("OpponentMana", opponentMana);
        if(playerDeckSize!= null) pbuRoot.put("PlayerDeckSize", playerDeckSize);
        if(opponentDeckSize!= null) pbuRoot.put("OpponentDeckSize", opponentDeckSize);
        if(opponentHandDiff!= null) pbuRoot.put("OpponentHandDiff", opponentHandDiff);
        if(playerElement!= null) pbuRoot.put("PlayerElement", playerElement);
        if(opponentElement!= null) pbuRoot.put("OpponentElement", opponentElement);
        if(!handCardsRemoved.isEmpty()){
            ArrayNode array = pbuRoot.arrayNode();
            for(Integer cardID : handCardsRemoved){
                array.add(cardID);
            }
            pbuRoot.put("HandCardsRemoved", array);
        }
        if(!handCardsAdded.isEmpty()){
            ArrayNode array = pbuRoot.arrayNode();
            for(Map.Entry<Integer, HandCard> entry : handCardsAdded.entrySet()){
                ObjectNode card = Json.newObject();
                card.put("PlayerCardID", entry.getKey());
                card.put("CardTypeID", entry.getValue().typeID);
                card.put("ImageID", entry.getValue().imageID);
                array.add(card);
            }
            pbuRoot.put("HandCardsAdded", array);
        }
        if(!addedMonsters.isEmpty()){
            ArrayNode array = pbuRoot.arrayNode();
            for(Map.Entry<PlayerBoardPosition, AddedMonster> entry : addedMonsters.entrySet()){
                ObjectNode monster = Json.newObject();
                monster.put("SlotID", entry.getKey().slotID);
                monster.put("OnEnemySide", entry.getKey().onEnemySide);
                monster.put("MonsterTypeID", entry.getValue().typeID);
                monster.put("ImageID", entry.getValue().imageID);
                monster.put("Health", entry.getValue().health);
                monster.put("Damage", entry.getValue().damage);
                monster.put("IsStunned", entry.getValue().isStunned);
                array.add(monster);
            }
            pbuRoot.put("AddedMonsters", array);
        }
        if(!removedMonsters.isEmpty()){
            ArrayNode array = pbuRoot.arrayNode();
            for(PlayerBoardPosition pos : removedMonsters){
                ObjectNode posJson = Json.newObject();
                posJson.put("SlotID", pos.slotID);
                posJson.put("OnEnemySide", pos.onEnemySide);
                array.add(posJson);
            }
            pbuRoot.put("RemovedMonsters", array);
        }
        if(!monsterUpdates.isEmpty()){
            ArrayNode array = pbuRoot.arrayNode();
            for(Map.Entry<PlayerBoardPosition, MonsterUpdate> entry : monsterUpdates.entrySet()){
                MonsterUpdate mu = entry.getValue();
                ObjectNode muJson = Json.newObject();
                muJson.put("SlotID", entry.getKey().slotID);
                muJson.put("OnEnemySide", entry.getKey().onEnemySide);
                if(mu.health != null) muJson.put("Health", mu.health);
                if(mu.damage != null) muJson.put("Damage", mu.damage);
                if(mu.isStunned != null) muJson.put("IsStunned", mu.isStunned);
                if(mu.typeID != null) muJson.put("MonsterTypeID", mu.typeID);
                if(mu.imageID != null) muJson.put("ImageID", mu.imageID);
                array.add(muJson);
            }
            pbuRoot.put("MonsterUpdates", array);
        }

        return pbuRoot;
    }

    public static class MonsterUpdate{
        public Integer health;
        public Integer damage;
        public Boolean isStunned;
        public Integer typeID;
        public Integer imageID;
        public MonsterUpdate(Integer health, Integer damage, Boolean isStunned, Integer typeID, Integer imageID){
            this.health = health;
            this.damage = damage;
            this.isStunned = isStunned;
            this.typeID = typeID;
            this.imageID = imageID;
        }
        public static MonsterUpdate healthUpdate(int health){ //pure shortcut
            return new MonsterUpdate(health, null, null, null, null);
        }
        public static MonsterUpdate stunUpdate(boolean isStunned){ //pure shortcut
            return new MonsterUpdate(null, null, isStunned, null, null);
        }
        public static MonsterUpdate damageUpdate(int damage){ //pure shortcut
            return new MonsterUpdate(null, damage, null, null, null);
        }
        public void applyUpdate(MonsterUpdate update){
            if(update.health != null) health = update.health;
            if(update.damage != null) damage = update.damage;
            if(update.isStunned != null) isStunned = update.isStunned;
            if(update.typeID != null) typeID = update.typeID;
            if(update.imageID != null) imageID = update.imageID;
        }
    }

    public static class AddedMonster{ //same as MonsterUpdate but null values aren't allowed
        public int typeID;
        public int imageID;
        public int health;
        public int damage;
        public boolean isStunned;
        public AddedMonster(int typeID, int imageID, int health, int damage, boolean isStunned){
            this.typeID = typeID;
            this.imageID = imageID;
            this.health = health;
            this.damage = damage;
            this.isStunned = isStunned;
        }
        public void applyUpdate(MonsterUpdate update){
            if(update.health != null) health = update.health;
            if(update.damage != null) damage = update.damage;
            if(update.isStunned != null) isStunned = update.isStunned;
            if(update.typeID != null) typeID = update.typeID;
            if(update.imageID != null) imageID = update.imageID;
        }
    }

    public static class HandCard {
        public final int typeID;
        public final int imageID;

        public HandCard(int typeID, int imageID) {
            this.typeID = typeID;
            this.imageID = imageID;
        }
    }

    public static class PlayerBoardPosition{
        public final int slotID;
        public final boolean onEnemySide;
        public PlayerBoardPosition(int slotID, boolean onEnemySide){
            this.slotID = slotID;
            this.onEnemySide = onEnemySide;
        }
        @Override public boolean equals(Object other){
            PlayerBoardPosition pbp = (PlayerBoardPosition)other;
            if(other == null) return false;
            else return slotID == pbp.slotID && onEnemySide == pbp.onEnemySide;
        }
        @Override public int hashCode() {
            return Objects.hash(slotID, onEnemySide);
        }
        @Override public String toString() { return (onEnemySide? "Opponent[" : "Player[") + slotID +"]";}
    }

    public void removeHandCard(int identifier){
        handCardsAdded.remove(identifier);
        handCardsRemoved.add(identifier);
    }
    public void addHandCard(int identifier, HandCard card){
        if(handCardsRemoved.contains(identifier)) throw new RuntimeException("You tried to add a card that has already been removed. That means Maxi fucked up with the CardIDs. Please go tell him!");
        handCardsAdded.put(identifier, card);
    }
    public void removeMonster(PlayerBoardPosition pos){
        if(removedMonsters.contains(pos)) throw new RuntimeException("The same monster was removed twice. That means Maxi fucked up with either BoardUpdatePair or how it's being used in GameInstance. Please go tell him!");
        addedMonsters.remove(pos);
        monsterUpdates.remove(pos);
        removedMonsters.add(pos);
    }
    public void updateMonster(PlayerBoardPosition pos, MonsterUpdate mon){
        if(removedMonsters.contains(pos)) throw new RuntimeException("A removed monster was updated. That means Maxi fucked up with either BoardUpdatePair or how it's being used in GameInstance. Please go tell him!");
        if(addedMonsters.containsKey(pos)){
            AddedMonster newMon = addedMonsters.get(pos);
            newMon.applyUpdate(mon);
        }
        else if(monsterUpdates.containsKey(pos)){
            MonsterUpdate update = monsterUpdates.get(pos);
            update.applyUpdate(mon);
        }
        else monsterUpdates.put(pos, mon);
    }
    public void addMonster(PlayerBoardPosition pos, AddedMonster mon){
        if(addedMonsters.containsKey(pos)) throw new RuntimeException("A monster was added twice. That means Maxi fucked up with either BoardUpdatePair or how it's being used in GameInstance. Please go tell him!");
        if(monsterUpdates.containsKey(pos)) throw new RuntimeException("An updated monster was added again. That means Maxi fucked up with either BoardUpdatePair or how it's being used in GameInstance. Please go tell him!");
        removedMonsters.remove(pos);
        addedMonsters.put(pos, mon);
    }
    public void setElements(int playerElement, int opponentElement){
        this.playerElement = playerElement;
        this.opponentElement = opponentElement;
    }

}
