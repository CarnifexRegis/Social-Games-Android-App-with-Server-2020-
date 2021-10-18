package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @author Simon Stolz
 * 
 * Creates the Picture Table
 */
public class TablePicture {
	private Connection c;

	public TablePicture() {
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
				String query1 = "SELECT COUNT(*) AS I FROM Picture ;";
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				rs.getInt("I");
				System.out.println("A Table  Picture already exists");
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (c != null)
					c.close();
				return true;
			} catch (Exception e) {
				// JDBC tried to acces non exsitant table
				// create a table with name Picture

				Class.forName("org.sqlite.JDBC");
				stmt = c.createStatement();
				String query2 = "CREATE TABLE Picture" 
						+ "(pid INTEGER PRIMARY KEY AUTOINCREMENT," 	//0 picture id
						+ "picture TEXT NOT NULL,"						// firebase url
						+ "owner INTEGER NOT NULL,"						// user that commited the picture
						+ " FOREIGN KEY(owner) REFERENCES User(iid)"
						+ ");";
				stmt.executeUpdate(query2);
				if (stmt != null)
					stmt.close();
				c.commit();
				System.out.println("Created new instance of Picture table");
				if (c != null)
					c.close();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Couldn't create a new Instance of the Picture Table");
			System.err.println(e.getClass().getName() + ": " + e.getMessage() + " creating Picture Table ");
			System.exit(0);
			try {
				if (c != null) {
					c.close();
				}
				return false;
			} catch (SQLException e1) {
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage() + "coudn't close connection in PictureTable");
				e1.printStackTrace();
				return false;
			}

		}
	}
}
