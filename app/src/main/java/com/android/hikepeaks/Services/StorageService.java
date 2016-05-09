package com.android.hikepeaks.Services;

import android.content.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class StorageService {

    protected static StorageService INSTANCE = null;
    private final Regions region = Regions.US_WEST_2;
    private String bucketName = "hikepeaks";
    private AmazonS3 service;
    private Context context;
    private TransferListener transferListener;

    protected StorageService(Context context, String accessKey, String secrectKey) {
        if (context == null)
            throw new NullPointerException("context can not be null");

        this.context = context;
        this.service = new AmazonS3Client(new BasicAWSCredentials(accessKey, secrectKey));
        this.service.setRegion(Region.getRegion(region));

    }

    public static StorageService getInstance() {
        return initInstance(null, null, null);
    }

    public static StorageService initInstance(Context context, String accessKey, String secrectKey) {
        if (INSTANCE == null) {
            synchronized (StorageService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StorageService(context, accessKey, secrectKey);
                }
            }
        }
        return INSTANCE;
    }

    public TransferListener getTransferListener() {
        return transferListener;
    }

    public void setTransferListener(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    public void downloadFileAsync(String remoteFilename, File localeTargetFile) {
        TransferUtility transferUtility = new TransferUtility(this.service, this.context);
        TransferObserver transferObserver = transferUtility.download(this.bucketName, remoteFilename, localeTargetFile);
        if (this.transferListener != null)
            transferObserver.setTransferListener(this.transferListener);
    }

    public void downloadFile(String remoteFilename, File localeTargetFile) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, remoteFilename);
        service.getObject(getObjectRequest, localeTargetFile);
    }

    public String uploadFile(File localeFile, String remoteFileName) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, remoteFileName, localeFile);
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        PutObjectResult result = service.putObject(putObjectRequest);
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, remoteFileName);
        URL url = service.generatePresignedUrl(urlRequest);
        String link = url.toString();
        if (link.contains("?")) {
            link = link.substring(0, link.lastIndexOf('?'));
        }

        return link;
    }

    public ArrayList<String> listFiles(String folder) {
        ObjectListing objects = service.listObjects(bucketName, folder);
        ArrayList<String> objectNames = new ArrayList<>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
        while (oIter.hasNext()) {
            objectNames.add(oIter.next().getKey());
        }
        while (objects.isTruncated()) {
            objects = service.listNextBatchOfObjects(objects);
            oIter = objects.getObjectSummaries().iterator();
            while (oIter.hasNext()) {
                objectNames.add(oIter.next().getKey());
            }
        }
        return objectNames;
    }
}
