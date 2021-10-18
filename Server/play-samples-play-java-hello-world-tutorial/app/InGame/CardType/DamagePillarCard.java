package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class DamagePillarCard extends CardType {
    private final int damage;
    public DamagePillarCard(int typeID, Element element, int manaCost, int damage){
        super(typeID, element, manaCost);
        this.damage = damage;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        return position.getSide() != owner;
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        EPlayer side = position.getSide();
        for(int i=0; i<5; i++){
            BoardPosition targetPos = new BoardPosition(side, i);
            Monster target = game.getMonsterAtPosition(targetPos);
            if(target != null) target.getAttacked(targetPos, null, damage, game);
        }
    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        throw new UnsupportedOperationException("This will never happen vecause canTarget(face) always returns false");
    }
}
