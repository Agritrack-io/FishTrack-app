package io.agritrack.fishtrack.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import io.agritrack.fishtrack.R;

public class YesNoDialogFragment extends DialogFragment {
    private Bundle args;
    private CharSequence msg;
    private ConfirmationDialogCommand confirmationCmd;

    public static YesNoDialogFragment newInstance(Bundle args) {
        YesNoDialogFragment fragment = new YesNoDialogFragment();
        // Supply inputs as bundle arguments.
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        this.args = getArguments();
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.hello_world, container, false);
//        View tv = v.findViewById(R.id.text);
//
//        return v;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        //mBuilder.setTitle(title);
        this.msg = (this.msg == null) ? getText(R.string.confirm_selection) : this.msg;
        mBuilder.setMessage(this.msg);
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (confirmationCmd != null) {
                    confirmationCmd.execute(args);
                }
            }
        });

        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
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
}
