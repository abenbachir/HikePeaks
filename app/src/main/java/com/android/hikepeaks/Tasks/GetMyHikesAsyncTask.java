package com.android.hikepeaks.Tasks;

import android.os.AsyncTask;

import com.android.hikepeaks.Models.Account;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Services.DynamoDBManager;

import java.util.ArrayList;


public class GetMyHikesAsyncTask extends AsyncTask<String, Void, ArrayList<Trail>> {

    DynamoDBManager dynamoDBManager;
    private OnGetMyHikesAsyncTaskCompleted listener;
    private Account account;

    public GetMyHikesAsyncTask(DynamoDBManager dynamoDBManager, Account account) {
        this.dynamoDBManager = dynamoDBManager;
        this.account = account;
    }

    public OnGetMyHikesAsyncTaskCompleted getListener() {
        return listener;
    }

    public GetMyHikesAsyncTask setListener(OnGetMyHikesAsyncTaskCompleted listener) {
        this.listener = listener;
        return this;
    }

    protected ArrayList<Trail> doInBackground(String... urls) {
        ArrayList<Trail> trails = new ArrayList<>();
        try{
            trails = dynamoDBManager.findTrailForAccount(account);
        }catch (Exception e){
            e.printStackTrace();
        }
        return trails;
    }

    protected void onPostExecute(ArrayList<Trail> trails) {
        if(listener != null)
            listener.OnGetMyHikesAsyncTaskCompleted(trails);
    }

    public interface OnGetMyHikesAsyncTaskCompleted {
        void OnGetMyHikesAsyncTaskCompleted(ArrayList<Trail> trails);
    }
}
