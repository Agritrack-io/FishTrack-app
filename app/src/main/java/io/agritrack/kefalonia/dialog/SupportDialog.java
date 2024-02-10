package io.agritrack.kefalonia.dialog;

import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.concurrent.Executors;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.common.EmailService;

public class SupportDialog {

    private final Activity activity;
    private final int minTelephoneDigits = 10;
    private Dialog dialog;
    private Button btnSubmit;
    private ImageView ivCancel;
    private EditText mtvRemarks, etName, etPhone;

    public SupportDialog(Activity activity) {
        this.activity = activity;

        setDialog();
        findViews();

        btnSubmit.setOnClickListener(view -> {

            // check if telephone is empty or invalid
            if (Strings.isEmptyOrWhitespace(etPhone.getText().toString()) || etPhone.getText().toString().length() < minTelephoneDigits) {
                CToast(activity.getApplicationContext(), render(activity.getString(R.string.invalid_telephone)), Toast.LENGTH_LONG);
                return;
            }

            // prevent spam of send button
            btnSubmit.setOnClickListener(null);

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // try send email
                    EmailService.sendEmail(etName.getText().toString(), etPhone.getText().toString(), mtvRemarks.getText().toString());
                    this.activity.runOnUiThread(() -> CToast(activity.getApplicationContext(), render(activity.getString(R.string.request_sent)), Toast.LENGTH_LONG));
                } catch (Exception e) {
                    this.activity.runOnUiThread(() -> CToast(activity.getApplicationContext(), render(activity.getString(R.string.request_not_send)), Toast.LENGTH_LONG));
                }

                this.dismiss();
            });
        });

        ivCancel.setOnClickListener(view -> dismiss());
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
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        etName = dialog.findViewById(R.id.etName);
        etPhone = dialog.findViewById(R.id.etPhone);
        ivCancel = dialog.findViewById(R.id.ivCancel);
        mtvRemarks = dialog.findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }
}