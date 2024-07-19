package io.agritrack.philosofish.ui.tools.caen;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.caen.common.CAENState;

public class LoggerDialogDecorator implements ILoggerDialog {
    public static final int ReadOp = 0x0001;
    public static final int ResetOp = 0x0010;
    public static final int InitOp = 0x0100;
    public static final int ValidOp = 0x1000;

    public static int LEVEL_INCREMENT = 20, MAX_LEVEL = 10000;

    protected ILoggerDialog loggerDlg;

    public LoggerDialogDecorator(ILoggerDialog dlg) {
        this.loggerDlg = dlg;
    }

    @Override
    public void show(FragmentManager fm) {
        loggerDlg.show(fm);
    }

    @Override
    public void setButtonsVisibility(int buttonBits) {
        loggerDlg.setButtonsVisibility(buttonBits);
    }

    @Override
    public void setStateObserver(MutableLiveData<CAENState> stateResult) {
        loggerDlg.setStateObserver(stateResult);
    }
}

