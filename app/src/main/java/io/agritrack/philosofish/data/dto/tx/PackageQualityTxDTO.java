package io.agritrack.philosofish.data.dto.tx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.agritrack.philosofish.data.dto.common.PackageFreshCheckDTO;
import io.agritrack.philosofish.data.dto.common.PackageLabelCheckDTO;
import io.agritrack.philosofish.data.dto.common.PackageSamplingDTO;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityTxDTO {

    private static final SimpleDateFormat simpleDateTime =  new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");

    public UUID id;

    @SerializedName("lot")
    public String lot;

    @SerializedName("fishing_lot")
    public String fishingLot;

    @SerializedName("fresh_check")
    public PackageFreshCheckDTO freshCheck = new PackageFreshCheckDTO();

    @SerializedName("sampling")
    public PackageSamplingDTO sampling = new PackageSamplingDTO();

    @SerializedName("label_check")
    public PackageLabelCheckDTO labelCheck = new PackageLabelCheckDTO();

    @JsonProperty
    public String fresh_occurred_at;

    @JsonProperty
    public String label_occurred_at;

    @JsonProperty
    public String sample_occurred_at;

    @JsonProperty
    public String plant;



    @JsonProperty
    public String user = LocalPreferences.getLoggedInUser("N/A");




    public static PackageQualityTxDTO convert(PackageQualityTransaction qualityTx) {
        PackageQualityTxDTO packageQualityTxDTO = new PackageQualityTxDTO();
        packageQualityTxDTO.lot = qualityTx.lot;
        packageQualityTxDTO.id = qualityTx.id;
        packageQualityTxDTO.plant = LocalPreferences.getCurrentSiteName();
        packageQualityTxDTO.freshCheck.freshGrade = qualityTx.freshGrade;
        packageQualityTxDTO.freshCheck.overallGrade = qualityTx.overallGrade;
        packageQualityTxDTO.freshCheck.skinGrade = qualityTx.skinGrade;
        packageQualityTxDTO.freshCheck.eyeGrade = qualityTx.eyeGrade;
        packageQualityTxDTO.freshCheck.gillGrade = qualityTx.gillGrade;
        packageQualityTxDTO.freshCheck.crookedMouth = qualityTx.crookedMouth;
        packageQualityTxDTO.freshCheck.headDeform = qualityTx.headDeform;
        packageQualityTxDTO.freshCheck.lowerJaw = qualityTx.lowerJaw;
        packageQualityTxDTO.freshCheck.jawOver = qualityTx.jawOver;
        packageQualityTxDTO.freshCheck.operculum = qualityTx.operculum;
        packageQualityTxDTO.freshCheck.lordosis = qualityTx.lordosis;
        packageQualityTxDTO.freshCheck.shortening = qualityTx.shortening;
        packageQualityTxDTO.freshCheck.skeletical = qualityTx.skeletical;
        packageQualityTxDTO.freshCheck.tailDeformity = qualityTx.tailDeformity;
        packageQualityTxDTO.freshCheck.tailDeform = qualityTx.tailDeform;
        packageQualityTxDTO.freshCheck.finDeform = qualityTx.finDeform;
        packageQualityTxDTO.freshCheck.woundsDeform = qualityTx.woundsDeform;
        packageQualityTxDTO.freshCheck.hemSlight = qualityTx.hemSlight;
        packageQualityTxDTO.freshCheck.hemSpots = qualityTx.hemSpots;
        packageQualityTxDTO.freshCheck.hemDiffuse = qualityTx.hemDiffuse;
        packageQualityTxDTO.freshCheck.hemWounds = qualityTx.hemWounds;
        packageQualityTxDTO.freshCheck.eyeBlurred = qualityTx.eyeBlurred;
        packageQualityTxDTO.freshCheck.eyeCured = qualityTx.eyeCured;
        packageQualityTxDTO.freshCheck.eyeBlind = qualityTx.eyeBlind;
        packageQualityTxDTO.freshCheck.eyeBleed = qualityTx.eyeBleed;
        packageQualityTxDTO.freshCheck.gillMucus = qualityTx.gillMucus;
        packageQualityTxDTO.freshCheck.gillBloody = qualityTx.gillBloody;
        packageQualityTxDTO.freshCheck.gillBrown = qualityTx.gillBrown;
        packageQualityTxDTO.freshCheck.gillDiscolor = qualityTx.gillDiscolor;
        packageQualityTxDTO.sampling.sortingSamples = qualityTx.sortingSamples;
        packageQualityTxDTO.sampling.tonneSamples = qualityTx.tonneSamples;
        packageQualityTxDTO.labelCheck.startPacking = qualityTx.startPacking;
        packageQualityTxDTO.labelCheck.middlePacking = qualityTx.middlePacking;
        packageQualityTxDTO.labelCheck.changePacking = qualityTx.changePacking;
        packageQualityTxDTO.labelCheck.endPacking = qualityTx.endPacking;
        packageQualityTxDTO.labelCheck.labelComments = qualityTx.labelComments;

        return packageQualityTxDTO;

    }

    public static PackageQualityTxDTO convertFresh(PackageQualityTransaction qualityTx) {
        PackageQualityTxDTO packageQualityTxDTO = new PackageQualityTxDTO();
        packageQualityTxDTO.lot = qualityTx.lot;
        packageQualityTxDTO.fishingLot = qualityTx.fishingLot;
        packageQualityTxDTO.id = qualityTx.id;
        packageQualityTxDTO.plant = LocalPreferences.getCurrentSiteName();
        packageQualityTxDTO.freshCheck.freshGrade = qualityTx.freshGrade;
        packageQualityTxDTO.freshCheck.overallGrade = qualityTx.overallGrade;
        packageQualityTxDTO.freshCheck.skinGrade = qualityTx.skinGrade;
        packageQualityTxDTO.freshCheck.eyeGrade = qualityTx.eyeGrade;
        packageQualityTxDTO.freshCheck.gillGrade = qualityTx.gillGrade;
        packageQualityTxDTO.freshCheck.crookedMouth = qualityTx.crookedMouth;
        packageQualityTxDTO.freshCheck.headDeform = qualityTx.headDeform;
        packageQualityTxDTO.freshCheck.lowerJaw = qualityTx.lowerJaw;
        packageQualityTxDTO.freshCheck.jawOver = qualityTx.jawOver;
        packageQualityTxDTO.freshCheck.operculum = qualityTx.operculum;
        packageQualityTxDTO.freshCheck.lordosis = qualityTx.lordosis;
        packageQualityTxDTO.freshCheck.shortening = qualityTx.shortening;
        packageQualityTxDTO.freshCheck.skeletical = qualityTx.skeletical;
        packageQualityTxDTO.freshCheck.tailDeformity = qualityTx.tailDeformity;
        packageQualityTxDTO.freshCheck.tailDeform = qualityTx.tailDeform;
        packageQualityTxDTO.freshCheck.finDeform = qualityTx.finDeform;
        packageQualityTxDTO.freshCheck.woundsDeform = qualityTx.woundsDeform;
        packageQualityTxDTO.freshCheck.hemSlight = qualityTx.hemSlight;
        packageQualityTxDTO.freshCheck.hemSpots = qualityTx.hemSpots;
        packageQualityTxDTO.freshCheck.hemDiffuse = qualityTx.hemDiffuse;
        packageQualityTxDTO.freshCheck.hemWounds = qualityTx.hemWounds;
        packageQualityTxDTO.freshCheck.eyeBlurred = qualityTx.eyeBlurred;
        packageQualityTxDTO.freshCheck.eyeCured = qualityTx.eyeCured;
        packageQualityTxDTO.freshCheck.eyeBlind = qualityTx.eyeBlind;
        packageQualityTxDTO.freshCheck.eyeBleed = qualityTx.eyeBleed;
        packageQualityTxDTO.freshCheck.gillMucus = qualityTx.gillMucus;
        packageQualityTxDTO.freshCheck.gillBloody = qualityTx.gillBloody;
        packageQualityTxDTO.freshCheck.gillBrown = qualityTx.gillBrown;
        packageQualityTxDTO.freshCheck.gillDiscolor = qualityTx.gillDiscolor;
        packageQualityTxDTO.fresh_occurred_at = simpleDateTime.format(new Date(qualityTx.freshCreatedAt));


        return packageQualityTxDTO;

    }

    public static PackageQualityTxDTO convertSample(PackageQualityTransaction qualityTx) {
        PackageQualityTxDTO packageQualityTxDTO = new PackageQualityTxDTO();
        packageQualityTxDTO.lot = qualityTx.lot;
        packageQualityTxDTO.fishingLot = qualityTx.fishingLot;
        packageQualityTxDTO.id = qualityTx.id;
        packageQualityTxDTO.plant = LocalPreferences.getCurrentSiteName();
        packageQualityTxDTO.sampling.sortingSamples = qualityTx.sortingSamples;
        packageQualityTxDTO.sampling.tonneSamples = qualityTx.tonneSamples;
        packageQualityTxDTO.sample_occurred_at = simpleDateTime.format(new Date(qualityTx.sampleCreatedAt));


        return packageQualityTxDTO;

    }

    public static PackageQualityTxDTO convertLabel(PackageQualityTransaction qualityTx) {
        PackageQualityTxDTO packageQualityTxDTO = new PackageQualityTxDTO();
        packageQualityTxDTO.lot = qualityTx.lot;
        packageQualityTxDTO.fishingLot = qualityTx.fishingLot;
        packageQualityTxDTO.id = qualityTx.id;
        packageQualityTxDTO.plant = LocalPreferences.getCurrentSiteName();
        packageQualityTxDTO.labelCheck.startPacking = qualityTx.startPacking;
        packageQualityTxDTO.labelCheck.middlePacking = qualityTx.middlePacking;
        packageQualityTxDTO.labelCheck.changePacking = qualityTx.changePacking;
        packageQualityTxDTO.labelCheck.endPacking = qualityTx.endPacking;
        packageQualityTxDTO.labelCheck.labelComments = qualityTx.labelComments;
        packageQualityTxDTO.labelCheck.totalKg = qualityTx.totalKg;
        packageQualityTxDTO.labelCheck.disinfectedBins = qualityTx.disinfectedBins;
        packageQualityTxDTO.label_occurred_at = simpleDateTime.format(new Date(qualityTx.labelCreatedAt));

        return packageQualityTxDTO;

    }

}
