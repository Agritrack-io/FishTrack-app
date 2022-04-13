package io.agritrack.fish.state;

import static io.agritrack.enums.AssetType.ALL;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.HarvestRequest;
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
    public static List<HarvestRequest> recHarvestRequests = new LinkedList<>();
    public static TransportationRecord recTransport = new TransportationRecord();
    public static ProcessingRecord recProcessing = new ProcessingRecord();
    public static QualityRecord recQuality = new QualityRecord();

    public static WHTxRecord recWHIncoming = new WHTxRecord();
    public static WHTxRecord recWHOutgoing = new WHTxRecord();
    public static WHCorrelationRecord recWHCorrelation = new WHCorrelationRecord();
    public static InventoryWHRecord recWHInventory = new InventoryWHRecord();

    public static RepairRecord recInternalRepair = new RepairRecord();
    public static RepairRecord recExternalRepair = new RepairRecord();

    public static SeaTemperatureRecord recTools = new SeaTemperatureRecord();
    public static LoggerDataRecord recLoggerData = new LoggerDataRecord();
    public static AssetRecord assetData = new AssetRecord();

    private GlobalState() {
    }

    public static List<HarvestRequest> initHarvestReq() {
        recHarvestRequests = new LinkedList<>();
        return recHarvestRequests;
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
        recQuality = new QualityRecord();
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
            txFishing.harvestRq = recFishing.harvestRq;
            txFishing.platformRFID = recFishing.platformRFID;
            txFishing.cageRFID = recFishing.cageRFID;
            txFishing.cageCode = recFishing.cageCode;
            txFishing.netRFID = recFishing.netRFID;
            txFishing.fishType = recFishing.speciesName;
            txFishing.fishSize = recFishing.fishSize;
            txFishing.ichthyopathologist = recFishing.pathologist;
            txFishing.packagingPlant = recFishing.packagingPlant;
            txFishing.lastFeed = recFishing.lastFed;
            txFishing.hlot = recFishing.hlot;
            txFishing.iceAdequacy = recFishing.adequateIce.toString();
            txFishing.iceSupplier = recFishing.iceSupplier;
            txFishing.seaTemperature = recFishing.seaTemperature;
            txFishing.harvestBinsCnt = recFishing.totalBinsUsed;
            txFishing.orderedQuantity = recFishing.reqWeight != null ? Double.valueOf(recFishing.reqWeight).intValue() : null;
            txFishing.requester = recFishing.requesterName;
            txFishing.totalQty = recFishing.totalFishWeight;
            txFishing.timestamp = System.currentTimeMillis();
            txFishing.harvestBinsData = recFishing.binWeightRecord.getBins();//.toJSONText();
            txFishing.team = recFishing.fishingTeam;
            txFishing.txStatus = Boolean.FALSE.equals(finalCommit) ? TxStatus.PENDING : TxStatus.COMPLETED;
            txFishing.tempData = recFishing.binTemperatureRecord.toJSONText();
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
            txProcess.cleanTruck = Boolean.toString(recProcessing.cleanTruck);
            txProcess.smells = Boolean.toString(recProcessing.smellyTruck);
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

    public static QualityTransaction commitQuality(MobileDB db) {
        try {
            QualityTransaction txQuality = new QualityTransaction();
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
            txQuality.remarks = recQuality.remarks;
            txQuality.qualityBins = recQuality.qualityBins;
            txQuality.noQualityBins = recQuality.noQualityBins;
            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.site = LocalPreferences.getCurrentSiteName();
            txQuality.timestamp = System.currentTimeMillis();
            txQuality.state = recQuality.state;
            txQuality.longitude = recQuality.longitude;
            txQuality.latitude = recQuality.latitude;

            db.qualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PostPackageQualityTransaction commitPostPackageQuality(MobileDB db) {
        try {
            PostPackageQualityTransaction txQuality = new PostPackageQualityTransaction();
            txQuality.plot = recQuality.pLot;
            txQuality.boxSn = recQuality.boxSn;
            txQuality.tempT1 = recQuality.etT1;
            txQuality.tempT2 = recQuality.etT2;
            txQuality.tempT3 = recQuality.etT3;
            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.site = LocalPreferences.getCurrentSiteName();
            txQuality.timestamp = recQuality.timestamp;
            txQuality.longitude = recQuality.longitude;
            txQuality.latitude = recQuality.latitude;

            db.postPackageQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<TemperatureTimeSeries> commitMeasurements(MobileDB db) {
        List<TemperatureTimeSeries> result = new ArrayList<>();
        try {
            for(String epc : recLoggerData.data.keySet()) {
                LoggerDataRecord.TemperatureModel model = recLoggerData.data.get(epc);
                
                Measurement measurement = new Measurement();
                measurement.loggerRFID = model.loggerEPC;
                measurement.retrievedAt = model.retrievedAt;
                measurement.enabledAt = model.enabledAt;

                long measurementId = db.measurementsDAO().insert(measurement);
                if (measurementId > 0 && model.values != null && !model.values.isEmpty()) {
                    List<TemperatureData> data = model.values.stream().map(x -> new TemperatureData(measurementId, x[0], Double.valueOf(x[1].replace(',', '.')))).collect(Collectors.toList());
                    db.temperatureDataDAO().insert(data.toArray(new TemperatureData[data.size()]));
                }

                result.add(db.measurementsDAO().getById(measurementId));
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AssetTransaction commitWHRFIDIncoming(MobileDB db) {
        try {
            AssetTransaction txWHIncoming = new AssetTransaction();
            txWHIncoming.state = recWHIncoming.state.name();
            txWHIncoming.assetType = (recWHIncoming.assetType != null) ? recWHIncoming.assetType : ALL;
            txWHIncoming.itemRFIDs = recWHIncoming.items;
            txWHIncoming.from = recWHIncoming.from;
            txWHIncoming.to = recWHIncoming.to;
            txWHIncoming.site = recWHIncoming.site;
            txWHIncoming.timestamp = System.currentTimeMillis();
            txWHIncoming.userId = LocalPreferences.getLoggedInUser("N/A");
            txWHIncoming.longitude = recWHIncoming.longitude;
            txWHIncoming.latitude = recWHIncoming.latitude;

            db.assetTransactionDAO().insert(txWHIncoming);

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
            txWHOutgoing.from = recWHOutgoing.from;
            txWHOutgoing.to = recWHOutgoing.to;
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
            txCorrelation.barcode = recWHCorrelation.barcode;
            txCorrelation.assetRFID = recWHCorrelation.rfid;
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
            seaTemperatureTransaction.timestamp = System.currentTimeMillis();
            seaTemperatureTransaction.siteName = LocalPreferences.getCurrentSiteName();
            seaTemperatureTransaction.siteId = LocalPreferences.getCurrentSiteId();
            seaTemperatureTransaction.refTemp = recTools.referencePointTemp;
            seaTemperatureTransaction.cageTemp = recTools.cageTemp;
            seaTemperatureTransaction.longitude = recTools.longitude;
            seaTemperatureTransaction.latitude = recTools.latitude;

            db.seaTemperatureTransactionDAO().insert(seaTemperatureTransaction);

            return seaTemperatureTransaction;
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
                txWHIncoming.consumableType = (recWHIncoming.consumableType != null) ? recWHIncoming.consumableType.name() : ALL;
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
