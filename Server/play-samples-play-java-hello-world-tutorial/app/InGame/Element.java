package InGame;

public enum Element {
    INFERNO(0, 3, 0),
    TSUNAMI(1, 1, 2),
    STORM(2, 2, 1),
    EARTHQUAKE(3, 0, 3),
    TWILIGHT(3, 1, 2);

    public final int databaseID;
    public final int healthBuff;
    public final int damageBuff;

    private Element(int databaseID, int damageBuff, int healthBuff){
        this.databaseID = databaseID;
        this.healthBuff = healthBuff;
        this.damageBuff = damageBuff;
    }

    public static Element fromID(int id){
        switch(id){
            case 0: return INFERNO;
            case 1: return TSUNAMI;
            case 2: return STORM;
            case 3: return EARTHQUAKE;
            case 4: return TWILIGHT;
            default: return null;
        }
    }
}
