package io.agritrack.ui.tools;

import static io.agritrack.caen.api.EncodingUtils.parseTemperatureNumeric;
import static io.agritrack.caen.api.EncodingUtils.parseTemperatureText;

import android.animation.TimeAnimator;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;
import io.agritrack.fish.ui.bo.LoggerReading;


public class LoggerInitFishDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {
    public static String TAG = "CAENInitFishDialogFragment";

    private static final int LEVEL_INCREMENT = 1000;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";

    private CAENCommander loggerCommander;
    private LoggerReading reading;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnReset, btnSetup, btnInit;
    private TaskRunner taskRunner;

    protected final View.OnClickListener initBtnListener = v -> {

        // draw btnInit background and text
        btnInit.setBackgroundResource(R.drawable.button_background);
        btnInit.setText("Start Logger...");

        startAnimation(v, btnInit);
        //Task for init button
        Callable<Double> enableLoggerTask = new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return enableLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(enableLoggerTask, (rs) -> {
            if (rs!=-99) {
                btnInit.setText("Success");
                btnInit.setOnClickListener(null);
                Map<String, Object> m = new HashMap<>();
                m.put("timestamp", System.currentTimeMillis());
                m.put("LastValue", rs);

                getDialog().dismiss();
                reading.setReading(m);
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

        //CAENCommander.Response rs = setupLogger(loggerCommander);
        Callable<CAENCommander.Response> setUpTask = new Callable<CAENCommander.Response>() {
            @Override
            public CAENCommander.Response call() throws Exception {
                return setupLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(setUpTask, (rs) -> {
            if (rs.succeeded()) {
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

        Callable<CAENCommander.Response> resetTask = new Callable<CAENCommander.Response>() {
            @Override
            public CAENCommander.Response call() throws Exception {
                return resetLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(resetTask, (rs) -> {
            if (rs.succeeded()) {
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

    public LoggerInitFishDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        taskRunner = new TaskRunner();
    }

    public static LoggerInitFishDialogFragment newInstance(String epc) {
        LoggerInitFishDialogFragment frag = new LoggerInitFishDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
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

        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            String loggerEPC = getArguments().getString(LOGGER_EPC);

            // get UhfReader instance
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            _uhfReader.setOutputPower(24);

            loggerCommander = new CAENCommander(_uhfReader, loggerEPC);

            btnReset.setText("Press to Start.");
            btnReset.setOnClickListener(resetBtnListener);
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
        //mClipDrawable.setLevel(MAX_LEVEL);
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

    private CAENCommander.Response resetLogger(CAENCommander cmd) {
        return cmd.RESET();
    }

    private CAENCommander.Response setupLogger(CAENCommander cmd) {
        return cmd.SETUP(CAENCommander.DefaultInterval);
    }

    private Double enableLogger(CAENCommander cmd) {
        try {
            short lastTemperature = cmd.START_LOGGING();
            return parseTemperatureNumeric(lastTemperature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}