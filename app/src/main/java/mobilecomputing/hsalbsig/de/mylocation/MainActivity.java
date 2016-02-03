package mobilecomputing.hsalbsig.de.mylocation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import mobilecomputing.hsalbsig.de.mylocation.dao.DaoMaster;
import mobilecomputing.hsalbsig.de.mylocation.dao.DaoSession;
import mobilecomputing.hsalbsig.de.mylocation.dao.Marker;
import mobilecomputing.hsalbsig.de.mylocation.dao.MarkerDao;
import mobilecomputing.hsalbsig.de.mylocation.dao.Track;
import mobilecomputing.hsalbsig.de.mylocation.dao.TrackDao;
import mobilecomputing.hsalbsig.de.mylocation.db.DBActivity;
import mobilecomputing.hsalbsig.de.mylocation.dialogs.DialogSaveFragment;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, DialogSaveFragment.DialogSaveListener {

    private double latitude;
    private double longitude;
    private float accuracy;
    private float speed;
    private float bearing;
    private LocationRequest mLocationRequest;
    private boolean isTracking = false;
    public int intervalTime = 10;
    public EditText editTextInterval;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;



    //map
    private GoogleMap googleMap;


    //GoogleLocationAPI
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //DatabaseDAO
    MarkerDao markerDao = null;
    TrackDao trackDao = null;
    List<Marker> markerList = new ArrayList<>();

    //Timer
    TextView timerTextView;
    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(getString(R.string.timer) + " " + String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //IntervalTime ext
        editTextInterval = (EditText)findViewById(R.id.editText_intervalTime);

        //Availability status check
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if(status!=ConnectionResult.SUCCESS){
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,this, 10);
            dialog.show();
        } else {


            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "location-db", null);
            SQLiteDatabase db = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            DaoSession daoSession = daoMaster.newSession();

            markerDao = daoSession.getMarkerDao();
            trackDao = daoSession.getTrackDao();

            //GoogleLocationAPI
            if (checkPlayServices()) {
                buildGoogleApiClient();
                Log.d("Test", "GoogleLocationAPI called");
            }


            //Timer
            timerTextView = (TextView) findViewById(R.id.textView_time);


            //Map Fragment
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            final Button buttonStart = (Button) findViewById(R.id.button_start);
            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Test", "Start Button clicked");
                    googleMap.clear();


                    if (isTracking == false) {
                        connectToGoogleMap();

                        Toast.makeText(getApplicationContext(), "Tracking Started!", Toast.LENGTH_LONG).show();
                        isTracking = true;

                        onResume();

                        //Timer
                        startTime = System.currentTimeMillis();
                        timerHandler.postDelayed(timerRunnable, 0);


                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("Test", "no permission");
                            return;
                        }
                        startLocation();

                        TextView textViewStart = (TextView) findViewById(R.id.textView_status);
                        textViewStart.setText(R.string.tracking);
                        buttonStart.setText(R.string.stop);
                    } else {
                        stopTracking();
                        openSaveDialog();
                    }
                }
            });

            Button buttonMarker = (Button) findViewById(R.id.button_intervalTime);
            buttonMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Test", "Interval Button clicked");
                    if(editTextInterval.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), "Bitte eine Zahl zwischen 1 und 59 eintragen", Toast.LENGTH_LONG).show();
                        Log.d("Test", "Button intervalTime gedrückt und im if Zweick zum Leezeichenpruefung gelandet");
                    }else if(isTracking == true){
                        Toast.makeText(getApplicationContext(), "Track läuft gerade, Änderung nicht möglich", Toast.LENGTH_LONG).show();
                        Log.d("Test", "Button intervalTime gedrückt und im if Zweick ifTracking gelandet");
                        editTextInterval.setText(String.valueOf(intervalTime));
                    }else{
                        intervalTime = Integer.parseInt(editTextInterval.getText().toString());
                        editTextInterval.setText(String.valueOf(setIntervalTime(intervalTime)));
                    }
                }
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void startLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            this.latitude = mLastLocation.getLatitude();
            this.longitude = mLastLocation.getLongitude();
            this.accuracy = mLastLocation.getAccuracy();
            this.speed = mLastLocation.getSpeed();
            this.bearing = mLastLocation.getBearing();

        }

        updateGui();
    }

    private void stopTracking() {
        Log.d("Test", "Stop Button clicked");
        if (isTracking == true) {
            Toast.makeText(getApplicationContext(), "Tracking Stopped!", Toast.LENGTH_LONG).show();
            isTracking = false;

            //Timer
            timerHandler.removeCallbacks(timerRunnable);


            disconnectFromGoogleMap();

            final Button buttonStart = (Button) findViewById(R.id.button_start);
            buttonStart.setText(R.string.start);
        }
    }

    private void updateGui() {

        TextView textViewLatitude = (TextView) findViewById(R.id.textView_latitude);
        TextView textViewLongitude = (TextView) findViewById(R.id.textView_longitude);
        TextView textViewAccuracy = (TextView) findViewById(R.id.textView_accuracy);
        TextView textViewSpeed = (TextView) findViewById(R.id.textView_speed);
        TextView textViewBearing = (TextView) findViewById(R.id.textView_bearing);


        textViewLatitude.setText(getString(R.string.latitude) + " " + String.valueOf(latitude));
        textViewLongitude.setText(getString(R.string.longitude) + " " + String.valueOf(longitude));
        textViewAccuracy.setText(getString(R.string.accuracy) + " " + String.valueOf(accuracy) + "m");
        textViewSpeed.setText(getString(R.string.speed) + " " + String.valueOf(speed));
        textViewBearing.setText(getString(R.string.bearing) + " " + String.valueOf(bearing));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_database:
                Toast.makeText(getApplicationContext(), "Database clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), DBActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.menu_save:


                openSaveDialog();
                break;

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            googleMap.animateCamera(zoom);
            LatLng myPos = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));

            Log.d("Test", "onMapReady called");
        }
    }

    public int setIntervalTime(int intervalTime){
        if(intervalTime >0 && intervalTime < 60){
            this.intervalTime = intervalTime;
        } else{
            Toast.makeText(getApplicationContext(), intervalTime + " ist keine erlaubte Zahl", Toast.LENGTH_LONG).show();
            Log.d("Test", intervalTime + " ist keine erlaubte Zahl");
        }
        return this.intervalTime;
    }

    @Override
    public void onConnected(Bundle bundle) {

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(this.intervalTime * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(this.intervalTime * 1000); // 10 second, in milliseconds
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
        }
        Log.d("Test", "onConnect called Location services connected");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToGoogleMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void connectToGoogleMap() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            checkPlayServices();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectFromGoogleMap();
    }

    private void disconnectFromGoogleMap() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("Test", "onLocationChanged called");

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();
        updateGui();

        placeWaypoint();
    }

    private void placeWaypoint() {
        LatLng myPos = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(myPos).title("Meine Position"));
        Marker marker = new Marker();
        marker.setLatitude(latitude);
        marker.setLongitude(longitude);
        markerList.add(marker);
    }


    public void openSaveDialog() {
        DialogFragment dialogTrack = new DialogSaveFragment();
        Bundle args = new Bundle();
        args.putString("title", getString(R.string.saveTrackTitle));
        dialogTrack.setArguments(args);
        dialogTrack.show(getSupportFragmentManager(), "dialogSaveTrack");
        Toast.makeText(getApplicationContext(), "Track saved", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDialogSaveSaveClick(String title, String tag) {
        //save the track
        Track myTrack = new Track();
        myTrack.setName(title);
        myTrack.setLogTime(new Date());
        trackDao.insertOrReplace(myTrack);

        for (Marker marker : markerList) {
            marker.setTrack(myTrack);
            markerDao.insertOrReplace(marker);
        }
        Log.d("Entity", trackDao.toString());


        //remove Markers
        googleMap.clear();
        markerList.clear();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            googleMap.clear();
            long trackID = data.getLongExtra("trackID", -1);
            if (trackID == -1) {
                return;
            }
            Track loaded = trackDao.load(trackID);
            if (loaded == null) {
                return;
            }
            List<Marker> markers = loaded.getTrack();
            for (Marker marker : markers) {
                LatLng myPos = new LatLng(marker.getLatitude(), marker.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(myPos).title(marker.getText()));
            }
            connectMarkers(markers);

        }
    }

    private void connectMarkers(List<Marker> markers) {
        PolylineOptions options = new PolylineOptions();
        for (Marker marker : markers) {
            options.add(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }
        googleMap.addPolyline(options);
    }

}
