package io.agritrack.ui.tools;

import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdINIT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadData;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadSamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdSETUP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdSTOP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBINZero;
import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.rfid.RFIDUtils.WaitFor;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.animation.TimeAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
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
    private LoggerReading reading;

    private boolean showReadButton = false;
    private boolean showInitButton = false;
    private boolean showResetButton = false;


    public enum State {STOP_LOGGER, COUNT_SAMPLES, READ_VALUES, RESET, INIT};
    private static final int RST_BIT = 4, RFU_BIT = 3, LE_BIT = 2, DE_BIT = 1, RFSL_BIT = 0;
    private static final int LEVEL_INCREMENT = 1000;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";
    public static String TAG = "CaenLoggerDialogFragment";

    private Button btnRead, btnReset, btnSetup, btnInit, btnValidate;

    // Local handler that receives the RFID scanner results.
    private final CAENCommandsHandler mScanHandler = new CAENCommandsHandler(this);
    private ICAEN_API cmd;
    private String loggerEPC;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0, resetCnt = 0;
    private Short cntSamples = 0;
    private ClipDrawable mClipDrawable;
    private boolean readyToDismiss = false;
    private LoggerInitFruitDialogFragment.State state = null;

    //##############################################################
    private String renderCTRLStatus(String state) {
        String status = "";

        if(Strings.isEmptyOrWhitespace(state) || state.length() != 5) {
            return state;// "invalid reading!";
        }

        if(state.charAt(RST_BIT)=='1') {
            status += "resetting ";
        }

        if(state.charAt(LE_BIT)=='1') {
            status += "logging ON ";
        }

        if(state.charAt(LE_BIT)=='0') {
            status += "logging OFF ";
        }


        return state; //"".equalsIgnoreCase(status) ? state : status ;
    }

    final Runnable readCTRLThread = new Runnable() {
        @Override
        public void run() {
            // read CONTROL register state
            String ctrlState = cmd.ReadControlRegister();
            // set button text to current CONTROL Register state
            btnValidate.setText(String.format("CTRL: %s", renderCTRLStatus(ctrlState)));

            try {Thread.sleep(500l);} catch(Exception x) {}

            // return to validate
            mScanHandler.post(validateThread);
        }
    };

    final Runnable validateThread = new Runnable() {
        @Override
        public void run() {
            // read CONTROL register state
            String ctrlState = cmd.ReadControlRegister();
            // set button text to current CONTROL Register state
            btnValidate.setText(String.format("CTRL: %s", renderCTRLStatus(ctrlState)));

            if(ctrlState.length() != 5) {
                mScanHandler.sendMessage(createMessage(1900, ctrlState.length()));
                return;
            }

            if(!LoggerInitFruitDialogFragment.State.RESET.equals(state) && ctrlState.charAt(RST_BIT)=='1' ) {
                mScanHandler.post(readCTRLThread);
            }

            if(LoggerInitFruitDialogFragment.State.STOP_LOGGER.equals(state)) {
                if(ctrlState.charAt(LE_BIT)=='0') {
                    mScanHandler.post(readSamplesCntThread);
                } else {
                    mScanHandler.sendMessage(createMessage(CmdSTOP, Reader.READER_ERR.MT_CMD_FAILED_ERR));
                }
            }

            if(LoggerInitFruitDialogFragment.State.RESET.equals(state)) {
                if(ctrlState.charAt(RST_BIT)=='0' ) {
                    mScanHandler.sendMessage(createMessage(CmdRESET, Reader.READER_ERR.MT_OK_ERR));
                    mScanHandler.post(initLoggingThread);
                } else if(++resetCnt < 3) {
                    mScanHandler.post(readCTRLThread);
                } else if(++resetCnt > 3) {
                    mScanHandler.sendMessage(createMessage(CmdRESET, Reader.READER_ERR.MT_CMD_FAILED_ERR));
                }
            }

            // Send message to read CTRL state
            if(LoggerInitFruitDialogFragment.State.INIT.equals(state)) {
                if(ctrlState.charAt(LE_BIT)=='1') {
                    // Send message to Enable Logging
                    mScanHandler.sendMessage(createMessage(CmdINIT, Reader.READER_ERR.MT_OK_ERR));
                    //mScanHandler.sendMessage(createMessage(1980, ctrlState));
                } else{
                    mScanHandler.post(initLoggingThread);
                }
            }
            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    // =============================================================
    final Runnable initLoggingThread = new Runnable() {
        @Override
        public void run() {
            // set current State
            state = LoggerInitFruitDialogFragment.State.INIT;
            // enable logger
            Reader.READER_ERR response = cmd.EnableLogging();

            // validate result
            mScanHandler.post(validateThread);

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    // -------------------------------------------------------------
    protected final View.OnClickListener initBtnListener = v -> {
        //...setup Init Button............
        getActivity().runOnUiThread(() -> {
            btnInit.setBackgroundResource(R.drawable.button_background);
            btnInit.setText("Start Logger...");
            startAnimation(getView(), btnInit);
        });
        // -------------------------------------
        cmd.setFilterEPC(loggerEPC);
        mScanHandler.post(initLoggingThread);
    };
    // =============================================================
    final Runnable setupLoggerThread = new Runnable() {
        @Override
        public void run() {
            // set time Bin to 0, (disable timestamps)
            Reader.READER_ERR resZeroBin = cmd.WriteTimeBinZERO();
            delay(100l);
            mScanHandler.sendMessage(createMessage(WriteTimeBINZero, resZeroBin));

            // set Default Interval
            Reader.READER_ERR resInterval = cmd.WriteInterval(DefaultInterval);
            delay(100l);
            mScanHandler.sendMessage(createMessage(WriteInterval, resInterval));

            // set Init time stamp
//            Reader.READER_ERR resCurrTime = cmd.WriteCurrentDatetime();
//            delay(200l);
//            mScanHandler.sendMessage(createMessage(WriteTimeStamp, resCurrTime));

            boolean success = Reader.READER_ERR.MT_OK_ERR.equals(resZeroBin) && Reader.READER_ERR.MT_OK_ERR.equals(resInterval);
            Reader.READER_ERR outcome = success ? Reader.READER_ERR.MT_OK_ERR : Reader.READER_ERR.MT_CMD_FAILED_ERR;

            // Send message to Start Setup sequence
            mScanHandler.sendMessage(createMessage(CmdSETUP, outcome));

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    // -------------------------------------------------------------
    protected final View.OnClickListener setupBtnListener = v -> {
        FragmentActivity mActivity = getActivity();
        //...setup Setup Button............
        mActivity.runOnUiThread(() -> {
            btnSetup.setBackgroundResource(R.drawable.button_background);
            btnSetup.setText("Setting Up...");
            startAnimation(getView(), btnSetup);
        });
        // -------------------------------------
        cmd.setFilterEPC(loggerEPC);
        mScanHandler.postDelayed(setupLoggerThread, 50l);
    };
    // =============================================================
    final Runnable resetThread = new Runnable() {
        @Override
        public void run() {
            // set current State
            state = LoggerInitFruitDialogFragment.State.RESET;
            // reset logger
            Reader.READER_ERR response = cmd.Reset();
            delay(2500l);

            // validate result
            mScanHandler.post(validateThread);

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    // -------------------------------------------------------------
    protected final View.OnClickListener resetBtnListener = v -> {
        FragmentActivity mActivity = getActivity();
        //...setup Reset Button............
        mActivity.runOnUiThread(() -> {
            btnReset.setBackgroundResource(R.drawable.button_background);
            btnReset.setText("Resetting...");
            startAnimation(getView(), btnReset);
        });

        // -------------------------------------
        cmd.setFilterEPC(loggerEPC);
        mScanHandler.post(resetThread);
    };
    // =============================================================
    final Runnable stopLoggerThread = new Runnable() {
        @Override
        public void run() {
            // in case of read failure, no reason to stop logging again.
            if(LoggerInitFruitDialogFragment.State.COUNT_SAMPLES.equals(state)) {
                // invoke read sample
                mScanHandler.post(readSamplesCntThread);
            }

            resetCnt = 0;              // counts reset retrials
            readyToDismiss = false;    // set var for first time in each repetition
            state = LoggerInitFruitDialogFragment.State.STOP_LOGGER; // set current State
            Reader.READER_ERR resHigh = cmd.HighSensitivity(); // stop logger and HIGH sensitivity

            if(!Reader.READER_ERR.MT_OK_ERR.equals(resHigh)) {
                btnRead.setText(String.format("Stop:: Failed."));
                stopAnimation();
                return;
            }

            // validate result
            mScanHandler.post(validateThread);
            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    final Runnable readSamplesCntThread = new Runnable() {
        @Override
        public void run() {
            // set current State
            state = LoggerInitFruitDialogFragment.State.COUNT_SAMPLES;

            // read number of Measurements
            short cntSamples = cmd.ReadSamplesCount();
            mScanHandler.sendMessage(createMessage(CmdReadSamplesCnt, cntSamples));

            if(cntSamples > 0) {
                mScanHandler.post(readValuesThread);
            }

            // validate result
            mScanHandler.post(validateThread);

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    final Runnable readValuesThread = new Runnable() {
        @Override
        public void run() {
            // set current State
            state = LoggerInitFruitDialogFragment.State.READ_VALUES;

            try {
                // reset existing values
                recLoggerData.clearData();

                // read the measurements from logger based on samples count.
                List<String[]> measurements = cmd.ReadSamples(cntSamples);

                if (measurements != null) {
                    long now = System.currentTimeMillis() / 1000L;
                    recLoggerData.addDataSet(loggerEPC, now, null, measurements);

                    // update buttons based on values read...
                    mScanHandler.sendMessage(createMessage(CmdReadData, (short) measurements.size()));

                    // since values are read, Reset the logger
                    mScanHandler.post(resetThread);
                } else {
                    // update read button text to display failure...
                    mScanHandler.sendMessage(createMessage(CmdReadData, (short) -1));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };
    // -------------------------------------------------------------

    private final View.OnClickListener readBtnListener = v -> {
        FragmentActivity mActivity = getActivity();

        //...setup Read Button............
        mActivity.runOnUiThread(() -> {
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setText("Reading Logger...");
            startAnimation(getView(), btnRead);
        });

        // -------------------------------------
        cmd.setFilterEPC(loggerEPC);

        mScanHandler.post(stopLoggerThread);
        // -------------------------------------
    };
    //##############################################################

    private final List<String[]> values = null;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_fruit_init_logger, container, false);

        btnRead = rootView.findViewById(R.id.btnRead);
        btnReset = rootView.findViewById(R.id.btnReset);
        btnSetup = rootView.findViewById(R.id.btnSetup);
        btnInit = rootView.findViewById(R.id.btnInit);
        btnValidate = rootView.findViewById(R.id.btnValidate);


        if (getArguments() != null && !Strings.isEmptyOrWhitespace(getArguments().getString(LOGGER_EPC))) {
            this.loggerEPC = getArguments().getString(LOGGER_EPC);

            // Enable Read button
            btnRead.setText("Reading Measurements...");
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setOnClickListener(readBtnListener);
        }

        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 130;
        getDialog().getWindow().setAttributes(p);

        return rootView;
    }

   /* @Override
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

            if (showResetButton) {
                btnReset.setVisibility(View.VISIBLE);
                //btnReset.setText("Press to Start.");
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
    }*/


    @Override
    public void onStart() {
        super.onStart();

        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();

        // Press First Button
        btnRead.callOnClick();
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
        FragmentActivity mActivity = getActivity();
        if(getActivity()!=null) {
            getActivity().runOnUiThread(() -> {
                mClipDrawable.setLevel(mCurrentLevel);
                if (mCurrentLevel >= MAX_LEVEL) {
                    mAnimator.cancel();
                } else {
                    mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
                }
            });
        }
    }

    public void animateButton(View view) {
        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }

    //--------------------------------------------
    private Message createMessage(int what, Object value) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();

        if (value != null && value instanceof String)
            b.putString("body", (String) value);
        else if (value != null && value instanceof Short)
            b.putShort("body", (short) value);
        else if (value != null && value instanceof Reader.READER_ERR)
            b.putString("body", ((Reader.READER_ERR) value).name());
        else if (value != null && value instanceof LinkedList)
            b.putSerializable("body", (LinkedList) value);

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

    // ###################################################
    private class CAENCommandsHandler extends Handler {
        private final WeakReference<LoggerInitDialogFragment> mActivity;

        public CAENCommandsHandler(LoggerInitDialogFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CmdReadData:
                    //-----------------------------------------------------------------------------
                    short valuesRead = msg.getData().getShort("body");
                    if (valuesRead > 0) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText(String.format("READ %s measurements.", valuesRead));
                            btnRead.setOnClickListener(null);
                            stopAnimation();
                        });

                        btnReset.setVisibility(View.VISIBLE);
                        btnReset.setOnClickListener(resetBtnListener);
                        btnReset.callOnClick();
                    } else {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText("Failed. Press the button again.");
                            stopAnimation();
                        });
                    }
                    break;

                case CmdReadSamplesCnt:
                    cntSamples = msg.getData().getShort("body");
                    if (cntSamples == null || cntSamples < 0) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText(String.format("Invalid measurements count."));
                            stopAnimation();
                        });
                    } else if(cntSamples == 0) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText(String.format("No measurements found."));
                            stopAnimation();
                        });
                    }

                    //if(cntSamples == null || cntSamples<=0) {
                    if(cntSamples == 0) {
                        btnInit.setOnClickListener(initBtnListener);
                        btnInit.setVisibility(View.VISIBLE);
                        btnInit.setEnabled(true);
                        btnInit.setText("Start Logging.");
                    }
                    break;

                case CmdRESET:
                    String resReset = msg.getData().getString("body");
                    try {
                        //-----------------------------------------------------------------------------
                        if (Reader.READER_ERR.MT_OK_ERR.name().equals(resReset)) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnReset.setText("Reset:: Success");
                                stopAnimation();
                            });
                            // diable Reset Button
                            btnReset.setOnClickListener(null);

                            // enable setup button
                            btnInit.setVisibility(View.VISIBLE);
                            btnInit.setOnClickListener(initBtnListener);
                            btnInit.callOnClick();

                            // close popup
                            //getDialog().dismiss();
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnReset.setText("Reset:: Failed");
                                stopAnimation();
                                mScanHandler.removeCallbacksAndMessages(null);
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case CmdSTOP:
                    String resStop = msg.getData().getString("body");
                    try {
                        //-----------------------------------------------------------------------------
                        if (Reader.READER_ERR.MT_OK_ERR.name().equals(resStop)) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText("Stop:: Success");
                                stopAnimation();
                            });
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText("Stop:: Failed");
                                stopAnimation();
                                mScanHandler.removeCallbacksAndMessages(null);
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case CmdSETUP:
                    String resSetup = msg.getData().getString("body");
                    try {
                        //-----------------------------------------------------------------------------
                        if (Reader.READER_ERR.MT_OK_ERR.name().equals(resSetup)) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnSetup.setText("Setup:: Success");
                                btnSetup.setOnClickListener(null);
                                stopAnimation();
                                btnInit.setOnClickListener(initBtnListener);
                                btnInit.callOnClick();
                            });
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnSetup.setText("Setup:: Failed");
                                stopAnimation();
                                mScanHandler.removeCallbacksAndMessages(null);
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case CmdINIT:
                    String resInit = msg.getData().getString("body");
                    try {
                        if (Reader.READER_ERR.MT_OK_ERR.name().equals(resInit)) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnInit.setText("Init:: Success");
                                stopAnimation();
                                btnInit.setOnClickListener(null);

                                readyToDismiss = true;
                                getDialog().dismiss();
                            });
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnInit.setText("Init:: Failed");
                                stopAnimation();
                                mScanHandler.removeCallbacksAndMessages(null);
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case 1980:
                    if(readyToDismiss) {
                        getDialog().dismiss();
                    }
                    break;
                case 1900:
                    CToast(getActivity(), render("Read invalid values!!\nPlease change your position!"), Toast.LENGTH_SHORT);
                    //getDialog().dismiss();

                    break;
            }
        }
    }

    private void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {}
    }
}