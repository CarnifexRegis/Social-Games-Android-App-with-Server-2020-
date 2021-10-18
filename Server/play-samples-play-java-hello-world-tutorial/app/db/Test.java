package db;

public class Test {
	public static void main(String[] args) {
		DBManager dbm = new DBManager();
		String fid = "yalla";
		String fid2 = "lel";

		insertUsers(dbm);
		dbm.getUser(1);
		dbm.getMyID(fid);
		dbm.updateAcceptFriend(3, 2);
		dbm.insertFriend(1, 2);
		dbm.insertFriend(2, 3);
		dbm.insertFriend(4, 2);
		dbm.insertFriend(2, 5);
		dbm.insertFriend(6, 7);
		dbm.insertFriend(7, 6);
		dbm.insertFriend(2, 7);
		dbm.insertFriend(7, 2);
		dbm.getFriends(2);
		dbm.insertCard(1, 4);
		int pid = dbm.insertPicture("neineinneineinein", 1);
		
		dbm.updatePidCard(pid, 1);
		dbm.updatePidUser(1, 2);
		dbm.updateSteps(300, 1);
		System.out.println(""+dbm.getSteps(1));
		dbm.updateSocialFriend(1, 1, 2,3 ,4,5, 5,0);
		dbm.getSocialScores(1);
		dbm.updateResetTempSocialAll();
		dbm.getSocialScores(1);
		Quest1 q1 = new Quest1(dbm);
		q1.checkCompletionMontly(2);
		q1.updateMonthlyScores(1, 1, 2,3 ,4,5, 5,0);
		q1.checkCompletionMontly(2);
	}
	public static void insertUsers(DBManager dbm) {
		String fid = "yalla";
		String fid2 = "lel";
		dbm.insertUser(fid, "jösdfj", "nahui@gmx.de");
		dbm.insertUser(fid2, "jösdfj", "nahui2@gmx.de");
		dbm.insertUser("nope", "jösdfj", "nahui3@gmx.de");
		dbm.insertUser("bla", "jösdfj", "nahui4@gmx.de");
		dbm.insertUser("nah", "jösdfj", "nahui5@gmx.de");
		dbm.insertUser("prr", "jösdfj", "nahui6@gmx.de");
		dbm.insertUser("true", "jösdfj", "nahui7@gmx.de");
	}
}
