package io.agritrack.philosofish.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.R;

public class SetSortingSampleDialog {

    private final Activity activity;
    private EditText etFishT, etWaterT;
    private Button btnOK;
    private Dialog dialog;
    private DataListener mDataListener;
    private Double fishT, waterT;

    public SetSortingSampleDialog(Activity activity) {
        this.activity = activity;

        setDialog();
        findViews();

        this.etFishT.setText("");
        this.etWaterT.setText("");

        btnOK.setOnClickListener(view -> {
            Double fishTP = !Strings.isEmptyOrWhitespace(this.etFishT.getText().toString()) ? Double.parseDouble(this.etFishT.getText().toString()) : 0;
            Double waterTP = !Strings.isEmptyOrWhitespace(this.etWaterT.getText().toString()) ? Double.parseDouble(this.etWaterT.getText().toString()) : 0;
            this.etFishT.setText("");
            this.etWaterT.setText("");
            if (mDataListener != null) {
                mDataListener.onDataPassed(fishTP, waterTP, null);
            }
            fishT = null;
            waterT = null;
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
        dialog.setContentView(R.layout.set_laundry_temp_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        dialog.getWindow().setAttributes(p);
    }

    private void findViews() {
        btnOK = dialog.findViewById(R.id.btnOk);
        etFishT = dialog.findViewById(R.id.tvFishT);
        etWaterT = dialog.findViewById(R.id.tvWaterT);

        etFishT.setSelectAllOnFocus(true);
        etWaterT.setSelectAllOnFocus(true);
    }

}
