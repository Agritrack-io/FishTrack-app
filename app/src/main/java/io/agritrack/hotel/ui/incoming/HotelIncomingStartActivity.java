package io.agritrack.hotel.ui.incoming;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Supplier;
import io.agritrack.dialog.ExpandableListDialog;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.SiteInfo;
import io.agritrack.ui.service.LocalPreferences;

public class HotelIncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<SiteInfo> fromAvramarSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSupplierSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();
    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private ExpandableListDialog avramarDialog;
    private SimpleListDialog supplierDialog;
    private SimpleListDialog siteDialog;
    //private SiteInfo fromSite;
    private String fromSupplier;
    private String toSite;
    private String fromSite;
    private MobileDB db;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_incoming_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        /*// set (any?) previously selected values to activity Controls.
        initControlsFromState();*/

        /*fromAvramarSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvIncomingFrom.setText(fromSite.getName());
                avramarDialog.dismiss();
            }
        });*/

        fromSupplierSelection.observe(this, response -> {
            if (response != null) {
                fromSupplier = response;
                tvIncomingFrom.setText(fromSupplier);
                supplierDialog.dismiss();
            }
        });

        toSiteSelection.observe(this, response -> {
            if (response != null) {
                toSite = response;
                tvIncomingTo.setText(toSite);
                siteDialog.dismiss();
            }
        });

        fromSiteSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvIncomingFrom.setText(fromSite);
                siteDialog.dismiss();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelIncomingStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvIncomingFrom = findViewById(R.id.tvIncomingFrom);
        tvIncomingTo = findViewById(R.id.tvIncomingTo);
        tgIncomingSource = findViewById(R.id.tgIncomingSource);
        tgIncomingDestination = findViewById(R.id.tgIncomingDestination);
        tgIncomingSource.setOnCheckedChangeListener(this);
        tgIncomingDestination.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToIncomingProcess);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            Intent i = new Intent(getApplicationContext(), HotelIncomingLinenActivity.class);
            startActivity(i);

        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
            startActivity(i);
        });
    }

    private Map<String, List<SiteInfo>> fillAvramarData() {
        Map<String, List<SiteInfo>> result = new HashMap<>();
        List<Site> allSites = db.siteDAO().getAll();
        if (allSites != null && !allSites.isEmpty()) {
            result = allSites.stream().filter(x -> x.siteLevel == 3).map(s -> new SiteInfo(s.name, s.description, s.lvl2)).collect(Collectors.groupingBy(SiteInfo::getCode));
        }

        return result;
    }

    private List<String> fillSupplierData() {
        List<String> result = new ArrayList<>();
        List<Site> allSuppliers = db.siteDAO().getAllSuppliers();
        //List<Supplier> allSuppliers = db.supplierDAO().getAll();
        if (allSuppliers != null && !allSuppliers.isEmpty()) {
            result = allSuppliers.stream().map(s -> s.name).collect(Collectors.toList());
        }

        return result;
    }

    private List<String> fillSubSiteData() {
        List<String> result = new ArrayList<>();
        List<Site> subSites = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (subSites != null && !subSites.isEmpty()) {
            result = subSites.stream().map(s -> s.name).collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbAvramar) {
            siteDialog = new SimpleListDialog(HotelIncomingStartActivity.this, fillSubSiteData(), fromSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            /*avramarDialog = new ExpandableListDialog(HotelIncomingStartActivity.this, fillAvramarData(), fromAvramarSelection, R.string.select_site);
            avramarDialog.showDialog();*/
            //tvIncomingFrom.setText("HOTEL");
            selectedToggleButtonFrom = Constants.ftAvramar;
        } else if (checkedId == R.id.tbSupplier) {
            supplierDialog = new SimpleListDialog(HotelIncomingStartActivity.this, fillSupplierData(), fromSupplierSelection, R.string.select_supplier);
            supplierDialog.showDialog();
            //tvIncomingFrom.setText("LAUNDRY");
            selectedToggleButtonFrom = Constants.ftSupplier;
        } else if (checkedId == R.id.tbAssetFrom) {
            GlobalState.recWHIncoming.from = Constants.ftAsset;
            tvIncomingFrom.setText("OTHER");
            selectedToggleButtonFrom = Constants.ftAsset;
        } else if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(HotelIncomingStartActivity.this, fillSubSiteData(), toSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSite;
        } else if (checkedId == R.id.tbAssetTo) {
            GlobalState.recWHIncoming.to = Constants.ftAsset;
            tvIncomingTo.setText("ROOM");
            selectedToggleButtonTo = Constants.ftAsset;
        } else if (checkedId == R.id.tbCarTo) {
            GlobalState.recWHIncoming.to = Constants.ftAsset;
            tvIncomingTo.setText("CAR");
            selectedToggleButtonTo = Constants.ftAsset;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whIncomingRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonFrom)) {
            whIncomingRecord.selectedToggleButtonFrom = selectedToggleButtonFrom;
        }

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonTo)) {
            whIncomingRecord.selectedToggleButtonTo = selectedToggleButtonTo;
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvIncomingFrom))) {
            whIncomingRecord.from = tvIncomingFrom.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvIncomingTo))) {
            whIncomingRecord.to = tvIncomingTo.getText().toString();
        }

        return whIncomingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)) {
                sb.append(String.format("\n%s is missing", "'Source site'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)) {
                sb.append(String.format("\n%s is missing", "'Target site'"));
            }
        }

        return sb.toString();
    }

/*    private void initControlsFromState() {

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.setCheckedStateForView(R.id.tbAvramar, true);
            avramarDialog.dismiss();
        } else if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.setCheckedStateForView(R.id.tbSupplier, true);
            supplierDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonTo)) {
            tgIncomingDestination.setCheckedStateForView(R.id.tbSite, true);
            siteDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonTo)) {
            tgIncomingDestination.check(R.id.tbAssetTo);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)) {
            tvIncomingFrom.setText(GlobalState.recWHIncoming.from);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)) {
            tvIncomingTo.setText(GlobalState.recWHIncoming.to);
        }
    }*/
}