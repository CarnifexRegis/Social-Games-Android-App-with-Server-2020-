package InGame.Utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import InGame.EPlayer;

public class BoardPosition {
    public final EPlayer side;
    public final int slotID;

    public BoardPosition(EPlayer side, int slotID){
        if(side == null || slotID < 0 || slotID > 4) throw new IllegalArgumentException("Invalid arguments for BoardPosition.");
        this.side = side;
        this.slotID = slotID;
    }

    public int getSlotID(){
        return slotID;
    }
    public EPlayer getSide(){
        return side;
    }

    public @Nullable BoardPosition slotOffset(int offset){
        int newSlot = slotID + offset;
        if(newSlot < 0 || newSlot > 4) return null;
        return new BoardPosition(side, newSlot);
    }
    public BoardPosition across(){
        return new BoardPosition(side.other(), slotID);
    }

    @Override @Nonnull
    public String toString(){
        return side + "[" + slotID+"]";
    }
    @Override
    public boolean equals(Object other){
        BoardPosition bp = (BoardPosition) other;
        if(other == null) return false;
        else return slotID == bp.slotID && side == bp.side;
    }
}
