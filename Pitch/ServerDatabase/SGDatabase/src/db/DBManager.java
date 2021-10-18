package db;

import java.security.SecureRandom;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import container.Friend;

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
	private TableMatchHistory matchHistory = new TableMatchHistory();
	private TableDeckLinker deckLinker = new TableDeckLinker();
	Connection c;
	SecureRandom random = new SecureRandom();

	public DBManager() {
		if ( picture.create() && user.create() && card.create() && friends.create() && deck.create()
				&& matchHistory.create() && deckLinker.create()) {
			try {
				Class.forName("org.sqlite.JDBC");
				c = null;
				c = DriverManager.getConnection("jdbc:sqlite:serverDatabase");
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

	// INSERT
	public boolean insertCard(int iid, int category, int element, int form, int atk, int def) {
		try {

			String query = "INSERT INTO CARD (iid,category,element,form,atk,def) VALUES (?,?,?,?,?,?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, iid);
			p.setInt(2, category);
			p.setInt(3, element);
			p.setInt(4, form);
			p.setInt(5, atk);
			p.setInt(6, def);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertCard(...)");
			return false;
		}
	}

	public boolean insertUser(int fid, String name) {
		try {
			String query = "INSERT INTO CARD (firebase_id,name) VALUES (?,?)";
			PreparedStatement p = c.prepareStatement(query);
			p.setInt(1, fid);
			p.setString(1, name);
			p.executeUpdate();
			if (p != null)
				p.close();
			c.commit();
			return true;
		} catch (Exception e) {
			printError(e, "insertUser(...)");
			return false;
		}
	}

	public boolean insertFriend(int iid1, int iid2) {
		try {
			String query1 = "SELECT COUNT (*) AS I FROM FRIENDS WHERE iid1 =" + iid1 + "AND iid2 = " + iid2 + ";";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query1);
			if (rs.getInt("I") > 0) {
				System.out.println("Friend already exists");

				return false;
			} else {
				try {
					String query2 = "INSERT  INTO FRIENDS (iid1, iid2) VALUES (?,?)";
					PreparedStatement p = c.prepareStatement(query2);
					if (p != null)
						p.close();
					p.setInt(1, iid1);
					p.setInt(2, iid2);
					p.executeUpdate();
					c.commit();
					return true;
				} catch (Exception e) {
					printError(e, "insertFriend(...)");
					return false;
				}
			}
		} catch (Exception e) {
			printError(e, "insertFriend(...)");
			return false;
		}
	}

	// UPDATE
	public boolean acceptFriend(int pid) {
		try {
			PreparedStatement p;
			String query = "UPDATE Card SET pid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, pid);
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
	public boolean updatePidCard(int pid) {
		try {
			PreparedStatement p;
			String query = "UPDATE Card SET pid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, pid);
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
	public boolean updatePidUser(int pid) {
		try {
			PreparedStatement p;
			String query = "UPDATE User SET pid = ?";
			p = c.prepareStatement(query);
			p.setInt(1, pid);
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

	// DELETE
	public boolean deleteFriend(int iid1, int iid2) {
		try {
			Statement stmt = c.createStatement();
			String query = "DELETE FROM FRIEND WHERE (iid1 =" + iid1 + " AND iid2 = " + iid2 + ") OR (iid2 =" + iid1 + " AND iid1 = "+iid2+") ;";
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

	// SELECT
	// gets all friends and other ppls friend requests
	public ArrayList<Friend> getFriends(int reqid) {
		ArrayList<Friend> friends = new ArrayList<Friend>();
		try {
			String query = "SELECT  u.iid, u.name, fu.id,p.picture,fu.accepted  FROM User u"
					+ "RIGHT JOIN (SELECT id, iid2 AS iid FROM Friends WHERE iid1 = "+reqid+" AND accpeted>0 UNION SELECT id, iid1 AS iid FROM Friends WHERE iid2 = "+ reqid+") fu "
					+ "ON u.iid = fu.iid "
					+ "LEFT JOIN Picture p"
					+ "ON p.pid = u.pid ORDER BY u.name DESC;";
			//http://helpdesk.objects.com.au/java/how-do-i-convert-a-jdbc-blob-to-a-string
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				Blob b = rs.getBlob("p.picture");
				String picture =  new String(b.getBytes(1l, (int)b.length()));
				friends.add(new Friend(rs.getInt("u.iid"), rs.getInt("fu.accepted"), rs.getString("u.name"),picture));
			}
		} catch (Exception e) {
			printError(e, "getFriends(...)");
		}
		return friends;
	}

	public void printError(Exception e, String msg) {
		System.err.println(e.getClass().getName() + ": " + e.getMessage() + msg);
		System.exit(0);
		e.printStackTrace();
	}

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
