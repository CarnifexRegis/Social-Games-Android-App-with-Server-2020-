package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class GenocideCard extends CardType {

    protected Element targetElement;

    public GenocideCard(int typeID, Element element, int manaCost, Element targetElement) {
        super(typeID, element, manaCost);
        this.targetElement = targetElement;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        return owner != position.getSide();
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        for(EPlayer side : EPlayer.values()){
            for(int i=0; i<5; i++){
                BoardPosition targetPos = new BoardPosition(side, i);
                Monster targetMon = game.getMonsterAtPosition(targetPos);
                if(targetMon != null){
                    if(targetMon.element == targetElement) game.killMonster(targetPos);
                }
            }
        }
    }
}
