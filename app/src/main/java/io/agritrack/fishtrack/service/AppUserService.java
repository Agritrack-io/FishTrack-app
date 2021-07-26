package io.agritrack.fishtrack.service;

import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;

public class AppUserService {
    public boolean authenticateUser(MobileDB db, String username, String pin) {
        AppUser user = db.userDAO().getByUsername(username);
        if (user != null) {
            return pin.equals(user.pin);
        } else {
            return false;
        }
    }
}
