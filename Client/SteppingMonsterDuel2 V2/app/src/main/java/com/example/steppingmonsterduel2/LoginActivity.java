package com.example.steppingmonsterduel2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.Util.Configuration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializes the variables
        Button loginButton = findViewById(R.id.RegisterButton);
        TextView registerField = findViewById(R.id.LoginView);
        final EditText email = findViewById(R.id.EmailField);
        passwordView = findViewById(R.id.PasswordField);

        if(Configuration.mAuth==null) { //initialize firebase instance
            Configuration.mAuth = FirebaseAuth.getInstance();
        }

        if (Configuration.fbUser != null) //automatically continue to main menu if a firebase user is already logged in.
        {
            Intent mainMenu = new Intent(LoginActivity.this,HomeActivity.class);
            startActivity(mainMenu);
        }

        //Checks if requirements are valid to login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailField = email.getText().toString();
                String passwordField = passwordView.getText().toString();
                //check whether the email and password are legal. Only super basic checks.
                if(emailField.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Email is empty!",Toast.LENGTH_SHORT).show();
                } else if(passwordField.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Password is empty!",Toast.LENGTH_SHORT).show();
                } else{
                    logIn(emailField,passwordField);
                }
            }
        });

        registerField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerMenu = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(registerMenu);
            }
        });
    }

    //Logs in the user sets the firebase Information to the fbUser and jumps to homeactivity
    //if there was a failure nothing happens
    private void logIn(String mail, String password)
    {
        OnSuccessListener<AuthResult> onSuccess = (result)->{
            Configuration.fbUser = Configuration.mAuth.getCurrentUser();
            Toast.makeText(LoginActivity.this,"Logged in successfully!",Toast.LENGTH_SHORT).show();
            Intent toMainMenu = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(toMainMenu);
        };
        OnFailureListener onFailure = (e)->{
            passwordView.setText(null);
            Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        };
        Configuration.mAuth.signInWithEmailAndPassword(mail, password).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }


    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
