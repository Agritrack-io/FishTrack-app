package io.agritrack.fishtrack.ui.wh.incoming;

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

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.data.model.common.Supplier;
import io.agritrack.fishtrack.dialog.ExpandableListDialog;
import io.agritrack.fishtrack.dialog.SimpleListDialog;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.WHTxRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class IncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<SiteInfo> fromAvramarSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSupplierSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();

    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination, tgIncomingItemType;
    private String selectedIncomingItemType;
    private ExpandableListDialog avramarDialog;
    private SimpleListDialog supplierDialog;
    private SimpleListDialog siteDialog;
    private SiteInfo fromSite;
    private String fromSupplier;
    private String toSite;
    private MobileDB db;

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
            } else {
                Intent i = new Intent(getApplicationContext(), IncomingConsumableActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private Map<String, List<SiteInfo>> fillAvramarData() {
        Map<String, List<SiteInfo>> result = new HashMap<>();
        List<Site> allSites = db.siteDAO().getAll();
        if (allSites != null && !allSites.isEmpty()) {
           result = allSites.stream().filter(x->x.lvl2 != null).map(s -> new SiteInfo(s.name, s.description, s.lvl2)).collect(Collectors.groupingBy(SiteInfo::getCode));
        }

        return result;
    }

    private List<String> fillSupplierData() {
        List<String> result = new ArrayList<>();
        List<Supplier> allSuppliers = db.supplierDAO().getAll();
        if (allSuppliers != null && !allSuppliers.isEmpty()) {
            result = allSuppliers.stream().map(s ->s.name).collect(Collectors.toList());
        }

        return result;
    }

    private List<String> fillSubSiteData() {
        List<String> result = new ArrayList<>();
        List<Site> subSites = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (subSites != null && !subSites.isEmpty()) {
            result = subSites.stream().map(s ->s.name).collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbAvramar) {
            avramarDialog = new ExpandableListDialog(IncomingStartActivity.this, fillAvramarData(), fromAvramarSelection, R.string.select_site);
            avramarDialog.showDialog();
        } else if (checkedId == R.id.tbSupplier) {
            supplierDialog = new SimpleListDialog(IncomingStartActivity.this, fillSupplierData(), fromSupplierSelection, R.string.select_supplier);
            supplierDialog.showDialog();
        } else if (checkedId == R.id.tbAssetFrom) {
            GlobalState.recWHIncoming.from = Constants.ftAsset;
            tvIncomingFrom.setText(Constants.ftAsset);
        } else if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(IncomingStartActivity.this, fillSubSiteData(), toSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
        } else if (checkedId == R.id.tbAssetTo) {
            GlobalState.recWHIncoming.to = Constants.ftAsset;
            tvIncomingTo.setText(Constants.ftAsset);
        }
        if (checkedId == R.id.tbAsset) {
            selectedIncomingItemType = Constants.ftAsset;
        } else if (checkedId == R.id.tbConsumable) {
            selectedIncomingItemType = Constants.ftConsumable;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whIncomingRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(selectedIncomingItemType)) {
            whIncomingRecord.incomingItemType = selectedIncomingItemType;
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

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.incomingItemType)) {
            sb.append(String.format("\n%s is missing", "'Item type'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)) {
            sb.append(String.format("\n%s is missing", "'Source site'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)) {
            sb.append(String.format("\n%s is missing", "'Target site'"));
        }

        return sb.toString();
    }

    private void initControlsFromState() {

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbAvramar);
        } else if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbSupplier);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHIncoming.to)) {
            tgIncomingDestination.check(R.id.tbSite);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.to)) {
            tgIncomingDestination.check(R.id.tbAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(GlobalState.recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbConsumable);
        }
    }
}