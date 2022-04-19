package io.agritrack.fish.ui.fishing;

import static java.time.temporal.ChronoUnit.DAYS;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.ui.service.LocalPreferences;

public class FishingDetailsActivity extends AppCompatActivity {

    private SwitchCompat bIceAdequacy;
    private EditText etIceSupplier;
    private TextView tvPathologist, tvLastFed, tvSpecies;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private static long fastingDays = LocalPreferences.getFastingDays();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_details);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingDetails);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        tvPathologist.setText(GlobalState.recFishing.pathologist);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate lastFeedDate = GlobalState.recFishing.lastFed;
        tvLastFed.setText(lastFeedDate.format(formatter));
        try {
            LocalDate now = LocalDate.now();
            long daysBetween = DAYS.between(lastFeedDate, now);
            if (daysBetween>fastingDays){
                CToast(getApplicationContext(), render("More than 2 days have been spent before last feeding!!!"), Toast.LENGTH_LONG);
            } else if (daysBetween<=(fastingDays-1)){
                CToast(getApplicationContext(), render("Less than 1 days has been spent before last feeding!!!"), Toast.LENGTH_LONG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvSpecies.setText(GlobalState.recFishing.speciesName);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingDetailsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingDetailsActivity.this);
            infoDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToFillBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToCage);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvPathologist = findViewById(R.id.tvNameOfIchthyopathologist);
        tvLastFed = findViewById(R.id.tvDateOfLastNutrition);
        tvSpecies = findViewById(R.id.tvTypeOfFish);
        bIceAdequacy = findViewById(R.id.switchIceAdequacy);
        etIceSupplier = findViewById(R.id.etIceSupplier);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

    }

    private void updateState() {
        GlobalState.recFishing.adequateIce = bIceAdequacy.isChecked();
        GlobalState.recFishing.iceSupplier = etIceSupplier.getText().toString();
    }
}