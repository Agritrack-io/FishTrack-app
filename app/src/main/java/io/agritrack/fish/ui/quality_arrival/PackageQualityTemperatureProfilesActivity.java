package io.agritrack.fish.ui.quality_arrival;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.data.model.common.Employee;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.ui.service.LocalPreferences;

public class PackageQualityTemperatureProfilesActivity extends AppCompatActivity {

    private ListView lvTempProfiles;
    private ArrayAdapter<io.agritrack.ui.bo.GenericListModel> tempProfileAdapter;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_temperature_profiles);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityTemperatureProfiles);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get main controls references
        this.lvTempProfiles = findViewById(R.id.lvTempProfiles);

        /*// load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.candidates = teamCandidates.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.fullName())).collect(Collectors.toList());
            tempProfileAdapter = new ArrayAdapter<io.agritrack.ui.bo.GenericListModel>(this, R.layout.temperature_profile, candidates) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvTempProfiles.setAdapter(candidatesAdapter);
            this.lvTempProfiles.setOnItemClickListener(this);
        }*/

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityTemperatureProfilesActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        lvTempProfiles = findViewById(R.id.lvTempProfiles);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityInfo);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackageQualityInfoActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackageQualityStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        /*if (prcRecord.availBins != null) {
            adapterBins.setValues(new LinkedList<String>(prcRecord.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }*/
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
       /* if (!IsDemo) {
            if (GlobalState.recProcessing.availBins == null || GlobalState.recProcessing.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
            }
        }*/
        return sb.toString();
    }

    private void updateState() {

        }


}