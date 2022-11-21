package io.agritrack.ui.tools.caen;

import static io.agritrack.caen.api.CAEN_CONSTANTS.InitSΤΑΤΕ;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSΤΑΤΕ;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ResetSΤΑΤΕ;
import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.InitOp;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.LEVEL_INCREMENT;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.MAX_LEVEL;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.ReadOp;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.ResetOp;
import static io.agritrack.ui.tools.caen.LoggerDialogDecorator.ValidOp;

import android.animation.TimeAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.concurrent.Executors;

import io.agritrack.R;
import io.agritrack.caen.api.CAENLoggerService;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.common.CAENState;

/**
 * Component implementation of ILoggerDialog.
 * Use the {@link LoggerDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * Part of a Decorator Design Pattern implementation to allow for
 * different Logger Dialogs creation at runtime.
 */
public class LoggerDialogFragment extends DialogFragment implements TimeAnimator.TimeListener, ILoggerDialog {
    // Local handler that receives the RFID scanner results.
    private Handler mScanHandler;
    private Context mContext;

    private Button btnRead, btnInit, btnReset, btnValidate;
    private int buttonVisibilityBits = 0x00000;

    private String loggerEPC;
    private String assetEPC;
    private String productionLane;
    private Long initedAt;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    private CAENLoggerService loggerSvc;
    private ICAEN_API cmd;

    //-------
    private MutableLiveData<CAENState> stateResult;
    private short samplingInterval = DefaultInterval;


    protected final View.OnClickListener initBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            FragmentActivity mActivity = getActivity();
            //...setup Init Button............
//            mActivity.runOnUiThread(() -> {
                btnInit.setBackgroundResource(R.drawable.button_background);
                btnInit.setText("Start Logger...");
                startAnimation(getView(), btnInit);
//            });

            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke reset() method of CAENLoggerService.
            Executors.newSingleThreadExecutor().execute(()->loggerSvc.doEnableLogger(samplingInterval));
        } else {
            CToast(getActivity(), render("No Tag detected!!\nPlease change your position!"), Toast.LENGTH_SHORT);
        }
    };
    //##############################################################

    protected final View.OnClickListener resetBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            FragmentActivity mActivity = getActivity();
            //...setup Reset Button............
//            mActivity.runOnUiThread(() -> {
                btnReset.setBackgroundResource(R.drawable.button_background);
                btnReset.setText("Resetting...");
                startAnimation(getView(), btnReset);
//            });

            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke reset() method of CAENLoggerService.
            Executors.newSingleThreadExecutor().execute(()->loggerSvc.doResetLogger());

        } else {
            CToast(getActivity(), render("No Tag detected!!\nPlease change your position!"), Toast.LENGTH_SHORT);
        }
    };
    //##############################################################

    private final View.OnClickListener readBtnListener = v -> {
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            FragmentActivity mActivity = getActivity();
            //...setup Read Button............
//            mActivity.runOnUiThread(() -> {
                btnRead.setBackgroundResource(R.drawable.button_background);
                btnRead.setText("Reading Logger...");
                startAnimation(getView(), btnRead);
//            });

            // pass selected EPC as RFID filter
            cmd.setFilterEPC(loggerEPC);
            //invoke read() method of CAENLoggerService.
            Executors.newSingleThreadExecutor().execute(()->loggerSvc.doReadMeasurements());

        } else {
            CToast(getActivity(), render("No Tag detected!!\nPlease change your position!"), Toast.LENGTH_SHORT);
        }
    };
    //##############################################################


    private LoggerDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC) {
        LoggerDialogFragment frag = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        frag.setArguments(args);

        return frag;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, Long initedAt) {
        LoggerDialogFragment frag = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putLong(INITED_AT, initedAt);
        frag.setArguments(args);

        return frag;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, String productionLane) {
        LoggerDialogFragment frag = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putString(PROD_LANE, productionLane);
        frag.setArguments(args);

        return frag;
    }

    public static ILoggerDialog newInstance(String loggerEPC, String assetEPC, String productionLane, Long initializedAt) {
        LoggerDialogFragment frag = new LoggerDialogFragment();
        Bundle args = new Bundle();
        args.putString(LOGGER_EPC, loggerEPC);
        args.putString(ASSET_EPC, assetEPC);
        args.putString(PROD_LANE, productionLane);
        args.putLong(INITED_AT, initializedAt);
        frag.setArguments(args);

        return frag;
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
        this.mScanHandler = new CAENCommandsHandler();

        if (getArguments() != null) {
            this.loggerEPC = getArguments().getString(LOGGER_EPC);
            this.assetEPC = getArguments().getString(ASSET_EPC);
            this.initedAt = getArguments().getLong(INITED_AT);
            this.productionLane = getArguments().getString(PROD_LANE);

            // show/hide buttons according to Visibility Bit values.
            applyButtonsVisibility();

            // Enable Read button
            btnRead.setText("Reading Measurements...");
            btnRead.setBackgroundResource(R.drawable.button_background);
            btnRead.setOnClickListener(readBtnListener);

            // assign listener to init button
            btnInit.setOnClickListener(initBtnListener);

            // assign listener to reset button
            btnReset.setOnClickListener(resetBtnListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
        this.loggerSvc = new CAENLoggerService(this.cmd, this.mScanHandler);

        // Press First Button
        if ((ReadOp & this.buttonVisibilityBits) == ReadOp) {
            btnRead.callOnClick();
        } else if ((InitOp & this.buttonVisibilityBits) == InitOp) {
            btnReset.callOnClick();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
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

    private void assignCtrlVars(View rootView) {
        btnRead = rootView.findViewById(R.id.btnRead);
        btnReset = rootView.findViewById(R.id.btnReset);
        btnInit = rootView.findViewById(R.id.btnInit);
        btnValidate = rootView.findViewById(R.id.btnValidate);
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
//        getActivity().runOnUiThread(() -> animateButton(view));
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

    // ###################################################
    private class CAENCommandsHandler extends Handler {

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
                        if (state.canProceed &&  valuesRead != null && valuesRead > 0 && !CollectionUtils.isEmpty(state.getSamples())) {
                            // pass business-related params
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);

                            mActivity.runOnUiThread(() -> {
                                btnRead.setText(String.format("READ %s measurements.", valuesRead));
                                btnRead.setOnClickListener(null);

                                if(stateResult != null) {
                                    stateResult.setValue(state);
                                }
                            });
                            //displayMeasurementsDialog(samples);
                            btnReset.callOnClick();
                        } else {
                            mActivity.runOnUiThread(() -> {
                                btnRead.setText("Error on reading. Press the button again.");
                            });
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnRead.setText("Failed. Press the button again.");
                        });
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
                                btnReset.setText("Cleared (reset) logger.");
                                btnReset.setOnClickListener(null);
                            });
                            // after Reset, initialize the logger and start logging...
                            btnInit.callOnClick();
                        } else {
                            mActivity.runOnUiThread(() -> {
                                btnReset.setText("Error on resetting logger...\nPress button again!");
                            });
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnRead.setText("Failed. Press the button again.");
                        });
                    }
                    break;
                case InitSΤΑΤΕ:
                    // stop btnInit animation
                    stopAnimation();

                    obj = extractData(CAENState.class, msg.getData());
                    if (obj != null) {
                        CAENState state = (CAENState) obj;
                        state.state = StatesEnum.INIT;

                        boolean successfulInit = Integer.valueOf(1).equals(state.getOpLogging()) && state.canProceed;
                        if (successfulInit) {
                            // pass business-related params
                            state.setLoggerEPC(loggerEPC);
                            state.setAssetEPC(assetEPC);
                            state.setProductionLane(productionLane);

//                            mActivity.runOnUiThread(() -> {
                                btnInit.setText("Started logging...");
                                btnInit.setOnClickListener(null);
                                dismiss();

//                            });
                        } else {
                            mActivity.runOnUiThread(() -> {
                                btnReset.setText("Error on initializing logger...\nPress button again!");
                            });
                        }
                    } else {
                        mActivity.runOnUiThread(() -> {
                            btnRead.setText("Failed. Press the button again.");
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Object extractData(Class clazz, Bundle data) {
        Class dataClazz = data.get("body").getClass();

        // Failed to read value from Logger.
        if(dataClazz.getSimpleName().equalsIgnoreCase(String.class.getSimpleName()) && "N/A".equalsIgnoreCase(data.getString("body"))) {
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

    private void displayMeasurementsDialog(List<String[]> values) {

        if (values == null) {
            CToast(mContext, "Invalid data read!!!", Toast.LENGTH_LONG);
            return;
        }

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mContext);
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%04d. [%s] -->%3$10s \u00B0C", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }
}