package io.agritrack.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public class LoggerDialogDecorator implements ILoggerDialog {
    public static final int ReadOp  = 0x0001;
    public static final int ResetOp = 0x0010;
    public static final int InitOp  = 0x0100;
    public static final int ValidOp = 0x1000;

    public static int LEVEL_INCREMENT = 1000, MAX_LEVEL = 10000;

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
}

