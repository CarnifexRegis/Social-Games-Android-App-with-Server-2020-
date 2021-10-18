package com.example.steppingmonsterduel2.Objects;

import com.example.steppingmonsterduel2.Services.GameContent;

public class PlayerCard {
    private int cardTypeID;
    private String picture;
    private GameContent.CardType cardType;
    private int cardID;

    public PlayerCard(int cardTypeID, String picture,int cardID){
        this.cardTypeID = cardTypeID;
        this.picture = picture;
        this.cardType = GameContent.getCardTypeByID(cardTypeID);
        this.cardID = cardID;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public int getCardTypeID(){
        return cardTypeID;
    }
    public GameContent.CardType getCardType(){
        return cardType;
    }
    public String getPicture(){
        return picture;
    }
    public void setPicture(String newPicture){
        this.picture = newPicture;
    }
}
