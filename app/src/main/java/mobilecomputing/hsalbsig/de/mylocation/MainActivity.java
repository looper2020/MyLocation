package mobilecomputing.hsalbsig.de.mylocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private double latitude;
    private double longitude;
    private float accuracy;
    private float speed;
    private float bearing;
    private LocationRequest mLocationRequest;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    //map
    private GoogleMap googleMap;
    //map

    //GoogleLocationAPI
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    //GoogleLocationAPI

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
    //Timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GoogleLocationAPI
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        //Timer
        timerTextView = (TextView) findViewById(R.id.textView_time);
        //Timer

        //Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Map


        Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "Start Button clicked");

                //Timer
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                //Timer

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "no permission");
                    return;
                }
                startLocation();
                updateGui();

                TextView textViewStart = (TextView) findViewById(R.id.textView_status);
                textViewStart.setText(R.string.tracking);
            }
        });

        Button buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "Stop Button clicked");

                //Timer
                timerHandler.removeCallbacks(timerRunnable);
                //Timer

                TextView textViewStop = (TextView) findViewById(R.id.textView_status);
                textViewStop.setText(R.string.stopping);
                updateGui();
            }
        });

        Button buttonRefresh = (Button) findViewById(R.id.button_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "Refresh Button clicked");
                onStart();
                updateGui();

            }
        });
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
        updateMapPosition(googleMap);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateMapPosition(googleMap);
    }

    private void updateMapPosition(GoogleMap googleMap) {
        if (googleMap != null && latitude != 0 && longitude != 0) {
            LatLng myPos = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(myPos).title("Meine Position"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            googleMap.animateCamera(zoom);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        Log.d("onConnect", "Location services connected");
        updateGui();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
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
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        checkPlayServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();

        updateGui();
    }
}
