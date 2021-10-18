package container;

public class Card {
	private int id;
	private int type;
	private String picture;

	public Card(int id, int type, String picture) {
		super();
		this.id = id;
		this.type = type;
		this.picture = picture;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

}
