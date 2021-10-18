package container;

public class TradeState{

    private Card cardUser;
    private Card cardTrader;
    private boolean acceptUser = false;
    private boolean acceptTrader = false;

    private boolean acceptMergeUser = false;
    private boolean acceptMergeTrader = false;

    private boolean cardTradedUser = false;
    private boolean cardTradedTrader = false;
    //0 if userCard or tradeCard is null
    //1 if userCard and tradeCard can be swapped
    //2 if userCard and tradcard are set and someone declined
    //3 initialization value nothing happened
    private int worked = 3;

    public void setWorked(int worked) {
        this.worked = worked;
    }

    public void initialize(){
        cardUser=null;
        cardTrader=null;
        acceptUser=false;
        acceptTrader=false;
        cardTradedUser=false;
        cardTradedTrader=false;
        acceptMergeUser = false;
        acceptMergeTrader = false;
        worked=3;
    }

    public int setIfWorked(){
        if(acceptUser&&acceptTrader){
            if(cardUser==null||cardTrader==null){
                worked = 0;
            } else{
                worked=1;
            }
        } else{
            worked = 2;
        }
        return worked;
    }

    public int setIfWorkedMerge(){
        if(acceptMergeUser&&acceptMergeTrader){
            if(cardUser==null||cardTrader==null){
                worked = 0;
            } else{
                worked=1;
            }
        } else{
            worked = 2;
        }
        return worked;
    }


    public void setCardUser(Card cardUser) {
        this.cardUser = cardUser;
    }

    public void setCardTrader(Card cardTrader) {
        this.cardTrader = cardTrader;
    }

    public Card getCardUser() {
        return cardUser;
    }

    public Card getCardTrader() {
        return cardTrader;
    }

    public boolean isAcceptUser() {
        return acceptUser;
    }

    public void setAcceptUser(boolean acceptUser) {
        this.acceptUser = acceptUser;
    }

    public boolean isAcceptTrader() {
        return acceptTrader;
    }

    public void setAcceptTrader(boolean acceptTrader) {
        this.acceptTrader = acceptTrader;
    }

    public boolean isCardTradedUser() {
        return cardTradedUser;
    }

    public boolean isCardTradedTrader() {
        return cardTradedTrader;
    }

    public void setCardTradedUser(boolean cardTradedUser) {
        this.cardTradedUser = cardTradedUser;
    }

    public void setCardTradedTrader(boolean cardTradedTrader) {
        this.cardTradedTrader = cardTradedTrader;
    }

    public boolean isAcceptMergeUser() {
        return acceptMergeUser;
    }

    public void setAcceptMergeUser(boolean acceptMergeUser) {
        this.acceptMergeUser = acceptMergeUser;
    }

    public boolean isAcceptMergeTrader() {
        return acceptMergeTrader;
    }

    public void setAcceptMergeTrader(boolean acceptMergeTrader) {
        this.acceptMergeTrader = acceptMergeTrader;
    }
}
