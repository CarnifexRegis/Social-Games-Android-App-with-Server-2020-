package db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the MatchHistory Table
 */
// not used yet
public class TableMatchHistory {
	private Connection c;

	public TableMatchHistory() {
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
				String query1 = "SELECT COUNT(*) AS I FROM MatchHistory ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table MatchHistory MatchHistory already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name MatchHistory

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE MatchHistory" 
						+ "(mhid INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "won INTEGER DEFAULT (-1) NOT NULL," 	//0 internal id
						+ "iid1 INTEGER NOT NULL," 						//1 Internal user id 1
						+ "iid2 INTEGER  NOT NULL, "					//2 internal user id 2
						+ "FOREIGN KEY(iid1) REFERENCES User(iid),"
						+ "FOREIGN KEY(iid2) REFERENCES User(iid)" 	
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of MatchHistory table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the MatchHistory Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating MatchHistory Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in MatchHistoryTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
