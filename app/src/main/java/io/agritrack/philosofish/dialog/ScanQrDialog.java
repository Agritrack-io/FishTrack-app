package io.agritrack.philosofish.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;

public class ScanQrDialog implements AdapterView.OnItemClickListener {
    private final Activity activity;
    private TextView tvTitle;
    ;
    private ImageView ivQrcode;
    private Button btnOk;
    private Dialog dialog;

    public ScanQrDialog(Activity activity, Bitmap bitmap) {
        this.activity = activity;

        setDialog();
        findViews();
        ivQrcode.setImageBitmap(bitmap);

        btnOk.setOnClickListener(view -> {
            Intent i = new Intent(activity.getApplicationContext(), FishHomeActivity.class);
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
        dialog.setContentView(R.layout.scan_qr_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        ivQrcode = dialog.findViewById(R.id.ivQrcode);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
