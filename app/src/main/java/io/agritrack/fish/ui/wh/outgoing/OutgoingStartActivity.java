package io.agritrack.fish.ui.wh.outgoing;

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
import io.agritrack.data.model.common.Customer;
import io.agritrack.dialog.ExpandableListDialog;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.wh.zebra.outgoing.ZebraOutgoingAssetActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.SiteInfoRS;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

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
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else if (selectedOutgoingItemType == Constants.ftAsset){
                Intent i = new Intent(getApplicationContext(), OutgoingAssetActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), OutgoingConsumableActivity.class);
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
        if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(OutgoingStartActivity.this, fillSubSiteData(), fromSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSite;
        } else if (checkedId == R.id.tbAssetFrom) {
            GlobalState.recWHOutgoing.fromSite = Constants.ftAsset;
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
            GlobalState.recWHOutgoing.toSite = Constants.ftAsset;
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
        WHTxRecord whOutgoingRecord = GlobalState.recWHOutgoing;

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

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.outgoingItemType)) {
                sb.append(String.format("\n%s is missing", "'Item type'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.toSite)) {
                sb.append(String.format("\n%s is missing", "'Target site'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.fromSite)) {
                sb.append(String.format("\n%s is missing", "'Source site'"));
            }
        }

        return sb.toString();
    }

    private void initControlsFromState()    {

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonFrom)) {
            tgOutgoingSource.setCheckedStateForView(R.id.tbSite, true);
            siteDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonFrom)) {
            tgOutgoingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.setCheckedStateForView(R.id.tbAvramar, true);
            avramarDialog.dismiss();
        } else if (Constants.ftCustomer.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.setCheckedStateForView(R.id.tbCustomer, true);
            customerDialog.dismiss();
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.check(R.id.tbOutAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(GlobalState.recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbConsumable);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.fromSite)) {
            tvOutgoingFrom.setText(GlobalState.recWHOutgoing.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.toSite)) {
            tvOutgoingTo.setText(GlobalState.recWHOutgoing.toSite);
        }
    }
}