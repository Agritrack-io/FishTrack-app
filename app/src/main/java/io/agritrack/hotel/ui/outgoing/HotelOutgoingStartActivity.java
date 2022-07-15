package io.agritrack.hotel.ui.outgoing;

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
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class HotelOutgoingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<String> toCustomerSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();

    private TextView tvOutgoingFrom, tvOutgoingTo;
    private ToggleGroup tgOutgoingSource, tgOutgoingDestination;
    private String  selectedToggleButtonFrom, selectedToggleButtonTo;
    private SimpleListDialog customerDialog;
    private SimpleListDialog siteDialog;
    private String fromSite;
    private String toSite;
    private String toCustomer;
    private MobileDB db;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_outgoing_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

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

        toSiteSelection.observe(this, response -> {
            if (response != null) {
                toSite = response;
                tvOutgoingTo.setText(toSite);
                siteDialog.dismiss();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelOutgoingStartActivity.this);
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
                return;
            }
            Intent i = new Intent(getApplicationContext(), HotelOutgoingLinenActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonFrom)) {
            tgOutgoingSource.setCheckedStateForView(R.id.tbFromSite, true);
            if(siteDialog != null) {
                siteDialog.dismiss();
            }
        }

        if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHOutgoing.selectedToggleButtonTo)) {
            tgOutgoingDestination.setCheckedStateForView(R.id.tbOutLaundry, true);
            if(customerDialog != null) {
                customerDialog.dismiss();
            }
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.fromSite)) {
            tvOutgoingFrom.setText(GlobalState.recWHOutgoing.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.toSite)) {
            tvOutgoingTo.setText(GlobalState.recWHOutgoing.toSite);
        }
    }

    private void assignCtrlVars() {
        tvOutgoingFrom = findViewById(R.id.tvOutgoingFrom);
        tvOutgoingTo = findViewById(R.id.tvOutgoingTo);
        tgOutgoingSource = findViewById(R.id.tgOutgoingSource);
        tgOutgoingDestination = findViewById(R.id.tgOutgoingDestination);
        tgOutgoingSource.setOnCheckedChangeListener(this);
        tgOutgoingDestination.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private List<String> fillLaundryList() {
        List<String> result = new ArrayList<>();
        List<Site> allLaundries = db.siteDAO().getAllBySiteType("LAUNDRY");
        if (allLaundries != null && !allLaundries.isEmpty()) {
            result = allLaundries.stream().map(s -> s.name).collect(Collectors.toList());
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
        if (checkedId == R.id.tbFromSite) {
            siteDialog = new SimpleListDialog(HotelOutgoingStartActivity.this, fillSubSiteData(), fromSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSite;
        } else if (checkedId == R.id.tbOutLaundry) {
            customerDialog = new SimpleListDialog(HotelOutgoingStartActivity.this, fillLaundryList(), toCustomerSelection, R.string.select_customer);
            customerDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSupplier;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whOutgoingRecord = GlobalState.initWHOutgoingRecord();

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
            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.toSite)) {
                sb.append(String.format("\n%s is missing", "'Target site'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.fromSite)) {
                sb.append(String.format("\n%s is missing", "'Source site'"));
            }
        }

        return sb.toString();
    }
}