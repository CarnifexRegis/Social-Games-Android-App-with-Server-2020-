package container;

public class FriendFullInfo {

        private int iid;
        private int accepted;
        private String name;
        private String picture;
        private int fid;
        private int steps;
        private int winrate;


        public FriendFullInfo(int fid,int iid, int accepted, String name, String picture,int steps, int winrate) {
            super();
            this.iid = iid;
            this.accepted = accepted;
            this.name = name;
            this.picture = picture;
            this.fid = fid;
            this.steps = steps;
            this.winrate = winrate;
        }

        public int getSteps(){
            return steps;}

        public int getWinrate(){
            return winrate;
        }

        public int getIid() {
            return iid;
        }

        public void setIid(int iid) {
            this.iid = iid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public int getAccepted() {
            return accepted;
        }

        public void setAccepted(int accepted) {
            this.accepted = accepted;
        }

        public int getFid() {
            return fid;
        }

        public void setFid(int fid) {
            this.fid = fid;
        }
    }

