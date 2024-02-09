package io.agritrack.fish.ui.wh.incoming;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
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

import io.agritrack.kefalonia.R;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Supplier;
import io.agritrack.dialog.ExpandableListDialog;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.SiteInfoRS;
import io.agritrack.ui.service.LocalPreferences;

public class IncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<SiteInfoRS> fromAvramarSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSupplierSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();

    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination, tgIncomingItemType;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private String selectedIncomingItemType = Constants.ftAsset;
    private ExpandableListDialog avramarDialog;
    private SimpleListDialog supplierDialog;
    private SimpleListDialog siteDialog;
    private SiteInfoRS fromSite;
    private String fromSupplier;
    private String toSite;
    private MobileDB db;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        fromAvramarSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvIncomingFrom.setText(fromSite.getName());
                avramarDialog.dismiss();
            }
        });

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

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(IncomingStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvIncomingFrom = findViewById(R.id.tvIncomingFrom);
        tvIncomingTo = findViewById(R.id.tvIncomingTo);
        tgIncomingSource = findViewById(R.id.tgIncomingSource);
        tgIncomingDestination = findViewById(R.id.tgIncomingDestination);
        tgIncomingItemType = findViewById(R.id.tgIncomingItemType);
        tgIncomingItemType.setOnCheckedChangeListener(this);
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
            } else if (selectedIncomingItemType == Constants.ftAsset) {
                Intent i = new Intent(getApplicationContext(), IncomingAssetActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private Map<String, List<SiteInfoRS>> fillAvramarData() {
        Map<String, List<SiteInfoRS>> result = new HashMap<>();
        List<Site> allSites = db.siteDAO().getAll();
        allSites.removeIf(s -> s.id.equals(LocalPreferences.getCurrentSiteId()));
        if (allSites != null && !allSites.isEmpty()) {
            result = allSites.stream().filter(x -> x.siteLevel == 3).map(s -> new SiteInfoRS(s.name, s.description, s.lvl2)).collect(Collectors.groupingBy(SiteInfoRS::getCode));
        }

        return result;
    }

    private List<String> fillSupplierData() {
        List<String> result = new ArrayList<>();
        List<Supplier> allSuppliers = db.supplierDAO().getAll();
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
            avramarDialog = new ExpandableListDialog(IncomingStartActivity.this, fillAvramarData(), fromAvramarSelection, R.string.select_site);
            avramarDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftAvramar;
        } else if (checkedId == R.id.tbSupplier) {
            supplierDialog = new SimpleListDialog(IncomingStartActivity.this, fillSupplierData(), fromSupplierSelection, R.string.select_supplier);
            supplierDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSupplier;
        } else if (checkedId == R.id.tbAssetFrom) {
            recWHIncoming.fromSite = Constants.ftAsset;
            tvIncomingFrom.setText(Constants.ftAsset);
            selectedToggleButtonFrom = Constants.ftAsset;
        }/* else if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(IncomingStartActivity.this, fillSubSiteData(), toSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSite;
        }*/ else if (checkedId == R.id.tbAssetTo) {
            recWHIncoming.toSite = Constants.ftAsset;
            tvIncomingTo.setText(Constants.ftAsset);
            selectedToggleButtonTo = Constants.ftAsset;
        }
        if (checkedId == R.id.tbAsset) {
            selectedIncomingItemType = Constants.ftAsset;
        } else if (checkedId == R.id.tbConsumable) {
            selectedIncomingItemType = Constants.ftConsumable;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whIncomingRecord = recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(selectedIncomingItemType)) {
            whIncomingRecord.incomingItemType = selectedIncomingItemType;
        }

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonFrom)) {
            whIncomingRecord.selectedToggleButtonFrom = selectedToggleButtonFrom;
        }

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonTo)) {
            whIncomingRecord.selectedToggleButtonTo = selectedToggleButtonTo;
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvIncomingFrom))) {
            whIncomingRecord.fromSite = tvIncomingFrom.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvIncomingTo))) {
            whIncomingRecord.toSite = tvIncomingTo.getText().toString();
        }

        return whIncomingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            /*if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.incomingItemType)) {
                sb.append(String.format("\n%s is missing", "'Item type'"));
            }*/

            if (Strings.isEmptyOrWhitespace(recWHIncoming.fromSite)) {
                sb.append(String.format("\n%s is missing", "'Source site'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHIncoming.toSite)) {
                sb.append(String.format("\n%s is missing", "'Target site'"));
            }
        }

        return sb.toString();
    }

    private void initControlsFromState() {

        selectedToggleButtonTo = Constants.ftSite;
        tgIncomingDestination.setCheckedStateForView(R.id.tbSite, true);
        recWHIncoming.toSite = LocalPreferences.getCurrentSiteName();
        tgIncomingDestination.setEnabled(false);

        if (Constants.ftAvramar.equalsIgnoreCase(recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.setCheckedStateForView(R.id.tbAvramar, true);
            avramarDialog.dismiss();
        } else if (Constants.ftSupplier.equalsIgnoreCase(recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.setCheckedStateForView(R.id.tbSupplier, true);
            supplierDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftSite.equalsIgnoreCase(recWHIncoming.selectedToggleButtonTo)) {
            tgIncomingDestination.setCheckedStateForView(R.id.tbSite, true);
//            siteDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(recWHIncoming.selectedToggleButtonTo)) {
            tgIncomingDestination.check(R.id.tbAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbConsumable);
        }

        if (!Strings.isEmptyOrWhitespace(recWHIncoming.fromSite)) {
            tvIncomingFrom.setText(recWHIncoming.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(recWHIncoming.toSite)) {
            tvIncomingTo.setText(recWHIncoming.toSite);
        }
    }
}