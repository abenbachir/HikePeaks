package com.android.hikepeaks.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Utilities.Connectivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PictureDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    ImageView myImage;
    TextView myLatitude;
    TextView myLongitude;
    FloatingActionButton ShareButton;
    Bitmap bitmap;
    File f;

    //Bluetooth variables
    int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_details);

        Bundle bundle = getIntent().getExtras();

        myImage = (ImageView) findViewById(R.id.detail_picture);
        myLatitude = (TextView) findViewById(R.id.detail_latitude);
        myLongitude = (TextView) findViewById(R.id.detail_longitude);
        ShareButton = (FloatingActionButton) findViewById(R.id.shareButton);

        if(bundle.get("src").equals("hikedetail") && !Connectivity.isNetworkOnline(this)){
            ShareButton.setVisibility(View.GONE);
        }

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }

        //chercher la photo cliquée
        if(bundle != null && bundle.containsKey("url")) {
            Glide.with(this)
                    .load(bundle.get("url").toString())
                    .crossFade()
                    .into(myImage);

            String latitude = bundle.get("latitude").toString();
            String longitude = bundle.get("longitude").toString();

            if ( !latitude.equals("null") || !longitude.equals("null")){

                myLatitude.setText("Latitude: " + latitude);
                myLongitude.setText("Longitude: " + longitude);
            }
            else{
                LinearLayout lay = (LinearLayout) findViewById(R.id.detail_info);
                lay.setVisibility(LinearLayout.GONE);

            }

        }

        //set action on floatbutton
        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myShareIntent = new Intent(Intent.ACTION_SEND);
                Bundle bundle = getIntent().getExtras();

                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();

                myShareIntent.setType("image/jpeg");



                //Bitmap bitmap = null;

                URL url = null;
                try {
                    url = new URL(bundle.get("url").toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);


                f = new File(Environment.getExternalStorageDirectory() + File.separator + "HikePeaks/hikepeaks_"+ ts + ".jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                myShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/"+ "HikePeaks/hikepeaks_"+ ts + ".jpg"));

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {

                    // Device does not support Bluetooth
                    Toast.makeText(getApplicationContext(),
                            "Your device does not support sharing files using Bluetooth",
                            Toast.LENGTH_LONG).show();

                } else if (!mBluetoothAdapter.isEnabled()) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {

                    startActivity(Intent.createChooser(myShareIntent, "Share Image"));

                }

                //f.delete();

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //to test is user enabled bluetooth service
        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == Activity.RESULT_OK) {

                f.delete();
            }
        }

    }

}
