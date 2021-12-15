package io.agritrack.fruit.ui.packaging;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.fruit.state.FruitGlobalState.recPackaging;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class PackagingLotActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private TextView tvHarvestLot;

    @Override
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
            Intent i = new Intent(getApplicationContext(), PackagingIfcoActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackagingStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackagingStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        PackagingRecord trns = recPackaging;

        if (!Strings.isEmptyOrWhitespace(trns.collectionLot)) {
            tvHarvestLot.setText(trns.collectionLot);
        }
    }

    private void assignCtrlVars() {
        tvHarvestLot = findViewById(R.id.tvHarvestLot);
        ivSupport = findViewById(R.id.ivSupport);
    }
}