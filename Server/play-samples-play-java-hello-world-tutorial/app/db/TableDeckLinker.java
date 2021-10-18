package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the DeckLinker Table
 */
// class links between card and deck elements
public class TableDeckLinker {
	private Connection c;

	public TableDeckLinker() {
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
				String query1 = "SELECT COUNT(*) AS I FROM DeckLinker ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table DeckLinker DeckLinker already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name DeckLinker

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE DeckLinker" 
						+ "(dlid INTEGER PRIMARY KEY AUTOINCREMENT," 	//0 id of the deck linker
						+ "did INTEGER NOT NULL,"	//1 deck id
						+ "cid INTEGER NOT NULL, "	// id of the linked card
						+ "owner INTEGER NOT NULL,"	// id of the owner of bot deck and card
						+ "FOREIGN KEY(owner) REFERENCES User(iid)," 
						+ "FOREIGN KEY(did) REFERENCES Deck(did)," 	
						+ "FOREIGN KEY(cid) REFERENCES Card(cid)"
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of DeckLinker table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the DeckLinker Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating DeckLinker Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in DeckLinkerTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
