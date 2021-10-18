package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class GrowingMonster extends Monster {
    protected int healing;

    public GrowingMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID, int healing) {
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.healing = healing;
    }

    @Override
    protected void onRoundFinish(BoardPosition self, GameInstance game){
        applyHeal(healing, self, game);
    }
}
