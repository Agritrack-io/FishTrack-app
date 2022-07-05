package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.time.format.DateTimeFormatter;
import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.FishingRequest;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class HarvestRequestsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ToggleGroup.OnCheckedChangeListener {
    private MobileDB db;
    private ListView lvFishingRequests;
    private GenericListModel[] fishingRQs;
    private ToggleGroup tgChooseDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private long harvestRQcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_requests);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestRequests);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get main controls references
        this.lvFishingRequests = findViewById(R.id.lvHarvestRequests);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFishingRequests.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        tgChooseDate = findViewById(R.id.tgChooseDate);
        tgChooseDate.setOnCheckedChangeListener(this);
        tgChooseDate.check(R.id.tbToday);
        getTodayHarvestReq();

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestRequestsActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    // Define 'back' / 'next' Buttons functionality
    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToStartFishing);
        ivNext.setOnClickListener(view -> {
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(v), Toast.LENGTH_LONG);
            } else {
                // transfer existing Record Data to Entity and persist to db.
                GlobalState.commitFishing(this.db, false);

                Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToHomeMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvFishingRequests.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        FishingRequest harvestRq = db.fishingRequestsDAO().getById(member.getRequestId());
        if (harvestRq != null) {

            GlobalState.recFishing.fishingRq = harvestRq.requestId;
            GlobalState.recFishing.requesterName = harvestRq.requester;
            GlobalState.recFishing.speciesName = harvestRq.species;
            GlobalState.recFishing.cageCode = harvestRq.cageCode;
            GlobalState.recFishing.typedCageCode = harvestRq.cageCode;
            GlobalState.recFishing.expectedCageRFID = harvestRq.cageRFID;
            GlobalState.recFishing.averageWeight = harvestRq.averageWeight;
            GlobalState.recFishing.reqWeight = harvestRq.reqQty;
            GlobalState.recFishing.notes = harvestRq.notes;
            GlobalState.recFishing.packagingPlant = harvestRq.packagingPlant;
        }
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        /*LocalDate now = LocalDate.now();
        String nowDate = now.format(formatter);
        LocalDate yesterday = now.minusDays(1);
        String yesterdayDate = yesterday.format(formatter);
        LocalDate date = now.minusDays(2);
        String previousDate = date.format(formatter);*/

        if (checkedId == R.id.tbToday) {
            getTodayHarvestReq();
        } else if (checkedId == R.id.tbYesterday) {
            getYesterdayHarvestReq();
        } else if (checkedId == R.id.tbOlderDays) {
            getPreviousHarvestReq();
        }
    }

    private void getTodayHarvestReq(){
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getTodayRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            //this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, "%s, %s, %s kg, %s", x.itinSNo, x.harvestDate.substring(0, x.harvestDate.indexOf("T")), x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);
            this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.harvestDate, x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
    }

    private void getYesterdayHarvestReq(){
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getYesterdayRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            //this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.harvestDate.substring(0, x.harvestDate.indexOf("T")), x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);
            this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.harvestDate, x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
    }

    private void getPreviousHarvestReq(){
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getPreviousRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            //
            this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.harvestDate, x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            //
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (this.harvestRQcnt == 0) {
            sb.append("No Harvest Requests available. \nPlz contact Harvest Dept.");
        } else if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.speciesName)) {
                sb.append(String.format("Please Select a Harvest Request to proceed", ""));
            }
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}