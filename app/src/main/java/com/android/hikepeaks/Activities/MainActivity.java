package com.android.hikepeaks.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Factories.FragmentFactory;
import com.android.hikepeaks.Fragments.OnFragmentInteractionListener;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.BatteryBroadReceiver;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Tasks.SynchronizeAsyncTask;
import com.android.hikepeaks.Utilities.Connectivity;
import com.bumptech.glide.Glide;

import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener,
        SynchronizeAsyncTask.OnSynchronizeTaskCompleted {

    //Instance du fragment manager
    private FragmentManager mFragmentManager;
    private android.support.v4.app.FragmentTransaction mFragmentTransaction;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToogle;
    private NavigationView mNavigationView;
    private Toolbar toolbar;
    private static Fragment activeFragment;
    private AccountManager accountManager = AccountManager.getInstance();
    private TerminalManager terminalManager = TerminalManager.getInstance();
    private BatteryBroadReceiver batteryBroadReceiver = new BatteryBroadReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("EXPLORE");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Setup DrawerLayout et NavigationView
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToogle = setupDrawerToggle();
        mDrawerLayout.setDrawerListener(mDrawerToogle);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        //ajouter le premier fragment
        mFragmentManager = getSupportFragmentManager();
        //mDrawerLayout.openDrawer(GravityCompat.START);

        mNavigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();


        if(activeFragment == null) {
            if (Connectivity.isNetworkOnline(this)) {
                activeFragment = FragmentFactory.getInstance().getMainFragment();
            } else {
                activeFragment = FragmentFactory.getInstance().getProfileFragment();
            }
        }
        //activeFragment = FragmentFactory.getInstance().getMainFragment();


        //ajoute tous les fragments et les hides
        for (Fragment fragment : FragmentFactory.getInstance().getAllFragments()) {
                fragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .hide(fragment)
                        .commit();
        }
        //montre le premier fragment
        fragmentManager.beginTransaction()
                .show(activeFragment)
                .commit();


        setupAccountBox();
    }

    protected void setupAccountBox() {
        View headerLayout = mNavigationView.getHeaderView(0);
        TextView profileName = (TextView) headerLayout.findViewById(R.id.nav_profileName);
        TextView profileEmail = (TextView) headerLayout.findViewById(R.id.nav_profileEmail);

        if(accountManager.getAccount() != null) {
            String url = accountManager.getAccount().getPictureUrl();
            profileName.setText(accountManager.getAccount().getDisplayName());
            profileEmail.setText(accountManager.getAccount().getEmail());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToogle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // `onPostCreate` called when activity start-up is complete after `onStart()`
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToogle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToogle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //ORGANISATION DU CHANGEMENT DE VUES/FRAGMENTS DANS LA BARRE DE NAVIGATION
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            accountManager.disconnect();
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        } else {
            switchFragment(FragmentFactory.getInstance().getFragment(id));
        }
        item.setChecked(true);
        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    protected void switchFragment(Fragment toFragment) {
        if (activeFragment != toFragment) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.trans_left_in, R.anim.trans_left_out)
                    .hide(activeFragment)
                    .show(toFragment)
                    .commit();
            activeFragment = toFragment;

            TextView msg = (TextView) activeFragment.getActivity().findViewById(R.id.txt_notavailable);
            msg.setVisibility(View.GONE);
        }
    }

    public void onSwitchToFragmentView(Fragment toFragment) {
        switchFragment(toFragment);
    }

    public void onSwitchToMainFragmentView() {
        switchFragment(FragmentFactory.getInstance().getMainFragment());
    }

    public void onSwitchToMapFragmentView() {
        switchFragment(FragmentFactory.getInstance().getMapFragment());
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //battery stuff

        batteryBroadReceiver.clearNotifications(this);
    }
    @Override
    protected void onPause() {
        super.onPause();

        this.registerReceiver(batteryBroadReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    protected void onStop() {
        super.onStop();

        //Battery stuff
        if (isFinishing()) {
            this.registerReceiver(batteryBroadReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    public void OnSynchronizeTaskCompleted() {
        // update the cache
        terminalManager.updateActiveAccount(accountManager.getAccount());
    }
}
