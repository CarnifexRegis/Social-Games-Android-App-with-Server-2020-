package com.example.steppingmonsterduel2.Services;

import android.util.SparseArray;

import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Util.HttpGetter;

import static com.example.steppingmonsterduel2.Services.Element.*;

public class GameContent {

    public static int MAX_PLAYER_HEALTH = 100;

    public static final CardType DEBUG_0;
    public static final MonsterType ABOMINATION;
    public static final MonsterType ANGEL;
    public static final MonsterType ARCHANGEL;
    //TODO maybe. Tbh I don't think we will ever need to refer to CardTypes by these variables.

    private static SparseArray<CardType> cards;
    private static SparseArray<MonsterType> monsters;

    static {
        cards = new SparseArray<>();
        monsters = new SparseArray<>();

        register(420, DEBUG_0 = new CardType("Debug0", EARTHQUAKE, 1, 1, "This is a debug Card. Maxi probably made it do a bunch of weird shit.", "Debug Card"));

        register(0, ABOMINATION = new MonsterType("Abomination", TWILIGHT, 2, 3, 3, 1, "His attacks are as bad as his looks.", ""));
        register(1, ANGEL = new MonsterType("Angel", TWILIGHT, 4, 10, 2, 1, "Its wings seem rather fluffy.", ""));
        register(2, ARCHANGEL = new MonsterType("Archangel", TWILIGHT, 8, 24, 27, 2, "Must be placed on an Angel, upgrading it.", "Upgrades Angel"));
        register(3, new MonsterType("Bat", TWILIGHT, 1, 2, 2, 1, "Also known as flying rat.", ""));
        register(4, new MonsterType("Dracula", TWILIGHT, 4, 12, 4, 3, "Has lifesteal. Summons 2 Bats on death.", "Lifesteal, Spawns Bats"));
        register(5, new CardType("Halo", TWILIGHT, 3, 2, "Increases an (Arch)Angel's damage by 8.", "Buffs Angel"));
        register(6, new CardType("The Fool", TWILIGHT, 5, 3, "Destroy all cards on the field and deal their current health as damage to their respective owner.", "/kill @e"));
        register(7, new MonsterType("Alura Une", EARTHQUAKE, 7, 23, 6, 3, "Reflects 3 damage upon being attacked.", "Thorns"));
        register(8, new MonsterType("Bee", EARTHQUAKE, 2, 3, 4, 1, "BEES?", ""));
        register(9, new MonsterType("Ent", EARTHQUAKE, 5, 12, 12, 1, "Can't attack on successive turns.", "Slow"));
        register(10, new MonsterType("Golem", EARTHQUAKE, 1, 8, 0, 1, "Not the Pokémon.", ""));
        register(11, new CardType("Infestation", EARTHQUAKE, 3, 1, "Infest an enemy monster with parasites, lowering its health and damage by 4", "Damage Debuff"));
        register(12, new MonsterType("Medusa", EARTHQUAKE, 8, 8, 15, 3,"Attacking any target turns the opposing monster to stone, making it unable to attack but increasing its health by 10.", "Petrifying!"));
        register(13, new CardType("Medusa's Head", EARTHQUAKE, 4, 2, "Turns target monster to stone, making it unable to attack but increasing its health by 10.", "Petrifies!"));
        register(14, new MonsterType("Monkey", EARTHQUAKE, 5, 7, 7, 1, "A mega-Corp.", ""));
        register(15, new CardType("Overgrown", EARTHQUAKE, 3, 1, "Entangle an enemy monster and the two next to it, making them unable to attack for the next turn.", "AoE Stun"));
        register(16, new MonsterType("Overlord", EARTHQUAKE, 9, 25, 30, 3, "HEAVY.", "HEAVY."));
        register(17, new MonsterType("Queen Bee", EARTHQUAKE, 4, 3, 8, 2, "When summoned, plays up to two Bees from your deck.", "+2 Bees"));
        register(18, new CardType("Quicksand", EARTHQUAKE, 6, 2,"Destroy target enemy monster.", "Avada Kedavra"));
        register(19, new MonsterType("Bomber", INFERNAL, 4, 4, 4, 2, "His attacks explode, also damaging the monsters directly next to his target. Explodes when it dies.", "AoE Attacks"));
        register(20, new CardType("Drought", INFERNAL, 4, 1, "Destroys all TSUNAMI-type monsters on the field.", "Remove Water"));
        register(21, new CardType("Forest Fire", INFERNAL, 4, 1, "Destroys all EARTHQUAKE-type monsters on the field.", "Remove Earth"));
        register(22, new MonsterType("Phoenix", INFERNAL, 6, 12, 3, 2, "The first time it dies, it revives after 3 rounds.", "Bird Jesus"));
        register(23, new MonsterType("Ra", INFERNAL, 8, 10, 30, 3, "Mega Ultra Chicken.", ""));
        register(24, new MonsterType("Wisp", INFERNAL, 1, 2, 2, 1, "A little wandering fire-sprite.", ""));
        register(25, new MonsterType("Electron", STORM, 0, 1, 1, 1, "Stings a little.", ""));
        register(26, new CardType("EMP", STORM, 2, 1, "Paralyzes all STORM-type monsters on the field, making them unable to attack for 2 turns.", "Stuns all Storms"));
        register(27, new MonsterType("Scientist", STORM, 3, 4, 5, 2, "I'M NOT CRAZY!", ""));
        register(28, new MonsterType("Nue", STORM, 4, 3, 8, 1, "Wha?", ""));
        register(29, new CardType("Sharknado", STORM, 5, 1, "Plays all of the Sharks from your hand to the field.", "See: Name"));
        register(30, new CardType("Taser", STORM, 3, 2, "Paralyzes the target monster, making it unable to attack for the next round.", "Stuns"));
        register(31, new MonsterType("Thor", STORM, 6, 5, 20, 3, "He isn't Greek, dumbass!", ""));
        register(32, new MonsterType("Zeus", STORM, 7, 15, 23, 3, "The OG.", ""));
        register(33, new MonsterType("Zeus Jr.", STORM, 3, 1, 7, 2, "Chronos' lil Boi.", ""));
        register(34, new MonsterType("Dutchman", TSUNAMI, 5, 3, 10, 3, "He doesn't actually fly...", ""));
        register(35, new CardType("Jacuzzi", TSUNAMI, 2, 1, "If you currently have a TSUNAMI-type monster on your side of the field, heal all your monsters for 2 health points.", "Heals all Waters"));
        register(36, new MonsterType("The Kraken", TSUNAMI, 4, 8, 8, 3, "RELEASE IT!", ""));
        register(37, new MonsterType("Lazy Frog", TSUNAMI, 3, 12, 0, 2, "Increases his health by 1 every round.", "Grows Every Round"));
        register(38, new MonsterType("Shark", TSUNAMI, 1, 3, 1, 1, "Not the left shark, nor a baby shark.", ""));
        register(39, new MonsterType("Shiva", TSUNAMI, 6, 16, 2, 1, "At the end of every round, heals all nearby monsters for 2 health points.", "Every Round: AoE Heal"));
        register(40, new MonsterType("Spring Spirit", TSUNAMI, 4, 3, 1, 2, "When summoned, heals the monsters next to it for 4 health points.", "Summon: AoE Heal"));

    }

    private static CardType register(int typeID, CardType cardType){
        if(cards.get(typeID) != null) throw new IllegalArgumentException("Tried to register a cardType with an ID that was already used!");
        cards.put(typeID, cardType);
        return cardType;
    }
    private static MonsterType register(int typeID, MonsterType monsterType){
        if(monsters.get(typeID) != null || cards.get(typeID) != null) throw new IllegalArgumentException("Tried to register a monsterType with an ID that was already used!");
        monsters.put(typeID, monsterType);
        cards.put(typeID, monsterType);
        return monsterType;
    }

    public static class CardType {
        public final String name;
        public final Element element;
        public final int mana;
        public final String descriptionLong;
        public final String descriptionShort;
        public final int tier;
        public CardType(String name, Element element, int mana, int tier, String descrLong, String descrShort){
            this.name = name;
            this.element = element;
            this.mana = mana;
            this.tier = tier;
            this.descriptionLong = descrLong;
            this.descriptionShort = descrShort;
        }
    }
    public static class MonsterType extends CardType {
        public final int health, damage;
        public MonsterType(String name, Element element, int mana, int health, int damage, int tier, String descrLong, String descrShort){
            super(name, element, mana, tier, descrLong, descrShort);
            this.health = health;
            this.damage = damage;
        }
    }

    public static CardType getCardTypeByID (int cardID){
        CardType result = cards.get(cardID);
        if(result == null) throw new RuntimeException("Client attempted to query a card with unused ID "+cardID);
        return result;
    }
    public static MonsterType getMonsterByID (int monsterID){
        MonsterType result = monsters.get(monsterID);
        if(result == null) throw new RuntimeException("Server told client that monster with unused ID "+monsterID+" was played");
        return result;
    }
    public static int getRandomCardType(int tier){
        String response = HttpGetter.quickGet("CardTiers", "GetRandomCard", tier);
        return Integer.parseInt(response);

        /*int cardSize = cards.size()-1;
        int type = -1;

        boolean found=true;
        while(found){
            int randomNumber = (int)Math.round(Math.random() * (cardSize - 0 + 1)+ 0);
            if(cards.get(randomNumber).tier==tier)
            {
                type = randomNumber;
                found =false;
            }
        }
        return type;*/
    }
}
