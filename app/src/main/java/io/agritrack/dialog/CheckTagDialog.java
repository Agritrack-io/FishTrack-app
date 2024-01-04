package io.agritrack.dialog;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import io.agritrack.R;
import io.agritrack.fish.ui.wh.zebra.correlation.ZebraExistingTagActivity;

public class CheckTagDialog {
    private final Activity activity;
    private TextView tvTitle;
    private Button btnOk;
    private Dialog dialog;

    @SuppressLint("StringFormatMatches")
    public CheckTagDialog(Activity activity, String rfid, String code) {
        this.activity = activity;

        setDialog();
        findViews();

//        tvTitle.setText(activity.getString(R.string.check_tag_msg, rfid, code));
        tvTitle.setText(Html.fromHtml(getAppContext().getResources().getString(R.string.check_tag_msg_1)
                + getAppContext().getResources().getString(R.string.check_tag_msg_2) + "<b>" + "<font color='#16325c'>"
                + code + "</font>" + "</b>" + getAppContext().getResources().getString(R.string.check_tag_msg_3) + "<b>" + "<font color='#16325c'>" + rfid, HtmlCompat.FROM_HTML_MODE_LEGACY));
        tvTitle.setBackgroundColor(Color.WHITE);
        tvTitle.setPadding(10, 10, 10, 10);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(Color.BLACK);

        btnOk.setOnClickListener(view -> {
            Intent i = new Intent(activity.getApplicationContext(), ZebraExistingTagActivity.class);
            activity.startActivity(i);
            dismiss();
        });
    }

    public void showDialog() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.check_tag_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }
}
