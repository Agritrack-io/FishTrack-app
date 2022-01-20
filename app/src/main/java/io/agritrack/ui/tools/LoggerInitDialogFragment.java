package io.agritrack.ui.tools;

import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.rfid.RFIDUtils.WaitFor;

import android.animation.TimeAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.fish.ui.bo.LoggerReading;


public class LoggerInitDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {

    private static final String SHOW_READ_BUTTON = "ShowReadButton";
    private static final String SHOW_INIT_BUTTON = "ShowInitButton";
    private static final String SHOW_RESET_BUTTON = "ShowResetButton";
    private static final int LEVEL_INCREMENT = 200;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";
    public static String TAG = "CAENInitDialogFragment";
    private ICAEN_API cmd;
    private LoggerReading reading;

    private ExecutorService tasksExecutor = Executors.newSingleThreadExecutor();
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnReset, btnSetup, btnInit, btnRead;
    private List<String[]> values = null;
    private boolean showReadButton = false;
    private boolean showInitButton = false;
    private boolean showResetButton = false;
    private String loggerEPC;


    protected final Runnable initRunnable = ((Runnable) () -> {
        Double enableRS = null;
        try {
            enableRS = this.cmd.StartLogging();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (enableRS != null && enableRS != -99) {
            btnInit.setText("Success");
            btnInit.setOnClickListener(null);
            Map<String, Object> m = new HashMap<>();
            m.put("timestamp", System.currentTimeMillis());
            m.put("EPC", loggerEPC);
            m.put("LastValue", enableRS);

            Dialog mDialog = getDialog();
            if (!showReadButton) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                reading.setReading(m);
            }
        } else {
            btnInit.setText("Init:: Failed");
        }
    });

    protected final View.OnClickListener initBtnListener = v -> {
        // draw btnInit background and text
        btnInit.setBackgroundResource(R.drawable.button_background);
        btnInit.setText("Start Logger...");

        startAnimation(v, btnInit);

        //Task for init button
        // execute the init task
        tasksExecutor.execute(initRunnable);
    };


    protected final Runnable setupRunnable = ((Runnable) () -> {
        Reader.READER_ERR setupRS = this.cmd.Setup(ICAEN_API.DefaultInterval);
        if (Reader.READER_ERR.MT_OK_ERR.equals(setupRS)) {
            btnSetup.setText("Setup:: OK");
            btnSetup.setOnClickListener(null);

            btnInit.setOnClickListener(initBtnListener);
            WaitFor(500l);
            btnInit.callOnClick();
        } else {
            btnSetup.setText("Setup:: Failed");
        }
    });

    protected final View.OnClickListener setupBtnListener = v -> {
        // draw btnSetup background and text
        btnSetup.setBackgroundResource(R.drawable.button_background);
        btnSetup.setText("Setting Up...");

        startAnimation(v, btnSetup);

        // execute the setup task
        tasksExecutor.execute(setupRunnable);
    };


    protected final Runnable resetRunnable = ((Runnable) () -> {
        Reader.READER_ERR resetRS = this.cmd.Reset();
        if (Reader.READER_ERR.MT_OK_ERR.equals(resetRS)) {
            btnReset.setText("Reset:: OK");
            btnReset.setOnClickListener(null);

            stopAnimation();

            btnSetup.setOnClickListener(setupBtnListener);
            WaitFor(300l);
            btnSetup.callOnClick();
        } else {
            btnReset.setText("Reset:: Failed");
        }
    });

    protected final View.OnClickListener resetBtnListener = v -> {
        // draw btnReset background and text
        btnReset.setBackgroundResource(R.drawable.button_background);
        btnReset.setText("Resetting...");

        startAnimation(v, btnReset);

        tasksExecutor.execute(resetRunnable);
    };


    protected final Runnable readRunnable = ((Runnable) () -> {
        List<String[]> readRS = null;
        try {
            cmd.HighSensitivity();
            Short cnt = cmd.ReadSamplesCount();
            if (cnt != null && cnt > 0) {
                try {
                    //values = cmd.READ_SAMPLES_WITHOUT_TIMESTAMP(cnt);
                    values = cmd.ReadSamples(cnt);
                    recLoggerData.addDataSet(this.loggerEPC, System.currentTimeMillis() / 1000L, values);

                    // display temperatures in popup.
                    displayMeasurementsDialog(values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            readRS = values;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cmd.LowSensitivity();
        }


        if (readRS != null) {
            btnRead.setText("Success");
            btnRead.setOnClickListener(null);

            Map<String, Object> m = new HashMap<>();
            m.put("timestamp", System.currentTimeMillis());
            m.put("EPC", loggerEPC);
            m.put("Measurements", values);

            if (getDialog() != null) {
                getDialog().dismiss();
            }
            reading.setReading(m);

            //displayMeasurementsDialog(values);
            // CToast(getActivity(), "Measurements:" + rs.size(), Toast.LENGTH_LONG);
        } else {
            btnRead.setText("Failed. Press the button again.");
        }
    });

    protected final View.OnClickListener readBtnListener = v -> {

        // draw btnRead background and text
        btnRead.setBackgroundResource(R.drawable.button_background);

        startAnimation(v, btnRead);

        //Task for read button
        tasksExecutor.execute(readRunnable);
    };

    public LoggerInitDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        // taskRunner = new TaskRunner();
    }

    public static LoggerInitDialogFragment newInstance(String epc, boolean showReadButton, boolean showResetButton, boolean showInitButton) {
        LoggerInitDialogFragment frag = new LoggerInitDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
        args.putBoolean(SHOW_READ_BUTTON, showReadButton);
        args.putBoolean(SHOW_INIT_BUTTON, showInitButton);
        args.putBoolean(SHOW_RESET_BUTTON, showResetButton);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        btnReset.callOnClick();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_fish_init_logger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnReset = view.findViewById(R.id.btnReset);
        btnSetup = view.findViewById(R.id.btnSetup);
        btnInit = view.findViewById(R.id.btnInit);
        btnRead = view.findViewById(R.id.btnRead);


        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            showReadButton = getArguments().getBoolean(SHOW_READ_BUTTON);
            showInitButton = getArguments().getBoolean(SHOW_INIT_BUTTON);
            showResetButton = getArguments().getBoolean(SHOW_RESET_BUTTON);
            loggerEPC = getArguments().getString(LOGGER_EPC);

            cmd = RFIDModuleFactory.getInstance();
            cmd.setFilterEPC(this.loggerEPC);

            if (showReadButton) {
                btnRead.setVisibility(View.VISIBLE);
                btnRead.setText("Press to Read data.");
                btnRead.setOnClickListener(readBtnListener);
            } else {
                btnRead.setVisibility(View.GONE);
            }

            if (showResetButton) {
                btnReset.setVisibility(View.VISIBLE);
                btnReset.setText("Press to Start.");
                btnReset.setOnClickListener(resetBtnListener);
            } else {
                btnReset.setVisibility(View.GONE);
            }

            if (showInitButton) {
                btnSetup.setVisibility(View.VISIBLE);
                btnInit.setVisibility(View.VISIBLE);
            } else {
                btnSetup.setVisibility(View.GONE);
                btnInit.setVisibility(View.GONE);
            }
        }

        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        getDialog().getWindow().setAttributes(p);

        reading = new ViewModelProvider(requireActivity()).get(LoggerReading.class);
    }

    @Override
    public void onStop() {
        tasksExecutor.shutdown();
        super.onStop();
    }

    private void startAnimation(View view, Button buttonID) {
        mCurrentLevel = 0;

        // Get a handle on the ClipDrawable that we will animate.
        LayerDrawable layerDrawable = (LayerDrawable) buttonID.getBackground();  //R.id.btnReset
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);

        // Set up TimeAnimator to fire off on button click.
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);

        FragmentActivity mActivity = getActivity();
        if (mActivity!=null) {
            mActivity.runOnUiThread(() -> animateButton(view));
        }
    }

    private void stopAnimation() {
        mCurrentLevel = MAX_LEVEL;
        onTimeUpdate(mAnimator, MAX_LEVEL, LEVEL_INCREMENT);
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mClipDrawable.setLevel(mCurrentLevel);
        if (mCurrentLevel >= MAX_LEVEL) {
            requireActivity().runOnUiThread(() -> mAnimator.cancel());
        } else {
            mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
        }
    }

    public void animateButton(View view) {
        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            requireActivity().runOnUiThread(() -> mAnimator.start());
            //mAnimator.start();
        }
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