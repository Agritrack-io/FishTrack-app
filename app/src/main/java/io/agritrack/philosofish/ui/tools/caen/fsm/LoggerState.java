package io.agritrack.philosofish.ui.tools.caen.fsm;

import static io.agritrack.philosofish.common.LargeString.render;

public enum LoggerState {
    IDLE {
        @Override
        public LoggerState process(LoggerContext ctx) {
            this.currentState = null;
            return this;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return true;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return true;
        }
    },
    STOP {
        @Override
        protected LoggerState process(LoggerContext ctx) {
            this.currentState = "Stop";
            return IDLE;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return false;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return false;
        }
    },
    INIT {
        @Override
        protected LoggerState process(LoggerContext ctx) {
            this.currentState = "Init";

            // call the <Init> op on caen logger
/*            if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                btnInit.setBackgroundResource(R.drawable.button_background);
                btnInit.setText(R.string.starting_logger);
                startAnimation(getView(), btnInit);

                // pass selected EPC as RFID filter
                cmd.setFilterEPC(loggerEPC);
                //invoke reset() method of CAENLoggerService.
                setCancelable(false);
                v.setEnabled(false);
                if (!executorService.isShutdown()) {
                    executorService.execute(() -> {
                        if (this != null && this.getActivity() != null) {
                            this.getActivity().runOnUiThread(() -> setCancelable(false));
                            this.getActivity().runOnUiThread(() -> v.setEnabled(false));
                            this.loggerSvc.doEnableLogger(samplingInterval);
                            this.getActivity().runOnUiThread(() -> setCancelable(true));
                            this.getActivity().runOnUiThread(() -> v.setEnabled(true));
                        }
                    });
                }
            } else {
                CToast(getActivity(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
            }*/

            return this;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return true;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return false;
        }
    },
    READ {
        @Override
        protected LoggerState process(LoggerContext ctx) {
            this.currentState = "Read";
            return IDLE;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return false;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return false;
        }
    },
    RESET {
        @Override
        protected LoggerState process(LoggerContext ctx) {
            this.currentState = "Reset";
            return IDLE;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return false;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return false;
        }
    },
    VALIDATE {
        @Override
        protected LoggerState process(LoggerContext ctx) {
            this.currentState = "Validate";
            return IDLE;
        }

        @Override
        protected boolean canEnter(LoggerContext ctx) {
            return false;
        }

        @Override
        protected boolean canExit(LoggerContext ctx) {
            return false;
        }
    };


    public LoggerState nextState(LoggerContext ctx) {
        if (!canEnter(ctx)) {
            return this;
        }

        LoggerState next = process(ctx);

        if (!canExit(ctx)) {
            return this;
        }

        return  next;
    }

    // ########################
    protected String currentState;
    private LoggerState previousState;
    private LoggerState nextState;

    protected abstract LoggerState process(LoggerContext ctx);

    protected boolean canEnter(LoggerContext ctx) {
        return false;
    }

    protected boolean canExit(LoggerContext ctx) {
        return false;
    }
}
