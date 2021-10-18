package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.android.hdhe.uhf.reader.UhfReader;

import cn.pda.serialport.Tools;
import io.agritrack.R;
import io.agritrack.caen.common.CAENRegistersIO;

import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_INTERVAL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_RESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIME_BIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class TempLoggerDialog {
    private TextView tvTitle, txtData;
    private Button btnOk;
    private final Activity activity;
    private Dialog dialog;
    private View loadingPanel;
    private final byte[] accessPassword = Tools.HexString2Bytes("00000000");

    public TempLoggerDialog (Activity activity, @StringRes int title) {
        this.activity = activity;

        setDialog();
        findViews();
        SetCaptions(title);

        btnOk.setOnClickListener(view -> {
            dismiss();
            CToast(activity.getApplicationContext(), render("Temp logger successfully initialized"), Toast.LENGTH_LONG);
        });
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void initDataLogger(UhfReader _uhfReader, String currentBin){
        _uhfReader.selectEPC(Tools.HexString2Bytes(currentBin));
        try {
            int counter0 = 10;
            byte reply0 = 1;
            while (counter0>0 && reply0!=0) {
                reply0 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_RESET, SHORT_ONE, SHORT_ONE, accessPassword);
                if (reply0==0){
                    txtData.setText("1. successfully reset\n");
                    break;
                }
                counter0--;
            }
            if (counter0==0){
                txtData.setText("Failed to reset data logger. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }

            int counter1 = 10;
            long unixTime = System.currentTimeMillis() / 1000L;
            byte reply1 = 1;
            while (counter1>0 && reply1!=0) {
                reply1 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIMESTAMP, (short) 2, unixTime, accessPassword);
                if (reply1==0){
                    txtData.append("2. successfully timestamp\n");
                    break;
                }
                counter1--;
                Thread.sleep(100);
            }
            if (counter1==0){
                txtData.setText("Failed to set timestamp. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }

            int counter2 = 10;
            byte reply2 = 1;
            while (counter2>0 && reply2!=0) {
                reply2 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIME_BIN, (short) 1, (short) 1, accessPassword);
                if (reply2==0){
                    txtData.append("3. successfully time bin\n");
                    break;
                }
                counter2--;
            }
            if (counter2==0){
                txtData.setText("Failed to set time bin. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }

            int counter3 = 10;
            short interval = 30;
            byte reply3 = 1;
            while (counter3>0 && reply3!=0) {
                reply3 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_INTERVAL, (short) 1, interval, accessPassword);
                if (reply3==0){
                    txtData.append("4. successfully interval\n");
                    break;
                }
                counter3--;
            }
            if (counter3==0){
                txtData.setText("Failed to set interval. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }

            int counter4 = 10;
            byte reply4 = 1;
            while (counter4>0 && reply4!=0) {
                reply4 = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_RESET, (short) 1, (short) 4, accessPassword);
                if (reply4==0){
                    txtData.append("5. successfully initialized");
                    loadingPanel.setVisibility(View.GONE);
                    break;
                }
                counter4--;
            }
            if (counter4==0){
                txtData.setText("Failed to start logger. Please scan bin again.");
                loadingPanel.setVisibility(View.GONE);
                return;
            }
            loadingPanel.setVisibility(View.GONE);

            if (reply0+reply1+reply2+reply3+reply4 >0){
                txtData.setText("Please scan bin again.");
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
        txtData = dialog.findViewById(R.id.etData);
        loadingPanel = dialog.findViewById(R.id.loadingPanel);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
