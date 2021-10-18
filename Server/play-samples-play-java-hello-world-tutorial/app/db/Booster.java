package db;
import static lists.CardListVorschlag.*;
import InGame.Element;
import InGame.CardType.CardType;


public class Booster {
    int[] m_inferno={19,20,21,22,23,24};
    int[] m_tsunami={34,35,36,37,38,39,40};
    int[] m_storm={25,26,27,28,29,30,31,32,33};
    int[] m_earthquake={7,8,9,10,11,12,13,14,15,16,17,18};
    int[] m_twilight={0,1,2,3,4,5,6};

    int[] m_thisPack;
	
	int maxCardId = 40;
	Element m_boosterElement = null;

    //0=inferno  1= tsunami 2=storm 3=earth 4=twilight 5=normal
    public Booster(int boostertype){
		switch(boostertype){
			case 0:
				this.m_boosterElement = Element.INFERNO;
				break;
			case 1:
				this.m_boosterElement = Element.TSUNAMI;
				break;
			case 2:
				this.m_boosterElement = Element.STORM;
				break;
			case 3:
				this.m_boosterElement = Element.EARTHQUAKE;
				break;
			case 4:
				this.m_boosterElement = Element.TWILIGHT;
				break;
			case 5:
				this.m_boosterElement = null;
				break;
				
		}
		
		System.out.println("Got pack id " + boostertype + " and element " + this.m_boosterElement);
		//this.m_boosterType = boostertype;
		/*
        if(boostertype==0){this.m_thisPack=m_inferno;}
        if(boostertype==1){this.m_thisPack=m_tsunami;}
        if(boostertype==2){this.m_thisPack=m_storm;}
        if(boostertype==3){this.m_thisPack=m_earthquake;}
        if(boostertype==4){this.m_thisPack=m_twilight;}
		*/
    }
	/*
    public int get_random_int(int min, int max){
        Random affe = new Random();
        return affe.nextInt((max - min) + 1) + min;
    }*/
    public int[] open(){	
        int[] output= new int[5];
		
		for (int i=0; i<5; i++){
			CardType card = getDrop(m_boosterElement);
			output[i] = card.typeID;
		}
        
        return output;
    }
	
	/*
	if (this.m_boosterType == 5){
			for (int i=0; i<5; i++){
				output[i] = get_random_int(0, maxCardId);
			}
		}
		else{
			output[0]=m_thisPack[get_random_int(0,(m_thisPack.length-1))];
			output[1]=m_thisPack[get_random_int(0,(m_thisPack.length-1))];
			output[2]=m_thisPack[get_random_int(0,(m_thisPack.length-1))];
			output[3]=m_thisPack[get_random_int(0,(m_thisPack.length-1))];
			output[4]=m_thisPack[get_random_int(0,(m_thisPack.length-1))];
		}
		*/
}

