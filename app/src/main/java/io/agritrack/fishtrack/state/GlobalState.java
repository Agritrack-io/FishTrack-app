package io.agritrack.fishtrack.state;

import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;
import io.agritrack.fishtrack.data.model.tx.TransportTransaction;

public class GlobalState {

    public static FishingRecord recFishing = new FishingRecord();
    public static TransportationRecord recTransport = new TransportationRecord();
    public static HarvestRecord recHarvest = new HarvestRecord();


    private GlobalState() { }

    public static FishingRecord initFishingTx() {
        recFishing = new FishingRecord();
        return recFishing;
    }

    public static TransportationRecord initTransportationTx() {
        recTransport = new TransportationRecord();
        return recTransport;
    }

    public static HarvestRecord initHarvestTx() {
        recHarvest = new HarvestRecord();
        return recHarvest;
    }

    public static FishingTransaction commitFishing(MobileDB db) {
        try {
            FishingTransaction txFishing = new FishingTransaction();
            txFishing.platformRFID = recFishing.platformRFID;
            txFishing.cageRFID = recFishing.cageRFID;
            txFishing.netRFID = recFishing.netRFID;
            txFishing.fishType = recFishing.speciesName;
            txFishing.ichthyopathologist = recFishing.pathologist;
            txFishing.lastFeed = recFishing.lastFed;
            txFishing.iceAdequacy = recFishing.adequateIce.toString();
            txFishing.iceSupplier = recFishing.iceSupplier;
            txFishing.seaTemperature = recFishing.seaTemperature;
            txFishing.harvestBinsCnt = recFishing.totalBinsUsed;
            txFishing.orderedQuantity = Double.valueOf(recFishing.reqWeight);
            txFishing.totalQty = recFishing.totalFishWeight;
            //txFishing.totalQty = recFishing.requesterName;

            db.fishingTransactionDAO().insert(txFishing);

            return txFishing;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static TransportTransaction commitTransport(MobileDB db) {
        try {
            TransportTransaction txTransport = new TransportTransaction();


            db.transportTransactionDAO().insert(txTransport);

            return txTransport;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static HarvestTransaction commitHarvest(MobileDB db) {
        try {
            HarvestTransaction txHarvest = new HarvestTransaction();


            db.harvestTransactionDAO().insert(txHarvest);

            return txHarvest;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


}
