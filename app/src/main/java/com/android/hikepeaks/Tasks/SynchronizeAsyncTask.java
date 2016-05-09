package com.android.hikepeaks.Tasks;

import android.os.AsyncTask;

import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.StorageService;
import com.android.hikepeaks.Services.SyncPool;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Synchronizer.ObjectSynchronizer;
import com.android.hikepeaks.Synchronizer.SyncObjectInterface;


public class SynchronizeAsyncTask extends AsyncTask<String, Void, Void> {

    TerminalManager terminalManager = TerminalManager.getInstance();
    StorageService storageService = StorageService.getInstance();
    DynamoDBManager dynamoDBManager = DynamoDBManager.getInstance();
    SyncPool syncPool = SyncPool.getInstance();
    private OnSynchronizeTaskCompleted listener;

    public SynchronizeAsyncTask() {

    }

    public OnSynchronizeTaskCompleted getListener() {
        return listener;
    }

    public SynchronizeAsyncTask setListener(OnSynchronizeTaskCompleted listener) {
        this.listener = listener;
        return this;
    }

    protected Void doInBackground(String... urls) {
//        String url = urls[0];
        ObjectSynchronizer synchronizer = new ObjectSynchronizer(storageService, dynamoDBManager, terminalManager);
        SyncObjectInterface object = syncPool.pollObject();
        while (object != null) {
            try {
                if (!object.isSynchronized())
                    synchronizer.sync(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            object = syncPool.pollObject();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (listener != null)
            listener.OnSynchronizeTaskCompleted();
    }

    public interface OnSynchronizeTaskCompleted {
        void OnSynchronizeTaskCompleted();
    }
}
