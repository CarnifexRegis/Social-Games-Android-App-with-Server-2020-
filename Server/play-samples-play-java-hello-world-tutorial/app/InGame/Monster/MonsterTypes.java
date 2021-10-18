package InGame.Monster;

import static InGame.Element.*;

import InGame.Element;
import InGame.Utility.MonsterSpawner;

import java.util.Map;
import java.util.HashMap;

public class MonsterTypes {
    public static final MonsterType ABOMINATION;
    public static final MonsterType ANGEL;
    public static final MonsterType ARCHANGEL;
    public static final MonsterType BAT;
    public static final MonsterType DRACULA;
    public static final MonsterType ALURA_UNE;
    public static final MonsterType BEE;
    public static final MonsterType ENT;
    public static final MonsterType GOLEM;
    public static final MonsterType MEDUSA;
    public static final MonsterType MONKEY_BUSINESS;
    public static final MonsterType OVERLORD_OF_ROCK_AND_METAL;
    public static final MonsterType QUEEN_BEE;
    public static final MonsterType BOMBER_THE_PYROMANIAC;
    public static final MonsterType PHOENIX;
    public static final MonsterType RA;
    public static final MonsterType WILL_O_WISP;
    public static final MonsterType ELECTRON;
    public static final MonsterType MAD_SCIENTIST;
    public static final MonsterType NUE;
    public static final MonsterType THOR;
    public static final MonsterType ZEUS;
    public static final MonsterType ZEUS_JR;
    public static final MonsterType FLYING_DUTCHMAN;
    public static final MonsterType THE_KRAKEN;
    public static final MonsterType LAZY_FROG;
    public static final MonsterType SHARK;
    public static final MonsterType SHIVA;
    public static final MonsterType SPIRIT_OF_THE_SPRING;

    private static Map<Integer, MonsterType> monsters;

    public static MonsterType fromID(int typeID){
        MonsterType result = monsters.get(typeID);
        if(result == null) throw new RuntimeException("Tried to look up monster with unused ID "+typeID);
        return result;
    }

    private static MonsterType register(int typeID, Element element, int baseHealth, int baseDamage, MonsterSpawner spawner){ //for the super lazy
        return register(new MonsterType(typeID, element, baseHealth, baseDamage, spawner));
    }

    private static MonsterType register(MonsterType type){
        if(monsters.get(type.typeID) != null) throw new RuntimeException("Tried to register a monster with ID "+type.typeID+" that was already taken.");
        monsters.put(type.typeID, type);
        return type;
    }

    public static class MonsterType {
        public final int typeID;
        public final Element element;
        public final int baseHealth;
        public final int baseDamage;
        private final MonsterSpawner spawner;
        public MonsterType(int typeID, Element element, int baseHealth, int baseDamage, MonsterSpawner spawner){
            this.typeID = typeID;
            this.element = element;
            this.baseHealth = baseHealth;
            this.baseDamage = baseDamage;
            this.spawner = spawner;
        }
        public Monster spawn(int imageID){
            return spawner.spawn(typeID, element, baseHealth, baseDamage, imageID);
        }
        /*private static class MonsterInfo{
            public final int t; //typeID
            public final Element e; //element
            public final int h; //health
            public final int d; //damage
            MonsterInfo(int typeID, Element element, int health, int damage){
                this.t = typeID;
                this.e = element;
                this.h = health;
                this.d = damage;
            }
        }*/
    }

    static {
        monsters = new HashMap<>();

        ABOMINATION = register(0, TWILIGHT, 3, 3, Monster::new);
        ANGEL = register(1, TWILIGHT, 10, 2, Monster::new);
        ARCHANGEL = register(2, TWILIGHT, 24, 27, Monster::new);
        BAT = register(3, TWILIGHT, 2, 2, Monster::new);
        DRACULA = register(4, TWILIGHT, 12, 4, (t, e, h, d, i)->new DraculaMonster(t, e, h, d, i, BAT, 2, 30));
        ALURA_UNE = register(7, EARTHQUAKE, 23, 6, (t, e, h, d, i)->new ThornsMonster(t, e, h, d, i, 3));
        BEE = register(8, EARTHQUAKE, 3, 4, Monster::new);
        ENT = register(9, EARTHQUAKE, 12, 12, (t, e, h, d, i)->new SlowMonster(t, e, h, d, i, 1));
        GOLEM = register(10, EARTHQUAKE, 8, 0, Monster::new);
        MEDUSA = register(12, EARTHQUAKE, 8, 15, (t, e, h, d, i)->new MedusaMonster(t, e, h, d, i, Integer.MAX_VALUE, 10));
        MONKEY_BUSINESS = register(14, EARTHQUAKE, 7, 7, Monster::new);
        OVERLORD_OF_ROCK_AND_METAL = register(16, EARTHQUAKE, 25, 30, Monster::new);
        QUEEN_BEE = register(17, EARTHQUAKE, 3, 8, (t, e, h, d, i)->new DeckEnforcementMonster(t, e, h, d, i, BEE, 2));
        BOMBER_THE_PYROMANIAC = register(19, INFERNO, 4, 4, BomberMonster::new);
        PHOENIX = register(22, INFERNO, 12, 3, JesusMonster::new);
        RA = register(23, INFERNO, 10, 30, Monster::new);
        WILL_O_WISP = register(24, INFERNO, 2, 2, Monster::new);
        ELECTRON = register(25, STORM, 1, 1, Monster::new);
        MAD_SCIENTIST = register(27, STORM, 4, 5, Monster::new);
        NUE = register(28, STORM, 3, 8, Monster::new);
        THOR = register(31, STORM, 5, 20, Monster::new);
        ZEUS = register(32, STORM, 15, 23, Monster::new);
        ZEUS_JR = register(33, STORM, 1, 7, Monster::new);
        FLYING_DUTCHMAN = register(34, TSUNAMI, 3, 10, Monster::new);
        THE_KRAKEN = register(36, TSUNAMI, 8, 8, Monster::new);
        LAZY_FROG = register(37, TSUNAMI, 12, 0, (t, e, h, d, i)->new GrowingMonster(t, e, h, d, i, 1));
        SHARK = register(38, TSUNAMI, 3, 1, Monster::new);
        SHIVA = register(39, TSUNAMI, 16, 2, (t, e, h, d, i)->new PriestMonster(t, e, h, d, i, 2));
        SPIRIT_OF_THE_SPRING = register(40, TSUNAMI, 3, 1, (t, e, h, d, i)->new ADHDPriestMonster(t, e, h, d, i, 4));
    }
}
