package io.agritrack.fishtrack.state;

import java.nio.charset.Charset;

import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;
import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;
import io.agritrack.fishtrack.data.model.tx.TransportTransaction;

public class GlobalState {

    public static FishingRecord recFishing = new FishingRecord();
    public static TransportationRecord recTransport = new TransportationRecord();
    public static ProcessingRecord recProcessing = new ProcessingRecord();
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

    public static ProcessingRecord initProcessingTx() {
        recProcessing = new ProcessingRecord();
        return recProcessing;
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

            txTransport.packagingSiteId = recTransport.packagingSite;
            txTransport.driverName = recTransport.driverName;
            txTransport.truckLicensePlate = recTransport.licensePlate;
            txTransport.securityClipNo = recTransport.clipNumber;
            txTransport.driverSignature = new String(recTransport.signatureBytes, Charset.forName("UTF8"));
            txTransport.isTruckRefrigerated = recTransport.refrigeratedTruck;
            txTransport.isParallelTransport = recTransport.parallelTransport;
            txTransport.transportHead = "N/A";

            db.transportTransactionDAO().insert(txTransport);
            return txTransport;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ProcessingTransaction commitProcessing(MobileDB db) {
        try {
            ProcessingTransaction txProcess = new ProcessingTransaction();
            txProcess.dispatchNote = recProcessing.dispatchNote;
            txProcess.fishCondition = recProcessing.fishCondition;
            txProcess.plot = recProcessing.packagingLot;
            txProcess.site = recProcessing.packagingSite;
            //txProcess.remarks = recProcessing.remarks;
            txProcess.securityClipNumber = recProcessing.securityClip;
            //txProcess.dispatchNote = recProcessing.dispatchNote;

            db.processingTransactionDAO().insert(txProcess);

            return txProcess;
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
