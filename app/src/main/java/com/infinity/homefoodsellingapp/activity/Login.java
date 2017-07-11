package com.infinity.homefoodsellingapp.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.infinity.homefoodsellingapp.R;

public class Login extends AppCompatActivity
        implements View.OnClickListener {

    //--constant
    private final String TAG_LOGIN = "LOGIN";

    //--Firebase Authentication
    FirebaseAuth mAuth;

    //--local variables
    private EditText mEmail, mPassword ;
    private Button mBtnLogin ;

    //--toolbar
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //initialize and add toolbar in activity
        initToolbar();

        //--assigning id's references to local variables
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mBtnLogin = (Button) findViewById(R.id.login);

        //Initialize firebase auth
        mAuth = FirebaseAuth.getInstance();

        //setOnClickListener
        mBtnLogin.setOnClickListener(this);
    }


    //--adding toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        //--if toolbar is set a default than add home back button
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }


    //--handle toolbar menu events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //--if user press the back button, close the current activity
        //--and go back to previous activity that is in stack
        if (item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    //--onCLick listener
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login : signInUserWithCredentials();
                break;
            default:
                Log.d(TAG_LOGIN,"Incorrect button pressed");
        }

    }


    //--signIn user to firebase using his/her credentials (Email and password)
    private void signInUserWithCredentials() {
        String sEmail = mEmail.getText().toString();
        String sPass = mPassword.getText().toString();

        if (validateCredentials(sEmail, sPass)) {
            mAuth.signInWithEmailAndPassword(sEmail, sPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //user login successfull
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                //user login failed
                                Toast.makeText(Login.this, "login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    //--validate user credentials (email and password)
    private boolean validateCredentials(String sEmail, String sPass) {
        //--return false if email field is empty or contain email address incorrect
        if (TextUtils.isEmpty(sEmail) || sEmail.contains(" ") || !sEmail.contains("@") || !sEmail.endsWith(".com")) {
            mEmail.setError("Please enter correct format e.g xxxx@gmail.com");
            return false;
        }

        //--return false if password field is empty or less than 5 digits
        if (TextUtils.isEmpty(sPass) || sPass.length()<5) {
            mPassword.setError("minimum password length 5 digits");
            return false;
        }
        return true;
    }

}