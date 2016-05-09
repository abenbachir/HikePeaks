package com.android.hikepeaks.Services;


import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.android.hikepeaks.Models.Account;
import com.android.hikepeaks.Models.Trail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBManager {

    protected static DynamoDBManager INSTANCE = null;
    protected Context context = null;
    protected CognitoCachingCredentialsProvider credentialsProvider;
    protected AmazonDynamoDBClient ddbClient;

    public static DynamoDBManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DynamoDBManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DynamoDBManager();
                }
            }
        }
        return INSTANCE;
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public DynamoDBManager setCredentialsProvider(CognitoCachingCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public void init() {
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
    }

    public Account findAccountByEmail(String email) {
        Account account = null;
        //Object mapper
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":email", new AttributeValue().withS(email));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withLimit(1)
                .withFilterExpression("Email = :email")
                .withExpressionAttributeValues(expressionAttributeValues);

        List<Account> result = mapper.scan(Account.class, scanExpression);
        if (result.size() > 0) {
            account = result.get(0);
            ArrayList<Trail> trails = findTrailForAccount(account);
            if (trails != null)
                account.setTrails(trails);
        }

        return account;
    }

    public ArrayList<Trail> exploreOtherHikes(String myuserId) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        List<Trail> result = mapper.scan(Trail.class, scanExpression);
        ArrayList<Trail> trails = new ArrayList<>();
        for(Trail trail : result){
            if(myuserId.equals(trail.getAccountId()))
                continue;
            try {
                Account account = mapper.load(Account.class, trail.getAccountId());
                trail.setAccount(account);
                trails.add(trail);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return trails;
    }
    public ArrayList<Trail> getAllTrails() {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        List<Trail> result = mapper.scan(Trail.class, scanExpression);
        ArrayList<Trail> trails = new ArrayList<>();
        for(Trail trail : result){
            Account account = mapper.load(Account.class, trail.getAccountId());
            trail.setAccount(account);
            trails.add(trail);
        }

        return trails;
    }

    public ArrayList<Trail> findTrailForAccount(Account account) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":accountId", new AttributeValue().withS(account.getId()));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("AccountId = :accountId")
                .withExpressionAttributeValues(expressionAttributeValues);

        List<Trail> result = mapper.scan(Trail.class, scanExpression);
        for(Trail trail : result){
            trail.setAccount(account);
        }
        Trail[] array = {};
        array = result.toArray(array);
        return new ArrayList<>(Arrays.asList(array));
    }

    public Trail findTrailById(String trailId) {
        Trail trail = null;
        //Object mapper
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", new AttributeValue().withS(trailId));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withLimit(1)
                .withFilterExpression("Id = :id")
                .withExpressionAttributeValues(expressionAttributeValues);

        List<Trail> result = mapper.scan(Trail.class, scanExpression);
        if (result.size() > 0) {
            trail = result.get(0);
        }

        return trail;
    }

    public void saveAccount(Account account) {
        //Object mapper
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        mapper.save(account);
    }

    public void saveTrail(Trail trail) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        mapper.save(trail);
    }
    public void deleteTrail(Trail trail) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        mapper.delete(trail);

//        Map<String, AttributeValue> params = new HashMap<>();
//        params.put("Id", new AttributeValue(trail.getId()));
//        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
//                .withTableName("Trails")
//                .withKey(params);
//        ddbClient.deleteItem(deleteItemRequest);
    }
}
