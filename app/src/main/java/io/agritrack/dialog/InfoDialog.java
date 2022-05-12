package io.agritrack.dialog;

import static io.agritrack.fish.state.GlobalState.recFishing;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.fish.state.FishingRecord;

public class InfoDialog {

    private TextView tvHarvest, tvCageName, tvFishType, tvFishSize, tvRequestedQuantity, tvPackagingPlant;
    private Button btnOk;
    private final Activity activity;
    private Dialog dialog;

    public InfoDialog (Activity activity) {
        this.activity = activity;

        setDialog();
        findViews();
        fillTextViewsWithData();

        btnOk.setOnClickListener(view -> {
            dismiss();
            //CToast(activity.getApplicationContext(), render("Your photo was saved locally"), Toast.LENGTH_LONG);
        });
    }

    private void fillTextViewsWithData(){
        FishingRecord hvst = recFishing;

        if (!Strings.isEmptyOrWhitespace(hvst.requesterName)){
            tvHarvest.setText(hvst.requesterName);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.cageCode)){
            tvCageName.setText(hvst.cageCode);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.speciesName)){
            tvFishType.setText(hvst.speciesName);
        }

        if (hvst.averageWeight!=null){
            tvFishSize.setText(hvst.averageWeight.toString());
        }

        if (hvst.reqWeight!=null){
            tvRequestedQuantity.setText(hvst.reqWeight.toString());
        }

        if (!Strings.isEmptyOrWhitespace(hvst.packagingPlant)){
            tvPackagingPlant.setText(hvst.packagingPlant);
        }
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
        dialog.setContentView(R.layout.info_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvHarvest = dialog.findViewById(R.id.tvHarvest);
        tvCageName = dialog.findViewById(R.id.tvCageName);
        tvFishType = dialog.findViewById(R.id.tvFishType);
        tvFishSize = dialog.findViewById(R.id.tvFishSize);
        tvRequestedQuantity = dialog.findViewById(R.id.tvRequestedQuantity);
        tvPackagingPlant = dialog.findViewById(R.id.tvPackagingPlant);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }
}
