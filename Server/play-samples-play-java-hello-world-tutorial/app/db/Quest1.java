package db;
import javax.inject.Inject;

public class Quest1 {
	private DBManager dbm ;
	private int standardTradesAndMatches = 2;

	@Inject
	public Quest1(DBManager dbm ) {
		this.dbm = dbm;
	}
	public boolean checkCompletionMontly(int fid) {
		int [] sc = dbm.getSocialScores(fid);
		if(sc!= null) {
			if(sc[1]>=3&&sc[3]>=3) {
				System.out.println("finish");
				return true;
			}else {
				System.out.println("no finish");
				return false;
			}
		}else {
			System.out.println("db error");
			return false;
		}
	}
	public void updateMonthlyScores(int fid, int trade, int match, int temprematchcount, int temptradecount,int score,int date,int merge) {
		dbm.updateSocialFriend(fid, trade,temptradecount, match ,temprematchcount, score, date,merge);
	}


	public int calculateQuests(int score){
		int result = score%19+standardTradesAndMatches;
		return result;
	}

	public int getStandardTradesAndMatches() {
		return standardTradesAndMatches;
	}
}
