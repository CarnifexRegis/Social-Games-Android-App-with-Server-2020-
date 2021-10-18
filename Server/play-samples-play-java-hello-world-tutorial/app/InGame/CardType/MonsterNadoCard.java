package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MonsterNadoCard extends CardType {
    protected int playedCardID;
    public MonsterNadoCard(int typeID, Element element, int manaCost, SummoningCard playedCard ) {
        super(typeID, element, manaCost);
        this.playedCardID = playedCard.typeID;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        return owner == position.getSide();
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        Map<Integer, GameInstance.HandCard> playerHand = game.getPlayerHand(player);
        List<Integer> playableCardIDs = new ArrayList<>();

        for(Map.Entry<Integer, GameInstance.HandCard> entry : playerHand.entrySet()){
            GameInstance.HandCard card = entry.getValue();
            if(card.cardType.typeID == playedCardID) playableCardIDs.add(entry.getKey());
        }

        for(int cardID : playableCardIDs){
            BoardPosition freePos = game.getFreeBoardPositionNear(position);
            if(freePos == null) break;
            game.placeCard(player, cardID, freePos, true);
        }
    }
}
