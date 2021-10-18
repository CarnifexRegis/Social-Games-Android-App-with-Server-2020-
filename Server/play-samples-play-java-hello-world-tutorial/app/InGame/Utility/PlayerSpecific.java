package InGame.Utility;

import InGame.EPlayer;

public class PlayerSpecific<T>{
    private T playerOnes;
    private T playerTwos;
    public PlayerSpecific (T playerOnes, T playerTwos){
        this.playerOnes = playerOnes;
        this.playerTwos = playerTwos;
    }
    public T get(EPlayer player){
        return player == EPlayer.ONE? playerOnes : playerTwos;
    }
    public void set(T value, EPlayer player){
        if(player == EPlayer.ONE) playerOnes = value;
        else playerTwos = value;
    }
}