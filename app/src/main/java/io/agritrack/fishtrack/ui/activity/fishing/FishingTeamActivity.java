package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingTeamActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private ListView lvFishingTeam;
    private ArrayAdapter adapterSelectedTeam;
    private ArrayList<String> selectedTeam;
    private GenericListModel[] candidates;

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

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFishingTeam.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.candidates = teamCandidates.stream().map(x -> new GenericListModel(x.id, x.fullName())).toArray(GenericListModel[]::new);
            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, candidates);

            this.lvFishingTeam.setAdapter(candidatesAdapter);
            this.lvFishingTeam.setOnItemClickListener(this);
        }

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCage);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });

    }

    private void initControlsFromState() {

        if (GlobalState.recFishing.fishingTeam != null) {
            int sz = GlobalState.recFishing.fishingTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i = 0; i < sz; i++) {
                this.lvFishingTeam.setItemChecked(GlobalState.recFishing.fishingTeam.get(i).intValue(), Boolean.TRUE);
            }

            //Get reference of selected Team Count textView
            TextView tvEmployeesCount = findViewById(R.id.tvEmployeesCount);
            tvEmployeesCount.setText(String.valueOf(sz));
        }
    }

    private void updateState() {
        // reset the list of selected Indexes.
        GlobalState.recFishing.fishingTeam = new ArrayList<>();
        SparseBooleanArray sp = this.lvFishingTeam.getCheckedItemPositions();
        for (int idx = 0; idx < sp.size(); idx++) {
            if (sp.valueAt(idx)) {
                GlobalState.recFishing.fishingTeam.add(Long.valueOf(sp.keyAt(idx)));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvFishingTeam.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        //Get reference of selected Team Count textView
        TextView tvEmployeesCount = findViewById(R.id.tvEmployeesCount);
        tvEmployeesCount.setText(String.valueOf(this.lvFishingTeam.getCheckedItemCount()));
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
            db = null;
        }
        super.onDestroy();
    }
}