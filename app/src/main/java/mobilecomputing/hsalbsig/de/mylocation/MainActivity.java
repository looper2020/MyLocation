package mobilecomputing.hsalbsig.de.mylocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private double latitude;
    private double longitude;
    private float accuracy;
    private float speed;
    private float bearing;
    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener = new MyLocationListener();
    private Criteria criteria;

    //map
    private GoogleMap googleMap;
    //map


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

        //Timer
        timerTextView = (TextView) findViewById(R.id.textView_time);
        //Timer

        //Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Map

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        criteria.setCostAllowed(false);
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //final Location location = locationManager.requestLocationUpdates(provider, 0, 0, new MyLocationListener());


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


                locationManager.requestLocationUpdates(provider, 1000, 0, mylistener);
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

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(mylistener);
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
                updateGui();

            }
        });
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
        onMapReady(googleMap);
    }

    //map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (latitude != 0 && longitude != 0) {
            LatLng myPos = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(myPos).title("Meine Position"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
        }
    }
    //map

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            longitude = location.getLongitude();
            latitude = location.getLatitude();
            accuracy = location.getAccuracy();
            speed = location.getSpeed();
            bearing = location.getBearing();
            updateGui();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
