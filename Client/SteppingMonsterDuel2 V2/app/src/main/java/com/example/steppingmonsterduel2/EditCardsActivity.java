package com.example.steppingmonsterduel2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.GameContent;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class EditCardsActivity extends AppCompatActivity {
    ViewGroup cardBar;
    SparseArray<ListCardLayout> cardTradeList;
    static int REQCODE = 1;
    static int MY_RESULT_ACCESS_FINE =1;

    ImageView ownerImage;
    PlayerCard cardAtm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cards);

        cardBar = findViewById(R.id.EditCards);
        cardTradeList = new SparseArray<>();

        ImageView backButton = findViewById(R.id.HomeMenuButtonFromEditCard);
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(EditCardsActivity.this,HomeActivity.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
            }
        } );

        integrateCards();
    }

    //adds the cards to the scrollview
    private void addViewToScrollbar(final Integer key, String picture, boolean isMonster, GameContent.CardType type) {
        EditCardsActivity.ListCardLayout layout;
        if(isMonster) {
            layout = new ListCardLayout( cardBar, key, picture);
        }else
            layout = new ListCardLayout(cardBar, key, picture,isMonster);
        cardTradeList.put(key, layout);
    }

    //integrates the card to the scrollview
    private void integrateCards() {
        for(Pair<Integer, PlayerCard> pair : Configuration.CARDS){
            boolean isMonster = false;
            if(pair.second.getCardType() instanceof GameContent.MonsterType){
                isMonster=true;
            }
            addViewToScrollbar(pair.first,pair.second.getPicture(),isMonster,pair.second.getCardType());
            System.out.println("Added");

        }
    }

    //Does the same as in swap card activity take a look there to understand this class
    private class ListCardLayout {

        final View card;
        final ViewGroup parent;
        int playerCardID;
        final TextView cardName;
        final ImageView cardElem;
        final ImageView cardImg;
        TextView cardDesc = null;
        final TextView cardMana;
        TextView cardAtk = null;
        TextView cardHealth = null;

        private GameContent.CardType spellType;
        private GameContent.MonsterType monsterType;

        ListCardLayout(ViewGroup parent, int playerCardID, String picture,boolean isMonster){
            //create view and add it to parent


            this.parent = parent;

            this.card = getLayoutInflater().inflate(R.layout.layoutforcardsspell, parent, false);
            parent.addView(card);
            parent.invalidate();
            //initialize members
            this.cardName = card.findViewById(R.id.name);
            this.cardElem = card.findViewById(R.id.element);
            this.cardImg = card.findViewById(R.id.image);
            this.cardDesc = card.findViewById(R.id.description);
            this.cardMana = card.findViewById(R.id.mana);
            //configure everything that has to do with the card's type
            updateSpellCardInfo(playerCardID,picture);
        }

        ListCardLayout(ViewGroup parent, int playerCardID, String picture){
            //create view and add it to parent
            this.parent = parent;
            this.card = getLayoutInflater().inflate(R.layout.layoutforcardsmonster, parent, false);
            parent.addView(card);
            parent.invalidate();
            //initialize members
            this.cardName = card.findViewById(R.id.name);
            this.cardElem = card.findViewById(R.id.element);
            this.cardImg = card.findViewById(R.id.image);
            this.cardMana = card.findViewById(R.id.mana);
            this.cardAtk = card.findViewById(R.id.damage);
            this.cardHealth = card.findViewById(R.id.health);
            //configure everything that has to do with the card's type
            updateMonsterCardInfo(playerCardID,picture);
        }

        private void updateMonsterCardInfo(int playerCardID, String picture){
            //get crucial info
            this.playerCardID = playerCardID;
            PlayerCard playerCard = Configuration.CARDS.get(playerCardID);
            this.monsterType = (GameContent.MonsterType)playerCard.getCardType();
            //update display
            cardName.setText( monsterType.name );
            cardElem.setImageResource( Element.getMonsterResourceID(monsterType.element));
            cardAtk.setText(""+monsterType.damage);
            cardAtk.setTextSize(55);
            cardHealth.setText(""+monsterType.health);
            cardHealth.setTextSize(55);
            cardMana.setText(""+monsterType.mana);
            cardMana.setTextSize(60);

            if(picture.equals("null")){
                cardImg.setImageResource(R.drawable.standart);
            } else{
                Configuration.getPictureOutOfStorageAndSetItToView(EditCardsActivity.this, cardImg,picture);
            }
            //update click listeners:
            //on click: display card description
            card.setOnClickListener((v)->
                    new AlertDialog.Builder(EditCardsActivity.this)
                            .setTitle("Description:")
                            .setMessage(monsterType.descriptionLong)
                            .show()
            );

            card.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ownerImage = cardImg;
                    cardAtm = playerCard;
                    checkForPermission();
                    return false;
                }
            } );
        }

        private void updateSpellCardInfo(int playerCardID, String picture){
            //get crucial info
            this.playerCardID = playerCardID;
            PlayerCard playerCard = Configuration.CARDS.get(playerCardID);
            this.spellType = playerCard.getCardType();
            //update display
            cardName.setText( spellType.name );
            cardElem.setImageResource( Element.getSpellResourceID(spellType.element));
            cardDesc.setText(spellType.descriptionShort);
            cardDesc.setTextSize(30);
            cardMana.setText(""+spellType.mana);
            cardMana.setTextSize(60);
            //cardImg.setImageResource( R.drawable.affe );

            if(picture.equals("null")){
                cardImg.setImageResource(R.drawable.standart);
            } else{
                Configuration.getPictureOutOfStorageAndSetItToView(EditCardsActivity.this, cardImg,picture);
            }
            //update click listeners:
            //on click: display card description
            card.setOnClickListener((v)->
                    new AlertDialog.Builder(EditCardsActivity.this)
                            .setTitle("Description:")
                            .setMessage(spellType.descriptionLong)
                            .show()
            );

            card.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ownerImage = cardImg;
                    cardAtm = playerCard;
                    checkForPermission();
                    return false;
                }
            } );
        }
    }

    //Checks if the user has the needed permission to update the picture
    private void checkForPermission(){
        if(ContextCompat.checkSelfPermission(EditCardsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(EditCardsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE))
                ActivityCompat.requestPermissions(EditCardsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_RESULT_ACCESS_FINE);
        } else{
            chosePicture();
        }
    }

    //Chose the picture from ur phone storage
    private void chosePicture() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQCODE);
    }

    //Stores the image in the firebasestorage

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Check if picture was succesfully taken
        if(requestCode==REQCODE&&resultCode==RESULT_OK&&data!=null){
            try {
                //gets the data which image was taken from storage
                Uri chosenImage = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),chosenImage);
                StorageReference storageRef = Configuration.storage.getReference();

                //Set path of image
                final String path = "userPhotos/cardIcon"+cardAtm.getCardID()+".jpg";
                final StorageReference imgRef = storageRef.child(path);

                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                byte[] bytes = byteArray.toByteArray();

                final UploadTask uploadTask = imgRef.putBytes(bytes);
                //If storage failed send a toast.make text notification
                //If storing picture worked save the picture in the card
                //and show the picutre to the user on the card
                //save the picture location on the server
                //since servercommunication does not allow / we need to replace them by ,
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(EditCardsActivity.this,"Picture could not be stored in cloud!",Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //set picture
                        if(ownerImage!=null)
                            ownerImage.setImageURI(chosenImage);
                        //Save imagepath in database
                        HttpGetter storePicture = new HttpGetter();
                        String pathChanged = path.replace('/',',');

                        storePicture.execute("save",""+cardAtm.getCardID(),pathChanged,"Picture");
                        try {
                            String getPid = storePicture.get(Configuration.timeoutTime, TimeUnit.SECONDS);
                            JSONObject jsonObject = new JSONObject(getPid);
                            //bind imagepath to user
                            HttpGetter getIfWorked = new HttpGetter();
                            getIfWorked.execute("save",""+jsonObject.getInt("Pid"),""+cardAtm.getCardID(),"PidCard");
                            String worked = getIfWorked.get(Configuration.timeoutTime,TimeUnit.SECONDS);
                            JSONObject jsonObject1 = new JSONObject(worked);
                            if(jsonObject1.getBoolean("Worked?")) {
                                cardAtm.setPicture(pathChanged);
                                Toast.makeText(EditCardsActivity.this, "Linking picture worked!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(EditCardsActivity.this, "Linking picture failed!", Toast.LENGTH_SHORT).show();
                            }
                            jsonObject.getInt("Pid");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(EditCardsActivity.this,1,true);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(EditCardsActivity.this,2,true);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(EditCardsActivity.this,0,true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Configuration.serverDownBehaviour(EditCardsActivity.this,3,true);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}