package io.agritrack.fishtrack.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.enums.CoordType;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ConfigActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;
    MobileDB db;
    ProgressBar progressBar;
    TextView loadingText, tvLongitude, tvLatitude;
    ImageView btGPS;
    LocationManager locationManager;
    volatile Location location;
    Long startLocationSearchTime;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get SharedPreferences instance
        pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        setContentView(R.layout.activity_config);

        progressBar = findViewById(R.id.loading);
//        loadingText = findViewById(R.id.loading_text);

        tvLongitude = findViewById(R.id.tvLongitude);
        tvLatitude = findViewById(R.id.tvLatitude);


        btGPS = findViewById(R.id.btnGPS);
        btGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // permission has been granted
                if (ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                locationManager.requestLocationUpdates("gps", 5000, 0, ConfigActivity.this);
            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        db = MobileDB.getInstance(getContext());

        //***************
        readCurrentLocationFromSharedPreferences();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("gps", "Location permission granted");
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                } catch (SecurityException ex) {
                    Log.d("gps", "Location permission did not work!");
                }
            }
        }
    }

    private void showCoords() {
        runOnUiThread(() -> {
            tvLatitude.setText(coord2Degrees(this.location.getLatitude(), CoordType.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.location.getLongitude(), CoordType.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void toggleSyncProgress(boolean show) {
        if (show) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setText(R.string.syncing);
                loadingText.setVisibility(View.VISIBLE);
            });
        } else {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
            });
        }
    }

    private void invokeSyncSites() {
        // display spinning progress bar
        toggleSyncProgress(true);

        try {
            SharedPreferences pref = getSharedPreferences("agritrack", Context.MODE_PRIVATE);
            String token = pref.getString("token", null);
            boolean syncResult = true;

//            RouteSyncService routeSyncService = new RouteSyncService();
//            syncResult = routeSyncService.syncRoute(db, token);

            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            i.putExtra("syncErrors", !syncResult);
            startActivity(i);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            toggleSyncProgress(false);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        locationManager.removeUpdates(this);
        showCoords();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }


    // ###################################
    private void setPreferenceParam(String key, String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getPreferenceParam(String key) {
        return pref.getString(key, null);
    }

    private void writeCurrentLocationToSharedPreferences() {
        if (this.location != null) {
            setPreferenceParam("lon", String.valueOf(this.location.getLongitude()));
            setPreferenceParam("lat", String.valueOf(this.location.getLatitude()));
        }
    }

    private void readCurrentLocationFromSharedPreferences() {

        String _lon = getPreferenceParam("lon");
        String _lat = getPreferenceParam("lat");

        if (_lon != null && _lat != null) {
            runOnUiThread(() -> {
                tvLatitude.setText(coord2Degrees(Double.valueOf(_lat), CoordType.LATITUDE));
                tvLongitude.setText(coord2Degrees(Double.valueOf(_lon), CoordType.LONGITUDE));
            });
        }
    }

    private String coord2Degrees(double coord, CoordType tp) {
        String hemisphere = "N";

        int deg = (int) coord;
        double minutes = (coord - deg) * 60;
        int min = (int) minutes;
        double sec = (minutes - min) * 60;

        if (CoordType.LATITUDE.equals(tp)) {
            hemisphere = deg < 0 ? "S" : "N";
        } else {
            hemisphere = deg < 0 ? "W" : "E";
        }

        return String.format("%s°%02d'%.3f\" %s", deg, min, sec, hemisphere);
    }
}