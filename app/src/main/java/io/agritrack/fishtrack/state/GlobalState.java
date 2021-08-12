package io.agritrack.fishtrack.state;

import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;

public class GlobalState {

    public static HarvestRecord recHarvest = new HarvestRecord();


    private GlobalState() { }

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


}
