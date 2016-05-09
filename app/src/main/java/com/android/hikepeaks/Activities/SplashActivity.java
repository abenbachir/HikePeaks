package com.android.hikepeaks.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.android.hikepeaks.Models.Account;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.BatteryBroadReceiver;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.SyncPool;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Tasks.SynchronizeAsyncTask;
import com.android.hikepeaks.Utilities.Connectivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.File;
import java.util.ArrayList;


public class SplashActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SynchronizeAsyncTask.OnSynchronizeTaskCompleted {

    //Timer pour le splash screen
    private static final int RC_SIGN_IN = 0;
    private static int SPLASH_TIME_OUT = 2000;
    private SignInButton btnSignIn;
    private GoogleApiClient mGoogleApiClient;
    private AccountManager accountManager = AccountManager.getInstance();
    private TerminalManager terminalManager = TerminalManager.getInstance();
    private boolean signInClicked = false;
    private boolean mIntentInProgress;
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignIn.setVisibility(View.GONE);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickSignIn(v);
            }
        });

        if (!accountManager.isConnected()) {
            mGoogleApiClient = buildGoogleAPIClient();
            accountManager.setGoogleApiClient(mGoogleApiClient);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchToMainActivity();
                }
            }, SPLASH_TIME_OUT);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!accountManager.isConnected())
            mGoogleApiClient.connect();

        if (!isChangingConfigurations()) {
            //battery stuff
            BatteryBroadReceiver batteryBroadReceiver = new BatteryBroadReceiver();
            this.registerReceiver(batteryBroadReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }

    private void switchToMainActivity() {
        // get trails from cache to sync

        Account account = terminalManager.getActiveAccount();
        if (SyncPool.getInstance().isEmpty() && account != null) {
            ArrayList<Trail> trails = account.getTrails();
            for (Trail trail : trails) {
                if (!trail.isSynchronized())
                    SyncPool.getInstance().queueObject(trail);
            }
            if (!SyncPool.getInstance().isEmpty() && Connectivity.isNetworkOnline(this)) {
                new SynchronizeAsyncTask().setListener(this).execute();
            }
        }

        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    //Initialisation du Client Google plus --> Permet de choisir le compte qui est déjà connecté au cell ou autre
    private GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    @NonNull
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (signInClicked) {
                //déjà connecté
                resolveSignInError();
            }
        }
        btnSignIn.setEnabled(true);
        btnSignIn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                signInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnected(Bundle arg0) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        updateInternetBuilder(builder);
        signInClicked = false;

        if (Connectivity.isNetworkOnline(getApplicationContext())) {
            Account account = DynamoDBManager.getInstance().findAccountByEmail(accountManager.getPersonEmail());
            if (account != null) {
                accountManager.setAccount(account);
            } else {
                accountManager.updateAccount();
                // update database avec les informations du profile
                DynamoDBManager.getInstance().saveAccount(accountManager.getAccount());
            }
            // save cache
            terminalManager.updateActiveAccount(account);

            switchToMainActivity();
        } else if (terminalManager.existActiveAccount()) {
            Account account = terminalManager.getActiveAccount();

            if (account != null){
                accountManager.setAccount(account);
            } else {
                builder.show();
            }
            switchToMainActivity();

        } else {
            builder.show();
        }
    }

    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void updateControls() {
        btnSignIn.setVisibility(accountManager.isConnected() ? View.GONE : View.VISIBLE);
    }


    private void OnClickSignIn(View v) {
        if (!mGoogleApiClient.isConnecting()) {
            signInClicked = true;
            resolveSignInError();
            updateControls();
        }
        btnSignIn.setEnabled(false);
    }

    //erreurs dans le signin
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void OnSynchronizeTaskCompleted() {
        Toast toast = Toast.makeText(this, "offline data been synced", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 20);
        toast.show();
    }

    public AlertDialog.Builder updateInternetBuilder(AlertDialog.Builder builder){

        builder.setMessage("You need to be connected to Internet the first time you are using this application")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

        return builder;
    }
}
