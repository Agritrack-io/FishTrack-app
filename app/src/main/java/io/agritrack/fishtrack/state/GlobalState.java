package io.agritrack.fishtrack.state;

import com.google.android.gms.common.util.Strings;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.tx.AssetTransaction;
import io.agritrack.fishtrack.data.model.tx.CorrelationTransaction;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;
import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;
import io.agritrack.fishtrack.data.model.tx.TransportTransaction;
import io.agritrack.fishtrack.enums.TxStatus;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class GlobalState {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public static HarvestRecord recHarvest = new HarvestRecord();
    public static FishingRecord recFishing = new FishingRecord();
    public static TransportationRecord recTransport = new TransportationRecord();
    public static ProcessingRecord recProcessing = new ProcessingRecord();

    public static WHTxRecord recWHIncoming = new WHTxRecord();
    public static WHTxRecord recWHOutgoing = new WHTxRecord();
    public static WHCorrelationRecord recWHCorrelation = new WHCorrelationRecord();
    public static InventoryWHRecord recWHInventory = new InventoryWHRecord();

    public static RepairRecord recInternalRepair = new RepairRecord();
    public static RepairRecord recExternalRepair = new RepairRecord();

    private GlobalState() {
    }

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

    public static WHTxRecord initWHIncomingTx() {
        recWHIncoming = new WHTxRecord();
        return recWHIncoming;
    }

    public static WHTxRecord initWHOutgoingTx() {
        recWHOutgoing = new WHTxRecord();
        return recWHOutgoing;
    }

    public static WHCorrelationRecord initWHCorrelationTx() {
        recWHCorrelation = new WHCorrelationRecord();
        return recWHCorrelation;
    }

    public static InventoryWHRecord initWHInventoryTx() {
        recWHInventory = new InventoryWHRecord();
        return recWHInventory;
    }

    public static HarvestRecord initHarvestTx() {
        recHarvest = new HarvestRecord();
        return recHarvest;
    }

    public static RepairRecord initInternalRepairTx() {
        recInternalRepair = new RepairRecord();
        return recInternalRepair;
    }

    public static RepairRecord initExternalRepairTx() {
        recExternalRepair = new RepairRecord();
        return recExternalRepair;
    }

    public static FishingTransaction commitFishing(MobileDB db, Boolean finalCommit) {
        try {
            FishingTransaction txFishing = new FishingTransaction();

            txFishing.id = recFishing.txKey;
            txFishing.harvestRq = recFishing.harvestRq;
            txFishing.platformRFID = recFishing.platformRFID;
            txFishing.cageRFID = recFishing.cageRFID;
            txFishing.netRFID = recFishing.netRFID;
            txFishing.fishType = recFishing.speciesName;
            txFishing.ichthyopathologist = recFishing.pathologist;
            if(!Strings.isEmptyOrWhitespace(recFishing.lastFed)) {
                try {
                    Date lf = sdf.parse(recFishing.lastFed);
                    txFishing.lastFeed = lf.getTime();
                } catch (Exception ignored) {
                }
            }
            txFishing.iceAdequacy = recFishing.adequateIce.toString();
            txFishing.iceSupplier = recFishing.iceSupplier;
            txFishing.seaTemperature = recFishing.seaTemperature;
            txFishing.harvestBinsCnt = recFishing.totalBinsUsed;
            txFishing.orderedQuantity = recFishing.reqWeight != null ? Double.valueOf(recFishing.reqWeight) : null;
            txFishing.requester = recFishing.requesterName;
            txFishing.totalQty = recFishing.totalFishWeight;
            txFishing.harvestBins = recFishing.availBins;
            txFishing.txStatus = Boolean.FALSE.equals(finalCommit) ? TxStatus.PENDING : TxStatus.COMPLETED;
            txFishing.user = LocalPreferences.getLoggedInUser("N/A");
            txFishing.site = LocalPreferences.getCurrentSiteId().toString();

            db.fishingTransactionDAO().update(txFishing);

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
            txTransport.driverSignature = new String(recTransport.signatureBytes, StandardCharsets.UTF_8);
            txTransport.isTruckRefrigerated = recTransport.refrigeratedTruck;
            txTransport.isParallelTransport = recTransport.parallelTransport;
            txTransport.transportHead = "N/A";
            txTransport.loadedBins = recTransport.availBins;
            txTransport.user = LocalPreferences.getLoggedInUser("N/A");
            txTransport.site = LocalPreferences.getCurrentSiteId().toString();

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
            txProcess.cleanTruck = Boolean.toString(recProcessing.cleanTruck);
            txProcess.smells = Boolean.toString(recProcessing.smellyTruck);
            txProcess.plot = recProcessing.packagingLot;
            txProcess.site = recProcessing.packagingSite;
            //txProcess.remarks = recProcessing.remarks;
            txProcess.receivedBins = recProcessing.availBins;
            txProcess.securityClipNumber = recProcessing.securityClip;
            txProcess.user = LocalPreferences.getLoggedInUser("N/A");
            txProcess.site = LocalPreferences.getCurrentSiteId().toString();

            db.processingTransactionDAO().insert(txProcess);

            return txProcess;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHIncoming(MobileDB db) {
        try {
            AssetTransaction txWHIncoming = new AssetTransaction();
            txWHIncoming.state = recWHIncoming.state.name();
            txWHIncoming.assetType = recWHIncoming.assetType.name();
            txWHIncoming.itemRFIDs = recWHIncoming.items;
            txWHIncoming.from = recWHIncoming.from;
            txWHIncoming.to = recWHIncoming.to;
            db.assetTransactionDAO().insert(txWHIncoming);

            return txWHIncoming;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHOutgoing(MobileDB db) {
        try {
            AssetTransaction txWHOutgoing = new AssetTransaction();
            txWHOutgoing.state = recWHOutgoing.state.name();
            txWHOutgoing.assetType = recWHOutgoing.assetType.name();
            txWHOutgoing.itemRFIDs = recWHOutgoing.items;
            txWHOutgoing.from = recWHOutgoing.from;
            txWHOutgoing.to = recWHOutgoing.to;
            db.assetTransactionDAO().insert(txWHOutgoing);

            return txWHOutgoing;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static CorrelationTransaction commitWHCorrelation(MobileDB db) {
        try {
            CorrelationTransaction txCorrelation = new CorrelationTransaction();
            txCorrelation.assetType = recWHCorrelation.assetType.name();
            txCorrelation.barcode = recWHCorrelation.barcode;
            txCorrelation.rfid = recWHCorrelation.rfid;
            db.correlationTransactionDAO().insert(txCorrelation);

            return txCorrelation;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static RepairTransaction commitInternalRepair(MobileDB db) {
        try {
            RepairTransaction txIndoorsRepair = new RepairTransaction();

            db.repairTransactionDAO().insert(txIndoorsRepair);

            return txIndoorsRepair;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static RepairTransaction commitExternalRepair(MobileDB db) {
        try {
            RepairTransaction txOutdoorsRepair = new RepairTransaction();

            db.repairTransactionDAO().insert(txOutdoorsRepair);

            return txOutdoorsRepair;
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
