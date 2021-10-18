package InGame.Monster;

import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class AvengedMonster extends Monster{
    protected MonsterTypes.MonsterType reinforcement;
    protected int reinforcementCount;
    public AvengedMonster(int typeID, Element element, MonsterTypes.MonsterType reinforcement, int reinforcementCount, int health, int damage, int imageID){
        super(typeID, element, health, damage, imageID);
        this.reinforcement = reinforcement;
        this.reinforcementCount = reinforcementCount;
    }

    @Override protected void postDeath(BoardPosition position, GameInstance game){
        for(int i=0; i<reinforcementCount; i++){
            BoardPosition freePos = game.getFreeBoardPositionNear(position);
            if(freePos == null) break;
            else game.spawnMonster(reinforcement.spawn(getImageID()), freePos);
        }
    }
}
