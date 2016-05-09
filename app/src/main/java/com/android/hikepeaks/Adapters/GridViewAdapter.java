package com.android.hikepeaks.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.hikepeaks.R;
import com.bumptech.glide.Glide;

import java.io.File;

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private File[] items;


    //Constructeur
    public GridViewAdapter(Context context, File[] items) {
        //super();
        this.context = context;
        this.items = items;
    }

    //Obtenir le nombre d'images
    @Override
    public int getCount() {
        return items.length;
    }

    //Prendre un item a x position dans l'array d'images
    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Génération de la vue
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView img;
        int top = 2, bottom = 2, left = 2, right = 2;
        if (convertView == null) {
            img = new ImageView(context);
            img.setPadding(left, top, right, bottom);
        } else {
            img = (ImageView) convertView;
        }

        int colWidth = ((GridView) parent).getColumnWidth();

        Glide.with(context)
                .load(items[position])
                .placeholder(R.drawable.blank_profile)
                .override(colWidth, colWidth)
                .centerCrop()
                .into(img);

        return img;

    }

}