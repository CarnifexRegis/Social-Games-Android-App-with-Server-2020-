package com.example.steppingmonsterduel2.Services;

import android.support.annotation.Nullable;

import com.example.steppingmonsterduel2.R;

/*
Some info for the database packs type integer :
 //0=inferno  1= tsunami 2=storm 3=earth 4=twilight
 */
public enum Element {
    INFERNAL(0, "Infernal", 3, 0, R.drawable.deck_inferno, R.drawable.spell_inferno, R.drawable.monster_inferno),
    TSUNAMI(1, "Tsunami", 1, 2, R.drawable.deck_tsunami, R.drawable.spell_tsunami, R.drawable.monster_tsunami),
    STORM(2, "Storm", 2, 1, R.drawable.deck_storm, R.drawable.spell_storm, R.drawable.monster_storm),
    EARTHQUAKE(3, "Earthquake", 0, 3, R.drawable.deck_land, R.drawable.spell_land, R.drawable.monster_land),
    TWILIGHT(4, "Twilight", 1, 2, R.drawable.deck_twilight, R.drawable.spell_twilight, R.drawable.monster_twilight);
    //NONE(-1, "None", 0, 0, R.drawable.profile_icon, R.drawable.affe, R.drawable.profile_icon);

    public final int databaseID;
    public final String name;
    public final int damageBuff;
    public final int healthBuff;
    public final int deckResourceID;
    public final int spellResourceID;
    public final int monsterResourceID;

    private Element(int databaseID, String name, int damageBuff, int healthBuff, int deckResourceID, int spellResourceID, int monsterResourceID){
        this.databaseID = databaseID;
        this.name = name;
        this.damageBuff = damageBuff;
        this.healthBuff = healthBuff;
        this.deckResourceID = deckResourceID;
        this.spellResourceID = spellResourceID;
        this.monsterResourceID = monsterResourceID;
    }

    public static Element fromID(int databaseID){
        switch (databaseID){
            case 0:
                return INFERNAL;
            case 1:
                return TSUNAMI;
            case 2:
                return STORM;
            case 3:
                return EARTHQUAKE;
            case 4:
                return TWILIGHT;
            default:
                throw new IllegalArgumentException("No element with ID "+databaseID+" exists.");
        }
    }

    public static int getDeckResourceID(@Nullable Element element){
        if(element == null) return R.drawable.standart;
        else return element.deckResourceID;
    }
    public static int getSpellResourceID(@Nullable Element element){
        if(element == null) return R.drawable.standart;
        else return element.spellResourceID;
    }
    public static int getMonsterResourceID(@Nullable Element element){
        if(element == null) return R.drawable.standart;
        else return element.monsterResourceID;
    }
}
