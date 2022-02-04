package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.LinkedList;
import java.util.List;

import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.QualityTransaction;

public class QualityTxDTO {

    public Long id;
    public Long timestamp;
    public String plot;
    public List<String> quality_bins = new LinkedList<String>();
    public String bin_condition;
    public String ice_condition;
    public String smell_condition;
    public Double fish_temp;
    public Integer rigor_mortis;
    public Integer elimination_food;
    public Integer elimination_sperm;
    public Integer parasites;
    public Integer peeling;
    public Integer shiny;
    public Integer blurred;
    public Integer healed;
    public Integer blind_eyes;
    public Integer coherent;
    public Integer soft;
    public Integer swollen;
    public Integer light_hematoma;
    public Integer heavy_hematoma;
    public Integer pink;
    public Integer dark;
    public Integer white;
    public Integer uncolored;
    public Integer hematomas;
    public Integer mucus;
    public Integer problematic_fish;
    public String packaging_site;
    public String harvest_load;
    public String user;
    public String state;
    public Double longitude;
    public Double latitude;

    public static QualityTxDTO convert(QualityTransaction quality) {
        QualityTxDTO qualityTxDto = new QualityTxDTO();

        qualityTxDto.plot = quality.plot;
        qualityTxDto.quality_bins = quality.qualityBins;
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
        qualityTxDto.uncolored = quality.uncolored;
        qualityTxDto.hematomas = quality.hematomas;
        qualityTxDto.mucus = quality.mucus;
        qualityTxDto.problematic_fish = quality.problematicFish;
        qualityTxDto.packaging_site = quality.site;
        qualityTxDto.user = quality.user;
        qualityTxDto.state = quality.state;
        qualityTxDto.timestamp = quality.timestamp;
        qualityTxDto.longitude = quality.longitude;
        qualityTxDto.latitude = quality.latitude;

        return qualityTxDto;
    }
}
