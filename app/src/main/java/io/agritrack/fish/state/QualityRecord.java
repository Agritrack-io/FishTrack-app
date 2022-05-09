package io.agritrack.fish.state;

import java.util.List;

public class QualityRecord {

    public String pLot;
    public String remarks;
    public List<String> qualityBins;
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

    public QualityRecord() {
    }
}
