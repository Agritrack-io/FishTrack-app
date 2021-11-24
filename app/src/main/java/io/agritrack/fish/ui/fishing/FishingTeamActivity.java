package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.stream.IntStream;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.Employee;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.service.LocalPreferences;

public class FishingTeamActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private ListView lvFishingTeam;
    private List<GenericListModel> candidates;

    private ArrayAdapter<GenericListModel> candidatesAdapter;
    private ImageButton ivAddEmployee;
    private String memberName;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_team);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingTeam);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get main controls references
        this.lvFishingTeam = findViewById(R.id.lvFishingTeam);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFishingTeam.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.candidates = teamCandidates.stream().map(x -> new GenericListModel(x.id, x.fullName())).collect(Collectors.toList());
            candidatesAdapter = new ArrayAdapter<GenericListModel>(this, android.R.layout.simple_list_item_checked, candidates) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvFishingTeam.setAdapter(candidatesAdapter);
            this.lvFishingTeam.setOnItemClickListener(this);
        }

        ivAddEmployee.setOnClickListener(view -> {
            showAddDialog();
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingTeamActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingTeamActivity.this);
            infoDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCage);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        ivAddEmployee = (ImageButton) findViewById(R.id.ivAddEmployee);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {

        if (GlobalState.recFishing.fishingTeam != null) {
            int[] matchingIndices = IntStream.range(0, this.candidates.size())
                    .filter(i -> GlobalState.recFishing.fishingTeam.contains(this.candidates.get(i).toString()))
                    .toArray();

            int sz = GlobalState.recFishing.fishingTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i : matchingIndices) {
                this.lvFishingTeam.setItemChecked(i, Boolean.TRUE);
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
                GlobalState.recFishing.fishingTeam.add(((GenericListModel) this.lvFishingTeam.getAdapter().getItem(sp.keyAt(idx))).toString());
            }
        }
        GlobalState.commitFishing(db, Boolean.FALSE);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if(!IsDemo) {
            if (GlobalState.recFishing.fishingTeam == null || GlobalState.recFishing.fishingTeam.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Team members'"));
            }
        }

        return sb.toString();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}