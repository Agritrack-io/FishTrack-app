package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.QualityTransaction;

public class QualityTxDTO {

    public Long id;
    public Long timestamp;
    public String plot;
    public List<String> quality_bins;
    public String bin_condition;
    public String ice_condition;
    public String smell_condition;
    public Double fish_temp;
    public Double rigor_mortis;
    public Double elimination_food;
    public Double elimination_sperm;
    public Double parasites;
    public Double peeling;
    public Double shiny;
    public Double blurred;
    public Double healed;
    public Double blind_eyes;
    public Double coherent;
    public Double soft;
    public Double swollen;
    public Double light_hematoma;
    public Double heavy_hematoma;
    public Double pink;
    public Double dark;
    public Double white;
    public Double hematomas;
    public Double mucus;
    public Double problematic_fish;
    public String site;
    public String harvest_load;
    public String user;
    public Double longitude;
    public Double latitude;

    public static QualityTxDTO convert(QualityTransaction quality) {
        QualityTxDTO qualityTxDto = new QualityTxDTO();

        qualityTxDto.plot = quality.plot;
        qualityTxDto.bin_condition = quality.binCondition;
        qualityTxDto.ice_condition = quality.iceCondition;
        qualityTxDto.smell_condition = quality.smellCondition;
        qualityTxDto.fish_temp = quality.fishTemp;
        qualityTxDto.rigor_mortis = quality.rigorMortis;
        qualityTxDto.elimination_food = quality.eliminationFood;
        qualityTxDto.elimination_sperm = quality.eliminationSperm;
        qualityTxDto.parasites = quality.parasites;
        qualityTxDto.peeling = quality.peeling;
        qualityTxDto.shiny = quality.shiny;
        qualityTxDto.blurred = quality.blurred;
        qualityTxDto.healed = quality.healed;
        qualityTxDto.blind_eyes = quality.blindEyes;
        qualityTxDto.coherent = quality.coherent;
        qualityTxDto.soft = quality.soft;
        qualityTxDto.swollen = quality.swollen;
        qualityTxDto.light_hematoma = quality.lightHematoma;
        qualityTxDto.heavy_hematoma = quality.heavyHematoma;
        qualityTxDto.pink = quality.pink;
        qualityTxDto.dark = quality.dark;
        qualityTxDto.white = quality.white;
        qualityTxDto.hematomas = quality.hematomas;
        qualityTxDto.mucus = quality.mucus;
        qualityTxDto.problematic_fish = quality.problematicFish;
        qualityTxDto.site = quality.site;
        qualityTxDto.user = quality.user;
        qualityTxDto.timestamp = quality.timestamp;
        qualityTxDto.longitude = quality.longitude;
        qualityTxDto.latitude = quality.latitude;

        return qualityTxDto;
    }
}
