package InGame.Monster;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class SlowMonster extends Monster{
    protected int selfStun;
    public SlowMonster(int typeID, Element element, int health, int damage, int imageID, int selfStun){
        super(typeID, element, health, damage, imageID);
        this.selfStun = selfStun+1;
    }

    protected void onAttack(BoardPosition self, BoardPosition target, GameInstance game){ //attack a monster
        super.onAttack(self, target, game);
        applyStun(selfStun, self, game);
    }
    protected void onAttack(BoardPosition self, EPlayer target, GameInstance game){ //attack someone's face
        super.onAttack(self, target, game);
        applyStun(selfStun, self, game);

    }
}
