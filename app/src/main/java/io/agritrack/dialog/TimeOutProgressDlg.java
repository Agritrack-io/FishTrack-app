package io.agritrack.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import io.agritrack.kefalonia.R;

import static io.agritrack.FishTrackApplication.getAppContext;

public abstract class TimeOutProgressDlg extends CountDownTimer {

    protected final AlertDialog dialog;
    private final TextView tvProgressMessage;

    protected TimeOutProgressDlg(long millisInFuture, long countDownInterval, Context parentActivity) {
        super(millisInFuture, countDownInterval);

        // instantiate an AlertDialog with countdown functionality
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(parentActivity);
        LayoutInflater inflater = (LayoutInflater) getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_indicator, null);
        this.tvProgressMessage = dialogView.findViewById(R.id.progressMsg);
        this.tvProgressMessage.setText(R.string.empty);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setCancelable(false);
        dialog = dlgBuilder.create();
    }

    public void setMessage(@StringRes int resId) {
        this.tvProgressMessage.setTextSize(24.0f);
        this.tvProgressMessage.setText(resId);
    }

    public void hide() {
        try {
            this.cancel();
        } finally {
            dialog.dismiss();
        }
    }

    public void show() {
        dialog.show();
        this.start();
    }


    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        try {
            doTasks();
        } finally {
            dialog.dismiss();
        }
    }

    public abstract void doTasks();
}
