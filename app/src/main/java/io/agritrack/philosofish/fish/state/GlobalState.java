package io.agritrack.philosofish.fish.state;

import static io.agritrack.philosofish.enums.AssetType.ALL;

import android.util.Base64;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.FishingRequest;
import io.agritrack.philosofish.data.model.common.IotLogger;
import io.agritrack.philosofish.data.model.common.Measurement;
import io.agritrack.philosofish.data.model.common.TemperatureData;
import io.agritrack.philosofish.data.model.common.TemperatureTimeSeries;
import io.agritrack.philosofish.data.model.tx.AssetTransaction;
import io.agritrack.philosofish.data.model.tx.AssetTxItem;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.model.tx.FishingTransaction;
import io.agritrack.philosofish.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.ProcessingTransaction;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.data.model.tx.TransportTransaction;
import io.agritrack.philosofish.data.model.wh.RFIDInventory;
import io.agritrack.philosofish.data.model.wh.RFIDInventoryItem;
import io.agritrack.philosofish.enums.TxStatus;
import io.agritrack.philosofish.fish.ui.bo.BinWeightRecord;
import io.agritrack.philosofish.ui.service.LocalPreferences;

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

    public static List<BinInfo> commitBinInfoTx(MobileDB db) {
        try {
            List<BinInfo> binInfos = new ArrayList<>();
            if (recFishing.binWeightRecord.getBinsData() != null) {
                for (BinWeightRecord.BinRecord bin : recFishing.binWeightRecord.getBinsData()) {
                    BinInfo txBinInfo = new BinInfo();
                    txBinInfo.rfid = bin.binEPC;
                    txBinInfo.initedAt = bin.init;
                    txBinInfo.isInit = true;
                    binInfos.add(txBinInfo);
                    db.binInfoDAO().insert(txBinInfo);
                }
            }

            return binInfos;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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
            if (finalCommit) {    //filter empty bins on final commit
                if (recFishing.binWeightRecord.getBinsData() != null) {
                    txFishing.harvestBinsData = recFishing.binWeightRecord.getBinsData()
                            .stream()
                            .filter(x -> x.weight != null && x.weight > 0)
                            .collect(Collectors.toList());
                }
                txFishing.createdAt = System.currentTimeMillis();
            } else {
                txFishing.harvestBinsData = recFishing.binWeightRecord.getBinsData();
            }
            txFishing.reasonOfDeviation = recFishing.reasonOfDeviation;
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

            txTransport.destination = recTransport.packagingSite;
            txTransport.driverName = recTransport.driverName;
            txTransport.driverPhone = recTransport.driverPhone;
            txTransport.truckLicensePlate = recTransport.licensePlate;
            txTransport.securityClipNo = recTransport.clipNumber;
            txTransport.driverSignature = Base64.encodeToString(recTransport.signatureBytes, Base64.NO_WRAP);
            txTransport.isTruckRefrigerated = recTransport.refrigeratedTruck;
            txTransport.isParallelTransport = recTransport.parallelTransport;
            txTransport.transportHead = "N/A";
            txTransport.createdAt = System.currentTimeMillis();
            txTransport.loadedBins = recTransport.availBins;
            txTransport.user = LocalPreferences.getLoggedInUser("N/A");
            txTransport.siteCode = LocalPreferences.getCurrentSiteName();
            txTransport.longitude = recTransport.longitude;
            txTransport.latitude = recTransport.latitude;
            txTransport.capacity = recTransport.capacity;
            txTransport.calcHash();
            recTransport.hashCode = txTransport.hashCode;

            // search DB for records having the same hashCode
            int cnt = db.transportTransactionDAO().countByHash(txTransport.hashCode);

            // Persist record if no duplicates exist
            if (cnt < 1) {
                recTransport.txKey = db.transportTransactionDAO().insert(txTransport);
            } else {
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

//            txProcess.id = recProcessing.txKey;
            txProcess.dispatchNote = recProcessing.dispatchNote;
            txProcess.cleanTruck = Boolean.toString(recProcessing.cleanTruck);
            txProcess.smells = Boolean.toString(recProcessing.smellyTruck);
            txProcess.site = recProcessing.packagingSite;
            txProcess.createdAt = System.currentTimeMillis();
            //txProcess.remarks = recProcessing.remarks;
            txProcess.receivedBins = recProcessing.availBins.stream().map(x -> x.epc).collect(Collectors.toList());
            txProcess.securityClipNumber = recProcessing.securityClip;
            txProcess.user = LocalPreferences.getLoggedInUser("N/A");
            txProcess.site = LocalPreferences.getCurrentSiteName();
            txProcess.longitude = recProcessing.longitude;
            txProcess.latitude = recProcessing.latitude;
            txProcess.isInit = true;
            txProcess.calcHash();

            // search DB for records having the same hashCode
            int cnt = db.processingTransactionDAO().countByHash(txProcess.hashCode);

            // Persist record if no duplicates exist
            if (cnt < 1) {
                db.processingTransactionDAO().insert(txProcess);
                recProcessing.txKey = txProcess.id;
            } else {
                return db.processingTransactionDAO().getByHash(txProcess.hashCode);
            }

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
            if (recQuality.qualityBins != null) {
                txQuality.qualityBins = recQuality.qualityBins.stream().map(x -> x.epc).collect(Collectors.toList());
            }
            txQuality.expectedBins = recQuality.expectedBins;
            txQuality.scannedBins = recQuality.scannedBins;
            txQuality.qualityBinsCnt = recQuality.qualityBinsCnt;
            txQuality.txStatus = Boolean.FALSE.equals(finalCommit) ? TxStatus.PENDING : TxStatus.COMPLETED;
            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.site = LocalPreferences.getCurrentSiteName();
            txQuality.sampleDate = new Date(System.currentTimeMillis());
            if (finalCommit) {
                txQuality.createdAt = System.currentTimeMillis();
            }
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
                    meas.measurement.lot = plot;
                    meas.measurement.surfaceTemp = model.surfaceT;
                    meas.measurement.bottomTemp = model.bottomT;
                    meas.measurement.correctiveAction = model.corrAction;
                    db.measurementsDAO().update(meas.measurement);
                    result.add(meas);
                    continue;
                }
                Measurement measurement = new Measurement();
                measurement.loggerRFID = model.loggerEPC;
                measurement.assetRFID = model.assetEPC;
                measurement.retrievedAt = model.retrievedAt;
                measurement.productionLane = model.productionLane;
                measurement.lot = plot;
                measurement.surfaceTemp = model.surfaceT;
                measurement.bottomTemp = model.bottomT;
                measurement.correctiveAction = model.corrAction;
                measurement.isInit = true;

                db.measurementsDAO().insert(measurement);
                UUID measurementId = measurement.id;

                if (measurementId != null && model.values != null && !model.values.isEmpty()) {
                    List<TemperatureData> data = model.values.stream()
                            .map(x -> new TemperatureData(measurementId, x.getTimeStamp(), Double.valueOf(x.getSample().replace(',', '.'))))
                            .collect(Collectors.toList());
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
            if (meas != null && productionLane == null) {
                return;
            }
            LoggerDataRecord.TemperatureModel model = recLoggerData.data.get(epc);

            Measurement measurement = new Measurement();
            measurement.loggerRFID = model.loggerEPC;
            measurement.assetRFID = model.assetEPC;
            measurement.retrievedAt = model.retrievedAt;
            measurement.productionLane = model.productionLane;
            measurement.surfaceTemp = model.surfaceT;
            measurement.correctiveAction = model.corrAction;
            measurement.bottomTemp = model.bottomT;

            db.measurementsDAO().insert(measurement);
            UUID measurementId = measurement.id;

            if (measurementId != null && model.values != null && !model.values.isEmpty()) {
                List<TemperatureData> data = model.values.stream().map(x -> new TemperatureData(measurementId, x.getTimeStamp(), x.getSample())).collect(Collectors.toList());
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

    public static List<AssetTxItem> commitAssetTxItem(MobileDB db, AssetTransaction assetTx) {
        try {
            List<AssetTxItem> items = new ArrayList<>();
            Set<Map.Entry<String, List<String>>> inventoryData = recWHIncoming.items != null ? recWHIncoming.items.entrySet() : recWHOutgoing.items.entrySet();

            for (Map.Entry<String, List<String>> entry : inventoryData) {
                List<String> epcs = entry.getValue();
                for (String epc : epcs) {
                    AssetTxItem newItem = new AssetTxItem();
                    newItem.assetType = entry.getKey();
                    newItem.itemRFID = epc;
                    newItem.inventory = assetTx.id;
                    items.add(newItem);
                }
            }

            db.assetTxItemDAO().insert(items.toArray(new AssetTxItem[items.size()]));
            return items;
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

    public static RFIDInventory commitWHRFIDInventory(MobileDB db) {
        try {
            RFIDInventory txWHRFIDInventory = new RFIDInventory();
            txWHRFIDInventory.site = recWHInventory.selectedSite;
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

    public static void commitBinInitTimes(MobileDB db) {
        try {
            for (String epc : recLoggerData.loggerInitData.keySet()) {
                Long initTs = recLoggerData.loggerInitData.get(epc);
                BinInfo bin = db.binInfoDAO().getByRFId(epc);
                if (bin == null) {
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
