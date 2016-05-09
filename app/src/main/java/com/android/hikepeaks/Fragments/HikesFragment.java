package com.android.hikepeaks.Fragments;

import com.android.hikepeaks.Models.Account;
import com.android.hikepeaks.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Activities.HikeDetailsActivity;
import com.android.hikepeaks.Adapters.HikesListViewAdapter;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Tasks.GetMyHikesAsyncTask;
import com.android.hikepeaks.Tasks.SynchronizeAsyncTask;
import com.android.hikepeaks.Utilities.Connectivity;

import java.util.ArrayList;


public class HikesFragment extends BaseFragment implements GetMyHikesAsyncTask.OnGetMyHikesAsyncTaskCompleted {

    private DynamoDBManager dynamoDBManager = DynamoDBManager.getInstance();
    private ListView hikesListView;

    public HikesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hikes, container, false);


        hikesListView = (ListView) view.findViewById(R.id.hikesListView);

        hikesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Trail item = (Trail) adapter.getItemAtPosition(position);

                // use this to prevent contacting retreiving item from database
                HikeDetailsActivity.CurrentTrail = item;
                Intent intent = new Intent(getContext(), HikeDetailsActivity.class);
                intent.putExtra("trail", item.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        // Delete feature desactivated
//        hikesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long l) {
//                final Trail item = (Trail) adapter.getItemAtPosition(position);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//
//                builder.setMessage("Do you want to delete this hike ?")
//                        .setNeutralButton("Cancel", null)
//                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                try {
//                                    if (isNetworkAvailable()) {
//                                        dynamoDBManager.deleteTrail(item);
//                                        new SynchronizeAsyncTask().setListener((SynchronizeAsyncTask.OnSynchronizeTaskCompleted) getActivity()).execute();
//                                        Toast.makeText(getContext(), "Your hike will be deleted in a few moments", Toast.LENGTH_LONG).show();
//                                    }
//                                }catch(Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        })
//                        .show();
//
//                return true;
//            }
//
//        });

        if(Connectivity.isNetworkOnline(getContext())){
            new GetMyHikesAsyncTask(dynamoDBManager, AccountManager.getInstance().getAccount())
                    .setListener(this)
                    .execute();
        }else{
            OnGetMyHikesAsyncTaskCompleted(TerminalManager.getInstance().getActiveAccount().getTrails());
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    public void OnGetMyHikesAsyncTaskCompleted(ArrayList<Trail> trails)
    {
        try {
            HikesListViewAdapter customAdapter = new HikesListViewAdapter(getContext(), R.layout.my_hikes_item_listview, trails);
            hikesListView.setAdapter(customAdapter);
        }catch(Exception ex){

        }
    }
}