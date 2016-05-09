package com.android.hikepeaks.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Activities.HikeDetailsActivity;
import com.android.hikepeaks.Adapters.ExploreHikesListViewAdapter;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Tasks.GetAllHikesAsyncTask;
import com.android.hikepeaks.Tasks.GetMyHikesAsyncTask;
import com.android.hikepeaks.Utilities.Connectivity;

import java.util.ArrayList;


public class ExploreFragment extends BaseFragment implements GetAllHikesAsyncTask.OnGetAllHikesAsyncTaskTaskCompleted {

    private TerminalManager terminalManager = TerminalManager.getInstance();
    private DynamoDBManager dynamoDBManager = DynamoDBManager.getInstance();
    private ListView explorehikesListView;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        explorehikesListView = (ListView) view.findViewById(R.id.explorehikesListView);
        explorehikesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Trail item = (Trail) adapter.getItemAtPosition(position);
                //Toast.makeText(getContext(), "Clicked item " + item.getTitle(), Toast.LENGTH_LONG).show();

                // use this to prevent contacting retreiving item from database
                HikeDetailsActivity.CurrentTrail = item;
                Intent intent = new Intent(getContext(), HikeDetailsActivity.class);
                intent.putExtra("trail", item.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        return view;
    }



    @Override
    public void onStart() {
        super.onStart();

//        new GetAllHikesAsyncTask(dynamoDBManager)
//                .setListener(this)
//                .execute();
    }

    @Override
    public void onVisible() {
        super.onVisible();

        if(Connectivity.isNetworkOnline(getContext())){
            new GetAllHikesAsyncTask(dynamoDBManager)
                    .setListener(this)
                    .execute();
        }else {
            TextView msg = (TextView) getActivity().findViewById(R.id.txt_notavailable);
            msg.setVisibility(View.VISIBLE);
        }

    }


    public void OnGetAllHikesAsyncTaskTaskCompleted(ArrayList<Trail> trails)
    {
        try {
            ExploreHikesListViewAdapter customAdapter = new ExploreHikesListViewAdapter(getContext(), R.layout.explore_item_listview, trails);
            explorehikesListView.setAdapter(customAdapter);
        }catch(Exception ex){

        }
    }
}

