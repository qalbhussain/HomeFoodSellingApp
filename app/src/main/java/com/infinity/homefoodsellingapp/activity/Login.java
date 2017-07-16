package com.infinity.homefoodsellingapp.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infinity.homefoodsellingapp.R;

import java.io.IOException;
import java.util.Arrays;

import static android.view.View.GONE;

public class Login extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //--constant
    private final String TAG_LOGIN = "LOGIN";
    private final int RC_SIGN_IN = 0b1001;

    //--Firebase Authentication object
    FirebaseAuth mAuth;
    //--Firebase database object
    DatabaseReference mDatabaseReference;

    //--local variables
    private EditText mEmail, mPassword;
    private Button mBtnLogin;

    //login button facebook/gmail
    private Button mBtnGmail, mBtnFacebook;

    //--toolbar
    private Toolbar toolbar;

    //--progress bar
    private ProgressBar progressBar;

    //GoogleApiClient instance
    GoogleApiClient mGoogleApiClient;
    //Google SignOption instance
    GoogleSignInOptions gso;

    //--Callback Manager for facebook
    CallbackManager callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        //initialize and add toolbar in activity
        initToolbar();

        //--assigning id's references to local variables
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        //--set progress bar visibility GONE
        progressBar.setVisibility(GONE);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mBtnLogin = (Button) findViewById(R.id.login);
        mBtnGmail = (Button) findViewById(R.id.signin_gmail);
        mBtnFacebook = (Button) findViewById(R.id.signin_facebook);

        //--Initialize firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //--Initialize firebase database reference instance
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        //--configure Google Signin
        GoogleSignInOptionsConfig();
        //--configure GoogleApiClient
        GoogleApiClientConfig();

        //--initialize facebook login callback
        initFacebookLoginCallback();


        //--setOnClickListener
        mBtnLogin.setOnClickListener(this);
        mBtnGmail.setOnClickListener(this);
        mBtnFacebook.setOnClickListener(this);

    }

    //--initialize facebook login callBack Manager
    private void initFacebookLoginCallback() {
        new CallbackManager.Factory();
        callback = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callback, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signInUserWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                    progressBar.setVisibility(GONE);
                Toast.makeText(Login.this, "Facebook login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }


    //--configure GoogleApiClient for using google play services
    private void GoogleApiClientConfig() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .build();
    }

    //--configure google signin & what info will be need from user's gmail account e.g. email-image etc
    private void GoogleSignInOptionsConfig() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
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


    //--Button onCLick listener event
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                //--login user using credentials
                signInUserWithCredentials();
                break;
            case R.id.signin_gmail:
                //--display gmail account dialog to user, so he can select the one he wants to sign in with
                signInIntent();
                break;
            case R.id.signin_facebook:
                progressBar.setVisibility(View.VISIBLE);
                //--sign in using facebook credentials, by passing permissions .
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                break;
            default:
                Log.d(TAG_LOGIN, "Incorrect button pressed");
        }

    }


    //--this will prompt a user to select a google account to sign in with
    private void signInIntent() {
        //always clear previous sign in account
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    //--signIn user to firebase using his/her credentials (Email and password)
    private void signInUserWithCredentials() {
        String sEmail = mEmail.getText().toString();
        String sPass = mPassword.getText().toString();

        if (validateCredentials(sEmail, sPass)) {
            //--set progress bar to visible
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(sEmail, sPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(GONE); //hide the progress bar
                                //user login successfull
                                Intent i = new Intent(getApplicationContext(), Home.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                progressBar.setVisibility(GONE); //hide the progress bar
                                //user login failed
                                Toast.makeText(Login.this, "login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    //--Sign in user with gmail account.
    //--If successful - save the details to firebase databse
    public void signInUserWithGmail(GoogleSignInAccount signInAccount) {
        //--set progress bar visible
        progressBar.setVisibility(View.VISIBLE);

        //--get user credential from signInAccount
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

        //--signIn with credentials and save the information to database
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        isSigninSuccessful(task);
                    }
                });

    }


    //--once the user logged in to facebook, signin to firebase using the information  and
    //--save user information to firebase
    private void signInUserWithFacebook(AccessToken accessToken) {
        AuthCredential credentials = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        isSigninSuccessful(task);
                    }
                });
    }

    //--if sign in is Successful, than save user details to firebase database
    //--else, display a toast
    private void isSigninSuccessful(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            //--TODO get the list and check whether current usre exists or not. else login

            //--save current user information to databse
            if (currentUser != null) {
                try {
                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("username").setValue(currentUser.getDisplayName());
                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("email").setValue(currentUser.getEmail());
                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("photo_url").setValue(currentUser.getPhotoUrl().toString());
                    mDatabaseReference.child("Users").child(currentUser.getUid()).child("isLoggedIn").setValue(false);
                } catch (Exception ex) {
                    Toast.makeText(Login.this, "Problem fetching user's info", Toast.LENGTH_SHORT).show();
                }
            }

            Toast.makeText(Login.this, "saved", Toast.LENGTH_SHORT).show();
            //--hide the progress bar
            progressBar.setVisibility(GONE);
            //--start new intent, Open user's home activity
            Intent i = new Intent(getApplicationContext(), Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            //--destroy this activity
            //--this will call onDestroy Method
            finish();
        } else {
            //--let user know that google signIn failed
            //--hide the progress bar
            progressBar.setVisibility(GONE);
            Toast.makeText(Login.this, "login failed", Toast.LENGTH_SHORT).show();
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
        if (TextUtils.isEmpty(sPass) || sPass.length() < 5) {
            mPassword.setError("minimum password length 5 digits");
            return false;
        }
        return true;
    }

    //-- GoogleApiClient Callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //--Wo-oh connection establish
        Toast.makeText(this, "mGoogleApiClient: connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    //---------------ACTIVITY METHODS----------------//


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess()) {
                GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
                signInUserWithGmail(signInAccount);
            }
        }

        //if login with facebook.
        //pass the activity results to callback
        callback.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //--connect googleApiClient to Google Play Services
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //--disconnect googleAPiClient
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (progressBar.isEnabled()) {
            progressBar.setVisibility(GONE);
        }
    }
}