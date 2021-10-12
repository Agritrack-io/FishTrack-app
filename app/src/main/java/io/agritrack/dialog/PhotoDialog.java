package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class PhotoDialog {

    private TextView tvTitle;
    private ImageView ivPhoto;
    private Button btnOk, btnCancel;
    private MutableLiveData<Bitmap> liveItem;
    private final Activity activity;
    private Dialog dialog;

    public PhotoDialog (Activity activity, MutableLiveData<Bitmap> selection, @StringRes int title) {
        this.activity = activity;
        this.liveItem = selection;

        setDialog();
        findViews();
        SetCaptions(title);

        ivPhoto.setImageBitmap(liveItem.getValue());
        ivPhoto.setVisibility(View.VISIBLE);

        btnCancel.setOnClickListener(view -> {
            dismiss();
            CToast(activity.getApplicationContext(), render("Your photo wasn't saved"), Toast.LENGTH_LONG);
        });

        btnOk.setOnClickListener(view -> {
            dismiss();
            CToast(activity.getApplicationContext(), render("Your photo was saved locally"), Toast.LENGTH_LONG);
        });
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
        dialog.setContentView(R.layout.photo_dialog);
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        ivPhoto = (ImageView) dialog.findViewById(R.id.ivPhoto);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
        btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
