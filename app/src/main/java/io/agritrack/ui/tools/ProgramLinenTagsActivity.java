package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.caen.api.BX6100Programmer;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;

public class ProgramLinenTagsActivity extends AppCompatActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    // Local handler that receives the RFID scanner results.
    private final ProgramLinenTagsActivity.ScanHandler mScanHandler = new ProgramLinenTagsActivity.ScanHandler(this);
    private final MutableLiveData<String> currentTypeSelection = new MutableLiveData<>();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private SimpleListDialog linenTypeDialog;
    private MobileDB db;
    private BX6100Programmer x9programmer;
    private Map<String, String> typeEPCSMap;
    private TextView tvProductCode, tvItemsCnt, tvFilterEPC, tvProgOutcome;
    private Button btnLinenType, btnWriteEPC;
    private SingleShotScanner scanner;
    private YesNoDialogFragment confirmWriteEpcDlg;
    private EditText etNextEPC;
    private RecyclerView rvEPCsPerType;
    private TemplateRecyclerAdapter adapterEPC;
    private ImageView ivProgOutcome;
    private String currentType, currentProductCode;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_linen_tags);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProgramLinen);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        this.typeEPCSMap = new HashMap<>();
        x9programmer = new BX6100Programmer();

        // get  references of the controls
        assignCtrlVars();

        // Initialize type EPCs map
        for (String productName : schemeSvc.distinctNamesOnly()) {
            String productCode = schemeSvc.codeOf(productName);
            String lastEPC = LocalPreferences.getLinenRFID(productCode);
            if (!Strings.isEmptyOrWhitespace(lastEPC)) {
                typeEPCSMap.put(productCode, lastEPC);
            }
        }

        confirmWriteEpcDlg = YesNoDialogFragment.instance();
        confirmWriteEpcDlg.onConfirm(bundle -> {
            confirmWriteEpc();
        });
        confirmWriteEpcDlg.onReject(bundle -> {

        });

        List<String> adapterData = typeEPCSMap.entrySet().stream().map(x -> String.format("%s: %s [%s]",x.getKey(), schemeSvc.nameOf(x.getKey()), x.getValue())).collect(Collectors.toList());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvEPCsPerType.setLayoutManager(layoutManager);
        rvEPCsPerType.setItemAnimator(new DefaultItemAnimator());
        adapterEPC = new TemplateRecyclerAdapter(this, adapterData, false);
        rvEPCsPerType.setAdapter(adapterEPC);
        rvEPCsPerType.setNestedScrollingEnabled(false);

        etNextEPC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (etNextEPC.getText().toString().trim().length() < 4) {
                        etNextEPC.setError("Type 4 digits");
                    } else {
                        // your code here
                        etNextEPC.setError(null);
                    }
                } else {
                    if (etNextEPC.getText().toString().trim().length() < 4) {
                        etNextEPC.setError("Type 4 digits");
                    } else {
                        // your code here
                        etNextEPC.setError(null);
                    }
                }
            }
        });

        currentTypeSelection.observe(this, response -> {
            if (response != null) {
                currentType = response;
                btnLinenType.setText(currentType);
                tvProductCode.setText(schemeSvc.codeOf(currentType));
                currentProductCode = tvProductCode.getText().toString().trim();
                linenTypeDialog.dismiss();
                if (!Strings.isEmptyOrWhitespace(currentType)) {
                    String currentEPC = this.typeEPCSMap.get(currentProductCode);
                    if (!Strings.isEmptyOrWhitespace(currentEPC)) {
                        int value = Integer.parseInt(currentEPC, 16);
                        value++;
                        String hex = String.format("%04X", value);
                        etNextEPC.setText(hex);
                        etNextEPC.setEnabled(false);
                    } else {
                        etNextEPC.setEnabled(true);
                        etNextEPC.setText("");
                    }
                } else {
                    etNextEPC.setEnabled(false);
                }
            }
        });

        btnLinenType.setOnClickListener(view -> {
            linenTypeDialog = new SimpleListDialog(ProgramLinenTagsActivity.this, Arrays.asList(schemeSvc.distinctNamesOnly()), currentTypeSelection, R.string.type_linen);
            linenTypeDialog.showDialog();
        });

        btnWriteEPC.setOnClickListener(view -> {
            if (etNextEPC.getText().toString().trim().length() < 4) {
                etNextEPC.setError("Type 4 digits");
                return;
            }
            String currSerialNumber = etNextEPC.getText().toString().trim();
            //x9programmer.LowPowerLevel();
            String filterEPCStr = tvFilterEPC.getText().toString().trim();
            if (!Strings.isEmptyOrWhitespace(filterEPCStr)) {
                Reader.READER_ERR res = x9programmer.writeTagEPCByFilter(currentProductCode + currSerialNumber, filterEPCStr);
                if (Reader.READER_ERR.MT_OK_ERR.compareTo(res) == 0) {
                    tvProgOutcome.setText(R.string.success);
                    tvProgOutcome.setTextColor(Color.GREEN);
                    ivProgOutcome.setColorFilter(Color.GREEN);
                    int value = Integer.parseInt(currSerialNumber, 16);
                    value++;
                    String hex = String.format("%04X", value);
                    etNextEPC.setText(hex);
                    tvFilterEPC.setText("");

                    // Update local cache values for current product code
                    typeEPCSMap.put(currentProductCode, currSerialNumber);
                    LocalPreferences.setLinenRFID(currentProductCode, currSerialNumber);

                    final List<String> adapterData1 = typeEPCSMap.entrySet().stream().map(x -> String.format("%s: %s [%s]", x.getKey(), schemeSvc.nameOf(x.getKey()), x.getValue())).collect(Collectors.toList());
                    adapterEPC.setValues(adapterData1);
                    adapterEPC.notifyDataSetChanged();

                    // Lock next EPC field
                    etNextEPC.setEnabled(false);
                } else {
                    tvProgOutcome.setText(R.string.failure);
                    tvProgOutcome.setTextColor(Color.RED);
                    ivProgOutcome.setColorFilter(Color.RED);
                }
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProgramLinenTagsActivity.this);
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
        scanner.HighEnergy();
        //unregister the receiver
        this.stopScanner();
        if (x9programmer != null) {
            x9programmer.stopProgramming();
        }
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @SuppressLint("StringFormatMatches")
    private void confirmWriteEpc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Set up the buttons
        AlertDialog dialog = builder.setTitle(getString(R.string.confirm_writing_epc))
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean wantToCloseDialog = false;
                        String code = input.getText().toString();
                        if (code.equalsIgnoreCase("2222")) {
                            btnWriteEPC.setEnabled(true);
                            btnWriteEPC.setTextColor(Color.GREEN);
                            input.getShowSoftInputOnFocus();
                            wantToCloseDialog = true;
                        } else {
                            builder.setMessage("Wrong typing");
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }

    private void assignCtrlVars() {
        tvProductCode = findViewById(R.id.tvProductCode);
        tvProgOutcome = findViewById(R.id.tvProgOutcome);
        tvFilterEPC = findViewById(R.id.tvFilterEPC);
        etNextEPC = findViewById(R.id.etNextEPC);
        btnLinenType = findViewById(R.id.btnLinenType);
        ivProgOutcome = findViewById(R.id.ivProgOutcome);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        btnWriteEPC = findViewById(R.id.btnWriteEPC);
        rvEPCsPerType = findViewById(R.id.rvEPCsPerType);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (mScanHandler!=null) {
                stopScanner();
            }

            //Stop scanning since we navigate to previous activity
            if (scanner != null) {
                scanner.stopReading();
            }
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    protected void onClick(View view) {
        scanner = new SingleShotScanner(mScanHandler);
        scanner.LowEnergy();
        scanner.startReading();
        mScanHandler.postDelayed(scanner, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner != null) {
            this.scanner.stopReading();
            mScanHandler.removeCallbacks(this.scanner);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<ProgramLinenTagsActivity> mActivity;

        public ScanHandler(ProgramLinenTagsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    tvProgOutcome.setText("");
                    ivProgOutcome.setColorFilter(null);

                    if (!Strings.isEmptyOrWhitespace(epcStr)) {
                        if (epcStr.length() > 8) {
                            tvFilterEPC.setText(epcStr);
                            tvFilterEPC.setTextColor(Color.GREEN);
                            btnWriteEPC.setEnabled(true);
                            btnWriteEPC.setTextColor(Color.GREEN);
                        } else {
                            tvFilterEPC.setText(epcStr);
                            tvFilterEPC.setTextColor(Color.RED);
                            btnWriteEPC.setEnabled(false);
                            btnWriteEPC.setTextColor(Color.DKGRAY);
                            FragmentManager fm = getSupportFragmentManager();
                            confirmWriteEpcDlg.setMessage(getString(R.string.proceed_with_written_epc, epcStr));
                            confirmWriteEpcDlg.showNow(fm, getString(R.string.confirm_selection));
                            //CToast(getApplicationContext(), render("Tag is already programmed!!"), Toast.LENGTH_SHORT);
                        }
                    } else {
                        CToast(getApplicationContext(), render("No Tag was detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}