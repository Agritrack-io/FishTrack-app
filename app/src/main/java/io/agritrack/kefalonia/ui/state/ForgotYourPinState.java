package io.agritrack.kefalonia.ui.state;

public class ForgotYourPinState {

    private static ForgotYourPinState INSTANCE = null;

    private boolean success;

    private ForgotYourPinState() {
    }

    public static ForgotYourPinState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgotYourPinState();
        }
        return (INSTANCE);
    }

    public static ForgotYourPinState getNewInstance() {
        INSTANCE = new ForgotYourPinState();
        return (INSTANCE);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}