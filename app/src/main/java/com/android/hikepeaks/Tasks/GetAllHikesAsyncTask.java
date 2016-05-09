package com.android.hikepeaks.Tasks;

import android.os.AsyncTask;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Services.DynamoDBManager;

import java.util.ArrayList;


public class GetAllHikesAsyncTask extends AsyncTask<String, Void, ArrayList<Trail>> {

    DynamoDBManager dynamoDBManager;
    private OnGetAllHikesAsyncTaskTaskCompleted listener;

    public GetAllHikesAsyncTask(DynamoDBManager dynamoDBManager) {
        this.dynamoDBManager = dynamoDBManager;
    }

    public OnGetAllHikesAsyncTaskTaskCompleted getListener() {
        return listener;
    }

    public GetAllHikesAsyncTask setListener(OnGetAllHikesAsyncTaskTaskCompleted listener) {
        this.listener = listener;
        return this;
    }

    protected ArrayList<Trail> doInBackground(String... urls) {
        ArrayList<Trail> trails = new ArrayList<>();
        try{
            trails = dynamoDBManager.getAllTrails();
        }catch (Exception e){
            e.printStackTrace();
        }
        return trails;
    }

    protected void onPostExecute(ArrayList<Trail> trails) {
        if(listener != null)
            listener.OnGetAllHikesAsyncTaskTaskCompleted(trails);
    }

    public interface OnGetAllHikesAsyncTaskTaskCompleted {
        void OnGetAllHikesAsyncTaskTaskCompleted(ArrayList<Trail> trails);
    }
}
