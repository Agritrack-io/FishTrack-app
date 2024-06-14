package io.agritrack.philosofish.ui.tools.caen.fsm;

import org.junit.Test;

public class TestLoggerFiniteStateMachine {

    //-------------------------------------------
    @Test
    public void initFSMTest() throws Exception {
        LoggerFiniteStateMachine fsm = new LoggerFiniteStateMachine();

        fsm.beginFrom(LoggerState.INIT);
        fsm.run();
    }
}
