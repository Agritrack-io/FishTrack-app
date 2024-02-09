package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.util.Strings;

import io.agritrack.kefalonia.R;

public class SetTempDataDialog {
    private final Activity activity;
    private EditText tvFishT, tvWaterT, tvFishT2;
    private Button btnOk;
    private Dialog dialog;
    private DataListener mDataListener;
    private Double fishT, waterT, fishT2;

    public SetTempDataDialog(Activity activity, Double fishT, Double waterT, Double fishT2) {
        this.activity = activity;
        this.fishT = fishT;
        this.waterT = waterT;
        this.fishT2 = fishT2;

        setDialog();
        findViews();

        this.tvFishT.setText(fishT == null ? "" : String.valueOf(fishT));
        this.tvWaterT.setText(waterT == null ? "" : String.valueOf(waterT));
        this.tvFishT2.setText(fishT2 == null ? "" : String.valueOf(fishT2));

        btnOk.setOnClickListener(view -> {
            Double fishTP = !Strings.isEmptyOrWhitespace(this.tvFishT.getText().toString()) ? Double.parseDouble(this.tvFishT.getText().toString()) : 0;
            Double waterTP = !Strings.isEmptyOrWhitespace(this.tvWaterT.getText().toString()) ? Double.parseDouble(this.tvWaterT.getText().toString()) : 0;
            Double fishT2P = !Strings.isEmptyOrWhitespace(this.tvFishT2.getText().toString()) ? Double.parseDouble(this.tvFishT2.getText().toString()) : 0;
            if (mDataListener != null) {
                mDataListener.onDataPassed(fishTP, waterTP, fishT2P);
            }
            dismiss();
        });
    }

    public void setMyDialogListener(DataListener mDataListener) {
        this.mDataListener = mDataListener;
    }

    public void showDialog() {
        dialog.show();
//        if (temperature <=  Double.valueOf(4)) {
//            String text = String.format("The temperature %.2f \u2103 in the bin %s is acceptable.", temperature, binEPC);
//            txtData.setText(text);
//        } else {
//            String text = String.format("The temperature %.2f \u2103 in the bin %s is not acceptable. Please check ice adequacy.", temperature, binEPC);
//            txtData.setText(text);
//        }
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_temp_data_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        dialog.getWindow().setAttributes(p);
    }

    private void findViews() {
        btnOk = dialog.findViewById(R.id.btnOk);
        tvFishT = dialog.findViewById(R.id.tvFishT);
        tvWaterT = dialog.findViewById(R.id.tvWaterT);
        tvFishT2 = dialog.findViewById(R.id.tvFishT2);

        tvFishT.setSelectAllOnFocus(true);
        tvWaterT.setSelectAllOnFocus(true);
        tvFishT2.setSelectAllOnFocus(true);
    }
}
