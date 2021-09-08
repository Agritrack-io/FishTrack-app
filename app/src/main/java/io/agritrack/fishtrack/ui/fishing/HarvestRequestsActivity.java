package io.agritrack.fishtrack.ui.fishing;

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

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.HarvestRequest;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;
import static io.agritrack.fishtrack.state.GlobalState.recFishing;

public class HarvestRequestsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private MobileDB db;
    private ListView lvHarvestRequests;
    private GenericListModel[] harvestReqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_requests);

        // get main controls references
        this.lvHarvestRequests = findViewById(R.id.lvHarvestRequests);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvHarvestRequests.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestReq);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load Harvest Request fetched via Synch op.
        List<HarvestRequest> harvestRequests = db.harvestRequestsDAO().getAll();
        if (harvestRequests != null && !harvestRequests.isEmpty()) {
            this.harvestReqs = harvestRequests.stream().map(x -> new GenericListModel(x.id, String.format("%s, %s", x.reqQty, x.fishName))).toArray(GenericListModel[]::new);
            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, android.R.layout.simple_list_item_checked, harvestReqs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvHarvestRequests.setAdapter(candidatesAdapter);
            this.lvHarvestRequests.setOnItemClickListener(this);
        }

        // create Footer
        configFooter();
    }


    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToStartFishing);
        ivNext.setOnClickListener(view -> {
            //updateState();
            String v = null; //validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToHomeMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
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
            recFishing.harvestRqPkId = harvestRq.id;
            recFishing.harvestRq = harvestRq.requestId;
            recFishing.speciesName = harvestRq.fishName;
            recFishing.requesterName = harvestRq.requester;
            recFishing.reqWeight = harvestRq.reqQty;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}