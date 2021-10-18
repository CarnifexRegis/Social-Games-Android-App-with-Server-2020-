package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class DraculaMonster extends AvengedMonster {
    protected int healthCap;
    public DraculaMonster(int typeID, Element element, int health, int damage, int imageID, MonsterTypes.MonsterType reinforcement, int reinforcementCount, int healthCap){
        super(typeID, element, reinforcement, reinforcementCount, health, damage, imageID);
        this.healthCap = healthCap;
    }

    @Override
    protected void onAttack(BoardPosition self, BoardPosition target, GameInstance game){ //attack a monster
        super.onAttack(self, target, game);
        healingHelper(self, game);
    }
    @Override
    protected void onAttack(BoardPosition self, EPlayer target, GameInstance game){ //attack someone's face
        super.onAttack(self, target, game);
        healingHelper(self, game);
    }

    protected void healingHelper(BoardPosition self, GameInstance game){
        int healing = getCurrentDamage();
        if(healing+getCurrentHealth() > healthCap) healing = healthCap - getCurrentHealth();
        applyHeal(healing, self, game);
    }
}
