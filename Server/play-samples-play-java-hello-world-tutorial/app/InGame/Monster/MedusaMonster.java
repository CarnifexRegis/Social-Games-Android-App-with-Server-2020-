package InGame.Monster;


import InGame.EPlayer;
import InGame.Element;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;

public class MedusaMonster extends Monster {
    protected int fortification;
    protected int stunRounds;
    public MedusaMonster(int typeID, Element element, int health, int damage, int imageID, int stunRounds, int fortification){
        super(typeID, element, health, damage, imageID);
        this.stunRounds = stunRounds;
        this.fortification = fortification;
    }
    protected void onAttack(BoardPosition self, BoardPosition target, GameInstance game){ //attack a monster
        super.onAttack(self, target, game);
        usePetrifyingGaze(self, game);
    }
    protected void onAttack(BoardPosition self, EPlayer target, GameInstance game){ //attack someone's face
        super.onAttack(self, target, game);
        usePetrifyingGaze(self, game);
    }

    protected void usePetrifyingGaze(BoardPosition self, GameInstance game){
        BoardPosition acrossMe = self.across();
        Monster mon = game.getMonsterAtPosition(acrossMe);
        if(mon != null){
            mon.applyHeal(fortification, acrossMe, game);
            mon.applyStun(stunRounds, acrossMe, game);
        }
    }
}
