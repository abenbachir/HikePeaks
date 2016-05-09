package com.android.hikepeaks.Synchronizer;


import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Models.TrailCapture;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.StorageService;
import com.android.hikepeaks.Services.TerminalManager;

import java.io.File;

public class ObjectSynchronizer {

    private TerminalManager terminalManager;
    private StorageService storageService;
    private DynamoDBManager dbManager;

    public ObjectSynchronizer(StorageService storageService, DynamoDBManager dbManager, TerminalManager terminalManager) {
        this.storageService = storageService;
        this.dbManager = dbManager;
        this.terminalManager = terminalManager;
    }

    public void sync(SyncObjectInterface object) {
        if (object instanceof Trail) {
            this.sync((Trail) object);
        }
    }

    public void sync(Trail trail) {

        // upload images to S3
        for (TrailCapture capture : trail.getCaptures()) {
            File fileImage = new File(capture.getPicturePath());
            if (fileImage.exists()) {
                String url = storageService.uploadFile(fileImage, terminalManager.getMediaDir().getName() + "/" + fileImage.getName());
                capture.setPicturePath(url);
            }
        }

        trail.setIsSynchronized(true);
        // update database
        dbManager.saveTrail(trail);
    }
}
