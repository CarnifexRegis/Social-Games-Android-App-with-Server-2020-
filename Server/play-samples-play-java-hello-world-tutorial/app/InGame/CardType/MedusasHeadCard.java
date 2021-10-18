package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class MedusasHeadCard extends CardType {
    protected int fortification;
    protected int stunRounds;

    public MedusasHeadCard(int typeID, Element element, int manaCost, int stunRounds, int fortification){
        super(typeID, element, manaCost);
        this.fortification = fortification;
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
        target.applyHeal(fortification, position, game);
    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        throw new UnsupportedOperationException("Tried to place a Medusa's Head on a face. TBH this should never happen as canTarget(face) always returns false in the first place. If you see this message, tell Maxi!");
    }
}
