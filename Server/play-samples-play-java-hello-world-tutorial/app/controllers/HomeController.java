package controllers;

import play.mvc.*;
import db.*;
import container.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lists.CardListVorschlag;
import InGame.GameInstance;
import InGame.Utility.BoardPosition;
import InGame.Utility.PlayerBoardUpdate;
import InGame.EPlayer;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     *
     */

    @Inject
    private DBManager dbm;
   // @Inject
   // private Quest1 q1;
    @Inject
    private HashMap<String,TradeState> trades;

    @Inject
    private MapDuo<Integer, GameInstance> gameMap;
    @Inject
    private HashMap<Integer, EPlayer> roleMap;
    @Inject
    private HashMap<Integer, MatchLobby> matchLobbyMap;
    @Inject
    private HashSet<Integer> matchmakingPool;

    public HomeController() {
        super();
        dbm = new DBManager();
     //    q1 = new Quest1();
        trades = new HashMap<>();
        gameMap = new MapDuo<>();
        roleMap = new HashMap<>();
        matchLobbyMap = new HashMap<>();
        matchmakingPool = new HashSet<>();
        initMatchmakingThread();
    }


    public Result index() {
        return ok(views.html.index.render());
    }

    public Result explore() {
        return ok(views.html.explore.render());
    }


    public Result tutorial() {
        return ok(views.html.tutorial.render());
    }

    public Result test() {
        return ok("hello it is me!");
    }

 //start quest handdling
        private int standardTradesAndMatches = 2;

    public boolean checkCompletionMontly(int fid) {
        int [] sc = dbm.getSocialScores(fid);
        if(sc!= null) {
            if(sc[1]>=3&&sc[3]>=3) {
                System.out.println("finish");
                return true;
            }else {
                System.out.println("no finish");
                return false;
            }
        }else {
            System.out.println("db error");
            return false;
        }
    }

    //update the quest
    public void updateMonthlyScores(int fid, int trade, int match, int temprematchcount, int temptradecount,int score,int date,int merge) {
        dbm.updateSocialFriend(fid, trade,temptradecount, match ,temprematchcount, score, date,merge);
    }


    //sets a picture to card in databse
    public Result setPidToCard(int iid, int pid){
        Result ret;
        boolean getIfWorked = dbm.updatePidCard(pid,iid);
        ObjectNode node = Json.newObject();
        node.put("Worked?",getIfWorked);
        ret = ok(node);
        return ret;
    }


    public int calculateQuests(int score){
        int result = (score+1)%19+standardTradesAndMatches;
        return result;
    }

    public int getStandardTradesAndMatches() {
        return standardTradesAndMatches;
    }
    //end questHandling


    //insert a new user into database if a new one registered
    public Result insertUser(String fid,String name, String mail){
        System.out.println("User inserted");
        boolean worked = dbm.insertUser(fid,name,mail);
        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);

        int id = dbm.getMyID(fid);
        dbm.insertCardsStandard(id);

        return ok(node);
    }

    //getid from the sure from the firebase id
    public Result getID(String fid){
        int i = dbm.getMyID(fid);
        ObjectNode node = Json.newObject();
        node.put("ID",i);
        return ok(node);
    }

    //get the currents user information as id steps wins loses name and picture
    public Result getMyUserInformation(int iid){
        User user = dbm.getUser(iid);
        ObjectNode node = Json.newObject();
        node.put("ID",user.getId());
        node.put("Steps",user.getSteps());
        node.put("Wins",user.getWins());
        node.put("Loses",user.getLosses());
        node.put("Name",user.getName());
        node.put("Picture",user.getPicture());
        return ok(node);
    }


    //get all the friends out of database
    public Result getFriends(int iid){
        Result ret;
        ArrayList<FriendFullInfo> friendArray = dbm.getFriends(iid);

        if(friendArray==null){
            return badRequest("Something went wrong by searching for friends!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (FriendFullInfo f: friendArray){
                ObjectNode node = Json.newObject();
                node.put("Fid",f.getFid());
                node.put("ID",f.getIid());
                node.put("Accepted",f.getAccepted());
                node.put("Name",f.getName());
                node.put("Picture",f.getPicture());
                node.put("Steps",f.getSteps());
                node.put("Winrate",f.getWinrate());
                gameArray.add(node);
            }
            result.put("Friends",gameArray);

            if(gameArray.size() > 0){
                result.put("Friends", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }


    //inserts a friend into database
    public Result insertFriend(int iid1, int iid2){
        int getIfWorked = dbm.insertFriend(iid1,iid2);
        ObjectNode node = Json.newObject();
        node.put("Worked",getIfWorked);
        return ok(node);
    }

    //accept s a friend and put the information into database
    public Result acceptFriend(int iid1, int iid2){
        Result ret;
        boolean getIfFriendAccepted = dbm.updateAcceptFriend(iid1,iid2);
        ObjectNode node = Json.newObject();
        node.put("FriendAccepted",getIfFriendAccepted);
        ret = ok(node);
        return ret;
    }

    //deletes a friend  of database
    public Result deleteFriend(int iid1, int iid2){
        Result ret;
        boolean getIfFriendDeleted = dbm.deleteFriend(iid1,iid2);
        ObjectNode node = Json.newObject();
        node.put("FriendDeleted",getIfFriendDeleted);
        ret = ok(node);
        return ret;
    }

    //set a picutre to the database
    public Result setPicture(String photo,int iid){
        Result ret;
        System.out.println(photo);
        int getPidForUser = dbm.insertPicture(photo,iid);
        ObjectNode node = Json.newObject();
        node.put("Pid",getPidForUser);
        ret = ok(node);
        return ret;
    }


    //sets the picture id to the user
    public Result setPidToUser(int iid, int pid){
        Result ret;
        boolean getIfWorked = dbm.updatePidUser(pid,iid);
        ObjectNode node = Json.newObject();
        node.put("Worked?",getIfWorked);
        ret = ok(node);
        return ret;
    }


    //updates the questscore at the start of a new month
    //quest will be initialized again and questscore will be increased
    public Result getQuest(int fid){
        int[] socialScore = dbm.getSocialScores(fid);
        Date date = new Date();
        //month +1 to get our months
        int monthATM = date.getMonth();

        System.out.println(socialScore[5]+"");
        if(socialScore==null){
            return badRequest("Something went wrong by searching for quests!");
        } else{
            if((socialScore[5]+1)%12==monthATM&&(socialScore[1]-socialScore[0])<=0&&(socialScore[3]-socialScore[2])<=0) {
                int tempQuest = calculateQuests(socialScore[4]);
                updateMonthlyScores(fid, 0, 0, tempQuest,tempQuest, socialScore[4]+20,monthATM,(socialScore[4]+20)/20);

                //add steps and boosterpack to user;
                int iid1 = socialScore[6];
                int iid2 = socialScore[7];
                int stepsIid1 = dbm.getSteps(iid1);
                int stepsIid2 = dbm.getSteps(iid2);

                int newStepsIid1 = stepsIid1+socialScore[4]*100;
                int newStepsIid2 = stepsIid2+socialScore[4]*100;


                for(int i=0;i<(socialScore[4]/20)+1;i++) {
                    int randomNumber1 = (int)(Math.random() * 5);
                    int randomNumber2 = (int)(Math.random() * 5);

                    dbm.insertPack( iid1, randomNumber1 );
                    dbm.insertPack( iid2, randomNumber2 );
                }
                dbm.updateSteps(newStepsIid1,iid1);
                dbm.updateSteps(newStepsIid2,iid2);
                ObjectNode node = Json.newObject();
                node.put("Trade", 0);
                node.put("TempTrade", tempQuest);
                node.put("Match", 0);
                node.put("TempMatch", tempQuest);
                node.put("Score", socialScore[4]+20);
                node.put("Update?", "Yes");
                return ok(node);
            } else if((socialScore[5]+1)%12==monthATM){
                updateMonthlyScores(fid, 0, 0, getStandardTradesAndMatches(), getStandardTradesAndMatches(),0,monthATM,0);
                ObjectNode node = Json.newObject();
                node.put("Trade", 0);
                node.put("TempTrade",getStandardTradesAndMatches());
                node.put("Match", 0);
                node.put("TempMatch", getStandardTradesAndMatches());
                node.put("Score",0);
                node.put("Update?", "No");
                return ok(node);
            } else{
                if(socialScore[5]%12!=monthATM){
                    updateMonthlyScores(fid, 0, 0, getStandardTradesAndMatches(), getStandardTradesAndMatches(),0,monthATM,0);
                }
                ObjectNode node = Json.newObject();
                node.put("Trade", socialScore[0]);
                node.put("TempTrade", socialScore[1]);
                node.put("Match", socialScore[2]);
                node.put("TempMatch", socialScore[3]);
                node.put("Score", socialScore[4]);
                node.put("Update?", "No");
                return ok(node);
            }
        }
    }


    //get all matches requests that are sent to  you
    public Result getMatchRequests(int iid){
        Result ret;
        ArrayList<Integer> matchArray = dbm.getMatchRequests(iid);

        if(matchArray==null){
            return badRequest("Something went wrong by searching for Trades!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (Integer f: matchArray){
                User user = dbm.getUser(f);
                ObjectNode node = Json.newObject();
                node.put("ID",user.getId());
                node.put("Steps",user.getSteps());
                node.put("Wins",user.getWins());
                node.put("Loses",user.getLosses());
                node.put("Name",user.getName());
                node.put("Picture",user.getPicture());
                gameArray.add(node);
            }
            result.put("MatchRequests",gameArray);

            if(gameArray.size() > 0){
                result.put("MatchRequests", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }

    //get all trade requests that are sent to  you
    public Result getTradeRequests(int iid){
        Result ret;
        ArrayList<Integer> tradeArray = dbm.getTradeRequests(iid);

        if(tradeArray==null){
            return badRequest("Something went wrong by searching for Trades!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (Integer f: tradeArray){
                User user = dbm.getUser(f);
                ObjectNode node = Json.newObject();
                node.put("ID",user.getId());
                node.put("Steps",user.getSteps());
                node.put("Wins",user.getWins());
                node.put("Loses",user.getLosses());
                node.put("Name",user.getName());
                node.put("Picture",user.getPicture());
                gameArray.add(node);
            }
            result.put("TradeRequests",gameArray);

            if(gameArray.size() > 0){
                result.put("TradeRequests", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }

    //if someone sent a trade requests this method will update this in the database
    public Result setTradeReq(int userID,int fid)
    {
        boolean worked = dbm.updateSendRequest(userID, fid, 1);
        if(!worked){
            return badRequest("Something went wrong by sending a request");
        } else{
            return ok("Worked");
        }
    }


    //if someone sent a match requests this method will update this in the database
    public Result setMatchReq(int userID,int fid)
    {
        boolean worked = dbm.updateSendRequest(userID, fid, 0);
        if(!worked){
            return badRequest("Something went wrong by sending a request");
        } else{
            return ok("Worked");
        }
    }

    //reset the matchrequests
    public Result resetMatchReq(int userID, int fid){
        System.out.println("ResetMatch");
        int friendID = dbm.getFid(userID, fid);
        System.out.println("Get this friendid: "+friendID);
        if(friendID==-1)
        {
            return badRequest("Something went wrong by sending a request");
        }
        boolean worked = dbm.updateResetRequest(0 , friendID);
        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);
        return ok(node);
    }

    //reset the traderequests
    public Result resetTradeReq(int userID, int fid){
        System.out.println("ResetTrade");
        int friendID = dbm.getFid(userID, fid);
        if(friendID==-1)
        {
            return badRequest("Something went wrong by sending a request");
        }
        boolean worked = dbm.updateResetRequest(1 , friendID);
        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);
        return ok(node);
    }


    //accept the trade request someone got from a friend
    public Result acceptTradeReq(int userID,int otherID){

        int friendID = dbm.getFid(userID, otherID);

        String tradesOfIDs = userID+";"+otherID;

        TradeState state = new TradeState();

        System.out.println(tradesOfIDs);
        trades.put(tradesOfIDs,state);

        if(friendID==-1)
        {
            return badRequest("Something went wrong by sending a request");
        }
        boolean worked = dbm.updateAcceptRequest(userID, 1, friendID);
        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);

        return ok(node);
    }


    //accept the match request someone got from a friend
    public Result acceptMatchReq(int userID,int otherID){

        int friendID = dbm.getFid(userID, otherID);
        if(friendID==-1)
        {
            return badRequest("Something went wrong by sending a request");
        }
        boolean worked = dbm.updateAcceptRequest(userID, 0, friendID);
        if(worked) instantiateLobby(userID, otherID); //LG Maxi
        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);
        return ok(node);
    }


    //get the trsdes that were accepted by a friend
    public Result getAcceptedTrades(int iid){
        Result ret;
        ArrayList<Integer> acceptedTrades = dbm.getAcceptedTradeRequests(iid);

        if(acceptedTrades==null){
            return badRequest("Something went wrong by searching for Trades!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (Integer f: acceptedTrades){
                User user = dbm.getUser(f);
                ObjectNode node = Json.newObject();
                node.put("ID",user.getId());
                node.put("Steps",user.getSteps());
                node.put("Wins",user.getWins());
                node.put("Loses",user.getLosses());
                node.put("Name",user.getName());
                node.put("Picture",user.getPicture());
                gameArray.add(node);
            }
            result.put("AcceptedTrades",gameArray);

            if(gameArray.size() > 0){
                result.put("AcceptedTrades", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }


    //gets the matches which got accepted by your friend
    public Result getAcceptedMatches(int iid){
        Result ret;
        ArrayList<Integer> acceptedMatches = dbm.getAcceptedMatchRequests(iid);

        if(acceptedMatches==null){
            return badRequest("Something went wrong by searching for Trades!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (Integer f: acceptedMatches){
                User user = dbm.getUser(f);
                ObjectNode node = Json.newObject();
                node.put("ID",user.getId());
                node.put("Steps",user.getSteps());
                node.put("Wins",user.getWins());
                node.put("Loses",user.getLosses());
                node.put("Name",user.getName());
                node.put("Picture",user.getPicture());
                gameArray.add(node);
            }
            result.put("AcceptedMatches",gameArray);

            if(gameArray.size() > 0){
                result.put("AcceptedMatches", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }

    //updates the steps of the user
    public Result updateSteps(int userID, float distance) {
    	Result ret;
    	int actualSteps = dbm.getSteps(userID);
    	if (actualSteps == -1)
    		return badRequest("Bad userID");
    	
    	ObjectNode node = Json.newObject();
    	//Distance too big, return actual steps
    	if (distance > 30) {
    		 node.put("Steps",actualSteps);
    		 ret = ok(node);
    		 return ret;
    	}
    	else {
    		int extraSteps = (int)Math.floor(distance*1.31f);
    		int updatedSteps = actualSteps + extraSteps;
    		//Dbm increments the steps and doesn't overwrite the value ( which is useful for shopping)
    		boolean worked = dbm.updateSteps(extraSteps, userID);
			System.out.println("user " + userID + " had " + actualSteps + " and added " + extraSteps + " and got " + updatedSteps);
    		if (worked)
    			node.put("Steps",updatedSteps);
    		else
    			node.put("Steps",actualSteps);
    		ret = ok(node);
            return ret;
    	}
    }

    //updates the trade count from questscore
    public Result updateTradeCount(int iid1, int iid2){
        Result ret;
        ObjectNode node = Json.newObject();

        int fid = dbm.getFid(iid1,iid2);
        boolean check = dbm.updateSocialTradeCount(fid);

        node.put("Worked?",check);
        ret = ok(node);
        return ret;
    }

    //increments the matchcount
    public Result updateMatchCount(int iid1, int iid2){
        Result ret;
        ObjectNode node = Json.newObject();

        int fid = dbm.getFid(iid1,iid2);
        if(fid!=-1) {
            boolean check = dbm.updateSocialMatchCount( fid );
            node.put( "Worked?", check );
            ret = ok( node );
            return ret;
        } else{
            System.out.println("Couple are no friends");
            return badRequest();
        }
    }

    public Result incrementWin(int iid){
        Result ret;
        ObjectNode node = Json.newObject();

        boolean worked = dbm.incrementWin(iid);
        node.put("Worked?",worked);
        ret = ok(node);
        return ret;
    }

    public Result incrementLose(int iid){
        Result ret;
        ObjectNode node = Json.newObject();

        boolean worked = dbm.incrementLose(iid);
        node.put("Worked?",worked);
        ret = ok(node);
        return ret;
    }
    
    public Result addPack(int userID, int packType) {
    	Result ret;
    	ObjectNode node = Json.newObject();
    	
    	boolean worked = dbm.insertPack(packType, userID);
    	node.put("Worked?",worked);
    	ret = ok(node);
    	return ret;
    }
	
	public Result getPacks(int userID){
		Result ret;
		ArrayList<Integer> userPacks = dbm.getPacks(userID);
		
		if (userPacks == null)
			return badRequest("Something went wrong by searching for Trades!");
			
		
		ObjectNode result = Json.newObject();
        ArrayNode packTypes = result.arrayNode();
        for (Integer type: userPacks){
            ObjectNode node = Json.newObject();
            node.put("Type", type);
            packTypes.add(node);
        }
        result.put("PackTypes",packTypes);
		
		ret = ok(result);
        return ret;
	}
	
	public Result buyPack(int userID, int packType){
		Result ret;
		final int PACK_PRICE = 0;
		ObjectNode result = Json.newObject();
		
		int actualSteps = dbm.getSteps(userID);
		if (actualSteps < PACK_PRICE){
			result.put("worked", false);
		}else{
			boolean worked = dbm.insertPack(packType, userID);
			System.out.println("user obtained pack " + packType);
			boolean worked2 = false;
			if (worked){
				worked2 = dbm.updateSteps(-PACK_PRICE, userID);
			}
			
			result.put("worked", worked && worked2);
		}
		
		ret = ok(result);
		return ret;
	}
	
	public Result openPack(int userID, int packType){
		Result ret;
		
		//remove pack with type from user's collection
		ArrayList<Integer> packFromCards = dbm.removePack(packType, userID);
		System.out.println("opened pack" + packType);
		ObjectNode result = Json.newObject();
		if (packFromCards == null){
			result.put("worked", false);
			ret = ok(result);
			return ret;	
		}
		result.put("worked", true);	
				
		//the arraylist contains 5 cards, return their ids to the user
		
		ArrayNode cardsId = result.arrayNode();
		for (Integer cardID: packFromCards){
			ObjectNode node = Json.newObject();
            node.put("ID", cardID);
            cardsId.add(node);
		}
		result.put("CardIds", cardsId);
		ret = ok(result);
		return ret;
	}


	// put the card you want to trade
	public Result putCard(int userID, int traderID,String role,int idCard,int type,String picture){
        if(trades.containsKey(userID+";" +traderID))
        {
            Card card = new Card(idCard,type,picture);
            if(role.equals("Opponent")){
                trades.get(userID+";" +traderID).setCardTrader(card);
            } else{
                trades.get(userID+";" +traderID).setCardUser(card);
            }
            return ok("Worked");
        }
        return ok( "NoTrade" );
    }

    //set trade accepted for the user
    public Result acceptTrade(int userID, int traderID,String role){
        if(trades.containsKey(userID+";" +traderID))
        {
            ObjectNode node = Json.newObject();
            node.put("Worked",-1);
            if(role.equals("Opponent")){
                trades.get(userID+";" +traderID).setAcceptTrader(true);
                int worked = trades.get(userID+";" +traderID).setIfWorked();
                node.put("Worked",worked);
                node.put("CardGot",true);
            } else{
                trades.get(userID+";" +traderID).setAcceptUser(true);
                int worked = trades.get(userID+";" +traderID).setIfWorked();
                node.put("Worked",worked);
                node.put("CardGot",true);
            }
            return ok(node);
        }
        return ok( "NoTrade" );
    }

    //set merge to accepted for the user
    public Result acceptTradeMerge(int userID, int traderID,String role){
        if(trades.containsKey(userID+";" +traderID))
        {
            ObjectNode node = Json.newObject();
            node.put("Worked",-1);
            if(role.equals("Opponent")){
                trades.get(userID+";" +traderID).setAcceptMergeTrader(true);
                int worked = trades.get(userID+";" +traderID).setIfWorkedMerge();
                System.out.println(worked+"");
                node.put("Worked",worked);
                node.put("CardGot",true);
            } else{
                trades.get(userID+";" +traderID).setAcceptMergeUser(true);
                int worked = trades.get(userID+";" +traderID).setIfWorkedMerge();
                node.put("Worked",worked);
                node.put("CardGot",true);
            }
            return ok(node);
        }
        return ok( "NoMerge" );
    }

    //gets if trade was accepted
    public Result getIfAccepted(int userID, int traderID,String role){
        if(trades.containsKey(userID+";" +traderID))
        {
            ObjectNode node = Json.newObject();
            node.put("Worked",-1);
            if(role.equals("Opponent")){
                int worked = trades.get(userID+";" +traderID).setIfWorked();
                node.put("Worked",worked);
            } else{
                int worked = trades.get(userID+";" +traderID).setIfWorked();
                node.put("Worked",worked);
            }
            return ok(node);
        }
        return ok( "NoTrade" );
    }

    //gets if merge was accepted
    public Result getIfAcceptedMerge(int userID, int traderID,String role){
        if(trades.containsKey(userID+";" +traderID))
        {
            ObjectNode node = Json.newObject();
            node.put("Worked",-1);
            if(role.equals("Opponent")){
                int worked = trades.get(userID+";" +traderID).setIfWorkedMerge();
                node.put("Worked",worked);
            } else{
                int worked = trades.get(userID+";" +traderID).setIfWorkedMerge();
                node.put("Worked",worked);
            }
            return ok(node);
        }
        return ok( "NoTrade" );
    }

    //returns the traded card
    public Result getTradedCard(int userID, int traderID,String role){
        if(trades.containsKey(userID+";" +traderID))
        {
            ObjectNode node = Json.newObject();
            node.put("CardID", -1);
            TradeState state = trades.get(userID+";" +traderID);
            if(role.equals("Opponent")){
                node.put("CardID", state.getCardUser().getId());
                node.put("CardType", state.getCardUser().getType());
                node.put("CardPicture", state.getCardUser().getPicture());
            } else{
                node.put("CardID", state.getCardTrader().getId());
                node.put("CardType", state.getCardTrader().getType());
                node.put("CardPicture", state.getCardTrader().getPicture());
            }
            return ok(node);
        }
        return ok( "NoTrade" );
    }


    //set if card was traded
    public Result setCardGot(int userID, int traderID,String role) {
        if (trades.containsKey( userID + ";" + traderID )) {

            TradeState state = trades.get(userID+";" +traderID);

            if(role.equals("Opponent")){
                state.setCardTradedTrader(true);
            }else{
                state.setCardTradedUser(true);
            }
            if(state.isCardTradedUser()&&state.isCardTradedTrader()){
                state.initialize();
                return ok("Initialized");
            }
            return ok("OneSided");
        }else{
            return ok("NoTrade");
        }

    }
    //checks if trading instance is available
    public Result checkIfInstanceAvailable(int userID, int traderID){
        if(trades.containsKey(userID+";" +traderID)) {
            return ok("InstanceFound");
        } else{
            return ok("Instance not found");
        }
    }


    //get card of opponent that he wants to trade
    public Result getOpponentCard(int userID, int traderID,String role,int idCard){


        if(trades.containsKey(userID+";" +traderID))
        {
            TradeState state = trades.get(userID+";" +traderID);
            ObjectNode node = Json.newObject();
            if(role.equals("Opponent")) {
                if(state.getCardUser()!=null) {
                    if (idCard == state.getCardUser().getId()) {
                        return ok( "Same" );
                    } else {
                        node.put( "CardID", state.getCardUser().getId() );
                        node.put( "CardType", state.getCardUser().getType() );
                        node.put( "CardPicture", state.getCardUser().getPicture() );
                        return ok( node );
                    }
                } else{
                    return ok("Not set");
                }
            } else{
                if(state.getCardTrader()!=null) {
                    if (idCard == state.getCardTrader().getId()) {
                        return ok( "Same" );
                    } else {
                        node.put( "CardID", state.getCardTrader().getId() );
                        node.put( "CardType", state.getCardTrader().getType() );
                        node.put( "CardPicture", state.getCardTrader().getPicture() );
                        return ok( node );
                    }
                } else{
                    return ok("Not set");
                }
            }
        } else{
            return ok("NoTrade");
        }

    }

    //get the cards of the user
    public Result getCards(int id){
        ArrayList<Card> cards = dbm.getMyCards(id);

        Result ret;

        if(cards==null){
            return badRequest("Something went wrong by searching for friends!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (Card f: cards){
                ObjectNode node = Json.newObject();
                node.put("Cid",f.getId());
                node.put("Type",f.getType());
                node.put("Picture",f.getPicture());
                gameArray.add(node);
            }
            result.put("Cards",gameArray);

            if(gameArray.size() > 0){
                result.put("Cards", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }

    //switches cards/ changes owner
    public Result switchCard(int cid, int owner){

        System.out.println(owner+" got "+cid);
        boolean hasWorked = dbm.updateCardOwner(cid,owner);

        if(!hasWorked){
            return badRequest("NoTrade");
        } else{
            return ok("Worked");
        }
    }

    //switches the card with the trader and updates the type
    public Result switchCardAndUpdate(int cid, int owner, int type){

        System.out.println(owner+" got "+cid);
        boolean hasWorked = dbm.updateCardOwnerAndUpdateCard(cid,owner,type);

        if(!hasWorked){
            return badRequest("NoTrade");
        } else{
            return ok("Worked");
        }
    }



    //destroys the tradeinstance
    public Result destroyTrade(int userID, int traderID){
        if(trades.containsKey(userID+";" +traderID)) {
            trades.remove(userID+";" +traderID);
            return ok("Worked");
        }
        return ok("NoTrade");
    }

    //returns if trade was acceptee
    public Result testIfTradeHappening(int userID,int traderID){
        System.out.println(userID+";" +traderID);
        if(trades.containsKey(userID+";" +traderID))
        {
            return ok("Worked");
        }
        return ok( "NoTrade" );
    }


    //gets the skeleton of the deck
    public Result getDeckSkeleton(int userID){
        System.out.println(" get deck Skeleton handle ");
        Result ret;
        ArrayList<DeckSkeleton> decks = dbm.getEditDeck(userID);
        if(decks==null){
            return badRequest("Something went wrong by searching for DeckSkeletons!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            for (DeckSkeleton d: decks){
                ObjectNode node = Json.newObject();
                node.put("ID",d.getId());
                node.put("Name",d.getName());
                node.put("AmountOfCards",d.getAmountOfCards());
                node.put("Element",d.getElement());
                gameArray.add(node);
            }
            result.put("Decks",gameArray);

            if(gameArray.size() > 0){
                result.put("Decks", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }


    //gets the elements of the deck
    public Result getDeckElements(int did){
        ArrayList<Card> cards = dbm.getDeckById(did).getDeck();
        Result ret;

        if(cards==null){
            return badRequest("Something went wrong by searching for deck elements!");
        } else{
            ObjectNode result = Json.newObject();
            ArrayNode gameArray = result.arrayNode();
            System.out.println("DeckByID: "+ did+"size"+cards.size());
            for (Card f: cards){
                ObjectNode node = Json.newObject();
                node.put("Cid",f.getId());
                node.put("Type",f.getType());
                node.put("Picture",f.getPicture());
                System.out.println("cid: "+ f.getId() +"Type: "+ f.getType() + "Picture: "+ f.getPicture());
                gameArray.add(node);
            }
            result.put("Cards",gameArray);

            if(gameArray.size() > 0){
                result.put("Cards", gameArray);
            }
            ret = ok(result);
            return ret;
        }
    }

    //removes card from deck
    public Result removeCardFromDeck(int cid , int did){
        Result ret;
        boolean worked = dbm.updateDeckDelete( did, cid);
        System.out.println("removeCardDeck did: "+did+" cid: "+cid+worked);
        // ObjectNode node = Json.newObject();
        //  node.put("Worked",worked);
        String response = "success";
        if(!worked){
            System.out.println("failed to remove Card from Deck");
            response = "failed";
        }
        ret = ok(response);
        return ret;
    }

    //adds a card to the deck
    public Result addCardToDeck(int owner,  int cid,int did){
        Result ret;
        boolean worked = dbm.updateDeckAdd(owner,did,cid);
        System.out.println("addCardDeck did: "+did+" cid: "+cid+worked);
        String response = "success";
        if(!worked){
            System.out.println("failed to add Card to Deck");
            response = "failed";
        }
        ret = ok(response);
        return ret;
    }

    //changes the name of the deck
    public Result changeDeckName(int did, String name){
        Result ret;
        boolean worked = dbm.updateDeckName(did, name);
        String response = "success";
        if(!worked){
            System.out.println("failed to change deck name");
            response = "failed";
        }
        ret = ok(response);
        return ret;
    }

    //Merge cards
    public Result getMergeCount(int iid1,int iid2){
        Result ret;
        int fid = dbm.getFid(iid1,iid2);
        int mergeCount = dbm.getMergeCount(fid);

        ObjectNode node = Json.newObject();
        node.put("MergeCount",mergeCount);

        ret = ok(node);
        return ret;
    }

    //decreasethemergecount
    public Result decreaseMergeCount(int iid1,int iid2){
        Result ret;
        int fid = dbm.getFid(iid1,iid2);
        boolean worked = dbm.updateSocialMergeCount(fid);

        ObjectNode node = Json.newObject();
        node.put("Worked?",worked);

        ret = ok(node);
        return ret;
    }






    //A helper for the merge bois. This function returns a random CardType that falls under the specified tier.
    public Result getRandomCardOfTier(int tier){
        return ok(Integer.toString(CardListVorschlag.getRandomCard(tier).typeID));
    }

    //##########################
    //    MAXI STUFF. I swear I'm not super possessive of my own code. I just like being able to find my stuff at a quick glance.
    //##########################

    private final Object pauseMutex = new Object();
    private boolean paused = true;
    private final Thread thready = new Thread(()->{
        while(true){
            System.out.println("OMEGALUL");
            try{
                System.out.println("\ttry");
                synchronized (pauseMutex){
                    System.out.println("\t\tinSync");
                    if(paused){
                        System.out.println("\t\t\tpaused");
                        synchronized (pauseMutex){
                            System.out.println("\t\t\t\tgoing into wait");
                            pauseMutex.wait();
                            System.out.println("\t\t\t\tcoming out of wait");
                        }
                    }
                }
                Thread.sleep(500);
            } catch (InterruptedException e){
                break;
            }
        }
    });


    private void initMatchmakingThread(){
        thready.start();
    }

    public Result pauseLUL(){
        paused = true;
        return ok();
    }
    public Result startLUL(){
        synchronized (pauseMutex){
            paused = false;
            pauseMutex.notifyAll();
        }
        return ok();
    }

    public Result enterMatchmaking(int playerID){
        System.out.println("Maxi: "+playerID+" entered matchmaking.");
        if(matchmakingPool.contains(playerID)) return badRequest();
        matchmakingPool.add(playerID);
        if(matchmakingPool.size() > 1){
            boolean isSecond = false;
            Integer first = null; //corporate, I mean the compiler, requires that this variable be initialized.
            ArrayList<Integer> removals = new ArrayList<>();
            for(Integer chosenPlayer : matchmakingPool){
                if(isSecond) instantiateLobby(first, chosenPlayer);
                else first = chosenPlayer;
                isSecond = !isSecond;
                removals.add(first);
                removals.add(chosenPlayer);
            }
            matchmakingPool.removeAll(removals);
        }
        return ok();
    }

    public Result cancelMatchmaking(int playerID){
        System.out.println("Maxi: "+playerID+" cancelled matchmaking.");
        matchmakingPool.remove(playerID);
        return ok();
    }
    public Result wasMatchFound(int playerID){
        if(!matchLobbyMap.containsKey(playerID)) return ok("no");
        else {
            MatchLobby lobby = matchLobbyMap.get(playerID);
            int opponentID = lobby.idPlayerOne == playerID ? lobby.idPlayerTwo : lobby.idPlayerOne;
            User opponent = dbm.getUser(opponentID);
            if(opponent == null){
                System.out.println("Opponent with unused ID was in matchmaking pool");
                return badRequest("opponent doesn't exist");
            }
            return ok(opponent.toJson());
        }
    }

    private void instantiateLobby(int idPlayerOne, int idPlayerTwo){
        closeGame(idPlayerOne, idPlayerTwo); System.out.println("Maxis TODO: end matches differently."); //TODO
        MatchLobby lobby;
        if(new Random().nextInt(2) == 0) lobby = new MatchLobby(idPlayerOne, idPlayerTwo); //THIS PART DECIDES WHO'S PLAYER ONE AND WHO'S LUIGI
        else lobby = new MatchLobby(idPlayerTwo, idPlayerOne);
        matchLobbyMap.put(idPlayerOne, lobby);
        matchLobbyMap.put(idPlayerTwo, lobby);
    }

    public Result selectDeck(int playerID, int deckID, int elementID){
        MatchLobby lobby = matchLobbyMap.get(playerID);
        if(lobby == null) return badRequest();
        lobby.setDeckAndWeather(playerID, deckID, elementID);
        matchLobbyMap.remove(playerID); //player doesn't need to set his deck twice.
        if(lobby.isReady()){
            instantiateGame(lobby.idPlayerOne, lobby.idPlayerTwo, lobby.deckPlayerOne, lobby.deckPlayerTwo, lobby.elementPlayerOne, lobby.elementPlayerTwo);
        }
        return ok();
    }
    public Result hasGameStarted(int playerID){
        return gameMap.containsKey(playerID) ? ok("yes") : ok("no");
    }

    private void instantiateGame(int idPlayerOne, int idPlayerTwo, int deckPlayerOne, int deckPlayerTwo, int elementPlayerOne, int elementPlayerTwo){
        Deck deckOne = dbm.getDeckById(deckPlayerOne);
        Deck deckTwo = dbm.getDeckById(deckPlayerTwo);
        GameInstance newGame = new GameInstance(deckOne, deckTwo, elementPlayerOne, elementPlayerTwo);
        if(!gameMap.put(idPlayerOne, idPlayerTwo, newGame)) System.out.println("Maxi: Game started but there was already an ongoing game. This should never happen!");
        roleMap.put(idPlayerOne, EPlayer.ONE);
        roleMap.put(idPlayerTwo, EPlayer.TWO);
    }

    private void closeGame(int idPlayerOne, int idPlayerTwo){
        gameMap.remove(idPlayerOne, idPlayerTwo);
        roleMap.remove(idPlayerOne);
        roleMap.remove(idPlayerTwo);
    }

    //InGame
    public Result getCardImages(int playerID){
        return ingameLaziness(playerID, (game, role)->game.getCardImages().toString());
    }
    public Result surrender(int playerID){
        return ingameLaziness(playerID, (game, role)->{
            game.surrender(role);
            return getBoardUpdate(game, role);
        });
    }
    public Result getFullBoard(int playerID){
        return ingameLaziness(playerID, this::getFullBoard);
    }
    public Result getBoardUpdate(int playerID){
        return ingameLaziness(playerID, this::getBoardUpdate);
    }
    public Result endTurn(int playerID){
        return ingameLaziness(playerID, (game, role)->{
            if(!game.endTurn(role)) return ("NotYourTurn");
            return getBoardUpdate(game, role);
        });
    }

    public Result getValidTargetsForCard(int playerID, int handCardID){
        return ingameLaziness(playerID, (game, role)->game.getValidTargets(role, handCardID).toJSON().toString());
    }

    public Result getValidTargetsForMonster(int playerID, int slotID){
        return ingameLaziness(playerID, (game, role)->game.getValidTargets(new BoardPosition(role, slotID)).toJSON().toString());
    }

    public Result placeCard(int playerID, int handCardID, boolean onEnemySide, int slotID){
        return ingameLaziness(playerID, (game, role)->{
            EPlayer side = onEnemySide ? role.other() : role;
            if(!game.placeCard(role, handCardID, new BoardPosition(side, slotID))) return ("InvalidMove");
            return getBoardUpdate(game, role);
        });
    }

    public Result placeCardOnFace(int playerID, int handCardID, boolean onEnemyFace){
        return ingameLaziness(playerID, (game, role)->{
            EPlayer face = onEnemyFace? role.other() : role;
            if(!game.placeCardOnFace(role, handCardID, face)) return ("InvalidMove");
            return getBoardUpdate(game, role);
        });
    }

    public Result attack(int playerID, int slotIDAttacker, boolean onEnemySide, int targetSlotID){
        return ingameLaziness(playerID, (game, role)->{
            EPlayer side = onEnemySide ? role.other() : role;
            BoardPosition attackerPos = new BoardPosition(role, slotIDAttacker);
            BoardPosition targetPos = new BoardPosition(side, targetSlotID);
            if(!game.attack(attackerPos, targetPos)) {
                System.out.println("INVALID MOVE!!");
                return ("InvalidMove");
            }
            return getBoardUpdate(game, role);
        });
    }

    public Result attackOnFace(int playerID, int slotIDAttacker, boolean onEnemyFace){
        return ingameLaziness(playerID, (game, role)->{
            EPlayer face = onEnemyFace ? role.other() : role;
            BoardPosition attackerPos = new BoardPosition(role, slotIDAttacker);
            if(!game.attackFace(attackerPos, face)) return ("InvalidMove");
            return getBoardUpdate(game, role);
        });
    }

    private Result ingameLaziness(int playerID, BiFunction<GameInstance, EPlayer, String> whatToReturn){
        //Error handling
        GameInstance game = gameMap.get(playerID);
        if(game == null) return badRequest("PlayerNotInGame");
        EPlayer role = roleMap.get(playerID);
        if(role == null) return badRequest("PlayerNotInGame"); //This should never happen
        //actual stuff
        if(whatToReturn == null) return ok();
        else return ok(String.valueOf(whatToReturn.apply(game, role)));
    }
    private String getBoardUpdate(GameInstance game, EPlayer player){
        EPlayer victor = game.getWinnerOrNull();
        if(victor != null){
            return victor == player? "Victory" : "Defeat";
        }
        PlayerBoardUpdate pbu = game.getUpdate(player);
        return pbu.toJSON().toString();
    }
    private String getFullBoard(GameInstance game, EPlayer player){
        EPlayer victor = game.getWinnerOrNull();
        if(victor != null){
            return victor == player? "Victory" : "Defeat";
        }
        PlayerBoardUpdate pbu = game.getFullBoard(player);
        return pbu.toJSON().toString();
    }

    //helper classes
    private static class MapDuo<K, V>{ //honestly I could have just used a standard map here but I really like adding classes :shrug:
        private HashMap<K,V> map = new HashMap<>();
        V get(K key){
            return map.get(key);
        }
        //returns whether both keys were available
        boolean put(K keyOne, K keyTwo, V value){
            if(map.get(keyOne) != null || map.get(keyTwo) != null) return false;
            map.put(keyOne, value);
            map.put(keyTwo, value);
            return true;
        }
        boolean remove(K keyOne, K keyTwo){
            boolean result = (map.get(keyOne) != null && map.get(keyTwo) != null);
            map.remove(keyOne);
            map.remove(keyTwo);
            return result;
        }
        boolean containsKey(int playerID){
            return map.containsKey(playerID);
        }
    }

    private static class MatchLobby {
        final int idPlayerOne;
        final int idPlayerTwo;
        Integer deckPlayerOne = null;
        Integer deckPlayerTwo = null;
        int elementPlayerOne = 0;
        int elementPlayerTwo = 0;
        MatchLobby(int idPlayerOne, int idPlayerTwo){
            this.idPlayerOne = idPlayerOne;
            this.idPlayerTwo = idPlayerTwo;
        }
        boolean isReady(){
            return deckPlayerOne != null && deckPlayerTwo != null;
        }
        void setDeckAndWeather(int playerID, int deckID, int elementID){
            if(playerID == idPlayerOne) {
                if(deckPlayerOne != null) System.out.println("Maxi: Player "+playerID+" re-chose their deck. This shouldn't be possible. Maxi fucked up.");
                deckPlayerOne = deckID;
                elementPlayerOne = elementID;
            }
            else if(playerID == idPlayerTwo){
                if(deckPlayerTwo != null) System.out.println("Maxi: Player "+playerID+" re-chose their deck. This shouldn't be possible. Maxi fucked up.");
                deckPlayerTwo = deckID;
                elementPlayerTwo = elementID;
            }
            else System.out.println("Maxi: player "+playerID+" tried to set a deck in the wrong lobby. This is 100% a server bug. Maxi's fault.");
        }
    }

    //##########################
    //    END MAXI STUFF
    //##########################
}
