package com.example.steppingmonsterduel2.Util.Duel;

import android.util.Pair;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PlayerBoardUpdate {
    public Boolean isPlayerTurn = null;
    public Integer roundMana = null;
    public Integer playerHealth = null;
    public Integer opponentHealth = null;
    public Integer playerMana = null;
    public Integer opponentMana = null;
    public Integer playerDeckSize = null;
    public Integer opponentDeckSize = null;
    public Integer playerElement = null;
    public Integer opponentElement = null;
    public Integer opponentHandDiff = 0;
    private Set<Integer> handCardsRemoved = new HashSet<>();
    private SparseArray<HandCard> handCardsAdded = new SparseArray<>();
    private Map<PlayerBoardPosition, AddedMonster> addedMonsters = new HashMap<>();
    private Set<PlayerBoardPosition> removedMonsters = new HashSet<>();
    private Map<PlayerBoardPosition, MonsterUpdate> monsterUpdates = new HashMap<>();

    public PlayerBoardUpdate(JSONObject json){
        isPlayerTurn = getJSONBoolean(json, "IsPlayerTurn");
        roundMana = getJSONInteger(json, "RoundMana");
        playerHealth = getJSONInteger(json, "PlayerHealth");
        opponentHealth = getJSONInteger(json, "OpponentHealth");
        playerMana = getJSONInteger(json, "PlayerMana");
        opponentMana = getJSONInteger(json, "OpponentMana");
        playerDeckSize = getJSONInteger(json, "PlayerDeckSize");
        opponentDeckSize = getJSONInteger(json, "OpponentDeckSize");
        opponentHandDiff = getJSONInteger(json, "OpponentHandDiff");
        playerElement = getJSONInteger(json, "PlayerElement");
        opponentElement = getJSONInteger(json, "OpponentInteger");
        if(json.has("HandCardsRemoved")){
            try{
                JSONArray handCardsRemoved = json.getJSONArray("HandCardsRemoved");
                for(int i=0; i<handCardsRemoved.length(); i++){
                    this.handCardsRemoved.add(handCardsRemoved.getInt(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(json.has("HandCardsAdded")){
            try{
                JSONArray handCardsAdded = json.getJSONArray("HandCardsAdded");
                for(int i=0; i<handCardsAdded.length(); i++){
                    JSONObject cardJson = handCardsAdded.getJSONObject(i);
                    HandCard card = new HandCard(cardJson.getInt("CardTypeID"), cardJson.getInt("ImageID"));
                    this.handCardsAdded.put(cardJson.getInt("PlayerCardID"), card);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(json.has("AddedMonsters")){
            try{
                JSONArray addedMonsters = json.getJSONArray("AddedMonsters");
                for(int i=0; i<addedMonsters.length(); i++){
                    JSONObject monJson = addedMonsters.getJSONObject(i);
                    AddedMonster monster = new AddedMonster(monJson.getInt("MonsterTypeID"), monJson.getInt("ImageID"), monJson.getInt("Health"), monJson.getInt("Damage"), monJson.getBoolean("IsStunned"));
                    PlayerBoardPosition pos = new PlayerBoardPosition(monJson.getInt("SlotID"), monJson.getBoolean("OnEnemySide"));
                    this.addedMonsters.put(pos, monster);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(json.has("RemovedMonsters")){
            try{
                JSONArray removedMonsters = json.getJSONArray("RemovedMonsters");
                for(int i=0; i<removedMonsters.length(); i++){
                    JSONObject monJson = removedMonsters.getJSONObject(i);
                    PlayerBoardPosition pos = new PlayerBoardPosition(monJson.getInt("SlotID"), monJson.getBoolean("OnEnemySide"));
                    this.removedMonsters.add(pos);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(json.has("MonsterUpdates")){
            try{
                JSONArray monsterUpdates = json.getJSONArray("MonsterUpdates");
                for(int i=0; i<monsterUpdates.length(); i++){
                    JSONObject monJson = monsterUpdates.getJSONObject(i);
                    PlayerBoardPosition pos = new PlayerBoardPosition(monJson.getInt("SlotID"), monJson.getBoolean("OnEnemySide"));
                    Integer health = getJSONInteger(monJson, "Health");
                    Integer damage = getJSONInteger(monJson, "Damage");
                    Boolean isStunned = getJSONBoolean(monJson, "IsStunned");
                    Integer typeID = getJSONInteger(monJson, "MonsterTypeID");
                    Integer imageID = getJSONInteger(monJson, "ImageID");
                    MonsterUpdate mon = new MonsterUpdate(health, damage, isStunned, typeID, imageID);
                    this.monsterUpdates.put(pos, mon);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private static Integer getJSONInteger(JSONObject json, String key){
        try{
            int result = json.getInt(key);
            return result;
        } catch (JSONException e) {
            return null;
        }
    }
    private static Boolean getJSONBoolean(JSONObject json, String key){
        try{
            boolean result = json.getBoolean(key);
            return result;
        } catch (JSONException e) {
            return null;
        }
    }

    //Member getters. These are const; they can't be used to manipulate the values of the containers
    public List<Integer> getHandCardsRemoved(){
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
    }

    public static class MonsterUpdate{
        public final  Integer health;
        public final Integer damage;
        public final Boolean isStunned;
        public final Integer typeID;
        public final Integer imageID;
        public MonsterUpdate(Integer health, Integer damage, Boolean isStunned, Integer typeID, Integer imageID){
            this.health = health;
            this.damage = damage;
            this.isStunned = isStunned;
            this.typeID = typeID;
            this.imageID = imageID;
        }
    }

    public static class AddedMonster{ //same as MonsterUpdate but null values aren't allowed
        public final int typeID;
        public final int imageID;
        public final int health;
        public final int dammage;
        public final boolean isStunned;
        public AddedMonster(int typeID, int imageID, int health, int damage, boolean isStunned){
            this.typeID = typeID;
            this.imageID = imageID;
            this.health = health;
            this.dammage = damage;
            this.isStunned = isStunned;
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
}
