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
    public Integer rigorMortis;

    @ColumnInfo(name = "elimination_food")
    public Integer eliminationFood;

    @ColumnInfo(name = "elimination_sperm")
    public Integer eliminationSperm;

    @ColumnInfo(name = "parasites")
    public Integer parasites;

    @ColumnInfo(name = "peeling")
    public Integer peeling;

    @ColumnInfo(name = "shiny")
    public Integer shiny;

    @ColumnInfo(name = "blurred")
    public Integer blurred;

    @ColumnInfo(name = "healed")
    public Integer healed;

    @ColumnInfo(name = "blind_eyes")
    public Integer blindEyes;

    @ColumnInfo(name = "coherent")
    public Integer coherent;

    @ColumnInfo(name = "soft")
    public Integer soft;

    @ColumnInfo(name = "swollen")
    public Integer swollen;

    @ColumnInfo(name = "light_hematoma")
    public Integer lightHematoma;

    @ColumnInfo(name = "heavy_hematoma")
    public Integer heavyHematoma;

    @ColumnInfo(name = "pink")
    public Integer pink;

    @ColumnInfo(name = "dark")
    public Integer dark;

    @ColumnInfo(name = "white")
    public Integer white;

    @ColumnInfo(name = "uncolored")
    public Integer uncolored;

    @ColumnInfo(name = "hematomas")
    public Integer hematomas;

    @ColumnInfo(name = "mucus")
    public Integer mucus;

    @ColumnInfo(name = "problematic_fish")
    public Integer problematicFish;

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
