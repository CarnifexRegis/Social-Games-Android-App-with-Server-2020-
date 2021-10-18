package com.example.steppingmonsterduel2.Objects;

import com.example.steppingmonsterduel2.Services.Element;

public class Deck {
    private int id;
    private String name;
    private int cards;
    private int amountOfCards;
    private Element element;


    public Deck(int id, String name, int amountOfCards, Element element){
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

    public Element getElement() {
        return element;
    }
}
