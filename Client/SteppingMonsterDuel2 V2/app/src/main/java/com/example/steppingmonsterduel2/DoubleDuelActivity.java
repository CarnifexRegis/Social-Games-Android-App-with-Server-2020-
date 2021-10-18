package com.example.steppingmonsterduel2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DoubleDuelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_duel);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        DialogInterface.OnClickListener toHomeMenu = (d, w)->{
            Intent intent = new Intent(DoubleDuelActivity.this, HomeActivity.class);
            startActivity(intent);
        };

        new AlertDialog.Builder(DoubleDuelActivity.this)
            .setTitle("Activity Removed!")
            .setMessage("Maxi removed this activity and the package it used (\"ServerSide\") in order to reduce confusion.")
            .setNegativeButton("OK", toHomeMenu)
            .setPositiveButton( "OK", toHomeMenu).show();
    }
}