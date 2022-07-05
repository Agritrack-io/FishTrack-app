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

public class HotelIncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final MutableLiveData<String> fromSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();
    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private SimpleListDialog supplierDialog;
    private SimpleListDialog siteDialog;
    private String fromSupplier;
    private String toSite;
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        fromSelection.observe(this, response -> {
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

    private void initControlsFromState() {

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonTo)) {
            tgIncomingDestination.setCheckedStateForView(R.id.tbSite, true);
            if(siteDialog != null) {
                siteDialog.dismiss();
            }
        }

        if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHIncoming.selectedToggleButtonFrom)) {
            tgIncomingSource.setCheckedStateForView(R.id.tbSupplier, true);
            if(supplierDialog != null) {
                supplierDialog.dismiss();
            }
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)) {
            tvIncomingFrom.setText(GlobalState.recWHIncoming.from);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)) {
            tvIncomingTo.setText(GlobalState.recWHIncoming.to);
        }
    }

    private List<String> fillSiteData(String siteTp) {
        List<String> result = new ArrayList<>();
        List<Site> allSuppliers = db.siteDAO().getAllBySiteType(siteTp);
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
        if (checkedId == R.id.tbSupplier) {
            supplierDialog = new SimpleListDialog(HotelIncomingStartActivity.this, fillSiteData("LAUNDRY"), fromSelection, R.string.select_supplier);
            supplierDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSupplier;
        } else if (checkedId == R.id.tbSite) {
            siteDialog = new SimpleListDialog(HotelIncomingStartActivity.this, fillSubSiteData(), toSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSite;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whIncomingRecord = GlobalState.initWHIncomingRecord();

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
}