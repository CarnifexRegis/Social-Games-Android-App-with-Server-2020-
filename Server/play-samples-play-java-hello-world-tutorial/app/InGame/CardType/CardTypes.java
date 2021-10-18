package InGame.CardType;

import InGame.Monster.MonsterTypes;

import static InGame.Element.*;

import java.util.HashMap;
import java.util.Map;

public class CardTypes {
    public static final CardType DEBUG_0;

    public static final SummoningCard ABOMINATION;
    public static final SummoningCard ANGEL;
    public static final MonsterUpgradeCard ARCHANGEL;
    public static final SummoningCard BAT;
    public static final SummoningCard DRACULA;
    public static final CardType HALO;
    public static final CardType THE_FOOL;
    public static final SummoningCard ALURA_UNE;
    public static final SummoningCard BEE;
    public static final SummoningCard ENT;
    public static final SummoningCard GOLEM;
    public static final CardType INFESTATION;
    public static final SummoningCard MEDUSA;
    public static final CardType MEDUSAS_HEAD;
    public static final SummoningCard MONKEY_BUSINESS;
    public static final CardType OVERGROWN;
    public static final SummoningCard OVERLORD_OF_ROCK_AND_METAL;
    public static final SummoningCard QUEEN_BEE;
    public static final CardType QUICKSAND;
    public static final SummoningCard BOMBER_THE_PYROMANIAC;
    public static final CardType DROUGHT;
    public static final CardType FOREST_FIRE;
    public static final SummoningCard PHOENIX;
    public static final SummoningCard RA;
    public static final SummoningCard WILL_O_WISP;
    public static final SummoningCard ELECTRON;
    public static final CardType EMP;
    public static final SummoningCard MAD_SCIENTIST;
    public static final SummoningCard NUE;
    public static final CardType SHARKNADO;
    public static final CardType TASER;
    public static final SummoningCard THOR;
    public static final SummoningCard ZEUS;
    public static final SummoningCard ZEUS_JR;
    public static final SummoningCard FLYING_DUTCHMAN;
    public static final CardType JACUZZI;
    public static final SummoningCard THE_KRAKEN;
    public static final SummoningCard LAZY_FROG;
    public static final SummoningCard SHARK;
    public static final SummoningCard SHIVA;
    public static final SummoningCard SPIRIT_OF_THE_SPRING;


    private static Map<Integer, CardType> cardTypes;

    public static CardType fromID(int typeID){
        CardType result = cardTypes.get(typeID);
        if(result == null) throw new RuntimeException("Player drew card with unused ID "+typeID);
        return result;
    }

    private static void register(CardType type){
        if(cardTypes.get(type.typeID) != null) throw new RuntimeException("Tried to register a CardType with ID "+type.typeID+" that was already taken.");
        cardTypes.put(type.typeID, type);
    }

    static {
        cardTypes = new HashMap<>();

        register(DEBUG_0 = new Debug0Card(420, MonsterTypes.ABOMINATION, 1));

        //TWILIGHT
        register(ABOMINATION = new SummoningCard(MonsterTypes.ABOMINATION, 2));
        register(ANGEL = new SummoningCard(MonsterTypes.ANGEL, 4));
        register(ARCHANGEL = new MonsterUpgradeCard(MonsterTypes.ARCHANGEL, 8, MonsterTypes.ANGEL.typeID));
        register(BAT = new SummoningCard(MonsterTypes.BAT, 1));
        register(DRACULA = new SummoningCard(MonsterTypes.DRACULA, 4));
        register(HALO = new SpecificMonsterBuffCard(5, TWILIGHT, 3, 8, new MonsterTypes.MonsterType[]{MonsterTypes.ANGEL, MonsterTypes.ARCHANGEL}));
        register(THE_FOOL = new TheFoolCard(6, TWILIGHT, 5));
        //EARTHQUAKE
        register(ALURA_UNE = new SummoningCard(MonsterTypes.ALURA_UNE, 7));
        register(BEE = new SummoningCard(MonsterTypes.BEE, 2));
        register(ENT = new SummoningCard(MonsterTypes.ENT, 5));
        register(GOLEM = new SummoningCard(MonsterTypes.GOLEM, 1));
        register(INFESTATION = new MonsterDebuffCard(11, EARTHQUAKE, 3, 4));
        register(MEDUSA = new SummoningCard(MonsterTypes.MEDUSA, 8));
        register(MEDUSAS_HEAD = new MedusasHeadCard(13, EARTHQUAKE, 4, Integer.MAX_VALUE, 10));
        register(MONKEY_BUSINESS = new SummoningCard(MonsterTypes.MONKEY_BUSINESS, 5));
        register(OVERGROWN = new AoEStunCard(15, EARTHQUAKE, 3, 1));
        register(OVERLORD_OF_ROCK_AND_METAL = new SummoningCard(MonsterTypes.OVERLORD_OF_ROCK_AND_METAL, 9));
        register(QUEEN_BEE = new SummoningCard(MonsterTypes.QUEEN_BEE, 4));
        register(QUICKSAND = new AvadaKedavraCard(18, EARTHQUAKE, 6));
        register(BOMBER_THE_PYROMANIAC = new SummoningCard(MonsterTypes.BOMBER_THE_PYROMANIAC, 4));
        register(DROUGHT = new GenocideCard(20, INFERNO, 4, TSUNAMI));
        register(FOREST_FIRE = new GenocideCard(21, INFERNO, 4, EARTHQUAKE));
        register(PHOENIX = new SummoningCard(MonsterTypes.PHOENIX, 6));
        register(RA = new SummoningCard(MonsterTypes.RA, 8));
        register(WILL_O_WISP = new SummoningCard(MonsterTypes.WILL_O_WISP, 1));
        register(ELECTRON = new SummoningCard(MonsterTypes.ELECTRON, 0));
        register(EMP = new RacistStun(26, STORM, 2, STORM, 2));
        register(MAD_SCIENTIST = new SummoningCard(MonsterTypes.MAD_SCIENTIST, 3));
        register(NUE = new SummoningCard(MonsterTypes.NUE, 4));
        /*read comment*/register(SHARK = new SummoningCard(MonsterTypes.SHARK, 1)); //needs to be initialized before sharknado TODO use references. This is because of my bad design(Maxi) but I'm too lazy to replaye all MonsterSpawners in the constructors with Supplier<MonsterSpawner> aka references.
        register(SHARKNADO = new MonsterNadoCard(29, STORM, 5, (SummoningCard)CardTypes.SHARK));

        register(TASER = new StunCard(30, STORM, 3, 1));
        register(THOR = new SummoningCard(MonsterTypes.THOR, 6));
        register(ZEUS = new SummoningCard(MonsterTypes.ZEUS, 7));
        register(ZEUS_JR = new SummoningCard(MonsterTypes.ZEUS_JR, 3));
        register(FLYING_DUTCHMAN = new SummoningCard(MonsterTypes.FLYING_DUTCHMAN, 5));
        register(JACUZZI = new JacuzziCard(35, TSUNAMI, 2, 2));
        register(THE_KRAKEN = new SummoningCard(MonsterTypes.THE_KRAKEN, 4));
        register(LAZY_FROG = new SummoningCard(MonsterTypes.LAZY_FROG, 3));
        register(SHIVA = new SummoningCard(MonsterTypes.SHIVA, 6));
        register(SPIRIT_OF_THE_SPRING = new SummoningCard(MonsterTypes.SPIRIT_OF_THE_SPRING, 4));
    }
}
