package com.android.hikepeaks.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Models.TrailCapture;
import com.android.hikepeaks.Models.TrailPoint;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.SyncPool;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.Tasks.CounterTask;
import com.android.hikepeaks.Tasks.SynchronizeAsyncTask;
import com.android.hikepeaks.Utilities.PermissionUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class MapFragment extends BaseFragment implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener,
        SensorEventListener,
        CounterTask.OnTimeChangedListener {

    private static final int TAKE_PHOTO_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private int stepCounter = 0;
    private int counterSteps = 0;
    private int stepDetector = 0;
    float[] mGravity;
    float[] mGeomagnetic;
    private int locationMinTimeIntervalCheck = 5000;
    private int locationMinDistance = 5;
    private SupportMapFragment mapFragment;
    private FloatingActionButton startHikingButton;
    private FloatingActionButton finishHikingButton;
    private FloatingActionButton captureImageButton;
    private FloatingActionButton focusButton;
    private LinearLayout dashboardLayout;
    private LinearLayout hikingActionsLayout;
    private TextView counterDisplay;
    private TextView averageSpeedDisplay;
    private TextView distanceDisplay;
    private TextView stepsDisplay;
    private GoogleMap map;
    private AccountManager accountManager = AccountManager.getInstance();
    private TerminalManager terminalManager = TerminalManager.getInstance();
    private SyncPool syncPool = SyncPool.getInstance();
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor countSensor;
    private Sensor magnetometer;
    private boolean isMapReady = false;
    private boolean hikingStarted = false;
    private Location currentLocation = null;
    private Marker currentLocationMarker = null;
    private Timer timer = new Timer();
    private CounterTask counter;
    private Trail recordTrail;
    private Polyline pathLine = null;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private File captureImageFile = null;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FragmentManager fm = getChildFragmentManager();

        TextView msg = (TextView) getActivity().findViewById(R.id.txt_notavailable);
        msg.setVisibility(View.GONE);

        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dashboardLayout = (LinearLayout) view.findViewById(R.id.dashboard_layout);
        dashboardLayout.setVisibility(View.GONE);
        hikingActionsLayout = (LinearLayout) view.findViewById(R.id.hiking_actions_layout);
        hikingActionsLayout.setVisibility(View.GONE);
        stepsDisplay = (TextView) view.findViewById(R.id.trail_steps_counter);
        counterDisplay = (TextView) view.findViewById(R.id.trail_counter_diplay);
        averageSpeedDisplay = (TextView) view.findViewById(R.id.trail_average_speed);
        distanceDisplay = (TextView) view.findViewById(R.id.trail_distance);
        startHikingButton = (FloatingActionButton) view.findViewById(R.id.start_hiking);
        finishHikingButton = (FloatingActionButton) view.findViewById(R.id.finish_hiking);
        captureImageButton = (FloatingActionButton) view.findViewById(R.id.trail_capture_image);
        focusButton = (FloatingActionButton) view.findViewById(R.id.trail_focus);
        startHikingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHiking(v);
            }
        });
        finishHikingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishHiking(v);
            }
        });
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickCaptureImageButton(v);
            }
        });
        focusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickFocusButton(v);
            }
        });
        locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        initLocationService();

        return view;
    }

    protected void OnClickFocusButton(View v){
        if(currentLocation == null)
            return;
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        map.animateCamera(center);
    }
    protected void onClickCaptureImageButton(View v) {
        String pictureName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        captureImageFile = TerminalManager.getInstance().getMediaFile(pictureName);
        Uri pictureUri = Uri.fromFile(captureImageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != getActivity().RESULT_OK)
            return;

        try {
//        String path = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            String path = captureImageFile.getPath();
            TrailCapture capture = new TrailCapture();
            capture.setPicturePath(path);
            capture.setLatitude(currentLocation.getLatitude());
            capture.setLongitude(currentLocation.getLongitude());
            recordTrail.addCapture(capture);
            addCaptureMarker(capture, null);
        } catch (Exception ex) {

        }
    }

    protected void startHiking(View v) {
        if (currentLocation == null) {
            Toast.makeText(this.mActivity, "We can't start the hiking now, current location is not found yet", Toast.LENGTH_LONG);
            return;
        }
        initSensorService();
        hikingStarted = true;
        updateControls();
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15);
        map.animateCamera(center);
        counterDisplay.setText("0:0:0");
        averageSpeedDisplay.setText("0 m/s");
        distanceDisplay.setText("0 km");
        timer = new Timer();
        counter = new CounterTask();
        counter.setListener(this);
        counter.setHandler(new Handler() {
            public void handleMessage(Message msg) {
                OnTimeChanged(msg.what);
            }
        });
        timer.scheduleAtFixedRate(counter, 0, 1000);

        recordTrail = new Trail();
        TrailPoint point = new TrailPoint();
        point.setLatitude(currentLocation.getLatitude());
        point.setLongitude(currentLocation.getLongitude());
        recordTrail.addPoint(point);
        recordTrail.setAccountId(accountManager.getAccount().getId());
        clearMap();
        setCurrentLocationMarker();
    }

    protected void finishHiking(View v) {
        if (currentLocation == null)
            return;

        removeSensorService();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setMessage("You are going to stop recording, are you sure to continue ?")
                .setNeutralButton("Resume", null)
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hikingStarted = false;
                        timer.cancel();
                        timer.purge();
                        recordTrail.getPath().clear();
                        recordTrail.getCaptures().clear();
                        recordTrail = null;
                        clearMap();
                        setCurrentLocationMarker();
                        updateControls();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                        View promptView = layoutInflater.inflate(R.layout.save_hike_input_dialog, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setView(promptView);
                        final EditText titleEditText = (EditText) promptView.findViewById(R.id.edittext);
                        alertDialogBuilder.setCancelable(false)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String title = String.valueOf(titleEditText.getText());
                                        if (title.isEmpty()) {
                                            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            hikingStarted = false;
                                            timer.cancel();
                                            timer.purge();
                                            recordTrail.setTitle(title.toUpperCase());
                                            recordTrail.setElapsedTimeInSeconds(counter.getTotalSeconds());
                                            recordTrail.setPathCompleted(true);
                                            recordTrail.setIsSynchronized(false);
                                            accountManager.getAccount().addTrail(recordTrail);
                                            // update cache
                                            terminalManager.updateActiveAccount(accountManager.getAccount());
                                            // add sync task to the SyncPool
                                            syncPool.queueObject(recordTrail);
                                            updateEndMarker(recordTrail.getEndPoint());
                                            updateControls();

                                            if (isNetworkAvailable()) {
                                                new SynchronizeAsyncTask().setListener((SynchronizeAsyncTask.OnSynchronizeTaskCompleted) getActivity()).execute();
                                                Toast.makeText(getContext(),"Your hike will be saved in a few moments", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                        // create an alert dialog
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();
                    }
                })
                .show();
    }

    public void OnTimeChanged(int totalsSeconds) {
        int hours = (int) Math.floor(totalsSeconds / 3600);
        int minutes = (int) Math.floor((totalsSeconds % 3600) / 60);
        int seconds = totalsSeconds % 60;
        recordTrail.setElapsedTimeInSeconds(totalsSeconds);
        counterDisplay.setText(hours + ":" + minutes + ":" + seconds);
    }

    public void updateControls() {
        startHikingButton.setVisibility(hikingStarted ? View.INVISIBLE : View.VISIBLE);
        hikingActionsLayout.setVisibility(hikingStarted ? View.VISIBLE : View.GONE);
        dashboardLayout.setVisibility(hikingStarted ? View.VISIBLE : View.GONE);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.isMapReady = true;
        this.map = map;
        this.map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//        this.map.setInfoWindowAdapter(new CustomInfoWindowAdapter(mActivity));
//        enableMyLocation();
    }

    protected void clearMap() {
        startMarker = null;
        endMarker = null;
        pathLine = null;
        currentLocationMarker = null;
        map.clear();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) mActivity,
                    LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
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
            enableMyLocation();
        } else {
            PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null)
            return;

        if(this.isVisible()){
            initLocationService();
        }
        if (hikingStarted) {
            clearMap();
            setCurrentLocationMarker();
            updateTrailPathMarker();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (map == null)
            return;

    }

    @Override
    public void onVisible() {
        super.onVisible();
        if (map == null)
            return;

        initLocationService();
    }

    @Override
    public void onHidden() {
        super.onHidden();
        if (map == null)
            return;

    }

    public void onLocationChanged(final Location location) {
        if (currentLocation == null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));

        currentLocation = location;
        setCurrentLocationMarker();
        if (hikingStarted && recordTrail != null) {
            TrailPoint point = new TrailPoint();
            point.setLatitude(currentLocation.getLatitude());
            point.setLongitude(currentLocation.getLongitude());
            recordTrail.addPoint(point);
            updateTrailPathMarker();
            updateCapturesMarker();
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            recordTrail.calculatePathDistance();
            recordTrail.calculateAverageSpeed();
            distanceDisplay.setText(new DecimalFormat("##.##").format(recordTrail.getDistance() / 1000) + " km");
            averageSpeedDisplay.setText(new DecimalFormat("##.##").format(recordTrail.getAverageSpeed()) + " m/s");
        }

        Log.i("onLocationChanged", "Location changed : " + location.toString());
    }

    public void onProviderDisabled(String provider) {
        Log.i("onProviderDisabled", provider + " Disabled");
    }

    public void onProviderEnabled(String provider) {
        Log.i("onProviderEnabled", provider + " Enabled");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_STEP_DETECTOR:
                stepDetector++;
                break;
            case Sensor.TYPE_STEP_COUNTER:
                //Since it will return the total number since we registered we need to subtract the initial amount
                //for the current steps since we opened app
                if (counterSteps < 1) {
                    // initial value
                    counterSteps = (int)event.values[0];
                }

                // Calculate steps taken based on first counter value received.
                stepCounter = (int)event.values[0] - counterSteps;
                recordTrail.setSteps(stepCounter);
                break;
        }

        stepsDisplay.setText(stepCounter +" steps");

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float mAzimuth = event.values[0];
                float mPitch = event.values[1];
                float mRoll = event.values[2];
//                Log.d("onSensorChanged", "mAzimuth :" + Float.toString(mAzimuth));
//                Log.d("onSensorChanged", "mPitch :" + Float.toString(mPitch));
//                Log.d("onSensorChanged", "mRoll :" + Float.toString(mRoll));
            }
        }

    }

    protected void updateCapturesMarker() {
        if (map == null || currentLocation == null)
            return;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.capture_marker);
        icon = Bitmap.createScaledBitmap(icon, 50, 50, false);
        List<LatLng> points = new ArrayList<>();
        List<TrailCapture> captures = recordTrail.getCaptures();
        for (int i = 0; i < captures.size(); i++) {
            TrailCapture capture = captures.get(i);
            addCaptureMarker(capture, icon);
        }
    }

    protected void addCaptureMarker(TrailCapture capture, Bitmap icon) {
        if (icon == null) {
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.capture_marker);
            icon = Bitmap.createScaledBitmap(icon, 50, 50, false);
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(capture.getLatitude(), capture.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        map.addMarker(markerOptions);
    }

    protected void updateTrailPathMarker() {
        if (map == null || currentLocation == null)
            return;

        List<LatLng> points = new ArrayList<>();
        List<TrailPoint> path = recordTrail.getPath();
        for (int i = 0; i < path.size(); i++) {
            TrailPoint trailPoint = path.get(i);
            points.add(new LatLng(trailPoint.getLatitude(), trailPoint.getLongitude()));
        }
        if (pathLine == null) {
            pathLine = map.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(8)
                    .color(Color.RED));
        } else {
            pathLine.setPoints(points);
        }

        if (path.size() > 1)
            updateStartMarker(recordTrail.getStartPoint());

        if (path.size() > 2 && recordTrail.isPathCompleted())
            updateEndMarker(recordTrail.getEndPoint());

    }

    protected void updateStartMarker(TrailPoint point) {
        LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
        if (startMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(position);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.start_marker);
            icon = Bitmap.createScaledBitmap(icon, 80, 80, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot));
            startMarker = map.addMarker(markerOptions);
        } else {
            startMarker.setPosition(position);
        }
    }

    protected void updateEndMarker(TrailPoint point) {
        LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
        if (endMarker == null) {
            int totalsSeconds = recordTrail.getElapsedTimeInSeconds();
            int hours = (int) Math.floor(totalsSeconds / 3600);
            int minutes = (int) Math.floor((totalsSeconds % 3600) / 60);
            int seconds = totalsSeconds % 60;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(position);
            markerOptions.title("Completed trail");
            markerOptions.snippet("Time : " + hours + "h " + minutes + "min " + seconds + "sec");
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.end_marker);
            icon = Bitmap.createScaledBitmap(icon, 90, 90, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot));
            endMarker = map.addMarker(markerOptions);
        } else {
            endMarker.setPosition(position);
        }
    }

    protected void setCurrentLocationMarker() {
        if (map == null || currentLocation == null)
            return;
        LatLng pos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.map_blue_dot);
            icon = Bitmap.createScaledBitmap(icon, 55, 55, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot));
            currentLocationMarker = map.addMarker(markerOptions);
        } else {
            currentLocationMarker.setPosition(pos);
        }
    }

    protected void moveCamera(LatLng position) {
        if (map == null)
            return;

        CameraUpdate center = CameraUpdateFactory.newLatLng(position);
        map.animateCamera(center);
    }

    protected void initSensorService() {
        stepCounter = 0;
        counterSteps = 0;
        stepDetector = 0;
        if(countSensor != null)
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        if(magnetometer != null)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void removeSensorService() {
        sensorManager.unregisterListener(this);
    }

    protected void initLocationService() {
        if(getActivity() == null)
            return;
        boolean isGpsProvEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetProvEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        if ((ActivityCompat.checkSelfPermission(getActivity(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED)) { //the checking of granted permission is necessary for API level 23+

            ActivityCompat.requestPermissions(getActivity(), new String[]{fineLocationPermission}, 1); //← this only works in API level 23
        }

        if (!isGpsProvEnabled && !isNetProvEnabled) {
//            showAlertDialog().show();
        } else if (isGpsProvEnabled && !isNetProvEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationMinTimeIntervalCheck, locationMinDistance, this);
        } else if (!isGpsProvEnabled && isNetProvEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationMinTimeIntervalCheck, locationMinDistance, this);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationMinTimeIntervalCheck, locationMinDistance, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationMinTimeIntervalCheck, locationMinDistance, this);
        }
    }

    protected void remozzveLocationService() {
        if(getActivity() == null)
            return;
        String fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        if ((ActivityCompat.checkSelfPermission(getActivity(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED)) { //the checking of granted permission is necessary for API level 23+

            ActivityCompat.requestPermissions(getActivity(), new String[]{fineLocationPermission}, 1); //← this only works in API level 23
        }
        locationManager.removeUpdates(this);
    }
}