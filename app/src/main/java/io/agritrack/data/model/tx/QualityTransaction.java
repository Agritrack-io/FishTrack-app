package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;

@Entity(tableName = "quality_transaction")
public class QualityTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "plot")
    public String plot;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "quality_bins")
    public List<String> qualityBins;

    @ColumnInfo(name = "bin_condition")
    public String binCondition;

    @ColumnInfo(name = "ice_condition")
    public String iceCondition;

    @ColumnInfo(name = "smell_condition")
    public String smellCondition;

    @ColumnInfo(name = "fish_temp")
    public Double fishTemp;

    @ColumnInfo(name = "rigor_mortis")
    public Double rigorMortis;

    @ColumnInfo(name = "elimination_food")
    public Double eliminationFood;

    @ColumnInfo(name = "elimination_sperm")
    public Double eliminationSperm;

    @ColumnInfo(name = "parasites")
    public Double parasites;

    @ColumnInfo(name = "peeling")
    public Double peeling;

    @ColumnInfo(name = "shiny")
    public Double shiny;

    @ColumnInfo(name = "blurred")
    public Double blurred;

    @ColumnInfo(name = "healed")
    public Double healed;

    @ColumnInfo(name = "blind_eyes")
    public Double blindEyes;

    @ColumnInfo(name = "coherent")
    public Double coherent;

    @ColumnInfo(name = "soft")
    public Double soft;

    @ColumnInfo(name = "swollen")
    public Double swollen;

    @ColumnInfo(name = "light_hematoma")
    public Double lightHematoma;

    @ColumnInfo(name = "heavy_hematoma")
    public Double heavyHematoma;

    @ColumnInfo(name = "pink")
    public Double pink;

    @ColumnInfo(name = "dark")
    public Double dark;

    @ColumnInfo(name = "white")
    public Double white;

    @ColumnInfo(name = "uncolored")
    public Double uncolored;

    @ColumnInfo(name = "hematomas")
    public Double hematomas;

    @ColumnInfo(name = "mucus")
    public Double mucus;

    @ColumnInfo(name = "problematic_fish")
    public Double problematicFish;

    @ColumnInfo(name = "site_id")
    public String site;

    @ColumnInfo(name = "harvest_load_id")
    public String harvestLoad;

    @ColumnInfo(name = "user_id")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
