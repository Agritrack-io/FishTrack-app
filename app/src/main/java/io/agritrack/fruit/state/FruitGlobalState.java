package io.agritrack.fruit.state;

import static io.agritrack.enums.AssetType.ALL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.CoInventoryItem;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.fish.state.RepairRecord;
import io.agritrack.fish.state.SeaTemperatureRecord;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.ui.service.LocalPreferences;

public class FruitGlobalState {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public static PlantRecord recPlant = new PlantRecord();
    public static HarvestRecord recHarvest = new HarvestRecord();
    public static StorageRecord recStorage = new StorageRecord();
    public static PackagingRecord recPackaging = new PackagingRecord();
    public static ShippingRecord recShipping = new ShippingRecord();
    public static CorrelationRecord recCorrelation = new CorrelationRecord();
    public static InventoryRecord recInventory = new InventoryRecord();

    public static WHTxRecord recWHIncoming = new WHTxRecord();
    public static WHTxRecord recWHOutgoing = new WHTxRecord();



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

    public static PackagingRecord initPackagingRecord() {
        recPackaging = new PackagingRecord();
        return recPackaging;
    }

    public static ShippingRecord initShippingRecord() {
        recShipping = new ShippingRecord();
        return recShipping;
    }

    public static CorrelationRecord initCorrelationRecord() {
        recCorrelation = new CorrelationRecord();
        return recCorrelation;
    }

    public static InventoryRecord initInventoryRecord() {
        recInventory = new InventoryRecord();
        return recInventory;
    }

    public static WHTxRecord initWHIncomingRecord() {
        recWHIncoming = new WHTxRecord();
        return recWHIncoming;
    }

    public static WHTxRecord initWHOutgoingRecord() {
        recWHOutgoing = new WHTxRecord();
        return recWHOutgoing;
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

    public static PlantTransaction commitPlanting(MobileDB db) {
        try {
            PlantTransaction txPlant = new PlantTransaction();

            txPlant.assetRFID = recPlant.poleRFID;
            txPlant.site = recPlant.greenhouse;
            txPlant.species = recPlant.speciesName;
            txPlant.userId = LocalPreferences.getLoggedInUser("N/A");
            txPlant.longitude = recPlant.longitude;
            txPlant.latitude = recPlant.latitude;
            txPlant.createdAt = System.currentTimeMillis();

            db.plantTransactionDAO().insert(txPlant);

            return txPlant;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static CollectTransaction commitCollecting(MobileDB db) {
        try {
            CollectTransaction txCollecting = new CollectTransaction();

            txCollecting.assetRFID = recHarvest.poleRFID;
            txCollecting.site = recHarvest.greenhouse;
            txCollecting.species = recHarvest.speciesName;
            txCollecting.userId = LocalPreferences.getLoggedInUser("N/A");
            txCollecting.longitude = recHarvest.longitude;
            txCollecting.latitude = recHarvest.latitude;
            txCollecting.createdAt = System.currentTimeMillis();

            db.collectingTransactionDAO().insert(txCollecting);

            return txCollecting;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static StorageTransaction commitSemiStorage(MobileDB db) {
        try {
            StorageTransaction txSemiStorage = new StorageTransaction();

            txSemiStorage.totesForStorage = recStorage.receivedTotes;
            txSemiStorage.totalWeight = recStorage.totalWeight;
            txSemiStorage.collectionLot = recStorage.harvestLot;
            txSemiStorage.userId = LocalPreferences.getLoggedInUser("N/A");
            txSemiStorage.longitude = recStorage.longitude;
            txSemiStorage.latitude = recStorage.latitude;
            txSemiStorage.createdAt = System.currentTimeMillis();

            db.storageTransactionDAO().insert(txSemiStorage);
            return txSemiStorage;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PackageTransaction commitPackaging(MobileDB db) {
        try {
            PackageTransaction txPackage = new PackageTransaction();

            txPackage.totesForProcess = recPackaging.totesForPackaging;
            txPackage.packagedIfco = recPackaging.packagedIfco;
            txPackage.collectionLot = recPackaging.collectionLot;
            txPackage.userId = LocalPreferences.getLoggedInUser("N/A");
            txPackage.longitude = recPackaging.longitude;
            txPackage.latitude = recPackaging.latitude;
            txPackage.createdAt = System.currentTimeMillis();

            db.packageTransactionDAO().insert(txPackage);

            return txPackage;
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
            txWHRFIDInventory.site = recInventory.subSite;
            txWHRFIDInventory.performedAt = System.currentTimeMillis();
            txWHRFIDInventory.longitude = recInventory.longitude;
            txWHRFIDInventory.latitude = recInventory.latitude;
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
            List<String> inventoryData = recInventory.items;

            /*for (String entry : inventoryData) {
                List<String> epcs = entry.getValue();
                for (String epc : epcs) {
                    RFIDInventoryItem newItem = new RFIDInventoryItem();
                    newItem.assetType = entry.getKey();
                    newItem.itemRFID = epc;
                    newItem.inventory = inventory.id;
                    items.add(newItem);
                }
            }*/

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
            txWHCoInventory.site = recInventory.subSite;
            txWHCoInventory.performedAt = System.currentTimeMillis();
            txWHCoInventory.longitude = recInventory.longitude;
            txWHCoInventory.latitude = recInventory.latitude;
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
            Set<Map.Entry<String, Integer>> inventoryData = recInventory.barcodeItems.entrySet();

            for (Map.Entry<String, Integer> entry : inventoryData) {
                CoInventoryItem newItem = new CoInventoryItem();
                newItem.consumableType = (recInventory.consumableType != null) ? recInventory.consumableType.name() : ALL.name();
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

            db.correlationTransactionDAO().insert(txCorrelation);

            return txCorrelation;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
