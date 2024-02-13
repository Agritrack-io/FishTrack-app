package io.agritrack.kefalonia.ui.tools.caen;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.caen.common.CAENState;

public interface ILoggerDialog {
    String TAG = "CAENLoggerDialogFragment";

    ;
    String LOGGER_EPC = "LoggerEPC";
    String ASSET_EPC = "AssetEPC";
    String INITED_AT = "InitializedAt";
    String PICKED_AT = "PickedAt";
    String PROD_LANE = "ProductionLane";

    // show the decorated Dialog.
    void show(FragmentManager fm);

    void setButtonsVisibility(int buttonBits);

    void setStateObserver(MutableLiveData<CAENState> stateResult);

    enum StatesEnum {STOP_LOGGER, COUNT_SAMPLES, READ_VALUES, RESET, INIT, VALID}
}
