package io.agritrack.philosofish.ui;

import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import io.agritrack.philosofish.fish.state.GlobalState;

public class LocationAwareActivity extends AppCompatActivity implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;
    protected Location mLastLocation;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load location immediately
        this.mLastLocation = findLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // GLOBAL DYNAMIC STATE RESET FOR ALL ACTIVITIES
        if (GlobalState.shouldReset) {
            GlobalState.resetAll();
            GlobalState.shouldReset = false;
        }
    }

    public Location findLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean checkNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                CToast(this, "Error: No GPS Service is available", Toast.LENGTH_LONG);
            } else {
                if (checkGPS) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return null;
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );

                    if (locationManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            locationManager.getCurrentLocation(
                                    LocationManager.GPS_PROVIDER,
                                    null,
                                    getApplication().getMainExecutor(),
                                    location -> {
                                        mLastLocation = location;
                                        stopListener();
                                    }
                            );
                        } else {
                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);

                            locationManager.requestSingleUpdate(criteria, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    mLastLocation = location;
                                    stopListener();
                                }
                            }, null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.mLastLocation;
    }

    public Location getLocation() {
        return this.mLastLocation;
    }

    private void stopListener() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLastLocation = location;
        stopListener();
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    protected void onDestroy() {
        this.stopListener();
        super.onDestroy();
    }
}
