package io.agritrack.fish.state;

import androidx.room.ColumnInfo;

import java.util.LinkedList;
import java.util.List;

public class QualityRecord {

    public String pLot;
    public String remarks;
    public List<String> qualityBins;
    public List<String[]> tempValues;
    public String binCondition;
    public String iceCondition;
    public String smellCondition;
    public Double fishTemp;
    public Double rigorMortis;
    public Double eliminationFood;
    public Double eliminationSperm;
    public Double parasites;
    public Double peeling;
    public Double shiny;
    public Double blurred;
    public Double healed;
    public Double blindEyes;
    public Double coherent;
    public Double soft;
    public Double swollen;
    public Double lightHematoma;
    public Double heavyHematoma;
    public Double pink;
    public Double dark;
    public Double white;
    public Double uncolored;
    public Double hematomas;
    public Double mucus;
    public Double problematicFish;
    public Long retrievedAt;
    public String photoPath;
    public Double longitude;
    public Double latitude;
    public String logger_rfid;
    public boolean qualityProcessing;

    public QualityRecord() {
    }
}
