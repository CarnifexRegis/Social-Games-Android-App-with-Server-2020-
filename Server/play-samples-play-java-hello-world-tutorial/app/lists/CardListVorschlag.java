package lists;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import InGame.Element;
import InGame.CardType.CardType;
import InGame.CardType.CardTypes;
import static InGame.CardType.CardTypes.*;


public class CardListVorschlag {

    private static int MAX_TIER = 3; //or whatever
    private static Map<Element, Map<Integer, List<CardType>>> cardEntries = new HashMap<>();
    private static int[] tierWeights = {10, 5, 2, 1};
    private static Map<CardType, Integer> cardTiers = new HashMap<>();

    private static void addEntry(CardType card, int tier){
        if(tier > MAX_TIER || tier < 1) throw new IllegalArgumentException("Tier is greater than MAX_TIER!");
        cardEntries.get(card.element).get(tier).add(card);
        cardTiers.put(card, tier);
    }

    public static CardType merge(CardType first, CardType second){
        int tierFirst = cardTiers.get(first);
        int tierSecond = cardTiers.get(second);
        if(first.element == second.element){ //same element
            if(tierFirst == tierSecond){ //same tier
                return getRandomCard(first.element, tierFirst == MAX_TIER? tierFirst : tierFirst + 1); //return a card of a higher tier (unless tier is already MAX_TIER)
            }
            else return getRandomCard(first.element, Integer.max(tierFirst, tierSecond)); //return card of the same tier.
        }
        else { //IDk. Merge elements? Random element?
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public static CardType getDrop(Element activeWeather){
        return getRandomCard(activeWeather, getRandomTier());
    }

    public static CardType getRandomCard(int tier){
    	return getRandomCard(getRandomElement(), tier);
	}

	public static Element getRandomElement(){
    	Random rand = new Random();
    	switch(rand.nextInt(5)){
			case 0: return Element.INFERNO;
			case 1: return Element.TSUNAMI;
			case 2: return Element.EARTHQUAKE;
			case 3: return Element.TWILIGHT;
			case 4: return Element.STORM;
			default: return null;
		}
	}

    private static CardType getRandomCard(Element element, int tier){
        //TODO maxi kann das und houssein wird das <3
		List<CardType> possibleCards = null; // list that will contains all the possible cards to drop
		if (element == null){
			//for normal packs going to use null ( so that no need to change the Element file
			//possibleCards is going to be the fusion of all lists of the tier
			possibleCards = new ArrayList<CardType>();
			for (Element elem : Element.values()){ 
				possibleCards.addAll(cardEntries.get(elem).get(tier));
			}
		}else{
			//if there's an element, just use a reference to the element / tier combo
			possibleCards = cardEntries.get(element).get(tier);
		}
		
		if (possibleCards == null){
			throw new NullPointerException("unexpected null list of cards to loot from");
		}
		if(possibleCards.size() == 0){
			throw new IllegalArgumentException("No card of the specified tier exists.");
		}
		
		//now that we have initialised the list of cards we can start the randomness
		Random rand = new Random();
		CardType output = possibleCards.get(rand.nextInt(possibleCards.size()));
		
		return output;
        
    }
	
	/**
	*tier chances : 
	* - tier 1 : 50%
	* - tier 2 : 40%
	* - tier 3 : 10%
	*/
    private static int getRandomTier(){
        //TODO maxi kann das und houssein wird das <3
		Random rand = new Random();
		int n = rand.nextInt(101);
		
		int tier = 1; // just default value in case
		if (n <= 50){
			tier = 1;
		} else if ( n <= 90){
			tier = 2;
		} else {
			tier = 3;
		}
        return tier;
    }

    static {
        for(Element element : Element.values()){
            Map<Integer, List<CardType>> tierEntries = new HashMap<>();
            for(int i=0; i<=MAX_TIER; i++){
                tierEntries.put(i, new LinkedList<>());
            }
            cardEntries.put(element, tierEntries);
        }

        addEntry(ABOMINATION, 1);
		addEntry(ANGEL, 1);
		addEntry(ARCHANGEL, 2);
		addEntry(BAT, 1);
		addEntry(DRACULA, 3);
		addEntry(HALO, 2);
		addEntry(THE_FOOL, 3);
		
		addEntry(ALURA_UNE, 3);
		addEntry(BEE, 1);
		addEntry(ENT, 1);
		addEntry(GOLEM, 1);
		addEntry(INFESTATION, 1);
		addEntry(MEDUSA, 3);
		addEntry(MEDUSAS_HEAD, 2);
		addEntry(MONKEY_BUSINESS, 1);
		addEntry(OVERGROWN, 1);
		addEntry(OVERLORD_OF_ROCK_AND_METAL, 3);
		addEntry(QUEEN_BEE, 2);
		addEntry(QUICKSAND, 2);
		
		addEntry(BOMBER_THE_PYROMANIAC, 2);
		addEntry(DROUGHT, 1);
		addEntry(FOREST_FIRE, 1);
		addEntry(PHOENIX, 2);
		addEntry(RA, 3);
		addEntry(WILL_O_WISP, 1);
		
		addEntry(ELECTRON, 1);
		addEntry(EMP, 1);
		addEntry(MAD_SCIENTIST, 2);
		addEntry(NUE, 1);
		addEntry(SHARKNADO, 1);
		addEntry(TASER, 2);
		addEntry(THOR, 3);
		addEntry(ZEUS, 3);
		addEntry(ZEUS_JR, 2);
		
		addEntry(FLYING_DUTCHMAN, 3);
		addEntry(JACUZZI, 1);
		addEntry(THE_KRAKEN, 3);
		addEntry(LAZY_FROG, 2);
		addEntry(SHARK, 1);
		addEntry(SHIVA, 1);
		addEntry(SPIRIT_OF_THE_SPRING, 2);
		
        //TODO...
    }
}
