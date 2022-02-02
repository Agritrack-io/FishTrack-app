package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdINIT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadData;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadSamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdSETUP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBINZero;
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
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;

public class LoggerInitFruitDialogFragment extends DialogFragment implements TimeAnimator.TimeListener {
    private static final int LEVEL_INCREMENT = 1000;
    private static final int MAX_LEVEL = 10000;
    private static final String LOGGER_EPC = "loggerEPC";
    public static String TAG = "CaenLoggerDialogFragment";

    private Button btnRead, btnReset, btnSetup, btnInit;

    // Local handler that receives the RFID scanner results.
    private final CAENCommandsHandler mScanHandler = new CAENCommandsHandler(this);
    private ICAEN_API cmd;
    private String loggerEPC;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private Short cntSamples = 0;
    private ClipDrawable mClipDrawable;

    //##############################################################
    final Runnable initLoggingThread = new Runnable() {
        @Override
        public void run() {
            //...Increase to MAX Sensitivity............
            cmd.HighSensitivity();
            // enable logger
            Reader.READER_ERR response = cmd.EnableLogging();
            // Send message to Enable Logging
            mScanHandler.sendMessage(createMessage(CmdINIT, response));
            delay(2000l);
            // revert to LOW sensitivity
            cmd.LowSensitivity();
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
        mScanHandler.postDelayed(initLoggingThread, 50l);
        // -------------------------------------
    };
    // =============================================================
    final Runnable setupLoggerThread = new Runnable() {
        @Override
        public void run() {
            //...Increase to MAX Sensitivity............
            cmd.HighSensitivity();

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

            // revert to LOW sensitivity
            cmd.LowSensitivity();

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
        // -------------------------------------
    };
    // =============================================================
    final Runnable resetThread = new Runnable() {
        @Override
        public void run() {
            //...Increase to MAX Sensitivity............
            cmd.HighSensitivity();

            // reset logger
            Reader.READER_ERR response = cmd.Reset();
            delay(2500l);
            mScanHandler.sendMessage(createMessage(CmdRESET, response));

            // revert to LOW sensitivity
            cmd.LowSensitivity();

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
        mScanHandler.postDelayed(resetThread, 50l);
        // -------------------------------------
    };
    // =============================================================
    final Runnable readSamplesCntThread = new Runnable() {
        @Override
        public void run() {
            //...Increase to MAX Sensitivity............
            cmd.HighSensitivity();

            // disable Logging...
            Reader.READER_ERR resDisable = cmd.DisableLogging();
            delay(200l);

            // reset logger
            short cntSamples = cmd.ReadSamplesCount();
            delay(200l);
            mScanHandler.sendMessage(createMessage(CmdReadSamplesCnt, cntSamples));

            // remove any pending message
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readThread = new Runnable() {
        @Override
        public void run() {
            // reset logger
            Reader.READER_ERR response = cmd.Reset();
            delay(2500l);
            mScanHandler.sendMessage(createMessage(CmdReadData, response));

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
            btnRead.setText("Read Logger...");
            startAnimation(getView(), btnRead);
        });

        // -------------------------------------
        cmd.setFilterEPC(loggerEPC);
        mScanHandler.post(readSamplesCntThread);
        // -------------------------------------

        mScanHandler.postDelayed(readThread, 150l);
        //-----------------------------------------------------------------------------

        // Send message to Start Resetting sequence
//        mScanHandler.sendMessage(createMessage(CmdReadData, null));
    };
    //##############################################################

    private final List<String[]> values = null;

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
        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();

        // Press First Button
        btnRead.callOnClick();
        //mScanHandler.sendMessage(createMessage(CmdReadData, null));
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
        private final WeakReference<LoggerInitFruitDialogFragment> mActivity;

        public CAENCommandsHandler(LoggerInitFruitDialogFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CmdReadData:
                    //-----------------------------------------------------------------------------
                    try {
                        List<String[]> measurements = cmd.ReadSamples(cntSamples);
                        if (measurements != null) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText(String.format("READ %s measurements.", measurements.size()));
                                btnRead.setOnClickListener(null);
                                stopAnimation();
                            });

                            if (measurements != null) {
                                long now = System.currentTimeMillis() / 1000L;
                                //displayMeasurementsDialog(measurements);
                                recLoggerData.addDataSet(loggerEPC, now, null, measurements);
                            }
                            btnReset.setOnClickListener(resetBtnListener);
                            btnReset.callOnClick();
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText("Failed. Press the button again.");
                                stopAnimation();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CmdReadSamplesCnt:
                    //String initTime = cmd.ReadInitDatetime();
                    cntSamples = msg.getData().getShort("body");
                    if (cntSamples == null || cntSamples < 0) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText(String.format("Invalid measurements count."));
                            stopAnimation();
                        });
                        return;
                    } else if(cntSamples == 0) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnRead.setText(String.format("No measurements found."));
                            btnRead.setOnClickListener(null);
                            stopAnimation();
                        });
                        btnReset.setOnClickListener(resetBtnListener);
                        btnReset.callOnClick();
                        return;
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
                            btnReset.setOnClickListener(null);

                            // enable setup button
                            //btnSetup.setOnClickListener(setupBtnListener);
                            //btnSetup.callOnClick();

                            // enable setup button
                            btnReset.setOnClickListener(null);
                            getDialog().dismiss();
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnReset.setText("Reset:: Failed");
                                stopAnimation();
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
                            });
                            btnInit.setOnClickListener(initBtnListener);
                            btnInit.callOnClick();
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnSetup.setText("Setup:: Failed");
                                stopAnimation();
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case CmdINIT:
                    String resInit = msg.getData().getString("body");
                    try {
                        //String.format("%.2f\u2103", lastTemperature);
                        if (Reader.READER_ERR.MT_OK_ERR.name().equals(resInit)) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnInit.setText("Init:: Success");
                                stopAnimation();
                            });
                            btnInit.setOnClickListener(null);
                            getDialog().dismiss();
                        } else {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnInit.setText("Init:: Failed");
                                stopAnimation();
                            });
                        }
                    } catch(Exception e) { }
                    break;

                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
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
