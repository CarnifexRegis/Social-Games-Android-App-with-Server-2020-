package InGame.CardType;


import InGame.EPlayer;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Monster.MonsterTypes;
import InGame.Utility.BoardPosition;
import InGame.Utility.MonsterCardType;

public class MonsterUpgradeCard extends CardType implements MonsterCardType {
    protected int baseMonsterID;
    protected MonsterTypes.MonsterType monsterType;

    public MonsterUpgradeCard(MonsterTypes.MonsterType monsterType, int mana, int baseMonsterID){
        super(monsterType.typeID, monsterType.element, mana);
        this.baseMonsterID = baseMonsterID;
        this.monsterType = monsterType;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board
        if(owner != position.getSide()) return false;
        Monster target = game.getMonsterAtPosition(position);
        if(target == null) return false;
        else return target.typeID == baseMonsterID;
    }
    @Override
    public void onPlace(EPlayer owner, BoardPosition position, int imageID, GameInstance game){
        Monster target = game.getMonsterAtPosition(position);
        boolean hasAttacked = target.getHasAttackedThisRound();
        int damageBuff = target.getDamageBuff();
        game.removeMonster(position); //kill the old monster
        Monster summon = monsterType.spawn(imageID);
        game.spawnMonster(summon, position);
        summon.setHasAttackedThisRound(hasAttacked);
        summon.modifyDamageBuffBy(damageBuff, position, game);
    }

    @Override public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){
        return false;
    }
    @Override
    public void onPlace(EPlayer owner, EPlayer face, int imageID, GameInstance game){
        throw new UnsupportedOperationException("Tried to place a monster upgrade on a face. TBH this should never happen as canTarget(face) always returns false in the first place. If you see this message, tell Maxi!");
    }

    @Override
    public MonsterTypes.MonsterType getMonsterType() {
        return monsterType;
    }
}
