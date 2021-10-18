package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the Friends Table
 */
public class TableFriends {
	private Connection c;

	public TableFriends() {
		super();
	}

	public boolean create() {
		try {
			c = null;
			Statement stmt;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:serverDatabase");
			c.setAutoCommit(false);
			try {
				String query1 = "SELECT COUNT(*) AS I FROM Friends ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table Friends already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name Friends

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE Friends" 
						+ "(fid INTEGER PRIMARY KEY AUTOINCREMENT," 	//0 Friendship id
						+ "iid1 INTEGER NOT NULL," 						//1 Internal user id 1
						+ "iid2 INTEGER  NOT NULL, "					//2 internal user id 2
						+"accepted INTEGER DEFAULT 0,"
						+ "FOREIGN KEY(iid1) REFERENCES User(iid),"
						+ "FOREIGN KEY(iid2) REFERENCES User(iid)" 			
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of Friends table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the Friends Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating Friends Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in FriendsTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
