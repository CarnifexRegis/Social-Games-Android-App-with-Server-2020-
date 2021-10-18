package InGame.Monster;

import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class BomberMonster extends AoEMonster {
    public BomberMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID) {
        super(typeID, element, baseHealth, baseDamage, imageID);
    }
    @Override
    protected void postDeath(BoardPosition self, GameInstance game){
        for(int i=-1; i<=1; i++){
            BoardPosition targetPos = self.slotOffset(i);
            Monster targetMon = game.getMonsterAtPosition(targetPos);
            if(targetMon != null) targetMon.getAttacked(targetPos, null, getCurrentDamage(), game);
        }
    }
}
