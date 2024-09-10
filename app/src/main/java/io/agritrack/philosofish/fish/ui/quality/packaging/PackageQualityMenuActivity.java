package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.fish.ui.binTurnover.BinTurnoverActivity;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.fish.ui.quality.finalCheck.QualityFinalCheckActivity;
import io.agritrack.philosofish.fish.ui.quality.receipt.ReceiptQualityStartActivity;
import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;
import io.agritrack.philosofish.ui.adapter.InventoryMenuAdapter;
import io.agritrack.philosofish.ui.adapter.MenuItem;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityMenuActivity extends AppCompatActivity {

    private static final int First_Step_Idx = 0, Second_Step_Idx = 1, Third_Step_Idx = 2;
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
        setContentView(R.layout.activity_quality_packaging_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualitySelect);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        binList = new ArrayList<String>();

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.freshness_evaluation), "", PackageQualityFreshnessActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.sampling), "", PackageQualitySamplingActivity.class));
        //menuItemsList.add(new MenuItem(getString(R.string.quality_second_step_text), "", PackageQualityStartActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.label_check), "", PackageQualityCheckLabelActivity.class));
        // menuItemsList.add(new MenuItem(getString(R.string.quality_third_step_text), "PP-DOC-02", PostPackagingQualityActivity.class));

        InventoryMenuAdapter adapter = new InventoryMenuAdapter(this, menuItemsList);

        gvQualityMenu.setAdapter(adapter);
        gvQualityMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(getAppContext(), ReceiptQualityStartActivity.class);

                switch (position) {
                    case First_Step_Idx:

//                        QualityTransaction openTx = db.qualityTransactionDAO().getMostRecentOpenTx(LocalPreferences.getLoggedInUser(""));
//                        QualityRecord qualityRecord;
//
//                        binList = new ArrayList<String>();
//
//                        // default Next Activity is FishingStart...
//                        i = new Intent(appCtx, BinTurnoverActivity.class);
//                        if (openTx != null) {
//                            // there is a FishingTx in progress
//                            qualityRecord = QualityRecord.convert(openTx);
//
//                            List<TemperatureTimeSeries> existingMeasurements = db.measurementsDAO().getAll();
//                            if (!existingMeasurements.isEmpty()) {
//                                qualityRecord.qualityBins = new LinkedList<>();
//
//                                for (TemperatureTimeSeries ts : existingMeasurements) {
//                                    Measurement m = ts.measurement;
//                                    List<TemperatureData> _temperatureData = ts.data;
//                                    List<TempSample> _dat = new ArrayList<>();
//                                    for (TemperatureData _temperatureD : _temperatureData) {
//                                        _dat.add(_temperatureD.rawData());
//                                    }
//                                    recLoggerData.addDataSet(m.loggerRFID, m.assetRFID, m.productionLane, m.retrievedAt, _dat);
//                                    binList.add(m.assetRFID);
//                                    //qualityRecord.qualityBins.add(m.assetRFID);
//                                }
//                                qualityRecord.qualityBins = convertEPCsToBinDetails(binList);
//                            }
//
//
//                            GlobalState.recQuality = qualityRecord;
//                        } else {
//                            // instantiate a new Fishing Record.
//                            qualityRecord = GlobalState.initQualityRecord();
//
//                            // NO FishingTx in progress
//                            if (openTx == null) {
//                                openTx = new QualityTransaction();
//                                openTx.txStatus = TxStatus.PENDING;
//                                qualityRecord.txKey = db.qualityTransactionDAO().insert(openTx);
//                            } else {
//                                qualityRecord.txKey = openTx.id;
//                            }
//
//                            //i = new Intent(appCtx, HarvestRequestsActivity.class);
//                        }

                        //GlobalState.initQualityRecord();
                        i = new Intent(getAppContext(), PackageQualityFreshnessActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);

                        //
                        /*YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                        confirmSiteSelectionDlg.setMessage(getText(R.string.quality_select_type));

                        Intent finalI = i;
                        confirmSiteSelectionDlg.onConfirm(bundle -> {
                            finalI.putExtra("processing", true);
                            startActivity(finalI);
                        });
                        confirmSiteSelectionDlg.onReject(bundle -> {
                            finalI.putExtra("processing", false);
                            startActivity(finalI);
                        });

                        FragmentManager fm = getSupportFragmentManager();
                        confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));*/
                        break;
                    /*case Second_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, PackageQualityStartActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;*/
                    case Second_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(getAppContext(), PackageQualitySamplingActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;

                    case Third_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(getAppContext(), PackageQualityCheckLabelActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;

                }
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
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
