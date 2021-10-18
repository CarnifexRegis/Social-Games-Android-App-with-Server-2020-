package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Utility.BoardPosition;

public class TheFoolCard extends CardType {
    public TheFoolCard(int typeID, Element element, int manaCost){
        super(typeID, element, manaCost);
    }

    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        return true;
    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        return false;
    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        foolEveryone(game);
    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        foolEveryone(game);
    }

    protected void foolEveryone(GameInstance game){
        for(EPlayer player : EPlayer.values()){
            for(int i=0; i<5; i++){
                BoardPosition position = new BoardPosition(player, i);
                Monster mon = game.getMonsterAtPosition(position);
                if(mon != null){
                    int damage = mon.getCurrentHealth();
                    game.removeMonster(position);
                    game.addOrRemovePlayerHealth(player, -damage);
                }
            }
        }
    }
}
