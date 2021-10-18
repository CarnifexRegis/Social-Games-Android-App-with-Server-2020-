package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        final Button gameDecision = findViewById(R.id.GameDecision);



        gameDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gamedec = new Intent(GameActivity.this, GameDecisionActivity.class);
                startActivity(gamedec);
            }
        });
    }


}
