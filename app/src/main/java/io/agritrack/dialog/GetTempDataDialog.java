package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import io.agritrack.R;

public class GetTempDataDialog {
    private final Activity activity;
    private TextView txtData;
    private Button btnOk;
    private Dialog dialog;
    private final String binEPC;
    private Double temperature;

    public GetTempDataDialog(Activity activity, Double temperature, String binEPC) {
        this.activity = activity;
        this.temperature = temperature;
        this.binEPC = binEPC;

        setDialog();
        findViews();

        txtData.setTextColor(Color.parseColor("#16325c"));

        btnOk.setOnClickListener(view -> {
            dismiss();
        });
    }

    public void showDialog() {
        dialog.show();
        if (temperature <=  Double.valueOf(4)) {
            String text = String.format("The temperature %.2f \u2103 in the bin %s is acceptable.", temperature, binEPC);
            txtData.setText(text);
        } else {
            String text = String.format("The temperature %.2f \u2103 in the bin %s is not acceptable. Please check ice adequacy.", temperature, binEPC);
            txtData.setText(text);
        }
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.get_temp_data_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        dialog.getWindow().setAttributes(p);
    }

    private void findViews() {
        btnOk = dialog.findViewById(R.id.btnOk);
        txtData = dialog.findViewById(R.id.etData);
    }
}
