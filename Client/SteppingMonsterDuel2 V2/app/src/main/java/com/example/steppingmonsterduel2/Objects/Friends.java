package com.example.steppingmonsterduel2.Objects;

import android.graphics.drawable.Icon;
import android.net.Uri;

public class Friends {

    //Some friend attributes easy to read tho
    private int iid;
    private int accepted;
    private String name;
    private String picture;
    private int fid;
    private int tradeCount;
    private int matchCount;
    private int score;
    private int steps;
    private int winrate;

    public Friends(int iid, int accepted, String name, String picture,int fid,int tradeCount,int matchCount,int score,int steps,int winrate) {
        super();
        this.iid = iid;
        this.accepted = accepted;
        this.name = name;
        this.picture = picture;
        this.fid = fid;
        this.tradeCount = tradeCount;
        this.matchCount = matchCount;
        this.score = score;
        this.steps = steps;
        this.winrate = winrate;
    }

    public int getSteps(){
        return steps;
    }

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
    public int getFid(){
        return fid;
    }
    public void setFid(int fid){
        this.fid = fid;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public int getTradeCount() {
        return tradeCount;
    }
    public int getScore(){
        return score;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }
}