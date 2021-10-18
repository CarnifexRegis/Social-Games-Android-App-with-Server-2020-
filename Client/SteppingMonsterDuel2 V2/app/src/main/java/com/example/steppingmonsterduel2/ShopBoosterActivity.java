package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ShopBoosterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_step);

        Button home = findViewById(R.id.homeButtonBoosterShop);
        Button buy = findViewById(R.id.BuyPack);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(ShopBoosterActivity.this,HomeActivity.class);
                startActivity(home);
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openPacks = new Intent(ShopBoosterActivity.this,OpenPacksActivity.class);
                startActivity(openPacks);
            }
        });
    }
}
