package InGame.Monster;

import javax.annotation.Nullable;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;


public class Monster {

    public final int typeID;
    public final Element element;

    //only modify these fields in the methods applyDamage, applyHeal, modifyDamageBy, and setDamageTo!
    private int imageID;
    private int currentHealth;
    private int baseDamage;
    private int stunnedRoundsLeft = 0;
    protected boolean hasAttackedThisRound = false;
    private int damageBuff = 0;


    public Monster(int typeID, Element element, int baseHealth, int baseDamage, int imageID){
        this.typeID = typeID;
        this.element = element;
        this.currentHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.imageID = imageID;
    }

    //should be called by GameInstance or update the BoardUpdate
    public final boolean attack(BoardPosition self, BoardPosition target, GameInstance game){ //attack other monster
        if(canAttack(self, target, game)){
            onAttack(self, target, game);
            return true;
        }
        return false;
    }
    public final boolean attack(BoardPosition self, EPlayer target, GameInstance game){ //attack player face
        if(canAttack(self, target, game)){
            onAttack(self, target, game);
            return true;
        }
        return false;
    }
    public final void enterNewRound(BoardPosition self, GameInstance game){
        onRoundBegin(self, game);
    }
    public final void finishRound(BoardPosition self, GameInstance game){
        if(isStunned()) setStunnedRoundsLeft(stunnedRoundsLeft-1, self, game);
        setHasAttackedThisRound(false);
        onRoundFinish(self, game);
    }

    //Member setters. Please use them to modify members. It is important that all changes are logged to game.getPlayerBoardUpdate(), so that the updated game state can be passed to the clients.
    public final void applyDamage(int damage, BoardPosition position, GameInstance game){
        if(damage < 0) damage = 0;
        setCurrentHealth(currentHealth - damage, position, game);
    }
    public final void applyHeal(int heal, BoardPosition position, GameInstance game){
        if(heal < 0) heal = 0;
        setCurrentHealth(currentHealth + heal, position, game);
    }
    public final void setCurrentHealth(int value, BoardPosition position, GameInstance game){ //can be used to straight up removeMonster someone
        if(value < 0) value = 0;
        currentHealth = value;
        game.getPlayerBoardUpdates().updateMonsterHealth(position, value);
        if(currentHealth <= 0) onHealthDepleted(position, game);
    }
    public final void modifyDamageBuffBy(int value, BoardPosition position, GameInstance game){
        setDamageBuffTo(damageBuff + value, position, game);
    }
    public final void setDamageTo(int value, BoardPosition position, GameInstance game){
        setDamageBuffTo(value - baseDamage, position, game);
    }
    public final void setDamageBuffTo(int value, BoardPosition position, GameInstance game){
        damageBuff = value;
        if(baseDamage + damageBuff < 0) damageBuff = -baseDamage;
        game.getPlayerBoardUpdates().updateMonsterDamage(position, getCurrentDamage());
    }
    public final void applyStun(int rounds, BoardPosition position, GameInstance game){
        setStunnedRoundsLeft(Integer.max(stunnedRoundsLeft, rounds), position, game);
    }

    public final void setStunnedRoundsLeft(int rounds, BoardPosition position, GameInstance game){
        if(rounds < 0) rounds = 0;
        stunnedRoundsLeft = rounds;
        game.getPlayerBoardUpdates().updateMonsterStunned(position, isStunned());
    }
    public final void removeStun(BoardPosition position, GameInstance game){
        stunnedRoundsLeft = 0;
        game.getPlayerBoardUpdates().updateMonsterStunned(position, false);
    }
    public final void setHasAttackedThisRound(boolean value){
        hasAttackedThisRound = value;
    }
    public final void getAttacked(BoardPosition self, @Nullable BoardPosition attacker, int damage, GameInstance game){
        onReceiveDamage(damage, self, attacker, game);
    }
    public final void getKilled(BoardPosition self, GameInstance game){
        setCurrentHealth(0, self, game);
    }


    //These are the functions Flo overrides in child classes.
    //I guess you can override this one, toom for example for healers(they target allies) or some other specific stuff (idk maybe Romeo can't attack Juliet specifically)
    public boolean canAttack(BoardPosition self, BoardPosition target, GameInstance game){ //whether it can attack a monster
        if(isStunned() || hasAttackedThisRound) return false;
        Monster targetMon = game.getMonsterAtPosition(target);
        if(targetMon == null) return false;
        else return target.getSide() != self.getSide();
    }
    public boolean canAttack(BoardPosition self, EPlayer target, GameInstance game){ //whether if can attack a face
        if(isStunned() || hasAttackedThisRound) return false;
        return target != self.getSide();
    }
    public void postSpawn(BoardPosition position, GameInstance game){

    }
    protected void onAttack(BoardPosition self, BoardPosition target, GameInstance game){ //attack a monster
        Monster targetMon = game.getMonsterAtPosition(target);
        setHasAttackedThisRound(true);
        targetMon.getAttacked(target, self, getCurrentDamage(), game);
    }
    protected void onAttack(BoardPosition self, EPlayer target, GameInstance game){ //attack someone's face
        game.addOrRemovePlayerHealth(target, -getCurrentDamage());
        setHasAttackedThisRound(true);
    }

    //@param attacker: the BoardPosition of the attacking monster. null if it was a spell.
    protected void onReceiveDamage(int damage, BoardPosition self, @Nullable BoardPosition attacker, GameInstance game){
        applyDamage(damage, self, game);
    }
    protected void onHealthDepleted(BoardPosition self, GameInstance game){ //This is overridden in phoenix, for example
        game.removeMonster(self);
        postDeath(self, game);
    }
    protected void postDeath(BoardPosition self, GameInstance game){

    }
    protected void onReceiveStun(int rounds, BoardPosition self, GameInstance game){
        applyStun(rounds, self, game); //maybe there are monsters that can't be stunned at all. Override this method to do nothing.
    }
    protected void onRoundBegin(BoardPosition self, GameInstance game){

    }
    protected void onRoundFinish(BoardPosition self, GameInstance game){

    }



    //simple getters
    public int getCurrentHealth(){
        return currentHealth;
    }
    public int getCurrentDamage() { return baseDamage + damageBuff; }
    public int getDamageBuff() { return damageBuff; }
    public int getImageID(){
        return imageID;
    }
    public boolean isStunned(){
        return stunnedRoundsLeft > 0;
    }
    public boolean getHasAttackedThisRound(){return hasAttackedThisRound; }
}
