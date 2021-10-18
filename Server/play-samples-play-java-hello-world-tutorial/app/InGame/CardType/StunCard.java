package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class StunCard extends CardType {
    protected int stunRounds;


    public StunCard(int typeID, Element element, int manaCost, int stunRounds){
        super(typeID, element, manaCost);
        this.stunRounds = stunRounds;
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
        Monster target = game.getMonsterAtPosition(position);
        target.applyStun(stunRounds, position, game);
    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        throw new UnsupportedOperationException("Tried to place a monster debuff on a face. TBH this should never happen as canTarget(face) always returns false in the first place. If you see this message, tell Maxi!");
    }
}
