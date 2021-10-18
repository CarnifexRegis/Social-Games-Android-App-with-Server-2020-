package com.example.steppingmonsterduel2.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Parcelable {

    private int id;
    private int steps;
    private String name;
    private int winRate;
    private String picture;

    public User(int id, int steps, String name,int winRate, String picture) {
        super();
        this.id = id;
        this.steps = steps;
        this.name = name;
        this.winRate = winRate;
        this.picture = picture;
    }
    public User(int id, int steps, String name, int wins, int losses, String picture){
        this(id, steps, name,
                wins+losses==0?50:((int)((100.0*wins)/(wins+losses))), //winrate
                picture);
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

    public int getWinRate() {
        return winRate;
    }

    public void setWinRate(int winRate) {
        this.winRate = winRate;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String toString(){
        String ret = "Name: " + name +"; ID:" + id + "; Steps:" + steps + "; Winrate:"+winRate;
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(steps);
        dest.writeString(name);
        dest.writeInt(winRate);
        dest.writeString(picture);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in) {
        id = in.readInt();
        steps = in.readInt();
        name = in.readString();
        winRate = in.readInt();
        picture = in.readString();
    }

    public static User fromJson(String json) throws JSONException{
        JSONObject root = new JSONObject(json);
        int id = root.getInt("ID");
        int steps = root.getInt("Steps");
        String name = root.getString("Name");
        int wins = root.getInt("Wins");
        int losses = root.getInt("Losses");
        String picture = root.getString("Picture");
        return new User(id, steps, name, wins, losses, picture);
    }
}
