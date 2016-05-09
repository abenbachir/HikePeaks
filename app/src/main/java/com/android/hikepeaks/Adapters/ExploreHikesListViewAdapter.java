package com.android.hikepeaks.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Views.ExploreHikesItemView;
import java.util.ArrayList;


public class ExploreHikesListViewAdapter extends ArrayAdapter<Trail> {
    private int resource;
    private ArrayList<Trail> items;

    public ExploreHikesListViewAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ExploreHikesListViewAdapter(Context context, int resource, ArrayList<Trail> items) {
        super(context, resource, items);
        this.resource = resource;
        this.items = items;
    }


    public long getItemId(int position) {
        return 0;
    }

    public ArrayList<Trail> getItems(){
        return this.items;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Trail place = getItem(position);
        ExploreHikesItemView placeView = (ExploreHikesItemView)convertView;
        if (placeView == null) {
            placeView = new ExploreHikesItemView(getContext(), place);
        }else{
            placeView.setModel(place);
            placeView.updateControls();
        }

        return placeView;
    }


}
