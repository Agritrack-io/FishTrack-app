package io.agritrack.fishtrack.ui.service;

import io.agritrack.fishtrack.crypto.Crypto;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;

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
