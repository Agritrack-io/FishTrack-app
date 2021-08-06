package io.agritrack.fishtrack.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.work.impl.model.WorkProgressDao_Impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.FishTrackAPIServiceGenerator;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.enums.Coordinates;
import io.agritrack.fishtrack.ui.activity.config.ClusterListViewAdapter;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.login.api.AuthApi;
import io.agritrack.fishtrack.ui.activity.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.activity.login.api.SitesRequest;
import io.agritrack.fishtrack.ui.custom.CustomInfoDialog;
import io.agritrack.fishtrack.ui.custom.YesNoDialog;
import io.agritrack.fishtrack.ui.service.SharedPreferenceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ConfigActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;
    private final MutableLiveData<List<SiteInfo>> siteInfoResults = new MutableLiveData<>();
    private final Boolean fetchSitesWhenNoGPSDataReturned = Boolean.TRUE;
    volatile Location currentLocation;
    private MobileDB db;
    private TextView tvLongitude, tvLatitude;
    private ImageView btGPS;
    private ExpandableListView xvClusters;
    private ClusterListViewAdapter clustersAdapter;
    private Map<String, List<SiteInfo>> mapOfSitesPerCluster;
    private List<String> clusterIDs;
    private LocationManager locationManager;
    private CustomInfoDialog progressDialog;


    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // start observing siteInfo results..
        initiateSiteInfoObserver();

        // get references to localDB instance
        db = MobileDB.getInstance(getContext());

        // get references to coordinate text views.
        tvLongitude = findViewById(R.id.tvLongitude);
        tvLatitude = findViewById(R.id.tvLatitude);

        // get reference to recyclerView holding the sites close to reader's location.
        xvClusters = findViewById(R.id.xvClusters);
        xvClusters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String clusterKey = clusterIDs.get(groupPosition);
                SiteInfo selectedSite = mapOfSitesPerCluster.get(clusterKey).get(childPosition);

                YesNoDialog ys = new YesNoDialog(selectedSite, LoginActivity.class);
                FragmentManager fm = getSupportFragmentManager();
                ys.showNow(fm, getString(R.string.confirm_selection));

                return false;
            }
        });
        // hide RecyclerView 'rvSites' until data is retrieved for backend REST API
        xvClusters.setVisibility(View.GONE);

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //
        progressDialog = new CustomInfoDialog(10000l, 500l, R.string.acquire_coordinates);

        // allow btGPS to invoke Location Updates Requests.
        btGPS = findViewById(R.id.btnGPS);
        btGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        locationManager.removeUpdates(ConfigActivity.this);
                    }
                }, 10000l);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, ConfigActivity.this);

                // show Progress Dialog
                progressDialog.start();
            }
        });

        // fill coordinate TextViews with previously stored (if any) coordinates.
        readLastKnownLocationFromSharedPreferences();

        // request permission to access GPS location.
        // if permission is granted, this call will trigger the location update.
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private void updateCoordinateViews() {
        runOnUiThread(() -> {
            tvLatitude.setText(coord2Degrees(this.currentLocation.getLatitude(), Coordinates.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.currentLocation.getLongitude(), Coordinates.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
            progressDialog.cancel();
        });
    }

    private void loadClusterInfo() {
        if (currentLocation == null) {
            Toast.makeText(getAppContext(), "No location is currently stored!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.start();

        AuthApi authService = FishTrackAPIServiceGenerator.createService(AuthApi.class);
        SitesRequest siteRQ = new SitesRequest(SharedPreferenceService.getLatitude(), SharedPreferenceService.getLongitude());
        Call<List<SiteInfo>> getSitesAsyncCall = authService.getSites(siteRQ.toMap());

        getSitesAsyncCall.enqueue(new Callback<List<SiteInfo>>() {
            @Override
            public void onResponse(Call<List<SiteInfo>> call, Response<List<SiteInfo>> response) {
                List<SiteInfo> rs = response.body();
                progressDialog.cancel();
                siteInfoResults.setValue(rs);
            }

            @Override
            public void onFailure(Call<List<SiteInfo>> call, Throwable t) {
                progressDialog.cancel();
                Toast.makeText(getAppContext(), "Plz Check WIFI connection...\n" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    // instantiate an AlertDialog with countdown functionality
                    progressDialog.start();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                } catch (SecurityException ex) {
                    Toast.makeText(getAppContext(), "Location Access Permission was not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.currentLocation = location;
        locationManager.removeUpdates(this);
        updateCoordinateViews();
        loadClusterInfo();
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

    // ######################################################################
    private void initiateSiteInfoObserver() {
        siteInfoResults.observe(this, response -> {
            if (response == null) {
                Toast.makeText(getAppContext(), "No site info received...", Toast.LENGTH_LONG).show();
                return;
            }
            if (response.size() == 0) {
                xvClusters.setVisibility(View.VISIBLE);
                Toast.makeText(getAppContext(), "No Clusetr/Site info found for current location...", Toast.LENGTH_LONG).show();
            }
            if (response.size() > 0) {
                mapOfSitesPerCluster = response.stream().collect(Collectors.groupingBy(SiteInfo::getLevel2, Collectors.toCollection(ArrayList::new)));
                clusterIDs = new LinkedList<>(mapOfSitesPerCluster.keySet());
                clustersAdapter = new ClusterListViewAdapter(this, mapOfSitesPerCluster);

                // setting list adapter
                xvClusters.setAdapter(clustersAdapter);
                // since there Sites available, display them in  a list.
                xvClusters.setVisibility(View.VISIBLE);
            }
        });
    }

    private void writeCurrentLocationToSharedPreferences() {
        if (this.currentLocation != null) {
            SharedPreferenceService.writeValue("lon", String.valueOf(this.currentLocation.getLongitude()));
            SharedPreferenceService.writeValue("lat", String.valueOf(this.currentLocation.getLatitude()));
        }
    }

    private void readLastKnownLocationFromSharedPreferences() {
        String _lon = SharedPreferenceService.getLongitude();
        String _lat = SharedPreferenceService.getLatitude();

        if (_lon != null && _lat != null) {
            runOnUiThread(() -> {
                tvLatitude.setText(coord2Degrees(Double.valueOf(_lat), Coordinates.LATITUDE));
                tvLongitude.setText(coord2Degrees(Double.valueOf(_lon), Coordinates.LONGITUDE));
            });
        }
    }

    private String coord2Degrees(double coord, Coordinates tp) {
        String hemisphere = "N";

        int deg = (int) coord;
        double minutes = (coord - deg) * 60;
        int min = (int) minutes;
        double sec = (minutes - min) * 60;

        if (Coordinates.LATITUDE.equals(tp)) {
            hemisphere = deg < 0 ? "S" : "N";
        } else {
            hemisphere = deg < 0 ? "W" : "E";
        }

        return String.format("%s°%02d'%.3f\" %s", deg, min, sec, hemisphere);
    }

}