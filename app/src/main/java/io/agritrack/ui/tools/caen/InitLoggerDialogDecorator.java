package io.agritrack.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public class InitLoggerDialogDecorator extends LoggerDialogDecorator {

    public InitLoggerDialogDecorator(ILoggerDialog dlg) {
        super(dlg);
        super.setButtonsVisibility(ResetOp | InitOp);
    }

    @Override
    public void show(FragmentManager fm) {
        super.show(fm);
    }
}
