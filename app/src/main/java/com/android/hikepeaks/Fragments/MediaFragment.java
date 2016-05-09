package com.android.hikepeaks.Fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Activities.PictureDetailsActivity;
import com.android.hikepeaks.Adapters.GridViewAdapter;
import com.android.hikepeaks.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MediaFragment extends BaseFragment {

    String path = Environment.getExternalStorageDirectory().toString() + "/HikePeaks/Media";
    File[] f;
    //Variables
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    //Bluetooth variables
    int REQUEST_ENABLE_BT = 1;

    FloatingActionButton ShareButton;

    //used HashMap to be able to easily add or remove items with respect to the selection
    //of pictures
    HashMap<Long,Uri> uris = new HashMap<>();
    Intent myShareIntent;


    public MediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);


        gridAdapter = new GridViewAdapter(this.getContext(), getFromSdcard(path));

        try {

            ShareButton = (FloatingActionButton) view.findViewById(R.id.shareButton);
            ShareButton.setVisibility(View.GONE);



            gridView = (GridView) view.findViewById(R.id.gridView);
            gridView.setAdapter(gridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(getContext(), PictureDetailsActivity.class);
                    intent.putExtra("url", Uri.fromFile(((File) parent.getItemAtPosition(position))));
                    intent.putExtra("latitude",  "null");
                    intent.putExtra("longitude", "null");
                    intent.putExtra("src","media");
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });

            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                    if ((view.getTag() == null) ||
                            !(view.getTag().toString().equals(getString(R.string.selectedPict)))) {

                        ShareButton.setVisibility(View.VISIBLE);
                        view.setAlpha(0.5f);
                        view.setBackgroundResource(R.color.colorAccent);
                        view.setTag(getString(R.string.selectedPict));

                        uris.put(id, Uri.fromFile((File) parent.getItemAtPosition(position)));
                        gridView.setOnItemClickListener(null);

                    } else {
                        view.setAlpha(1f);
                        view.setBackgroundColor(0);
                        view.setTag(null);

                        uris.remove(id);

                        if (uris.isEmpty()){
                            ShareButton.setVisibility(View.GONE);
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Intent intent = new Intent(getContext(), PictureDetailsActivity.class);
                                    intent.putExtra("url", Uri.fromFile(((File) parent.getItemAtPosition(position))));
                                    intent.putExtra("latitude",  "null");
                                    intent.putExtra("longitude", "null");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                }
                            });

                        }

                    }

                    return true; //"false" si on veut que le onItemClick s'exécute même si le onItemLongClick s'est exécuté et "true" sinon
                }
            });


        } catch (NullPointerException e) {
            /*Toast.makeText(getContext(),"null pointer exception",Toast.LENGTH_LONG).show();*/
        }

        ShareButton.setOnClickListener(oclButton);

        return view;
    }

    public File[] getFromSdcard(String path) {
        File f = new File(path);
        return f.listFiles();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //to test is user enabled bluetooth service
        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(getContext(),
                        "Sharing cannot take place while Bluetooth service is disabled",
                        Toast.LENGTH_LONG).show();

            } else if (resultCode == Activity.RESULT_OK) {

                //on bluetooth activated, push sharing intent right away
                startActivity(myShareIntent);

            }
        }

    }

    View.OnClickListener oclButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.shareButton:
                    sendSelectedPics();
                    break;

            }

        }
    };

    private void sendSelectedPics(){
        final ArrayList<Uri> uriList = new ArrayList<>();
        //just to put the hashmap into an ArrayList to be passed to the sharing intent
        for (Uri uri : uris.values()) {
            uriList.add(uri);
        }

        myShareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        myShareIntent.setType("image/*");
        myShareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {

            // Device does not support Bluetooth
            Toast.makeText(getContext(),
                    "Your device does not support sharing files using Bluetooth",
                    Toast.LENGTH_LONG).show();

        } else if (!mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {

            startActivity(myShareIntent);
        }

    }



}


