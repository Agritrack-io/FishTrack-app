package io.agritrack.fishtrack.ui.maintenance;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.common.Employee;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;

public class MaintenanceInternalTeamActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private List<GenericListModel> selectedTeam;
    private ListView lvTeam;
    private ArrayAdapter<GenericListModel> candidatesAdapter;
    private TextView tvInMtTeamCount;
    private EditText atvInMtWorkDescription;
    private ImageButton ivAddEmployee;
    private String memberName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_team);

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalTeam);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvTeam.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.selectedTeam = teamCandidates.stream().map(x -> new GenericListModel(x.id, x.fullName())).collect(Collectors.toList());
            candidatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, selectedTeam);

            this.lvTeam.setAdapter(candidatesAdapter);
            this.lvTeam.setOnItemClickListener(this);
        }

        ivAddEmployee.setOnClickListener(view -> {
            showAddDialog();
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirmInternal);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), MaintenanceInternalConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMaintenanceInternalStartMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceInternalStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        lvTeam = findViewById(R.id.lvTeam);
        tvInMtTeamCount = findViewById(R.id.tvInMtTeamCount);
        atvInMtWorkDescription = findViewById(R.id.atvInMtWorkDescription);
        ivAddEmployee = (ImageButton) findViewById(R.id.ivAddEmployee);
    }

    private void initControlsFromState() {
        if (GlobalState.recInternalRepair.repairTeam != null) {
            int sz = GlobalState.recInternalRepair.repairTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i = 0; i < sz; i++) {
                this.lvTeam.setItemChecked(GlobalState.recInternalRepair.repairTeam.get(i).intValue(), Boolean.TRUE);
            }
            //Get reference of selected Team Count textView
            tvInMtTeamCount.setText(String.valueOf(sz));
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.remarks)) {
            atvInMtWorkDescription.setText(GlobalState.recInternalRepair.remarks);
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type member's name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                memberName = input.getText().toString();
                candidatesAdapter.add(new GenericListModel(null, memberName, Boolean.TRUE));
                candidatesAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void updateState() {
        GlobalState.recInternalRepair.teamSize = tvInMtTeamCount.getText().toString();
        // reset the list of selected Indexes.
        GlobalState.recInternalRepair.repairTeam = new ArrayList<>();
        SparseBooleanArray sp = this.lvTeam.getCheckedItemPositions();
        for (int idx = 0; idx < sp.size(); idx++) {
            if (sp.valueAt(idx)) {
                GlobalState.recInternalRepair.repairTeam.add(Long.valueOf(sp.keyAt(idx)));
            }
        }

        GlobalState.recInternalRepair.remarks = atvInMtWorkDescription.getText().toString();
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (GlobalState.recInternalRepair.repairTeam == null || GlobalState.recInternalRepair.repairTeam.isEmpty()) {
            sb.append(String.format("\n%s is missing", "'Team members'"));
        }

        return sb.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvTeam.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        //Get reference of selected Team Count textView
        tvInMtTeamCount.setText(String.valueOf(this.lvTeam.getCheckedItemCount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}