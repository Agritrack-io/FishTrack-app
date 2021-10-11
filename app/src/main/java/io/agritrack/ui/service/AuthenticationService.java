package io.agritrack.ui.service;

import io.agritrack.crypto.Crypto;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.AppUser;

public class AuthenticationService {
    public boolean authenticateUser(MobileDB db, String username, String pin) {
        try {
            AppUser appUser = db.userDAO().getByUsername(username);
            if (appUser != null) {
                return pin.equals(Crypto.decodeAndDecrypt(appUser.pin));
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
