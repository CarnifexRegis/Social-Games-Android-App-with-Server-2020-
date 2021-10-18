package container;



public class DeckSkeleton {
    private int id;
    private String name;
    private int amountOfCards;
    private int element;


    public DeckSkeleton(int id, String name, int amountOfCards, int element){
        this.id = id;
        this.name = name;
        this.amountOfCards = amountOfCards;
        this.element = element;
    }

    public int getId() {
        return id;
    }

    public int getAmountOfCards() {
        return amountOfCards;
    }

    public String getName() {
        return name;
    }

    public int getElement() {
        return element;
    }
}
