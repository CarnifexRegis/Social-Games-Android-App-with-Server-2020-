package InGame;

public enum EPlayer{
    ONE, TWO;

    public EPlayer other(){
        return this == ONE ? TWO : ONE;
    }
}