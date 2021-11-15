package io.agritrack.ui.login;

import java.util.List;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private final String username;
    private final String token;
    private final List<String> roles;

    LoggedInUserView(String username, String token, List<String> roles) {
        this.username = username;
        this.token = token;
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}