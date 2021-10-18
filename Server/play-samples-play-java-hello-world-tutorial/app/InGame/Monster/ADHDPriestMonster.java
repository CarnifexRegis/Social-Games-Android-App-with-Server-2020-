package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class ADHDPriestMonster extends Monster {
    protected int healing;

    public ADHDPriestMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID, int healing) {
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.healing = healing;
    }

    @Override
    public void postSpawn(BoardPosition self, GameInstance game){
        BoardPosition left = self.slotOffset(-1);
        Monster leftMonster = game.getMonsterAtPosition(left);
        if(leftMonster != null) leftMonster.applyHeal(healing, left, game);
        BoardPosition right = self.slotOffset(1);
        Monster rightMonster = game.getMonsterAtPosition(right);
        if(rightMonster != null) rightMonster.applyHeal(healing, right, game);
    }
}
