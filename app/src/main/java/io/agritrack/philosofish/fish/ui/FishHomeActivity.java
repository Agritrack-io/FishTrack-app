package io.agritrack.philosofish.fish.ui;

import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//import io.agritrack.BuildConfig;
import io.agritrack.api.sync.EncodingSchemeCallBack;
//import io.agritrack.philosofish.BuildConfig;
import io.agritrack.philosofish.BuildConfig;
import io.agritrack.philosofish.FishTrackApplication;
import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.sync.PendindQualityMeasurementsTxCallBack;
import io.agritrack.philosofish.api.sync.PendingBinInfoTxCallBack;
import io.agritrack.philosofish.api.sync.PendingCorrelationTxCallBack;
import io.agritrack.philosofish.api.sync.PendingFinalQualityTxCallBack;
import io.agritrack.philosofish.api.sync.PendingFishingTxCallBack;
import io.agritrack.philosofish.api.sync.PendingPackQualityTxCallBack;
import io.agritrack.philosofish.api.sync.PendingProcessTxCallBack;
import io.agritrack.philosofish.api.sync.PendingQualityTxCallBack;
import io.agritrack.philosofish.api.sync.PendingRecQualityTxCallBack;
import io.agritrack.philosofish.api.sync.SyncApi;
import io.agritrack.philosofish.api.sync.SyncAssetsCallBack;
import io.agritrack.philosofish.api.sync.SyncBinInfo;
import io.agritrack.philosofish.api.sync.SyncCageDetailsCallBack;
import io.agritrack.philosofish.api.sync.SyncClusterSitesCallBack;
import io.agritrack.philosofish.api.sync.SyncCustomersCallBack;
import io.agritrack.philosofish.api.sync.SyncEmployeesCallBack;
import io.agritrack.philosofish.api.sync.SyncFishingRequestCallBack;
import io.agritrack.philosofish.api.sync.SyncIOTLoggersCallBack;
import io.agritrack.philosofish.api.sync.SyncSpeciesCallBack;
import io.agritrack.philosofish.api.sync.SyncSuppliersCallBack;
import io.agritrack.philosofish.api.sync.SyncUsersCallBack;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.api.upload.UploadingApi;
import io.agritrack.philosofish.common.FileUtils;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.AppUserDTO;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import io.agritrack.philosofish.data.dto.CageDetailsDTO;
import io.agritrack.philosofish.data.dto.EncodingSchemeDTO;
import io.agritrack.philosofish.data.dto.FishingRequestDTO;
import io.agritrack.philosofish.data.dto.SiteDTO;
import io.agritrack.philosofish.data.dto.common.CustomerDTO;
import io.agritrack.philosofish.data.dto.common.EmployeeDTO;
import io.agritrack.philosofish.data.dto.common.IotLoggerDTO;
import io.agritrack.philosofish.data.dto.common.SpeciesDTO;
import io.agritrack.philosofish.data.dto.common.SupplierDTO;
import io.agritrack.philosofish.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.philosofish.data.dto.tx.CorrelationTxDTO;
import io.agritrack.philosofish.data.dto.tx.FinalQualityTxDTO;
import io.agritrack.philosofish.data.dto.tx.FishingTxDTO;
import io.agritrack.philosofish.data.dto.tx.PackageQualityTxDTO;
import io.agritrack.philosofish.data.dto.tx.ProcessingTxDTO;
import io.agritrack.philosofish.data.dto.tx.QualityTxDTO;
import io.agritrack.philosofish.data.dto.tx.ReceiptQualityTxDTO;
import io.agritrack.philosofish.data.dto.wh.AssetDTO;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.common.TemperatureTimeSeries;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.FishingTransaction;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.ProcessingTransaction;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.enums.TxStatus;
import io.agritrack.philosofish.fish.state.FishingRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.binTurnover.BinTurnoverActivity;
import io.agritrack.philosofish.fish.ui.fishing.FishingStartActivity;
import io.agritrack.philosofish.fish.ui.fishing.FishingTeamActivity;
import io.agritrack.philosofish.fish.ui.fishing.HarvestRequestsActivity;
import io.agritrack.philosofish.fish.ui.initBins.InitBinsActivity;
import io.agritrack.philosofish.fish.ui.process.ProcessBinsActivity;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.philosofish.fish.ui.transport.TransportBinsActivity;
import io.agritrack.philosofish.fish.ui.transport.TransportInfoActivity;
import io.agritrack.philosofish.ui.adapter.HomeMenuAdapter;
import io.agritrack.philosofish.ui.adapter.MenuItem;
import io.agritrack.philosofish.ui.login.LoginActivity;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FishHomeActivity extends AppCompatActivity {
    private static final int InitBins_Idx = 0, Fishing_Idx = 1,Test_Temp_Idx = 2, Transport_Idx = 3, Receiving_Idx = 4, Packaging_Quality_Idx = 5, Bin_Overturn_Idx = 6, Warehouse_Idx = 7; /*Maintenance_Idx = 5,*/
    private static final Map<Integer, String[]> Privileges = new HashMap<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private GridView gvMainMenu;
    private ImageView ivSupport, ivRefresh;
    private TextView tvOfflineWork, tvVersionLabel;
    private ProgressDialog progressDialog;
    private SupportDialog supportDialog;
    private MobileDB db;
    private int syncCounter = 1;
    private int syncLimit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_home);

        //Initialize mapping of roles to menus
        assignPrivilegesToRoles();

        List<String> userRoles = LocalPreferences.getUserRoles();
        syncLimit = 12;
        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvVersionLabel = findViewById(R.id.tvVersionLabel);
        tvVersionLabel.setText("v 1." + BuildConfig.VERSION_CODE);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        Set<MenuItem> menuItemsSet = new LinkedHashSet<MenuItem>();
        if (roleCanAccessMenu(userRoles, InitBins_Idx)) {
            menuItemsSet.add(new MenuItem(InitBins_Idx, getString(R.string.menu_title_init_bins), TestBinTempActivity.class, R.drawable.test_bin_temp));
        }
        if (roleCanAccessMenu(userRoles, Fishing_Idx)) {
            menuItemsSet.add(new MenuItem(Fishing_Idx, getString(R.string.menu_title_fishing), FishingStartActivity.class, R.drawable.fishing));
        }
        if (roleCanAccessMenu(userRoles, Test_Temp_Idx)) {
            menuItemsSet.add(new MenuItem(Test_Temp_Idx, getString(R.string.menu_title_test_temp), TestBinTempActivity.class, R.drawable.test_bin_temp));
        }
        if (roleCanAccessMenu(userRoles, Receiving_Idx)) {
            menuItemsSet.add(new MenuItem(Receiving_Idx, getString(R.string.menu_title_fish_receiving), ProcessBinsActivity.class, R.drawable.processing));
        }
        if (roleCanAccessMenu(userRoles, Packaging_Quality_Idx)) {
            menuItemsSet.add(new MenuItem(Packaging_Quality_Idx, getString(R.string.menu_title_fish_packaging), QualitySelectStepsActivity.class, R.drawable.quality));
        }
//        if (roleCanAccessMenu(userRoles, Bin_Overturn_Idx)) {
//            menuItemsSet.add(new MenuItem(Bin_Overturn_Idx, getString(R.string.menu_title_bin_overturn), BinTurnoverActivity.class, R.drawable.bin_turnover));
//        }
//        if (roleCanAccessMenu(userRoles, Transport_Idx)) {
//            menuItemsSet.add(new MenuItem(Transport_Idx, getString(R.string.menu_title_transport), TransportInfoActivity.class, R.drawable.transport));
//        }
        if (roleCanAccessMenu(userRoles, Warehouse_Idx)) {
            menuItemsSet.add(new MenuItem(Warehouse_Idx, getString(R.string.menu_title_warehouse), WhMenuActivity.class, R.drawable.warehouse));
        }
//        if (roleCanAccessMenu(userRoles, Maintenance_Idx)) {
//            menuItemsSet.add(new MenuItem(Maintenance_Idx, getString(R.string.menu_title_maintenance), MaintenanceMenuActivity.class, R.drawable.maintenance));
//        }
//        if (roleCanAccessMenu(userRoles, SeaTemp_Idx)) {
//            menuItemsSet.add(new MenuItem(SeaTemp_Idx, getString(R.string.menu_title_sea_temp), SeaTemperatureActivity.class, R.drawable.sea_temp));
//        }


        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(FishHomeActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        syncResult.observe(this, response -> {
            syncCounter++;
//            if (response == null) {
//                hideProgressDialog();
//                return;
//            }
            if (response != null) {
                progressDialog.setMessage(render(response));
                if (syncCounter > syncLimit) {
                    syncLimit = 12;
                    hideProgressDialog();
                }
            }
        });

        List<MenuItem> miList = menuItemsSet.stream().sorted(Comparator.comparingInt(MenuItem::getLoc)).collect(Collectors.toList());
        HomeMenuAdapter adapter = new HomeMenuAdapter(this, (ArrayList<MenuItem>) miList);

        gvMainMenu = findViewById(R.id.gvMainMenu);
        gvMainMenu.setAdapter(adapter);
        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);
                MenuItem mi = (MenuItem) gvMainMenu.getItemAtPosition(position);

                switch (mi.getLoc()) {
                    case InitBins_Idx:
                        GlobalState.initFishingRecord();
                        i = new Intent(appCtx, InitBinsActivity.class);
                        break;
                    case Fishing_Idx:
                        FishingTransaction openTx = db.fishingTransactionDAO().getMostRecentOpenTx(LocalPreferences.getLoggedInUser(""));
                        FishingRecord fishingRecord;

                        // default Next Activity is FishingTeam...
                        i = new Intent(appCtx, FishingTeamActivity.class);
                        if (openTx != null && !Strings.isEmptyOrWhitespace(openTx.fishingRq)) {
                            // there is a FishingTx in progress
                            fishingRecord = FishingRecord.convert(openTx);
                            GlobalState.recFishing = fishingRecord;
                        } else if (openTx != null && openTx.outOfSystemFishing) {
                            // there is a out of system FishingTx in progress
                            i = new Intent(appCtx, FishingTeamActivity.class);

                            fishingRecord = FishingRecord.convert(openTx);
                            GlobalState.recFishing = fishingRecord;
                        } else {
                            // instantiate a new Fishing Record.
                            fishingRecord = GlobalState.initFishingRecord();

                            // NO FishingTx in progress
                            if (openTx == null) {
                                openTx = new FishingTransaction();
                                openTx.txStatus = TxStatus.PENDING;
                                db.fishingTransactionDAO().insert(openTx);
                                fishingRecord.txKey = openTx.id;
                            } else {
                                fishingRecord.txKey = openTx.id;
                            }

                            i = new Intent(appCtx, HarvestRequestsActivity.class);
                        }
                        break;
                    case Test_Temp_Idx:
                        i = new Intent(appCtx, TestBinTempActivity.class);
                        i.putExtra("BinActivity", false);
                        break;
                    case Transport_Idx:
                        GlobalState.initTransportationRecord();
                        i = new Intent(appCtx, TransportBinsActivity.class);
                        break;
                    case Receiving_Idx:
                        GlobalState.initProcessingRecord();
                        i = new Intent(appCtx, ProcessBinsActivity.class);
                        break;
                    case Packaging_Quality_Idx:
                        i = new Intent(appCtx, QualitySelectStepsActivity.class);
                        break;
                    case Bin_Overturn_Idx:
                        i = new Intent(appCtx, BinTurnoverActivity.class);
                        break;
                    case Warehouse_Idx:
                        i = new Intent(appCtx, WhMenuActivity.class);
                        break;
//                    case Maintenance_Idx:
//                        i = new Intent(appCtx, MaintenanceMenuActivity.class);
//                        break;
//                    case SeaTemp_Idx:
//                        i = new Intent(appCtx, SeaTemperatureActivity.class);
//                        break;
//                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishHomeActivity.this);
            supportDialog.showDialog();
        });

        ivRefresh = findViewById(R.id.ivRefresh);
        tvOfflineWork = findViewById(R.id.tvOfflineWork);
        if (!IsOnline) {
            ((ImageView) ivRefresh).setImageResource(R.drawable.out_of_network);
            ivRefresh.setEnabled(false);
            tvOfflineWork.setVisibility(View.VISIBLE);
        } else {
            ((ImageView) ivRefresh).setImageResource(R.drawable.refresh);
            ivRefresh.setEnabled(true);
            tvOfflineWork.setVisibility(View.INVISIBLE);
        }
        ivRefresh.setOnClickListener(view -> {
            syncCounter = 1;
            showProgressDialog(getString(R.string.syncing));
            invokeUploadPendingAll();
            invokeSyncAll();
        });

        configHeader();
    }

    protected void configHeader() {
        ImageButton ivBack = findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            LocalPreferences.resetLogin();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void invokeUploadPendingAll() {
        try {
            TransactionApi pendingTxSvc = APIServiceGenerator.createAPI(TransactionApi.class);
            String token = LocalPreferences.getToken();

            // select all pending binInfos TXs
            List<BinInfo> binInfoTXs = db.binInfoDAO().getPendingBins();
            if (!binInfoTXs.isEmpty()) {
                Call<List<BinInfoDTO>> binInfoTxAsyncCall = pendingTxSvc.syncBinInfoTx(BinInfoDTO.convert(binInfoTXs), "Bearer " + token);
                binInfoTxAsyncCall.enqueue(new PendingBinInfoTxCallBack(this.syncResult));
                syncLimit++;
            }

            // select all pending fishing TXs
            List<FishingTransaction> fishingTXs = db.fishingTransactionDAO().getAllCompleted();
            if (!fishingTXs.isEmpty()) {
                for (FishingTransaction fishingTX : fishingTXs) {
                    Call<FishingTxDTO> fishingTxAsyncCall = pendingTxSvc.syncFishingTx(FishingTxDTO.convert(fishingTX), "Bearer " + token);
                    fishingTxAsyncCall.enqueue(new PendingFishingTxCallBack(this.syncResult));
                    syncLimit++;
                }
            }

            // select all pending receipt TXs
            List<ProcessingTransaction> processTXs = db.processingTransactionDAO().getPendingProcessTx();
            if (!processTXs.isEmpty()) {
                for (ProcessingTransaction processTX : processTXs) {
                    Call<ProcessingTxDTO> processTxAsyncCall = pendingTxSvc.syncProcessingTx(ProcessingTxDTO.convert(processTX), "Bearer " + token);
                    processTxAsyncCall.enqueue(new PendingProcessTxCallBack(this.syncResult));
                    syncLimit++;

                }
            }

            List<ReceiptQualityTransaction> receiptTXs = db.receiptQualityTransactionDAO().getAll().stream().filter(x -> !x.isSynced && (x.createdAt != null)).collect(Collectors.toList());
            if (!receiptTXs.isEmpty()) {
                for (ReceiptQualityTransaction tx : receiptTXs) {
                    Call<ReceiptQualityTxDTO> recQualityTxAsyncCall = pendingTxSvc.syncRecQualityTx(ReceiptQualityTxDTO.convert(tx), "Bearer " + token);
                    recQualityTxAsyncCall.enqueue(new PendingRecQualityTxCallBack(this.syncResult));
                    syncLimit++;

                }
            }

            List<PackageQualityTransaction> packageFreshTxs = db.packageQualityTransactionDAO().getAll().stream().filter(x -> !x.isFreshSynced && (x.freshCreatedAt != null)).collect(Collectors.toList());
            if (!packageFreshTxs.isEmpty()) {
                for (PackageQualityTransaction tx : packageFreshTxs) {
                    Call<PackageQualityTxDTO> packFreshQualityTxAsyncCall = pendingTxSvc.syncPackQualityTx(PackageQualityTxDTO.convertFresh(tx), "Bearer " + token);
                    packFreshQualityTxAsyncCall.enqueue(new PendingPackQualityTxCallBack(this.syncResult));
                    syncLimit++;


                }
            }

            List<PackageQualityTransaction> packageSampleTxs = db.packageQualityTransactionDAO().getAll().stream().filter(x -> !x.isSampleSynced && (x.sampleCreatedAt != null)).collect(Collectors.toList());
            if (!packageSampleTxs.isEmpty()) {
                for (PackageQualityTransaction tx : packageSampleTxs) {
                    Call<PackageQualityTxDTO> packSampleQualityTxAsyncCall = pendingTxSvc.syncPackQualityTx(PackageQualityTxDTO.convertSample(tx), "Bearer " + token);
                    packSampleQualityTxAsyncCall.enqueue(new PendingPackQualityTxCallBack(this.syncResult));
                    syncLimit++;


                }
            }

            List<PackageQualityTransaction> packageLabelTxs = db.packageQualityTransactionDAO().getAll().stream().filter(x -> !x.isLabelSynced && (x.labelCreatedAt != null)).collect(Collectors.toList());
            if (!packageLabelTxs.isEmpty()) {
                for (PackageQualityTransaction tx : packageLabelTxs) {
                    Call<PackageQualityTxDTO> packLabelQualityTxAsyncCall = pendingTxSvc.syncPackQualityTx(PackageQualityTxDTO.convertLabel(tx), "Bearer " + token);
                    packLabelQualityTxAsyncCall.enqueue(new PendingPackQualityTxCallBack(this.syncResult));
                    syncLimit++;


                }
            }

            List<FinalQualityTransaction> finalQualityTxs = db.finalQualityTransactionDAO().getAll().stream().filter(x -> !x.isSynced && (x.createdAt != null)).collect(Collectors.toList());
            if (!finalQualityTxs.isEmpty()) {
                for (FinalQualityTransaction tx : finalQualityTxs) {
                    Call<FinalQualityTxDTO> finalQualityTxAsyncCall = pendingTxSvc.syncFinalQualityTx(FinalQualityTxDTO.convert(tx), "Bearer " + token);
                    finalQualityTxAsyncCall.enqueue(new PendingFinalQualityTxCallBack(this.syncResult));
                    syncLimit++;


                }
            }

            // select all pending post quality TXs
            List<TemperatureTimeSeriesDTO> temperatureTimeSeriesDTOs = new ArrayList<>();
            for (TemperatureTimeSeries ts : db.measurementsDAO().getPendingMeasurements()) {
                temperatureTimeSeriesDTOs.add(TemperatureTimeSeriesDTO.convert(ts));
            }

            if (!temperatureTimeSeriesDTOs.isEmpty()) {
                Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall = pendingTxSvc.syncMeasurements(temperatureTimeSeriesDTOs, "Bearer " + token);
                syncMsAsyncCall.enqueue(new PendindQualityMeasurementsTxCallBack(this.syncResult));
                syncLimit++;

            }

            //===================================================================================================
            // select all SIMPLE pending correlation TXs (identification events, e.g. correlate rfid <--> code)
            List<CorrelationTransaction> correlationTXs = db.correlationTransactionDAO().getAll();
            if (!correlationTXs.isEmpty()) {
                // filter out the simple correlation transactions
                List<CorrelationTransaction> identifications = correlationTXs.stream().filter(f -> f.assetRFID == null).collect(Collectors.toList());
                // filter out the inter-correlation transactions
                List<CorrelationTransaction> interCorrelations = correlationTXs.stream().filter(f -> f.assetRFID != null).collect(Collectors.toList());

                if (!identifications.isEmpty()) {
                    List<CorrelationTxDTO> identificationDTOs = identifications.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetIdentificationAsyncCall = pendingTxSvc.syncAssetCorrelationTx(identificationDTOs, "Bearer " + token);
                    assetIdentificationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                    syncLimit++;

                }

                if (!interCorrelations.isEmpty()) {
                    List<CorrelationTxDTO> interCorrelationDTOs = interCorrelations.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetInterCorrelationAsyncCall = pendingTxSvc.syncAssetWithAssetCorrelationTx(interCorrelationDTOs, "Bearer " + token);
                    assetInterCorrelationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                    syncLimit++;

                }
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private void invokeSyncAll() {
        try {
            db.finalQualityTransactionDAO().deleteThreeDaysOld(System.currentTimeMillis() - 259200000L);
            db.packageQualityTransactionDAO().deleteThreeDaysOld(System.currentTimeMillis() - 259200000L);
            db.receiptQualityTransactionDAO().deleteThreeDaysOld(System.currentTimeMillis() - 259200000L);
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
            String token = LocalPreferences.getToken();
            UUID siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync harvestRequests for current Site
            Call<List<FishingRequestDTO>> syncHarvestResAsyncCall = syncService.getFishingRequestsBySiteId(siteId, "Bearer " + token);
            syncHarvestResAsyncCall.enqueue(new SyncFishingRequestCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack(this.syncResult));

            // sync suppliers
            Call<List<SupplierDTO>> syncSuppliersAsyncCall = syncService.getAllSuppliers("Bearer " + token);
            syncSuppliersAsyncCall.enqueue(new SyncSuppliersCallBack(this.syncResult));

            // sync customers
            Call<List<CustomerDTO>> syncCustomersAsyncCall = syncService.getCustomersBySiteId(siteId, "Bearer " + token);
            syncCustomersAsyncCall.enqueue(new SyncCustomersCallBack(this.syncResult));

            //sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync Bin Info (complete BinLedger)
            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));

            // sync fish species
            Call<List<SpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCodeAndType(FishTrackApplication.COUNTRY, FishTrackApplication.getProduct(), "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

            // sync IOT Loggers
            Call<List<IotLoggerDTO>> syncIOTLoggersAsyncCall = syncService.getIOTLoggersBySiteId(siteId, "Bearer " + token);
            syncIOTLoggersAsyncCall.enqueue(new SyncIOTLoggersCallBack(this.syncResult));

            // sync Encoding scheme info
            Call<List<EncodingSchemeDTO>> syncEncodingShemeAsyncCall = syncService.getEncodingScheme("Bearer " + token);
            syncEncodingShemeAsyncCall.enqueue(new EncodingSchemeCallBack(this.syncResult));

            //Traverse the crash folder in the sd card to get each file
            File file = new File(FishHomeActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "agriLogs");
            File[] files = file.listFiles();

            if (files != null) {
                for (File f : files) {
                    String strFileName = f.getName();
                    //Upload file using okhttp post
                    //File jsonFile = new File(FishHomeActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), strFileName);

                    // create RequestBody instance from file
                    RequestBody requestFile = RequestBody.create(f, MediaType.parse("text/plain"));

                    // MultipartBody.Part is used to send also the actual file name
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("crashLog", strFileName, requestFile);

                    Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadCrashLog(filePart, "Bearer " + token);
                    uploadJsonFileAsyncCall.enqueue(new FishHomeActivity.CrashFileUploadCallBack());

                    //Delete the uploaded file crash folder
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    // show Progress bar
    private void showProgressDialog(String substring) {
        //Without this user can hide loader by tapping outside screen
        progressDialog.setCancelable(false);
        progressDialog.setMessage(render(substring));
        progressDialog.show();
    }

    // hide/dismiss Progress bar
    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    private void assignPrivilegesToRoles() {
        Privileges.put(InitBins_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Fishing_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Test_Temp_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Receiving_Idx, new String[]{"ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Packaging_Quality_Idx, new String[]{"ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Bin_Overturn_Idx, new String[]{"ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Transport_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Warehouse_Idx, new String[]{"ROLE_FISHING", "ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN", "ROLE_WAREHOUSE"});
//        Privileges.put(Maintenance_Idx, new String[]{"ROLE_PACKAGING", "ROLE_FISHING","ROLE_SUPER_USER", "ROLE_ADMIN"});
//        Privileges.put(SeaTemp_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
    }

    private boolean roleCanAccessMenu(List<String> roles, Integer menuId) {
        String[] privileges = Privileges.get(menuId);
        if (privileges != null && privileges.length > 0) {
            for (String role : roles) {
                if (Arrays.stream(privileges).anyMatch(role::equalsIgnoreCase)) {
                    return true;
                }
            }
        }
        return false;
    }

    public class CrashFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.isSuccessful()) {
                    String fileName = response.body().string();
                    FileUtils.deleteCrashFile(FishHomeActivity.this, fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.file_failed_to_sync), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_timeout), Toast.LENGTH_LONG));
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render("Network Error :: " + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }
}