package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the Deck Table
 */
public class TableDeck {
	private Connection c;

	public TableDeck() {
		super();
	}

	public boolean create() {
		try {
			c = null;
			Statement stmt;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:serverDatabase.db");
			c.setAutoCommit(false);
			try {
				String query1 = "SELECT COUNT(*) AS I FROM Deck ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table Deck Deck already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name Deck

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE Deck" 
						+ "(did INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "owner INTEGER NOT NULL,"		// iid of the owner
						+ "name TEXT NOT NULL," 		// custom name of the deck
						+ " FOREIGN KEY(owner) REFERENCES User(iid)"
						 	//0 deck id
					
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of Deck table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the Deck Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating Deck Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in DeckTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
