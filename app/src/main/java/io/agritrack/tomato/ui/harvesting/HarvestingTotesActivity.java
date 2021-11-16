package io.agritrack.tomato.ui.harvesting;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.tomato.ui.FruitHomeActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class HarvestingTotesActivity extends AppCompatActivity {

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread harvestTotesThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterTotes;

    private RecyclerView rvUsedTotesHarvest;
    private TextView tvTotesCount;

    private ImageButton ivAddTote, ivDeleteTote;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private String binBarcode;

    // Instantiate a clickListener to be passed to adapterBins.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvesting_totes);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestingTotesActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HarvestingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToHarvestingStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HarvestingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
        /*tvPoleName = findViewById(R.id.tvPoleName);
        btnScanPole = findViewById(R.id.btnScanPole);
        tvHarvestLot = findViewById(R.id.tvHarvestLot);*/
    }

    private void initControlsFromState() {

    }

    private void updateState() {

    }
}