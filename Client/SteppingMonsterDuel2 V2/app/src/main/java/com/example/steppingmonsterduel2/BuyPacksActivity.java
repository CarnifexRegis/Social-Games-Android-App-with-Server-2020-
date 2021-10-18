package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Services.UIUpdater;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class BuyPacksActivity extends AppCompatActivity {

    TextView StepNumberView;
    TextView packTypeView;
    //0=inferno  1= tsunami 2=storm 3=earth 4=twilight 5=normal
    private int selectedPackElement = -1;
    private final String TAG = "BuyPacksActivity";
    private final int PACK_PRICE = 15000;
    private ImageView[] packViews; // contains the pack elements ( needed for highlighting and stuff)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_packs);
        packViews = new ImageView[6];

        ImageView backButton = findViewById(R.id.BackToPackOp);
        packTypeView = findViewById(R.id.packTypeText);
        TextView packPrice = findViewById(R.id.PackPriceText);
        packPrice.setText("Pack price : " + PACK_PRICE);

        backButton.setOnClickListener(v -> {
            Intent buyPackIntent = new Intent(BuyPacksActivity.this, OpenPacksActivity.class);
            startActivity(buyPackIntent);
        });

        Button buyPackButton = findViewById(R.id.BuyPackButton);

        buyPackButton.setOnClickListener(v -> {
            if (selectedPackElement == -1){
                Toast.makeText(BuyPacksActivity.this,"No pack is selected!",Toast.LENGTH_SHORT).show();
                return;
            }

            serverBuyPackRequest(selectedPackElement);

        });


        //set packs
        packViews[0] = findViewById(R.id.packInferno);
        packViews[1] = findViewById(R.id.packTsunami);
        packViews[2] = findViewById(R.id.packStorm);
        packViews[3] = findViewById(R.id.packEarth);
        packViews[4] = findViewById(R.id.packTwilight);
        packViews[5] = findViewById(R.id.packNormal);

        packViews[0].setOnClickListener(v -> {
            updateUI(0);
            packTypeView.setText("inferno Pack");
        });

        packViews[1].setOnClickListener(v -> {
            updateUI(1);
            packTypeView.setText("Tsunami Pack");
        });

        packViews[2].setOnClickListener(v -> {
            updateUI(2);
            packTypeView.setText("Storm Pack");
        });

        packViews[3].setOnClickListener(v -> {
            updateUI(3);
            packTypeView.setText("Earth Pack");
        });

        packViews[4].setOnClickListener(v -> {
            updateUI(4);
            packTypeView.setText("Twilight Pack");
        });

        packViews[5].setOnClickListener(v -> {
            updateUI(5);
            packTypeView.setText("Normal Pack");
        });

        StepNumberView = findViewById(R.id.StepsView);

    }

    /**
     * Updates the UI Depending on the element of the card :
     * - gets a color (from color.xml) and sets the text color and pack name in the packName Textview
     * - sets a highlighted border around the actual selected element (inspired from https://stackoverflow.com/questions/3263611/border-for-an-image-view-in-android)
     * @param element
     */
    protected void updateUI(int element){
        selectedPackElement = element;
        Drawable background = getDrawable(R.drawable.packhighlight);

        if (background instanceof GradientDrawable){
            GradientDrawable shapeDrawable = (GradientDrawable) background;
            int color = Color.BLACK;

            switch (element){
                case 0:
                    color = R.color.packInferno;
                    break;
                case 1:
                    color = R.color.packTsunami;
                    break;
                case 2:
                    color = R.color.packStorm;
                    break;
                case 3:
                    color = R.color.packEarth;
                    break;
                case 4:
                    color = R.color.packTwilight;
                    break;
                case 5:
                    color = R.color.packNormal;
                    break;
            }

            packTypeView.setTextColor(ContextCompat.getColor(getApplicationContext(), color));
            shapeDrawable.setStroke(10, ContextCompat.getColor(getApplicationContext(), color));

            packViews[element].setBackground(shapeDrawable);

        }else{
            Log.e(TAG, "unexpected drawable not shape drawable");
        }



        for (int i=0; i<packViews.length; i++){
            if (i == element)
                continue;

            packViews[i].setBackgroundColor(Color.TRANSPARENT);
        }
    }


    /**
     *  Send a pack buy request to the server, both client and server check if the user can afford the pack ( for uncessary requests)
     *  if the pack was successfully bought, the price will be deducted and a pack added to the user account
     * @param packType : integer mapped to the pack element
     */
    protected void serverBuyPackRequest(int packType){
        //if displayed number of steps is too low, do not send the request ( this may be erronous with bad updates but it lowers the server burden)
        if (UIUpdater.getInstance().getSteps() < PACK_PRICE){
            Log.d(TAG, "not enough moneyyy!");
            Toast.makeText(BuyPacksActivity.this,"Not enough steps !",Toast.LENGTH_SHORT).show();
            return;
        }

        //now server request to add the pack, it checks the actual number of steps and returns true if the user can buy ( and bought) the pack in question
        HttpGetter getRequest = new HttpGetter();
        getRequest.execute("buy",""+Configuration.currentUser.getId(),""+packType,"Pack");

        try {
            JSONObject jsonObject = new JSONObject(getRequest.get(Configuration.timeoutTime, TimeUnit.SECONDS));
            boolean didItWork = jsonObject.getBoolean("worked");
            Log.d(TAG, "sent an add pack request to server for pack " + packType + " and got " + didItWork);

            if (didItWork){
                OpenPacksActivity.numberOfPacks++;
                OpenPacksActivity.packsTypesStack.add(packType);
                UIUpdater.getInstance().setSteps(UIUpdater.getInstance().getSteps() - PACK_PRICE);
                Toast.makeText(BuyPacksActivity.this, "You successfully bought a pack ", Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(BuyPacksActivity.this,3,false);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(BuyPacksActivity.this,2,false);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Configuration.serverDownBehaviour(BuyPacksActivity.this,1,false);
        } catch (TimeoutException e){
            e.printStackTrace();
            Configuration.serverDownBehaviour(BuyPacksActivity.this,0,false);
        }
    }

    //UI updater takes care of updating the steps with help of the location service
    @Override
    protected void onResume() {
        super.onResume();
        UIUpdater.getInstance().addWatcher(this.StepNumberView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        UIUpdater.getInstance().removeWatcher(this.StepNumberView);
    }
}
