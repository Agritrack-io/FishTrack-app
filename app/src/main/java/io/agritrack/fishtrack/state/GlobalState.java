package io.agritrack.fishtrack.state;

import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.dto.AppUserDTO;
import io.agritrack.fishtrack.data.dto.SiteDTO;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;

public class GlobalState {

    private static GlobalState INSTANCE = null;

    private SiteDTO curSite;
    private AppUserDTO curUser;
    public static HarvestRecord recHarvest;


    private GlobalState() { }

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

    public static HarvestRecord initHarvest() {
        recHarvest = new HarvestRecord();
        return recHarvest;
    }

    public static boolean commitHarvest(MobileDB db) {
        try {
            HarvestTransaction txHarvest = new HarvestTransaction();
            db.harvestTransactionDAO().insert(txHarvest);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    public SiteDTO getTank() {
        return curSite;
    }

    public void setTank(SiteDTO site) {
        this.curSite = site;
    }

}
