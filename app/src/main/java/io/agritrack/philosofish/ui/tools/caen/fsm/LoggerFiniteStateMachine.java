package io.agritrack.philosofish.ui.tools.caen.fsm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggerFiniteStateMachine {
    // Single Thread
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    LoggerState currentState;

    public void beginFrom(LoggerState beginState) {
        this.currentState = beginState;
    }

    public void run() {
        LoggerContext ctx = new LoggerContext();

        this.currentState.nextState(ctx);
    }
}
