package io.agritrack.ui.tools;

import static io.agritrack.ui.custom.CustomToast.CToast;

import android.animation.TimeAnimator;
import android.app.AlertDialog;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.concurrent.Callable;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;

public class LoggerReadFishDialogFragment extends DialogFragment implements TimeAnimator.TimeListener{
    public static String TAG = "CaenLoggerDialogFragment";

    private static final int LEVEL_INCREMENT = 1000;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";

    private CAENCommander loggerCommander;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnReset, btnRead;
    private TextView tvTitle;
    private TaskRunner taskRunner;

    private List<Double> values = null;






    protected final View.OnClickListener resetBtnListener = v -> {

        btnReset.setBackgroundResource(R.drawable.button_background);
        btnReset.setText("Resetting...");

        startAnimation(v, btnReset);

        Callable<CAENCommander.Response> resetTask = new Callable<CAENCommander.Response>() {
            @Override
            public CAENCommander.Response call() throws Exception {
                return resetLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(resetTask, (rs) -> {
            if (rs.succeeded()) {
                btnReset.setText("Success");
                btnReset.setOnClickListener(null);

                getDialog().dismiss();
            } else {
                btnReset.setText("Failed");
            }
        });
    };

    private final View.OnClickListener readBtnListener = v -> {

        btnRead.setBackgroundResource(R.drawable.button_background);
        btnRead.setText("Read Logger...");

        startAnimation(v, btnRead);
        //Task for read button
        Callable<List<Double>> readLoggerTask = new Callable<List<Double>>() {
            @Override
            public List<Double> call() throws Exception {
                return readLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(readLoggerTask, (rs) -> {
            if (rs != null) {
                btnRead.setText("Success");
                btnRead.setOnClickListener(null);
                getDialog().dismiss();
                displayMeasurementsDialogWithoutTimestamp(values);

                //btnReset.setOnClickListener(resetBtnListener);
                //btnReset.callOnClick();
                CToast(getActivity(), "Measurements:"+ rs.size(), Toast.LENGTH_LONG);
            } else {
                btnRead.setText("Failed. Press the button again.");
            }
        });
    };

    public LoggerReadFishDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        taskRunner = new TaskRunner();
    }

    public static LoggerReadFishDialogFragment newInstance(String epc) {
        LoggerReadFishDialogFragment frag = new LoggerReadFishDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_fish_read_logger, container, false);

        btnRead = rootView.findViewById(R.id.btnRead);
        btnReset = rootView.findViewById(R.id.btnReset);

        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            String loggerEPC = getArguments().getString(LOGGER_EPC);

            // get UhfReader instance
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            _uhfReader.setOutputPower(24);

            loggerCommander = new CAENCommander(_uhfReader, loggerEPC);

            btnRead.setText("Press to Start.");
            btnRead.setOnClickListener(readBtnListener);
        }

        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        getDialog().getWindow().setAttributes(p);

        return rootView;
    }

    private void startAnimation(View view, Button buttonID) {

        mCurrentLevel = 0;

        // Get a handle on the ClipDrawable that we will animate.
        LayerDrawable layerDrawable = (LayerDrawable) buttonID.getBackground();  //R.id.btnReset
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);

        // Set up TimeAnimator to fire off on button click.
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);

        getActivity().runOnUiThread(() -> animateButton(view));
    }

    private void stopAnimation() {
        mAnimator.cancel();
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mClipDrawable.setLevel(mCurrentLevel);
        if (mCurrentLevel >= MAX_LEVEL) {
            mAnimator.cancel();
        } else {
            mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
        }
    }

    public void animateButton(View view) {
        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }

    private CAENCommander.Response resetLogger(CAENCommander cmd) {
        return cmd.RESET();
    }

    private List<Double> readLogger(CAENCommander cmd) {
        //List<String[]> values = null;
        try {
            cmd.HighSensitivity();
            short cnt = cmd.READ_SAMPLES_COUNT();
            if (cnt > 0) {
                try {
                    values = cmd.READ_SAMPLES_WITHOUT_TIMESTAMP(cnt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return values;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cmd.LowSensitivity();
        }
        return values;
    }

    private void displayMeasurementsDialog(List<String[]> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity());
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%3d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }

    private void displayMeasurementsDialogWithoutTimestamp(List<Double> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity());
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.agri_list_item_12dp);

        int idx = 1;
        for (Double value : values) {
            arrayAdapter.add(String.format("%s:/t%.2f", idx++, value));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }
}
