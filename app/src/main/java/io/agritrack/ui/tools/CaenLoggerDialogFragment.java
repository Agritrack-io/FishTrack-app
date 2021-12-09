package io.agritrack.ui.tools;

import static io.agritrack.caen.api.EncodingUtils.parseTemperature;

import android.animation.TimeAnimator;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.concurrent.Callable;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;


public class CaenLoggerDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {
    private static final int LEVEL_INCREMENT = 100;
    private static final int MAX_LEVEL = Integer.MAX_VALUE;
    private static final String LOGGER_EPC = "loggerEPC";
    public static String TAG = "CaenLoggerDialogFragment";

    private CAENCommander loggerCommander;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnReset, btnSetup, btnInit;
    private TextView tvTitle;
    private TaskRunner taskRunner;

    protected final View.OnClickListener initBtnListener = v -> {
        btnInit.setBackgroundResource(R.drawable.button_background);

        tvTitle.setText("Start Logging...");
        btnInit.setText("Start Logger...");

        startAnimation(v, btnInit);
        //Task for init button
        Callable<String> enableLoggerTask = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return enableLogger(loggerCommander);
            }
        };

        taskRunner.executeAsync(enableLoggerTask, (rs) -> {
            if (!Strings.isEmptyOrWhitespace(rs)) {
                btnInit.setText("Success");
                btnInit.setOnClickListener(null);

                getDialog().dismiss();
            } else {
                btnInit.setText("Failed");
            }
        });
    };
    protected final View.OnClickListener setupBtnListener = v -> {
        btnSetup.setBackgroundResource(R.drawable.button_background);

        tvTitle.setText("Setting Logger up...");
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
                btnSetup.setText("Success");
                btnSetup.setOnClickListener(null);

                btnInit.setOnClickListener(initBtnListener);
                btnInit.callOnClick();
            } else {
                btnSetup.setText("Failed");
            }
        });
    };

    private final View.OnClickListener resetBtnListener = v -> {
        btnReset.setBackgroundResource(R.drawable.button_background);

        tvTitle.setText("Resetting Logger...");
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

                btnSetup.setOnClickListener(setupBtnListener);
                btnSetup.callOnClick();
            } else {
                btnReset.setText("Failed");
            }
        });
    };

    public CaenLoggerDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        taskRunner = new TaskRunner();
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

//        getDialog().setCanceledOnTouchOutside(false);

        tvTitle = (TextView) rootView.findViewById(R.id.loggerMessage);
        tvTitle.setText("Preparing Logger...");

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

    private CAENCommander.Response setupLogger(CAENCommander cmd) {
        return cmd.SETUP(CAENCommander.DefaultInterval);
    }

    private String enableLogger(CAENCommander cmd) {
        try {
            short lastTemperature = cmd.START_LOGGING();
            return parseTemperature(lastTemperature) + "\u2103";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}