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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.fish.ui.bo.LoggerReading;


public class LoggerInitDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {
    public static String TAG = "CAENInitDialogFragment";

    private static final int LEVEL_INCREMENT = 500;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";
    private static final String SHOW_READ_BUTTON = "ShowReadButton";

    private ICAEN_API cmd;
    private LoggerReading reading;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnReset, btnSetup, btnInit, btnRead;
    private TaskRunner taskRunner;

    private List<String[]> values = null;
    private boolean showReadButton = false;


    protected final View.OnClickListener readBtnListener = v -> {

        // draw btnRead background and text
        btnRead.setBackgroundResource(R.drawable.button_background);

        startAnimation(v, btnRead);
        //Task for read button
        Callable<List<String[]>> readLoggerTask = new Callable<List<String[]>>() {
            @Override
            public List<String[]> call() throws Exception {
                return readLogger(cmd);
            }
        };

        taskRunner.executeAsync(readLoggerTask, (rs) -> {
            if (rs != null) {
                btnRead.setText("Success");
                btnRead.setOnClickListener(null);

                Map<String, Object> m = new HashMap<>();
                m.put("timestamp", System.currentTimeMillis());
                m.put("Measurements", values);

                getDialog().dismiss();
                reading.setReading(m);

                displayMeasurementsDialog(values);
                CToast(getActivity(), "Measurements:" + rs.size(), Toast.LENGTH_LONG);
            } else {
                btnRead.setText("Failed. Press the button again.");
            }
        });
    };

    protected final View.OnClickListener initBtnListener = v -> {

        // draw btnInit background and text
        btnInit.setBackgroundResource(R.drawable.button_background);
        btnInit.setText("Start Logger...");

        startAnimation(v, btnInit);
        //Task for init button
        Callable<Double> enableLoggerTask = () -> {
            return enableLogger();
        };

        taskRunner.executeAsync(enableLoggerTask, (rs) -> {
            if (rs != null && rs != -99) {
                btnInit.setText("Success");
                btnInit.setOnClickListener(null);
                Map<String, Object> m = new HashMap<>();
                m.put("timestamp", System.currentTimeMillis());
                m.put("LastValue", rs);

                if(!showReadButton) {
                    getDialog().dismiss();
                    reading.setReading(m);
                }
            } else {
                btnInit.setText("Init:: Failed");
            }
        });
    };

    protected final View.OnClickListener setupBtnListener = v -> {
        // draw btnSetup background and text
        btnSetup.setBackgroundResource(R.drawable.button_background);
        btnSetup.setText("Setting Up...");

        startAnimation(v, btnSetup);

        Callable<Reader.READER_ERR> setUpTask = new Callable<Reader.READER_ERR>() {
            @Override
            public Reader.READER_ERR call() throws Exception {
                return setupLogger();
            }
        };

        taskRunner.executeAsync(setUpTask, (rs) -> {
            if (Reader.READER_ERR.MT_OK_ERR.equals(rs)) {
                btnSetup.setText("Setup:: OK");
                btnSetup.setOnClickListener(null);

                btnInit.setOnClickListener(initBtnListener);
                btnInit.callOnClick();
            } else {
                btnSetup.setText("Setup:: Failed");
            }
        });
    };

    private final View.OnClickListener resetBtnListener = v -> {
        // draw btnReset background and text
        btnReset.setBackgroundResource(R.drawable.button_background);
        btnReset.setText("Resetting...");

        startAnimation(v, btnReset);

        Callable<Reader.READER_ERR> resetTask = () -> resetLogger();

        taskRunner.executeAsync(resetTask, (rs) -> {
            if (Reader.READER_ERR.MT_OK_ERR.equals(rs)) {
                btnReset.setText("Reset:: OK");
                btnReset.setOnClickListener(null);

                stopAnimation();

                btnSetup.setOnClickListener(setupBtnListener);
                btnSetup.callOnClick();
            } else {
                btnReset.setText("Reset:: Failed");
            }
        });
    };

    public LoggerInitDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        taskRunner = new TaskRunner();
    }

    public static LoggerInitDialogFragment newInstance(String epc, boolean showReadButton) {
        LoggerInitDialogFragment frag = new LoggerInitDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
        args.putBoolean(SHOW_READ_BUTTON, showReadButton);
        frag.setArguments(args);

        return frag;
    }

    public void startLoggerPreparation() {
        btnReset.callOnClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        btnReset.callOnClick();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_fish_init_logger, container, false);

        btnReset = rootView.findViewById(R.id.btnReset);
        btnSetup = rootView.findViewById(R.id.btnSetup);
        btnInit = rootView.findViewById(R.id.btnInit);
        btnRead = rootView.findViewById(R.id.btnRead);


        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            showReadButton = getArguments().getBoolean(SHOW_READ_BUTTON);
            String loggerEPC = getArguments().getString(LOGGER_EPC);

            cmd = RFIDModuleFactory.getInstance();
            cmd.setFilterEPC(loggerEPC);

            btnReset.setText("Press to Start.");
            btnReset.setOnClickListener(resetBtnListener);
            if (showReadButton) {
                btnRead.setVisibility(View.VISIBLE);
                btnRead.setText("Press to Read data.");
                btnRead.setOnClickListener(readBtnListener);
            }
        }

        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        getDialog().getWindow().setAttributes(p);


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reading = new ViewModelProvider(requireActivity()).get(LoggerReading.class);
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
        mCurrentLevel = MAX_LEVEL;
        onTimeUpdate(mAnimator, MAX_LEVEL, LEVEL_INCREMENT);
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

    private Reader.READER_ERR resetLogger() {
        return this.cmd.Reset();
    }

    private Reader.READER_ERR setupLogger() {
        return this.cmd.Setup(ICAEN_API.DefaultInterval);
    }

    private Double enableLogger() {
        try {
            return this.cmd.StartLogging();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String[]> readLogger(ICAEN_API cmd) {
        try {
            cmd.HighSensitivity();
            short cnt = cmd.ReadSamplesCount();
            if (cnt > 0) {
                try {
                    //values = cmd.READ_SAMPLES_WITHOUT_TIMESTAMP(cnt);
                    values = cmd.ReadSamples(cnt);
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