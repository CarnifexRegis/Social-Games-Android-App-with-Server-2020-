package InGame.CardType;

import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public abstract class CardType {
    //public final String name;
    public final int typeID; //honestly idk if that's technically needed but let's go
    public final Element element;
    public final int manaCost;

    public CardType(int typeID, Element element, int manaCost){
        this.typeID = typeID;
        this.element = element;
        this.manaCost = manaCost;
    }

    //returns true if successful
    public final boolean place(EPlayer owner, BoardPosition position, int imageID, GameInstance game){ //place on board
        if(canTarget(owner, position, game)){
            onPlace(owner, position, imageID, game);
            //game.applyManaCost(params, manaCost); Mana costs are being handled by GameInstance
            return true;
        }
        else return false;
    }
    public final boolean place(EPlayer owner, EPlayer face, int imageID, GameInstance game){ //place on face
        if(canTarget(owner, face, game)){
            onPlace(owner, face, imageID, game);
            //mana costs aren't being handled here
            return true;
        }
        else return false;
    }


    //These are the functions Flo can override in child classes.

    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?
        throw new UnsupportedOperationException("This method must be overridden!");
    }
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?
        throw new UnsupportedOperationException("This method must be overridden!");
    }

    protected void onPlace(EPlayer player, BoardPosition position, int imageID, GameInstance game){ //place on board
        throw new UnsupportedOperationException("This method must be overridden!");
    }
    protected void onPlace(EPlayer player, EPlayer face, int imageID, GameInstance game){ //place on face
        throw new UnsupportedOperationException("This method must be overridden!");
    }


    /*
    @Override
    public boolean canTarget(EPlayer owner, BoardPosition position, GameInstance game){ //can target board?

    }
    @Override
    public boolean canTarget(EPlayer owner, EPlayer face, GameInstance game){ //can target face?

    }
    @Override
    protected void onPlace(EPlayer player, BoardPosition position, GameInstance game){ //place on board

    }
    @Override
    protected void onPlace(EPlayer player, EPlayer face, GameInstance game){ //place on face

    }
    */
}
