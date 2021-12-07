package io.agritrack.dialog;

import static android.os.Looper.getMainLooper;
import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.TempInterval;
import static io.agritrack.caen.api.EncodingUtils.parseTemperature;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
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

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;

public class TempLoggerDialog {
    private final Activity activity;
    private final UhfReader uhfReader;
    private TextView tvTitle, txtData;
    private Button btnOk, btnInit;
    private Dialog dialog;
    private View loadingPanel;
    private String currentBinEPC;

    public TempLoggerDialog(Activity activity, @StringRes int title) {
        this.activity = activity;

        setDialog();
        findViews();

        this.uhfReader = UhfReader.getInstance();
        this.uhfReader.setWorkArea(3);
        this.uhfReader.setOutputPower(24);

        txtData.setMovementMethod(new ScrollingMovementMethod());
        txtData.setTextColor(Color.parseColor("#16325c"));

        btnOk.setOnClickListener(view -> {
            loadingPanel.setVisibility(View.GONE);
            dismiss();
        });

        btnInit.setOnClickListener(view -> {
            try {
                initDataLogger(this.currentBinEPC);
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

    public void initDataLogger(String currentBin) throws InterruptedException {

        if (Strings.isEmptyOrWhitespace(this.currentBinEPC) && !IsDemo) {
            CToast(this.activity.getApplicationContext(), render("NO Logger Tag detected!!!"), Toast.LENGTH_LONG);
            return;
        }
        String lastTemperatureStr = "N/A";
        Handler handler = new Handler(getMainLooper());
        handler.post(() -> loadingPanel.setVisibility(View.VISIBLE));

        try {
            CAENCommander cmd = new CAENCommander(this.uhfReader, currentBin);
            cmd.HighSensitivity();
            CAENCommander.Response rs = cmd.RESET();
            short lastTemperature = cmd.INIT(TempInterval);
            lastTemperatureStr = parseTemperature(lastTemperature) + "\u2103";
            cmd.LowSensitivity();
        } catch (Exception e) {
            e.printStackTrace();
        }

        txtData.setText(String.format("Successful initialization. \n\n[ACCEPTABLE TEMPERATURE: %s]", lastTemperatureStr));
        loadingPanel.setVisibility(View.GONE);
        btnOk.setEnabled(true);
        btnOk.setTextColor(Color.parseColor("#FFEB3B"));
        btnInit.setEnabled(false);
        btnInit.setTextColor(Color.GRAY);
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.temp_logger_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        btnOk = dialog.findViewById(R.id.btnOk);
        btnInit = dialog.findViewById(R.id.btnInit);
        txtData = dialog.findViewById(R.id.etData);
        loadingPanel = dialog.findViewById(R.id.loadingPanel);
    }
}
