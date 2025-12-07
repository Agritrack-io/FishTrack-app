package io.agritrack.philosofish.fish.ui.quality;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.fish.state.GlobalState.recLoggerData;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.data.model.TempSample;
import io.agritrack.philosofish.fish.state.QualityStepsState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.common.Measurement;
import io.agritrack.philosofish.data.model.common.TemperatureData;
import io.agritrack.philosofish.data.model.common.TemperatureTimeSeries;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.enums.TxStatus;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.QualityRecord;
import io.agritrack.philosofish.fish.ui.binTurnover.BinTurnoverActivity;
import io.agritrack.philosofish.fish.ui.quality.finalCheck.QualityFinalCheckActivity;
import io.agritrack.philosofish.fish.ui.quality.packaging.PackageQualityMenuActivity;
import io.agritrack.philosofish.fish.ui.quality.postpackage.PostPackagingQualityActivity;
import io.agritrack.philosofish.fish.ui.quality.receipt.ReceiptQualityStartActivity;
import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;
import io.agritrack.philosofish.ui.adapter.InventoryMenuAdapter;
import io.agritrack.philosofish.ui.adapter.MenuItem;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class QualitySelectStepsActivity extends AppCompatActivity {

    private static final int First_Step_Idx = 0, Second_Step_Idx = 1, Third_Step_Idx = 2, Fourth_Step_Idx = 3;
    private MobileDB db;
    private GridView gvQualityMenu;
    private List<String> binList;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private static List<BinWeightCageAdapter.BinDetails> convertEPCsToBinDetails(List<String> epcs) {
        List<BinWeightCageAdapter.BinDetails> result = new ArrayList<>();
        for (String epc : epcs) {
            result.add(new BinWeightCageAdapter.BinDetails(epc));
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_select_steps);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualitySelect);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        binList = new ArrayList<String>();

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.read_temperatures), "", BinTurnoverActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.quality_first_step_text), "", ReceiptQualityStartActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.quality_third_step_text), "", PackageQualityMenuActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.final_quality_check), "", QualityFinalCheckActivity.class));

// show green check if step completed
        menuItemsList.get(First_Step_Idx).isDone = QualityStepsState.completed[0];
        menuItemsList.get(Second_Step_Idx).isDone = QualityStepsState.completed[1];
        menuItemsList.get(Third_Step_Idx).isDone = QualityStepsState.completed[2];
        menuItemsList.get(Fourth_Step_Idx).isDone = QualityStepsState.completed[3];
        boolean fullDone = allStepsCompleted();
        InventoryMenuAdapter adapter = new InventoryMenuAdapter(this, menuItemsList);

        gvQualityMenu.setAdapter(adapter);
        gvQualityMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(QualitySelectStepsActivity.this, ReceiptQualityStartActivity.class);

                switch (position) {
                    case First_Step_Idx:

                        //GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, BinTurnoverActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);

                        break;
                    /*case Second_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, PackageQualityStartActivity.class);
                        i.putExtra("uid", position);
                        startActivity(i);
                        break;*/
                    case Second_Step_Idx:
                        GlobalState.initReceiptQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, ReceiptQualityStartActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;

                    case Third_Step_Idx:
                        i = new Intent(QualitySelectStepsActivity.this, PackageQualityMenuActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;

                    case Fourth_Step_Idx:
                        GlobalState.initFinalQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, QualityFinalCheckActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;
                }
            }
        });


        // =========================
// MAIN BUTTON COLOR LOGIC
// =========================

        int total = QualityStepsState.completed.length;
        int done = 0;
        for (boolean step : QualityStepsState.completed) if (step) done++;

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(QualitySelectStepsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private boolean allStepsCompleted() {
        for (boolean b : QualityStepsState.completed) {
            if (!b) return false;
        }
        return true;
    }


    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        gvQualityMenu = findViewById(R.id.gvQualityMenu);
        ivSupport = findViewById(R.id.ivSupport);
    }
}