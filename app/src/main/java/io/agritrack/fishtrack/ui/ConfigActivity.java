package io.agritrack.fishtrack.ui;

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
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.dialog.ConfirmationDialogCommand;
import io.agritrack.fishtrack.dialog.TimeOutProgressDlg;
import io.agritrack.fishtrack.dialog.YesNoDialogFragment;
import io.agritrack.fishtrack.enums.Coordinates;
import io.agritrack.fishtrack.ui.config.ClusterListViewAdapter;
import io.agritrack.fishtrack.ui.login.LoginActivity;
import io.agritrack.fishtrack.ui.login.api.AuthApi;
import io.agritrack.fishtrack.ui.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.login.api.SitesRequest;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Latitude_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Longitude_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.SelectedCluster_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.SelectedSiteId_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.SelectedSiteName_Key;

public class ConfigActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;
    private final MutableLiveData<List<SiteInfo>> siteInfoResults = new MutableLiveData<>();
    private MobileDB db;
    private TextView tvLongitude, tvLatitude;
    private ImageView btGPS;
    private ExpandableListView xvClusters;
    private ClusterListViewAdapter clustersAdapter;
    volatile Location currentLocation;
    private Map<String, List<SiteInfo>> mapOfSitesPerCluster;
    private List<String> clusterIDs;
    private LocationManager locationManager;
    private SharedPreferences pref;
    private TimeOutProgressDlg alertDialog;
    private YesNoDialogFragment confirmSiteSelectionDialog;
    private SiteInfo selectedSite;

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
                String clusterKey = clusterIDs.get(groupPosition);
                selectedSite = mapOfSitesPerCluster.get(clusterKey).get(childPosition);

                // instantiate Site selection confirm dialog
                Bundle args = new Bundle();
                args.putSerializable("selectedSite", selectedSite);
                YesNoDialogFragment confirmSiteSelectionDialog = YesNoDialogFragment.newInstance(args);
                confirmSiteSelectionDialog.setMessage(getText(R.string.accept_selected_site) + selectedSite.getName());
                confirmSiteSelectionDialog.onConfirm(new ConfirmationDialogCommand() {
                    @Override
                    public void execute(Bundle args) {
                        SiteInfo selSite = (SiteInfo) args.getSerializable("selectedSite");

                        // persist selected Site to local Preferences.
                        LocalPreferences.writeValue(SelectedSiteName_Key, selSite.getName());
                        LocalPreferences.writeValue(SelectedSiteId_Key, selSite.getId());
                        LocalPreferences.writeValue(SelectedCluster_Key, selSite.getLevel2());

                        // move to Login Screen
                        Intent i = new Intent(getAppContext(), LoginActivity.class);
                        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
                        startActivity(i);
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDialog.showNow(fm, getString(R.string.confirm_selection));

                return false;
            }
        });

        // start observing siteInfo results..
        initiateSiteInfoObserver();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // get references to localDB instance
        db = MobileDB.getInstance(getAppContext());

        // get SharedPreferences instance
        pref = getAppContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);

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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ConfigActivity.this);
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
        alertDialog = new TimeOutProgressDlg(10000l, 500l, this) {
            @Override
            protected void doTasks() {
                locationManager.removeUpdates(ConfigActivity.this);

                // if No GPS info was fetched, the currently stored Location will be used.
                loadClusterInfo(Boolean.FALSE);
            }
        };
        alertDialog.setMessage(R.string.acquire_coordinates);

        // instantiate Footer controls
        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void loadClusterInfo() {
        loadClusterInfo(Boolean.TRUE);
    }

    private void loadClusterInfo(Boolean useGPSoutcome) {
        if (currentLocation == null && useGPSoutcome) {
            // hide progress Dialog
            alertDialog.hide();

            // show error cause message
            Toast.makeText(getAppContext(), "No location returned by GPS!", Toast.LENGTH_LONG).show();
            return;
        } else if (!useGPSoutcome && !LocalPreferences.locationExists()) {
            // hide progress Dialog
            alertDialog.hide();

            // show error cause message
            Toast.makeText(getAppContext(), "No location found locally!", Toast.LENGTH_LONG).show();
            return;
        }

        if (alertDialog != null) {
            alertDialog.setMessage(R.string.acquire_cluster_info);
        }

        AuthApi authService = APIServiceGenerator.createAPI(AuthApi.class);
        SitesRequest siteRQ = new SitesRequest(LocalPreferences.getLatitude(), LocalPreferences.getLongitude());
        Call<List<SiteInfo>> getSitesAsyncCall = authService.getSites(siteRQ.toMap());

        getSitesAsyncCall.enqueue(new Callback<List<SiteInfo>>() {
            @Override
            public void onResponse(Call<List<SiteInfo>> call, Response<List<SiteInfo>> response) {
                List<SiteInfo> rs = response.body();
                siteInfoResults.setValue(rs);

                // hide progress Dialog
                alertDialog.hide();
            }

            @Override
            public void onFailure(Call<List<SiteInfo>> call, Throwable t) {
                // hide progress Dialog
                alertDialog.hide();

                System.out.println(t);
                Toast.makeText(getAppContext(), "Plz Check WIFI connection..", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    toggleProgress(true, R.string.acquire_coordinates);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                } catch (SecurityException ex) {
                    Toast.makeText(getAppContext(), "Location Access Permission was not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showCoords() {
        runOnUiThread(() -> {
            tvLatitude.setText(coord2Degrees(this.currentLocation.getLatitude(), Coordinates.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.currentLocation.getLongitude(), Coordinates.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
            toggleProgress(false, R.string.app_name);
        });
    }

    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            runOnUiThread(() -> {
                alertDialog.show();
            });
        } else {
            runOnUiThread(() -> {
                alertDialog.hide();
            });
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.currentLocation = location;
        locationManager.removeUpdates(this);
        showCoords();
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
    private void initiateSiteInfoObserver() {
        siteInfoResults.observe(this, response -> {
            if (response == null) {
                Toast.makeText(getAppContext(), "No site info received...", Toast.LENGTH_LONG).show();
                return;
            }
            if (response.size() == 0) {
                xvClusters.setVisibility(View.VISIBLE);
                Toast.makeText(getAppContext(), "No site info received...", Toast.LENGTH_LONG).show();
            }
            if (response.size() > 0) {
                // Site coordinates may be close to >1 cluster. These sites will be displayed grouped by cluster.
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
            LocalPreferences.writeValue(Longitude_Key, String.valueOf(this.currentLocation.getLongitude()));
            LocalPreferences.writeValue(Latitude_Key, String.valueOf(this.currentLocation.getLatitude()));
        }
    }

    private void readCurrentLocationFromSharedPreferences() {
        String _lon = LocalPreferences.getLongitude();
        String _lat = LocalPreferences.getLatitude();

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
