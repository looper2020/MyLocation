package mobilecomputing.hsalbsig.de.mylocation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, DialogSaveFragment.DialogSaveListener, View.OnClickListener {

    public Button button_start, button_intervalTime;
    private double latitude, longitude;
    private float accuracy, speed, bearing;
    private boolean isTracking = false;
    public int intervalTime = 10;
    long startTime = 0;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private LocationRequest mLocationRequest;
    private ProgressBar progressBar;

    private EditText editTextInterval;
    private GoogleMap googleMap;

    //GoogleLocationAPI
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //DatabaseDAO
    private MarkerDao markerDao = null;
    private TrackDao trackDao = null;
    private List<Marker> markerList = new ArrayList<>();

    //Timer
    TextView timerTextView;
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

        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.GONE);
        this.button_start = (Button) findViewById(R.id.button_start);
        this.button_intervalTime = (Button) findViewById(R.id.button_intervalTime);

        button_start.setOnClickListener(this);
        this.button_intervalTime.setOnClickListener(this);


        //IntervalTimer
        editTextInterval = (EditText) findViewById(R.id.editText_intervalTime);

        //GooglePlayServices availability status check
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 10);
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
        }
    }

    //Options Menu with buttons DB Log and DB Delete
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    //Stops the tracking and resets the timer
    private void stopTracking() {
        Log.d("Test", "Stop Button clicked");
        if (isTracking == true) {
            Toast.makeText(getApplicationContext(), R.string.track_stop, Toast.LENGTH_LONG).show();
            isTracking = false;

            //Timer
            timerHandler.removeCallbacks(timerRunnable);

            disconnectFromGoogleMap();

            final Button buttonStart = (Button) findViewById(R.id.button_start);
            buttonStart.setText(R.string.start);
        }
    }

    //updates gui values
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

    //decides which actions to take when button Delete DB and DB Log pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_database:
                Toast.makeText(getApplicationContext(), "Database clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), DBActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Confirm");
                builder.setMessage("Delete the whole database?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        trackDao.deleteAll();
                        markerDao.deleteAll();
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //checks google map availability
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            googleMap.animateCamera(zoom);
            LatLng myPos = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));

            Log.d("Test", "onMapReady called");
        }
    }


    //set interval and create request
    @Override
    public void onConnected(Bundle bundle) {
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(this.intervalTime * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(this.intervalTime * 1000); // 10 second, in milliseconds
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
        }
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

    //connects the GoogleApi Client
    private void connectToGoogleMap() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            checkPlayServices();
        }
    }

    //disconnects the GoogleApi client
    private void disconnectFromGoogleMap() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    //Updates gps values on call and places waypoint
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

    //adds marker on GoogleMap
    private void placeWaypoint() {
        LatLng myPos = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(myPos).title("Meine Position"));
        Marker marker = new Marker();
        marker.setLatitude(latitude);
        marker.setLongitude(longitude);
        markerList.add(marker);
    }

    //opens save Dialog when stop button is pressed
    public void openSaveDialog() {
        DialogFragment dialogTrack = new DialogSaveFragment();
        Bundle args = new Bundle();
        args.putString("title", getString(R.string.saveTrackTitle));
        dialogTrack.setArguments(args);
        dialogTrack.show(getSupportFragmentManager(), "dialogSaveTrack");
        Toast.makeText(getApplicationContext(), "Track saved", Toast.LENGTH_LONG).show();
    }

    //save button for dialog
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

    //get data from dbactivity
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

    //draws the polyline between the markers
    private void connectMarkers(List<Marker> markers) {
        PolylineOptions options = new PolylineOptions();
        for (Marker marker : markers) {
            options.add(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }
        googleMap.addPolyline(options);
    }


    //    Hier wird das klick Event abgefangen und geprüft, welcher Button gedrückt wurde!!!
    @Override
    public void onClick(View v) {
        if (v == button_start) {
            Toast.makeText(getApplicationContext(), "onClick Methode Start Button geklickt", Toast.LENGTH_LONG).show();

            //Remove all Marker on google Map
            googleMap.clear();

            TextView textViewStart = (TextView) findViewById(R.id.textView_status);

            if (isTracking == false) {
                connectToGoogleMap();
                this.progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Tracking Started!", Toast.LENGTH_LONG).show();
                isTracking = true;

                //Timer
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Test", "no permission");
                    return;
                }


                textViewStart.setText(R.string.tracking);
                button_start.setText(R.string.stop);
            } else {
                this.progressBar.setVisibility(View.INVISIBLE);
                stopTracking();
                textViewStart.setText(R.string.track_stop);
                openSaveDialog();
            }

        } else if (v == button_intervalTime) {
            if (editTextInterval.getText().toString().equals("")) {
                AlertDialog alertNoIntervalTime = new AlertDialog.Builder(this).create();
                alertNoIntervalTime.setMessage("Bitte eine Zahl zwischen 1 und 60 eintragen");
                alertNoIntervalTime.show();
                editTextInterval.setText(String.valueOf(this.intervalTime));
            } else if (isTracking == true) {
                Toast.makeText(getApplicationContext(), "Track läuft gerade, Änderung nicht möglich", Toast.LENGTH_LONG).show();
                Log.d("Test", "Button intervalTime gedrückt und im if Zweick ifTracking gelandet");
                editTextInterval.setText(String.valueOf(intervalTime));
            } else {
                intervalTime = Integer.parseInt(editTextInterval.getText().toString());
                editTextInterval.setText(String.valueOf(setIntervalTime(this.intervalTime)));
            }
        } else
            Toast.makeText(getApplicationContext(), "OOPS something went wrong", Toast.LENGTH_LONG).show();
    }

    // set interval time for location updates
    public int setIntervalTime(int intervalTime) {
        if (intervalTime > 0 && intervalTime < 61) {
            this.intervalTime = intervalTime;
        } else {
            AlertDialog alertNoIntervalTime = new AlertDialog.Builder(this).create();
            alertNoIntervalTime.setMessage("Bitte eine Zahl zwischen 1 und 60 eintragen");
            alertNoIntervalTime.show();
        }
        return this.intervalTime;
    }
}
