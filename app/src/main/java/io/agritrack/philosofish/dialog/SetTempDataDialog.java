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

import java.util.Locale;

import io.agritrack.philosofish.R;

public class SetTempDataDialog {
    private final Activity activity;
    private EditText tvSurfaceT, tvBottomT, corrAction;
    private Button btnOk;
    private Dialog dialog;
    private DataListener mDataListener;
    private Double surfaceT, bottomT;
    private String corrAct;

    public SetTempDataDialog(Activity activity, Double surfaceT, Double bottomT, String  corrActi) {
        this.activity = activity;
        this.surfaceT = surfaceT;
        this.bottomT = bottomT;
        this.corrAct = corrActi;

        setDialog();
        findViews();

        this.tvSurfaceT.setText(surfaceT == null ? "" : String.format(Locale.US,"%.1f", surfaceT));
        this.tvBottomT.setText(bottomT == null ? "" : String.format(Locale.US,"%.1f", bottomT));
        this.corrAction.setText(corrActi == null ? "" : corrActi);

        btnOk.setOnClickListener(view -> {
            Double fishTP = !Strings.isEmptyOrWhitespace(this.tvSurfaceT.getText().toString()) ? Double.parseDouble(this.tvSurfaceT.getText().toString()) : 0;
            Double waterTP = !Strings.isEmptyOrWhitespace(this.tvBottomT.getText().toString()) ? Double.parseDouble(this.tvBottomT.getText().toString()) : 0;
            String corrAct = !Strings.isEmptyOrWhitespace(this.corrAction.getText().toString()) ? (this.corrAction.getText().toString()) : "";
            if (mDataListener != null) {
                mDataListener.onDataPassed(fishTP, waterTP, corrAct);
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
        tvSurfaceT = dialog.findViewById(R.id.tvFishT);
        tvBottomT = dialog.findViewById(R.id.tvWaterT);
        corrAction = dialog.findViewById(R.id.tvFishT2);

        tvSurfaceT.setSelectAllOnFocus(true);
        tvBottomT.setSelectAllOnFocus(true);
        corrAction.setSelectAllOnFocus(true);
    }
}
