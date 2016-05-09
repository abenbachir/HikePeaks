package com.android.hikepeaks.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Views.HikesItemView;
import java.util.ArrayList;


public class HikesListViewAdapter extends ArrayAdapter<Trail> {
    private int resource;
    private ArrayList<Trail> items;

    public HikesListViewAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public HikesListViewAdapter(Context context, int resource, ArrayList<Trail> items) {
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
        Trail trail = getItem(position);
        HikesItemView hikeView = (HikesItemView)convertView;
        if (hikeView == null) {
            hikeView = new HikesItemView(getContext(), trail, resource);
        }else{
            hikeView.setModel(trail);
            hikeView.updateControls();
        }

        return hikeView;
    }


}
