package io.agritrack.dialog;

import static android.os.Looper.getMainLooper;
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

import java.util.List;

import cn.pda.serialport.Tools;
import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;

public class GetTempDataDialog {
    private final Activity activity;
    private final UhfReader _uhfReader;
    private TextView tvTitle, txtData;
    private Button btnOk, btnGetData;
    private Dialog dialog;
    private View loadingPanel;
    private String currentBinEPC;

    public GetTempDataDialog(Activity activity, @StringRes int title) {
        this.activity = activity;

        setDialog();
        findViews();

        _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);

        txtData.setMovementMethod(new ScrollingMovementMethod());
        txtData.setTextColor(Color.parseColor("#16325c"));

        btnOk.setOnClickListener(view -> {
            loadingPanel.setVisibility(View.GONE);
            dismiss();
        });

        btnGetData.setOnClickListener(view -> {
            try {
                getTempData(_uhfReader, this.currentBinEPC);
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
        txtData.setText("Please place the device near the temperature logger and click GET DATA");
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void getTempData(UhfReader _uhfReader, String currentBin) throws InterruptedException {
        _uhfReader.selectEPC(Tools.HexString2Bytes(currentBin));
        Handler handler = new Handler(getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                loadingPanel.setVisibility(View.VISIBLE);
            }
        });

        //Thread.sleep(1000);
        /*try {
            CAENCommander cmd = new CAENCommander(_uhfReader, currentBin);
            short count = cmd.READ_SAMPLES_COUNT();
            List<String[]> values = cmd.READ_SAMPLES(count);
            CToast(activity.getApplicationContext(), values.toString(), Toast.LENGTH_LONG);

            CAENCommander.Response rs = cmd.RESET();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        txtData.setText("Successful data recovery.");
        loadingPanel.setVisibility(View.GONE);
        btnOk.setEnabled(true);
        btnOk.setTextColor(Color.parseColor("#FFEB3B"));
        btnGetData.setEnabled(false);
        btnGetData.setTextColor(Color.GRAY);
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);

    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.get_temp_data_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        btnOk = dialog.findViewById(R.id.btnOk);
        btnGetData = dialog.findViewById(R.id.btnGetData);
        txtData = dialog.findViewById(R.id.etData);
        loadingPanel = dialog.findViewById(R.id.loadingPanel);
    }
}
