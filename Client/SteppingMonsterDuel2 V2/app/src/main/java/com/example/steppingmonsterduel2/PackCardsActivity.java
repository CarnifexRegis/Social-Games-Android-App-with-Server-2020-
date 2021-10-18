package com.example.steppingmonsterduel2;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.GameContent;

import java.util.Locale;

/*
This will be a transparent activity that appears on top when player open
packs and they can swipe the cards
 */

/**
 * Special activity that acts as a dialog box, the activity receives the 5 packs opened by the user and makes it so the user
 * can either:
 * -swipe the cards to see the next card
 * -click on the card to read the description
 * when all the cards have been swiped the actiity automatically leaves
 */

public class PackCardsActivity extends Activity {

    public static int[] cardsInPack;
    private final String TAG = "PacksCardsActivity";
    private ConstraintLayout cardView[];
    private GameContent.CardType cardInfos[];
    private int actualCard = 0;

    private final int CARD_WIDTH = 249;
    private final int CARD_HEIGHT = 391;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack_cards);
        cardView = new ConstraintLayout[5];
        cardInfos = new GameContent.CardType[5];

        cardView[0] = (ConstraintLayout) findViewById(R.id.CardContainer4);
        cardView[1] = (ConstraintLayout) findViewById(R.id.CardContainer3);
        cardView[2] = (ConstraintLayout) findViewById(R.id.CardContainer2);
        cardView[3] = (ConstraintLayout) findViewById(R.id.CardContainer1);
        cardView[4] = (ConstraintLayout) findViewById(R.id.CardContainer);

        final GestureDetector myGestureListener = new GestureDetector(getApplicationContext(), new UpSwipeDetector());
        cardView[0].setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return myGestureListener.onTouchEvent(event);
            }
        });
        cardView[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDescriptionDisplay();
            }
        });

        for (int i=0; i<5; i++){
            cardView[i].setEnabled(false);
            cardInfos[i] = GameContent.getCardTypeByID(cardsInPack[i]);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        actualCard = 0;
        if (cardsInPack == null){
            return;
        }
        if (cardsInPack.length < 5){
            Log.e(TAG, "unexpected pack size ! leaving");
            leaveActivity();
        }

        setCardModel();


    }

    //sets the card layout infos
    protected void setCardModel(){
        //set the layout inflater
        LayoutInflater setCardInflater = getLayoutInflater();

        //Get the shared stats
        int manaCost = cardInfos[actualCard].mana;
        String cardName = cardInfos[actualCard].name;
        Element cardElement = cardInfos[actualCard].element;

        if (cardInfos[actualCard] instanceof GameContent.MonsterType){
            View layoutCard = setCardInflater.inflate(R.layout.card_layout_monster,cardView[actualCard],false);

            //get the views
            TextView atkText = layoutCard.findViewById(R.id.damage);
            TextView defenseText = layoutCard.findViewById(R.id.health);
            TextView manaText = layoutCard.findViewById(R.id.mana);
            TextView nameText = layoutCard.findViewById( R.id.name );
            ImageView elementBorder = layoutCard.findViewById(R.id.element);

            cardView[actualCard].addView(layoutCard);

            //get special monster stats
            GameContent.MonsterType monsterCardStats = (GameContent.MonsterType)cardInfos[actualCard];
            int damage = monsterCardStats.damage;
            int health = monsterCardStats.health;


            //set the info
            switch(cardElement){
                case INFERNAL:
                    elementBorder.setImageResource(R.drawable.monster_inferno);
                    break;
                case TSUNAMI:
                    elementBorder.setImageResource(R.drawable.monster_tsunami);
                    break;
                case STORM:
                    elementBorder.setImageResource(R.drawable.monster_storm);
                    break;
                case EARTHQUAKE:
                    elementBorder.setImageResource(R.drawable.monster_land);
                    break;
                case TWILIGHT:
                    elementBorder.setImageResource(R.drawable.monster_twilight);
                    break;
                default:
                    Log.e(TAG, "error with the card ID");
                    break;
            }

            //modify the views
            nameText.setText(cardName);
            nameText.setTextSize(13f);
            manaText.setText(intToString(manaCost));
            manaText.setTextSize(20f);
            atkText.setText(intToString(damage) );
            atkText.setTextSize( 15f );
            defenseText.setText( intToString(health) );
            defenseText.setTextSize( 15f );
        }else{
            View layoutCard = setCardInflater.inflate(R.layout.card_layout_spell,cardView[actualCard],false);

            //get the views
            TextView manaText = layoutCard.findViewById(R.id.mana);
            TextView nameText = layoutCard.findViewById( R.id.name );
            TextView descriptionText = layoutCard.findViewById(R.id.description);
            ImageView elementBorder = layoutCard.findViewById(R.id.element);
            String description = cardInfos[actualCard].descriptionLong;

            cardView[actualCard].addView(layoutCard);

            //set the info
            switch(cardElement){
                case INFERNAL:
                    elementBorder.setImageResource(R.drawable.spell_inferno);
                    break;
                case TSUNAMI:
                    elementBorder.setImageResource(R.drawable.spell_tsunami);
                    break;
                case STORM:
                    elementBorder.setImageResource(R.drawable.spell_storm);
                    break;
                case EARTHQUAKE:
                    elementBorder.setImageResource(R.drawable.spell_land);
                    break;
                case TWILIGHT:
                    elementBorder.setImageResource(R.drawable.spell_twilight);
                    break;
                default:
                    Log.e(TAG, "error with the card ID");
                    break;
            }

            //set the views
            nameText.setText(cardName);
            nameText.setTextSize(13f);
            manaText.setText(intToString(manaCost));
            manaText.setTextSize(20f);
            descriptionText.setText(description);
            descriptionText.setTextSize(13f);

        }


        //enable the button so the player can swipe it
        cardView[actualCard].setEnabled(true);
    }
    protected void leaveActivity(){
        Intent backToPacks = new Intent(PackCardsActivity.this, OpenPacksActivity.class);
        startActivity(backToPacks);
    }

    /*
    Custom class for swipe detection (help gotten from https://stackoverflow.com/questions/17830529/onfling-not-being-called-for-some-reason)
    and for click detection to display the card description
     */
    private class UpSwipeDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "swipe detected " + velocityY);
            if (velocityY>-0.5f){
                return false;
            }

            //TODO: swipe up actual Card
            swipeAnimation(actualCard);
            actualCard++;
            if (actualCard > 4) {
                leaveActivity();
                return true;
            }
            setCardModel();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e){
            CardDescriptionDisplay();
            return true;
        }
    }

    //display the card description
    private void CardDescriptionDisplay(){
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DescriptionDialog))
                .setTitle("Description:")
                .setMessage(cardInfos[actualCard].descriptionLong)
                .show();
    }

    //animation triggered by the swipe mouvement
    private void swipeAnimation(int id){
        Animation animation = new TranslateAnimation(0, 0,0, -2000);
        animation.setDuration(500);
        animation.setFillAfter(true);
        cardView[id].startAnimation(animation);
        cardView[id].setVisibility(View.INVISIBLE);
    }

    private String intToString(int integer){
        return String.format(Locale.US, "%d", integer);
    }
}




/*
actualCard++;
            if (actualCard > 4){
                leaveActivity();
            }
            //TODO: adapted image with card id
            Log.d(TAG, "swipe detected");
            cardView.setImageResource(R.drawable.testcard);
            return true;
 */
