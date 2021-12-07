package io.agritrack.ui.tools;

import static io.agritrack.caen.api.EncodingUtils.parseTemperature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.hdhe.uhf.reader.UhfReader;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;


public class CaenLoggerDialogFragment extends DialogFragment {

    public static String TAG = "CaenLoggerDialogFragment";

    private static final String LOGGER_EPC = "loggerEPC";
    private String loggerEPC;

    public CaenLoggerDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CaenLoggerDialogFragment newInstance(String epc) {
        CaenLoggerDialogFragment frag = new CaenLoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_caen_logger, container, false);

        if (getArguments() != null) {
            loggerEPC = getArguments().getString(LOGGER_EPC);
        }

//        getDialog().setCanceledOnTouchOutside(false);

        TextView msg = (TextView) rootView.findViewById(R.id.loggerMessage);
        msg.setText("Initializing...");

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getArguments() != null) {
//            //update scanning, uhfReader, tvPlatformName values in thread
//            UhfReader _uhfReader = UhfReader.getInstance();
//            _uhfReader.setWorkArea(3);
//            _uhfReader.setOutputPower(24);
//
//            handleLogger(_uhfReader);
        }
    }

    public void handleLogger(UhfReader _uhfReader) {
        CAENCommander cmd = new CAENCommander(_uhfReader, loggerEPC);
        cmd.HighSensitivity();
        CAENCommander.Response rs = resetLogger(cmd);
        String temp = initializeLogger(cmd);
        cmd.LowSensitivity();

        TextView msg = (TextView) getView().findViewById(R.id.loggerMessage);
        msg.setText("First value: " + temp);
    }

    private CAENCommander.Response resetLogger(CAENCommander cmd) {
        return cmd.RESET();
    }

    private String initializeLogger(CAENCommander cmd) {
        try {
            short lastTemperature = cmd.INIT();
            return parseTemperature(lastTemperature) + "\u2103";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}