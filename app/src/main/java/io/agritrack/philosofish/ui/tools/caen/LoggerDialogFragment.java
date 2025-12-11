package io.agritrack.philosofish.ui.tools.caen;

import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.InitSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ResetSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ValidSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.ICAEN_API.DefaultInterval;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.InitOp;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.LEVEL_INCREMENT;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.MAX_LEVEL;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.ReadOp;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.ResetOp;
import static io.agritrack.philosofish.ui.tools.caen.LoggerDialogDecorator.ValidOp;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.caen.api.CAENLoggerService;
import io.agritrack.philosofish.caen.api.ICAEN_API;
import io.agritrack.philosofish.caen.api.RFIDModuleFactory;
import io.agritrack.philosofish.caen.common.CAENState;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;

/**
 * Component implementation of ILoggerDialog.
 * Use the {@link LoggerDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p>
 * Part of a Decorator Design Pattern implementation to allow for
 * different Logger Dialogs creation at runtime.
 */
public class LoggerDialogFragment extends DialogFragment implements TimeAnimator.TimeListener, ILoggerDialog {
    private static Long initTS;
    // Single Thread
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    boolean toExit = false;
    // Local handler that receives the RFID scanner results.
    private Handler mScanHandler;
    private Context mContext;
    private Button btnRead, btnInit, btnReset, btnValidate;
    private TextView tvCurrentLoggerEPC;
    private int buttonVisibilityBits = 0x00000;
    private String loggerEPC;
    private String assetEPC;
    private String currentLoggerEPC = null;
    private String productionLane;
    private Long pickedAt;
    private Long initedAt;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;
    private CAENLoggerService loggerSvc;
    private ICAEN_API cmd;
    protected BroadcastReceiver keyReceiver = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getDialog().setCanceledOnTouchOutside(true);
//        keyReceiver = new X9KeyReceiver(this::onClick);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.rfid.FUN_KEY");
//        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
//        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        // ((InitBinsActivity)getActivity()).registerKeyReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int numArgs = this.getArguments().size();
//        if (numArgs == 2) {
//            ((InitBinsActivity)getActivity()).registerKeyReceiver();
//        }
        if (keyReceiver != null) {
            getActivity().unregisterReceiver(keyReceiver);
            keyReceiver = null;
        }
//        if (keyReceiver != null){
//            getActivity().unregisterReceiver(keyReceiver);
//        }
    }

    //##############################################################
    protected final View.OnClickListener validBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            btnValidate.setBackgroundResource(R.drawable.button_background);
            btnValidate.setText(R.string.validate);
            startAnimation(getView(), btnValidate);

            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke reset() method of CAENLoggerService.
            getDialog().setCanceledOnTouchOutside(false);
            setCancelable(false);
            //v.setEnabled(false);
            if (!executorService.isShutdown()) {
                executorService.execute(() -> {
                    if (this != null && this.getActivity() != null && v != null) {
                        this.getActivity().runOnUiThread(() -> setCancelable(false));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(false));
                        //this.loggerSvc.doValidation();
                        this.getActivity().runOnUiThread(() -> setCancelable(true));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(true));
                    }
                    this.loggerSvc.doValidation();
                });
            }
        } else {
            CToast(getActivity(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
        }
    };

    protected final View.OnClickListener resetBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            btnReset.setBackgroundResource(R.drawable.button_background);
            btnReset.setText(R.string.reset);
            startAnimation(getView(), btnReset);

            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke reset() method of CAENLoggerService.
            getDialog().setCanceledOnTouchOutside(false);
            setCancelable(false);
            if (!executorService.isShutdown()) {
                executorService.execute(() -> {
                    if (this != null && this.getActivity() != null && v != null) {
                        this.getActivity().runOnUiThread(() -> setCancelable(false));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(false));
//                        this.loggerSvc.doResetLogger();
                        this.getActivity().runOnUiThread(() -> setCancelable(true));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(true));
                    }
                    this.loggerSvc.doResetLogger();
                });
            }

        } else {
            CToast(getActivity(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
        }
    };

    private final View.OnClickListener readBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setText(R.string.reading);
            startAnimation(getView(), btnRead);
            getDialog().setCanceledOnTouchOutside(false);
            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            currentLoggerEPC = loggerEPC != null ? loggerEPC : currentLoggerEPC;
            //invoke read() method of CAENLoggerService.
            setCancelable(false);
            //v.setEnabled(false);
            if (!executorService.isShutdown()) {
                executorService.execute(() -> {
                    if (this != null && this.getActivity() != null && v != null) {
                        this.getActivity().runOnUiThread(() -> setCancelable(false));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(false));
                        //this.loggerSvc.doReadMeasurements(currentLoggerEPC != null);
                        this.getActivity().runOnUiThread(() -> setCancelable(true));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(true));
                    }
                    this.loggerSvc.doReadMeasurements(currentLoggerEPC != null);
                });
            }

        } else {
            CToast(getActivity(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
        }
    };

    //##############################################################
    //-------
    private MutableLiveData<CAENState> stateResult;
    //##############################################################
    private final short samplingInterval = DefaultInterval;
    protected final View.OnClickListener initBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            btnInit.setBackgroundResource(R.drawable.button_background);
            btnInit.setText(R.string.starting_logger);
            startAnimation(getView(), btnInit);
            getDialog().setCanceledOnTouchOutside(false);
            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke reset() method of CAENLoggerService.
            setCancelable(false);
            //v.setEnabled(false);
            if (!executorService.isShutdown()) {
                executorService.execute(() -> {
                    if (this != null && this.getActivity() != null && v != null) {
                        this.getActivity().runOnUiThread(() -> setCancelable(false));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(false));
                        //this.loggerSvc.doEnableLogger(samplingInterval);
                        this.getActivity().runOnUiThread(() -> setCancelable(true));
                        this.getActivity().runOnUiThread(() -> v.setEnabled(true));
                    }
                    this.loggerSvc.doEnableLogger(samplingInterval);

                });
            }
        } else {
            CToast(getActivity(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
        }
    };
    //##############################################################


    private LoggerDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
        //getDialog().setCanceledOnTouchOutside(true);
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC) {
        LoggerDialogFragment loggerDlgFragment = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        loggerDlgFragment.setArguments(args);

        return loggerDlgFragment;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, Long initedAt) {
        LoggerDialogFragment loggerDlgFragment = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putLong(INITED_AT, initedAt);
        loggerDlgFragment.setArguments(args);

        return loggerDlgFragment;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, String productionLane) {
        LoggerDialogFragment loggerDlgFragment = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putString(PROD_LANE, productionLane);
        loggerDlgFragment.setArguments(args);

        return loggerDlgFragment;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, String productionLane, Long initializedAt, Long pickedAt) {
        LoggerDialogFragment loggerDlgFragment = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putString(PROD_LANE, productionLane);
        args.putLong(INITED_AT, initializedAt);
        // args.putLong() does not accept null values.
        if (pickedAt != null) {
            args.putLong(PICKED_AT, pickedAt);
        }
        loggerDlgFragment.setArguments(args);

        return loggerDlgFragment;
    }

    @Override
    public void show(FragmentManager fm) {
        this.show(fm, ILoggerDialog.TAG);
    }

    @Override
    public void setButtonsVisibility(int buttonBits) {
        this.buttonVisibilityBits = buttonBits;
    }

    @Override
    public void setStateObserver(MutableLiveData<CAENState> stateResult) {
        this.stateResult = stateResult;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logger_dialog, container, false);

        // assign global variable to button objects.
        assignCtrlVars(rootView);

        // initiate raw sound
        SoundUtil.initSoundPool(mContext);

        // show dialog at bottom-center of current screen.
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 130;
        getDialog().getWindow().setAttributes(p);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // instantiate local handler to manipulate button animation effects.
        getDialog().setCanceledOnTouchOutside(true);
        this.mScanHandler = new CAENCommandsHandler();

        if (getArguments() != null) {
            this.loggerEPC = getArguments().getString(LOGGER_EPC);
            this.assetEPC = getArguments().getString(ASSET_EPC);
            this.initedAt = getArguments().getLong(INITED_AT);
            this.productionLane = getArguments().getString(PROD_LANE);
            this.pickedAt = getArguments().getLong(PICKED_AT);

            // show/hide buttons according to Visibility Bit values.
            applyButtonsVisibility();

            if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                tvCurrentLoggerEPC.setText(loggerEPC.substring(loggerEPC.length() - 10));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
        this.loggerSvc = new CAENLoggerService(this.cmd, this.mScanHandler, this.pickedAt);

        // Press First Button
        if ((ReadOp & this.buttonVisibilityBits) == ReadOp) {
            btnRead.setText(R.string.reading_measurements);
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setOnClickListener(readBtnListener);
            btnRead.callOnClick();
        } else if ((InitOp & this.buttonVisibilityBits) == InitOp) {
            btnReset.setOnClickListener(resetBtnListener);
            btnReset.callOnClick();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        loggerSvc.shutdownExecutorService();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof IDialogCloseListener)
            ((IDialogCloseListener) activity).handleDialogClose(dialog);
    }

    // ------------------------
    // ---  Private Methods ---
    // ------------------------
    private void applyButtonsVisibility() {
        if ((this.buttonVisibilityBits & ReadOp) == ReadOp) {
            btnRead.setVisibility(View.VISIBLE);
        }

        if ((this.buttonVisibilityBits & ResetOp) == ResetOp) {
            btnReset.setVisibility(View.VISIBLE);
        }

        if ((this.buttonVisibilityBits & InitOp) == InitOp) {
            btnInit.setVisibility(View.VISIBLE);
        }

        if ((this.buttonVisibilityBits & ValidOp) == ValidOp) {
            btnValidate.setVisibility(View.VISIBLE);
        }
    }

    private boolean isButtonVisible(Integer buttonOp) {
        return (this.buttonVisibilityBits & buttonOp) == buttonOp;
    }

    private void assignCtrlVars(View rootView) {
        btnRead = rootView.findViewById(R.id.btnRead);
        btnReset = rootView.findViewById(R.id.btnReset);
        btnInit = rootView.findViewById(R.id.btnInit);
        btnValidate = rootView.findViewById(R.id.btnValidate);
        tvCurrentLoggerEPC = rootView.findViewById(R.id.tvCurrentLoggerEPC);
    }

    private void startAnimation(View view, Button buttonID) {
        mCurrentLevel = 0;

        // Get a handle on the ClipDrawable that we will animate.
        LayerDrawable layerDrawable = (LayerDrawable) buttonID.getBackground();
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);

        // Set up TimeAnimator to fire off on button click.
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);

        animateButton(view);
    }

    private void stopAnimation() {
        mCurrentLevel = MAX_LEVEL;
        onTimeUpdate(mAnimator, MAX_LEVEL, LEVEL_INCREMENT);
    }

    private void animateButton(View view) {
        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }
    // ------------------------------------------------------------------------

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        FragmentActivity mActivity = getActivity();
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                mClipDrawable.setLevel(mCurrentLevel);
                if (mCurrentLevel >= MAX_LEVEL) {
                    mAnimator.cancel();
                } else {
                    mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
                    mActivity.invalidateOptionsMenu();
                }
            });
        }
    }

    private Object extractData(Class clazz, Bundle data) {
        Class dataClazz = data.get("body").getClass();

        // Failed to read value from Logger.
        if (dataClazz.getSimpleName().equalsIgnoreCase(String.class.getSimpleName()) && "N/A".equalsIgnoreCase(data.getString("body"))) {
            return null;
        }

        // Incompatible data types.
        if (!dataClazz.getSimpleName().equalsIgnoreCase(clazz.getSimpleName()))
            throw new RuntimeException(String.format("Incompatible Types [extracted %s vs expected: %s] for extraction!!", dataClazz.getSimpleName(), clazz.getSimpleName()));

        switch (clazz.getSimpleName()) {
            case "CAENState":
                return data.getSerializable("body");
            case "String":
                return data.getString("body");
            case "Short":
                return data.getShort("body");
            case "Integer":
                return data.getInt("body");
            case "Boolean":
                return data.getBoolean("body");
            default:
                return data.get("body");
        }
    }

    // ###################################################
    private class CAENCommandsHandler extends Handler {

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            Object obj;
            Activity mActivity = (Activity) mContext;
            switch (msg.what) {
                case ReadSΤΑΤΕ:
                    // stop btnRead animation
                    stopAnimation();

                    obj = extractData(CAENState.class, msg.getData());
                    if (obj != null) {
                        CAENState state = (CAENState) obj;
                        state.state = StatesEnum.READ_VALUES;

                        Short valuesRead = state.getSamplesCnt();
                        // set the appropriate text, based on the operation outcome.
                        if (state.canProceed && valuesRead != null && valuesRead > 0 && !CollectionUtils.isEmpty(state.getSamples())) {
                            // pass business-related params
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);

                            //For philosofish project we need temperatures only after filling bins at fishing tx, not from initializing them.
                            //So the valuesRead attribute must be recalculated from total sample.
                            final short valuesRead2 = (short) state.getSamples().stream().count();

                            mActivity.runOnUiThread(() -> {
                                btnRead.setText(getString(R.string.read_number_measurements, valuesRead2));
                                btnRead.setOnClickListener(null);

                                if (stateResult != null) {
                                    stateResult.setValue(state);
                                }
                            });
                            if (keyReceiver != null) {
                                getActivity().unregisterReceiver(keyReceiver);
                                keyReceiver = null;
                            }
                            // assign listener to reset button
                            btnReset.setOnClickListener(resetBtnListener);
                            btnReset.callOnClick();
                        } else if ("00000".equalsIgnoreCase(state.ctrlReg) && state.samplesCnt != null && state.samplesCnt > 0 && currentLoggerEPC == null) {
                            btnRead.setText(getString(R.string.idle_logger_with_data) + " [" + state.samplesCnt + "]");
                            currentLoggerEPC = loggerEPC;
                        } else if ((state.ctrlReg == null && state.samplesCnt != null && state.samplesCnt > 0) || "00000".equalsIgnoreCase(state.ctrlReg)) {
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);
                            //currentLoggerEPC = null;
                            btnRead.setText(R.string.idle_logger);
                            attachTriggertoButton(readBtnListener);
                            //btnRead.setOnClickListener(null);
                            if (stateResult != null) {
                                stateResult.setValue(state);
                            }
                            // after Reset, initialize the logger and start logging...
//                            btnReset.setOnClickListener(resetBtnListener);
//                            btnReset.setText(R.string.starting_logger);
//                            btnReset.callOnClick();
                        } else if (state.ctrlReg == null || "N/A".equalsIgnoreCase(state.ctrlReg) || "null".equalsIgnoreCase(state.ctrlReg)) {
                            btnRead.setText(R.string.invalid_status);
                            attachTriggertoButton(readBtnListener);
//                        }
                        } else {
                            mActivity.runOnUiThread(() -> {
                                btnRead.setText(R.string.error_reading_logger);
                            });
                            attachTriggertoButton(readBtnListener);

                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnRead.setText(R.string.failed_logger_press_again);
                        });
                        attachTriggertoButton(readBtnListener);
                    }
                    break;
                case ResetSΤΑΤΕ:
                    // stop btnReset animation
                    stopAnimation();

                    obj = extractData(CAENState.class, msg.getData());
                    if (obj != null) {
                        CAENState state = (CAENState) obj;
                        state.state = StatesEnum.RESET;

                        boolean successfulReset = state.getOpReset() == 1 && state.canProceed;
                        if (successfulReset) {
                            mActivity.runOnUiThread(() -> {
                                btnReset.setText(R.string.reseted_logger);
                                btnReset.setOnClickListener(null);
                                if (!isButtonVisible(InitOp)) {
                                    dismiss();
                                }
                            });
                            // after Reset, initialize the logger and start logging...
                            if (isButtonVisible(InitOp)) {
                                if (keyReceiver != null) {
                                    getActivity().unregisterReceiver(keyReceiver);
                                    keyReceiver = null;
                                }
                                btnInit.setOnClickListener(initBtnListener);
                                btnInit.callOnClick();
                            }
                        } else {
                            mActivity.runOnUiThread(() -> {
                                btnReset.setText(R.string.error_resetting_logger);
                            });
                            //                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(resetBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                            attachTriggertoButton(resetBtnListener);
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnReset.setText(R.string.failed_logger_press_again);
                        });
//                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(resetBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                        attachTriggertoButton(resetBtnListener);
                    }
                    break;
                case InitSΤΑΤΕ:
                    // stop btnInit animation
                    stopAnimation();

                    obj = extractData(CAENState.class, msg.getData());
                    if (obj != null) {
                        CAENState state = (CAENState) obj;
                        state.state = StatesEnum.INIT;

                        boolean successfulInit = state.ctrlReg != null && state.ctrlReg.endsWith("100");
                        if (successfulInit) {
                            // pass business-related params
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);
                            // initTS will hold initialisation timestamps stored in the logger
                            initTS = state.getInitTS();

                            if (stateResult != null) {
                                stateResult.setValue(state);
                            }

                            if (keyReceiver != null) {
                                getActivity().unregisterReceiver(keyReceiver);
                                keyReceiver = null;
                            }

                            btnInit.setText(R.string.started_logging);
                            btnInit.setOnClickListener(null);
                            btnValidate.setOnClickListener(validBtnListener);
                            btnValidate.callOnClick();
                        } else {
                            btnInit.setText(R.string.error_initializing_logger);
                            //                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(initBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                            attachTriggertoButton(initBtnListener);
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnInit.setText(R.string.failed_logger_press_again);
                        });
//                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(initBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                        attachTriggertoButton(initBtnListener);
                    }
                    break;
                case ValidSΤΑΤΕ:
                    // stop btnInit animation
                    stopAnimation();

                    obj = extractData(CAENState.class, msg.getData());
                    if (obj != null) {
                        CAENState state = (CAENState) obj;
                        state.state = StatesEnum.VALID;

                        boolean validInitTS = initTS != null && initTS.equals(state.getInitTS());
                        boolean successfulValidation = state.ctrlReg != null && state.ctrlReg.endsWith("100") && validInitTS && state.timeBin == 0; //Integer.valueOf(1).equals(state.getOpLogging()) && state.canProceed;

                        if (successfulValidation) {
                            // pass business-related params
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);

                            if (stateResult != null) {
                                stateResult.setValue(state);
                            }
                            if (keyReceiver != null) {
                                getActivity().unregisterReceiver(keyReceiver);
                                keyReceiver = null;
                            }

                            btnValidate.setText(R.string.validated);
                            btnValidate.setOnClickListener(null);

                            dismiss();
                        } else {
//                            btnReset.setText(R.string.error_validating_logger);
//                            btnReset.setOnClickListener(validBtnListener);
                            btnValidate.setOnClickListener(resetBtnListener);
                            //btnValidate.setOnClickListener(null);
                            btnValidate.setText(R.string.not_validated);
//                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(validBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                            attachTriggertoButton(resetBtnListener);
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnValidate.setText(R.string.failed_logger_press_again);
                        });
//                        if (keyReceiver ==null) {
//                            keyReceiver = new X9KeyReceiver(validBtnListener);
//                            IntentFilter filter = new IntentFilter();
//                            filter.addAction("android.rfid.FUN_KEY");
//                            getActivity().registerReceiver(keyReceiver, filter);
//                        }
                        attachTriggertoButton(resetBtnListener);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void attachTriggertoButton(View.OnClickListener ButtonListener) {
        if (keyReceiver == null) {
            keyReceiver = new X9KeyReceiver(ButtonListener);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.rfid.FUN_KEY");
            getActivity().registerReceiver(keyReceiver, filter);
        }
        getDialog().setCanceledOnTouchOutside(true);
    }
}