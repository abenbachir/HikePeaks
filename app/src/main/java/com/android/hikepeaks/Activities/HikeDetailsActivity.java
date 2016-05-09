package com.android.hikepeaks.Activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Models.TrailCapture;
import com.android.hikepeaks.Models.TrailPoint;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.DynamoDBManager;
import com.android.hikepeaks.Utilities.PermissionUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HikeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static Trail CurrentTrail = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DynamoDBManager dynamoDBManager = DynamoDBManager.getInstance();
    private Toolbar toolbar;
    private Trail trail;
    private LinearLayout myGallery;
    private TextView myTitle;
    private TextView myDate;
    private TextView myDuration;
    private TextView myDistance;
    private TextView mySpeed;
    private TextView mySteps;

    // maps
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_details);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.hike_details_map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);



        trail = CurrentTrail;
        if(trail == null) {
            //chercher la trail cliquée
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.containsKey("trail")) {
                trail = dynamoDBManager.findTrailById(bundle.get("trail").toString());
            }
        }

        setTitle(trail.getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            Spannable text = new SpannableString(actionBar.getTitle());
            text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            actionBar.setTitle(text);

        }

        //INFORMATIONS
        myTitle = (TextView) findViewById(R.id.detail_title);
        myDate = (TextView) findViewById(R.id.detail_date);
        myDuration = (TextView) findViewById(R.id.detail_duration);
        myDistance = (TextView) findViewById(R.id.detail_distance);
        mySpeed = (TextView) findViewById(R.id.detail_speed);
        mySteps = (TextView) findViewById(R.id.detail_steps);

        myTitle.setText(trail.getTitle());
        myDate.setText(DateFormat.getDateTimeInstance().format(new Date(trail.getCreatedAt())));
        myDuration.setText(timeConversion(trail.getElapsedTimeInSeconds()));
        myDistance.setText(roundDoubletoString(trail.getDistance()/1000) + " km");
        mySpeed.setText(roundDoubletoString(trail.getAverageSpeed()) + " m/s");

        if ((int)trail.getSteps() == 0){
            mySteps.setText("The device wasn't equiped with a step counter");
        }else{
            mySteps.setText((int)trail.getSteps() + " steps");
        }

        //GALLERY
        myGallery = (LinearLayout)findViewById(R.id.mygallery);
        final ArrayList <TrailCapture> pictures = trail.getCaptures();

        for (int i = 0; i< pictures.size(); i++){

            View view = insertPhoto(pictures.get(i),i);
            myGallery.addView(view);

        }
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
    public void onMapReady(GoogleMap map) {
        try {
            this.map = map;
            this.map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            if (trail != null) {
                drawTrailPath(trail);
                zoomToFitPathBounds(trail);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
        } else {
            PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
        }
    }


    View insertPhoto(final TrailCapture capture, final int position){


        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new ActionBar.LayoutParams(750, 750));
        layout.setGravity(Gravity.CENTER);

        final ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ActionBar.LayoutParams(720, 720));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(capture.getPicturePath()).centerCrop().into(imageView);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), PictureDetailsActivity.class);
                intent.putExtra("url", capture.getPicturePath());
                intent.putExtra("latitude", Double.toString(capture.getLatitude()));
                intent.putExtra("longitude",Double.toString(capture.getLongitude()));
                intent.putExtra("src","hikedetail");
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        layout.addView(imageView);

        return layout;
    }

    private static String timeConversion(int seconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        return hours + " hours " + minutes + " minutes " + seconds + " seconds";
    }

    private static String roundDoubletoString(double d){
        DecimalFormat df = new DecimalFormat("0.00");
        return (df.format(d));
    }


    protected void zoomToFitPathBounds(Trail trail)
    {
        CameraUpdate cameraUpdate1 = CameraUpdateFactory
                .newLatLngZoom(new LatLng(trail.getStartPoint().getLatitude(), trail.getStartPoint().getLongitude()), 20);
        map.moveCamera(cameraUpdate1);

        ArrayList<TrailPoint> points = trail.getPath();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (TrailPoint point : points) {
            builder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cameraUpdate2 = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cameraUpdate2);
    }

    protected void drawTrailPath(Trail trail) {
        if (map == null )
            return;

        List<LatLng> points = new ArrayList<>();
        List<TrailPoint> path = trail.getPath();
        for (int i = 0; i < path.size(); i++) {
            TrailPoint trailPoint = path.get(i);
            points.add(new LatLng(trailPoint.getLatitude(), trailPoint.getLongitude()));
        }

        Polyline pathLine = map.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(8)
                    .color(Color.BLUE));

        // start point
        if (path.size() > 1 && trail.getStartPoint() != null)
        {
            LatLng position = new LatLng(trail.getStartPoint().getLatitude(), trail.getStartPoint().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(position);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.start_marker);
            icon = Bitmap.createScaledBitmap(icon, 80, 80, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot));
            map.addMarker(markerOptions);
        }

        // end point
        if (path.size() > 2 && trail.isPathCompleted()){
            int totalsSeconds = trail.getElapsedTimeInSeconds();
            int hours = (int) Math.floor(totalsSeconds / 3600);
            int minutes = (int) Math.floor((totalsSeconds % 3600) / 60);
            int seconds = totalsSeconds % 60;
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng position = new LatLng(trail.getEndPoint().getLatitude(), trail.getEndPoint().getLongitude());
            markerOptions.position(position);
            markerOptions.title("Completed trail");
            markerOptions.snippet("Time : " + hours + "h " + minutes + "min " + seconds + "sec");
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.end_marker);
            icon = Bitmap.createScaledBitmap(icon, 90, 90, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            map.addMarker(markerOptions);
        }

        // add captures
        List<TrailCapture> captures = trail.getCaptures();
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.capture_marker);
        icon = Bitmap.createScaledBitmap(icon, 50, 50, false);
        for (int i = 0; i < captures.size(); i++) {
            TrailCapture capture = captures.get(i);
            addCaptureMarker(capture, icon);
        }
    }
    protected void addCaptureMarker(TrailCapture capture, Bitmap icon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(capture.getLatitude(), capture.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        map.addMarker(markerOptions);
    }
}
