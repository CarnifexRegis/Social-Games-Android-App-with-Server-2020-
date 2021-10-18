package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Util.Configuration;
import com.example.steppingmonsterduel2.Util.HttpPoster;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailView;
    private EditText usernameView;
    private EditText passwordView;
    private EditText passwordView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //set layout views
        Button register = findViewById(R.id.RegisterButton);
        TextView loginView = findViewById(R.id.LoginView);
        //Initializing variables to access them
        emailView = findViewById(R.id.EmailRegistrationField);
        usernameView = findViewById(R.id.Username);
        passwordView = findViewById(R.id.PasswordRegistrationField);
        passwordView2 = findViewById(R.id.PasswordRegistrationField2);

        if (Configuration.mAuth == null) { //initialize firebase instance
            Configuration.mAuth = FirebaseAuth.getInstance();
        }

        //Checking if everything is fulfilled to create the user
        //If not user gets informed
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = emailView.getText().toString();
                String usernameText = usernameView.getText().toString();
                String passwordText = passwordView.getText().toString();
                String passwordText2 = passwordView2.getText().toString();
                if (emailText.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Registration failed!\nEmail is empty!", Toast.LENGTH_LONG).show();
                } else if (usernameText.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Registration failed!\nUsername is empty!", Toast.LENGTH_LONG).show();
                } else if (passwordText.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Registration failed!\nPassword is empty!", Toast.LENGTH_LONG).show();
                } else if (!(passwordText.equals(passwordText2))) {
                    Toast.makeText(RegisterActivity.this, "Registration failed!\nPasswords don't match!", Toast.LENGTH_LONG).show();
                } else {
                    register(emailText, usernameText, passwordText);
                }
            }
        });

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });
    }


    private void OnFailure(Exception e){
        passwordView.setText(null);
        passwordView2.setText(null);
        Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_LONG).show();
    }

    //If server down jump to login
    private void OnGameServerFailure(){
        Toast.makeText(RegisterActivity.this, "Firebase user was created but the game server was down. Try to log in again.", Toast.LENGTH_LONG).show();
        Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(toLogin);
    }

    private void register(String email, final String username, String password) {

        //This is called once firebase successfully creates the new user.
        OnSuccessListener<AuthResult> onSuccess = (result)->{
            Configuration.fbUser = Configuration.mAuth.getCurrentUser();

            //The user is created with email and password only. Their username has to be updated.
            UserProfileChangeRequest usernameUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            //this is called once firebase successfully sets the new username. Only then is the main menu activity started
            OnSuccessListener<Void> onSetUsername = (empty)->{
                if(HttpPoster.safePost(this::OnFailure, "insert", Configuration.fbUser.getUid(), username, Configuration.fbUser.getEmail(), "user")){
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    Intent toMainMenu = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(toMainMenu);
                }
                else OnGameServerFailure();
            };

            Configuration.fbUser.updateProfile(usernameUpdate).addOnSuccessListener(onSetUsername).addOnFailureListener(this::OnFailure);
        };

        Configuration.mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(onSuccess).addOnFailureListener(this::OnFailure);
    }
}
