package io.agritrack.data.dto.tx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.agritrack.data.model.tx.QualityTransaction;

public class QualityTxDTO {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public UUID id;
    public String sample_date;
    public String plot;
    public List<String> quality_bins = new LinkedList<String>();
    public Integer no_quality_bins;
    public String bin_condition;
    public String ice_condition;
    public String smell_condition;
    public Double min_bin_temp;
    public Double avg_bin_temp;
    public Double max_bin_temp;
    public Double min_fish_temp;
    public Double avg_fish_temp;
    public Double max_fish_temp;
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
    public Integer no_hematoma;
    public Integer light_hematoma;
    public Integer heavy_hematoma;
    public Integer pink;
    public Integer dark;
    public Integer white;
    public Integer uncolored;
    public Integer hematomas;
    public Integer mucus;
    public Integer problematic_fish;
    public String overall_evaluation;
    public String remarks;
    public String plant;
    public String user;
    public Long occurred_at;
    public Double longitude;
    public Double latitude;

    public static QualityTxDTO convert(QualityTransaction quality) {
        QualityTxDTO qualityTxDto = new QualityTxDTO();

        qualityTxDto.plot = quality.plot;
        qualityTxDto.quality_bins = quality.qualityBins;
        qualityTxDto.no_quality_bins = quality.qualityBinsCnt;
        qualityTxDto.bin_condition = quality.binCondition;
        qualityTxDto.ice_condition = quality.iceCondition;
        qualityTxDto.smell_condition = quality.smellCondition;
        qualityTxDto.min_bin_temp = quality.minBinTemp;
        qualityTxDto.avg_bin_temp = quality.avgBinTemp;
        qualityTxDto.max_bin_temp = quality.maxBinTemp;
        qualityTxDto.min_fish_temp = quality.minFishTemp;
        qualityTxDto.avg_fish_temp = quality.avgFishTemp;
        qualityTxDto.max_fish_temp = quality.maxFishTemp;
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
        qualityTxDto.no_hematoma = quality.noHematoma;
        qualityTxDto.light_hematoma = quality.lightHematoma;
        qualityTxDto.heavy_hematoma = quality.heavyHematoma;
        qualityTxDto.pink = quality.pink;
        qualityTxDto.dark = quality.dark;
        qualityTxDto.white = quality.white;
        qualityTxDto.uncolored = quality.uncolored;
        qualityTxDto.hematomas = quality.hematomas;
        qualityTxDto.mucus = quality.mucus;
        qualityTxDto.problematic_fish = quality.problematicFish;
        qualityTxDto.overall_evaluation = quality.overallEvaluation;
        qualityTxDto.remarks = quality.remarks;
        qualityTxDto.plant = quality.site;
        qualityTxDto.user = quality.user;
        qualityTxDto.sample_date = dateFormat.format(quality.sampleDate);
        qualityTxDto.occurred_at = quality.createdAt;
        qualityTxDto.longitude = quality.longitude;
        qualityTxDto.latitude = quality.latitude;

        return qualityTxDto;
    }
}
