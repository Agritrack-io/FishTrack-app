package io.agritrack.ui.tools;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;
import io.agritrack.fish.ui.FishHomeActivity;

public class ToolsActivity extends AppCompatActivity {

    private TextView tvRevision, tvEPC, tvDateTime, tvInterval, tvSamplesCnt, tvLastSampleValue;
    private Button btnRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        // get  references of the controls
        assignCtrlVars();

        btnRead.setOnClickListener(view -> {
            // read values from logger and assign to controls.
            ReadLogger();
        });

        // create Footer
        configFooter();
    }


    private void ReadLogger() {

        try {
            CAENCommander cmd = new CAENCommander();
            short rev = cmd.READ_REVISION();
            tvRevision.setText(rev + "");

            short cnt = cmd.READ_SAMPLES_COUNT();
            tvSamplesCnt.setText(cnt + "");


            long epoch = cmd.READ_CURRENT_DATETIME();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            String epochAsString = formatter.format(epoch);
            tvDateTime.setText(epochAsString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void assignCtrlVars() {
        btnRead = findViewById(R.id.btnRead);
        tvRevision = findViewById(R.id.tvRevision);
        tvEPC = findViewById(R.id.tvEPC);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvInterval = findViewById(R.id.tvInterval);
        tvSamplesCnt = findViewById(R.id.tvSamplesCnt);
        tvLastSampleValue = findViewById(R.id.tvLastSampleValue);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }
}