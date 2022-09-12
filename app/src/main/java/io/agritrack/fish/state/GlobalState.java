package io.agritrack.fish.state;

import static io.agritrack.enums.AssetType.ALL;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.BinInfo;
import io.agritrack.data.model.FishingRequest;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.data.model.common.Measurement;
import io.agritrack.data.model.common.TemperatureData;
import io.agritrack.data.model.common.TemperatureTimeSeries;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.QualityTransaction;
import io.agritrack.data.model.tx.RepairTransaction;
import io.agritrack.data.model.tx.SeaTemperatureTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.CoInventoryItem;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.enums.TxStatus;
import io.agritrack.ui.service.LocalPreferences;

public class GlobalState {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public static HarvestRecord recHarvest = new HarvestRecord();
    public static FishingRecord recFishing = new FishingRecord();
    public static List<FishingRequest> recFishingRequests = new LinkedList<>();
    public static TransportationRecord recTransport = new TransportationRecord();
    public static ProcessingRecord recProcessing = new ProcessingRecord();
    public static QualityRecord recQuality = new QualityRecord();

    public static WHTxRecord recWHIncoming = new WHTxRecord();
    public static WHTxRecord recWHOutgoing = new WHTxRecord();
    public static WHTxRecord recWHInternal = new WHTxRecord();
    public static WHCorrelationRecord recWHCorrelation = new WHCorrelationRecord();
    public static InventoryWHRecord recWHInventory = new InventoryWHRecord();

    public static RepairRecord recInternalRepair = new RepairRecord();
    public static RepairRecord recExternalRepair = new RepairRecord();

    public static SeaTemperatureRecord recTools = new SeaTemperatureRecord();
    public static LoggerDataRecord recLoggerData = new LoggerDataRecord();
    public static AssetRecord assetData = new AssetRecord();

    private GlobalState() {
    }

    public static List<FishingRequest> initHarvestReq() {
        recFishingRequests = new LinkedList<>();
        return recFishingRequests;
    }

    public static FishingRecord initFishingRecord() {
        recFishing = new FishingRecord();
        return recFishing;
    }

    public static TransportationRecord initTransportationRecord() {
        recTransport = new TransportationRecord();
        return recTransport;
    }

    public static ProcessingRecord initProcessingRecord() {
        recProcessing = new ProcessingRecord();
        return recProcessing;
    }

    public static QualityRecord initQualityRecord() {
        //MobileDB db = MobileDB.getInstance(getAppContext());
        recQuality = new QualityRecord();
        /*List<TemperatureTimeSeries> existingMeasurements = db.measurementsDAO().getAll();
        if (!existingMeasurements.isEmpty()) {
            recQuality.qualityBins = new LinkedList<>();
        }
        for (TemperatureTimeSeries ts : existingMeasurements) {
            Measurement m = ts.measurement;
            List<TemperatureData> _temperatureData = ts.data;
            List<String[]> _dat = new ArrayList<>();
            for (TemperatureData _temperatureD : _temperatureData) {
                _dat.add(_temperatureD.rawData());
            }
            recLoggerData.addDataSet(m.loggerRFID, m.assetRFID, m.productionLane, m.retrievedAt, _dat);
            recQuality.qualityBins.add(m.assetRFID);
        }*/

        return recQuality;
    }

    public static WHTxRecord initWHIncomingRecord() {
        recWHIncoming = new WHTxRecord();
        return recWHIncoming;
    }

    public static WHTxRecord initWHOutgoingRecord() {
        recWHOutgoing = new WHTxRecord();
        return recWHOutgoing;
    }

    public static WHTxRecord initWHInternalRecord() {
        recWHInternal = new WHTxRecord();
        return recWHInternal;
    }

    public static WHCorrelationRecord initWHCorrelationRecord() {
        recWHCorrelation = new WHCorrelationRecord();
        return recWHCorrelation;
    }

    public static InventoryWHRecord initWHInventoryRecord() {
        recWHInventory = new InventoryWHRecord();
        return recWHInventory;
    }

    public static HarvestRecord initHarvestRecord() {
        recHarvest = new HarvestRecord();
        return recHarvest;
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

    public static LoggerDataRecord initLoggerDataRecord() {
        recLoggerData = new LoggerDataRecord();
        return recLoggerData;
    }

    public static FishingTransaction commitFishing(MobileDB db, Boolean finalCommit) {
        try {
            FishingTransaction txFishing = new FishingTransaction();

            txFishing.id = recFishing.txKey;
            txFishing.outOfSystemFishing = recFishing.outOfSystemFishing;
            txFishing.fishingRq = recFishing.fishingRq;
            txFishing.requester = recFishing.requesterName;
            txFishing.platformRFID = recFishing.platformRFID;
            txFishing.cageRFID = recFishing.cageRFID;
            txFishing.cageCode = recFishing.cageCode;
            txFishing.fishType = recFishing.speciesName;
            txFishing.notes = recFishing.notes;
            txFishing.hlot = recFishing.hlot;
            txFishing.parentItinSno = "Split Request".equalsIgnoreCase(recFishing.notes) ? recFishing.parentItinSno : null;
            txFishing.lastFeed = recFishing.lastFed;
            txFishing.averageWeight = String.valueOf(recFishing.averageWeight);
            txFishing.ichthyopathologist = recFishing.pathologist;
            txFishing.packagingPlant = recFishing.packagingPlant;
            txFishing.iceAdequacy = recFishing.adequateIce.toString();
            txFishing.iceSupplier = recFishing.iceSupplier;
            txFishing.availBins = recFishing.availBins;
            txFishing.harvestBinsCnt = recFishing.totalBinsUsed;
            txFishing.orderedQuantity = recFishing.reqWeight != null ? Double.valueOf(recFishing.reqWeight).intValue() : null;
            txFishing.totalQty = recFishing.totalFishWeight;
            txFishing.timestamp = System.currentTimeMillis();
            if(recFishing.binWeightRecord.getBinsData() != null){
                txFishing.harvestBinsData = recFishing.binWeightRecord.getBinsData()
                        .stream()
                        .filter(x -> x.weight != null && x.weight > 0)
                        .collect(Collectors.toList());
            }
            txFishing.team = recFishing.fishingTeam;
            txFishing.txStatus = Boolean.FALSE.equals(finalCommit) ? TxStatus.PENDING : TxStatus.COMPLETED;
            txFishing.user = LocalPreferences.getLoggedInUser("N/A");
            txFishing.site = LocalPreferences.getCurrentSiteName();
            txFishing.longitude = recFishing.longitude;
            txFishing.latitude = recFishing.latitude;

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

            //txTransport.id = recTransport.txKey;
            txTransport.destination = recTransport.packagingSite;
            txTransport.driverName = recTransport.driverName;
            txTransport.driverPhone = recTransport.driverPhone;
            txTransport.truckLicensePlate = recTransport.licensePlate;
            txTransport.securityClipNo = recTransport.clipNumber;
            txTransport.driverSignature = new String(recTransport.signatureBytes, StandardCharsets.UTF_8);
            txTransport.isTruckRefrigerated = recTransport.refrigeratedTruck;
            txTransport.isParallelTransport = recTransport.parallelTransport;
            txTransport.transportHead = "N/A";
            txTransport.timestamp = System.currentTimeMillis();
            txTransport.loadedBins = recTransport.availBins;
            txTransport.user = LocalPreferences.getLoggedInUser("N/A");
            txTransport.siteCode = LocalPreferences.getCurrentSiteName();
            txTransport.longitude = recTransport.longitude;
            txTransport.latitude = recTransport.latitude;
            txTransport.calcHash();

            // search DB for records having the same hashCode
            int cnt = db.transportTransactionDAO().countByHash(txTransport.hashCode);

            // Persist record if no duplicates exist
            if (cnt < 1) {
                recTransport.txKey = db.transportTransactionDAO().insert(txTransport);
            } else {
                recTransport.hashCode = txTransport.hashCode;
                return db.transportTransactionDAO().getByHash(txTransport.hashCode);
            }

            //recTransport.txKey = db.transportTransactionDAO().insert(txTransport);

            return txTransport;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ProcessingTransaction commitProcessing(MobileDB db) {
        try {
            ProcessingTransaction txProcess = new ProcessingTransaction();

            //txProcess.id = recProcessing.txKey;
            txProcess.dispatchNote = recProcessing.dispatchNote;
            txProcess.cleanTruck = Boolean.toString(recProcessing.cleanTruck);
            txProcess.smells = Boolean.toString(recProcessing.smellyTruck);
            txProcess.site = recProcessing.packagingSite;
            txProcess.timestamp = System.currentTimeMillis();
            //txProcess.remarks = recProcessing.remarks;
            txProcess.receivedBins = recProcessing.availBins.stream().map(x -> x.epc).collect(Collectors.toList());
            txProcess.securityClipNumber = recProcessing.securityClip;
            txProcess.user = LocalPreferences.getLoggedInUser("N/A");
            txProcess.site = LocalPreferences.getCurrentSiteName();
            txProcess.longitude = recProcessing.longitude;
            txProcess.latitude = recProcessing.latitude;
            txProcess.calcHash();

            // search DB for records having the same hashCode
            int cnt = db.processingTransactionDAO().countByHash(txProcess.hashCode);

            // Persist record if no duplicates exist
            if (cnt < 1) {
                recProcessing.txKey = db.processingTransactionDAO().insert(txProcess);
            } else {
                return db.processingTransactionDAO().getByHash(txProcess.hashCode);
            }

            //recProcessing.txKey = db.processingTransactionDAO().insert(txProcess);

            return txProcess;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static QualityTransaction commitQuality(MobileDB db, Boolean finalCommit) {
        try {
            QualityTransaction txQuality = new QualityTransaction();

            txQuality.id = recQuality.txKey;
            txQuality.plot = recQuality.pLot;
            txQuality.iceCondition = recQuality.iceCondition;
            txQuality.binCondition = recQuality.binCondition;
            txQuality.smellCondition = recQuality.smellCondition;
            txQuality.minBinTemp = recQuality.minBinTemp;
            txQuality.avgBinTemp = recQuality.meanBinTemp;
            txQuality.maxBinTemp = recQuality.maxBinTemp;
            txQuality.minFishTemp = recQuality.minFishTemp;
            txQuality.avgFishTemp = recQuality.meanFishTemp;
            txQuality.maxFishTemp = recQuality.maxFishTemp;
            txQuality.rigorMortis = recQuality.rigorMortis;
            txQuality.eliminationFood = recQuality.eliminationFood;
            txQuality.eliminationSperm = recQuality.eliminationSperm;
            txQuality.parasites = recQuality.parasites;
            txQuality.peeling = recQuality.peeling;
            txQuality.shiny = recQuality.shiny;
            txQuality.blurred = recQuality.blurred;
            txQuality.healed = recQuality.healed;
            txQuality.blindEyes = recQuality.blindEyes;
            txQuality.coherent = recQuality.coherent;
            txQuality.soft = recQuality.soft;
            txQuality.swollen = recQuality.swollen;
            txQuality.noHematoma = recQuality.noHematoma;
            txQuality.lightHematoma = recQuality.lightHematoma;
            txQuality.heavyHematoma = recQuality.heavyHematoma;
            txQuality.pink = recQuality.pink;
            txQuality.dark = recQuality.dark;
            txQuality.white = recQuality.white;
            txQuality.uncolored = recQuality.uncolored;
            txQuality.hematomas = recQuality.hematomas;
            txQuality.mucus = recQuality.mucus;
            txQuality.problematicFish = recQuality.problematicFish;
            txQuality.overallEvaluation = recQuality.evaluation;
            txQuality.selectedRgId = recQuality.selectedRgId;
            txQuality.remarks = recQuality.remarks;
            txQuality.qualityBins = recQuality.qualityBins;
            txQuality.qualityBinsCnt = recQuality.qualityBinsCnt;
            txQuality.txStatus = Boolean.FALSE.equals(finalCommit) ? TxStatus.PENDING : TxStatus.COMPLETED;
            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.site = LocalPreferences.getCurrentSiteName();
            txQuality.sampleDate = new Date(System.currentTimeMillis());
            txQuality.timestamp = System.currentTimeMillis();
            txQuality.longitude = recQuality.longitude;
            txQuality.latitude = recQuality.latitude;

            db.qualityTransactionDAO().update(txQuality);

            return txQuality;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PostPackageQualityTransaction commitPostPackageQuality(MobileDB db) {
        try {
            PostPackageQualityTransaction txQuality = new PostPackageQualityTransaction();

            //txQuality.id = recQuality.txKey;
            txQuality.plot = recQuality.pLot;
            txQuality.boxSn = recQuality.boxSn;
            txQuality.tempT1 = recQuality.etT1;
            txQuality.tempT2 = recQuality.etT2;
            txQuality.tempT3 = recQuality.etT3;
            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.site = LocalPreferences.getCurrentSiteName();
            txQuality.sampleDate = recQuality.timestamp;
            txQuality.longitude = recQuality.longitude;
            txQuality.latitude = recQuality.latitude;

            recQuality.txKey = db.postPackageQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<TemperatureTimeSeries> commitMeasurements(MobileDB db, String plot) {
        List<TemperatureTimeSeries> result = new ArrayList<>();

        try {
            for (String epc : recLoggerData.data.keySet()) {
                LoggerDataRecord.TemperatureModel model = recLoggerData.data.get(epc);
                TemperatureTimeSeries meas = db.measurementsDAO().getByEPC(epc);
                if (meas != null) {
                    if (meas.measurement.productionLane==null) {
                        meas.measurement.lot = plot;
                        db.measurementsDAO().update(meas.measurement);
                    }
                    result.add(meas);
                    continue;
                }
                Measurement measurement = new Measurement();
                measurement.loggerRFID = model.loggerEPC;
                measurement.assetRFID = model.assetEPC;
                measurement.retrievedAt = model.retrievedAt;
                measurement.productionLane = model.productionLane;
                measurement.lot = plot;

                long measurementId = db.measurementsDAO().insert(measurement);
                if (measurementId > 0 && model.values != null && !model.values.isEmpty()) {
                    List<TemperatureData> data = model.values.stream().map(x -> new TemperatureData(measurementId, x[0], Double.valueOf(x[1].replace(',', '.')))).collect(Collectors.toList());
                    db.temperatureDataDAO().insert(data.toArray(new TemperatureData[data.size()]));
                }
                //TODO:: can't we get it directly from the insert statement?
                result.add(db.measurementsDAO().getById(measurementId));
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void commitMeasurement(MobileDB db, String epc, String productionLane) {

        try {
            TemperatureTimeSeries meas = db.measurementsDAO().getByEPC(epc);
            if (meas != null && productionLane==null) {
                return;
            }
            LoggerDataRecord.TemperatureModel model = recLoggerData.data.get(epc);

            Measurement measurement = new Measurement();
            measurement.loggerRFID = model.loggerEPC;
            measurement.assetRFID = model.assetEPC;
            measurement.retrievedAt = model.retrievedAt;
            measurement.productionLane = model.productionLane;

            long measurementId = db.measurementsDAO().insert(measurement);
            if (measurementId > 0 && model.values != null && !model.values.isEmpty()) {
                List<TemperatureData> data = model.values.stream().map(x -> new TemperatureData(measurementId, x[0], Double.valueOf(x[1].replace(',', '.')))).collect(Collectors.toList());
                db.temperatureDataDAO().insert(data.toArray(new TemperatureData[data.size()]));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static AssetTransaction commitWHRFIDIncoming(MobileDB db) {
        try {
            AssetTransaction txWHIncoming = new AssetTransaction();

            txWHIncoming.state = recWHIncoming.state.name();
            txWHIncoming.assetType = (recWHIncoming.assetType != null) ? recWHIncoming.assetType : ALL;
            txWHIncoming.itemRFIDs = recWHIncoming.items;
            txWHIncoming.fromSite = recWHIncoming.fromSite;
            txWHIncoming.toSite = recWHIncoming.toSite;
            txWHIncoming.site = recWHIncoming.site;
            txWHIncoming.timestamp = System.currentTimeMillis();
            txWHIncoming.userId = LocalPreferences.getLoggedInUser("N/A");
            txWHIncoming.longitude = recWHIncoming.longitude;
            txWHIncoming.latitude = recWHIncoming.latitude;

            recWHIncoming.txKey = db.assetTransactionDAO().insert(txWHIncoming);

            return txWHIncoming;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public static AssetTransaction commitWHRFIDOutgoing(MobileDB db) {
        try {
            AssetTransaction txWHOutgoing = new AssetTransaction();
            txWHOutgoing.state = recWHOutgoing.state.name();
            txWHOutgoing.assetType = (recWHOutgoing.assetType != null) ? recWHOutgoing.assetType : ALL;
            txWHOutgoing.itemRFIDs = recWHOutgoing.items;
            txWHOutgoing.fromSite = recWHOutgoing.fromSite;
            txWHOutgoing.toSite = recWHOutgoing.toSite;
            txWHOutgoing.site = recWHOutgoing.site;
            txWHOutgoing.timestamp = System.currentTimeMillis();
            txWHOutgoing.userId = LocalPreferences.getLoggedInUser("N/A");
            txWHOutgoing.longitude = recWHOutgoing.longitude;
            txWHOutgoing.latitude = recWHOutgoing.latitude;

            db.assetTransactionDAO().insert(txWHOutgoing);

            return txWHOutgoing;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHRFIDInternal(MobileDB db) {
        try {
            AssetTransaction txWHInternal = new AssetTransaction();
            txWHInternal.state = recWHInternal.state.name();
            txWHInternal.assetType = (recWHInternal.assetType != null) ? recWHInternal.assetType : ALL;
            txWHInternal.itemRFIDs = recWHInternal.items;
            txWHInternal.fromSite = recWHInternal.fromSite;
            txWHInternal.toSite = recWHInternal.toSite;
            txWHInternal.fromAsset = recWHInternal.fromAsset;
            txWHInternal.toAsset = recWHInternal.toAsset;
            txWHInternal.site = recWHInternal.site;
            txWHInternal.timestamp = System.currentTimeMillis();
            txWHInternal.userId = LocalPreferences.getLoggedInUser("N/A");
            txWHInternal.longitude = recWHInternal.longitude;
            txWHInternal.latitude = recWHInternal.latitude;

            recWHInternal.txKey = db.assetTransactionDAO().insert(txWHInternal);

            return txWHInternal;
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
                txWHOutgoing.consumableType = (recWHOutgoing.consumableType != null) ? recWHOutgoing.consumableType.name() : ALL;
                txWHOutgoing.barcode = entry.getKey();
                txWHOutgoing.quantity = entry.getValue();
                txWHOutgoing.timestamp = System.currentTimeMillis();
                txWHOutgoing.from = recWHOutgoing.fromSite;
                txWHOutgoing.to = recWHOutgoing.toSite;
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
            txWHRFIDInventory.rfidInvType = "BLIND";
            txWHRFIDInventory.latitude = recWHInventory.latitude;
            long _id = db.rFIDInventoryDAO().insert(txWHRFIDInventory);
            txWHRFIDInventory.id = _id;
            txWHRFIDInventory.user = LocalPreferences.getLoggedInUser("N/A");

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
                newItem.consumableType = (recWHInventory.consumableType != null) ? recWHInventory.consumableType.name() : ALL;
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
            txCorrelation.assetType = recWHCorrelation.assetType;
            txCorrelation.assetRFID = recWHCorrelation.assetRFID;
            txCorrelation.assetCode = recWHCorrelation.assetCode;
            txCorrelation.rfid = recWHCorrelation.rfid;
            txCorrelation.type = recWHCorrelation.type;
            txCorrelation.code = recWHCorrelation.code;
            txCorrelation.timestamp = System.currentTimeMillis();
            txCorrelation.longitude = recWHCorrelation.longitude;
            txCorrelation.latitude = recWHCorrelation.latitude;
            txCorrelation.site = LocalPreferences.getCurrentSiteId();

            recWHCorrelation.txKey = db.correlationTransactionDAO().insert(txCorrelation);

            return txCorrelation;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static RepairTransaction commitInternalRepair(MobileDB db) {
        try {
            RepairTransaction txIndoorsRepair = new RepairTransaction();
            txIndoorsRepair.timestamp = System.currentTimeMillis();
            txIndoorsRepair.longitude = recInternalRepair.longitude;
            txIndoorsRepair.latitude = recInternalRepair.latitude;

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
            txOutdoorsRepair.timestamp = System.currentTimeMillis();
            txOutdoorsRepair.longitude = recExternalRepair.longitude;
            txOutdoorsRepair.latitude = recExternalRepair.latitude;

            db.repairTransactionDAO().insert(txOutdoorsRepair);

            return txOutdoorsRepair;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SeaTemperatureTransaction commitSeaTemp(MobileDB db) {
        try {
            SeaTemperatureTransaction seaTemperatureTransaction = new SeaTemperatureTransaction();
            seaTemperatureTransaction.timestamp = System.currentTimeMillis() / 1000L;
            seaTemperatureTransaction.siteId = LocalPreferences.getCurrentSiteName();
            seaTemperatureTransaction.refTemp = recTools.referencePointTemp;
            seaTemperatureTransaction.cageTemp = recTools.cageTemp;
            seaTemperatureTransaction.longitude = recTools.longitude;
            seaTemperatureTransaction.latitude = recTools.latitude;

            recTools.txKey = db.seaTemperatureTransactionDAO().insert(seaTemperatureTransaction);

            return seaTemperatureTransaction;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void commitBinInitTimes(MobileDB db) {
        try {
            for(String epc : recLoggerData.loggerInitData.keySet()){
                Long initTs = recLoggerData.loggerInitData.get(epc);
                BinInfo bin = db.binInfoDAO().getByRFId(epc);
                if (bin == null){
                    bin = new BinInfo();
                    bin.rfid = epc;
                }
                bin.initedAt = initTs;
                db.binInfoDAO().update(bin);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<ConsumableTransaction> commitWHBarcodeIncoming(MobileDB db) {
        try {
            List<ConsumableTransaction> consumablesList = new ArrayList<>();
            Set<Map.Entry<String, Integer>> barcodeEntries = recWHIncoming.barcodeItems.entrySet();

            for (Map.Entry<String, Integer> entry : barcodeEntries) {
                ConsumableTransaction txWHIncoming = new ConsumableTransaction();

                txWHIncoming.state = recWHIncoming.state.name();
                txWHIncoming.consumableType = (recWHIncoming.consumableType != null) ? recWHIncoming.consumableType.name() : ALL;
                txWHIncoming.barcode = entry.getKey();
                txWHIncoming.quantity = entry.getValue();
                txWHIncoming.timestamp = System.currentTimeMillis();
                txWHIncoming.from = recWHIncoming.fromSite;
                txWHIncoming.to = recWHIncoming.toSite;
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

    public static List<IotLogger> commitIotLoggers(MobileDB db) {
        try {
            List<IotLogger> iotLoggerList = new ArrayList<>();
            List<String> iotLoggers = assetData.loggers;
            for (String iotLogger : iotLoggers) {
                IotLogger iot = new IotLogger();

                iot.model = assetData.model;
                iot.type = assetData.type;
                iot.vendor = assetData.vendor;
                iot.rfid = iotLogger;

                iotLoggerList.add(iot);
            }
            db.iotLoggerDAO().insert(iotLoggerList.toArray(new IotLogger[iotLoggerList.size()]));

            return iotLoggerList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
