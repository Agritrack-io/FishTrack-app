package io.agritrack.philosofish.fish.ui.quality.receipt;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recLoggerData;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.DoubleSummaryStatistics;
import java.util.Map;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.LoggerDataRecord;
import io.agritrack.philosofish.ui.adapter.TemperatureProfileAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class ReceiptQualityTemperatureProfilesActivity extends AppCompatActivity {

    private RecyclerView lvTempProfiles;
    private TemperatureProfileAdapter tempProfileAdapter;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_temperature_profiles);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityTemperatureProfiles);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get main controls references
        this.lvTempProfiles = findViewById(R.id.lvTempProfiles);

        tempProfileAdapter = new TemperatureProfileAdapter(this);
        lvTempProfiles.setAdapter(tempProfileAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ReceiptQualityTemperatureProfilesActivity.this);
        lvTempProfiles.setLayoutManager(layoutManager);
        lvTempProfiles.setHasFixedSize(false);

        tempProfileAdapter.notifyDataSetChanged();

//        for(int i=0; i<=1; i++){
//            CToast(getApplicationContext(), render(R.string.press_card_to_see_temps), Toast.LENGTH_LONG);
//        }

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityTemperatureProfilesActivity.this);
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
//            String v = validate();
//            if (!Strings.isEmptyOrWhitespace(v)) {
//                CToast(getApplicationContext(), render("Errors : " + v), Toast.LENGTH_LONG);
//            } else {
//                updateState();
                Intent i = new Intent(getApplicationContext(), ReceiptQualityInfoActivity.class);
                startActivity(i);
//            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        Map<String, LoggerDataRecord.TemperatureModel> data = recLoggerData.data;
        tempProfileAdapter.refill(data);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
       if (!IsDemo) {
            if (recLoggerData == null || recLoggerData.data.isEmpty()) {
                sb.append(String.format("\n%s is missing", getString(R.string.logger_data)));
            }
        }
        return sb.toString();
    }

    private void updateState() {

        if (recLoggerData.data != null && recLoggerData.data.size() > 0) {
            DoubleSummaryStatistics stats = recLoggerData.data.values().stream()
                    .flatMap(x -> x.values.stream())
                    .filter(y -> !"N/A".equalsIgnoreCase(y.getSample()))
                    .mapToDouble(x -> Double.valueOf(x.getSample().replace(',', '.')))
                    .summaryStatistics();

            recLoggerData.highT = stats.getMax();
            recLoggerData.lowT = stats.getMin();
            recLoggerData.avgT = stats.getAverage();
        }
    }
}