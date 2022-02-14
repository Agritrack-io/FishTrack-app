package io.agritrack.hotel.ui.incoming;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.enums.AssetType.ALL;
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.login.api.UploadingApi;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelIncomingLinenActivity<uploadSvc> extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ScanHandler mScanHandler;

    private ScanInventoryThread scanner_runnable;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);

    private ToggleGroup tgChooseAssetType;
    private String selectedAssetType = AssetType.ALL.name();
    private String activeFilter = null;
    private int selectedToggleButton = -1;
    private MobileDB db;

    private TreelikeAdapter adapterIncomingItems;

    private TextView tvIncomingProcessFrom, tvIncomingProcessTo;
    private ExpandableListView xvIncomingItems;

    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    private Integer selectedParent, selectedChild;

    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private TextView tvGroupsCnt;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_incoming_linen);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // get  references of the controls
        assignCtrlVars();

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HotelIncomingLinenActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        xvIncomingItems.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                clearSelectedItem();

                selectedParent = null;
                selectedChild = null;
                return false;
            }
        });

        xvIncomingItems.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ConstraintLayout view = (ConstraintLayout) v;
                TextView tvSiteName = v.findViewById(R.id.tvSiteName);
                selectedBarcode = tvSiteName.getText().toString();

                clearSelectedItem();

                v.setSelected(true);
                view.setBackgroundColor(Color.GRAY);
                selectedItem = view;

                selectedParent = groupPosition;
                selectedChild = childPosition;

                return true;
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

            if (selectedParent != null && selectedChild != null) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterIncomingItems.removeItem(selectedParent, selectedChild);
                        adapterIncomingItems.notifyDataSetChanged();
                        selectedBarcode = null;
                        selectedChild = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelIncomingLinenActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop scanning since we navigate to next activity
                if (scanner_runnable!=null) {
                    scanner_runnable.stopReading();
                }

                if (mLastLocation != null) {
                    recWHIncoming.longitude = mLastLocation.getLongitude();
                    recWHIncoming.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(HotelIncomingLinenActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable!=null) {
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), HotelIncomingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgChooseAssetType = findViewById(R.id.tgChooseAssetType);
        xvIncomingItems = findViewById(R.id.xvIncomingItems);
        tvIncomingProcessFrom = findViewById(R.id.tvIncomingProcessFrom);
        tvIncomingProcessTo = findViewById(R.id.tvIncomingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        tgChooseAssetType.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
    }

    private boolean updateState() {
        if (adapterIncomingItems != null) {
            GlobalState.recWHIncoming.items = adapterIncomingItems.getValues();
        }
        String v = validate();
        if (!Strings.isEmptyOrWhitespace(v)) {
            CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            return false;
        }
        GlobalState.recWHIncoming.state = WarehouseTxState.Incoming;
        GlobalState.recWHIncoming.assetType = AssetType.valueOf(this.selectedAssetType);
        GlobalState.recWHIncoming.site = LocalPreferences.getCurrentSiteName();

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHRFIDIncoming(db);


            // save data in a local file.
            String fileName = storeRecordToLocalJSONFile();
            if(fileName != null) {
                File jsonFile = new File(HotelIncomingLinenActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
                String content_type = getMimeType(jsonFile.getPath());

                // create RequestBody instance from file
                RequestBody requestFile = RequestBody.create(jsonFile , MediaType.parse(content_type));

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("incoming", fileName, requestFile);

                Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadHotelInventory(null, filePart, "Bearer " + token);
                //syncTxAsyncCall.enqueue(new HotelIncomingLinenActivity.UploadIncomingJsonCallBack());
            }

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncRFIDIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new HotelIncomingLinenActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private String storeRecordToLocalJSONFile() {
        String fileName = null;
        // if WHIncoming record contains data, then save it to a local file.
        if(recWHIncoming.items!=null && recWHIncoming.items.size() > 0) {
            Date currentDate = new Date();
            String compactTSFormat = "yyyyMMddHHmmss";
            SimpleDateFormat sdf = new SimpleDateFormat(compactTSFormat);

            // get asset Type, based on what toggle button was pressed.
            String assetType = (recWHIncoming.assetType != null) ? recWHIncoming.assetType.name() : ALL.name();

            // create the local json file name
            fileName = String.format("Incoming.%s.%s.json",assetType, sdf.format(currentDate));
            File outputFile = new File(HotelIncomingLinenActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            ObjectMapper mapper = new ObjectMapper();
            try (JsonGenerator jGenerator = mapper.getFactory().createGenerator(outputFile, JsonEncoding.UTF8)) {
                jGenerator.writeStartObject(); // {

                for (Map.Entry<String, List<String>> entry : recWHIncoming.items.entrySet()) {
                    String catCode = entry.getKey();
                    List<String> epcs = entry.getValue();

                    jGenerator.writeStringField("cat", catCode);
                    jGenerator.writeStringField("state", recWHIncoming.state.name());
                    jGenerator.writeStringField("site", recWHIncoming.site);
                    jGenerator.writeStringField("fromSite", recWHIncoming.from);
                    jGenerator.writeStringField("toSite", recWHIncoming.to);
                    jGenerator.writeNumberField("lon", recWHIncoming.longitude);
                    jGenerator.writeNumberField("lat", recWHIncoming.latitude);

                    // put the epcs in an array
                    jGenerator.writeFieldName("epcs");
                    jGenerator.writeStartArray(); // [
                    for (String epc : epcs) {
                        jGenerator.writeString(epc); // "epc..."
                    }
                    jGenerator.writeEndArray();
                }

                jGenerator.writeEndObject(); // }
            } catch (JsonGenerationException e) {
                e.printStackTrace();
                Toast.makeText(HotelIncomingLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            } catch (JsonMappingException e) {
                e.printStackTrace();
                Toast.makeText(HotelIncomingLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(HotelIncomingLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return fileName;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recWHIncoming.items == null || GlobalState.recWHIncoming.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Incoming items'"));
            }
        }
        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord WHTxRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.from)) {
            tvIncomingProcessFrom.setText(WHTxRecord.from);
        }

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.to)) {
            tvIncomingProcessTo.setText(WHTxRecord.to);
        }

        if (WHTxRecord.items != null) {
            adapterIncomingItems.setValues(WHTxRecord.items);
            adapterIncomingItems.notifyDataSetChanged();
        }
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if (selectedToggleButton == checkedId) {
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch (checkedId) {
            case R.id.tbCage:
                selectedAssetType = Constants.ftCage;
                activeFilter = Filters.RFID_CAGE;
                break;
            case R.id.tbNet:
                selectedAssetType = Constants.ftNet;
                activeFilter = Filters.RFID_NET;
                break;
            case R.id.tbBin:
                selectedAssetType = Constants.ftBin;
                activeFilter = Filters.RFID_BIN;
                break;
            default:
                selectedAssetType = Constants.ftAll;
                activeFilter = null;
                selectedToggleButton = -1;
                break;
        }
    }

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
            AssetTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_AssetTx_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<AssetTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
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

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            scanButton.setText(R.string.scan_assets);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if(this.scanner_runnable !=null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<HotelIncomingLinenActivity> mActivity;

        public ScanHandler(HotelIncomingLinenActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    //clearSelectedItem();
                    if (epcList != null && !epcList.isEmpty()) {
                        Map<String, List<String>> values = epcList.stream().map(x->x.toString()).collect(Collectors.groupingBy(g -> g.substring(0, 4), Collectors.toCollection(ArrayList::new)));

                        if (adapterIncomingItems == null) {
                            adapterIncomingItems = new TreelikeAdapter(mActivity.get(), values);
                            xvIncomingItems.setAdapter(adapterIncomingItems);
                        } else {
                            adapterIncomingItems.appendItems(values);
                        }
                        adapterIncomingItems.notifyDataSetChanged();
                    }
                    tvGroupsCnt.setText(String.valueOf(adapterIncomingItems.getGroupCount()));
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Scanning is finished!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }


    private String getMimeType(String path) {
//        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
//        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return "application/json";
    }
}