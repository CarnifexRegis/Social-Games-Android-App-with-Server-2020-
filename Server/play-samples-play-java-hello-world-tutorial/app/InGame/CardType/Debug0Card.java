package InGame.CardType;

import InGame.EPlayer;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Monster.MonsterTypes;
import InGame.Utility.BoardPosition;

public class Debug0Card extends CardType {
    protected MonsterTypes.MonsterType monsterType;
    public Debug0Card(int typeID, MonsterTypes.MonsterType monsterType, int manaCost){
        super(typeID, monsterType.element, manaCost);
        this.monsterType = monsterType;
    }
    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board
        return owner == position.getSide();
    }

    @Override
    public void onPlace(EPlayer owner, BoardPosition position, int imageID, GameInstance game){
        BoardPosition tryHere = game.getFreeBoardPositionNear(position);
        if(tryHere != null) {
            Monster summon = monsterType.spawn(imageID);
            game.spawnMonster(summon, tryHere);
        }
    }

    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){
        return false;
    }
}
