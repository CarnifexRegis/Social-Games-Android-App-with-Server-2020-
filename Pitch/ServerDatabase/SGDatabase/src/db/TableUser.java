package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the User Table
 */
public class TableUser {
	private Connection c;

	public TableUser() {
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
				String query1 = "SELECT COUNT(*) AS I FROM User ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table USER user already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name User

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE User" 
						+ "(iid INTEGER PRIMARY KEY AUTOINCREMENT," 	//0 internal id public id
						+ "firebase_id INTEGER UNIQUE," 		//1 firedbase id
						+ "name STRING  NOT NULL," 	//2 Display name
						+ "locked INTEGER DEFAULT 0" 	//4 Locked when Player is ingame
						+ "distance INTEGER DEFAULT 0"
						+"pid INTEGER NOT NULL"
						+"FOREIGN KEY (pid) REFERENCES Picture (pid)"
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of USER table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the USER Table");
			System.out.println(e.getClass().getName() + ": " + e.getMessage() + " creating USER Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in UserTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
