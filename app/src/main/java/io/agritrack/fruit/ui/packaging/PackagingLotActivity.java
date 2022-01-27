package io.agritrack.fruit.ui.packaging;

import static java.time.temporal.ChronoUnit.MINUTES;
import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.FishTrackUtils.LotToDate;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recPackaging;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class PackagingLotActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private Spinner spPackagingSite;

    private String packagingLot;
    private TextView tvHarvestLot;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaging_lot);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackagingLot);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> packagingSites = db.siteDAO().getAllProcessingPlants();
        if (packagingSites != null && !packagingSites.isEmpty()) {
            String[] packagingSite = packagingSites.stream().map(x -> x.name).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, packagingSite);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spPackagingSite.setAdapter(hrAdapter);
        }

        LocalDateTime dateStart = LotToDate(recPackaging.collectionLot);

        long minutesBetween = MINUTES.between(dateStart, LocalDateTime.now());
        String hexMinutes = Long.toHexString(minutesBetween).toUpperCase();
        //Decoding hex to minutes dec
        //Long aa = new BigInteger(hexMinutes, 16).longValue();
        packagingLot = String.format("%s%s",recPackaging.collectionLot, hexMinutes);
        tvHarvestLot.setText(packagingLot);
            /*//Decoding packaging lot to date time
            LocalDateTime tt = LotToDate(packagingLot);
            Long aa = new BigInteger(packagingLot.substring(3), 16).longValue();
            LocalDateTime ttt = tt.plusMinutes(aa.intValue());*/

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackagingLotActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackagingIfco);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackagingIfcoActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackagingStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackagingStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        PackagingRecord trns = recPackaging;

        /*if (!Strings.isEmptyOrWhitespace(trns.packagingLot)) {
            tvHarvestLot.setText(trns.packagingLot);
        }*/

        if (trns.sitePos > -1) {
            spPackagingSite.setSelection(trns.sitePos);
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(FruitGlobalState.recPackaging.packagingSite)) {
                sb.append(String.format("\n%s is missing", "'Packaging site'"));
            }
        }

        return sb.toString();
    }

    private PackagingRecord updateState() {
        PackagingRecord packagingRecord = recPackaging;

        if (spPackagingSite.getSelectedItem() != null) {
            packagingRecord.packagingSite = spPackagingSite.getSelectedItem().toString();
        }
        packagingRecord.sitePos = spPackagingSite.getSelectedItemPosition();

        return packagingRecord;
    }

    private void assignCtrlVars() {
        tvHarvestLot = findViewById(R.id.tvHarvestLot);
        ivSupport = findViewById(R.id.ivSupport);
        spPackagingSite = findViewById(R.id.spPackagingSite);
    }
}