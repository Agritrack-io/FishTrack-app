package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdENABLE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadData;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdSETUP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.HideProgressBar;
import static io.agritrack.caen.api.CAEN_CONSTANTS.InitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.LastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ShowProgressBar;
import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;
import static io.agritrack.fish.state.GlobalState.recLoggerData;

import android.animation.TimeAnimator;
import android.app.AlertDialog;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;

public class LoggerInitFruitDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {
    public static String TAG = "CaenLoggerDialogFragment";

    // Local handler that receives the RFID scanner results.
    private final CAENCommandsHandler mScanHandler = new CAENCommandsHandler(this);

    private static final int LEVEL_INCREMENT = 500;
    private static final int MAX_LEVEL = 1000;
    private static final String LOGGER_EPC = "loggerEPC";

    private ICAEN_API cmd;
    private String loggerEPC;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private Button btnRead, btnReset, btnSetup, btnInit;

    //####################################################
    //Task to Read Existing Measurements
    final Runnable readValuesThread = new Runnable() {
        @Override
        public void run() {
            try {
                Short samplesCnt = cmd.ReadSamplesCount();
                List<String[]> measurements = cmd.ReadSamples(samplesCnt);
                mScanHandler.sendMessage(createMessage(CmdReadData, measurements));
                mScanHandler.removeCallbacks(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //Task that triggers Logging
    final Runnable enableLoggingThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.EnableLogging();
            Double lastMeasurement = cmd.ReadLastSample();
            mScanHandler.sendMessage(createMessage(CmdENABLE, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    //Task to set Current Date/Time
    final Runnable setupThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR resBinOne = cmd.WriteTimeBinONE();
            Reader.READER_ERR resCurrTS = cmd.WriteCurrentDatetime();
            Reader.READER_ERR resInterval = cmd.WriteInterval(DefaultInterval);
            Reader.READER_ERR response = cmd.WriteCurrentDatetime();
            mScanHandler.sendMessage(createMessage(CmdSETUP, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    //Task that triggers Logging
    final Runnable resetLoggerThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.Reset();
            mScanHandler.sendMessage(createMessage(CmdRESET, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    //####################################################
    protected final View.OnClickListener initBtnListener = v -> {
        btnInit.setBackgroundResource(R.drawable.button_background);
        btnInit.setText("Start Logger...");

        startAnimation(v, btnInit);

        // Initiate Logging thread
        mScanHandler.postDelayed(enableLoggingThread, 50);

    };

    protected final View.OnClickListener setupBtnListener = v -> {

        btnSetup.setBackgroundResource(R.drawable.button_background);
        btnSetup.setText("Setting Up...");

        startAnimation(v, btnSetup);

        // Initiate Setup thread
        mScanHandler.postDelayed(setupThread, 50);
    };

    protected final View.OnClickListener resetBtnListener = v -> {

        btnReset.setBackgroundResource(R.drawable.button_background);
        btnReset.setText("Resetting...");

        startAnimation(v, btnReset);

        // Initiate Logging thread
        mScanHandler.postDelayed(resetLoggerThread, 50);
    };

    private List<String[]> values = null;
    private final View.OnClickListener readBtnListener = v -> {

        btnRead.setBackgroundResource(R.drawable.button_background);
        btnRead.setText("Read Logger...");

        startAnimation(v, btnRead);

        // Initiate Logging thread
        mScanHandler.postDelayed(readValuesThread, 50);
    };

    public LoggerInitFruitDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static LoggerInitFruitDialogFragment newInstance(String epc) {
        LoggerInitFruitDialogFragment frag = new LoggerInitFruitDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, epc);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_fruit_init_logger, container, false);

        btnRead = rootView.findViewById(R.id.btnRead);
        btnReset = rootView.findViewById(R.id.btnReset);
        btnSetup = rootView.findViewById(R.id.btnSetup);
        btnInit = rootView.findViewById(R.id.btnInit);


        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            this.loggerEPC = getArguments().getString(LOGGER_EPC);

            // get UhfReader instance
            cmd = RFIDModuleFactory.getInstance();
            cmd.setFilterEPC(this.loggerEPC);

            // Enable Read button
            btnRead.setText("Reading Measurements...");
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setOnClickListener(readBtnListener);
        }

        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 100;
        getDialog().getWindow().setAttributes(p);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Press First Button
        mScanHandler.postDelayed(readValuesThread, 0);
    }

    private void startAnimation(View view, Button buttonID) {

        mCurrentLevel = 0;

        // Get a handle on the ClipDrawable that we will animate.
        LayerDrawable layerDrawable = (LayerDrawable) buttonID.getBackground();
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
        getActivity().runOnUiThread(() -> {
            mClipDrawable.setLevel(mCurrentLevel);
            if (mCurrentLevel >= MAX_LEVEL) {
                mAnimator.cancel();
            } else {
                mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
            }
        });
    }

    public void animateButton(View view) {
        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }


    // ###################################################
    private class CAENCommandsHandler extends Handler {
        private final WeakReference<LoggerInitFruitDialogFragment> mActivity;

        public CAENCommandsHandler(LoggerInitFruitDialogFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String value = null;
            switch (msg.what) {
                case LastSample:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
//                        tvLastSampleValue.setText(value);
                    } else {
//                        tvLastSampleValue.setText("ERR");
                    }
                    break;
                case InitTimeStamp:
                    value = msg.getData().getString("body");
//                    tvDateTime.setText(value);
                    break;
                case ShowProgressBar:
//                    mActivity.get().getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    break;
                case HideProgressBar:
//                    mActivity.get().getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    break;

                case CmdReadData:
                    LinkedList<String[]> measurements = (LinkedList<String[]>)msg.getData().getSerializable("body");
                    if (measurements != null) {
                        //btnRead.setText("Success");
                        btnRead.setText(String.format("READ %s measurements.", measurements.size()));
                        btnRead.setOnClickListener(null);

                        if (measurements != null) {
                            //displayMeasurementsDialog(measurements);
                            recLoggerData.addDataSet(loggerEPC, System.currentTimeMillis() / 1000L, measurements);
                        }

                        btnReset.setOnClickListener(resetBtnListener);
                        btnReset.callOnClick();
                        //CToast(getActivity(), "Measurements:" + result3.size(), Toast.LENGTH_LONG);
                    } else {
                        btnRead.setText("Failed. Press the button again.");
                    }
                    break;

                case CmdENABLE:
                    //String.format("%.2f\u2103", lastTemperature);
                    value = msg.getData().getString("body");
                    if (Reader.READER_ERR.MT_OK_ERR.name().equals(value)) {
                        btnInit.setText("Success");
                        btnInit.setOnClickListener(null);
                        stopAnimation();
                        getDialog().dismiss();
                    } else {
                        btnInit.setText("Failed");
                    }
                    break;

                case CmdRESET:
                    String result = msg.getData().getString("body");
                    if (Reader.READER_ERR.MT_OK_ERR.name().equals(result)) {
                        btnReset.setText("Success");
                        btnReset.setOnClickListener(null);

                        stopAnimation();
                        // enable setup button
                        btnSetup.setOnClickListener(setupBtnListener);
                        btnSetup.callOnClick();
                    } else {
                        btnReset.setText("Failed");
                    }
                    break;

                case CmdSETUP:
                    String result2 = msg.getData().getString("body");
                    if (Reader.READER_ERR.MT_OK_ERR.name().equals(result2)) {
                        btnSetup.setText("Success");
                        btnSetup.setOnClickListener(null);

                        stopAnimation();

                        btnInit.setOnClickListener(initBtnListener);
                        btnInit.callOnClick();
                    } else {
                        btnSetup.setText("Failed");
                    }
                    break;

                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }


    //--------------------------------------------
    private Message createMessage(int what, Object value) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();

        if (value!=null && value instanceof String)
            b.putString("body", (String)value);
        else if (value!=null && value instanceof Short)
            b.putShort("body", (short)value);
        else if (value!=null && value instanceof Reader.READER_ERR)
            b.putString("body", ((Reader.READER_ERR) value).name());
        else if (value!=null && value instanceof LinkedList)
            b.putSerializable("body", (LinkedList)value);

        msg.setData(b);

        return msg;
    }

    private void displayMeasurementsDialog(List<String[]> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity());
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%04d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }
}
