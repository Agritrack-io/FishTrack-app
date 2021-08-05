package io.agritrack.fishtrack.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.FishTrackAPIServiceGenerator;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.enums.Coordinates;
import io.agritrack.fishtrack.ui.activity.config.ExpandableClusterListAdapter;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.login.api.AuthApi;
import io.agritrack.fishtrack.ui.activity.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.activity.login.api.SitesRequest;
import io.agritrack.fishtrack.ui.service.SharedPreferenceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ConfigActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;
    private final MutableLiveData<List<SiteInfo>> siteInfoResults = new MutableLiveData<>();
    private Boolean fetchSitesWhenNoGPSDataAcquireed = Boolean.TRUE;
    MobileDB db;
    TextView tvLongitude, tvLatitude;
    ImageView btGPS;
    ExpandableListView xvClusters;
    ExpandableClusterListAdapter clustersAdapter;
    volatile Location currentLocation;
    private Map<String, List<SiteInfo>> mapOfSitesPerCluster;
    private List<String> clusterIDs;
    private LocationManager locationManager;
    private TimeoutService timeoutService;

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
        // Listview on child click listener
        xvClusters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String clusterKey = clusterIDs.get(groupPosition);
                SiteInfo selectedSite = mapOfSitesPerCluster.get(clusterKey).get(childPosition);

                YesNoDialogFragment ys = new YesNoDialogFragment(selectedSite);
                FragmentManager fm = getSupportFragmentManager();
                ys.showNow(fm, getString(R.string.confirm_selection));

                return false;
            }
        });
        // hide RecyclerView 'rvSites' until data is retrieved for backend REST API
        xvClusters.setVisibility(View.GONE);

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
                toggleProgress(true);
            }
        });

        //************************************************************************
        // fill coordinate TextViews with previously stored (if any) coordinates.
        readCurrentLocationFromSharedPreferences();
    }

    private void showCoords() {
        runOnUiThread(() -> {
            tvLatitude.setText(coord2Degrees(this.currentLocation.getLatitude(), Coordinates.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.currentLocation.getLongitude(), Coordinates.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
            toggleProgress(false);
            timeoutService.cancel();
        });
    }

    private void toggleProgress(boolean show) {
        if (show) {
            runOnUiThread(() -> {
                timeoutService.start();
            });
        } else {
            runOnUiThread(() -> {
                timeoutService.cancel();
            });
        }
    }

    private void loadClusterInfo() {
        if (currentLocation == null) {
            Toast.makeText(getAppContext(), "No location is currently stored!", Toast.LENGTH_SHORT).show();
            return;
        }

        timeoutService = new TimeoutService(5000l, 500l, createDialog(R.string.acquire_cluster_info));
        timeoutService.start();

        AuthApi authService = FishTrackAPIServiceGenerator.createService(AuthApi.class);
        SitesRequest siteRQ = new SitesRequest(SharedPreferenceService.getLatitude(), SharedPreferenceService.getLongitude());
        Call<List<SiteInfo>> getSitesAsyncCall = authService.getSites(siteRQ.toMap());

        getSitesAsyncCall.enqueue(new Callback<List<SiteInfo>>() {
            @Override
            public void onResponse(Call<List<SiteInfo>> call, Response<List<SiteInfo>> response) {
                List<SiteInfo> rs = response.body();
                siteInfoResults.setValue(rs);
            }

            @Override
            public void onFailure(Call<List<SiteInfo>> call, Throwable t) {
                Toast.makeText(getAppContext(), "Plz Check WIFI connection...\n"+t.getMessage(), Toast.LENGTH_LONG).show();
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
                    timeoutService = new TimeoutService(10000l, 1000l, createDialog(R.string.acquire_coordinates));

                    toggleProgress(true);
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
                clustersAdapter = new ExpandableClusterListAdapter(this, mapOfSitesPerCluster);

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

    private void readCurrentLocationFromSharedPreferences() {
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

    private AlertDialog createDialog(@StringRes int msg) {
        // instantiate an AlertDialog with countdown functionality
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_indicator, null);
        TextView tvProgressMessage = dialogView.findViewById(R.id.progressMsg);
        tvProgressMessage.setTextSize(24.0f);
        tvProgressMessage.setText(msg);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setCancelable(false);
        return dlgBuilder.create();
    }

    private class TimeoutService extends CountDownTimer {
        private final AlertDialog alertDlg;

        public TimeoutService(long millisInFuture, long countDownInterval, AlertDialog dlg) {
            super(millisInFuture, countDownInterval);
            this.alertDlg = dlg;
            this.alertDlg.show();
        }

        @Override
        public void onTick(long millisUntilFinished) { /* Nothing to do during service execution*/}

        @Override
        public void onFinish() {
            locationManager.removeUpdates(ConfigActivity.this);
            this.alertDlg.dismiss();
            Toast.makeText(getAppContext(), "No GPS coords found. Plz retry...", Toast.LENGTH_LONG).show();
            // if No GPS info was fetched, the currently stored Location will be used.
            if(fetchSitesWhenNoGPSDataAcquireed) {
                currentLocation = new Location("gps");
                currentLocation.setLongitude(Double.valueOf(SharedPreferenceService.getLongitude()));
                currentLocation.setLatitude(Double.valueOf(SharedPreferenceService.getLatitude()));
                //loadClusterInfo();
            }
        }
    }

    public static class YesNoDialogFragment extends DialogFragment {
        private final SiteInfo mSite;
        public YesNoDialogFragment(SiteInfo selectedSite) {
            mSite = selectedSite;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getText(R.string.accept_selected_site) + mSite.getName())
                    .setPositiveButton(R.string.dialog_accept, (dialog, id) -> {
                        // persist selected Site to local Preferences.
                        SharedPreferenceService.writeValue(SharedPreferenceService.SelectedSite_Key, mSite.getName());
                        // move to Login Screen
                        Intent i = new Intent(getAppContext(), LoginActivity.class);
                        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
                        startActivity(i);
                    })
                    .setNegativeButton(R.string.dialog_deny, (dialog, id) -> {return;});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}