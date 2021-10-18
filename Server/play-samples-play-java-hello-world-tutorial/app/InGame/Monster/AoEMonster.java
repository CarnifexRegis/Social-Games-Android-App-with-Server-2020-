package InGame.Monster;

import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class AoEMonster extends Monster {
    public AoEMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID) {
        super(typeID, element, baseHealth, baseDamage, imageID);
    }
    @Override
    public boolean canAttack(BoardPosition self, BoardPosition target, GameInstance game){ //whether it can attack a monster
        if(isStunned() || hasAttackedThisRound) return false;
        return target.getSide() != self.getSide();
    }
    @Override
    protected void onAttack(BoardPosition self, BoardPosition target, GameInstance game){ //attack a monster
        for(int i=-1; i<=1; i++){
            BoardPosition targetPos = target.slotOffset(i);
            Monster targetMon = game.getMonsterAtPosition(targetPos);
            if(targetMon != null) targetMon.getAttacked(targetPos, self, getCurrentDamage(), game);
        }
        setHasAttackedThisRound(true);
    }
}
