package com.android.hikepeaks;

import android.app.Application;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.StorageService;
import com.android.hikepeaks.Services.TerminalManager;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                this,
                "us-east-1:8762f9c0-a3dd-4a15-80a6-fd1b4eefb3d6", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        DynamoDBManager.getInstance().setCredentialsProvider(credentialsProvider).init();
        // Initialize S3 storage
        StorageService.initInstance(this);
        // Initialise Terminal
        TerminalManager.getInstance().setContext(this);
        // Initialise AccountManager
        AccountManager.getInstance();
    }
}
