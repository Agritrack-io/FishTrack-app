package io.agritrack.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public interface ILoggerDialog {
    enum State {STOP_LOGGER, COUNT_SAMPLES, READ_VALUES, RESET, INIT};

    String TAG = "CAENLoggerDialogFragment";
    String LOGGER_EPC = "LoggerEPC";
    String ASSET_EPC = "AssetEPC";
    String INITED_AT = "InitializedAt";
    String PROD_LANE = "ProductionLane";

    int LEVEL_INCREMENT = 1000, MAX_LEVEL = 10000;

    // show the decorated Dialog.
    void show(FragmentManager fm);

    void setButtonsVisibility(int buttonBits);
}
