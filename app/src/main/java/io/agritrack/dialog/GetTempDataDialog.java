package io.agritrack.dialog;

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

import androidx.annotation.StringRes;

import com.android.hdhe.uhf.reader.UhfReader;

import cn.pda.serialport.Tools;
import io.agritrack.R;
import io.agritrack.caen.common.CAENRegistersIO;

import static android.os.Looper.getMainLooper;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_INTERVAL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_LOGS;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_RESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_SAMPLES_CNT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIME_BIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.caen.api.EncodingUtils.ToShort;
import static io.agritrack.caen.api.EncodingUtils.parseData;

public class GetTempDataDialog {
    private static Short numOfSamples = Short.valueOf("0");
    private final Activity activity;
    private final byte[] accessPassword = Tools.HexString2Bytes("00000000");
    private TextView tvTitle, txtData;
    private Button btnOk, btnGetData;
    private Dialog dialog;
    private View loadingPanel;
    private final UhfReader _uhfReader;

    public GetTempDataDialog(Activity activity, @StringRes int title) {
        this.activity = activity;

        setDialog();
        findViews();

        _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);

        txtData.setMovementMethod(new ScrollingMovementMethod());
        txtData.setTextColor(Color.BLACK);

        btnOk.setOnClickListener(view -> {
            loadingPanel.setVisibility(View.GONE);
            dismiss();
        });

        btnGetData.setOnClickListener(view -> {
            getTempData(_uhfReader, "300EFE2F94D01C02540BE4BE");
        });
    }

    public void showDialog() {
        dialog.show();
        btnOk.setEnabled(false);
        txtData.setText("Please place the device near the temperature logger and click INITIALIZE");
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void getTempData(UhfReader _uhfReader, String currentBin) {
        _uhfReader.selectEPC(Tools.HexString2Bytes(currentBin));

        try {

            byte[] reply0;

            reply0 = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_SAMPLES_CNT, SHORT_ONE, accessPassword);
            numOfSamples = ToShort(reply0);
            if (numOfSamples == 0) {
                txtData.append("count of samples is 0. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }
            txtData.setText("Successfully count samples: " + numOfSamples + "\n");

            int counter1 = 10;
            while (counter1 > 0) {
                byte[] temperatures = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_LOGS, (short) (numOfSamples * 3), accessPassword);
                if (temperatures == null) {
                    txtData.append("Failed to read temperatures. Please scan bin again");
                } else {
                    txtData.append("DATA:\n" + parseData(temperatures));
                    break;
                }
                counter1--;
            }
            if (counter1 == 0) {
                txtData.setText("Failed to load temperatures. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }

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
        btnGetData = (Button) dialog.findViewById(R.id.btnGetData);
        txtData = dialog.findViewById(R.id.etData);
        loadingPanel = dialog.findViewById(R.id.loadingPanel);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
