package io.agritrack.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.fragment.app.DialogFragment;

import java.util.function.Function;

import io.agritrack.R;

public class YesNoDialogFragment extends DialogFragment {
    private Bundle args = new Bundle();
    private CharSequence msg;
    private ConfirmationDialogCommand confirmationCmd;
    private ConfirmationDialogCommand rejectionCmd;

    public static YesNoDialogFragment instance() {
        YesNoDialogFragment fragment = new YesNoDialogFragment();
        return fragment;
    }

    public Bundle args() {
        return this.args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        setArguments(this.args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));

        //mBuilder.setTitle(title);
        this.msg = (this.msg == null) ? getText(R.string.confirm_selection) : this.msg;
        mBuilder.setMessage(this.msg);
        mBuilder.setPositiveButton("Yes", (dialog, which) -> {
            if (confirmationCmd != null) {
                confirmationCmd.execute(args);
            }
        });

        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    if (rejectionCmd != null) {
                        rejectionCmd.execute(args);
                    }
                    dialog.dismiss();
                }
            }
        });

        return mBuilder.create();
    }

    public void setMessage(CharSequence text) {
        this.msg = text;
    }

    public void onConfirm(ConfirmationDialogCommand cmd) {
        this.confirmationCmd = cmd;
    }

    public void onReject(ConfirmationDialogCommand cmd) {
        this.rejectionCmd = cmd;
    }
}
