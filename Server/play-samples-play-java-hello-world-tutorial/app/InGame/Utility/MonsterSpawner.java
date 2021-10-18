package InGame.Utility;


import InGame.Element;
import InGame.Monster.Monster;

public interface MonsterSpawner {
    Monster spawn(int typeID, Element element, int baseHealth, int baseDamage, int imageID);
}
