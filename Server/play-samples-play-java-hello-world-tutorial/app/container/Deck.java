package container;

import java.util.ArrayList;

public class Deck {
	private int id;
	private ArrayList<Card> deck;
	
	public Deck(int id, ArrayList<Card> deck) {
		super();
		this.id = id;
		this.deck = deck;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<Card> getDeck() {
		return deck;
	}
	public void setDeck(ArrayList<Card> deck) {
		this.deck = deck;
	}
	
}
