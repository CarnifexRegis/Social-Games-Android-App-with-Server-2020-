package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TableCard {
	public TableCard() {
		super();
	}
	private Connection c;
	public boolean create() {
		try {
			c = null;
			Statement stmt;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:serverDatabase.db");
			c.setAutoCommit(false);
			try {
				String query1 = "SELECT COUNT(*) AS I FROM Card ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table Card already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name Card

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE Card" 
						+ "(cid INTEGER PRIMARY KEY AUTOINCREMENT,"	// 0 CardID
						+ "iid INTEGER NOT NULL," 	// 1 owner
						+ "type INTEGER NOT NULL," 	// 2 card type (e.g. Archangel)
						+ "pid INTEGER DEFAULT -1,"	// refernce on picture element related to the card
						+ "did INTEGER DEFAULT -1," // obsolete was used to reference decks
						+ "FOREIGN KEY(iid) REFERENCES User(iid),"
						+ "FOREIGN KEY(pid) REFERENCES Picture(pid)" 			// 7 Last name
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of Card table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the Card Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating Card Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in Card Table");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
