package io.agritrack.fishtrack.ui.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class CustomInfoDialog extends CountDownTimer {
    private final AlertDialog alertDlg;

    public CustomInfoDialog(long millisInFuture, long countDownInterval, @StringRes int msg) {
        super(millisInFuture, countDownInterval);
        this.alertDlg = createDialog(msg);
        this.alertDlg.show();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        // If there is something to be executed peridically, put it here.
    }

    @Override
    public void onFinish() {
        this.alertDlg.dismiss();
    }

    private AlertDialog createDialog(@StringRes int msg) {
        // instantiate an AlertDialog with countdown functionality
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(null);

        LayoutInflater inflater = (LayoutInflater) getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_indicator, null);
        TextView tvProgressMessage = dialogView.findViewById(R.id.progressMsg);
        tvProgressMessage.setTextSize(24.0f);
        tvProgressMessage.setText(msg);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setCancelable(false);

        return dlgBuilder.create();
    }
}