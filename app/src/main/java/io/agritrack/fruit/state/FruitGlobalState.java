package io.agritrack.fruit.state;

import static io.agritrack.enums.AssetType.ALL;

import com.google.android.gms.common.util.CollectionUtils;

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
import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.data.model.tx.ShippingTransaction;
import io.agritrack.data.model.tx.TotesTransaction;
import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.tx.items.CollectionTxWithItems;
import io.agritrack.data.model.tx.items.PackageTxWithItems;
import io.agritrack.data.model.tx.items.ShippingTxWithItems;
import io.agritrack.data.model.tx.items.StorageTxWithItems;
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

    public static CollectionTxWithItems commitCollecting(MobileDB db) {
        try {
            CollectTransaction txCollecting = new CollectTransaction();

            txCollecting.assetRFID = recHarvest.poleRFID;
            txCollecting.site = recHarvest.greenhouse;
            txCollecting.collectionLot = recHarvest.harvestLot;
            txCollecting.species = recHarvest.speciesName;
            txCollecting.totesCnt = recHarvest.totalTotesUsed;
            txCollecting.userId = LocalPreferences.getLoggedInUser("N/A");
            txCollecting.longitude = recHarvest.longitude;
            txCollecting.latitude = recHarvest.latitude;
            txCollecting.createdAt = System.currentTimeMillis();

            Long[] ids = db.collectingTransactionDAO().insert(txCollecting);
            if (!CollectionUtils.isEmpty(recHarvest.totes)) {
                TotesTransaction[] items = new TotesTransaction[recHarvest.totes.size()];
                int i =0;
                for (String tote : recHarvest.totes) {
                    TotesTransaction itemTx = new TotesTransaction();
                    itemTx.collectionTxId = ids[0];
                    itemTx.epc = tote;
                    items[i] = itemTx;
                    i++;
                }
                db.totesTransactionDAO().insert(items);
            }

            return db.collectingTransactionDAO().getById(ids[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static StorageTxWithItems commitSemiStorage(MobileDB db) {
        try {
            StorageTransaction txSemiStorage = new StorageTransaction();

            txSemiStorage.totesCnt = recStorage.totalTotesReceived;
            txSemiStorage.totalWeight = recStorage.totalWeight;
            txSemiStorage.collectionLot = recStorage.harvestLot;
            txSemiStorage.site = LocalPreferences.getCurrentSiteName();
            txSemiStorage.category = recStorage.category.name();
            txSemiStorage.to = recStorage.warehouse;
            txSemiStorage.user = LocalPreferences.getLoggedInUser("N/A");
            txSemiStorage.longitude = recStorage.longitude;
            txSemiStorage.latitude = recStorage.latitude;
            txSemiStorage.createdAt = System.currentTimeMillis();

            Long[] ids = db.storageTransactionDAO().insert(txSemiStorage);
            if (!CollectionUtils.isEmpty(recStorage.receivedTotes)) {
                TotesTransaction[] items = new TotesTransaction[recStorage.receivedTotes.size()];
                int i =0;
                for (String tote : recStorage.receivedTotes) {
                    TotesTransaction itemTx = new TotesTransaction();
                    itemTx.storageTxId = ids[0];
                    itemTx.epc = tote;
                    items[i] = itemTx;
                    i++;
                }
                db.totesTransactionDAO().insert(items);
            }

            return db.storageTransactionDAO().getById(ids[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PackageTxWithItems commitPackaging(MobileDB db) {
        try {
            PackageTransaction txPackage = new PackageTransaction();

            txPackage.totesCnt = recPackaging.totalTotesForPackaging;
            txPackage.ifcoCnt = recPackaging.totalPackagedIfco;
            txPackage.collectionLot = recPackaging.collectionLot;
            txPackage.user = LocalPreferences.getLoggedInUser("N/A");
            txPackage.longitude = recPackaging.longitude;
            txPackage.latitude = recPackaging.latitude;
            txPackage.createdAt = System.currentTimeMillis();

            Long[] ids = db.packageTransactionDAO().insert(txPackage);
            if (!CollectionUtils.isEmpty(recPackaging.totesForPackaging)) {
                TotesTransaction[] totes = new TotesTransaction[recPackaging.totesForPackaging.size()];
                int i =0;
                for (String tote : recPackaging.totesForPackaging) {
                    TotesTransaction itemTx = new TotesTransaction();
                    itemTx.packageTxId = ids[0];
                    itemTx.epc = tote;
                    totes[i] = itemTx;
                    i++;
                }
                db.totesTransactionDAO().insert(totes);
            }

            if (!CollectionUtils.isEmpty(recPackaging.packagedIfco)) {
                IfcoTransaction[] ifcos = new IfcoTransaction[recPackaging.packagedIfco.size()];
                int i =0;
                for (String ifco : recPackaging.packagedIfco) {
                    IfcoTransaction itemTx = new IfcoTransaction();
                    itemTx.packageTxId = ids[0];
                    itemTx.barcode = ifco;
                    ifcos[i] = itemTx;
                    i++;
                }
                db.ifcoTransactionDAO().insert(ifcos);
            }

            return db.packageTransactionDAO().getById(ids[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static StorageTxWithItems commitReadyStorage(MobileDB db) {
        try {
            StorageTransaction txReadyStorage = new StorageTransaction();

            txReadyStorage.ifcoCnt = recStorage.totalIfcoCnt;
            txReadyStorage.collectionLot = recStorage.harvestLot;
            txReadyStorage.category = recStorage.category.name();
            txReadyStorage.to = recStorage.warehouse;
            txReadyStorage.user = LocalPreferences.getLoggedInUser("N/A");
            txReadyStorage.longitude = recStorage.longitude;
            txReadyStorage.latitude = recStorage.latitude;
            txReadyStorage.createdAt = System.currentTimeMillis();

            Long[] ids = db.storageTransactionDAO().insert(txReadyStorage);
            if (!CollectionUtils.isEmpty(recStorage.packagedIfco)) {
                IfcoTransaction[] items = new IfcoTransaction[recStorage.packagedIfco.size()];
                int i =0;
                for (String ifco : recStorage.packagedIfco) {
                    IfcoTransaction itemTx = new IfcoTransaction();
                    itemTx.storageTxId = ids[0];
                    itemTx.barcode = ifco;
                    items[i] = itemTx;
                    i++;
                }
                db.ifcoTransactionDAO().insert(items);
            }

            return db.storageTransactionDAO().getById(ids[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ShippingTxWithItems commitShipping(MobileDB db) {
        try {
            ShippingTransaction txShipping = new ShippingTransaction();

            txShipping.driverName = recShipping.driverName;
            txShipping.truckLicensePlate = recShipping.licensePlate;
            txShipping.timestamp = System.currentTimeMillis();
            txShipping.user = LocalPreferences.getLoggedInUser("N/A");
            txShipping.customer = recShipping.customer;
            txShipping.ifcoCnt = recShipping.totalIfcoCnt;
            txShipping.longitude = recShipping.longitude;
            txShipping.latitude = recShipping.latitude;

            Long[] ids = db.shippingTransactionDAO().insert(txShipping);
            if (!CollectionUtils.isEmpty(recShipping.packagedIfco)) {
                IfcoTransaction[] items = new IfcoTransaction[recShipping.packagedIfco.size()];
                int i =0;
                for (String ifco : recShipping.packagedIfco) {
                    IfcoTransaction itemTx = new IfcoTransaction();
                    itemTx.shippingTxId = ids[0];
                    itemTx.barcode = ifco;
                    items[i] = itemTx;
                    i++;
                }
                db.ifcoTransactionDAO().insert(items);
            }

            return db.shippingTransactionDAO().getById(ids[0]);
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
            txWHRFIDInventory.user = LocalPreferences.getLoggedInUser("N/A");
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
            List<String> inventoryData = recInventory.totesItems;

            for (String entry : inventoryData) {
                    RFIDInventoryItem newItem = new RFIDInventoryItem();

                    newItem.itemRFID = entry;
                    newItem.assetType = (recInventory.assetType != null) ? recInventory.assetType.name() : null;
                    newItem.inventory = inventory.id;
                    items.add(newItem);
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
            txWHCoInventory.site = recInventory.subSite;
            txWHCoInventory.user = LocalPreferences.getLoggedInUser("N/A");
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
            List<String> inventoryData = recInventory.ifcoItems;

            for (String entry : inventoryData) {
                CoInventoryItem newItem = new CoInventoryItem();
                newItem.consumableType = (recInventory.consumableType != null) ? recInventory.consumableType.name() : null;
                newItem.barcode = entry;
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

            txCorrelation.site = recCorrelation.site;
            txCorrelation.assetType = (recCorrelation.assetType != null) ? recCorrelation.assetType.name() : null;
            txCorrelation.assetRFID = recCorrelation.loggerRFID;
            txCorrelation.parentType = (recCorrelation.parentType != null) ? recCorrelation.parentType.name() : null;
            txCorrelation.parentRFID = recCorrelation.poleRFID;
            txCorrelation.site = recCorrelation.site;
            txCorrelation.user = LocalPreferences.getLoggedInUser("N/A");
            txCorrelation.timestamp = System.currentTimeMillis();
            txCorrelation.longitude = recCorrelation.longitude;
            txCorrelation.latitude = recCorrelation.latitude;

            db.correlationTransactionDAO().insert(txCorrelation);

            return txCorrelation;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
