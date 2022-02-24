package io.agritrack.hotel.ui.inventory;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.enums.AssetType.ALL;
import static io.agritrack.fish.state.GlobalState.recWHInventory;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import io.agritrack.common.FileUtils;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.ui.login.api.UploadingApi;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelInventoryLinenActivity extends LocationAwareActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;
    private MobileDB db;
    private ExpandableListView xvInventoryItems;
    private Spinner spLinenType;

    private TreelikeAdapter adapterInventoryItems;
    private String selectedAssetType = AssetType.ALL.name();
    private String activeFilter = null;
    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private Integer selectedParent, selectedChild;
    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    private ProgressDialog progressDialog;

    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private boolean storeLocation = true;
    private ImageView ivSupport, ivNext, ivBack;
    private TextView tvGroupsCnt, tvItemsCnt;
    private SupportDialog supportDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_inventory_linen);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        //String[] assetTypes = new String[]{"All", "Παπλ/θήκη Υπ/πλη Raso 280X250", "Σεντόνι Υπ/πλο Raso 300X300", "Μαξ/θήκη Φάκελος Raso 54X95", "Μπουρνούζι Λευκό XL", "Πετσέτα Πισίνας Sand 80Χ200"};
        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.

        ArrayAdapter<String> hrAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item_1, schemeSvc.allNames()) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position % 2 == 0) { // we're on an even row
                    view.setBackgroundColor(getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getColor(R.color.light_grey));
                }
                return view;
            }
        };
        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spLinenType.setAdapter(hrAdapter);

        confirmGPSSelectionDlg = YesNoDialogFragment.instance();
        confirmGPSSelectionDlg.setMessage(getText(R.string.procced_without_location));
        confirmGPSSelectionDlg.onConfirm(bundle -> {
            proceedWithoutLocation = true;
            moveToNextScreen();
        });
        confirmGPSSelectionDlg.onReject(bundle -> {
            mLastLocation = findLocation();
            proceedWithoutLocation = false;
        });

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HotelInventoryLinenActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        // display items counter
        tvItemsCnt.setVisibility(View.VISIBLE);

        xvInventoryItems.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            clearSelectedItem();
            selectedParent = null;
            selectedChild = null;
            return false;
        });

        xvInventoryItems.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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

        // onClick button event handling...
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
                        adapterInventoryItems.removeItem(selectedParent, selectedChild);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
                        selectedBarcode = null;
                        selectedChild = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else if (adapterInventoryItems.getGroupCount() > 0) {
                // <delete> Button was pressed without selecting a Bin first.
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                //confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    adapterInventoryItems.removeAll();
                    adapterInventoryItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                CToast(getApplicationContext(), render("Item list is empty!!"), Toast.LENGTH_LONG);
            }
        });

        // display support dialog
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelInventoryLinenActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = updateState();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
                startActivity(i);
            }
        }
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

    private void assignCtrlVars() {
        xvInventoryItems = findViewById(R.id.xvInventoryItems);
        spLinenType = findViewById(R.id.spLinenType);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }
            if (adapterInventoryItems != null) {
                recWHInventory.items = adapterInventoryItems.getValues();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            recWHInventory.assetType = AssetType.valueOf(this.selectedAssetType);
            if (mLastLocation != null) {
                recWHInventory.longitude = mLastLocation.getLongitude();
                recWHInventory.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), HotelInventoryStartActivity.class);
            startActivity(i);
        });
    }


    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            if (mLastLocation == null) {
                storeLocation = false;
            }
            // save data in a local file.
            String fileName = storeRecordToLocalJSONFile();
            if (fileName != null) {
                File jsonFile = new File(HotelInventoryLinenActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

                // create RequestBody instance from file
                RequestBody requestFile = RequestBody.create(jsonFile, MediaType.parse("application/json"));

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("inventory", fileName, requestFile);

                Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadHotelInventory(filePart, "Bearer " + token);
                uploadJsonFileAsyncCall.enqueue(new InventoryFileUploadCallBack());
            }

            // The commented code was used to upload data as JSON body of Http request.
            // removed since data will be uploaded as file...
            // sync WH Inventory Tx
         /* RFIDInventoryDTO inventoryDto = RFIDInventoryDTO.convert(invtx);
            List<RFIDInventoryItemDTO> invItemsDto = RFIDInventoryItemDTO.convert(invItemtxs);
            inventoryDto.rfid_items = invItemsDto.stream().map(x -> new RFIDInventoryItemDTO(x.rfid)).collect(Collectors.groupingBy(g -> g.code, Collectors.toCollection(ArrayList::new)));

            Call<RFIDInventoryDTO> syncInvTxCallBack = updService.syncRFIDInventoryTx(inventoryDto, "Bearer " + token);
            syncInvTxCallBack.enqueue(new HotelInventoryLinenActivity.SyncInvTxCallBack());*/

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
        } else {
            scanner_runnable.stopReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanButton.setText(R.string.scan_assets);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recWHInventory.items == null || recWHInventory.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Inventory items'"));
            }
        }
        return sb.toString();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private String storeRecordToLocalJSONFile() {
        String fileName = null;

        // if WHIncoming record contains data, then save it to a local file.
        if (recWHInventory.items != null && recWHInventory.items.size() > 0) {
            Date currentDate = new Date();
            String compactTSFormat = "yyyyMMddHHmmss";
            SimpleDateFormat sdf = new SimpleDateFormat(compactTSFormat);

            // get asset Type, based on what toggle button was pressed.
            String assetType = (recWHInventory.assetType != null) ? recWHInventory.assetType.name() : ALL.name();

            // create the local json file name
            fileName = String.format("Inventory_%s_%s.json", assetType, sdf.format(currentDate));
            File outputFile = new File(HotelInventoryLinenActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            ObjectMapper mapper = new ObjectMapper();
            try (JsonGenerator jGenerator = mapper.getFactory().createGenerator(outputFile, JsonEncoding.UTF8)) {
                jGenerator.writeStartObject(); // {

                // add some general attributes describing the inventory, i.e. be similar to FISH WH inventory
                jGenerator.writeStringField("inventory_type", "BLIND");
                jGenerator.writeStringField("user", LocalPreferences.getLoggedInUser("n/a"));
                jGenerator.writeStringField("site", recWHInventory.subSite);
                jGenerator.writeNumberField("created_at", System.currentTimeMillis());
                if (storeLocation) {
                    jGenerator.writeNumberField("latitude", recWHInventory.latitude);
                    jGenerator.writeNumberField("longitude", recWHInventory.longitude);
                }

                jGenerator.writeFieldName("rfid_items");
                jGenerator.writeStartObject(); // {

                for (Map.Entry<String, List<String>> entry : recWHInventory.items.entrySet()) {

                    String catCode = entry.getKey();
                    // set the code of current category.
                    jGenerator.writeFieldName(catCode);

                    List<String> epcs = entry.getValue();
                    // put the epcs in an array
                    jGenerator.writeStartArray(); // [
                    for (String epc : epcs) {
                        jGenerator.writeStartObject(); // {
                        jGenerator.writeStringField("rfid", epc); // "epc..."
                        jGenerator.writeStringField("code", catCode); // "category code..."
                        jGenerator.writeEndObject(); // }
                    }
                    jGenerator.writeEndArray();
                }

                jGenerator.writeEndObject(); // }
            } catch (JsonGenerationException e) {
                e.printStackTrace();
                Toast.makeText(HotelInventoryLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            } catch (JsonMappingException e) {
                e.printStackTrace();
                Toast.makeText(HotelInventoryLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(HotelInventoryLinenActivity.this, getResources().getString(R.string.inventorySaveFailed), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return fileName;
    }

    private class ScanHandler extends Handler {
        private final WeakReference<HotelInventoryLinenActivity> mActivity;

        public ScanHandler(HotelInventoryLinenActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    clearSelectedItem();
                    Map<String, List<String>> values = epcList.stream().map(m -> m.toString()).collect(Collectors.groupingBy(g -> g.substring(0, 4), Collectors.toCollection(ArrayList::new)));
                    if (adapterInventoryItems == null) {
                        adapterInventoryItems = new TreelikeAdapter(HotelInventoryLinenActivity.this, values);
                        xvInventoryItems.setAdapter(adapterInventoryItems);
                    } else {
                        adapterInventoryItems.appendItems(values);
                    }
                    adapterInventoryItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Inventory scanning is over!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

    public class InventoryFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.body() != null) {
                    String fileName = response.body().string();
                    boolean res = FileUtils.deleteInventoryFile(HotelInventoryLinenActivity.this, fileName);
                    if (res) {
                        runOnUiThread(() -> CToast(getApplicationContext(), render("File was uploaded successfully!!!"), Toast.LENGTH_LONG));
                    } else {
                        runOnUiThread(() -> CToast(getApplicationContext(), render("Failed to remove file from local folder!!!"), Toast.LENGTH_LONG));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> CToast(getApplicationContext(), render("Error:" + e.getMessage()), Toast.LENGTH_LONG));
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