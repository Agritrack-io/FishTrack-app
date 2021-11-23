package io.agritrack.fruit.state;

import static io.agritrack.enums.AssetType.ALL;

import com.google.android.gms.common.util.Strings;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.RepairTransaction;
import io.agritrack.data.model.tx.SeaTemperatureTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.CoInventoryItem;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.enums.TxStatus;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fruit.state.HarvestRecord;
import io.agritrack.fish.state.InventoryWHRecord;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.state.RepairRecord;
import io.agritrack.fish.state.SeaTemperatureRecord;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fish.state.WHCorrelationRecord;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.ui.service.LocalPreferences;

public class FruitGlobalState {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public static PlantRecord recPlant = new PlantRecord();
    public static HarvestRecord recHarvest = new HarvestRecord();
    public static StorageRecord recStorage = new StorageRecord();
    public static ProcessingRecord recProcessing = new ProcessingRecord();

    public static WHTxRecord recWHIncoming = new WHTxRecord();
    public static WHTxRecord recWHOutgoing = new WHTxRecord();
    public static WHCorrelationRecord recWHCorrelation = new WHCorrelationRecord();
    public static InventoryWHRecord recWHInventory = new InventoryWHRecord();

    public static RepairRecord recInternalRepair = new RepairRecord();
    public static RepairRecord recExternalRepair = new RepairRecord();

    public static SeaTemperatureRecord recTools = new SeaTemperatureRecord();

    private FruitGlobalState() {
    }

    public static PlantRecord initPlantRecord() {
        recPlant = new PlantRecord();
        return recPlant;
    }

    public static HarvestRecord initHarvestRecord() {
        recHarvest = new HarvestRecord();
        return recHarvest;
    }

    public static StorageRecord initStorageRecord() {
        recStorage = new StorageRecord();
        return recStorage;
    }

    public static ProcessingRecord initProcessingRecord() {
        recProcessing = new ProcessingRecord();
        return recProcessing;
    }

    public static WHTxRecord initWHIncomingRecord() {
        recWHIncoming = new WHTxRecord();
        return recWHIncoming;
    }

    public static WHTxRecord initWHOutgoingRecord() {
        recWHOutgoing = new WHTxRecord();
        return recWHOutgoing;
    }

    public static WHCorrelationRecord initWHCorrelationRecord() {
        recWHCorrelation = new WHCorrelationRecord();
        return recWHCorrelation;
    }

    public static InventoryWHRecord initWHInventoryRecord() {
        recWHInventory = new InventoryWHRecord();
        return recWHInventory;
    }

    public static RepairRecord initInternalRepairRecord() {
        recInternalRepair = new RepairRecord();
        return recInternalRepair;
    }

    public static RepairRecord initExternalRepairRecord() {
        recExternalRepair = new RepairRecord();
        return recExternalRepair;
    }

    public static SeaTemperatureRecord initToolsRecord() {
        recTools = new SeaTemperatureRecord();
        return recTools;
    }

    public static PlantTransaction commitPlanting(MobileDB db, Boolean finalCommit) {
        try {
            PlantTransaction txPlant = new PlantTransaction();

            txPlant.assetRFID = recPlant.poleRFID;
            txPlant.species = recPlant.speciesName;

            db.plantTransactionDAO().update(txPlant);

            return txPlant;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitSemiStorage(MobileDB db) {
        try {
            AssetTransaction txSemiStorage = new AssetTransaction();

            txSemiStorage.collectionLot = recStorage.harvestLot;

            db.assetTransactionDAO().insert(txSemiStorage);
            return txSemiStorage;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ProcessingTransaction commitProcessing(MobileDB db) {
        try {
            ProcessingTransaction txProcess = new ProcessingTransaction();
            txProcess.dispatchNote = recProcessing.dispatchNote;
            txProcess.productCondition = recProcessing.fishCondition;
            txProcess.cleanTruck = Boolean.toString(recProcessing.cleanTruck);
            txProcess.smells = Boolean.toString(recProcessing.smellyTruck);
            txProcess.plot = recProcessing.pLot;
            txProcess.site = recProcessing.packagingSite;
            txProcess.timestamp = System.currentTimeMillis();
            //txProcess.remarks = recProcessing.remarks;
            txProcess.receivedBins = recProcessing.availBins;
            txProcess.securityClipNumber = recProcessing.securityClip;
            txProcess.user = LocalPreferences.getLoggedInUser("N/A");
            txProcess.site = LocalPreferences.getCurrentSiteId().toString();
            txProcess.longitude = recProcessing.longitude;
            txProcess.latitude = recProcessing.latitude;

            db.processingTransactionDAO().insert(txProcess);

            return txProcess;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHRFIDIncoming(MobileDB db) {
        try {
            AssetTransaction txWHIncoming = new AssetTransaction();
            txWHIncoming.state = recWHIncoming.state.name();
            txWHIncoming.assetType = (recWHIncoming.assetType != null) ? recWHIncoming.assetType.name() : ALL.name();
            txWHIncoming.itemRFIDs = recWHIncoming.items;
            txWHIncoming.from = recWHIncoming.from;
            txWHIncoming.to = recWHIncoming.to;
            txWHIncoming.site = recWHIncoming.site;
            txWHIncoming.timestamp = System.currentTimeMillis();
            txWHIncoming.longitude = recWHIncoming.longitude;
            txWHIncoming.latitude = recWHIncoming.latitude;

            db.assetTransactionDAO().insert(txWHIncoming);

            return txWHIncoming;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<ConsumableTransaction> commitWHBarcodeIncoming(MobileDB db) {
        try {
            List<ConsumableTransaction> consumablesList = new ArrayList<>();
            Set<Map.Entry<String, Integer>> barcodeEntries = recWHIncoming.barcodeItems.entrySet();

            for (Map.Entry<String, Integer> entry : barcodeEntries) {
                ConsumableTransaction txWHIncoming = new ConsumableTransaction();

                txWHIncoming.state = recWHIncoming.state.name();
                txWHIncoming.consumableType = (recWHIncoming.consumableType != null) ? recWHIncoming.consumableType.name() : ALL.name();
                txWHIncoming.barcode = entry.getKey();
                txWHIncoming.quantity = entry.getValue();
                txWHIncoming.timestamp = System.currentTimeMillis();
                txWHIncoming.from = recWHIncoming.from;
                txWHIncoming.to = recWHIncoming.to;
                txWHIncoming.site = recWHIncoming.site;
                txWHIncoming.longitude = recWHIncoming.longitude;
                txWHIncoming.latitude = recWHIncoming.latitude;

                consumablesList.add(txWHIncoming);
            }
            db.consumableTransactionDAO().insert(consumablesList.toArray(new ConsumableTransaction[consumablesList.size()]));

            return consumablesList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHRFIDOutgoing(MobileDB db) {
        try {
            AssetTransaction txWHOutgoing = new AssetTransaction();
            txWHOutgoing.state = recWHOutgoing.state.name();
            txWHOutgoing.assetType = (recWHOutgoing.assetType != null) ? recWHOutgoing.assetType.name() : ALL.name();
            txWHOutgoing.itemRFIDs = recWHOutgoing.items;
            txWHOutgoing.from = recWHOutgoing.from;
            txWHOutgoing.to = recWHOutgoing.to;
            txWHOutgoing.site = recWHOutgoing.site;
            txWHOutgoing.timestamp = System.currentTimeMillis();
            txWHOutgoing.longitude = recWHOutgoing.longitude;
            txWHOutgoing.latitude = recWHOutgoing.latitude;

            db.assetTransactionDAO().insert(txWHOutgoing);

            return txWHOutgoing;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<ConsumableTransaction> commitWHBarcodeOutgoing(MobileDB db) {
        try {
            List<ConsumableTransaction> consumablesList = new ArrayList<>();
            Set<Map.Entry<String, Integer>> barcodeEntries = recWHOutgoing.barcodeItems.entrySet();

            for (Map.Entry<String, Integer> entry : barcodeEntries) {
                ConsumableTransaction txWHOutgoing = new ConsumableTransaction();

                txWHOutgoing.state = recWHOutgoing.state.name();
                txWHOutgoing.consumableType = (recWHOutgoing.consumableType != null) ? recWHOutgoing.consumableType.name() : ALL.name();
                txWHOutgoing.barcode = entry.getKey();
                txWHOutgoing.quantity = entry.getValue();
                txWHOutgoing.timestamp = System.currentTimeMillis();
                txWHOutgoing.from = recWHOutgoing.from;
                txWHOutgoing.to = recWHOutgoing.to;
                txWHOutgoing.site = recWHOutgoing.site;
                txWHOutgoing.longitude = recWHOutgoing.longitude;
                txWHOutgoing.latitude = recWHOutgoing.latitude;

                consumablesList.add(txWHOutgoing);
            }
            db.consumableTransactionDAO().insert(consumablesList.toArray(new ConsumableTransaction[consumablesList.size()]));

            return consumablesList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static RFIDInventory commitWHRFIDInventory(MobileDB db) {
        try {
            RFIDInventory txWHRFIDInventory = new RFIDInventory();
            txWHRFIDInventory.site = recWHInventory.subSite;
            txWHRFIDInventory.performedAt = System.currentTimeMillis();
            txWHRFIDInventory.longitude = recWHInventory.longitude;
            txWHRFIDInventory.latitude = recWHInventory.latitude;
            long _id = db.rFIDInventoryDAO().insert(txWHRFIDInventory);
            txWHRFIDInventory.id = _id;

            return txWHRFIDInventory;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<RFIDInventoryItem> commitWHRFIDInventoryItem(MobileDB db, RFIDInventory inventory) {
        try {
            List<RFIDInventoryItem> items = new ArrayList<>();
            Set<Map.Entry<String, List<String>>> inventoryData = recWHInventory.items.entrySet();

            for (Map.Entry<String, List<String>> entry : inventoryData) {
                List<String> epcs = entry.getValue();
                for (String epc : epcs) {
                    RFIDInventoryItem newItem = new RFIDInventoryItem();
                    newItem.assetType = entry.getKey();
                    newItem.itemRFID = epc;
                    newItem.inventory = inventory.id;
                    items.add(newItem);
                }
            }

            db.rFIDInventoryItemDAO().insert(items.toArray(new RFIDInventoryItem[items.size()]));
            return items;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static CoInventory commitWHCoInventory(MobileDB db) {
        try {
            CoInventory txWHCoInventory = new CoInventory();
            txWHCoInventory.site = recWHInventory.subSite;
            txWHCoInventory.performedAt = System.currentTimeMillis();
            txWHCoInventory.longitude = recWHInventory.longitude;
            txWHCoInventory.latitude = recWHInventory.latitude;
            long _id = db.coInventoryDAO().insert(txWHCoInventory);
            txWHCoInventory.id = _id;

            return txWHCoInventory;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<CoInventoryItem> commitWHCoInventoryItem(MobileDB db, CoInventory inventory) {
        try {
            List<CoInventoryItem> items = new ArrayList<>();
            Set<Map.Entry<String, Integer>> inventoryData = recWHInventory.barcodeItems.entrySet();

            for (Map.Entry<String, Integer> entry : inventoryData) {
                CoInventoryItem newItem = new CoInventoryItem();
                newItem.consumableType = (recWHInventory.consumableType != null) ? recWHInventory.consumableType.name() : ALL.name();
                newItem.barcode = entry.getKey();
                newItem.quantity = entry.getValue();
                newItem.timestamp = System.currentTimeMillis();
                newItem.coInventory = inventory.id;

                items.add(newItem);
            }

            db.coInventoryItemDAO().insert(items.toArray(new CoInventoryItem[items.size()]));
            return items;
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
            txCorrelation.timestamp = System.currentTimeMillis();
            txCorrelation.longitude = recWHCorrelation.longitude;
            txCorrelation.latitude = recWHCorrelation.latitude;

            db.correlationTransactionDAO().insert(txCorrelation);

            return txCorrelation;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
