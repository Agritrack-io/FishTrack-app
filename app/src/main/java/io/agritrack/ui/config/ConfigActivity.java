package io.agritrack.ui.config;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;
import static io.agritrack.ui.service.LocalPreferences.Latitude_Key;
import static io.agritrack.ui.service.LocalPreferences.Longitude_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedCluster_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteId_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteLevel_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteName_Key;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.ConfirmationDialogCommand;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.Coordinates;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.login.api.AuthApi;
import io.agritrack.ui.login.api.SiteInfo;
import io.agritrack.ui.login.api.SitesRequest;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigActivity extends LocationAwareActivity {
    private final MutableLiveData<List<SiteInfo>> siteInfoResults = new MutableLiveData<>();
    private MobileDB db;
    private TextView tvLongitude, tvLatitude;
    private ImageView btGPS;
    private ExpandableListView xvClusters;
    private ClusterListViewAdapter clustersAdapter;
    private Map<String, List<SiteInfo>> mapOfSitesPerCluster;
    private List<String> clusterIDs;
    private SiteInfo selectedSite;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // get references to coordinate text views.
        tvLongitude = findViewById(R.id.tvLongitude);
        tvLatitude = findViewById(R.id.tvLatitude);

        // get reference to recyclerView holding the sites close to reader's location.
        xvClusters = findViewById(R.id.xvClusters);

        // activate GPS location update feature.
        Location loc = super.findLocation();

        if(loc == null) {
            if(LocalPreferences.locationExists()) {
                CToast(getAppContext(), render("No location returned by GPS! \nPrevious Coordinates will be used."), Toast.LENGTH_LONG);
            } else {
                CToast(getAppContext(), render("No location returned by GPS! Plz try again"), Toast.LENGTH_LONG);
            }
        } else {
            showCoords();
            loadClusterInfo();
        }

        // Listview on child click listener
        xvClusters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String clusterKey = clusterIDs.get(groupPosition);
                selectedSite = mapOfSitesPerCluster.get(clusterKey).get(childPosition);

                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDialog = YesNoDialogFragment.instance();
                confirmSiteSelectionDialog.args().putSerializable("selectedSite", selectedSite);
                confirmSiteSelectionDialog.setMessage(getText(R.string.accept_selected_site) + selectedSite.getName());
                confirmSiteSelectionDialog.onConfirm(new ConfirmationDialogCommand() {
                    @Override
                    public void execute(Bundle args) {
                        SiteInfo siteInfo = (SiteInfo) args.getSerializable("selectedSite");

                        // persist selected Site to local Preferences.
                        LocalPreferences.writeValue(SelectedSiteName_Key, siteInfo.getName());
                        LocalPreferences.writeValue(SelectedSiteId_Key, siteInfo.getId());
                        LocalPreferences.writeValue(SelectedCluster_Key, siteInfo.getLevel2());
                        LocalPreferences.writeValue(SelectedSiteLevel_Key, siteInfo.getLevel3());

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

        // get references to localDB instance
        db = MobileDB.getInstance(getAppContext());

        // allow btGPS to invoke Location Updates Requests.
        btGPS = findViewById(R.id.btnGPS);
        btGPS.setOnClickListener(v -> {
            Location loc1 = findLocation();
            if(loc1 == null) {
                CToast(getAppContext(), render("No location returned by GPS!"), Toast.LENGTH_LONG);
            } else {
                showCoords();
                loadClusterInfo();
            }
        });

        // hide RecyclerView 'rvSites' until data is retrieved for backend REST API
        xvClusters.setVisibility(View.GONE);

        //************************************************************************
        // fill coordinate TextViews with previously stored (if any) coordinates.
        readCurrentLocationFromSharedPreferences();

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ConfigActivity.this);
            supportDialog.showDialog();
        });

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
        if (mLastLocation == null && useGPSoutcome) {
            // show error cause message
            CToast(getAppContext(), render("No location returned by GPS!"), Toast.LENGTH_LONG);
            return;
        } else if (!useGPSoutcome && !LocalPreferences.locationExists()) {
            // show error cause message
            CToast(getAppContext(), render("No location found locally!"), Toast.LENGTH_LONG);
            return;
        }

        AuthApi authService = APIServiceGenerator.createAPI(AuthApi.class);
        SitesRequest siteRQ = new SitesRequest(LocalPreferences.getLatitude(), LocalPreferences.getLongitude());
        Call<List<SiteInfo>> getSitesAsyncCall = authService.getSites(siteRQ.toMap());

        getSitesAsyncCall.enqueue(new Callback<List<SiteInfo>>() {
            @Override
            public void onResponse(Call<List<SiteInfo>> call, Response<List<SiteInfo>> response) {
                List<SiteInfo> rs = response.body();
                siteInfoResults.setValue(rs);
            }

            @Override
            public void onFailure(Call<List<SiteInfo>> call, Throwable t) {
                System.out.println(t);
                Toast.makeText(getAppContext(), render("Plz Check WIFI connection.."), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCoords() {
        runOnUiThread(() -> {
            tvLatitude.setText(coord2Degrees(this.mLastLocation.getLatitude(), Coordinates.LATITUDE));
            tvLongitude.setText(coord2Degrees(this.mLastLocation.getLongitude(), Coordinates.LONGITUDE));
            writeCurrentLocationToSharedPreferences();
        });
    }

    // ###################################
    private void initiateSiteInfoObserver() {
        siteInfoResults.observe(this, response -> {
            if (response == null) {
                Toast.makeText(getAppContext(), render("No site info received..."), Toast.LENGTH_LONG).show();
                return;
            }
            if (response.size() == 0) {
                xvClusters.setVisibility(View.VISIBLE);
                Toast.makeText(getAppContext(), render("No site info received..."), Toast.LENGTH_LONG).show();
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
        if (this.mLastLocation != null) {
            LocalPreferences.writeValue(Longitude_Key, String.valueOf(this.mLastLocation.getLongitude()));
            LocalPreferences.writeValue(Latitude_Key, String.valueOf(this.mLastLocation.getLatitude()));
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
