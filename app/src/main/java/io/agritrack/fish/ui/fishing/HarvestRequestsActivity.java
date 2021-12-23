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

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.service.LocalPreferences;

public class HarvestRequestsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private MobileDB db;
    private ListView lvHarvestRequests;
    private GenericListModel[] harvestReqs;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private long harvestRQcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_requests);

        // get main controls references
        this.lvHarvestRequests = findViewById(R.id.lvHarvestRequests);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvHarvestRequests.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestReq);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load Harvest Request fetched via Synch op.
        List<HarvestRequest> harvestRequests = db.harvestRequestsDAO().getAll();
        if (harvestRequests != null && !harvestRequests.isEmpty()) {
            //
            this.harvestReqs = harvestRequests.stream().map(x -> new GenericListModel(x.id, String.format("%s, %s kg, %s", x.cageCode, x.reqQty, x.fishName))).toArray(GenericListModel[]::new);
            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, android.R.layout.simple_list_item_checked, harvestReqs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            //
            this.lvHarvestRequests.setAdapter(candidatesAdapter);
            this.lvHarvestRequests.setOnItemClickListener(this);
            this.harvestRQcnt = harvestRequests.size();
        }

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
        GenericListModel member = (GenericListModel) this.lvHarvestRequests.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        HarvestRequest harvestRq = db.harvestRequestsDAO().getById(member.getId().toString());
        if (harvestRq != null) {
            GlobalState.recFishing.harvestRqPkId = harvestRq.id;
            GlobalState.recFishing.harvestRq = harvestRq.requestId;
            GlobalState.recFishing.speciesName = harvestRq.fishName;
            GlobalState.recFishing.cageCode = harvestRq.cageCode;
            GlobalState.recFishing.expectedCageRFID = harvestRq.cageRFID;
            GlobalState.recFishing.requesterName = harvestRq.requester;
            GlobalState.recFishing.fishSize = harvestRq.fishSize;
            GlobalState.recFishing.reqWeight = harvestRq.reqQty;
            GlobalState.recFishing.notes = harvestRq.notes;
            GlobalState.recFishing.packagingPlant = harvestRq.packagingPlant;
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