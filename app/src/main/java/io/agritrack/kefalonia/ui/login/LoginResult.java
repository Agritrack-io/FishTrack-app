package io.agritrack.kefalonia.ui.login;

import androidx.annotation.Nullable;

import io.agritrack.kefalonia.FishTrackApplication;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInUserView success;

    @Nullable
    private Integer error;

    @Nullable
    private String errorMsg;

    LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    LoginResult(@Nullable String errorMsg) {
        this.errorMsg = errorMsg;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    String getError() {
        if (error != null) {
            return FishTrackApplication.getAppContext().getResources().getString(error);
        } else {
            return this.errorMsg;
        }
    }
}