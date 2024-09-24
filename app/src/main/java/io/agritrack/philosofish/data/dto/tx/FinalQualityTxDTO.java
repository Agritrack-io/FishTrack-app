package io.agritrack.philosofish.data.dto.tx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.agritrack.philosofish.data.model.common.FinalSample;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class FinalQualityTxDTO {

    private static final SimpleDateFormat simpleDateTime =  new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");

    public UUID id;

    @SerializedName("lot")
    public String lot;

    @SerializedName("exfo_rating")
    public Integer exfoRating;

    @SerializedName("palette_rating")
    public Integer paletteRating;

    @SerializedName("box_rating")
    public Integer boxRating;

    @SerializedName("cylinrical")
    public Boolean cylinrical;

    @SerializedName("expanded")
    public Boolean expanded;

    @SerializedName("soft")
    public Boolean soft;

    @SerializedName("head")
    public Boolean head;

    public String user;

    @SerializedName("body")
    public Boolean body;

    @SerializedName("areas")
    public Boolean areas;

    @SerializedName("sample_1")
    public FinalSample sample1 = new FinalSample();

    @SerializedName("sample_2")
    public FinalSample sample2 = new FinalSample();

    @SerializedName("sample_3")
    public FinalSample sample3 = new FinalSample();

    @JsonProperty
    public String occurred_at;

    @JsonProperty
    public String plant;


    @JsonProperty
    public String signature;

    public static FinalQualityTxDTO convert(FinalQualityTransaction qualityTx) {
        FinalQualityTxDTO finalQualityTxDTO = new FinalQualityTxDTO();
        finalQualityTxDTO.id = qualityTx.id;
        finalQualityTxDTO.lot = qualityTx.lot;
        finalQualityTxDTO.user = LocalPreferences.getLoggedInUser("N/A");
        finalQualityTxDTO.plant = LocalPreferences.getCurrentSiteName();

        finalQualityTxDTO.exfoRating = qualityTx.exfoRating;
        finalQualityTxDTO.paletteRating = qualityTx.paletteRating;
        finalQualityTxDTO.boxRating = qualityTx.boxRating;
        finalQualityTxDTO.expanded = qualityTx.expanded;
        finalQualityTxDTO.soft = qualityTx.soft;
        finalQualityTxDTO.cylinrical = qualityTx.cylinrical;
        finalQualityTxDTO.head = qualityTx.head;
        finalQualityTxDTO.body = qualityTx.body;
        finalQualityTxDTO.areas = qualityTx.areas;


        finalQualityTxDTO.sample1.size = qualityTx.sizeFirst;
        finalQualityTxDTO.sample1.boxType = qualityTx.boxTypeFirst;
        finalQualityTxDTO.sample1.labelPieces = qualityTx.labelPiecesFirst;
        finalQualityTxDTO.sample1.countedPieces = qualityTx.countedPiecesFirst;
        finalQualityTxDTO.sample1.underWeight = qualityTx.underWeightFirst;
        finalQualityTxDTO.sample1.overWeight = qualityTx.overWeightFirst;
        finalQualityTxDTO.sample1.netWeight = qualityTx.netWeightFirst;
        finalQualityTxDTO.sample1.iceQuantity = qualityTx.iceQuantityFirst;
        finalQualityTxDTO.sample1.fishTemp = qualityTx.fishTempFirst;


        finalQualityTxDTO.sample2.size = qualityTx.sizeSecond;
        finalQualityTxDTO.sample2.boxType = qualityTx.boxTypeSecond;
        finalQualityTxDTO.sample2.labelPieces = qualityTx.labelPiecesSecond;
        finalQualityTxDTO.sample2.countedPieces = qualityTx.countedPiecesSecond;
        finalQualityTxDTO.sample2.underWeight = qualityTx.underWeightSecond;
        finalQualityTxDTO.sample2.overWeight = qualityTx.overWeightSecond;
        finalQualityTxDTO.sample2.netWeight = qualityTx.netWeightSecond;
        finalQualityTxDTO.sample2.iceQuantity = qualityTx.iceQuantitySecond;
        finalQualityTxDTO.sample2.fishTemp = qualityTx.fishTempSecond;


        finalQualityTxDTO.sample3.size = qualityTx.sizeThird;
        finalQualityTxDTO.sample3.boxType = qualityTx.boxTypeThird;
        finalQualityTxDTO.sample3.labelPieces = qualityTx.labelPiecesThird;
        finalQualityTxDTO.sample3.countedPieces = qualityTx.countedPiecesThird;
        finalQualityTxDTO.sample3.underWeight = qualityTx.underWeightThird;
        finalQualityTxDTO.sample3.overWeight = qualityTx.overWeightThird;
        finalQualityTxDTO.sample3.netWeight = qualityTx.netWeightThird;
        finalQualityTxDTO.sample3.iceQuantity = qualityTx.iceQuantityThird;
        finalQualityTxDTO.sample3.fishTemp = qualityTx.fishTempThird;

        finalQualityTxDTO.signature = qualityTx.signature;

        finalQualityTxDTO.occurred_at = simpleDateTime.format(new Date(qualityTx.createdAt));


        return finalQualityTxDTO;

    }

}
