package com.infinity.homefoodsellingapp.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.infinity.homefoodsellingapp.R;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //--side Navigation view
    NavigationView mNavigationView;
    //--Drawerlayout
    DrawerLayout mDrawerLayout;
    //--action bar drawer toggle hamburger icon
    ActionBarDrawerToggle actionBarDrawerToggle;

    //--toolbar
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //--initialize and add toolbar in activity
        initToolbar();

        //--asign id references to variables
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        //--initialize actionBar drawer toggle
        initActionBarDrawer();

        //--set item selected Listener to our navigation view
        mNavigationView.setNavigationItemSelectedListener(this);
    }


    //--initialize action bar hamburger icon
    private void initActionBarDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }


    //--adding toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
    }


    //--perform specific actions when a user press an item in navigation view
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
