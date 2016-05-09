package com.android.hikepeaks.Models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class TrailCaptureCollectionMarshaller implements DynamoDBMarshaller<ArrayList<TrailCapture>> {

    @Override
    public String marshall(ArrayList<TrailCapture> captures) {
        Gson gson = new Gson();
        return gson.toJson(captures);
    }

    @Override
    public ArrayList<TrailCapture> unmarshall(Class<ArrayList<TrailCapture>> clazz, String s) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<TrailCapture>>() {
        }.getType();
        return gson.fromJson(s, listType);
    }

}