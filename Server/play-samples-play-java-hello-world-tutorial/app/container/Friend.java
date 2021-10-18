package container;

public class Friend {
	private int iid;
	private int accepted;
	private String name;
	private String picture;
	private int fid;


	public Friend(int fid,int iid, int accepted, String name, String picture) {
		super();
		this.iid = iid;
		this.accepted = accepted;
		this.name = name;
		this.picture = picture;
		this.fid = fid;
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
