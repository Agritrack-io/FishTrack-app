package io.agritrack.philosofish.data.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class PackageFreshCheckDTO {

    @SerializedName("fresh_grade")
    public Integer freshGrade;

    @SerializedName("skin_grade")
    public Integer skinGrade;

    @SerializedName("eye_grade")
    public Integer eyeGrade;

    @SerializedName("gill_grade")
    public Integer gillGrade;

    @SerializedName("crooked_mouth")
    public Integer crookedMouth;

    @SerializedName("lower_law")
    public Integer lowerJaw;

    @SerializedName("jaw_over")
    public Integer jawOver;

    @SerializedName("overall_grade")
    public Integer overallGrade;

    @SerializedName("operculum")
    public Integer operculum;

    @SerializedName("lordosis")
    public Integer lordosis;

    @SerializedName("shortening")
    public Integer shortening;

    @SerializedName("skeletical")
    public Integer skeletical;

    @SerializedName("tail_deformity")
    public Integer tailDeformity;

    @SerializedName("tail_deform")
    public Integer tailDeform;

    @SerializedName("fin_deform")
    public Integer finDeform;

    @SerializedName("wound_deform")
    public Integer woundsDeform;

    @SerializedName("hem_slight")
    public Integer hemSlight;

    @SerializedName("hem_spots")
    public Integer hemSpots;

    @SerializedName("hem_diffuse")
    public Integer hemDiffuse;

    @SerializedName("hem_wounds")
    public Integer hemWounds;

    @SerializedName("eye_blurred")
    public Integer eyeBlurred;

    @SerializedName("eye_cured")
    public Integer eyeCured;

    @SerializedName("eye_blind")
    public Integer eyeBlind;

    @SerializedName("eye_bleed")
    public Integer eyeBleed;

    @SerializedName("gill_mucus")
    public Integer gillMucus;

    @SerializedName("gill_bloody")
    public Integer gillBloody;

    @SerializedName("gill_brown")
    public Integer gillBrown;

    @SerializedName("gill_discolor")
    public Integer gillDiscolor;

    @SerializedName("head_deform")
    public Integer headDeform;
}
