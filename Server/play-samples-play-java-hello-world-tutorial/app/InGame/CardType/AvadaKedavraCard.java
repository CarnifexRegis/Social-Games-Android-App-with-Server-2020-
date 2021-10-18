package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class AvadaKedavraCard extends CardType {

    public AvadaKedavraCard(int typeID, Element element, int manaCost){
        super(typeID, element, manaCost);
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        if(owner == position.getSide()) return false;
        return game.getMonsterAtPosition(position) != null;
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        game.killMonster(position);
    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        throw new UnsupportedOperationException("Tried to place an Avada Kedavra on a face. TBH this should never happen as canTarget(face) always returns false in the first place. If you see this message, tell Maxi!");
    }
}
