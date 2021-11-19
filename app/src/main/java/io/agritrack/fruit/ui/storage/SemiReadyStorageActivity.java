package io.agritrack.fruit.ui.storage;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.Employee;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.fruit.ui.planting.PlantingConfirmActivity;
import io.agritrack.ui.service.LocalPreferences;

public class SemiReadyStorageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private ListView lvGreenhouse;
    private List<io.agritrack.ui.bo.GenericListModel> greenhouse;

    private ArrayAdapter<io.agritrack.ui.bo.GenericListModel> greenhouseAdapter;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semi_ready_storage);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSemiReadyStorage);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get main controls references
        this.lvGreenhouse = findViewById(R.id.lvGreenhouse);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvGreenhouse.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.greenhouse = teamCandidates.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.fullName())).collect(Collectors.toList());
            greenhouseAdapter = new ArrayAdapter<io.agritrack.ui.bo.GenericListModel>(this, android.R.layout.simple_list_item_checked, greenhouse) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvGreenhouse.setAdapter(greenhouseAdapter);
            this.lvGreenhouse.setOnItemClickListener(this);
        }

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToSemiReadyStorage);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PlantingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {

    }

    private void updateState() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        io.agritrack.ui.bo.GenericListModel member = (io.agritrack.ui.bo.GenericListModel) this.lvGreenhouse.getItemAtPosition(position);
        member.setChecked(!currentCheck);
    }
}