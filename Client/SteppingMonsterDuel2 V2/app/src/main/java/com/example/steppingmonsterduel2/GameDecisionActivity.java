package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GameDecisionActivity extends AppCompatActivity {


    private User opponent;
    private boolean isWin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_decision_game);


        //initialize all the views to use them
        Button addFriend = findViewById(R.id.addFriendWinLose);
        Button home = findViewById(R.id.ContinueButton);
        ImageView image = findViewById(R.id.WinLoseImage);
        addFriend.setVisibility(View.VISIBLE);
        //gets the id of the opponent
        opponent = getIntent().getParcelableExtra("Opponent");
        //gets the information if you won or lost
        isWin = getIntent().getBooleanExtra("Victory", false);

        if(isWin) {
            //if won update your win/lose
            HttpPoster postWin = new HttpPoster();
            postWin.execute("increment",Configuration.currentUser.getId()+"","Win");

            image.setImageResource( R.drawable.wins );
            //is in win condition that this is not updated twice
            HttpPoster postMatchCount = new HttpPoster();
            postMatchCount.execute("update",Configuration.currentUser.getId()+"",opponent.getId()+"","MatchCount");


        }else {
            image.setImageResource( R.drawable.lose );
            //if won update your win/lose
            HttpPoster postLose = new HttpPoster();
            postLose.execute("increment",Configuration.currentUser.getId()+"","Lose");
        }


        //get your information from server again so that it is updated in your friendlist
        HttpGetter getUserInfo = new HttpGetter();
        try {
           String result = getUserInfo.execute(new String[]{"" + Configuration.currentUser.getId(), "getMyUserInformation"}).get(Configuration.timeoutTime, TimeUnit.SECONDS);
            JSONObject obj = new JSONObject(result);

           Configuration.currentUser.setWinRate(
                   (obj.getInt("Wins")+ obj.getInt("Loses"))==0?50:((int)((100.0*obj.getInt("Wins"))/(obj.getInt("Wins")+obj.getInt("Loses")))));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //sends a friendrequest to the opponent
        //If friend already exists then u get notified
        //set the button to invisible afterwards
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(opponent.getId()+"");
                HttpGetter addUser = new HttpGetter();
                addUser.execute( "search", "" + Configuration.currentUser.getId(), "" + opponent.getId(), "Friend" );
                try {
                    JSONObject jsonObject = new JSONObject( addUser.get( Configuration.timeoutTime, TimeUnit.SECONDS ) );
                    int result = jsonObject.getInt( "Worked" );

                    if (result == 1) {
                        Toast.makeText( GameDecisionActivity.this, "Your opponent has been successfully added!", Toast.LENGTH_SHORT ).show();
                        addFriend.setVisibility(View.INVISIBLE);
                    } else if (result == 0) {
                        Toast.makeText( GameDecisionActivity.this, "This friend already exists!", Toast.LENGTH_SHORT ).show();
                        addFriend.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText( GameDecisionActivity.this, "Some error occured by searching opponent!", Toast.LENGTH_SHORT ).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour( GameDecisionActivity.this, 3 ,true);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour( GameDecisionActivity.this, 2 ,true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour( GameDecisionActivity.this, 1 ,true);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    Configuration.serverDownBehaviour( GameDecisionActivity.this, 0 ,true);
                }
            }
        });

        //Continue to home menu
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(GameDecisionActivity.this, HomeActivity.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                finish();
            }
        });
    }

}
