package container;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;

public class User {
	private int id;
	private int steps;
	private String name;
	private int wins;
	private int losses;
	private String picture;
	

	public User(int id, int steps, String name, int wins, int losses, String picture) {
		super();
		this.id = id;
		this.steps = steps;
		this.name = name;
		this.wins = wins;
		this.losses = losses;
		this.picture = picture;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public ObjectNode toJson(){
		ObjectNode root = Json.newObject();
		root.put("ID", id);
		root.put("Steps", steps);
		root.put("Name", name);
		root.put("Wins", wins);
		root.put("Losses", losses);
		root.put("Picture", picture);
		return root;
	}
}
