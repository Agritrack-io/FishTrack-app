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
    private final String binEPC; // NEW → unique per bin

    private EditText tvSurfaceT, tvBottomT, corrAction;
    private Button btnOk;
    private Dialog dialog;

    private DataListener mDataListener;
    private Double surfaceT, bottomT;
    private String corrAct;

    public interface DataListener {
        void onDataPassed(String binEPC, Double surface, Double bottom, String corrAct); // EPC added
    }

    public SetTempDataDialog(Activity activity, String binEPC, Double surfaceT, Double bottomT, String corrActi) {
        this.activity = activity;
        this.binEPC = binEPC;     // store bin id
        this.surfaceT = surfaceT;
        this.bottomT = bottomT;
        this.corrAct = corrActi;

        setDialog();
        findViews();

        tvSurfaceT.setText(surfaceT == null ? "" : String.format(Locale.US, "%.1f", surfaceT));
        tvBottomT.setText(bottomT == null ? "" : String.format(Locale.US, "%.1f", bottomT));
        corrAction.setText(corrActi == null ? "" : corrActi);

        btnOk.setOnClickListener(v -> {
            Double fishTP = !Strings.isEmptyOrWhitespace(tvSurfaceT.getText().toString()) ? Double.valueOf(tvSurfaceT.getText().toString()) : null;
            Double waterTP = !Strings.isEmptyOrWhitespace(tvBottomT.getText().toString()) ? Double.valueOf(tvBottomT.getText().toString()) : null;
            String act = !Strings.isEmptyOrWhitespace(corrAction.getText().toString()) ? corrAction.getText().toString() : null;

            if (mDataListener != null) mDataListener.onDataPassed(binEPC, fishTP, waterTP, act);
            dismiss();
        });
    }

    public void setMyDialogListener(DataListener listener) {
        this.mDataListener = listener;
    }

    public void showDialog() {
        dialog.show();
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
