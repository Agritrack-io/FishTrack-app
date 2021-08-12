package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.common.Employee;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.adapter.CheckableListAdapter;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingTeamActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private ListView lvFishingTeam;
    private TemplateRecyclerAdapter adapterSelectedTeam;
    private ArrayList<String> selectedTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_team);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // instantiate the holder list for Selected Team Members
        this.selectedTeam = new ArrayList<>();

        // get main controls references
        this.lvFishingTeam = findViewById(R.id.lvFishingTeam);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if(teamCandidates!=null && !teamCandidates.isEmpty()) {
            GenericListModel[] candidates = teamCandidates.stream().map(x->new GenericListModel(x.id, x.lastName + " " + x.firstName)).toArray(GenericListModel[]::new);
            CheckableListAdapter candidatesAdapter = new CheckableListAdapter(this, candidates);

            this.lvFishingTeam.setAdapter(candidatesAdapter);
            this.lvFishingTeam.setOnItemClickListener(this);
        }

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // create Footer
        configFooter();
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCage);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        if (GlobalState.getInstance().recHarvest == null) {
            return;
        }
        HarvestRecord hvst = GlobalState.getInstance().recHarvest;

        if (hvst.fishingTeam != null) {
            this.adapterSelectedTeam.setValues((ArrayList<String>) hvst.fishingTeam);
            this.adapterSelectedTeam.notifyDataSetChanged();

            //Get reference of selected Team Count textView
            TextView tvEmployeesCount = findViewById(R.id.tvEmployeesCount);
            tvEmployeesCount.setText(String.valueOf(this.selectedTeam.size()));
        }
    }

    private void updateState() {
        long[] aa = lvFishingTeam.getCheckedItemIds();

        GlobalState.getInstance().recHarvest.fishingTeam = this.selectedTeam;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //lvFishingTeam.getCheckedItemPositions();
        runOnUiThread(() -> {
            final int cnt = ((ListView)parent).getCheckedItemCount();
            ((TextView)findViewById(R.id.tvEmployeesCount)).setText(String.valueOf(cnt)); ;
        });
    }
}