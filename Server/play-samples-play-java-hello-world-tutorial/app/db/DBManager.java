package db;

import java.security.SecureRandom;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;

import container.DeckSkeleton;
import container.Card;
import container.Deck;
import container.Friend;
import container.User;
import container.FriendFullInfo;
import static InGame.CardType.CardTypes.*;

/*

 * @author Simon Stolz
 * 
 * https://www.tutorialspoint.com/sqlite/sqlite_java.htmhttps://www.tutorialspoint.com/sqlite/sqlite_java.htm
 * Partially based on a Legacy Project of mine: 
 * https://github.com/CarnifexRegis/TanzschulApp
 * 
 * Manages SQL  queries and Tables of the Database
 * 
 */
public class DBManager {
	private TableUser user = new TableUser();
	private TableCard card = new TableCard();
	private TablePicture picture = new TablePicture();
	private TableFriends friends = new TableFriends();
	private TableDeck deck = new TableDeck();
	private TableDeckLinker decklinker = new TableDeckLinker();
	private TableMatchHistory matchHistory = new TableMatchHistory();
	private TablePack pack = new TablePack();
	private int deckCapacity = 15;
	// private TableDeckLinker deckLinker = new TableDeckLinker();
	Connection c;
	SecureRandom random = new SecureRandom();

	public DBManager() {
		if (picture.create() && user.create() && card.create() && friends.create() && deck.create()
				&& matchHistory.create() && decklinker.create()&&pack.create()) {
			try {
				Class.forName("org.sqlite.JDBC");
				c = null;
				c = DriverManager.getConnection("jdbc:sqlite:serverDatabase.db");
				c.setAutoCommit(false);
				System.out.println("DBManager established connection to database");
			} catch (Exception e) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "while establishing connection in DBManager");
				System.exit(0);
			}
		} else {
			System.out.println(
					"Table Manager did not try to establish a connetion to the database due an error while creating new tables.");
		}
	}

	public int getDeckCapacity() {
		return deckCapacity;
	}

	public void setDeckCapacity(int deckCapacity) {
		this.deckCapacity = deckCapacity;
	}

	// INSERT
	// inserts a new picture and returns its unique integer id
	public int insertPicture(String picture, int owner) {
		try {
			String query = "INSERT INTO Picture (picture, owner) VALUES (?, ?)";
			PreparedStatement p = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			p.setString(1, picture);
			p.setInt(2, owner);
			p.executeUpdate();
			ResultSet rs = p.getGeneratedKeys();
			if (p != null)
				p.close();
			c.commit();
			int id = rs.getInt(1);
			rs.close();
			return id;
		} catch (Exception e) {
			printError(e, "insertPicture(...)");
			return -1;
		}
	}
	// inserts a new element into the Pack table
	public boolean insertPack (int type, int owner) {
		try {
			String query = "INSERT INTO pack (owner, type) VALUES (?, ?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, owner);
			p.setInt(2, type);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertPack(...)");
			return false;
		}
	}
	// inserts a new Deck element into deck table
	public boolean insertDeck(int owner, String name) {
		try {
			String query = "INSERT INTO Deck (owner, name) VALUES (?, ?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, owner);
			p.setString(2, name);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertDeck(...)");
			return false;
		}
	}
	// inserts a link between a card and a deck Element
	public boolean insertDeckLinker(int did, int cid, int owner) {
		try {
			String query = "INSERT INTO DeckLinker (did,cid,owner) VALUES (?, ?, ?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, did);
			p.setInt(2, cid);
			p.setInt(3, owner);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertDeckLinker(...)");
			return false;
		}
	}
	// not used
	public boolean insertMatch(int iid1, int iid2) {
		try {
			String query = "INSERT INTO MatchHistory (iid1, iid2) VALUES (?, ?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, iid1);
			p.setInt(2, iid2);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertMatch(...)");
			return false;
		}
	}
	// inserts a new Card element into Card table
	public boolean insertCard(int iid, int type) {
		try {

			String query = "INSERT INTO CARD (iid,type) VALUES (?,?)";
			PreparedStatement p = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, iid);
			p.setInt(2, type);
			p.executeUpdate();
			ResultSet rs = p.getGeneratedKeys();
			if (p != null)
				p.close();
			c.commit();
			int id = rs.getInt(1);
			if(rs!=null)
				rs.close();
			return true;
		} catch (Exception e) {
			printError(e, "insertCard(...)");
			return false;
		}
	}

	// Inserts a new user element into user table. User has to be registered via firebase first
	public boolean insertUser(String fid, String name, String mail) {
		try {
			String query = "INSERT INTO User (fid,mail,name) VALUES (?,?,?)";
			PreparedStatement p = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			p.setString(1, fid);
			p.setString(2, mail);
			p.setString(3, name);
			p.executeUpdate();
			ResultSet rs = p.getGeneratedKeys();
			if (p != null)
				p.close();
			c.commit();
			int id = rs.getInt(1);
			rs.close();
			if (p != null)
				p.close();
			c.commit();
			for(int i = 1;i<=5;i++) {
				insertDeck(id, "Deck "+i);
			}
			
			return true;
		} catch (Exception e) {
			printError(e, "insertUser(...)");
			return false;
		}
	}

	// 1 friend addesd successfully
	// 0 Friend already exists
	// -1 error adding Friend
	public int insertFriend(int iid1, int iid2) {
		try {
			String query1 = "SELECT COUNT (*) AS I FROM FRIENDS WHERE iid1 = " + iid1 + " AND iid2 = " + iid2 + ";";
			String query3 = "SELECT COUNT (*) AS I FROM FRIENDS WHERE iid1 = " + iid2 + " AND iid2 = " + iid1 + ";";
			String query4 = "SELECT COUNT (*) AS I FROM FRIENDS WHERE iid1 = " + iid2 + " AND iid2 = " + iid1 + " AND accepted = 1;";
			Statement stmt = c.createStatement();
			int count = 0;
			ResultSet rs = stmt.executeQuery(query1);
			count +=rs.getInt("I");
			Statement stmt2 = c.createStatement();
			ResultSet rs2 = stmt.executeQuery(query3);

			if(rs2.getInt("I")>0) {
				stmt.close();
				rs.close();
				rs2.close();
				stmt2.close();
				Statement stmt4 = c.createStatement();
				ResultSet rs4 = stmt4.executeQuery(query4);
				if(!(rs4.getInt("I")>0)) {
					stmt4.close();
					rs4.close();
					System.out.println("Accepting friend");
					updateAcceptFriend(iid2, iid1);
					return 1;
				}else {
					System.out.println("Friend already accepted");
					return 0;
				}

			}else {
				System.out.println("No requests from this Friend");
			}
			stmt.close();
			rs.close();
			rs2.close();
			stmt2.close();
			if (count > 0) {
				System.out.println("Friend already exists");

				return 0;
			} else {
				System.out.println("inserting friend");
				try {
					String query2 = "INSERT INTO Friends (iid1,iid2) VALUES (?, ?)";
					PreparedStatement p = c.prepareStatement(query2);
					p.setInt(1, iid1);
					p.setInt(2, iid2);
					p.executeUpdate();
					c.commit();
					if (p != null)
						p.close();

					return 1;
				} catch (Exception e) {
					printError(e, "insertFriend(...)");
					return -1;
				}
			}
		} catch (Exception e) {
			printError(e, "insertFriend(...)");
			return -1;
		}
	}

	// type = 0 for matchrequest type = 1 for traderequest
	public boolean updateSendRequest(int requestingUser, int fid, int type) {
		try {
			PreparedStatement p;
			String t = null;
			if (type == 1) {
				t = "traderequest";
			}
			if (type == 0) {
				t = "matchrequest";
			}
			if (t == null) {
				System.out.println("No viable rquest type");
				return false;
			}
			String query = "UPDATE Friends SET " + t + " = CASE WHEN iid1 = ? THEN 1 ELSE 2 END WHERE fid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, requestingUser);
			p.setInt(2, fid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSendRequest(...)");
			return false;
		}
	}

	// type 1 =traderequests type 0 = matchrequest
	public boolean updateAcceptRequest(int acceptingUser, int type, int fid) {
		try {
			PreparedStatement p;
			String t = null;
			if (type == 1) {
				t = "traderequest";
			}
			if (type == 0) {
				t = "matchrequest";
			}
			if (t == null) {
				System.out.println("No viable rquest type");
				return false;
			}
			String query = "UPDATE Friends SET " + t + " = CASE WHEN iid1 = ? THEN 3 ELSE 4 END WHERE fid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, acceptingUser);
			p.setInt(2, fid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateAcceptRequest(...)");
			return false;
		}
	}

	// type 1 =traderequests type 0 = matchrequest
	public boolean updateResetRequest(int type, int fid) {
		try {
			PreparedStatement p;
			String t = null;
			if (type == 1) {
				t = "traderequest";
			}
			if (type == 0) {
				t = "matchrequest";
			}
			if (t == null) {
				System.out.println("No viable rquest type");
				return false;
			}
			String query = "UPDATE Friends SET " + t + " = 0 WHERE fid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, fid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateResetRequest(...)");
			return false;
		}
	}

	// adds parameter amount to social attributes
	public boolean updateSocialFriend(int fid, int tradecount, int temptradecount, int rematchcount,int temprematchcount, int questcount,int date,int merge) {
		boolean check = true;

		try {
			PreparedStatement p;
			String query = "UPDATE Friends SET " + "tradecount" + " = ?," + "rematchcount"
					+ " = ?,"+ "questscore"
					+ " = ?,"+ "temprematchcount"
					+ " = ?,"+ "temptradecount"
					+ " = ?,"+ "date"
					+ " = ?,"+ "mergeCount"
			        + " = ? WHERE fid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, tradecount);
			p.setInt(2, rematchcount);
			p.setInt(3, questcount);
			p.setInt(4, temprematchcount);
			p.setInt(5, temptradecount);
			p.setInt(6, date);
			p.setInt(7,merge);
			p.setInt(8, fid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSocialFriendHelper(...)");
			return false;
		}
	}
	// gets the amount of performed trades
	public int getTradeCount(int fid) {
		try {
			String query = "SELECT tradecount FROM Friends WHERE fid = " + fid + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int result = rs.getInt("tradecount");
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			return result;
		} catch (Exception e) {
			printError(e, "getTradeCount(...)");
			return -1;
		}
	}

	//getting matchcount out of storage
	public int getMatchCount(int fid) {
		try {
			String query = "SELECT rematchcount FROM Friends WHERE fid = " + fid + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int result = rs.getInt("rematchcount");
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMatchCount(...)");
			return -1;
		}
	}
	//
	public int getMergeCount(int fid) {
		try {
			String query = "SELECT mergeCount FROM Friends WHERE fid = " + fid + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int result = rs.getInt("mergeCount");
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMergeCount(...)");
			return -1;
		}
	}

	// increments the tradecount
	// optimiseable reading the tradecount first is not needed
	public boolean updateSocialTradeCount(int fid) {
		int getTradeCount = getTradeCount(fid);
		if(getTradeCount!=-1) {
			try {
				PreparedStatement p;
				String query = "UPDATE Friends SET  tradecount = ? WHERE fid = ?;";
				p = c.prepareStatement( query );
				p.setInt( 1, getTradeCount+1 );
				p.setInt( 2, fid );
				p.executeUpdate();
				if (p != null)
					p.close();
				c.commit();
				return true;
			} catch (Exception e) {
				printError( e, "updateSocialTradecount(...)" );
				return false;
			}
		} else{
			return false;
		}
	}

	//update the matchcount
	public boolean updateSocialMatchCount(int fid) {
		int getMatchCount = getMatchCount(fid);
		if(getMatchCount!=-1) {
			try {
				PreparedStatement p;
				String query = "UPDATE Friends SET  rematchcount = ? WHERE fid = ?;";
				p = c.prepareStatement( query );
				p.setInt( 1, getMatchCount+1 );
				p.setInt( 2, fid );
				p.executeUpdate();
				if (p != null)
					p.close();
				c.commit();
				return true;
			} catch (Exception e) {
				printError( e, "updateSocialMatchcount(...)" );
				return false;
			}
		} else{
			return false;
		}
	}
	//
	public boolean updateSocialMergeCount(int fid) {
		int getMergeCount = getMergeCount(fid);
		if(getMergeCount!=-1) {
			try {
				PreparedStatement p;
				String query = "UPDATE Friends SET  mergeCount = ? WHERE fid = ?;";
				p = c.prepareStatement( query );
				p.setInt( 1, getMergeCount-1 );
				p.setInt( 2, fid );
				p.executeUpdate();
				if (p != null)
					p.close();
				c.commit();
				return true;
			} catch (Exception e) {
				printError( e, "updateSocialTradecount(...)" );
				return false;
			}
		} else{
			return false;
		}
	}

	// obsolete caused to lock db
	public boolean updateSocialFriendHelper(int fid, String column, int value) {
		try {
			PreparedStatement p;
			String query = "UPDATE Friends SET " + column + " = ? WHERE fid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, value);
			p.setInt(2, fid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSocialFriendHelper(...)");
			return false;
		}

	}

	// resets the value of all ShortTermQuests
	// TODO might cause db lock put it in one Method
	public boolean updateResetTempSocialAll() {
		boolean result = true;
		result &= updateResetSocialHelper("questscore");
		result &= updateResetSocialHelper("temprematchcount");
		result &= updateResetSocialHelper("temptradecount");
		result &= updateResetSocialHelper("mergeCount");
		return result;
	}

	public boolean updateResetSocialHelper(String column) {
		try {
			Statement stmt;
			String query = "UPDATE Friends SET " + column + " = 0;";
			stmt = c.createStatement();
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateResetSocialHelper(...)");
			return false;
		}
	}

	// changes the owner of a certain card used for trading
	public boolean updateCardOwner(int cid, int owner) {
		try {
			PreparedStatement p;
			String query = "UPDATE Card SET iid = ? WHERE cid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, owner);
			p.setInt(2, cid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateCardOwner(...)");
			return false;
		}
	}
	// changes the card ower and sets the cards type
	public boolean updateCardOwnerAndUpdateCard(int cid, int owner,int type) {
		try {
			PreparedStatement p;
			String query = "UPDATE Card SET iid = ?, type = ? WHERE cid = ?;";
			p = c.prepareStatement(query);
			p.setInt(1, owner);
			p.setInt(2,type);
			p.setInt(3, cid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateCardOwnerAndUpdateCard(...)");
			return false;
		}
	}

	// updates the firebase picture url in the picture table of a certain picture
	// object
	public boolean updatePicture(int pid, String picture) {
		try {
			PreparedStatement p;
			String query = "UPDATE Picture SET picture = ? WHERE pid = ?;";
			p = c.prepareStatement(query);
			p.setString(1, picture);
			p.setInt(2, pid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updatePicture(...)");
			return false;
		}
	}

	// accepts a specific Friendrequest
	public boolean updateAcceptFriend(int friendid, int myid) {
		try {
			PreparedStatement p;
			Date date = new Date();
			//month +1 to get our months
			int monthATM = date.getMonth();
			String query = "UPDATE Friends SET accepted = 1, date = "+monthATM+" WHERE iid1 = ? AND iid2 = ?";
			p = c.prepareStatement(query);
			p.setInt(1, friendid);
			p.setInt(2, myid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateAcceptFriend(...)");
			return false;
		}
	}

	// updates cards pciture reference
	public boolean updatePidCard(int pid, int cid) {
		try {
			PreparedStatement p;
			String query = "UPDATE Card SET pid = ? WHERE cid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, pid);
			p.setInt(2, cid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updatePidCard(...)");
			return false;
		}
	}

	// updates users picture refernce
	public boolean updatePidUser(int pid, int iid) {
		try {
			PreparedStatement p;
			String query = "UPDATE User SET pid = ? WHERE iid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, pid);
			p.setInt(2, iid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updatePidUser(...)");
			return false;
		}
	}

	// adds new steps to step count
	public boolean updateSteps(int steps, int iid) {
		try {
			Statement stmt;
			String query = "UPDATE User SET steps = steps + "+steps+" WHERE iid = "+iid;
			stmt = c.createStatement();
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSteps(...)");
			return false;
		}
	}
	// Swaps two cards from a deck
	// Obsolete not used
	public boolean updateDeckSwap(int owner, int did, int newCard, int oldCard) {

		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM DeckLinker WHERE (did =" + did + " AND cid = " + oldCard + ") ;";
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();

		} catch (Exception e) {
			printError(e, "updateDeckSwap(...)");
			return false;
		}
		return updateDeckAdd(owner, did, newCard);
	}

	// inserts a new card into a deck
    public boolean updateDeckAdd(int owner, int did, int newCard) {
        int decksize = 0;
        int exists = 0;
        try {

            String query = "SELECT COUNT(*) AS decksize FROM DeckLinker WHERE did = ?;";
            PreparedStatement p = c.prepareStatement(query);
            p.setInt(1, did);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                decksize = rs.getInt("decksize");
                if (rs != null)
                    rs.close();
                if (p != null)
                    p.close();

                if(decksize>=15){
                    System.out.println("Deck: "+did+" contains more cards than maximum value");
                    return false;
                }else{
                    System.out.println("Deck: "+did+" space for more cardss");
                }
            } else {
                if (rs != null)
                    rs.close();
                if (p != null)
                    p.close();

                System.out.println("Error in updateDeckAdd()");
                return false;
            }


        } catch (Exception e) {
            printError(e, "updateDeckAdd(...)");
            return false;
        }
        try {
            String query2 = "SELECT COUNT(*) AS ex FROM DeckLinker WHERE did = ? AND cid = ?;";
            PreparedStatement p2 = c.prepareStatement(query2);
            p2.setInt(1, did);
            p2.setInt(2, newCard);
            ResultSet rs2 = p2.executeQuery();
            if (rs2.next()) {
                exists = rs2.getInt("ex");
            } else {
                System.out.println("Error in updateDeckAdd()");
                return false;
            }
            if (rs2 != null)
                rs2.close();
            if (p2 != null)
                p2.close();
        } catch (Exception e) {
            printError(e, "updateDeckAdd(...)");
            return false;
        }
        // checks if max decksize is not reached and if card doesnt exist in requested deck yet
        if (decksize < deckCapacity && exists == 0) {
            System.out.println("Added  Card in updateDeckAdd()");
            return insertDeckLinker(did, newCard, owner);
        } else {
            System.out.println("Cant add Card in updateDeckAdd()");
            return false;
        }
    }

	// Changes name of a certain deck (optional)
	public boolean updateDeckName(int did, String name) {
		try {
			PreparedStatement p;
			String query = "UPDATE Deck SET name = ? WHERE did = ?";
			p = c.prepareStatement(query);
			p.setString(1, name);
			p.setInt(2, did);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateDeckName(...)");
			return false;
		}
	}

	// This method adds a positive or negative amount of money to a certain user,
	// this should only used for actions intiated by the server
	// Obsolete most likely  not used
	public boolean updateUserMoney(int iid, int amount) {
		try {
			PreparedStatement p;
			String query = "UPDATE User SET money = money + ?  WHERE iid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, amount);
			p.setInt(2, iid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateUserMoney(...)");
			return false;
		}
	}

	// -1 error 0 no money 1 successfull (2 Internet ausverkauft)
	// purchaser = Person acting in the payment
	// profiting person who gets the purchased
	// not used
	public int updatePurchase(int obj, int purchaser, int profiting) {
		return 0;
	}


	// deletes friend
	public boolean deleteFriend(int iid1, int iid2) {
		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM Friends WHERE (iid1 =" + iid1 + " AND iid2 = " + iid2 + ") OR (iid2 =" + iid1
					+ " AND iid1 = " + iid2 + ") ;";
			stmt.executeUpdate(query);
			c.commit();
			if (stmt != null)
				stmt.close();
			return true;
		} catch (Exception e) {
			printError(e, "deleteFriend(...)");
			return false;
		}
	}

	// deletes deck
	public boolean deleteDeck(int did) {
		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM DECK WHERE (did =" + did + ") ;";
			stmt.executeUpdate(query);
			c.commit();
			if (stmt != null)
				stmt.close();
			return true;
		} catch (Exception e) {
			printError(e, "deleteDeck(...)");
			return false;
		}
	}
	//delete all Packs of a ceretain user
	public boolean deletePackByOwner(int owner) {
		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM Pack WHERE (owner =" + owner + ") ;";
			stmt.executeUpdate(query);
			c.commit();
			if (stmt != null)
				stmt.close();
			return true;
		} catch (Exception e) {
			printError(e, "deletePackByOwner(...)");
			return false;
		}
	}
	// opens all packs owned by one user and deletes the packs from pck table after
	// obsolete
	public ArrayList<Integer> deleteAndOpenPackByOwner(int owner) {
		ArrayList<Integer> packs = getPacks(owner);
		ArrayList<Integer> cards= openPacksByOwner(packs, owner);
		deletePackByOwner(owner);
		return cards;
	}
	// creates new cards for opened packs
	// obsolete
	public ArrayList<Integer> openPacksByOwner(ArrayList<Integer> type, int owner) {
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for(int i = 0; i <type.size(); i++) {
			Booster b = new Booster(type.get(i));
			int[] temp = b.open();
			for(int c = 0; c < temp.length;c++) {
				insertCard(owner, temp[c]);
				cards.add(temp[c]);
			}
		}
		return cards;
	}
	// SELECT
	// gets all friends and other ppls friend requests
	public ArrayList<FriendFullInfo> getFriends(int reqid) {
		ArrayList<FriendFullInfo> friends = new ArrayList<FriendFullInfo>();
		try {
			String query = "SELECT   u.name, fu.iid, p.picture, fu.accepted, fu.fid  FROM User u "
					+ "INNER JOIN (SELECT fid, accepted, iid2 AS iid FROM Friends WHERE iid1 = " + reqid
					+ " AND accepted > 0 UNION SELECT fid, accepted, iid1 AS iid FROM Friends WHERE iid2 = " + reqid + ") fu "
					+ "ON u.iid = fu.iid " + "LEFT JOIN Picture p" + " ON p.pid = u.pid ORDER BY u.name DESC;";
			// http://helpdesk.objects.com.au/java/how-do-i-convert-a-jdbc-blob-to-a-string
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				User user = getUser(rs.getInt("iid"));
				int winrate;
				if(user.getWins()==0&&user.getLosses()==0){
					winrate = 50;
				} else{
					winrate = (int)(100.0*user.getWins()/(user.getWins()+user.getLosses()));
					System.out.println(winrate);
				}
				friends.add(new FriendFullInfo(rs.getInt("fid"),rs.getInt("iid"), rs.getInt("accepted"),
						rs.getString("name"), rs.getString("picture"),user.getSteps(),winrate));
				System.out.println("Friends: "+rs.getInt("fid")+rs.getInt("iid")+ rs.getInt("accepted")
						+ rs.getString("name")+ rs.getString("picture")+user.getSteps()+winrate);
			}
		} catch (Exception e) {
			printError(e, "getFriends(...)");
		}
		return friends;
	}



	public boolean incrementWin(int iid){
		try {
			Statement stmt;
			String query = "UPDATE User SET wins = wins + 1 WHERE iid = "+iid;
			stmt = c.createStatement();
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateWins(...)");
			return false;
		}
	}

	public boolean incrementLose(int iid){
		try {
			Statement stmt;
			String query = "UPDATE User SET losses = losses + 1 WHERE iid = "+iid;
			stmt = c.createStatement();
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateLoses(...)");
			return false;
		}
	}

	// returns a list of all pack types owned by a certain user
	public ArrayList<Integer> getPacks (int owner){
		try {
			String query = "SELECT type, pkid FROM Pack WHERE owner = "+owner+"; ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				System.out.println("pack: " + rs.getInt("type")+"  " +owner);
				result.add((Integer) rs.getInt("type"));
			//	result.add((Integer) rs.getInt("pkid"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getPacks(...)");
			return null;
		}
	}
	// removes one pack by type and owner id if it exists returns newly created cards or null if no such pack exists
	public ArrayList<Integer> removePack(int type , int owner){
		int pkid = -1;
		ArrayList<Integer> cards = new ArrayList<Integer>();
		try {
			String query = "SELECT pkid FROM Pack WHERE type = " + type + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()) {
				pkid = rs.getInt("pkid");

			}else {
				System.out.println("no such pack exists");
				return null;
			}
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			Booster b = new Booster(type);
			int[] temp = b.open();
			deletePackById(pkid);
			for(int c = 0; c < temp.length;c++) {
				insertCard(owner, temp[c]);
				cards.add(temp[c]);
			}
			
			return cards;
		} catch (Exception e) {
			printError(e, "removePack(...)");
			return null;
		}
		
	}
	public boolean deletePackById(int pkid) {
		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM Pack WHERE (pkid =" + pkid + ") ;";
			stmt.executeUpdate(query);
			c.commit();
			if (stmt != null)
				stmt.close();
			return true;
		} catch (Exception e) {
			printError(e, "deletePackByID(...)");
			return false;
		}
	}
	// returns the friendship id of 2 specified users
	public int getFid(int iid1, int iid2){
		try {
			String query = "SELECT fid FROM Friends WHERE iid1 = "+iid1+" AND iid2 = "+iid2+" UNION  SELECT fid FROM Friends WHERE iid1 = "+iid2+" AND iid2 = "+iid1+"; ";
			PreparedStatement p = c.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			int fid = -1;
			if(rs.next()) {
				fid =rs.getInt("fid");
			}
			System.out.println("FID: "+fid);
			if(rs!=null)rs.close();
			if(p != null)p.close();
			return fid;
		}catch (Exception e){
			printError(e, "getFid(...)");
			return -1;
		}
	}

	// get your step count
	public int getSteps(int iid) {
		try {
			String query = "SELECT steps FROM User WHERE iid = " + iid + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int result = rs.getInt("steps");
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			return result;
		} catch (Exception e) {
			printError(e, "getSteps(...)");
			return -1;
		}
	}


	// match id // player id
	// for match history in the future
	public ArrayList<Integer> getMyMatches() {
		return null;
	}
	// here you can obtain all relevant information about a certain user
	public User getUser(int id) {
		try {
			String query = "SELECT  u.iid, u.steps, u.name ,u.wins, u.losses, p.picture FROM User u  LEFT JOIN Picture p ON u.pid = p.pid WHERE u.iid = ?";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, id);
			ResultSet rs = p.executeQuery();
			User u = null;
			if (rs.next()) {
				System.out.println("" + rs.getInt("iid") + rs.getInt("steps") + rs.getString("name") + rs.getInt("wins")
						+ rs.getInt("losses") + rs.getString("picture"));
				u = new User(rs.getInt("iid"), rs.getInt("steps"), rs.getString("name"), rs.getInt("wins"),
						rs.getInt("losses"), rs.getString("picture"));
			} else {
				System.out.println("User not found ");
			}
			if (rs != null)
				rs.close();
			if (p != null)
				p.close();
			return u;
		} catch (Exception e) {
			printError(e, "getUser(...)");
			return null;
		}

	}
	// checks if there exist requests to battle from another user
	public ArrayList<Integer> getMatchRequests(int iid) {
		try {
			String query = "SELECT iid1 AS iid FROM Friends WHERE iid2 =" + iid
					+ " AND matchrequest = 1 UNION SELECT iid2 AS iid FROM Friends WHERE iid1 =" + iid
					+ " AND matchrequest = 2;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				System.out.println("mreq: " + rs.getInt("iid"));
				result.add((Integer) rs.getInt("iid"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMatchRequests(...)");
			return null;
		}
	}
	// gets a list of all match requests u sent that where accepted
	public ArrayList<Integer> getAcceptedMatchRequests(int iid) {
		try {
			String query = "SELECT iid1 AS iid FROM Friends WHERE iid2 =" + iid
					+ " AND matchrequest = 3 UNION SELECT iid2 AS iid FROM Friends WHERE iid1 =" + iid
					+ " AND matchrequest = 4;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				System.out.println("mreq: " + rs.getInt("iid"));
				result.add((Integer) rs.getInt("iid"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMatchRequests(...)");
			return null;
		}
	}

	public ArrayList<Integer> getTradeRequests(int iid) {
		try {
			String query = "SELECT iid1 AS iid FROM Friends WHERE iid2 =" + iid
					+ " AND traderequest = 1 UNION SELECT iid2 AS iid FROM Friends WHERE iid1 =" + iid
					+ " AND traderequest = 2;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				System.out.println("treq: " + rs.getInt("iid"));
				result.add((Integer) rs.getInt("iid"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getTradeRequests(...)");
			return null;
		}
	}
	public ArrayList<Integer> getAcceptedTradeRequests(int iid) {
		try {
			String query = "SELECT iid1 AS iid FROM Friends WHERE iid2 =" + iid
					+ " AND traderequest = 3 UNION SELECT iid2 AS iid FROM Friends WHERE iid1 =" + iid
					+ " AND traderequest = 4;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				System.out.println("treq: " + rs.getInt("iid"));
				result.add((Integer) rs.getInt("iid"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getTradeRequests(...)");
			return null;
		}
	}

	// returns the deck ids owned by a certain user id
	public ArrayList<Integer> getMyDecks(int myid) {
		try {
			String query = "SELECT did FROM Deck WHERE owner =" + myid + " ;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (rs.next()) {
				result.add((Integer) rs.getInt("did"));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMyDecks(...)");
			return null;
		}

	}

	public Deck getDeckById(int did) {
		try {
			String query = "SELECT Card.cid, type,picture FROM Card INNER JOIN DeckLinker ON Card.cid = DeckLinker.cid LEFT JOIN Picture ON Picture.pid = Card.pid WHERE DeckLinker.did = "
					+ did + " ;";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Card> cards = new ArrayList<Card>();
			while (rs.next()) {
				cards.add(new Card(rs.getInt("cid"), rs.getInt("type"), rs.getString("picture")));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return new Deck(did, cards);
		} catch (Exception e) {
			printError(e, "getDeckById(...)");
			return null;
		}
	}

	// recives users firebase id and returns internal id
	public int getMyID(String fid) {
		try {
			String query = "SELECT iid FROM User where fid = ?";
			PreparedStatement p = c.prepareStatement(query);
			p.setString(1, fid);
			ResultSet rs = p.executeQuery();
			int id = -1;
			if (rs.next()) {
				id = rs.getInt("iid");
			}
			System.out.println(id);
			if (rs != null)
				rs.close();
			if (p != null)
				p.close();
			return id;
		} catch (Exception e) {
			printError(e, "getMyID(...)");
			return -1;
		}

	}

	// Returns Social Scores in an Array of 3 in the order tradecount, rematchcount,
	// questscore
	public int[] getSocialScores(int fid) {
		try {
			String query = "SELECT iid1,iid2,tradecount,rematchcount,questscore,temptradecount,temprematchcount,date FROM Friends where fid = ?";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, fid);
			ResultSet rs = p.executeQuery();
			if (rs.next()) {
				System.out.println("Social: " + rs.getInt("tradecount") + ", " + rs.getInt("temptradecount") + ", "
						+ rs.getInt("rematchcount") + ", " + rs.getInt("temprematchcount") + ", "
						+ rs.getInt("questscore"));
				int[] result = { rs.getInt("tradecount"), rs.getInt("temptradecount"), rs.getInt("rematchcount"),
						rs.getInt("temprematchcount"), rs.getInt("questscore"),
						rs.getInt("date") ,rs.getInt("iid1"),rs.getInt("iid2")};
				if (rs != null)
					rs.close();
				if (p != null)
					p.close();
				return result;
			} else {
				if (rs != null)
					rs.close();
				if (p != null)
					p.close();
				return null;
			}
		} catch (Exception e) {
			printError(e, "getSocialScores(...)");
			return null;
		}
	}

	public void printError(Exception e, String msg) {
		System.err.println(e.getClass().getName() + ": " + e.getMessage() + msg);
		// if (c != null)c.close();
		// System.exit(0);
		e.printStackTrace();

	}
	// gets all cards owned by one user
	public ArrayList<Card>  getMyCards(int owner) {
		try {
			String query = "SELECT c.cid, c.type, p.picture FROM Card c LEFT JOIN Picture p ON c.pid = p.pid WHERE c.iid = "
					+ owner + " ;";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Card> cards = new ArrayList<>();
			while (rs.next()) {
			    cards.add(new Card(rs.getInt("cid"), rs.getInt("type"), rs.getString("picture")));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return cards;
		} catch (Exception e) {
			printError(e, "getMyCards(...)");
			return null;
		}
	}

	// basic stuff owned by creation of the user
	public boolean insertCardsStandard(int owner) {
		int[] types = {BAT.typeID,BEE.typeID,ANGEL.typeID,DRACULA.typeID,ARCHANGEL.typeID,PHOENIX.typeID,MEDUSA.typeID,HALO.typeID,SPIRIT_OF_THE_SPRING.typeID,WILL_O_WISP.typeID,SHARK.typeID,GOLEM.typeID,LAZY_FROG.typeID,ELECTRON.typeID,OVERGROWN.typeID,FOREST_FIRE.typeID,THE_FOOL.typeID};
		boolean success = true;
		for(int i = 0; i<types.length;i++) {
			success &=insertCard(owner,types[i]);
		}
		return success;

	}
	// deletes a card with cid = odlcard from a deck with did
	public boolean updateDeckDelete( int did, int oldCard) {

		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM DeckLinker WHERE (did =" + did + " AND cid = " + oldCard + ") ;";
			stmt.executeUpdate(query);
			if (stmt != null)
				stmt.close();
			return true;

		} catch (Exception e) {
			printError(e, "updateDeckDelete(...)");
			return false;
		}
	}

	// inner class to save a int and a string
	public class Duo {
		int id;
		String name;

		public Duo(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

	}

// returns id an name of all owned decks of user with iid = myid
	public ArrayList<Duo> getMyDecksEdit(int myid) {
		try {
			String query = "SELECT did , name FROM Deck WHERE owner =" + myid + " ;  ";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Duo> result = new ArrayList<Duo>();
			while (rs.next()) {
				result.add(new Duo(rs.getInt("did"),rs.getString("name")));
			}
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			return result;
		} catch (Exception e) {
			printError(e, "getMyDecksEdit(...)");
			return null;
		}

	}
	// returns the necessary information about owned decks(5) in edit deck
	public ArrayList<DeckSkeleton> getEditDeck(int owner){
		ArrayList<Duo> myd = getMyDecksEdit(owner);
		if (myd == null)
			return null;
		ArrayList<DeckSkeleton> result = new ArrayList<DeckSkeleton>();
		Deck d = null;

		for(int i =0; i<myd.size();i++) {
			d = getDeckById(myd.get(i).getId());
			int[] cids = new int[d.getDeck().size()];
			for (int j=0; j<cids.length; j++){
				cids[j] = d.getDeck().get(j).getType();
			}
			result.add(new DeckSkeleton(d.getId(), myd.get(i).getName(), d.getDeck().size(), getElement(cids)));
			System.out.println(" Deck Skeleton"+d.getId()+", "+myd.get(i).getName()+ ", "+ d.getDeck().size() + ", "+ getElement(cids));
		}
		return result;
	}
		//
	public boolean updateSetMMR(int iid, int mmr) {
		try {
			PreparedStatement p;
			String query = "UPDATE User SET mmr =  ?  WHERE iid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, mmr);
			p.setInt(2, iid);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSetMMR(...)");
			return false;
		}
	}
	//
	public Integer getMMR(int iid) {
		Integer mmr = null;
		try {
			String query = "SELECT mmr FROM User WHERE iid = " + iid + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()) {
				mmr = rs.getInt("mmr");
			}
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			return mmr;
		} catch (Exception e) {
			printError(e, "getMMR(...)");
			return mmr;
		}
	}
	// increments for said umber
	public boolean updateSetWinLoss(int wins, int losses, int iidOwner) {
		try {
			PreparedStatement p;
			String query = "UPDATE User SET wins = wins + ?, losses = losses + ?  WHERE iid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, wins);
			p.setInt(2, losses);
			p.setInt(3, iidOwner);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "updateSetWinLoss(...)");
			return false;
		}
	}

	// calculates the element of a certain decks and gets it´s card ids as input
	private int getElement(int [] type){
		ArrayList<Integer> counts = new ArrayList<Integer>();

		for(int i = 0; i<5;i++){
			counts.add(0);
		}
		for(int i = 0; i< type.length;i++){
			if(type[i]<=6){
				counts.set(4,counts.get(4)+1);
			}else{
				if(type[i]<=18){
					counts.set(3,counts.get(3)+1);
				}else{
					if(type[i]<=24){
						counts.set(0,counts.get(0)+1);
					}else{
						if(type[i]<=33){
							counts.set(2,counts.get(2)+1);
						}else{
							if(type[i]<=40){
								counts.set(1,counts.get(1)+1);
							}else{
								// todo error handling
							}
						}
					}
				}

			}
		}
		ArrayList<Integer> countord = new ArrayList<Integer>();
		countord.addAll(counts);
		Collections.sort(countord);
		for(int i = 0 ;i<5; i++ ){
			if(counts.get(i)==countord.get(countord.size()-1)){
				return i;
			}
		}
		return 0;
	}
	// end todo 30.6

	public void closeDatabaseConnection() {
		try {

			if (c != null)
				c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " in closeDatabaseConnection ");
			System.exit(0);
		}
	}
}
