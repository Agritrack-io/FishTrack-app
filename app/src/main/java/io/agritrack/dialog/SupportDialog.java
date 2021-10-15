package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class SupportDialog {

    private final Activity activity;
    private Dialog dialog;
    private Button btnSubmit, btnUrgentRequest;
    private ImageView ivCancel;
    private EditText mtvRemarks;

    public SupportDialog (Activity activity) {
        this.activity = activity;

        setDialog();
        findViews();

        btnUrgentRequest.setOnClickListener(view -> {
            dismiss();
            CToast(activity.getApplicationContext(), render("Your request was sent. Please wait for remote control."), Toast.LENGTH_SHORT);
        });

        btnSubmit.setOnClickListener(view -> {
            dismiss();
                CToast(activity.getApplicationContext(), render("Your request was sent. We will call you as soon as possible."), Toast.LENGTH_SHORT);
        });

        ivCancel.setOnClickListener(view -> {
            dismiss();
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
        dialog.setContentView(R.layout.support_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        btnSubmit = (Button) dialog.findViewById(R.id.btnSubmit);
        btnUrgentRequest = (Button) dialog.findViewById(R.id.btnUrgentRequest);
        ivCancel = dialog.findViewById(R.id.ivCancel);
        mtvRemarks = dialog.findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }
}
