package io.agritrack.dialog;

import static android.os.Looper.getMainLooper;
import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import cn.pda.serialport.Tools;
import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;
import io.agritrack.caen.common.CAENRegistersIO;

public class TempLoggerDialog {
    private static final Short numOfSamples = Short.valueOf("0");
    private final Activity activity;
    private final byte[] accessPassword = Tools.HexString2Bytes("00000000");
    private final UhfReader _uhfReader;
    private TextView tvTitle, txtData;
    private Button btnOk, btnInit;
    private Dialog dialog;
    private View loadingPanel;
    private String currentBinEPC;

    public TempLoggerDialog(Activity activity, @StringRes int title) {
        this.activity = activity;

        setDialog();
        findViews();

        _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);
        _uhfReader.setOutputPower(24);

        txtData.setMovementMethod(new ScrollingMovementMethod());
        txtData.setTextColor(Color.parseColor("#16325c"));

        btnOk.setOnClickListener(view -> {
            loadingPanel.setVisibility(View.GONE);
            dismiss();
        });

        btnInit.setOnClickListener(view -> {
            try {
                initDataLogger(_uhfReader, this.currentBinEPC);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void showDialog(String binEPC) {
        this.currentBinEPC = binEPC;
        dialog.show();
        btnOk.setEnabled(false);
        btnOk.setTextColor(Color.GRAY);
        txtData.setText("Please place the device near the temperature logger and click INITIALIZE");
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void initDataLogger(UhfReader _uhfReader, String currentBin) throws InterruptedException {

        if (Strings.isEmptyOrWhitespace(this.currentBinEPC) && !IsDemo) {
            CToast(this.activity.getApplicationContext(), render("NO Logger Tag detected!!!"), Toast.LENGTH_LONG);
            return;
        }

        _uhfReader.selectEPC(Tools.HexString2Bytes(currentBin));
        Handler handler = new Handler(getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                loadingPanel.setVisibility(View.VISIBLE);
            }
        });

//        Thread.sleep(1000);

//        txtData.setText("Successful initialization. \n\n[ACCEPTABLE TEMPERATURE.]");
//        loadingPanel.setVisibility(View.GONE);
//        btnOk.setEnabled(true);
//        btnOk.setTextColor(Color.parseColor("#FFEB3B"));
//        btnInit.setEnabled(false);
//        btnInit.setTextColor(Color.GRAY);
//        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
//        tg.startTone(ToneGenerator.TONE_PROP_BEEP);


        try {
//            byte reply0 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_CONTROL, SHORT_ONE, SHORT_ONE, accessPassword);
//            if (reply0 != REPLY_NACK) {
//                reply0 = 0;
//            } else {
//                txtData.setText("Failed to reset data logger. Please scan bin again.");
//                loadingPanel.setVisibility(View.GONE);
//                return;
//            }
//
            long unixTime = System.currentTimeMillis() / 1000L;
            int reply1 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIMESTAMP, SHORT_TWO, unixTime, accessPassword);
            if (reply1 != REPLY_NACK) {
                reply1 = 0;
            } else {
                txtData.setText("Failed to set timestamp. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }
//
//            int reply2 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIME_BIN, SHORT_ONE, SHORT_ONE, accessPassword);
//            if (reply2 != REPLY_NACK) {
//                reply2 = 0;
//            } else {
//                txtData.setText("Failed to set time bin. Please scan bin again.");
//                loadingPanel.setVisibility(View.GONE);
//                return;
//            }
//
//            short interval = 30;
//            int reply3 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_INTERVAL, SHORT_ONE, interval, accessPassword);
//            if (reply3 != REPLY_NACK) {
//                reply3 = 0;
//            } else {
//                txtData.setText("Failed to set interval. Please scan bin again.");
//                loadingPanel.setVisibility(View.GONE);
//                return;
//            }
//
//            int reply4 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_CONTROL, SHORT_ONE, SHORT_FOUR, accessPassword);
//            if (reply4 != REPLY_NACK) {
//                txtData.setText("Successfully initialized");
//                loadingPanel.setVisibility(View.GONE);
//                reply4 = 0;
//            } else {
//                txtData.setText("Failed to start logger. Please scan bin again.");
//                loadingPanel.setVisibility(View.GONE);
//                return;
//            }
//
//            loadingPanel.setVisibility(View.GONE);
//            btnOk.setEnabled(true);
//            btnOk.setTextColor(Color.parseColor("#FFEB3B"));
//            btnInit.setEnabled(false);
//            btnInit.setTextColor(Color.GRAY);
//
//            if (reply0 + reply1 + reply2 + reply3 + reply4 > 0) {
//                txtData.setText("Please scan bin again.");
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            CAENCommander cmd = new CAENCommander();
            //short rev = cmd.READ_REVISION();
            short cnt = cmd.READ_SAMPLES_COUNT();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.temp_logger_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
        btnInit = (Button) dialog.findViewById(R.id.btnInit);
        txtData = dialog.findViewById(R.id.etData);
        loadingPanel = dialog.findViewById(R.id.loadingPanel);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
