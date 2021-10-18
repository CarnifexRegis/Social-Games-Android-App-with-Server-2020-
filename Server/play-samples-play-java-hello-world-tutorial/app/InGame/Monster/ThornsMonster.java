package InGame.Monster;

import javax.annotation.Nullable;

import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class ThornsMonster extends Monster {
    protected int thornsDamage;
    public ThornsMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID, int thornsDamage){
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.thornsDamage = thornsDamage;
    }
    @Override
    protected void onReceiveDamage(int damage, BoardPosition self, @Nullable BoardPosition attackerPos, GameInstance game){
        applyDamage(damage, self, game);
        if(attackerPos != null) {
            Monster attackerMon = game.getMonsterAtPosition(attackerPos);
            if(attackerMon != null) attackerMon.getAttacked(attackerPos, null, thornsDamage, game); //pass null for attacker here so two thornsMonsters dont attack each other for all eternity. TODO use damageType or something
        }
    }
}
