package io.agritrack.kefalonia.fish.state;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.model.tx.QualityTransaction;
import io.agritrack.kefalonia.ui.adapter.BinWeightCageAdapter;

public class QualityRecord {

    public long txKey;
    public String pLot;
    public String remarks;
    public List<BinWeightCageAdapter.BinDetails> qualityBins;
    public List<String> expectedBins;
    public List<String> scannedBins;
    public Integer qualityBinsCnt;
    public String binCondition;
    public String iceCondition;
    public String smellCondition;
    public Double minFishTemp;
    public Double meanFishTemp;
    public Double maxFishTemp;
    public Integer rigorMortis;
    public Integer eliminationFood;
    public Integer eliminationSperm;
    public Integer parasites;
    public Integer peeling;
    public Integer shiny;
    public Integer blurred;
    public Integer healed;
    public Integer blindEyes;
    public Integer coherent;
    public Integer soft;
    public Integer swollen;
    public Integer noHematoma;
    public Integer lightHematoma;
    public Integer heavyHematoma;
    public Integer pink;
    public Integer dark;
    public Integer white;
    public Integer uncolored;
    public Integer hematomas;
    public Integer mucus;
    public Integer problematicFish;
    public Long retrievedAt;
    public String photoPath;
    public Double longitude;
    public Double latitude;
    public String logger_rfid;
    public Double etT1;
    public Double etT2;
    public Double etT3;
    public String boxSn;
    public Long timestamp;
    public String evaluation;
    public int selectedRgId;
    public Double minBinTemp;
    public Double meanBinTemp;
    public Double maxBinTemp;
    private MobileDB db;

    public QualityRecord() {
    }

    public static QualityRecord convert(QualityTransaction tx) {

        QualityRecord qualityRecord = new QualityRecord();

        qualityRecord.txKey = tx.id;
        qualityRecord.pLot = tx.plot;
        qualityRecord.remarks = tx.remarks;
        qualityRecord.qualityBins = convertEPCsToBinDetails(tx.qualityBins);
        qualityRecord.expectedBins = tx.expectedBins;
        qualityRecord.scannedBins = tx.scannedBins;
        qualityRecord.qualityBinsCnt = tx.qualityBinsCnt;
        qualityRecord.binCondition = tx.binCondition;
        qualityRecord.iceCondition = tx.iceCondition;
        qualityRecord.smellCondition = tx.smellCondition;
        qualityRecord.minFishTemp = tx.minFishTemp;
        qualityRecord.meanFishTemp = tx.avgFishTemp;
        qualityRecord.maxFishTemp = tx.maxFishTemp;
        qualityRecord.rigorMortis = tx.rigorMortis;
        qualityRecord.eliminationFood = tx.eliminationFood;
        qualityRecord.eliminationSperm = tx.eliminationSperm;
        qualityRecord.parasites = tx.parasites;
        qualityRecord.peeling = tx.peeling;
        qualityRecord.shiny = tx.shiny;
        qualityRecord.blurred = tx.blurred;
        qualityRecord.healed = tx.healed;
        qualityRecord.blindEyes = tx.blindEyes;
        qualityRecord.coherent = tx.coherent;
        qualityRecord.soft = tx.soft;
        qualityRecord.swollen = tx.swollen;
        qualityRecord.noHematoma = tx.noHematoma;
        qualityRecord.lightHematoma = tx.lightHematoma;
        qualityRecord.heavyHematoma = tx.heavyHematoma;
        qualityRecord.pink = tx.pink;
        qualityRecord.dark = tx.dark;
        qualityRecord.white = tx.white;
        qualityRecord.uncolored = tx.uncolored;
        qualityRecord.hematomas = tx.hematomas;
        qualityRecord.mucus = tx.mucus;
        qualityRecord.problematicFish = tx.problematicFish;
        qualityRecord.evaluation = tx.overallEvaluation;
        qualityRecord.selectedRgId = tx.selectedRgId;
        qualityRecord.minBinTemp = tx.minBinTemp;
        qualityRecord.meanBinTemp = tx.avgBinTemp;
        qualityRecord.maxBinTemp = tx.maxBinTemp;

        return qualityRecord;
    }

    private static List<BinWeightCageAdapter.BinDetails> convertEPCsToBinDetails(List<String> epcs) {
        List<BinWeightCageAdapter.BinDetails> result = new ArrayList<>();
        for (String epc : epcs) {
            result.add(new BinWeightCageAdapter.BinDetails(epc));
        }
        return result;
    }
}
