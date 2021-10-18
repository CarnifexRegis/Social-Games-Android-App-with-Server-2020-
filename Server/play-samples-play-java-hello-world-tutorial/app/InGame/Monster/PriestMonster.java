package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class PriestMonster extends Monster {
    protected int healing;

    public PriestMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID, int healing) {
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.healing = healing;
    }

    @Override
    protected void onRoundFinish(BoardPosition self, GameInstance game){
        for(int i=-1; i<=1; i++){
            BoardPosition targetPos = self.slotOffset(i);
            Monster targetMon = game.getMonsterAtPosition(targetPos);
            if(targetMon != null) targetMon.applyHeal(healing, targetPos, game);
        }
    }
}
