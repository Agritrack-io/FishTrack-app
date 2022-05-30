package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.caen.api.BX6100Programmer;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
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
    private TextView tvProductCode, tvItemsCnt, tvFilterEPC;
    private Button btnLinenType, btnWriteEPC;
    private SingleShotScanner scanner;
    private EditText etNextEPC;
    private RecyclerView rvEPCsPerType;
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

        currentTypeSelection.observe(this, response -> {
            if (response != null) {
                currentType = response;
                btnLinenType.setText(currentType);
                tvProductCode.setText(schemeSvc.codeOf(currentType));
                currentProductCode = tvProductCode.getText().toString().trim();
                linenTypeDialog.dismiss();
                if (!Strings.isEmptyOrWhitespace(currentType)) {
                    String currentEPC = this.typeEPCSMap.get(currentType);
                    etNextEPC.setEnabled(true);
                    if (!Strings.isEmptyOrWhitespace(currentEPC)) {
                        etNextEPC.setText(currentEPC);
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
            String currSerialNumber = etNextEPC.getText().toString().trim();
            x9programmer.LowPowerLevel();
            String filterEPCStr = tvFilterEPC.getText().toString().trim();
            if (!Strings.isEmptyOrWhitespace(filterEPCStr)){
                Reader.READER_ERR res = x9programmer.writeTagEPCByFilter(currentProductCode + currSerialNumber, filterEPCStr);
                if (Reader.READER_ERR.MT_OK_ERR.compareTo(res) == 0){
                    ivProgOutcome.setColorFilter(Color.GREEN);
                } else {
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
        //unregister the receiver
        this.stopScanner();
        if (x9programmer != null){
            x9programmer.close();
        }

        super.onStop();
    }

    private void assignCtrlVars() {
        tvProductCode = findViewById(R.id.tvProductCode);
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
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    protected void onClick(View view) {
        scanner = new SingleShotScanner(mScanHandler);
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

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    if (!Strings.isEmptyOrWhitespace(epcStr)) {
                        tvFilterEPC.setText(epcStr);
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