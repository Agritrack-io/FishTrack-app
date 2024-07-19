package io.agritrack.philosofish.fish.ui.wh.outgoing;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recWHOutgoing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

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

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.common.Constants;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.Site;
import io.agritrack.philosofish.data.model.common.Customer;
import io.agritrack.philosofish.dialog.ExpandableListDialog;
import io.agritrack.philosofish.dialog.SimpleListDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.WHTxRecord;
import io.agritrack.philosofish.fish.ui.WhMenuActivity;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.login.api.SiteInfoRS;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class OutgoingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<SiteInfoRS> toAvramarSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toCustomerSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();

    private TextView tvOutgoingFrom, tvOutgoingTo;
    private ToggleGroup tgOutgoingSource, tgOutgoingDestination, tgOutgoingItemType;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private String selectedOutgoingItemType = Constants.ftAsset;
    private ExpandableListDialog avramarDialog;
    private SimpleListDialog customerDialog;
    private SimpleListDialog siteDialog;
    private String fromSite;
    private String toCustomer;
    private SiteInfoRS toSite;
    private MobileDB db;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        toAvramarSelection.observe(this, response -> {
            if (response != null) {
                toSite = response;
                tvOutgoingTo.setText(toSite.getName());
                avramarDialog.dismiss();
            }
        });

        toCustomerSelection.observe(this, response -> {
            if (response != null) {
                toCustomer = response;
                tvOutgoingTo.setText(toCustomer);
                customerDialog.dismiss();
            }
        });

        fromSiteSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvOutgoingFrom.setText(fromSite);
                siteDialog.dismiss();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(OutgoingStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToOutgoingProcess);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else if (selectedOutgoingItemType == Constants.ftAsset) {
                Intent i = new Intent(getApplicationContext(), OutgoingAssetActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvOutgoingFrom = findViewById(R.id.tvOutgoingFrom);
        tvOutgoingTo = findViewById(R.id.tvOutgoingTo);
        tgOutgoingSource = findViewById(R.id.tgOutgoingSource);
        tgOutgoingDestination = findViewById(R.id.tgOutgoingDestination);
        tgOutgoingItemType = findViewById(R.id.tgOutgoingItemType);
        tgOutgoingItemType.setOnCheckedChangeListener(this);
        tgOutgoingSource.setOnCheckedChangeListener(this);
        tgOutgoingDestination.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
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

    private List<String> fillCustomerData() {
        List<String> result = new ArrayList<>();
        List<Customer> allCustomers = db.customerDAO().getAll();
        if (allCustomers != null && !allCustomers.isEmpty()) {
            result = allCustomers.stream().map(s -> s.name).collect(Collectors.toList());
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
        /*if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(OutgoingStartActivity.this, fillSubSiteData(), fromSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSite;
        } else */
        if (checkedId == R.id.tbAssetFrom) {
            recWHOutgoing.fromSite = Constants.ftAsset;
            tvOutgoingFrom.setText(Constants.ftAsset);
            selectedToggleButtonFrom = Constants.ftAsset;
        } else if (checkedId == R.id.tbAvramar) {
            avramarDialog = new ExpandableListDialog(OutgoingStartActivity.this, fillAvramarData(), toAvramarSelection, R.string.select_site);
            avramarDialog.showDialog();
            selectedToggleButtonTo = Constants.ftAvramar;
        } else if (checkedId == R.id.tbCustomer) {
            customerDialog = new SimpleListDialog(OutgoingStartActivity.this, fillCustomerData(), toCustomerSelection, R.string.select_customer);
            customerDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSupplier;
        } else if (checkedId == R.id.tbOutAssetTo) {
            recWHOutgoing.toSite = Constants.ftAsset;
            tvOutgoingTo.setText(Constants.ftAsset);
            selectedToggleButtonTo = Constants.ftAsset;
        }
        if (checkedId == R.id.tbAsset) {
            selectedOutgoingItemType = Constants.ftAsset;
        } else if (checkedId == R.id.tbConsumable) {
            selectedOutgoingItemType = Constants.ftConsumable;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whOutgoingRecord = recWHOutgoing;

        if (!Strings.isEmptyOrWhitespace(selectedOutgoingItemType)) {
            whOutgoingRecord.outgoingItemType = selectedOutgoingItemType;
        }

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonFrom)) {
            whOutgoingRecord.selectedToggleButtonFrom = selectedToggleButtonFrom;
        }

        if (!Strings.isEmptyOrWhitespace(selectedToggleButtonTo)) {
            whOutgoingRecord.selectedToggleButtonTo = selectedToggleButtonTo;
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvOutgoingFrom))) {
            whOutgoingRecord.fromSite = tvOutgoingFrom.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(String.valueOf(tvOutgoingTo))) {
            whOutgoingRecord.toSite = tvOutgoingTo.getText().toString();
        }
        return whOutgoingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
//            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.outgoingItemType)) {
//                sb.append(String.format("\n%s is missing", "'Item type'"));
//            }

            if (Strings.isEmptyOrWhitespace(recWHOutgoing.toSite)) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.target_site)));

            }

            if (Strings.isEmptyOrWhitespace(recWHOutgoing.fromSite)) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.source_site)));
            }
        }

        return sb.toString();
    }

    private void initControlsFromState() {

        selectedToggleButtonFrom = Constants.ftSite;
        tgOutgoingSource.setCheckedStateForView(R.id.tbSite, true);
        recWHOutgoing.fromSite = LocalPreferences.getCurrentSiteName();
        tgOutgoingSource.setEnabled(false);

        if (Constants.ftSite.equalsIgnoreCase(recWHOutgoing.selectedToggleButtonFrom)) {
            tgOutgoingSource.setCheckedStateForView(R.id.tbSite, true);
//            siteDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(recWHOutgoing.selectedToggleButtonFrom)) {
            tgOutgoingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftAvramar.equalsIgnoreCase(recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.setCheckedStateForView(R.id.tbAvramar, true);
            avramarDialog.dismiss();
        } else if (Constants.ftCustomer.equalsIgnoreCase(recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.setCheckedStateForView(R.id.tbCustomer, true);
            customerDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.check(R.id.tbOutAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbConsumable);
        }

        if (!Strings.isEmptyOrWhitespace(recWHOutgoing.fromSite)) {
            tvOutgoingFrom.setText(recWHOutgoing.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(recWHOutgoing.toSite)) {
            tvOutgoingTo.setText(recWHOutgoing.toSite);
        }
    }
}