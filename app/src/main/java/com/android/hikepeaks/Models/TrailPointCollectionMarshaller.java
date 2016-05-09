package com.android.hikepeaks.Models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class TrailPointCollectionMarshaller implements DynamoDBMarshaller<ArrayList<TrailPoint>> {

    @Override
    public String marshall(ArrayList<TrailPoint> points) {
        Gson gson = new Gson();
        return gson.toJson(points);
    }

    @Override
    public ArrayList<TrailPoint> unmarshall(Class<ArrayList<TrailPoint>> clazz, String s) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<TrailPoint>>() {
        }.getType();
        return gson.fromJson(s, listType);
    }
}