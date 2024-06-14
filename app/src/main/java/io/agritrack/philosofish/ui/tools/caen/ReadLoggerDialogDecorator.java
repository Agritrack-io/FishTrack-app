package io.agritrack.philosofish.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public class ReadLoggerDialogDecorator extends LoggerDialogDecorator {


    public ReadLoggerDialogDecorator(ILoggerDialog dlg) {
        super(dlg);
        super.setButtonsVisibility(ReadOp | ResetOp | InitOp);
    }

    @Override
    public void show(FragmentManager fm) {
        super.show(fm);
    }
}
