package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Monster.MonsterTypes;
import InGame.Utility.BoardPosition;

public class SpecificMonsterBuffCard extends MonsterBuffCard {
    protected int[] monsterIDs;
    public SpecificMonsterBuffCard(int id, Element element, int manaCost, int damageBuff, MonsterTypes.MonsterType[] monsters){
        super(id, element, manaCost, damageBuff);
        this.monsterIDs = new int[monsters.length];
        for(int i=0; i<monsters.length; i++){
            monsterIDs[i] = monsters[i].typeID;
        }
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        if(owner != position.getSide()) return false;
        Monster target = game.getMonsterAtPosition(position);
        if(target == null) return false;
        int targetID = target.typeID;
        for(int id : monsterIDs){
            if(id == targetID) return true;
        }
        return false;
    }
}
