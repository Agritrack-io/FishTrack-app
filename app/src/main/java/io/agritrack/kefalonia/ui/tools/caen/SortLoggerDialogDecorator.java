package io.agritrack.kefalonia.ui.tools.caen;

import androidx.fragment.app.FragmentManager;

public class SortLoggerDialogDecorator extends LoggerDialogDecorator {


    public SortLoggerDialogDecorator(ILoggerDialog dlg) {
        super(dlg);
        super.setButtonsVisibility(ReadOp | ResetOp);
    }

    @Override
    public void show(FragmentManager fm) {
        super.show(fm);
    }
}
