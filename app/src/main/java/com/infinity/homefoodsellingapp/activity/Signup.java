package com.infinity.homefoodsellingapp.activity;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infinity.homefoodsellingapp.R;

public class Signup extends AppCompatActivity {

    private final String TAG = "Signup";

    //--toolbar
    private Toolbar toolbar;
    //--progress bar
    private ProgressBar progressBar;

    //--local variables
    EditText mEmail, mPassword, mUsername;

    //--Firebase Authentication
    FirebaseAuth mAuth;
    //--Firebase Database Referencec
    DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //--initialize and add toolbar in activity
        initToolbar();
        //--asign id to variable
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        //--set progress bar visibility GONE
        progressBar.setVisibility(View.GONE);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mUsername = (EditText) findViewById(R.id.username);

        //--initialize firebase authentication instance
        mAuth = FirebaseAuth.getInstance();
        //--initialize firebase database reference instance
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


    }


    //--adding toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        //--if toolbar is set a default than add home back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }


    //--handle toolbar menu events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //--if user press the back button, close the current activity
        //--and go back to previous activity that is in stack
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    //--signup user with credendials (email & password)
    //--save details to firebase database
    public void signupWithCredentials(View view) {
        if (validateCredentials(mEmail.getText().toString(), mPassword.getText().toString(), mUsername.getText().toString())) {
            //--set progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //--if the task is successful, save the user signup details to firebase database
                            //--else log error
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE); //hide the progress bar
                                //get current user from firebase auth
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                //save current user information to databse
                                if (currentUser != null) {
                                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("username").setValue(mUsername.getText().toString());
                                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("email").setValue(mEmail.getText().toString());
                                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("photo_url").setValue("null");
                                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("isLoggedIn").setValue(false);
                                }

                                Toast.makeText(Signup.this, "Thankyou for Registeration", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE); //hide progress bar
                                Log.d(TAG, task.getException().getMessage());
                                Toast.makeText(Signup.this, "Please check you internet connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    //--validate user credentials (email and password)
    private boolean validateCredentials(String sEmail, String sPass, String sUsername) {
        //--return false if email field is empty or contain email address incorrect
        if (TextUtils.isEmpty(sUsername)) {
            mEmail.setError("Please enter username");
            return false;
        }

        //--return false if username length is < 2
        if (sUsername.length() < 2) {
            mEmail.setError("Username lenght must be greater than 2 letters");
            return false;
        }

        //--return false if email field is empty or contain email address incorrect
        if (TextUtils.isEmpty(sEmail) || sEmail.contains(" ") || !sEmail.contains("@") || !sEmail.endsWith(".com")) {
            mEmail.setError("Please enter correct format e.g xxxx@gmail.com");
            return false;
        }

        //--return false if password field is empty or less than 5 digits
        if (TextUtils.isEmpty(sPass) || sPass.length() < 5) {
            mPassword.setError("minimum password length 5 digits");
            return false;
        }
        return true;
    }
}
