package io.agritrack.philosofish.fish.state;

import static io.agritrack.philosofish.enums.AssetType.ALL;

import android.util.Base64;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
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
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.FishingTransaction;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.ProcessingTransaction;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
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
    public static ReceiptQualityRecord recQualityReceipt = new ReceiptQualityRecord();
    public static PackageQualityRecord recQualityPackage = new PackageQualityRecord();
    public static FinalQualityRecord recQualityFinal = new FinalQualityRecord();
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

    public static ReceiptQualityRecord initReceiptQualityRecord() {
        recQualityReceipt  = new ReceiptQualityRecord();

        return recQualityReceipt;
    }

    public static FinalQualityRecord initFinalQualityRecord() {
        recQualityFinal  = new FinalQualityRecord();

        return recQualityFinal;
    }

    public static PackageQualityRecord initPackageQualityRecord() {
        recQualityPackage  = new PackageQualityRecord();

        return recQualityPackage;
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

    public static ReceiptQualityTransaction commitReceiptQuality(MobileDB db, Boolean finalCommit) {
        try {
            ReceiptQualityTransaction txQuality = db.receiptQualityTransactionDAO().getByLot(recQualityReceipt.lot);
            if (txQuality == null) {
                txQuality = new ReceiptQualityTransaction();
            }
            DateFormat formatter = new SimpleDateFormat("HH:mm");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            txQuality.lot = recQualityReceipt.lot;
            try {
                txQuality.arrivalTime = new Time(formatter.parse(recQualityReceipt.arrivalTime).getTime());
            } catch (Exception e) {
                txQuality.arrivalTime = null;
            }
            try {
                txQuality.startTime = new Time(formatter.parse(recQualityReceipt.startTime).getTime());
            } catch (Exception e) {
                txQuality.startTime = null;
            }
            try {
                txQuality.fishingDate = dateFormat.parse(recQualityReceipt.fishingDate);
            } catch (Exception e) {
                txQuality.fishingDate = null;
            }

            txQuality.user = LocalPreferences.getLoggedInUser("N/A");
            txQuality.plant = LocalPreferences.getCurrentSiteName();
            txQuality.farm = recQualityReceipt.farm;
            txQuality.cage = recQualityReceipt.cage;
            txQuality.species = recQualityReceipt.fishSpecies;
            txQuality.sealed = recQualityReceipt.binSeal;
            txQuality.eyeRating = recQualityReceipt.eyeRating;
            txQuality.gillRating = recQualityReceipt.gillRating;
            txQuality.fleshRating = recQualityReceipt.fleshRating;
            txQuality.skinRating = recQualityReceipt.skinRating;
            txQuality.disEyes = recQualityReceipt.disEyes;
            txQuality.disTail = recQualityReceipt.disTail;
            txQuality.disSkeletal = recQualityReceipt.disSkeletal;
            txQuality.disBlood = recQualityReceipt.disBlood;
            txQuality.disMouth = recQualityReceipt.disMouth;
            txQuality.disOper = recQualityReceipt.disOper;
            txQuality.comments = recQualityReceipt.comments;

            if (finalCommit) {
                txQuality.createdAt = System.currentTimeMillis();
            }

            db.receiptQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PackageQualityTransaction commitPackageSampleQuality(MobileDB db, Boolean finalCommit) {
        try {

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


            PackageQualityTransaction txQuality = db.packageQualityTransactionDAO().getByLot(recQualityPackage.lot);
            if (txQuality == null) {
                txQuality = new PackageQualityTransaction();
                txQuality.lot = recQualityPackage.lot;

                try {
                    txQuality.bestBefore = dateFormat.parse(recQualityPackage.bestBefore);
                } catch (Exception e) {
                    txQuality.bestBefore = null;
                }
            }

            txQuality.fishingLot = recQualityPackage.fishLot;
            txQuality.sortingSamples = recQualityPackage.sortingSamples;
            txQuality.tonneSamples = recQualityPackage.tonneSamples;
//
//
            if (finalCommit && txQuality.labelCreatedAt == null) {
                txQuality.sampleCreatedAt = System.currentTimeMillis();
            }

            db.packageQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static PackageQualityTransaction commitPackageLabelCheckQuality(MobileDB db, Boolean finalCommit) {
        try {

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            PackageQualityTransaction txQuality = db.packageQualityTransactionDAO().getByLot(recQualityPackage.lot);
            if (txQuality == null) {
                txQuality = new PackageQualityTransaction();
                txQuality.lot = recQualityPackage.lot;
                try {
                    txQuality.bestBefore = dateFormat.parse(recQualityPackage.bestBefore);
                } catch (Exception e) {
                    txQuality.bestBefore = null;
                }
            }

            txQuality.fishingLot = recQualityPackage.fishLot;
            txQuality.startPacking = recQualityPackage.startPacking;
            txQuality.changePacking = recQualityPackage.changePacking;
            txQuality.middlePacking = recQualityPackage.middlePacking;
            txQuality.endPacking = recQualityPackage.endPacking;
            txQuality.labelComments = recQualityPackage.labelComments;
            txQuality.disinfectedBins = recQualityPackage.disinfectedBins;
//

            if (finalCommit && txQuality.labelCreatedAt == null) {
                txQuality.labelCreatedAt = System.currentTimeMillis();
            }

            db.packageQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PackageQualityTransaction commitPackageFreshQuality(MobileDB db, Boolean finalCommit) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


            PackageQualityTransaction txQuality = db.packageQualityTransactionDAO().getByLot(recQualityPackage.lot);
            if (txQuality == null) {
                txQuality = new PackageQualityTransaction();
                txQuality.lot = recQualityPackage.lot;
                try {
                    txQuality.bestBefore = dateFormat.parse(recQualityPackage.bestBefore);
                } catch (Exception e) {
                    txQuality.bestBefore = null;
                }
            }
            txQuality.fishingLot = recQualityPackage.fishLot;
            txQuality.freshGrade = recQualityPackage.freshGrade;
            txQuality.overallGrade = recQualityPackage.overallGrade;
            txQuality.skinGrade = recQualityPackage.skinGrade;
            txQuality.eyeGrade = recQualityPackage.eyeGrade;
            txQuality.gillGrade = recQualityPackage.gillGrade;
            txQuality.crookedMouth = recQualityPackage.crookedMouth;
            txQuality.lowerJaw = recQualityPackage.lowerJaw;
            txQuality.jawOver = recQualityPackage.jawOver;
            txQuality.operculum = recQualityPackage.operculum;
            txQuality.lordosis = recQualityPackage.lordosis;
            txQuality.shortening = recQualityPackage.shortening;
            txQuality.skeletical = recQualityPackage.skeletical;
            txQuality.tailDeformity = recQualityPackage.tailDeformity;
            txQuality.tailDeform = recQualityPackage.tailDeform;
            txQuality.finDeform = recQualityPackage.finDeform;
            txQuality.woundsDeform = recQualityPackage.woundsDeform;
            txQuality.hemSlight = recQualityPackage.hemSlight;
            txQuality.hemDiffuse = recQualityPackage.hemDiffuse;
            txQuality.hemSpots = recQualityPackage.hemSpots;
            txQuality.hemWounds = recQualityPackage.hemWounds;
            txQuality.eyeBlurred = recQualityPackage.eyeBlurred;
            txQuality.eyeCured = recQualityPackage.eyeCured;
            txQuality.eyeBlind = recQualityPackage.eyeBlind;
            txQuality.eyeBleed = recQualityPackage.eyeBleed;
            txQuality.gillMucus = recQualityPackage.gillMucus;
            txQuality.gillBloody = recQualityPackage.gillBloody;
            txQuality.gillBrown = recQualityPackage.gillBrown;
            txQuality.gillDiscolor = recQualityPackage.gillDiscolor;
            txQuality.headDeform = recQualityPackage.headDeform;
//            txQuality.laundrySamples = recQualityPackage.laundrySamples;
//            txQuality.tonneSamples = recQualityPackage.tonneSamples;
//            txQuality.changePacking = recQualityPackage.changePacking;
//            txQuality.middlePacking = recQualityPackage.middlePacking;
//            txQuality.startPacking = recQualityPackage.startPacking;
//            txQuality.endPacking = recQualityPackage.endPacking;
//            txQuality.labelComments = recQualityPackage.labelComments;
//

            if (finalCommit && txQuality.freshCreatedAt == null) {
                txQuality.freshCreatedAt = System.currentTimeMillis();
            }

            db.packageQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FinalQualityTransaction commitFinalQuality(MobileDB db, Boolean finalCommit) {
        try {


            FinalQualityTransaction txQuality = db.finalQualityTransactionDAO().getByLot(recQualityFinal.lot);
            if (txQuality == null) {
                txQuality = new FinalQualityTransaction();
                txQuality.lot = recQualityFinal.lot;

            }
            txQuality.fishingLot = recQualityFinal.fishLot;

            txQuality.exfoRating = recQualityFinal.exfoRating;
            txQuality.paletteRating = recQualityFinal.paletteRating;
            txQuality.boxRating = recQualityFinal.boxRating;
            txQuality.expanded = recQualityFinal.expanded;
            txQuality.soft = recQualityFinal.soft;
            txQuality.cylinrical = recQualityFinal.cylinrical;
            txQuality.head = recQualityFinal.head;
            txQuality.body = recQualityFinal.body;
            txQuality.areas = recQualityFinal.areas;


            txQuality.sizeFirst = recQualityFinal.sample1.size;
            txQuality.boxTypeFirst = recQualityFinal.sample1.boxType;
            txQuality.labelPiecesFirst = recQualityFinal.sample1.labelPieces;
            txQuality.countedPiecesFirst = recQualityFinal.sample1.countedPieces;
            txQuality.underWeightFirst1 = recQualityFinal.sample1.underWeight1;
            txQuality.underWeightFirst2 = recQualityFinal.sample1.underWeight2;
            txQuality.underWeightFirst3 = recQualityFinal.sample1.underWeight3;
            txQuality.overWeightFirst1 = recQualityFinal.sample1.overWeight1;
            txQuality.overWeightFirst2 = recQualityFinal.sample1.overWeight2;
            txQuality.overWeightFirst3 = recQualityFinal.sample1.overWeight3;
            txQuality.netWeightFirst = recQualityFinal.sample1.netWeight;
            txQuality.iceQuantityFirst = recQualityFinal.sample1.iceQuantity;
            txQuality.fishTempFirst = recQualityFinal.sample1.fishTemp;


            txQuality.sizeSecond = recQualityFinal.sample2.size;
            txQuality.boxTypeSecond = recQualityFinal.sample2.boxType;
            txQuality.labelPiecesSecond = recQualityFinal.sample2.labelPieces;
            txQuality.countedPiecesSecond = recQualityFinal.sample2.countedPieces;
            txQuality.underWeightSecond1 = recQualityFinal.sample2.underWeight1;
            txQuality.underWeightSecond2 = recQualityFinal.sample2.underWeight2;
            txQuality.underWeightSecond3 = recQualityFinal.sample2.underWeight3;
            txQuality.overWeightSecond1 = recQualityFinal.sample2.overWeight1;
            txQuality.overWeightSecond2 = recQualityFinal.sample2.overWeight2;
            txQuality.overWeightSecond3 = recQualityFinal.sample2.overWeight3;
            txQuality.netWeightSecond = recQualityFinal.sample2.netWeight;
            txQuality.iceQuantitySecond = recQualityFinal.sample2.iceQuantity;
            txQuality.fishTempSecond = recQualityFinal.sample2.fishTemp;

            txQuality.sizeThird = recQualityFinal.sample3.size;
            txQuality.boxTypeThird = recQualityFinal.sample3.boxType;
            txQuality.labelPiecesThird = recQualityFinal.sample3.labelPieces;
            txQuality.countedPiecesThird = recQualityFinal.sample3.countedPieces;
            txQuality.underWeightThird1 = recQualityFinal.sample3.underWeight1;
            txQuality.underWeightThird2 = recQualityFinal.sample3.underWeight2;
            txQuality.underWeightThird3 = recQualityFinal.sample3.underWeight3;
            txQuality.overWeightThird1 = recQualityFinal.sample3.overWeight1;
            txQuality.overWeightThird2 = recQualityFinal.sample3.overWeight2;
            txQuality.overWeightThird3 = recQualityFinal.sample3.overWeight3;
            txQuality.netWeightThird = recQualityFinal.sample3.netWeight;
            txQuality.iceQuantityThird = recQualityFinal.sample3.iceQuantity;
            txQuality.fishTempThird = recQualityFinal.sample3.fishTemp;

            if (recQualityFinal.signatureBytes != null) {
                txQuality.signature = Base64.encodeToString(recQualityFinal.signatureBytes, Base64.NO_WRAP);

            }

            txQuality.foreignBody = recQualityFinal.foreignBody;
            txQuality.corrAction = recQualityFinal.corrAction;
            txQuality.lotAccepted = recQualityFinal.lotAccepted;
            txQuality.discardedQty = recQualityFinal.discardedQty;

//            txQuality.laundrySamples = recQualityPackage.laundrySamples;
//            txQuality.tonneSamples = recQualityPackage.tonneSamples;
//            txQuality.changePacking = recQualityPackage.changePacking;
//            txQuality.middlePacking = recQualityPackage.middlePacking;
//            txQuality.startPacking = recQualityPackage.startPacking;
//            txQuality.endPacking = recQualityPackage.endPacking;
//            txQuality.labelComments = recQualityPackage.labelComments;
//

            if (finalCommit && txQuality.createdAt == null) {
                txQuality.createdAt = System.currentTimeMillis();
            }

            db.finalQualityTransactionDAO().insert(txQuality);

            return txQuality;
        } catch (Exception e) {
            e.printStackTrace();
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
