package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.steppingmonsterduel2.Objects.User;
import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpGetter;
import com.example.steppingmonsterduel2.Util.HttpPoster;

import org.json.JSONException;

public class MatchmakingActivity extends AppCompatActivity {

    private Handler handler;
    private boolean searching;

    private int searchTextIDX = 0;
    private String[] searchText = new String[]{"Searching for Players", " Searching for Players.", "  Searching for Players..", "   Searching for Players..."};

    private Button searchForPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);

        searching = false;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkIfMatchFound();
                handler.postDelayed(this, Configuration.REFRESH_TIME);
            }
        }, Configuration.REFRESH_TIME);

        ImageView home = findViewById(R.id.GoBackToMenu);
        searchForPlayers = findViewById(R.id.SearchForPlayers);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(MatchmakingActivity.this,HomeActivity.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
            }
        });


        searchForPlayers.setOnClickListener((v)->{
            if(!searching){
                if(HttpPoster.safePost(null, "GameManagement", "EnterMatchmaking", Configuration.currentUser.getId())){
                    searching = true;
                    searchForPlayers.setBackgroundTintList(ColorStateList.valueOf(0xFFFFF0A4));
                    searchForPlayers.setText("Searching for Players");
                }
                else System.out.println("Maxi: The server somehow did not want to enter the player into the matchmaking pool...");
            }
            else {
                if(HttpPoster.safePost(null, "GameManagement", "CancelMatchmaking", Configuration.currentUser.getId())){
                    searching = false;
                    searchForPlayers.setBackgroundTintList(ColorStateList.valueOf(0xFFDDDDDD));
                    searchForPlayers.setText("Search for Players!");
                }
                else System.out.println("Maxi: The server somehow did not want to remove the player from the matchmaking pool...");
            }
        });
    }

    private void checkIfMatchFound(){
        if(!searching) return;
        searchForPlayers.setText(searchText[searchTextIDX++]); //dot dot dot
        if(searchTextIDX >= searchText.length) searchTextIDX = 0; //dot dot dot
        String result = HttpGetter.safeGet(null, "GameManagement", "WasMatchFound", Configuration.currentUser.getId());
        if(result != null && !"no".equals(result)) {
            try{
                User opponent = User.fromJson(result);
                startGameLobbyActivity(opponent);
            } catch (JSONException e) {
                throw new RuntimeException("Couldn't parse opponent from server response:\n"+result);
            }
        }
    }

    private void startGameLobbyActivity(User opponent){
        //System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        searching = false;
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(MatchmakingActivity.this, ChooseDuelDeckActivity.class);
        intent.putExtra("Opponent", opponent);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(handler != null) handler.removeCallbacksAndMessages(null);
    }
}
