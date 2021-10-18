package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class JacuzziCard extends CardType {

    protected int healing;

    public JacuzziCard(int typeID, Element element, int manaCost, int healing) {
        super(typeID, element, manaCost);
        this.healing = healing;
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        return position.getSide() == owner;
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        boolean foundOne = false;
        Monster[] monsters = new Monster[5];
        for(int i=0; i<5; i++){
            BoardPosition testPos = new BoardPosition(player, i);
            monsters[i] = game.getMonsterAtPosition(testPos);
            if(monsters[i] != null && monsters[i].element == element) foundOne = true;
        }
        if(foundOne){
            for(int i=0; i<5; i++){
                BoardPosition targetPos = new BoardPosition(player, i);
                if(monsters[i]!=null) monsters[i].applyHeal(healing, targetPos, game);
            }
        }
    }
}
