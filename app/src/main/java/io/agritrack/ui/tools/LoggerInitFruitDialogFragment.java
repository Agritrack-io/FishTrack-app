package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdENABLE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdReadData;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdSETUP;
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
    private ClipDrawable mClipDrawable;

    //####################################################
    protected final View.OnClickListener initBtnListener = v -> {
        // Send message to Enable Logging
        mScanHandler.sendMessage(createMessage(CmdENABLE, null));
    };
    protected final View.OnClickListener setupBtnListener = v -> {
        // Send message to Start Setup sequence
        mScanHandler.sendMessage(createMessage(CmdSETUP, null));
    };

    protected final View.OnClickListener resetBtnListener = v -> {
        // Send message to Start Reading sequence
        mScanHandler.sendMessage(createMessage(CmdRESET, null));
    };

    private final View.OnClickListener readBtnListener = v -> {
        // Send message to Start Resetting sequence
        mScanHandler.sendMessage(createMessage(CmdReadData, null));
    };
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
        if (!Strings.isEmptyOrWhitespace(this.loggerEPC)) {
            cmd.setFilterEPC(this.loggerEPC);
        }

        // Press First Button
        mScanHandler.sendMessage(createMessage(CmdReadData, null));
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
                    //...setup Read Button............
                    mActivity.get().getActivity().runOnUiThread(() -> {
                        btnRead.setBackgroundResource(R.drawable.button_background);
                        btnRead.setText("Read Logger...");
                        startAnimation(getView(), btnRead);
                    });
                    //-----------------------------------------------------------------------------
                    try {
                        cmd.HighSensitivity();
                        String initTime = cmd.ReadInitDatetime();
                        Short samplesCnt = cmd.ReadSamplesCount();
                        if (samplesCnt < 0) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText(String.format("Invalid measurements count."));
                                stopAnimation();
                            });
                            return;
                        } else if(samplesCnt == 0) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText(String.format("No measurements found."));
                                btnRead.setOnClickListener(null);
                                stopAnimation();
                            });
                            btnReset.setOnClickListener(resetBtnListener);
                            btnReset.callOnClick();
                            return;
                        }

                        List<String[]> measurements = cmd.ReadSamples(samplesCnt);
                        if (measurements != null) {
                            mActivity.get().getActivity().runOnUiThread(() -> {
                                btnRead.setText(String.format("READ %s measurements.", measurements.size()));
                                btnRead.setOnClickListener(null);
                                stopAnimation();
                            });

                            if (measurements != null) {
                                //displayMeasurementsDialog(measurements);
                                recLoggerData.addDataSet(loggerEPC, System.currentTimeMillis() / 1000L, initTime, measurements);
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

                case CmdRESET:
                    //...setup Reset Button............
                    mActivity.get().getActivity().runOnUiThread(() -> {
                        btnReset.setBackgroundResource(R.drawable.button_background);
                        btnReset.setText("Resetting...");
                    });
                    startAnimation(getView(), btnReset);
                    //-----------------------------------------------------------------------------
                    Reader.READER_ERR response = cmd.Reset();

                    if (Reader.READER_ERR.MT_OK_ERR.equals(response)) {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnReset.setText("Reset:: Success");
                            stopAnimation();
                        });
                        btnReset.setOnClickListener(null);
                        // enable setup button
                        btnSetup.setOnClickListener(setupBtnListener);
                        btnSetup.callOnClick();
                    } else {
                        mActivity.get().getActivity().runOnUiThread(() -> {
                            btnReset.setText("Reset:: Failed");
                            stopAnimation();
                        });
                    }
                    break;

                case CmdSETUP:
                    //...setup Setup Button............
                    mActivity.get().getActivity().runOnUiThread(() -> {
                        btnSetup.setBackgroundResource(R.drawable.button_background);
                        btnSetup.setText("Setting Up...");
                        startAnimation(getView(), btnSetup);
                    });
                    //-----------------------------------------------------------------------------
                    // Initiate Setup (Zero time bin, default interval, current timestamp)
                    Reader.READER_ERR resBinZero = cmd.WriteTimeBinZERO();
                    Reader.READER_ERR resInterval = cmd.WriteInterval(DefaultInterval);
                    Reader.READER_ERR resDateTime = cmd.WriteCurrentDatetime();

                    if (Reader.READER_ERR.MT_OK_ERR.equals(resBinZero) && Reader.READER_ERR.MT_OK_ERR.equals(resInterval) && Reader.READER_ERR.MT_OK_ERR.equals(resDateTime)) {
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
                    break;

                case CmdENABLE:
                    //...setup Init Button............
                    mActivity.get().getActivity().runOnUiThread(() -> {
                        btnInit.setBackgroundResource(R.drawable.button_background);
                        btnInit.setText("Start Logger...");
                        startAnimation(getView(), btnInit);
                    });
                    //-----------------------------------------------------------------------------
                    // Initiate Logging
                    Reader.READER_ERR resEnable = cmd.EnableLogging();
                    Double lastMeasurement = cmd.ReadLastSample();

                    //String.format("%.2f\u2103", lastTemperature);
                    if (Reader.READER_ERR.MT_OK_ERR.equals(resEnable)) {
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
                    break;

                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}
