package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class JesusMonster extends Monster {
    protected static final int RESPAWN_TIME = 3; //idk whether I should leave this hardcoded. Probably not. On the other hand, it's 2:14AM
    protected boolean hasDied = false;
    protected boolean respawning = false;
    protected int deadRoundsLeft;
    protected int respawnHP;

    public JesusMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID) {
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.respawnHP = baseHealth;
    }

    @Override
    protected void onHealthDepleted(BoardPosition self, GameInstance game){
        if(hasDied && !respawning) super.onHealthDepleted(self, game);
        else if(!respawning) {
            hasDied = true;
            respawning = true;
            applyStun(Integer.MAX_VALUE, self, game);
            deadRoundsLeft = RESPAWN_TIME+1;
        } //else do nothing
    }

    @Override
    protected void onRoundBegin(BoardPosition self, GameInstance game){
        if(respawning){
            deadRoundsLeft--;
            if(deadRoundsLeft <= 0){
                respawning = false;
                setCurrentHealth( respawnHP, self, game);
                removeStun(self, game);
            }
        }
    }

    @Override
    public boolean canAttack(BoardPosition self, BoardPosition target, GameInstance game){ //whether it can attack a monster
        if(respawning) return false;
        return super.canAttack(self, target, game);
    }
    @Override
    public boolean canAttack(BoardPosition self, EPlayer target, GameInstance game){ //whether if can attack a face
        if(respawning) return false;
        return super.canAttack(self, target, game);
    }
}
