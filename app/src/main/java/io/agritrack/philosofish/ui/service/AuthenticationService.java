package io.agritrack.philosofish.ui.service;

import io.agritrack.philosofish.crypto.Crypto;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.AppUser;

public class AuthenticationService {

    public boolean authenticateUser(MobileDB db, String username, String pin) {
        try {
            AppUser user = db.userDAO().getByUsername(username);
            if (user == null) return false;

            // Load PIN saved on online login
            String savedEncryptedPin = LocalPreferences.loadUserPin(username);

            if (savedEncryptedPin != null) {
                String savedPin = Crypto.decodeAndDecrypt(savedEncryptedPin);
                return savedPin.equals(pin);
            }

            // Fallback for older installs (rare)
            if (user.pin != null) {
                String dbPin = Crypto.decodeAndDecrypt(user.pin);
                return dbPin.equals(pin);
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
