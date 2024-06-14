package io.agritrack.philosofish.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public class ResetLoggerDialogDecorator extends LoggerDialogDecorator {

    public ResetLoggerDialogDecorator(ILoggerDialog dlg) {
        super(dlg);
        super.setButtonsVisibility(InitOp);
    }

    @Override
    public void show(FragmentManager fm) {
        super.show(fm);
    }
}
