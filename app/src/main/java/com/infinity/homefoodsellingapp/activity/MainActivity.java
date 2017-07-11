package com.infinity.homefoodsellingapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.infinity.homefoodsellingapp.R;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private final String TAG = "MainActivity";

    //--local variables
    private Button mSignup, mSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSignin = (Button) findViewById(R.id.signin);
        mSignup = (Button) findViewById(R.id.signup);

        //set click listeners to button
        mSignup.setOnClickListener(this);
        mSignin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signin : loginActivity();
                break;
            case R.id.signup : signupActivity();
                break;

            default:
                Log.d(TAG,"Not exist in Mainactivity");
        }
    }

    //--switch to signup activity
    private void signupActivity() { startActivity(new Intent(this, Signup.class)); }

    //--switch to login activity
    private void loginActivity() {
        startActivity(new Intent(this, Login.class));
    }
}
