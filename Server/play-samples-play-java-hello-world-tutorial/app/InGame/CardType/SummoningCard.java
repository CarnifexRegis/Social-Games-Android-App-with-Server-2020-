package InGame.CardType;

import InGame.EPlayer;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Monster.MonsterTypes;
import InGame.Utility.BoardPosition;
import InGame.Utility.MonsterCardType;

public class SummoningCard extends CardType implements MonsterCardType {

    protected final MonsterTypes.MonsterType monsterType;

    public SummoningCard(MonsterTypes.MonsterType monsterType, int manaCost){
        super(monsterType.typeID, monsterType.element, manaCost);
        this.monsterType = monsterType;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board
        if(owner != position.getSide()) return false;
        if(game.getMonsterAtPosition(position) != null) return false;
        else return true;
    }
    @Override public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){
        return false;
    }

    @Override
    public void onPlace(EPlayer owner, BoardPosition position, int imageID, GameInstance game){
        Monster summon = monsterType.spawn(imageID);
        game.spawnMonster(summon, position);
    }
    @Override
    public void onPlace(EPlayer owner, EPlayer face, int imageID, GameInstance game){
        throw new UnsupportedOperationException("Tried to place a summoning card on a face. TBH this should never happen as canTarget(face) always returns false in the first place. If you see this message, tell Maxi!");
    }

    @Override
    public MonsterTypes.MonsterType getMonsterType() {
        return monsterType;
    }
}
