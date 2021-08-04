package io.agritrack.fishtrack.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.FishTrackAPIServiceGenerator;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.enums.CoordType;
import io.agritrack.fishtrack.ui.activity.config.ExpandableClusterListAdapter;
import io.agritrack.fishtrack.ui.activity.login.api.AuthApi;
import io.agritrack.fishtrack.ui.activity.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.activity.login.api.SitesRequest;
import io.agritrack.fishtrack.ui.service.SharedPreferenceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ConfigActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;
    MobileDB db;
    TextView tvLongitude, tvLatitude;
    ImageView btGPS;
    ExpandableListView xvClusters;
    ExpandableClusterListAdapter clustersAdapter;
    List<String> listOfClusters;
    Map<String, List<SiteInfo>> mapOfSitesPerCluster;
    LocationManager locationManager;
    volatile Location currentLocation;
    private SharedPreferences pref;
    private AlertDialog dialog;
    private TimeoutService timeoutService;

    private final MutableLiveData<List<SiteInfo>> siteInfoResults = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // get references to coordinate text views.
        tvLongitude = findViewById(R.id.tvLongitude);
        tvLatitude = findViewById(R.id.tvLatitude);

        // get reference to recyclerView holding the sites close to reader's location.
        xvClusters = findViewById(R.id.xvClusters);
        // Listview on child click listener
        xvClusters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getApplicationContext(),
                        listOfClusters.get(groupPosition) + " : " + mapOfSitesPerCluster.get(listOfClusters.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });

        // start observing siteInfo results..
        registerSiteInfo();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // get references to localDB instance
        db = MobileDB.getInstance(getContext());

        // get SharedPreferences instance
        pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        // allow btGPS to invoke Location Updates Requests.
        btGPS = findViewById(R.id.btnGPS);
        btGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConfigActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });

        // hide RecyclerView 'rvSites' until data is retrieved for backend REST API
        xvClusters.setVisibility(View.GONE);

        //************************************************************************
        // fill coordinate TextViews with previously stored (if any) coordinates.
        readCurrentLocationFromSharedPreferences();

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_indicator, null);
        TextView tvProgressMessage = dialogView.findViewById(R.id.progressMsg);
        tvProgressMessage.setTextSize(24.0f);
        tvProgressMessage.setText(R.string.acquire_coordinates);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setCancelable(false);
        dialog = dlgBuilder.create();

        timeoutService = new TimeoutService(5000l, 500l, dialog);
    }

    private void loadClusterInfo() {
        AuthApi authService = FishTrackAPIServiceGenerator.createService(AuthApi.class);
        SitesRequest siteRQ = new SitesRequest(SharedPreferenceService.getLatitude(), SharedPreferenceService.getLongitude());
        Call<List<SiteInfo>> callAsync = authService.getSites(siteRQ.toMap());

        callAsync.enqueue(new Callback<List<SiteInfo>>() {
            @Override
            public void onResponse(Call<List<SiteInfo>> call, Response<List<SiteInfo>> response) {
                List<SiteInfo> rs = response.body();
                siteInfoResults.setValue(rs);
            }

            @Override
            public void onFailure(Call<List<SiteInfo>> call, Throwable t) {
                System.out.println(t);
                Toast.makeText(getApplicationContext(), "Plz Check WIFI connection..", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("gps", "Location permission granted");
                try {
                    toggleProgress(true, R.string.acquire_coordinates);
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
            tvLatitude.setText(coord2Degrees(this.currentLocation.getLatitude(), CoordType.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.currentLocation.getLongitude(), CoordType.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
            toggleProgress(false, R.string.app_name);
        });
    }

    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            runOnUiThread(() -> {
                dialog.show();
                timeoutService.start();
            });
        } else {
            runOnUiThread(() -> {
                dialog.dismiss();
                timeoutService.cancel();
            });
        }
    }

    private void invokeSyncSites() {
        // display spinning progress bar
        toggleProgress(true, R.string.sync_sites);

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
            toggleProgress(false, R.string.app_name);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.currentLocation = location;
        locationManager.removeUpdates(this);
        showCoords();
        locationManager.removeUpdates(this);
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

    // ###################################

    private void registerSiteInfo() {
        siteInfoResults.observe(this, response -> {
            if (response == null) {
                Toast.makeText(getApplicationContext(), "No site info received...", Toast.LENGTH_LONG).show();
                return;
            }
            if (response.size() == 0) {
                xvClusters.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "No site info received...", Toast.LENGTH_LONG).show();
//                showLoginFailed(response.getError());
//                loadingProgressBar.setVisibility(View.GONE);
//                loadingText.setVisibility(View.GONE);
//                loadingText.setText(null);
            }
            if (response.size() > 0) {
                mapOfSitesPerCluster = response.stream().collect(Collectors.groupingBy(SiteInfo::getLevel2, Collectors.toCollection(ArrayList::new)));
                listOfClusters = new ArrayList<>(mapOfSitesPerCluster.keySet());

                clustersAdapter = new ExpandableClusterListAdapter(this, listOfClusters, mapOfSitesPerCluster);
                // setting list adapter
                xvClusters.setAdapter(clustersAdapter);

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

    private void readCurrentLocationFromSharedPreferences() {

        String _lon = SharedPreferenceService.getLongitude();
        String _lat = SharedPreferenceService.getLatitude();

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

    private class TimeoutService extends CountDownTimer {
        private final AlertDialog alertDlg;

        public TimeoutService(long millisInFuture, long countDownInterval, AlertDialog dlg) {
            super(millisInFuture, countDownInterval);

            this.alertDlg = dlg;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            alertDlg.dismiss();
            // if No GPS info was fetched, the currently stored Location will be used.
            loadClusterInfo();
        }
    }
}