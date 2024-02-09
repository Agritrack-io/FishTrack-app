package io.agritrack.fish.ui.quality.postpackage;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recQuality;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

import io.agritrack.kefalonia.R;
import io.agritrack.barcode.BarcodeScanService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.service.LocalPreferences;

public class PostPackagingQualityActivity extends AppCompatActivity {

    private static long timestamp;
    private EditText etT1, etT2, etT3;
    private TextView tvCurrentDate, tvCurrentLot, tvCurrentBox;
    private ProgressDialog progressDialog;
    private boolean scanning = false;
    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (barcode.length() >= 24) {
                    String currentLot = barcode.substring(18, 24);
                    String currentBox = barcode.substring(barcode.length() - 8);
                    Set<String> boxSn = LocalPreferences.getBoxSn();
                    if (!boxSn.contains(currentBox)) {
                        tvCurrentLot.setText(currentLot);
                        tvCurrentBox.setText(currentBox);
                    } else {
                        tvCurrentLot.setText(currentLot);
                        tvCurrentBox.setText("");
                        CToast(getApplicationContext(), render(R.string.scan_another_box), Toast.LENGTH_LONG);
                    }
                    scanning = false;
                }
            }
        }
    };
    private BarcodeScanService scanService;
    private Button btnScanBox;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String Today() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();
        Date convertedDatetime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        timestamp = convertedDatetime.getTime();

        return dtf.format(now);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_packaging_quality);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderAfterPackagingQuality);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(PostPackagingQualityActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        assignCtrlVars();

        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PostPackagingQualityActivity.this);
            supportDialog.showDialog();
        });

        InputFilter textFilter = new InputFilter() {
            final int maxDigitsBeforeDecimalPoint = 2;
            final int maxDigitsAfterDecimalPoint = 2;

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source
                        .subSequence(start, end).toString());
                if (!builder.toString().matches(
                        "(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"
                )) {
                    if (source.length() == 0)
                        return dest.subSequence(dstart, dend);
                    return "";
                }
                return null;
            }
        };

        btnScanBox.setOnClickListener(view -> {
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        etT1.setOnClickListener(v -> {
            if (!Strings.isEmptyOrWhitespace(etT1.getText().toString()) && Double.parseDouble(etT1.getText().toString()) > 7) {
                etT1.setBackgroundColor(Color.RED);
            } else if (!Strings.isEmptyOrWhitespace(etT1.getText().toString()) && Double.parseDouble(etT1.getText().toString()) <= 7) {
                etT1.setBackgroundColor(Color.WHITE);
            }
            etT2.requestFocus();
        });

        etT2.setOnClickListener(v -> {
            if (!Strings.isEmptyOrWhitespace(etT2.getText().toString()) && Double.parseDouble(etT2.getText().toString()) > 7) {
                etT2.setBackgroundColor(Color.RED);
            } else if (!Strings.isEmptyOrWhitespace(etT2.getText().toString()) && Double.parseDouble(etT2.getText().toString()) <= 7) {
                etT2.setBackgroundColor(Color.WHITE);
            }
            etT3.requestFocus();
        });

        etT3.setOnEditorActionListener((v, actionId, event) -> {
            if (!Strings.isEmptyOrWhitespace(etT3.getText().toString()) && Double.parseDouble(etT3.getText().toString()) > 7) {
                etT3.setBackgroundColor(Color.RED);
            } else if (!Strings.isEmptyOrWhitespace(etT3.getText().toString()) && Double.parseDouble(etT3.getText().toString()) <= 7) {
                etT3.setBackgroundColor(Color.WHITE);
            }
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //Clear focus here from edittext
                etT3.clearFocus();
            }
            return false;
        });

        etT1.setFilters(new InputFilter[]{textFilter});
        etT2.setFilters(new InputFilter[]{textFilter});
        etT3.setFilters(new InputFilter[]{textFilter});

        tvCurrentDate.setText(Today());

        configFooter();
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PostPackagingQualityConfirmActivity.class);
                startActivity(i);
            }
        });

        ivBack.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
    }

    private void startScanning() {
        if (scanService != null) {
            scanning = true;
            scanService.scan();
        }
    }

    private void stopScanning() {
        if (scanService != null) {
            scanService.stopScan();
            scanning = false;
        }
    }

    private void assignCtrlVars() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCurrentBox = findViewById(R.id.tvCurrentBox);
        tvCurrentLot = findViewById(R.id.tvCurrentLot);
        etT1 = findViewById(R.id.etT1);
        etT2 = findViewById(R.id.etT2);
        etT3 = findViewById(R.id.etT3);
        ivNext = findViewById(R.id.ivToAfterPackagingQualityConfirm);
        ivBack = findViewById(R.id.ivBackToQualityMenu);
        btnScanBox = findViewById(R.id.btnScanBox);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        if (!Strings.isEmptyOrWhitespace(recQuality.pLot)) {
            tvCurrentLot.setText(recQuality.pLot);
        }
        if (!Strings.isEmptyOrWhitespace(recQuality.boxSn)) {
            tvCurrentBox.setText(recQuality.boxSn);
        }
        if (recQuality.etT1 != null) {
            etT1.setText(String.valueOf(recQuality.etT1));
        }
        if (recQuality.etT2 != null) {
            etT2.setText(String.valueOf(recQuality.etT2));
        }
        if (recQuality.etT3 != null) {
            etT3.setText(String.valueOf(recQuality.etT3));
        }
    }

    private void updateState() {

        if (etT1.getText() != null && !Strings.isEmptyOrWhitespace(etT1.getText().toString())) {
            recQuality.etT1 = Double.valueOf(etT1.getText().toString());
        }
        if (etT2.getText() != null && !Strings.isEmptyOrWhitespace(etT2.getText().toString())) {
            recQuality.etT2 = Double.valueOf(etT2.getText().toString());
        }
        if (etT3.getText() != null && !Strings.isEmptyOrWhitespace(etT3.getText().toString())) {
            recQuality.etT3 = Double.valueOf(etT3.getText().toString());
        }
        if (tvCurrentDate.getText() != null && !Strings.isEmptyOrWhitespace(tvCurrentDate.getText().toString())) {
            recQuality.timestamp = timestamp;
        }
        if (tvCurrentLot.getText() != null && !Strings.isEmptyOrWhitespace(tvCurrentLot.getText().toString())) {
            recQuality.pLot = tvCurrentLot.getText().toString();
        }
        if (tvCurrentBox.getText() != null && !Strings.isEmptyOrWhitespace(tvCurrentBox.getText().toString())) {
            recQuality.boxSn = tvCurrentBox.getText().toString();
            LocalPreferences.addBoxSn(recQuality.boxSn);
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recQuality.pLot == null || recQuality.boxSn == null) {
                sb.append(String.format("\n%s is missing", "'Scan'"));
            }

            if (recQuality.etT1 == null) {
                sb.append(String.format("\n%s is missing", "'T1 temperature'"));
            }

            if (recQuality.etT2 == null) {
                sb.append(String.format("\n%s is missing", "'T2 temperature'"));
            }

            if (recQuality.etT3 == null) {
                sb.append(String.format("\n%s is missing", "'T3 temperature'"));
            }
        }

        return sb.toString();
    }

    @Override
    protected void onResume() {
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}