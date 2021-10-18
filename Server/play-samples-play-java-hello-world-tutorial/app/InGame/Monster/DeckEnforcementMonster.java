package InGame.Monster;

import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class DeckEnforcementMonster extends Monster {
    protected MonsterTypes.MonsterType enforcement;
    protected int enforcementCount;

    public DeckEnforcementMonster(int typeID, Element element, int baseHealth, int baseDamage, int imageID, MonsterTypes.MonsterType enforcement, int enforcementCount){
        super(typeID, element, baseHealth, baseDamage, imageID);
        this.enforcement = enforcement;
        this.enforcementCount = enforcementCount;
    }

    @Override
    public void postSpawn(BoardPosition position, GameInstance game){
        for(int i=0; i<enforcementCount; i++){
            boolean deckCardFound = true; //TODO actually search for a deck card
            //searchForDeckCards
            if(deckCardFound){
                BoardPosition placementPos = game.getFreeBoardPositionNear(position);
                if(placementPos == null) break;
                game.spawnMonster(enforcement.spawn(getImageID()), placementPos);
                //play it

            }
        }
    }
}
