package com.infinity.homefoodsellingapp.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.infinity.homefoodsellingapp.R;
import com.infinity.homefoodsellingapp.fragments.NearbyChef;

public class Home extends AppCompatActivity {

    //--side Navigation view
    NavigationView mNavigationView;
    //--Drawerlayout
    DrawerLayout mDrawerLayout;
    //--action bar drawer toggle hamburger icon
    ActionBarDrawerToggle actionBarDrawerToggle;

    //--toolbar
    private Toolbar toolbar;

    //--toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    private Handler mHandler;

    //--index to identify current nav menu item
    public static int navItemIndex = 0;

    //--tags used to attach the fragments
    private static final String TAG_SEARCH = "search_cook";
    private static final String TAG_BE_CHEF = "be_chef";
    private static final String TAG_INBOX = "inbox";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_FAVOURITE = "favourite";
    public static String CURRENT_TAG = TAG_SEARCH;

    //--flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;

    //--firebase authentication instance
    FirebaseAuth mAuth;
    //--current user
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //--initialize and add toolbar in activity
        initToolbar();

        mHandler = new Handler();

        //--initialize firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //--asign id references to variables
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        //--load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        //--initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_SEARCH;
            loadHomeFragment();
        }

    }


    //--initialize action bar drawer
    private void initActionBarDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);

        //--Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //--calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }


    //--adding toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
    }


    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_search_cook_nearby:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_SEARCH;
                        break;
                    case R.id.nav_be_a_chef:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_BE_CHEF;
                        break;
                    case R.id.nav_inbox:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_INBOX;
                        break;
                    case R.id.nav_notification:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_NOTIFICATIONS;
                        break;
                    case R.id.nav_fav:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_FAVOURITE;
                        break;
                    case R.id.nav_setting:
                        //TODO settings
                        break;
                    case R.id.sign_out:
                        //TODO signout
                        if (currentUser != null) {
                            mAuth.signOut();
                            Intent i = new Intent(getApplicationContext(), Login.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            //--destroy this activity
                            //--this will call onDestroy Method
                            finish();
                        }
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });
        initActionBarDrawer();
    }


    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawerLayout.closeDrawers();

            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }


        //Closing drawer on item click
        mDrawerLayout.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                NearbyChef nearbyChef = new NearbyChef();
                return nearbyChef;
            default:
                return new NearbyChef();
        }
    }

    private void selectNavMenu() {
        mNavigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    //----------------------ACTIVITY METHOD----------------------//

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_SEARCH;
                loadHomeFragment();
                return;
            }
        }
        super.onBackPressed();
    }

}
