package io.agritrack.fishtrack.state;

import io.agritrack.fishtrack.data.dto.AppUserDTO;
import io.agritrack.fishtrack.data.dto.SiteDTO;

public class GlobalState {

    private static GlobalState INSTANCE = null;

    private SiteDTO curSite;
    private AppUserDTO curUser;

    private GlobalState() {}

    public static GlobalState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GlobalState();
        }
        return(INSTANCE);
    }

    public static GlobalState getNewInstance() {
        INSTANCE = new GlobalState();
        return(INSTANCE);
    }

    public SiteDTO getTank() {
        return curSite;
    }

    public void setTank(SiteDTO site) {
        this.curSite = site;
    }

}
